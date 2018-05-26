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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.FileCharsetConverter.ConversionConfig;
import org.apache.commons.csv.FileCharsetConverter.ConversionMode;
import org.apache.commons.csv.FileCharsetConverter.FileNameGenerator;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * File encoding test case for all java standard character sets.
 */
public class CSVParserCharsetTest {

    private static final ByteOrderMark NOBOM  = null;

    private static final boolean STRIP_BOM    = true;
    private static final boolean NOSTRIP_BOM  = false;

    private static final String MODE_SUFFIX   = null;
    private static final String NOMODE_SUFFIX = "";

    private static File baseFile;

    @BeforeClass
    public static void init() throws Exception {
        // NOTE: The base cstest.csv file uses charset UTF-8 without BOM and is exactly
        // 500 bytes in size.  This makes conversion from 8-bit to 16-bit and 32-bit charsets
        // and the 2-4 byte bom sequences show up nicely in the ls output.
        baseFile = new File(ClassLoader.getSystemClassLoader().getResource("CSVFileParser/cstest.csv").toURI());
        final Charset baseCharset = StandardCharsets.UTF_8;
        final File destDir = new File(ClassLoader.getSystemClassLoader().getResource("CSVFileParser").toURI());

        final FileNameGenerator nomodeGenerator = FileCharsetConverter.nomodeGenerator();
        final List<ConversionConfig> conf = Collections.unmodifiableList(Arrays.asList(new ConversionConfig[] {
            FileCharsetConverter.configOf(StandardCharsets.US_ASCII,   ConversionMode.NOBOM, nomodeGenerator),
            FileCharsetConverter.configOf(StandardCharsets.ISO_8859_1, ConversionMode.NOBOM, nomodeGenerator),
            FileCharsetConverter.configOf(StandardCharsets.UTF_8,      ConversionMode.NOBOM),
            FileCharsetConverter.configOf(StandardCharsets.UTF_8,      ConversionMode.BOM),
            // NOTE: UTF-16 is defined as Big Endian with BOM.
            // It will always be written with a two byte BOM sequence when using OutputStreamWriter through the underlying StreamEncoder/CharsetEncoder.
            // Simply strip any BOM from the base stream so that we don't create a file with two BOM sequences.
            FileCharsetConverter.configOf(StandardCharsets.UTF_16,     ConversionMode.NOBOM, nomodeGenerator),
            FileCharsetConverter.configOf(StandardCharsets.UTF_16BE,   ConversionMode.NOBOM),
            FileCharsetConverter.configOf(StandardCharsets.UTF_16BE,   ConversionMode.BOM),
            FileCharsetConverter.configOf(StandardCharsets.UTF_16LE,   ConversionMode.NOBOM),
            FileCharsetConverter.configOf(StandardCharsets.UTF_16LE,   ConversionMode.BOM),
            // NOTE: UTF-32 is defined as Big Endian without BOM.
            FileCharsetConverter.configOf(Charset.forName("UTF-32"),   ConversionMode.NOBOM, nomodeGenerator),
            FileCharsetConverter.configOf(Charset.forName("UTF-32BE"), ConversionMode.NOBOM),
            FileCharsetConverter.configOf(Charset.forName("UTF-32BE"), ConversionMode.BOM),
            FileCharsetConverter.configOf(Charset.forName("UTF-32LE"), ConversionMode.NOBOM),
            FileCharsetConverter.configOf(Charset.forName("UTF-32LE"), ConversionMode.BOM),
        }));

        FileCharsetConverter.copyConvertToCharsets(baseFile, baseCharset, destDir, conf);
    }

    @Test
    public void testCharset_US_ASCII() throws IOException {
        doTestCharset(StandardCharsets.US_ASCII, CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, NOMODE_SUFFIX);
    }

    @Test
    public void testCharset_ISO_8859_1() throws IOException {
        doTestCharset(StandardCharsets.ISO_8859_1, CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, NOMODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_8_NOBOM() throws IOException {
        doTestCharset(StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test
    @Ignore
    public void testCharset_UTF_8_BOM() throws IOException {
        doTestCharset(StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_8, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_8_BOM_strip() throws IOException {
        doTestCharset(StandardCharsets.UTF_8, CSVFormat.EXCEL.withHeader(), ByteOrderMark.UTF_8, STRIP_BOM, MODE_SUFFIX);
    }

    //NOTE: UTF-16 works fine since the StreamDecoder/CharsetDecoder automatically strips the BOM.
    @Test
    public void testCharset_UTF_16_BOM() throws IOException {
        doTestCharset(StandardCharsets.UTF_16, CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_16BE, NOSTRIP_BOM, NOMODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_16_BOM_strip() throws IOException {
        doTestCharset(StandardCharsets.UTF_16, CSVFormat.EXCEL.withHeader(), ByteOrderMark.UTF_16BE, STRIP_BOM, NOMODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_16BE() throws IOException {
        doTestCharset(StandardCharsets.UTF_16BE, CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test
    @Ignore
    public void testCharset_UTF_16BE_BOM() throws IOException {
        doTestCharset(StandardCharsets.UTF_16BE, CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_16BE, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_16BE_BOM_strip() throws IOException {
        doTestCharset(StandardCharsets.UTF_16BE, CSVFormat.EXCEL.withHeader(), ByteOrderMark.UTF_16BE, STRIP_BOM, MODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_16LE() throws IOException {
        doTestCharset(StandardCharsets.UTF_16LE, CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test
    @Ignore
    public void testCharset_UTF_16LE_BOM() throws IOException {
        doTestCharset(StandardCharsets.UTF_16LE, CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_16LE, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_16LE_BOM_strip() throws IOException {
        doTestCharset(StandardCharsets.UTF_16LE, CSVFormat.EXCEL.withHeader(), ByteOrderMark.UTF_16LE, STRIP_BOM, MODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_32_NOBOM() throws IOException {
        doTestCharset(Charset.forName("UTF-32"), CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, NOMODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_32BE() throws IOException {
        doTestCharset(Charset.forName("UTF-32BE"), CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, MODE_SUFFIX);
    }

    //NOTE: UTF-32BE-BOM works fine since the StreamDecoder/CharsetDecoder automatically strips the BOM.
    @Test
    public void testCharset_UTF_32BE_BOM() throws IOException {
        doTestCharset(Charset.forName("UTF-32BE"), CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_32BE, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_32BE_BOM_strip() throws IOException {
        doTestCharset(Charset.forName("UTF-32BE"), CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_32BE, STRIP_BOM, MODE_SUFFIX);
    }

    @Test
    public void testCharset_UTF_32LE() throws IOException {
        doTestCharset(Charset.forName("UTF-32LE"), CSVFormat.DEFAULT.withHeader(), NOBOM, NOSTRIP_BOM, MODE_SUFFIX);
    }

    //NOTE: UTF-32LE-BOM works fine since the StreamDecoder/CharsetDecoder automatically strips the BOM.
    @Test
    public void testCharset_UTF_32LE_BOM() throws IOException {
        doTestCharset(Charset.forName("UTF-32LE"), CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_32LE, NOSTRIP_BOM, MODE_SUFFIX);
    }

    @Test(/*CSV-107*/)
    public void testCharset_UTF_32LE_BOM_strip() throws IOException {
        doTestCharset(Charset.forName("UTF-32LE"), CSVFormat.DEFAULT.withHeader(), ByteOrderMark.UTF_32LE, STRIP_BOM, MODE_SUFFIX);
    }

    /**
     * Driver method for encoding tests.
     * Expects to find a filename in the form of:
     * cstest-{outcharset}[-(BOM|NOBOM)].csv
     *
     * @param charset The charset of the input file.
     * @param format Format options used during parsing.
     * @param bom The BOM byte sequence in the first 2-4 bytes.  This should be null for input files without BOM.
     * @param stripBOM Whether the BOM sequence should stripped off by inserting BOMInputStream ahead of CSVParser.
     * This has no effect if the bom param is null.
     * @param suffix The filename suffix used to designate Unicode BOM/NOBOM variants.
     * @throws IOException
     */
    private static void doTestCharset(final Charset charset, final CSVFormat format, final ByteOrderMark bom, final boolean stripBOM, final String suffix) throws IOException {
        final String dirName = baseFile.getParentFile().getName();
        final String baseName = FilenameUtils.getBaseName(baseFile.getName());
        final String suffixStr = (suffix == null) ? ((bom == null) ? "-NOBOM" : "-BOM") : suffix;
        final String path = String.format("%s/%s-%s%s.csv", dirName, baseName, charset.name(), suffixStr);
        final URL url = ClassLoader.getSystemClassLoader().getResource(path);
        final String[] row1data = new String[] { "14-Mar-14", "223.80", "225.20", "220.85", "221.51", "79448141" };
        try (final InputStream input = (bom != null && stripBOM) ? new BOMInputStream(url.openStream(), bom) : url.openStream();
                final CSVParser parser = CSVParser.parse(input, charset, format)) {
            for (final CSVRecord record : parser) {
                Assert.assertNotNull(record.get("Date"));
                Assert.assertNotNull(record.get("Volume"));
                if (parser.getRecordNumber() == 1) {
                    Assert.assertArrayEquals(row1data, record.values());
                }
            }
        }
    }

}
