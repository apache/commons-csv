/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Character set converter for text files.
 * Inspired by an iconv shell script.
 */
final class FileCharsetConverter {

    private FileCharsetConverter() {}

    public static long copyConvert(final File src, final Charset incs, final File dest, final Charset outcs, final ConversionMode conv)
            throws IOException {
        try (final InputStream input   = new FileInputStream(src);
             final OutputStream output = new FileOutputStream(dest)) {
           return copyConvert(input, incs, output, outcs, conv);
       }
    }

    public static long copyConvert(final InputStream input, final Charset incs, final OutputStream output, final Charset outcs, final ConversionMode conv)
            throws IOException {
        try (final Reader reader = new InputStreamReader(input, incs);
             final Writer writer = new OutputStreamWriter(output, outcs)) {
            return copyConvert(reader, writer, conv);
        }
    }

    public static long copyConvert(final Reader reader, final Writer writer, final ConversionMode conv) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null.");
        }
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null.");
        }
        if (conv == null) {
            throw new IllegalArgumentException("Conversion mode cannot be null.");
        }

        final int c = reader.read();
        if (IOUtils.EOF == c) {
            return 0L;
        }

        // Inline read/write filter for first char.
        long bytesWritten = 0L;
        switch (conv) {
            case DUP: // Duplicate without filtering.
                writer.write(c); // First data char.
                bytesWritten++;
                break;
            case NOBOM: // If input contains BOM then consume it.
                if (ByteOrderMark.UTF_BOM != c) {
                    writer.write(c); // First data char.
                    bytesWritten++;
                }
                break;
            case BOM: // If input does not contain BOM as first char then write it.
                if (ByteOrderMark.UTF_BOM != c) {
                    writer.write(ByteOrderMark.UTF_BOM);
                    bytesWritten++;
                }
                writer.write(c); // First data char or existing BOM.
                bytesWritten++;
                break;
            default:
                throw new IllegalArgumentException("Unsupported conversion mode: " + conv);
        }

        // Copy using default 4K char buffer size.
        bytesWritten += IOUtils.copyLarge(reader, writer);
        return bytesWritten;
    }

    public static int copyConvertToCharsets(final File srcFile, final Charset incs, final File destDir, final List<ConversionConfig> ccs)
            throws IOException {
        if (srcFile == null || srcFile.isDirectory()) {
            throw new IllegalArgumentException("Source file cannot be null and cannot be a directory.");
        }
        if (incs == null) {
            throw new IllegalArgumentException("Input charset cannot be null.");
        }
        if (destDir == null || !destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination dir cannot be null and must be a directory.");
        }
        if (ccs == null || ccs.isEmpty()) {
            throw new IllegalArgumentException("Conversion config cannot be null or empty.");
        }

        int converted = 0;
        for (final ConversionConfig conf : ccs) {
            final String fileName = conf.generator.fileName(srcFile, incs, conf);
            final File destFile = new File(destDir, fileName);
            //System.out.printf("Converting file: %s (%s) to %s (%s)%n", srcFile, incs.name(), destFile, conf.charset.name());
            copyConvert(srcFile, incs, destFile, conf.charset, conf.conv);
            converted++;
        }

        return converted;
    }

    private static final List<ConversionConfig> standardCharsets = Collections.unmodifiableList(Arrays.asList(new ConversionConfig[] {
        configOf(StandardCharsets.US_ASCII,   ConversionMode.NOBOM, nomodeGenerator()),
        configOf(StandardCharsets.ISO_8859_1, ConversionMode.NOBOM, nomodeGenerator()),
        configOf(StandardCharsets.UTF_8,      ConversionMode.NOBOM),
        configOf(StandardCharsets.UTF_8,      ConversionMode.BOM),
        // NOTE: UTF-16 is defined as Big Endian with BOM.
        // It will always be written with a two byte BOM sequence when using OutputStreamWriter through the underlying StreamEncoder/CharsetEncoder.
        // Simply strip any BOM from the base stream so that we don't create a file with two BOM sequences.
        configOf(StandardCharsets.UTF_16,     ConversionMode.NOBOM, nomodeGenerator()),
        configOf(StandardCharsets.UTF_16BE,   ConversionMode.NOBOM),
        configOf(StandardCharsets.UTF_16BE,   ConversionMode.BOM),
        configOf(StandardCharsets.UTF_16LE,   ConversionMode.NOBOM),
        configOf(StandardCharsets.UTF_16LE,   ConversionMode.BOM),
    }));

    public static int copyConvertToStandardCharsets(final File srcFile, final Charset incs, final File destDir)
            throws IOException {
        return copyConvertToCharsets(srcFile, incs, destDir, standardCharsets);
    }

    /**
     * Filter mode selector.
     */
    public static enum ConversionMode {
        /** Designates a duplication operation which will write all characters without filtering. */
        DUP,
        /** Designates that output should NOT contain a BOM char and that a BOM encountered as the first char of the input stream should be consumed. */
        NOBOM,
        /** Designates that the output should contain a BOM char. */
        BOM;
        private ConversionMode() {}
    }

    public static final ConversionConfig configOf(final Charset incs, final ConversionMode conv) {
        return new ConversionConfig(incs, conv, defaultGenerator());
    }

    public static final ConversionConfig configOf(final Charset incs, final ConversionMode conv, final FileNameGenerator generator) {
        return new ConversionConfig(incs, conv, generator);
    }

    /**
     * Configuration object for batch charset conversion.
     */
    public static final class ConversionConfig {
        private final Charset charset;
        private final ConversionMode conv;
        private final FileNameGenerator generator;

        private ConversionConfig(final Charset charset, final ConversionMode conv, final FileNameGenerator generator) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset cannot be null.");
            }
            if (conv == null) {
                throw new IllegalArgumentException("Conversion mode cannot be null.");
            }
            if (generator == null) {
                throw new IllegalArgumentException("File name generator cannot be null.");
            }
            this.charset = charset;
            this.conv = conv;
            this.generator = generator;
        }

        @Override
        public String toString() {
            return this.charset.name() + "-" + this.conv.name();
        }
    }

    /**
     * Plugable strategy to control destination filename generation.
     */
    public static interface FileNameGenerator {
        public String fileName(File srcFile, Charset incs, ConversionConfig conf);
    }

    public static FileNameGenerator defaultGenerator() {
        return new DefaultFileNameGenerator(false);
    }

    public static FileNameGenerator nomodeGenerator() {
        return new DefaultFileNameGenerator(true);
    }

    /**
     * Default strategy which will generate filenames in the form of:
     * {basename}-{outcharset}[-{mode}][.{ext}]
     */
    private static final class DefaultFileNameGenerator implements FileNameGenerator {
        private final boolean nomode;

        private DefaultFileNameGenerator(final boolean nomode) {
            this.nomode = nomode;
        }

        @Override
        public String fileName(final File srcFile, final Charset incs, final ConversionConfig conf) {
            final String baseName = FilenameUtils.getBaseName(srcFile.getName());
            final String ext = FilenameUtils.getExtension(srcFile.getName());
            final String mode = (this.nomode) ? "" : "-" + conf.conv.name();
            final String fileName = String.format("%s-%s%s%s", baseName, conf.charset.name(), mode, (ext == null) ? "" : "." + ext);
            return fileName;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 2) {
            System.err.printf("Usage: java %s <in-charset> <out-charset> [in-file]%n", FileCharsetConverter.class.getName());
            return;
        }

        final Charset csin = Charset.forName(args[0]);
        final Charset csout = Charset.forName(args[1]);

        try (final InputStream in = (args.length > 2) ? new FileInputStream(new File(args[2])) : System.in) {
            copyConvert(in, csin, System.out, csout, ConversionMode.DUP);
        }
    }

}
