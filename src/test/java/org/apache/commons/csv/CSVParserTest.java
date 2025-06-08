/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link CSVParser}.
 *
 * The test are organized in three different sections: The 'setter/getter' section, the lexer section and finally the parser section. In case a test fails, you
 * should follow a top-down approach for fixing a potential bug (its likely that the parser itself fails if the lexer has problems...).
 */
class CSVParserTest {

    private static final CSVFormat EXCEL_WITH_HEADER = CSVFormat.EXCEL.withHeader();

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String UTF_8_NAME = UTF_8.name();

    private static final String CSV_INPUT = "a,b,c,d\n" + " a , b , 1 2 \n" + "\"foo baar\", b,\n" +
            // + " \"foo\n,,\n\"\",,\n\\\"\",d,e\n";
            "   \"foo\n,,\n\"\",,\n\"\"\",d,e\n"; // changed to use standard CSV escaping

    private static final String CSV_INPUT_1 = "a,b,c,d";

    private static final String CSV_INPUT_2 = "a,b,1 2";

    private static final String[][] RESULT = { { "a", "b", "c", "d" }, { "a", "b", "1 2" }, { "foo baar", "b", "" }, { "foo\n,,\n\",,\n\"", "d", "e" } };

    // CSV with no header comments
    private static final String CSV_INPUT_NO_COMMENT = "A,B" + CRLF + "1,2" + CRLF;

    // CSV with a header comment
    private static final String CSV_INPUT_HEADER_COMMENT = "# header comment" + CRLF + "A,B" + CRLF + "1,2" + CRLF;

    // CSV with a single line header and trailer comment
    private static final String CSV_INPUT_HEADER_TRAILER_COMMENT = "# header comment" + CRLF + "A,B" + CRLF + "1,2" + CRLF + "# comment";

    // CSV with a multi-line header and trailer comment
    private static final String CSV_INPUT_MULTILINE_HEADER_TRAILER_COMMENT = "# multi-line" + CRLF + "# header comment" + CRLF + "A,B" + CRLF + "1,2" + CRLF +
            "# multi-line" + CRLF + "# comment";

    // Format with auto-detected header
    private static final CSVFormat FORMAT_AUTO_HEADER = CSVFormat.Builder.create(CSVFormat.DEFAULT).setCommentMarker('#').setHeader().get();

    // Format with explicit header
    // @formatter:off
    private static final CSVFormat FORMAT_EXPLICIT_HEADER = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setSkipHeaderRecord(true)
            .setCommentMarker('#')
            .setHeader("A", "B")
            .get();
    // @formatter:on

    // Format with explicit header that does not skip the header line
    // @formatter:off
    CSVFormat FORMAT_EXPLICIT_HEADER_NOSKIP = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setCommentMarker('#')
            .setHeader("A", "B")
            .get();
    // @formatter:on

    @SuppressWarnings("resource") // caller releases
    private BOMInputStream createBOMInputStream(final String resource) throws IOException {
        return new BOMInputStream(ClassLoader.getSystemClassLoader().getResource(resource).openStream());
    }

    CSVRecord parse(final CSVParser parser, final int failParseRecordNo) throws IOException {
        if (parser.getRecordNumber() + 1 == failParseRecordNo) {
            assertThrows(IOException.class, () -> parser.nextRecord());
            return null;
        }
        return parser.nextRecord();
    }

    private void parseFully(final CSVParser parser) {
        parser.forEach(Assertions::assertNotNull);
    }

    @Test
    void testBackslashEscaping() throws IOException {
        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        // @formatter:off
        final String code = "one,two,three\n" + // 0
            "'',''\n" + // 1) empty encapsulators
            "/',/'\n" + // 2) single encapsulators
            "'/'','/''\n" + // 3) single encapsulators encapsulated via escape
            "'''',''''\n" + // 4) single encapsulators encapsulated via doubling
            "/,,/,\n" + // 5) separator escaped
            "//,//\n" + // 6) escape escaped
            "'//','//'\n" + // 7) escape escaped in encapsulation
            "   8   ,   \"quoted \"\" /\" // string\"   \n" + // don't eat spaces
            "9,   /\n   \n" + // escaped newline
            "";
        final String[][] res = {{"one", "two", "three"}, // 0
            {"", ""}, // 1
            {"'", "'"}, // 2
            {"'", "'"}, // 3
            {"'", "'"}, // 4
            {",", ","}, // 5
            {"/", "/"}, // 6
            {"/", "/"}, // 7
            {"   8   ", "   \"quoted \"\" /\" / string\"   "}, {"9", "   \n   "} };
        // @formatter:on
        final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'').withRecordSeparator(CRLF).withEscape('/').withIgnoreEmptyLines();
        try (CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Records do not match expected result", res, records, -1);
        }
    }

    @Test
    void testBackslashEscaping2() throws IOException {
        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.
        // @formatter:off
        final String code = "" + " , , \n" + // 1)
            " \t ,  , \n" + // 2)
            " // , /, , /,\n" + // 3)
            "";
        final String[][] res = {{" ", " ", " "}, // 1
            {" \t ", "  ", " "}, // 2
            {" / ", " , ", " ,"}, // 3
        };
        // @formatter:on
        final CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(CRLF).withEscape('/').withIgnoreEmptyLines();
        try (CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("", res, records, -1);
        }
    }

    @Test
    @Disabled
    void testBackslashEscapingOld() throws IOException {
        final String code = "one,two,three\n" + "on\\\"e,two\n" + "on\"e,two\n" + "one,\"tw\\\"o\"\n" + "one,\"t\\,wo\"\n" + "one,two,\"th,ree\"\n" +
                "\"a\\\\\"\n" + "a\\,b\n" + "\"a\\\\,b\"";
        final String[][] res = { { "one", "two", "three" }, { "on\\\"e", "two" }, { "on\"e", "two" }, { "one", "tw\"o" }, { "one", "t\\,wo" }, // backslash in
                                                                                                                                               // quotes only
                                                                                                                                               // escapes a
                                                                                                                                               // delimiter
                                                                                                                                               // (",")
                { "one", "two", "th,ree" }, { "a\\\\" }, // backslash in quotes only escapes a delimiter (",")
                { "a\\", "b" }, // a backslash must be returned
                { "a\\\\,b" } // backslash in quotes only escapes a delimiter (",")
        };
        try (CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

    @Test
    @Disabled("CSV-107")
    void testBOM() throws IOException {
        final URL url = ClassLoader.getSystemClassLoader().getResource("org/apache/commons/csv/CSVFileParser/bom.csv");
        try (CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, EXCEL_WITH_HEADER)) {
            parser.forEach(record -> assertNotNull(record.get("Date")));
        }
    }

    @Test
    void testBOMInputStreamParserWithInputStream() throws IOException {
        try (BOMInputStream inputStream = createBOMInputStream("org/apache/commons/csv/CSVFileParser/bom.csv");
                CSVParser parser = CSVParser.parse(inputStream, UTF_8, EXCEL_WITH_HEADER)) {
            parser.forEach(record -> assertNotNull(record.get("Date")));
        }
    }

    @Test
    void testBOMInputStreamParserWithReader() throws IOException {
        try (Reader reader = new InputStreamReader(createBOMInputStream("org/apache/commons/csv/CSVFileParser/bom.csv"), UTF_8_NAME);
                CSVParser parser = CSVParser.builder()
                        .setReader(reader)
                        .setFormat(EXCEL_WITH_HEADER)
                        .get()) {
            parser.forEach(record -> assertNotNull(record.get("Date")));
        }
    }

    @Test
    void testBOMInputStreamParseWithReader() throws IOException {
        try (Reader reader = new InputStreamReader(createBOMInputStream("org/apache/commons/csv/CSVFileParser/bom.csv"), UTF_8_NAME);
                CSVParser parser = CSVParser.builder()
                        .setReader(reader)
                        .setFormat(EXCEL_WITH_HEADER)
                        .get()) {
            parser.forEach(record -> assertNotNull(record.get("Date")));
        }
    }

    @Test
    void testCarriageReturnEndings() throws IOException {
        final String string = "foo\rbaar,\rhello,world\r,kanu";
        try (CSVParser parser = CSVParser.builder().setCharSequence(string).get()) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

    @Test
    void testCarriageReturnLineFeedEndings() throws IOException {
        final String string = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        try (CSVParser parser = CSVParser.builder().setCharSequence(string).get()) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

    @Test
    void testClose() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records;
        try (CSVParser parser = CSVFormat.DEFAULT.withCommentMarker('#').withHeader().parse(in)) {
            records = parser.iterator();
            assertTrue(records.hasNext());
        }
        assertFalse(records.hasNext());
        assertThrows(NoSuchElementException.class, records::next);
    }

    @Test
    void testCSV141CSVFormat_DEFAULT() throws Exception {
        testCSV141Failure(CSVFormat.DEFAULT, 3);
    }

    @Test
    void testCSV141CSVFormat_INFORMIX_UNLOAD() throws Exception {
        testCSV141Failure(CSVFormat.INFORMIX_UNLOAD, 1);
    }

    @Test
    void testCSV141CSVFormat_INFORMIX_UNLOAD_CSV() throws Exception {
        testCSV141Failure(CSVFormat.INFORMIX_UNLOAD_CSV, 3);
    }

    @Test
    void testCSV141CSVFormat_ORACLE() throws Exception {
        testCSV141Failure(CSVFormat.ORACLE, 2);
    }

    @Test
    void testCSV141CSVFormat_POSTGRESQL_CSV() throws Exception {
        testCSV141Failure(CSVFormat.POSTGRESQL_CSV, 3);
    }

    @Test
    void testCSV141Excel() throws Exception {
        testCSV141Ok(CSVFormat.EXCEL);
    }

    private void testCSV141Failure(final CSVFormat format, final int failParseRecordNo) throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/csv/CSV-141/csv-141.csv");
        try (CSVParser parser = CSVParser.parse(path, StandardCharsets.UTF_8, format)) {
            // row 1
            CSVRecord record = parse(parser, failParseRecordNo);
            if (record == null) {
                return; // expected failure
            }
            assertEquals("1414770317901", record.get(0));
            assertEquals("android.widget.EditText", record.get(1));
            assertEquals("pass sem1 _84*|*", record.get(2));
            assertEquals("0", record.get(3));
            assertEquals("pass sem1 _8", record.get(4));
            assertEquals(5, record.size());
            // row 2
            record = parse(parser, failParseRecordNo);
            if (record == null) {
                return; // expected failure
            }
            assertEquals("1414770318470", record.get(0));
            assertEquals("android.widget.EditText", record.get(1));
            assertEquals("pass sem1 _84:|", record.get(2));
            assertEquals("0", record.get(3));
            assertEquals("pass sem1 _84:\\", record.get(4));
            assertEquals(5, record.size());
            // row 3: Fail for certain
            assertThrows(IOException.class, () -> parser.nextRecord());
        }
    }

    private void testCSV141Ok(final CSVFormat format) throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/csv/CSV-141/csv-141.csv");
        try (CSVParser parser = CSVParser.parse(path, StandardCharsets.UTF_8, format)) {
            // row 1
            CSVRecord record = parser.nextRecord();
            assertEquals("1414770317901", record.get(0));
            assertEquals("android.widget.EditText", record.get(1));
            assertEquals("pass sem1 _84*|*", record.get(2));
            assertEquals("0", record.get(3));
            assertEquals("pass sem1 _8", record.get(4));
            assertEquals(5, record.size());
            // row 2
            record = parser.nextRecord();
            assertEquals("1414770318470", record.get(0));
            assertEquals("android.widget.EditText", record.get(1));
            assertEquals("pass sem1 _84:|", record.get(2));
            assertEquals("0", record.get(3));
            assertEquals("pass sem1 _84:\\", record.get(4));
            assertEquals(5, record.size());
            // row 3
            record = parser.nextRecord();
            assertEquals("1414770318327", record.get(0));
            assertEquals("android.widget.EditText", record.get(1));
            assertEquals("pass sem1\n1414770318628\"", record.get(2));
            assertEquals("android.widget.EditText", record.get(3));
            assertEquals("pass sem1 _84*|*", record.get(4));
            assertEquals("0", record.get(5));
            assertEquals("pass sem1\n", record.get(6));
            assertEquals(7, record.size());
            // EOF
            record = parser.nextRecord();
            assertNull(record);
        }
    }

    @Test
    void testCSV141RFC4180() throws Exception {
        testCSV141Failure(CSVFormat.RFC4180, 3);
    }

    @Test
    void testCSV235() throws IOException {
        final String dqString = "\"aaa\",\"b\"\"bb\",\"ccc\""; // "aaa","b""bb","ccc"
        try (CSVParser parser = CSVFormat.RFC4180.parse(new StringReader(dqString))) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertFalse(records.hasNext());
            assertEquals(3, record.size());
            assertEquals("aaa", record.get(0));
            assertEquals("b\"bb", record.get(1));
            assertEquals("ccc", record.get(2));
        }
    }

    @Test
    void testCSV57() throws Exception {
        try (CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            final List<CSVRecord> list = parser.getRecords();
            assertNotNull(list);
            assertEquals(0, list.size());
        }
    }

    @Test
    void testDefaultFormat() throws IOException {
        // @formatter:off
        final String code = "" + "a,b#\n" + // 1)
            "\"\n\",\" \",#\n" +            // 2)
            "#,\"\"\n" +                    // 3)
            "# Final comment\n"             // 4)
        ;
        // @formatter:on
        final String[][] res = { { "a", "b#" }, { "\n", " ", "#" }, { "#", "" }, { "# Final comment" } };
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isCommentMarkerSet());
        final String[][] resComments = { { "a", "b#" }, { "\n", " ", "#" } };
        try (CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Failed to parse without comments", res, records, -1);
            format = CSVFormat.DEFAULT.withCommentMarker('#');
        }
        try (CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            Utils.compare("Failed to parse with comments", resComments, records, -1);
        }
    }

    @Test
    void testDuplicateHeadersAllowedByDefault() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader())) {
            // noop
        }
    }

    @Test
    void testDuplicateHeadersNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader().withAllowDuplicateHeaderNames(false)));
    }

    @Test
    void testEmptyFile() throws Exception {
        try (CSVParser parser = CSVParser.parse(Paths.get("src/test/resources/org/apache/commons/csv/empty.txt"), StandardCharsets.UTF_8,
                CSVFormat.DEFAULT)) {
            assertNull(parser.nextRecord());
        }
    }

    @Test
    void testEmptyFileHeaderParsing() throws Exception {
        try (CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            assertNull(parser.nextRecord());
            assertTrue(parser.getHeaderNames().isEmpty());
        }
    }

    @Test
    void testEmptyLineBehaviorCSV() throws Exception {
        final String[] codes = { "hello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n", "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" } // CSV format ignores empty lines
        };
        for (final String code : codes) {
            try (CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

    @Test
    void testEmptyLineBehaviorExcel() throws Exception {
        final String[] codes = { "hello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n", "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" }, { "" }, // Excel format does not ignore empty lines
                { "" } };
        for (final String code : codes) {
            try (CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

    @Test
    void testEmptyString() throws Exception {
        try (CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            assertNull(parser.nextRecord());
        }
    }

    @Test
    void testEndOfFileBehaviorCSV() throws Exception {
        final String[] codes = { "hello,\r\n\r\nworld,\r\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\r\n", "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\n", "hello,\r\n\r\nworld,\"\"" };
        final String[][] res = { { "hello", "" }, // CSV format ignores empty lines
                { "world", "" } };
        for (final String code : codes) {
            try (CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

    @Test
    void testEndOfFileBehaviorExcel() throws Exception {
        final String[] codes = { "hello,\r\n\r\nworld,\r\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\r\n", "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\n", "hello,\r\n\r\nworld,\"\"" };
        final String[][] res = { { "hello", "" }, { "" }, // Excel format does not ignore empty lines
                { "world", "" } };

        for (final String code : codes) {
            try (CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

    @Test
    void testExcelFormat1() throws IOException {
        final String code = "value1,value2,value3,value4\r\na,b,c,d\r\n  x,,," + "\r\n\r\n\"\"\"hello\"\"\",\"  \"\"world\"\"\",\"abc\ndef\",\r\n";
        final String[][] res = { { "value1", "value2", "value3", "value4" }, { "a", "b", "c", "d" }, { "  x", "", "", "" }, { "" },
                { "\"hello\"", "  \"world\"", "abc\ndef", "" } };
        try (CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

    @Test
    void testExcelFormat2() throws Exception {
        final String code = "foo,baar\r\n\r\nhello,\r\n\r\nworld,\r\n";
        final String[][] res = { { "foo", "baar" }, { "" }, { "hello", "" }, { "" }, { "world", "" } };
        try (CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

    /**
     * Tests an exported Excel worksheet with a header row and rows that have more columns than the headers
     */
    @Test
    void testExcelHeaderCountLessThanData() throws Exception {
        final String code = "A,B,C,,\r\na,b,c,d,e\r\n";
        try (CSVParser parser = CSVParser.parse(code, EXCEL_WITH_HEADER)) {
            parser.getRecords().forEach(record -> {
                assertEquals("a", record.get("A"));
                assertEquals("b", record.get("B"));
                assertEquals("c", record.get("C"));
            });
        }
    }

    @Test
    void testFirstEndOfLineCr() throws IOException {
        final String data = "foo\rbaar,\rhello,world\r,kanu";
        try (CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\r", parser.getFirstEndOfLine());
        }
    }

    @Test
    void testFirstEndOfLineCrLf() throws IOException {
        final String data = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        try (CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\r\n", parser.getFirstEndOfLine());
        }
    }

    @Test
    void testFirstEndOfLineLf() throws IOException {
        final String data = "foo\nbaar,\nhello,world\n,kanu";
        try (CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\n", parser.getFirstEndOfLine());
        }
    }

    @Test
    void testForEach() throws Exception {
        try (Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
                CSVParser parser = CSVFormat.DEFAULT.parse(in)) {
            final List<CSVRecord> records = new ArrayList<>();
            for (final CSVRecord record : parser) {
                records.add(record);
            }
            assertEquals(3, records.size());
            assertArrayEquals(new String[] { "a", "b", "c" }, records.get(0).values());
            assertArrayEquals(new String[] { "1", "2", "3" }, records.get(1).values());
            assertArrayEquals(new String[] { "x", "y", "z" }, records.get(2).values());
        }
    }

    @Test
    void testGetHeaderComment_HeaderComment1() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            // Expect a header comment
            assertTrue(parser.hasHeaderComment());
            assertEquals("header comment", parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_HeaderComment2() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_EXPLICIT_HEADER)) {
            parser.getRecords();
            // Expect a header comment
            assertTrue(parser.hasHeaderComment());
            assertEquals("header comment", parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_HeaderComment3() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_EXPLICIT_HEADER_NOSKIP)) {
            parser.getRecords();
            // Expect no header comment - the text "comment" is attached to the first record
            assertFalse(parser.hasHeaderComment());
            assertNull(parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_HeaderTrailerComment() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_MULTILINE_HEADER_TRAILER_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            // Expect a header comment
            assertTrue(parser.hasHeaderComment());
            assertEquals("multi-line" + LF + "header comment", parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_NoComment1() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_NO_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            // Expect no header comment
            assertFalse(parser.hasHeaderComment());
            assertNull(parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_NoComment2() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_NO_COMMENT, FORMAT_EXPLICIT_HEADER)) {
            parser.getRecords();
            // Expect no header comment
            assertFalse(parser.hasHeaderComment());
            assertNull(parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderComment_NoComment3() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_NO_COMMENT, FORMAT_EXPLICIT_HEADER_NOSKIP)) {
            parser.getRecords();
            // Expect no header comment
            assertFalse(parser.hasHeaderComment());
            assertNull(parser.getHeaderComment());
        }
    }

    @Test
    void testGetHeaderMap() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            final Iterator<String> columnNames = headerMap.keySet().iterator();
            // Headers are iterated in column order.
            assertEquals("A", columnNames.next());
            assertEquals("B", columnNames.next());
            assertEquals("C", columnNames.next());
            final Iterator<CSVRecord> records = parser.iterator();

            // Parse to make sure getHeaderMap did not have a side-effect.
            for (int i = 0; i < 3; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertEquals(record.get(0), record.get("A"));
                assertEquals(record.get(1), record.get("B"));
                assertEquals(record.get(2), record.get("C"));
            }

            assertFalse(records.hasNext());
        }
    }

    @Test
    void testGetHeaderNames() throws IOException {
        try (CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final Map<String, Integer> nameIndexMap = parser.getHeaderMap();
            final List<String> headerNames = parser.getHeaderNames();
            assertNotNull(headerNames);
            assertEquals(nameIndexMap.size(), headerNames.size());
            for (int i = 0; i < headerNames.size(); i++) {
                final String name = headerNames.get(i);
                assertEquals(i, nameIndexMap.get(name).intValue());
            }
        }
    }

    @Test
    void testGetHeaderNamesReadOnly() throws IOException {
        try (CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final List<String> headerNames = parser.getHeaderNames();
            assertNotNull(headerNames);
            assertThrows(UnsupportedOperationException.class, () -> headerNames.add("This is a read-only list."));
        }
    }

    @Test
    void testGetLine() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            for (final String[] re : RESULT) {
                assertArrayEquals(re, parser.nextRecord().values());
            }

            assertNull(parser.nextRecord());
        }
    }

    @Test
    void testGetLineNumberWithCR() throws Exception {
        validateLineNumbers(String.valueOf(CR));
    }

    @Test
    void testGetLineNumberWithCRLF() throws Exception {
        validateLineNumbers(CRLF);
    }

    @Test
    void testGetLineNumberWithLF() throws Exception {
        validateLineNumbers(String.valueOf(LF));
    }

    @Test
    void testGetOneLine() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_1, CSVFormat.DEFAULT)) {
            final CSVRecord record = parser.getRecords().get(0);
            assertArrayEquals(RESULT[0], record.values());
        }
    }

    /**
     * Tests reusing a parser to process new string records one at a time as they are being discovered. See [CSV-110].
     *
     * @throws IOException when an I/O error occurs.
     */
    @Test
    void testGetOneLineOneParser() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        try (PipedWriter writer = new PipedWriter();
                PipedReader origin = new PipedReader(writer);
                CSVParser parser = CSVParser.builder()
                        .setReader(origin)
                        .setFormat(format)
                        .get()) {
            writer.append(CSV_INPUT_1);
            writer.append(format.getRecordSeparator());
            final CSVRecord record1 = parser.nextRecord();
            assertArrayEquals(RESULT[0], record1.values());
            writer.append(CSV_INPUT_2);
            writer.append(format.getRecordSeparator());
            final CSVRecord record2 = parser.nextRecord();
            assertArrayEquals(RESULT[1], record2.values());
        }
    }

    @Test
    void testGetRecordFourBytesRead() throws Exception {
        final String code = "id,a,b,c\n" +
            "1,😊,🤔,😂\n" +
            "2,😊,🤔,😂\n" +
            "3,😊,🤔,😂\n";
        final CSVFormat format = CSVFormat.Builder.create()
            .setDelimiter(',')
            .setQuote('\'')
            .get();
        try (CSVParser parser = CSVParser.builder().setReader(new StringReader(code)).setFormat(format).setCharset(UTF_8).setTrackBytes(true).get()) {
            CSVRecord record = new CSVRecord(parser, null, null, 1L, 0L, 0L);

            assertEquals(0, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(1, record.getRecordNumber());
            assertEquals(code.indexOf('i'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), record.getCharacterPosition());

            assertNotNull(record = parser.nextRecord());
            assertEquals(2, record.getRecordNumber());
            assertEquals(code.indexOf('1'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), record.getCharacterPosition());
            assertNotNull(record = parser.nextRecord());
            assertEquals(3, record.getRecordNumber());
            assertEquals(code.indexOf('2'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), 26);
            assertNotNull(record = parser.nextRecord());
            assertEquals(4, record.getRecordNumber());
            assertEquals(code.indexOf('3'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), 43);
        }
    }

    @Test
    void testGetRecordNumberWithCR() throws Exception {
        validateRecordNumbers(String.valueOf(CR));
    }

    @Test
    void testGetRecordNumberWithCRLF() throws Exception {
        validateRecordNumbers(CRLF);
    }

    @Test
    void testGetRecordNumberWithLF() throws Exception {
        validateRecordNumbers(String.valueOf(LF));
    }

    @Test
    void testGetRecordPositionWithCRLF() throws Exception {
        validateRecordPosition(CRLF);
    }

    @Test
    void testGetRecordPositionWithLF() throws Exception {
        validateRecordPosition(String.valueOf(LF));
    }

    @Test
    void testGetRecords() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(RESULT.length, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < RESULT.length; i++) {
                assertArrayEquals(RESULT[i], records.get(i).values());
            }
        }
    }

    @Test
    void testGetRecordsFromBrokenInputStream() throws IOException {
        @SuppressWarnings("resource") // We also get an exception on close, which is OK but can't assert in a try.
        final CSVParser parser = CSVParser.parse(new BrokenInputStream(), UTF_8, CSVFormat.DEFAULT);
        assertThrows(UncheckedIOException.class, parser::getRecords);

    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1, 2, 3, 4, Long.MAX_VALUE })
    void testGetRecordsMaxRows(final long maxRows) throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.builder().setIgnoreSurroundingSpaces(true).setMaxRows(maxRows).get())) {
            final List<CSVRecord> records = parser.getRecords();
            final long expectedLength = maxRows <= 0 || maxRows > RESULT.length ? RESULT.length : maxRows;
            assertEquals(expectedLength, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < expectedLength; i++) {
                assertArrayEquals(RESULT[i], records.get(i).values());
            }
        }
    }

    @Test
    void testGetRecordThreeBytesRead() throws Exception {
        final String code = "id,date,val5,val4\n" +
            "11111111111111,'4017-09-01',きちんと節分近くには咲いてる～,v4\n" +
            "22222222222222,'4017-01-01',おはよう私の友人～,v4\n" +
            "33333333333333,'4017-01-01',きる自然の力ってすごいな～,v4\n";
        final CSVFormat format = CSVFormat.Builder.create()
            .setDelimiter(',')
            .setQuote('\'')
            .get();
        try (CSVParser parser = CSVParser.builder().setReader(new StringReader(code)).setFormat(format).setCharset(UTF_8).setTrackBytes(true).get()) {
            CSVRecord record = new CSVRecord(parser, null, null, 1L, 0L, 0L);

            assertEquals(0, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(1, record.getRecordNumber());
            assertEquals(code.indexOf('i'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), record.getCharacterPosition());

            assertNotNull(record = parser.nextRecord());
            assertEquals(2, record.getRecordNumber());
            assertEquals(code.indexOf('1'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), record.getCharacterPosition());

            assertNotNull(record = parser.nextRecord());
            assertEquals(3, record.getRecordNumber());
            assertEquals(code.indexOf('2'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), 95);

            assertNotNull(record = parser.nextRecord());
            assertEquals(4, record.getRecordNumber());
            assertEquals(code.indexOf('3'), record.getCharacterPosition());
            assertEquals(record.getBytePosition(), 154);
        }
    }

    @Test
    void testGetRecordWithMultiLineValues() throws Exception {
        try (CSVParser parser = CSVParser.parse("\"a\r\n1\",\"a\r\n2\"" + CRLF + "\"b\r\n1\",\"b\r\n2\"" + CRLF + "\"c\r\n1\",\"c\r\n2\"",
                CSVFormat.DEFAULT.withRecordSeparator(CRLF))) {
            CSVRecord record;
            assertEquals(0, parser.getRecordNumber());
            assertEquals(0, parser.getCurrentLineNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(3, parser.getCurrentLineNumber());
            assertEquals(1, record.getRecordNumber());
            assertEquals(1, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(6, parser.getCurrentLineNumber());
            assertEquals(2, record.getRecordNumber());
            assertEquals(2, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(9, parser.getCurrentLineNumber());
            assertEquals(3, record.getRecordNumber());
            assertEquals(3, parser.getRecordNumber());
            assertNull(record = parser.nextRecord());
            assertEquals(9, parser.getCurrentLineNumber());
            assertEquals(3, parser.getRecordNumber());
        }
    }

    @Test
    void testGetTrailerComment_HeaderComment1() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            assertFalse(parser.hasTrailerComment());
            assertNull(parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_HeaderComment2() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_EXPLICIT_HEADER)) {
            parser.getRecords();
            assertFalse(parser.hasTrailerComment());
            assertNull(parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_HeaderComment3() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_COMMENT, FORMAT_EXPLICIT_HEADER_NOSKIP)) {
            parser.getRecords();
            assertFalse(parser.hasTrailerComment());
            assertNull(parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_HeaderTrailerComment1() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_TRAILER_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            assertTrue(parser.hasTrailerComment());
            assertEquals("comment", parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_HeaderTrailerComment2() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_TRAILER_COMMENT, FORMAT_EXPLICIT_HEADER)) {
            parser.getRecords();
            assertTrue(parser.hasTrailerComment());
            assertEquals("comment", parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_HeaderTrailerComment3() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_HEADER_TRAILER_COMMENT, FORMAT_EXPLICIT_HEADER_NOSKIP)) {
            parser.getRecords();
            assertTrue(parser.hasTrailerComment());
            assertEquals("comment", parser.getTrailerComment());
        }
    }

    @Test
    void testGetTrailerComment_MultilineComment() throws IOException {
        try (CSVParser parser = CSVParser.parse(CSV_INPUT_MULTILINE_HEADER_TRAILER_COMMENT, FORMAT_AUTO_HEADER)) {
            parser.getRecords();
            assertTrue(parser.hasTrailerComment());
            assertEquals("multi-line" + LF + "comment", parser.getTrailerComment());
        }
    }

    @Test
    void testHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();

            for (int i = 0; i < 2; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertEquals(record.get(0), record.get("a"));
                assertEquals(record.get(1), record.get("b"));
                assertEquals(record.get(2), record.get("c"));
            }

            assertFalse(records.hasNext());
        }
    }

    @Test
    void testHeaderComment() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withCommentMarker('#').withHeader().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            for (int i = 0; i < 2; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertEquals(record.get(0), record.get("a"));
                assertEquals(record.get(1), record.get("b"));
                assertEquals(record.get(2), record.get("c"));
            }
            assertFalse(records.hasNext());
        }
    }

    @Test
    void testHeaderMissing() throws Exception {
        final Reader in = new StringReader("a,,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            for (int i = 0; i < 2; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertEquals(record.get(0), record.get("a"));
                assertEquals(record.get(2), record.get("c"));
            }
            assertFalse(records.hasNext());
        }
    }

    @Test
    void testHeaderMissingWithNull() throws Exception {
        final Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().withNullString("").withAllowMissingColumnNames().parse(in)) {
            parser.iterator();
        }
    }

    @Test
    void testHeadersMissing() throws Exception {
        try (Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
                CSVParser parser = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames().parse(in)) {
            parser.iterator();
        }
    }

    @Test
    void testHeadersMissingException() {
        final Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withHeader().parse(in).iterator());
    }

    @Test
    void testHeadersMissingOneColumnException() {
        final Reader in = new StringReader("a,,c,d,e\n1,2,3,4,5\nv,w,x,y,z");
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withHeader().parse(in).iterator());
    }

    @Test
    void testHeadersWithNullColumnName() throws IOException {
        final Reader in = new StringReader("header1,null,header3\n1,2,3\n4,5,6");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().withNullString("null").withAllowMissingColumnNames().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            // Expect the null header to be missing
            @SuppressWarnings("resource")
            final CSVParser recordParser = record.getParser();
            assertEquals(Arrays.asList("header1", "header3"), recordParser.getHeaderNames());
            assertEquals(2, recordParser.getHeaderMap().size());
        }
    }

    @Test
    void testIgnoreCaseHeaderMapping() throws Exception {
        final Reader reader = new StringReader("1,2,3");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("One", "TWO", "three").withIgnoreHeaderCase().parse(reader)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("one"));
            assertEquals("2", record.get("two"));
            assertEquals("3", record.get("THREE"));
        }
    }

    @Test
    void testIgnoreEmptyLines() throws IOException {
        final String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        // String code = "world\r\n\n";
        // String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
        try (CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(3, records.size());
        }
    }

    @Test
    void testInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter(CR));
    }

    @Test
    void testIterator() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.parse(in)) {
            final Iterator<CSVRecord> iterator = parser.iterator();
            assertTrue(iterator.hasNext());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
            assertArrayEquals(new String[] { "a", "b", "c" }, iterator.next().values());
            assertArrayEquals(new String[] { "1", "2", "3" }, iterator.next().values());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertArrayEquals(new String[] { "x", "y", "z" }, iterator.next().values());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1, 2, 3, 4, 5, Long.MAX_VALUE })
    void testIteratorMaxRows(final long maxRows) throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get().parse(in)) {
            final Iterator<CSVRecord> iterator = parser.iterator();
            assertTrue(iterator.hasNext());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
            assertArrayEquals(new String[] { "a", "b", "c" }, iterator.next().values());
            final boolean noLimit = maxRows <= 0;
            final int fixtureLen = 3;
            final long expectedLen = noLimit ? fixtureLen : Math.min(fixtureLen, maxRows);
            if (expectedLen > 1) {
                assertTrue(iterator.hasNext());
                assertArrayEquals(new String[] { "1", "2", "3" }, iterator.next().values());
            }
            assertEquals(expectedLen > 2, iterator.hasNext());
            // again
            assertEquals(expectedLen > 2, iterator.hasNext());
            if (expectedLen == fixtureLen) {
                assertTrue(iterator.hasNext());
                assertArrayEquals(new String[] { "x", "y", "z" }, iterator.next().values());
            }
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Test
    void testIteratorSequenceBreaking() throws IOException {
        final String fiveRows = "1\n2\n3\n4\n5\n";
        // Iterator hasNext() shouldn't break sequence
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows))) {
            final Iterator<CSVRecord> iter = parser.iterator();
            int recordNumber = 0;
            while (iter.hasNext()) {
                final CSVRecord record = iter.next();
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
                if (recordNumber >= 2) {
                    break;
                }
            }
            iter.hasNext();
            while (iter.hasNext()) {
                final CSVRecord record = iter.next();
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
            }
        }
        // Consecutive enhanced for loops shouldn't break sequence
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows))) {
            int recordNumber = 0;
            for (final CSVRecord record : parser) {
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
                if (recordNumber >= 2) {
                    break;
                }
            }
            for (final CSVRecord record : parser) {
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
            }
        }
        // Consecutive enhanced for loops with hasNext() peeking shouldn't break sequence
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows))) {
            int recordNumber = 0;
            for (final CSVRecord record : parser) {
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
                if (recordNumber >= 2) {
                    break;
                }
            }
            parser.iterator().hasNext();
            for (final CSVRecord record : parser) {
                recordNumber++;
                assertEquals(String.valueOf(recordNumber), record.get(0));
            }
        }
    }

    @Test
    void testLineFeedEndings() throws IOException {
        final String code = "foo\nbaar,\nhello,world\n,kanu";
        try (CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

    @Test
    void testMappedButNotSetAsOutlook2007ContactExport() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("A", "B", "C").withSkipHeaderRecord().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            CSVRecord record;
            // 1st record
            record = records.next();
            assertTrue(record.isMapped("A"));
            assertTrue(record.isMapped("B"));
            assertTrue(record.isMapped("C"));
            assertTrue(record.isSet("A"));
            assertTrue(record.isSet("B"));
            assertFalse(record.isSet("C"));
            assertEquals("1", record.get("A"));
            assertEquals("2", record.get("B"));
            assertFalse(record.isConsistent());
            // 2nd record
            record = records.next();
            assertTrue(record.isMapped("A"));
            assertTrue(record.isMapped("B"));
            assertTrue(record.isMapped("C"));
            assertTrue(record.isSet("A"));
            assertTrue(record.isSet("B"));
            assertTrue(record.isSet("C"));
            assertEquals("x", record.get("A"));
            assertEquals("y", record.get("B"));
            assertEquals("z", record.get("C"));
            assertTrue(record.isConsistent());
            // end
            assertFalse(records.hasNext());
        }
    }

    @Test
    @Disabled
    void testMongoDbCsv() throws Exception {
        try (CSVParser parser = CSVParser.parse("\"a a\",b,c" + LF + "d,e,f", CSVFormat.MONGODB_CSV)) {
            final Iterator<CSVRecord> itr1 = parser.iterator();
            final Iterator<CSVRecord> itr2 = parser.iterator();

            final CSVRecord first = itr1.next();
            assertEquals("a a", first.get(0));
            assertEquals("b", first.get(1));
            assertEquals("c", first.get(2));

            final CSVRecord second = itr2.next();
            assertEquals("d", second.get(0));
            assertEquals("e", second.get(1));
            assertEquals("f", second.get(2));
        }
    }

    @Test
    // TODO this may lead to strange behavior, throw an exception if iterator() has already been called?
    void testMultipleIterators() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b,c" + CRLF + "d,e,f", CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> itr1 = parser.iterator();

            final CSVRecord first = itr1.next();
            assertEquals("a", first.get(0));
            assertEquals("b", first.get(1));
            assertEquals("c", first.get(2));

            final CSVRecord second = itr1.next();
            assertEquals("d", second.get(0));
            assertEquals("e", second.get(1));
            assertEquals("f", second.get(2));
        }
    }

    @Test
    void testNewCSVParserNullReaderFormat() {
        assertThrows(NullPointerException.class, () -> new CSVParser(null, CSVFormat.DEFAULT));
    }

    @Test
    void testNewCSVParserReaderNullFormat() {
        assertThrows(NullPointerException.class, () -> new CSVParser(new StringReader(""), null));
    }

    @Test
    void testNoHeaderMap() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT)) {
            assertNull(parser.getHeaderMap());
        }
    }

    @Test
    void testNotValueCSV() throws IOException {
        final String source = "#";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertNull(csvRecord);
        }
    }

    @Test
    void testParse() throws Exception {
        final URL url = ClassLoader.getSystemClassLoader().getResource("org/apache/commons/csv/CSVFileParser/test.csv");
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader("A", "B", "C", "D").get();
        final Charset charset = StandardCharsets.UTF_8;
        // Reader
        try (CSVParser parser = CSVParser.parse(new InputStreamReader(url.openStream(), charset), format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setReader(new InputStreamReader(url.openStream(), charset)).setFormat(format).get()) {
            parseFully(parser);
        }
        // String
        final Path path = Paths.get(url.toURI());
        final String string = new String(Files.readAllBytes(path), charset);
        try (CSVParser parser = CSVParser.parse(string, format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setCharSequence(string).setFormat(format).get()) {
            parseFully(parser);
        }
        // File
        final File file = new File(url.toURI());
        try (CSVParser parser = CSVParser.parse(file, charset, format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setFile(file).setCharset(charset).setFormat(format).get()) {
            parseFully(parser);
        }
        // InputStream
        try (CSVParser parser = CSVParser.parse(url.openStream(), charset, format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setInputStream(url.openStream()).setCharset(charset).setFormat(format).get()) {
            parseFully(parser);
        }
        // Path
        try (CSVParser parser = CSVParser.parse(path, charset, format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setPath(path).setCharset(charset).setFormat(format).get()) {
            parseFully(parser);
        }
        // URL
        try (CSVParser parser = CSVParser.parse(url, charset, format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setURI(url.toURI()).setCharset(charset).setFormat(format).get()) {
            parseFully(parser);
        }
        // InputStreamReader
        try (CSVParser parser = new CSVParser(new InputStreamReader(url.openStream(), charset), format)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setReader(new InputStreamReader(url.openStream(), charset)).setFormat(format).get()) {
            parseFully(parser);
        }
        // InputStreamReader with longs
        try (CSVParser parser = new CSVParser(new InputStreamReader(url.openStream(), charset), format, /* characterOffset= */0, /* recordNumber= */1)) {
            parseFully(parser);
        }
        try (CSVParser parser = CSVParser.builder().setReader(new InputStreamReader(url.openStream(), charset)).setFormat(format).setCharacterOffset(0)
                .setRecordNumber(0).get()) {
            parseFully(parser);
        }
    }

    @Test
    void testParseFileCharsetNullFormat() throws IOException {
        final File file = new File("src/test/resources/org/apache/commons/csv/CSVFileParser/test.csv");
        try (CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), null)) {
            // null maps to DEFAULT.
            parseFully(parser);
        }
    }

    @Test
    void testParseInputStreamCharsetNullFormat() throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get("src/test/resources/org/apache/commons/csv/CSVFileParser/test.csv"));
                CSVParser parser = CSVParser.parse(in, Charset.defaultCharset(), null)) {
            // null maps to DEFAULT.
            parseFully(parser);
        }
    }

    @Test
    void testParseNullFileFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    void testParseNullPathFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    void testParseNullStringFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse((String) null, CSVFormat.DEFAULT));
    }

    @Test
    void testParseNullUrlCharsetFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    void testParsePathCharsetNullFormat() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/csv/CSVFileParser/test.csv");
        try (CSVParser parser = CSVParser.parse(path, Charset.defaultCharset(), null)) {
            // null maps to DEFAULT.
            parseFully(parser);
        }
    }

    @Test
    void testParserUrlNullCharsetFormat() throws IOException {
        final URL url = ClassLoader.getSystemClassLoader().getResource("org/apache/commons/csv/CSVFileParser/test.csv");
        try (CSVParser parser = CSVParser.parse(url, null, CSVFormat.DEFAULT)) {
            // null maps to DEFAULT.
            parseFully(parser);
        }
    }

    @Test
    void testParseStringNullFormat() throws IOException {
        try (CSVParser parser = CSVParser.parse("1,2,3", null)) {
            // null maps to DEFAULT.
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            final CSVRecord record = records.get(0);
            assertEquals(3, record.size());
            assertEquals("1", record.get(0));
            assertEquals("2", record.get(1));
            assertEquals("3", record.get(2));
        }
    }

    @Test
    void testParseUrlCharsetNullFormat() throws IOException {
        final URL url = ClassLoader.getSystemClassLoader().getResource("org/apache/commons/csv/CSVFileParser/test.csv");
        try (CSVParser parser = CSVParser.parse(url, Charset.defaultCharset(), null)) {
            // null maps to DEFAULT.
            parseFully(parser);
        }
    }

    @Test
    void testParseWithDelimiterStringWithEscape() throws IOException {
        final String source = "a![!|!]b![|]c[|]xyz\r\nabc[abc][|]xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter("[|]").setEscape('!').get();
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a[|]b![|]c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
            csvRecord = csvParser.nextRecord();
            assertEquals("abc[abc]", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    void testParseWithDelimiterStringWithQuote() throws IOException {
        final String source = "'a[|]b[|]c'[|]xyz\r\nabc[abc][|]xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter("[|]").setQuote('\'').get();
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a[|]b[|]c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
            csvRecord = csvParser.nextRecord();
            assertEquals("abc[abc]", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    void testParseWithDelimiterWithEscape() throws IOException {
        final String source = "a!,b!,c,xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withEscape('!');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    void testParseWithDelimiterWithQuote() throws IOException {
        final String source = "'a,b,c',xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    void testParseWithQuoteThrowsException() {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'');
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'a,b,c','")).nextRecord());
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'a,b,c'abc,xyz")).nextRecord());
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'abc'a,b,c',xyz")).nextRecord());
    }

    @Test
    void testParseWithQuoteWithEscape() throws IOException {
        final String source = "'a?,b?,c?d',xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'').withEscape('?');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c?d", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @ParameterizedTest
    @EnumSource(CSVFormat.Predefined.class)
    void testParsingPrintedEmptyFirstColumn(final CSVFormat.Predefined format) throws Exception {
        final String[][] lines = { { "a", "b" }, { "", "x" } };
        final StringWriter buf = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(buf, format.getFormat())) {
            printer.printRecords(Stream.of(lines));
        }
        try (CSVParser csvRecords = CSVParser.builder()
                .setReader(new StringReader(buf.toString()))
                .setFormat(format.getFormat())
                .get()) {
            for (final String[] line : lines) {
                assertArrayEquals(line, csvRecords.nextRecord().values());
            }
            assertNull(csvRecords.nextRecord());
        }
    }

    @Test
    void testProvidedHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("A", "B", "C").parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            for (int i = 0; i < 3; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertTrue(record.isMapped("A"));
                assertTrue(record.isMapped("B"));
                assertTrue(record.isMapped("C"));
                assertFalse(record.isMapped("NOT MAPPED"));
                assertEquals(record.get(0), record.get("A"));
                assertEquals(record.get(1), record.get("B"));
                assertEquals(record.get(2), record.get("C"));
            }
            assertFalse(records.hasNext());
        }
    }

    @Test
    void testProvidedHeaderAuto() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            for (int i = 0; i < 2; i++) {
                assertTrue(records.hasNext());
                final CSVRecord record = records.next();
                assertTrue(record.isMapped("a"));
                assertTrue(record.isMapped("b"));
                assertTrue(record.isMapped("c"));
                assertFalse(record.isMapped("NOT MAPPED"));
                assertEquals(record.get(0), record.get("a"));
                assertEquals(record.get(1), record.get("b"));
                assertEquals(record.get(2), record.get("c"));
            }
            assertFalse(records.hasNext());
        }
    }

    @Test
    void testRepeatedHeadersAreReturnedInCSVRecordHeaderNames() throws IOException {
        final Reader in = new StringReader("header1,header2,header1\n1,2,3\n4,5,6");
        try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            @SuppressWarnings("resource")
            final CSVParser recordParser = record.getParser();
            assertEquals(Arrays.asList("header1", "header2", "header1"), recordParser.getHeaderNames());
        }
    }

    @Test
    void testRoundtrip() throws Exception {
        final StringWriter out = new StringWriter();
        final String data = "a,b,c\r\n1,2,3\r\nx,y,z\r\n";
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
                CSVParser parse = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            for (final CSVRecord record : parse) {
                printer.printRecord(record);
            }
            assertEquals(data, out.toString());
        }
    }

    @Test
    void testSkipAutoHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("a"));
            assertEquals("2", record.get("b"));
            assertEquals("3", record.get("c"));
        }
    }

    @Test
    void testSkipHeaderOverrideDuplicateHeaders() throws Exception {
        final Reader in = new StringReader("a,a,a\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("X"));
            assertEquals("2", record.get("Y"));
            assertEquals("3", record.get("Z"));
        }
    }

    @Test
    void testSkipSetAltHeaders() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("X"));
            assertEquals("2", record.get("Y"));
            assertEquals("3", record.get("Z"));
        }
    }

    @Test
    void testSkipSetHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("a", "b", "c").withSkipHeaderRecord().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("a"));
            assertEquals("2", record.get("b"));
            assertEquals("3", record.get("c"));
        }
    }

    @Test
    @Disabled
    void testStartWithEmptyLinesThenHeaders() throws Exception {
        final String[] codes = { "\r\n\r\n\r\nhello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n", "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" }, { "" }, // Excel format does not ignore empty lines
                { "" } };
        for (final String code : codes) {
            try (CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

    @Test
    void testStream() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.parse(in)) {
            final List<CSVRecord> list = parser.stream().collect(Collectors.toList());
            assertFalse(list.isEmpty());
            assertArrayEquals(new String[] { "a", "b", "c" }, list.get(0).values());
            assertArrayEquals(new String[] { "1", "2", "3" }, list.get(1).values());
            assertArrayEquals(new String[] { "x", "y", "z" }, list.get(2).values());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1, 2, 3, 4, Long.MAX_VALUE })
    void testStreamMaxRows(final long maxRows) throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get().parse(in)) {
            final List<CSVRecord> list = parser.stream().collect(Collectors.toList());
            assertFalse(list.isEmpty());
            assertArrayEquals(new String[] { "a", "b", "c" }, list.get(0).values());
            if (maxRows <= 0 || maxRows > 1) {
                assertArrayEquals(new String[] { "1", "2", "3" }, list.get(1).values());
            }
            if (maxRows <= 0 || maxRows > 2) {
                assertArrayEquals(new String[] { "x", "y", "z" }, list.get(2).values());
            }
        }
    }

    @Test
    void testThrowExceptionWithLineAndPosition() throws IOException {
        final String csvContent = "col1,col2,col3,col4,col5,col6,col7,col8,col9,col10\nrec1,rec2,rec3,rec4,rec5,rec6,rec7,rec8,\"\"rec9\"\",rec10";
        final StringReader stringReader = new StringReader(csvContent);
        // @formatter:off
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();
        // @formatter:on
        try (CSVParser csvParser = csvFormat.parse(stringReader)) {
            final UncheckedIOException exception = assertThrows(UncheckedIOException.class, csvParser::getRecords);
            assertInstanceOf(CSVException.class, exception.getCause());
            assertTrue(exception.getMessage().contains("Invalid character between encapsulated token and delimiter at line: 2, position: 94"),
                    exception::getMessage);
        }
    }

    @Test
    void testTrailingDelimiter() throws Exception {
        final Reader in = new StringReader("a,a,a,\n\"1\",\"2\",\"3\",\nx,y,z,");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().withTrailingDelimiter().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("X"));
            assertEquals("2", record.get("Y"));
            assertEquals("3", record.get("Z"));
            assertEquals(3, record.size());
        }
    }

    @Test
    void testTrim() throws Exception {
        final Reader in = new StringReader("a,a,a\n\" 1 \",\" 2 \",\" 3 \"\nx,y,z");
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().withTrim().parse(in)) {
            final Iterator<CSVRecord> records = parser.iterator();
            final CSVRecord record = records.next();
            assertEquals("1", record.get("X"));
            assertEquals("2", record.get("Y"));
            assertEquals("3", record.get("Z"));
            assertEquals(3, record.size());
        }
    }

    private void validateLineNumbers(final String lineSeparator) throws IOException {
        try (CSVParser parser = CSVParser.parse("a" + lineSeparator + "b" + lineSeparator + "c", CSVFormat.DEFAULT.withRecordSeparator(lineSeparator))) {
            assertEquals(0, parser.getCurrentLineNumber());
            assertNotNull(parser.nextRecord());
            assertEquals(1, parser.getCurrentLineNumber());
            assertNotNull(parser.nextRecord());
            assertEquals(2, parser.getCurrentLineNumber());
            assertNotNull(parser.nextRecord());
            // Read EOF without EOL should 3
            assertEquals(3, parser.getCurrentLineNumber());
            assertNull(parser.nextRecord());
            // Read EOF without EOL should 3
            assertEquals(3, parser.getCurrentLineNumber());
        }
    }

    private void validateRecordNumbers(final String lineSeparator) throws IOException {
        try (CSVParser parser = CSVParser.parse("a" + lineSeparator + "b" + lineSeparator + "c", CSVFormat.DEFAULT.withRecordSeparator(lineSeparator))) {
            CSVRecord record;
            assertEquals(0, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(1, record.getRecordNumber());
            assertEquals(1, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(2, record.getRecordNumber());
            assertEquals(2, parser.getRecordNumber());
            assertNotNull(record = parser.nextRecord());
            assertEquals(3, record.getRecordNumber());
            assertEquals(3, parser.getRecordNumber());
            assertNull(record = parser.nextRecord());
            assertEquals(3, parser.getRecordNumber());
        }
    }

    private void validateRecordPosition(final String lineSeparator) throws IOException {
        final String nl = lineSeparator; // used as linebreak in values for better distinction
        final String code = "a,b,c" + lineSeparator + "1,2,3" + lineSeparator +
                // to see if recordPosition correctly points to the enclosing quote
                "'A" + nl + "A','B" + nl + "B',CC" + lineSeparator +
                // unicode test... not very relevant while operating on strings instead of bytes, but for
                // completeness...
                "\u00c4,\u00d6,\u00dc" + lineSeparator + "EOF,EOF,EOF";
        final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'').withRecordSeparator(lineSeparator);
        final long positionRecord3;
        try (CSVParser parser = CSVParser.parse(code, format)) {
            CSVRecord record;
            assertEquals(0, parser.getRecordNumber());
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(1, record.getRecordNumber());
            assertEquals(code.indexOf('a'), record.getCharacterPosition());
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(2, record.getRecordNumber());
            assertEquals(code.indexOf('1'), record.getCharacterPosition());
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            positionRecord3 = record.getCharacterPosition();
            assertEquals(3, record.getRecordNumber());
            assertEquals(code.indexOf("'A"), record.getCharacterPosition());
            assertEquals("A" + lineSeparator + "A", record.get(0));
            assertEquals("B" + lineSeparator + "B", record.get(1));
            assertEquals("CC", record.get(2));
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(4, record.getRecordNumber());
            assertEquals(code.indexOf('\u00c4'), record.getCharacterPosition());
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(5, record.getRecordNumber());
            assertEquals(code.indexOf("EOF"), record.getCharacterPosition());
        }
        // now try to read starting at record 3
        try (CSVParser parser = CSVParser.builder()
                .setReader(new StringReader(code.substring((int) positionRecord3)))
                .setFormat(format)
                .setCharacterOffset(positionRecord3)
                .setRecordNumber(3)
                .get()) {
            CSVRecord record;
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(3, record.getRecordNumber());
            assertEquals(code.indexOf("'A"), record.getCharacterPosition());
            assertEquals("A" + lineSeparator + "A", record.get(0));
            assertEquals("B" + lineSeparator + "B", record.get(1));
            assertEquals("CC", record.get(2));
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(4, record.getRecordNumber());
            assertEquals(code.indexOf('\u00c4'), record.getCharacterPosition());
            assertEquals("\u00c4", record.get(0));
        } // again with ctor
        try (CSVParser parser = new CSVParser(new StringReader(code.substring((int) positionRecord3)), format, positionRecord3, 3)) {
            CSVRecord record;
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(3, record.getRecordNumber());
            assertEquals(code.indexOf("'A"), record.getCharacterPosition());
            assertEquals("A" + lineSeparator + "A", record.get(0));
            assertEquals("B" + lineSeparator + "B", record.get(1));
            assertEquals("CC", record.get(2));
            // nextRecord
            assertNotNull(record = parser.nextRecord());
            assertEquals(4, record.getRecordNumber());
            assertEquals(code.indexOf('\u00c4'), record.getCharacterPosition());
            assertEquals("\u00c4", record.get(0));
        }
    }
}
