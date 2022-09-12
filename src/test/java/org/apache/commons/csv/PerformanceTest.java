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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Basic test harness.
 */
@SuppressWarnings("boxing")
public class PerformanceTest {

    @FunctionalInterface
    private interface CSVParserFactory {
        CSVParser createParser() throws IOException;
    }

    // Container for basic statistics
    private static class Stats {
        final int count;
        final int fields;
        Stats(final int c, final int f) {
            count = c;
            fields = f;
        }
    }

    private static final String[] PROPERTY_NAMES = {
        "java.version",                  // Java Runtime Environment version
        "java.vendor",                   // Java Runtime Environment vendor
//        "java.vm.specification.version", // Java Virtual Machine specification version
//        "java.vm.specification.vendor",  // Java Virtual Machine specification vendor
//        "java.vm.specification.name",    // Java Virtual Machine specification name
        "java.vm.version",               // Java Virtual Machine implementation version
//        "java.vm.vendor",                // Java Virtual Machine implementation vendor
        "java.vm.name",                  // Java Virtual Machine implementation name
//        "java.specification.version",    // Java Runtime Environment specification version
//        "java.specification.vendor",     // Java Runtime Environment specification vendor
//        "java.specification.name",       // Java Runtime Environment specification name

        "os.name",                       // Operating system name
        "os.arch",                       // Operating system architecture
        "os.version",                    // Operating system version
    };
    private static int max = 11; // skip first test

    private static int num; // number of elapsed times recorded

    private static final long[] ELAPSED_TIMES = new long[max];
    private static final CSVFormat format = CSVFormat.EXCEL;

    private static final String TEST_RESRC = "org/apache/commons/csv/perf/worldcitiespop.txt.gz";

    private static final File BIG_FILE = new File(System.getProperty("java.io.tmpdir"), "worldcitiespop.txt");

    private static Reader createReader() throws IOException {
        return new InputStreamReader(new FileInputStream(BIG_FILE), StandardCharsets.ISO_8859_1);
    }

    private static Lexer createTestCSVLexer(final String test, final ExtendedBufferedReader input)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        return test.startsWith("CSVLexer") ? getLexerCtor(test).newInstance(format, input) : new Lexer(format, input);
    }

    private static Constructor<Lexer> getLexerCtor(final String clazz) throws Exception {
        @SuppressWarnings("unchecked")
        final Class<Lexer> lexer = (Class<Lexer>) Class.forName("org.apache.commons.csv." + clazz);
        return lexer.getConstructor(CSVFormat.class, ExtendedBufferedReader.class);
    }

    private static Stats iterate(final Iterable<CSVRecord> iterable) {
        int count = 0;
        int fields = 0;
        for (final CSVRecord record : iterable) {
            count++;
            fields += record.size();
        }
        return new Stats(count, fields);
    }

    public static void main(final String [] args) throws Exception {
        if (BIG_FILE.exists()) {
            System.out.printf("Found test fixture %s: %,d bytes.%n", BIG_FILE, BIG_FILE.length());
        } else {
          System.out.println("Decompressing test fixture to: " + BIG_FILE + "...");
          try (
              final InputStream input = new GZIPInputStream(
                  PerformanceTest.class.getClassLoader().getResourceAsStream(TEST_RESRC));
              final OutputStream output = new FileOutputStream(BIG_FILE)) {
              IOUtils.copy(input, output);
              System.out.println(String.format("Decompressed test fixture %s: %,d bytes.", BIG_FILE, BIG_FILE.length()));
          }
        }
        final int argc = args.length;
        if (argc > 0) {
            max = Integer.parseInt(args[0]);
        }

        final String[] tests;
        if (argc > 1) {
            tests = new String[argc - 1];
            System.arraycopy(args, 1, tests, 0, argc - 1);
        } else {
            tests = new String[] { "file", "split", "extb", "exts", "csv", "csv-path", "csv-path-db", "csv-url", "lexreset", "lexnew" };
        }
        for (final String p : PROPERTY_NAMES) {
            System.out.printf("%s=%s%n", p, System.getProperty(p));
        }
        System.out.printf("Max count: %d%n%n", max);

        for (final String test : tests) {
            if ("file".equals(test)) {
                testReadBigFile(false);
            } else if ("split".equals(test)) {
                testReadBigFile(true);
            } else if ("csv".equals(test)) {
                testParseCommonsCSV();
            } else if ("csv-path".equals(test)) {
                testParsePath();
            } else if ("csv-path-db".equals(test)) {
                testParsePathDoubleBuffering();
            } else if ("csv-url".equals(test)) {
                testParseURL();
            } else if ("lexreset".equals(test)) {
                testCSVLexer(false, test);
            } else if ("lexnew".equals(test)) {
                testCSVLexer(true, test);
            } else if (test.startsWith("CSVLexer")) {
                testCSVLexer(false, test);
            } else if ("extb".equals(test)) {
                testExtendedBuffer(false);
            } else if ("exts".equals(test)) {
                testExtendedBuffer(true);
            } else {
                System.out.printf("Invalid test name: %s%n", test);
            }
        }
    }

    private static Stats readAll(final BufferedReader in, final boolean split) throws IOException {
        int count = 0;
        int fields = 0;
        String record;
        while ((record = in.readLine()) != null) {
            count++;
            fields += split ? record.split(",").length : 1;
        }
        return new Stats(count, fields);
    }

    // calculate and show average
    private static void show(){
        if (num > 1) {
            long tot = 0;
            for (int i = 1; i < num; i++) { // skip first test
                tot += ELAPSED_TIMES[i];
            }
            System.out.printf("%-20s: %5dms%n%n", "Average(not first)", tot / (num - 1));
        }
        num = 0; // ready for next set
    }

    // Display end stats; store elapsed for average
    private static void show(final String msg, final Stats s, final long start) {
        final long elapsed = System.currentTimeMillis() - start;
        System.out.printf("%-20s: %5dms %d lines %d fields%n", msg, elapsed, s.count, s.fields);
        ELAPSED_TIMES[num] = elapsed;
        num++;
    }

    private static void testCSVLexer(final boolean newToken, final String test) throws Exception {
        Token token = new Token();
        String dynamic = "";
        for (int i = 0; i < max; i++) {
            final String simpleName;
            final Stats stats;
            final long startMillis;
            try (final ExtendedBufferedReader input = new ExtendedBufferedReader(createReader());
                    final Lexer lexer = createTestCSVLexer(test, input)) {
                if (test.startsWith("CSVLexer")) {
                    dynamic = "!";
                }
                simpleName = lexer.getClass().getSimpleName();
                int count = 0;
                int fields = 0;
                startMillis = System.currentTimeMillis();
                do {
                    if (newToken) {
                        token = new Token();
                    } else {
                        token.reset();
                    }
                    lexer.nextToken(token);
                    switch (token.type) {
                    case EOF:
                        break;
                    case EORECORD:
                        fields++;
                        count++;
                        break;
                    case INVALID:
                        throw new IOException("invalid parse sequence <" + token.content.toString() + ">");
                    case TOKEN:
                        fields++;
                        break;
                    case COMMENT: // not really expecting these
                        break;
                    default:
                        throw new IllegalStateException("Unexpected Token type: " + token.type);
                    }
                } while (!token.type.equals(Token.Type.EOF));
                stats = new Stats(count, fields);
            }
            show(simpleName + dynamic + " " + (newToken ? "new" : "reset"), stats, startMillis);
        }
        show();
    }

    private static void testExtendedBuffer(final boolean makeString) throws Exception {
        for (int i = 0; i < max; i++) {
            int fields = 0;
            int lines = 0;
            final long startMillis;
            try (final ExtendedBufferedReader in = new ExtendedBufferedReader(createReader())) {
                startMillis = System.currentTimeMillis();
                int read;
                if (makeString) {
                    StringBuilder sb = new StringBuilder();
                    while ((read = in.read()) != -1) {
                        sb.append((char) read);
                        if (read == ',') { // count delimiters
                            sb.toString();
                            sb = new StringBuilder();
                            fields++;
                        } else if (read == '\n') {
                            sb.toString();
                            sb = new StringBuilder();
                            lines++;
                        }
                    }
                } else {
                    while ((read = in.read()) != -1) {
                        if (read == ',') { // count delimiters
                            fields++;
                        } else if (read == '\n') {
                            lines++;
                        }
                    }
                }
                fields += lines; // EOL is a delimiter too
            }
            show("Extended" + (makeString ? " toString" : ""), new Stats(lines, fields), startMillis);
        }
        show();
    }

    private static void testParseCommonsCSV() throws Exception {
        testParser("CSV", () -> new CSVParser(createReader(), format));
    }

    private static void testParsePath() throws Exception {
        testParser("CSV-PATH", () -> CSVParser.parse(Files.newInputStream(Paths.get(BIG_FILE.toURI())), StandardCharsets.ISO_8859_1, format));
    }

    private static void testParsePathDoubleBuffering() throws Exception {
        testParser("CSV-PATH-DB", () -> CSVParser.parse(Files.newBufferedReader(Paths.get(BIG_FILE.toURI()), StandardCharsets.ISO_8859_1), format));
    }

    private static void testParser(final String msg, final CSVParserFactory fac) throws Exception {
        for (int i = 0; i < max; i++) {
            final long startMillis;
            final Stats stats;
            try (final CSVParser parser = fac.createParser()) {
                startMillis = System.currentTimeMillis();
                stats = iterate(parser);
            }
            show(msg, stats, startMillis);
        }
        show();
    }

    private static void testParseURL() throws Exception {
        testParser("CSV-URL", () -> CSVParser.parse(BIG_FILE.toURI().toURL(), StandardCharsets.ISO_8859_1, format));
    }

    private static void testReadBigFile(final boolean split) throws Exception {
        for (int i = 0; i < max; i++) {
            final long startMillis;
            final Stats stats;
            try (final BufferedReader in = new BufferedReader(createReader())) {
                startMillis = System.currentTimeMillis();
                stats = readAll(in, split);
            }
            show(split ? "file+split" : "file", stats, startMillis);
        }
        show();
    }

}