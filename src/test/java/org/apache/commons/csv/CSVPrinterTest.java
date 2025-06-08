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

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.CR;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link CSVPrinter}.
 */
class CSVPrinterTest {

    private static final int TABLE_RECORD_COUNT = 2;
    private static final int TABLE_AND_HEADER_RECORD_COUNT = TABLE_RECORD_COUNT + 1;
    private static final char DQUOTE_CHAR = '"';
    private static final char EURO_CH = '\u20AC';
    private static final int ITERATIONS_FOR_RANDOM_TEST = 50_000;
    private static final char QUOTE_CH = '\'';
    private static final String RECORD_SEPARATOR = CSVFormat.DEFAULT.getRecordSeparator();

    private static String printable(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch <= ' ' || ch >= 128) {
                sb.append("(").append((int) ch).append(")");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String longText2;

    private void assertInitialState(final CSVPrinter printer) {
        assertEquals(0, printer.getRecordCount());
    }

    private void assertRowCount(final CSVFormat format, final String resultString, final int rowCount) throws IOException {
        try (CSVParser parser = format.parse(new StringReader(resultString))) {
            assertEquals(rowCount, parser.getRecords().size());
        }
    }

    private File createTempFile() throws IOException {
        return createTempPath().toFile();
    }

    private Path createTempPath() throws IOException {
        return Files.createTempFile(getClass().getName(), ".csv");
    }

    private void doOneRandom(final CSVFormat format) throws Exception {
        final Random r = new Random();

        final int nLines = r.nextInt(4) + 1;
        final int nCol = r.nextInt(3) + 1;
        // nLines=1;nCol=2;
        final String[][] lines = generateLines(nLines, nCol);

        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {

            for (int i = 0; i < nLines; i++) {
                // for (int j=0; j<lines[i].length; j++) System.out.println("### VALUE=:" + printable(lines[i][j]));
                printer.printRecord((Object[]) lines[i]);
            }

            printer.flush();
        }
        final String result = sw.toString();
        // System.out.println("### :" + printable(result));

        try (CSVParser parser = CSVParser.parse(result, format)) {
            final List<CSVRecord> parseResult = parser.getRecords();

            final String[][] expected = lines.clone();
            for (int i = 0; i < expected.length; i++) {
                expected[i] = expectNulls(expected[i], format);
            }
            Utils.compare("Printer output :" + printable(result), expected, parseResult, -1);
        }
    }

    private void doRandom(final CSVFormat format, final int iter) throws Exception {
        for (int i = 0; i < iter; i++) {
            doOneRandom(format);
        }
    }

    /**
     * Converts an input CSV array into expected output values WRT NULLs. NULL strings are converted to null values because the parser will convert these
     * strings to null.
     */
    private <T> T[] expectNulls(final T[] original, final CSVFormat csvFormat) {
        final T[] fixed = original.clone();
        for (int i = 0; i < fixed.length; i++) {
            if (Objects.equals(csvFormat.getNullString(), fixed[i])) {
                fixed[i] = null;
            }
        }
        return fixed;
    }

    private String[][] generateLines(final int nLines, final int nCol) {
        final String[][] lines = new String[nLines][];
        for (int i = 0; i < nLines; i++) {
            final String[] line = new String[nCol];
            lines[i] = line;
            for (int j = 0; j < nCol; j++) {
                line[j] = randStr();
            }
        }
        return lines;
    }

    private Connection getH2Connection() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:mem:my_test;", "sa", "");
    }

    private CSVPrinter printWithHeaderComments(final StringWriter sw, final Date now, final CSVFormat baseFormat) throws IOException {
        // Use withHeaderComments first to test CSV-145
        // @formatter:off
        final CSVFormat format = baseFormat.builder()
                .setHeaderComments((String[]) null) // don't blow up
                .setHeaderComments((Object[]) null) // don't blow up
                .setHeaderComments("Generated by Apache Commons CSV 1.1", now)
                .setCommentMarker('#')
                .setHeader("Col1", "Col2")
                .get();
        // @formatter:on
        final CSVPrinter printer = format.print(sw);
        printer.printRecord("A", "B");
        printer.printRecord("C", "D");
        printer.close();
        return printer;
    }

    private String randStr() {
        final Random r = new Random();
        final int sz = r.nextInt(20);
        // sz = r.nextInt(3);
        final char[] buf = new char[sz];
        for (int i = 0; i < sz; i++) {
            // stick in special chars with greater frequency
            final char ch;
            final int what = r.nextInt(20);
            switch (what) {
            case 0:
                ch = '\r';
                break;
            case 1:
                ch = '\n';
                break;
            case 2:
                ch = '\t';
                break;
            case 3:
                ch = '\f';
                break;
            case 4:
                ch = ' ';
                break;
            case 5:
                ch = ',';
                break;
            case 6:
                ch = DQUOTE_CHAR;
                break;
            case 7:
                ch = '\'';
                break;
            case 8:
                ch = BACKSLASH;
                break;
            default:
                ch = (char) r.nextInt(300);
                break;
            // default: ch = 'a'; break;
            }
            buf[i] = ch;
        }
        return new String(buf);
    }

    private void setUpTable(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255), TEXT CLOB, BIN_DATA BLOB)");
            statement.execute("insert into TEST values(1, 'r1', 'long text 1', 'binary data 1')");
            longText2 = StringUtils.repeat('a', IOUtils.DEFAULT_BUFFER_SIZE - 4);
            longText2 += "\"\r\n\"b\"";
            longText2 += StringUtils.repeat('c', IOUtils.DEFAULT_BUFFER_SIZE - 1);
            statement.execute("insert into TEST values(2, 'r2', '" + longText2 + "', 'binary data 2')");
            longText2 = longText2.replace("\"", "\"\"");
        }
    }

    @Test
    void testCloseBackwardCompatibility() throws IOException {
        try (Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT;
            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                assertInitialState(printer);
            }
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    void testCloseWithCsvFormatAutoFlushOff() throws IOException {
        try (Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(false);
            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                assertInitialState(printer);
            }
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    void testCloseWithCsvFormatAutoFlushOn() throws IOException {
        // System.out.println("start method");
        try (Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(true);
            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                assertInitialState(printer);
            }
            verify(writer, times(1)).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    void testCloseWithFlushOff() throws IOException {
        try (Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT;
            @SuppressWarnings("resource")
            final CSVPrinter printer = new CSVPrinter(writer, csvFormat);
            assertInitialState(printer);
            printer.close(false);
            assertEquals(0, printer.getRecordCount());
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    void testCloseWithFlushOn() throws IOException {
        try (Writer writer = mock(Writer.class)) {
            @SuppressWarnings("resource")
            final CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            assertInitialState(printer);
            printer.close(true);
            assertEquals(0, printer.getRecordCount());
            verify(writer, times(1)).flush();
        }
    }

    @Test
    void testCRComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final Object value = "abc";
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentMarker('#'))) {
            assertInitialState(printer);
            printer.print(value);
            assertEquals(0, printer.getRecordCount());
            printer.printComment("This is a comment\r\non multiple lines\rthis is next comment\r");
            assertEquals("abc" + RECORD_SEPARATOR + "# This is a comment" + RECORD_SEPARATOR + "# on multiple lines" + RECORD_SEPARATOR +
                    "# this is next comment" + RECORD_SEPARATOR + "# " + RECORD_SEPARATOR, sw.toString());
            assertEquals(0, printer.getRecordCount());
        }
    }

    @Test
    void testCSV135() throws IOException {
        final List<String> list = new LinkedList<>();
        list.add("\"\""); // ""
        list.add("\\\\"); // \\
        list.add("\\\"\\"); // \"\
        //
        // "",\\,\"\ (unchanged)
        tryFormat(list, null, null, "\"\",\\\\,\\\"\\");
        //
        // """""",\\,"\""\" (quoted, and embedded DQ doubled)
        tryFormat(list, '"', null, "\"\"\"\"\"\",\\\\,\"\\\"\"\\\"");
        //
        // "",\\\\,\\"\\ (escapes escaped, not quoted)
        tryFormat(list, null, '\\', "\"\",\\\\\\\\,\\\\\"\\\\");
        //
        // "\"\"","\\\\","\\\"\\" (quoted, and embedded DQ & escape escaped)
        tryFormat(list, '"', '\\', "\"\\\"\\\"\",\"\\\\\\\\\",\"\\\\\\\"\\\\\"");
        //
        // """""",\\,"\""\" (quoted, embedded DQ escaped)
        tryFormat(list, '"', '"', "\"\"\"\"\"\",\\\\,\"\\\"\"\\\"");
    }

    @Test
    void testCSV259() throws IOException {
        final StringWriter sw = new StringWriter();
        try (Reader reader = new FileReader("src/test/resources/org/apache/commons/csv/CSV-259/sample.txt");
                CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape('!').withQuote(null))) {
            assertInitialState(printer);
            printer.print(reader);
            assertEquals("x!,y!,z", sw.toString());
        }
    }

    @Test
    void testDelimeterQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''))) {
            assertInitialState(printer);
            printer.print("a,b,c");
            printer.print("xyz");
            assertEquals("'a,b,c',xyz", sw.toString());
        }
    }

    @Test
    void testDelimeterQuoteNone() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuoteMode(QuoteMode.NONE);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            assertInitialState(printer);
            printer.print("a,b,c");
            printer.print("xyz");
            assertEquals("a!,b!,c,xyz", sw.toString());
        }
    }

    @Test
    void testDelimeterStringQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setDelimiter("[|]").setQuote('\'').get())) {
            assertInitialState(printer);
            printer.print("a[|]b[|]c");
            printer.print("xyz");
            assertEquals("'a[|]b[|]c'[|]xyz", sw.toString());
        }
    }

    @Test
    void testDelimeterStringQuoteNone() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter("[|]").setEscape('!').setQuoteMode(QuoteMode.NONE).get();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            assertInitialState(printer);
            printer.print("a[|]b[|]c");
            printer.print("xyz");
            printer.print("a[xy]bc[]");
            assertEquals("a![!|!]b![!|!]c[|]xyz[|]a[xy]bc[]", sw.toString());
        }
    }

    @Test
    void testDelimiterEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape('!').withQuote(null))) {
            assertInitialState(printer);
            printer.print("a,b,c");
            printer.print("xyz");
            assertEquals("a!,b!,c,xyz", sw.toString());
        }
    }

    @Test
    void testDelimiterPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null))) {
            assertInitialState(printer);
            printer.print("a,b,c");
            printer.print("xyz");
            assertEquals("a,b,c,xyz", sw.toString());
        }
    }

    @Test
    void testDelimiterStringEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setDelimiter("|||").setEscape('!').setQuote(null).get())) {
            assertInitialState(printer);
            printer.print("a|||b|||c");
            printer.print("xyz");
            assertEquals("a!|!|!|b!|!|!|c|||xyz", sw.toString());
        }
    }

    @Test
    void testDisabledComment() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printComment("This is a comment");
            assertEquals("", sw.toString());
            assertEquals(0, printer.getRecordCount());
        }
    }

    @Test
    void testDontQuoteEuroFirstChar() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
            assertInitialState(printer);
            printer.printRecord(EURO_CH, "Deux");
            assertEquals(EURO_CH + ",Deux" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testEolEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withEscape('!'))) {
            assertInitialState(printer);
            printer.print("a\rb\nc");
            printer.print("x\fy\bz");
            assertEquals("a!rb!nc,x\fy\bz", sw.toString());
        }
    }

    @Test
    void testEolPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null))) {
            assertInitialState(printer);
            printer.print("a\rb\nc");
            printer.print("x\fy\bz");
            assertEquals("a\rb\nc,x\fy\bz", sw.toString());
        }
    }

    @Test
    void testEolQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''))) {
            assertInitialState(printer);
            printer.print("a\rb\nc");
            printer.print("x\by\fz");
            assertEquals("'a\rb\nc',x\by\fz", sw.toString());
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    void testEquals() throws IOException {
        // Don't use assertNotEquals here
        assertFalse(CSVFormat.DEFAULT.equals(null));
        // Don't use assertNotEquals here
        assertFalse(CSVFormat.DEFAULT.equals(""));
    }

    @Test
    void testEscapeBackslash1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
            assertInitialState(printer);
            printer.print("\\");
        }
        assertEquals("\\", sw.toString());
    }

    @Test
    void testEscapeBackslash2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
            assertInitialState(printer);
            printer.print("\\\r");
        }
        assertEquals("'\\\r'", sw.toString());
    }

    @Test
    void testEscapeBackslash3() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
            assertInitialState(printer);
            printer.print("X\\\r");
        }
        assertEquals("'X\\\r'", sw.toString());
    }

    @Test
    void testEscapeBackslash4() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
            assertInitialState(printer);
            printer.print("\\\\");
        }
        assertEquals("\\\\", sw.toString());
    }

    @Test
    void testEscapeBackslash5() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
            assertInitialState(printer);
            printer.print("\\\\");
        }
        assertEquals("\\\\", sw.toString());
    }

    @Test
    void testEscapeNull1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape(null))) {
            assertInitialState(printer);
            printer.print("\\");
        }
        assertEquals("\\", sw.toString());
    }

    @Test
    void testEscapeNull2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape(null))) {
            assertInitialState(printer);
            printer.print("\\\r");
        }
        assertEquals("\"\\\r\"", sw.toString());
    }

    @Test
    void testEscapeNull3() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape(null))) {
            assertInitialState(printer);
            printer.print("X\\\r");
        }
        assertEquals("\"X\\\r\"", sw.toString());
    }

    @Test
    void testEscapeNull4() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape(null))) {
            assertInitialState(printer);
            printer.print("\\\\");
        }
        assertEquals("\\\\", sw.toString());
    }

    @Test
    void testEscapeNull5() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape(null))) {
            assertInitialState(printer);
            printer.print("\\\\");
        }
        assertEquals("\\\\", sw.toString());
    }

    @Test
    void testExcelPrintAllArrayOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } });
            assertEquals("r1c1,r1c2" + RECORD_SEPARATOR + "r2c1,r2c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllArrayOfArraysWithFirstEmptyValue2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new String[][] { { "" } });
            assertEquals("\"\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllArrayOfArraysWithFirstSpaceValue1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new String[][] { { " ", "r1c2" } });
            assertEquals("\" \",r1c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllArrayOfArraysWithFirstTabValue1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new String[][] { { "\t", "r1c2" } });
            assertEquals("\"\t\",r1c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllArrayOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new List[] { Arrays.asList("r1c1", "r1c2"), Arrays.asList("r2c1", "r2c2") });
            assertEquals("r1c1,r1c2" + RECORD_SEPARATOR + "r2c1,r2c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllArrayOfListsWithFirstEmptyValue2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords((Object[]) new List[] { Arrays.asList("") });
            assertEquals("\"\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllIterableOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords(Arrays.asList(new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } }));
            assertEquals("r1c1,r1c2" + RECORD_SEPARATOR + "r2c1,r2c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllIterableOfArraysWithFirstEmptyValue2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords(Arrays.asList(new String[][] { { "" } }));
            assertEquals("\"\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrintAllIterableOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecords(Arrays.asList(Arrays.asList("r1c1", "r1c2"), Arrays.asList("r2c1", "r2c2")));
            assertEquals("r1c1,r1c2" + RECORD_SEPARATOR + "r2c1,r2c2" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1, 2, Long.MAX_VALUE })
    void testExcelPrintAllStreamOfArrays(final long maxRows) throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.EXCEL.builder().setMaxRows(maxRows).get();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            assertInitialState(printer);
            printer.printRecords(Stream.of(new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } }));
            String expected = "r1c1,r1c2" + RECORD_SEPARATOR;
            if (maxRows != 1) {
                expected += "r2c1,r2c2" + RECORD_SEPARATOR;
            }
            assertEquals(expected, sw.toString());
        }
    }

    @Test
    void testExcelPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecord("a", "b");
            assertEquals("a,b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testExcelPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL)) {
            assertInitialState(printer);
            printer.printRecord("a,b", "b");
            assertEquals("\"a,b\",b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testHeader() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3"))) {
            assertEquals(1, printer.getRecordCount());
            printer.printRecord("a", "b", "c");
            printer.printRecord("x", "y", "z");
            assertEquals("C1,C2,C3\r\na,b,c\r\nx,y,z\r\n", sw.toString());
        }
    }

    @Test
    void testHeaderCommentExcel() throws IOException {
        final StringWriter sw = new StringWriter();
        final Date now = new Date();
        final CSVFormat format = CSVFormat.EXCEL;
        try (CSVPrinter csvPrinter = printWithHeaderComments(sw, now, format)) {
            assertEquals("# Generated by Apache Commons CSV 1.1\r\n# " + now + "\r\nCol1,Col2\r\nA,B\r\nC,D\r\n", sw.toString());
        }
    }

    @Test
    void testHeaderCommentTdf() throws IOException {
        final StringWriter sw = new StringWriter();
        final Date now = new Date();
        final CSVFormat format = CSVFormat.TDF;
        try (CSVPrinter csvPrinter = printWithHeaderComments(sw, now, format)) {
            assertEquals("# Generated by Apache Commons CSV 1.1\r\n# " + now + "\r\nCol1\tCol2\r\nA\tB\r\nC\tD\r\n", sw.toString());
        }
    }

    @Test
    void testHeaderNotSet() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null))) {
            assertInitialState(printer);
            printer.printRecord("a", "b", "c");
            printer.printRecord("x", "y", "z");
            assertEquals("a,b,c\r\nx,y,z\r\n", sw.toString());
        }
    }

    @Test
    void testInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter(CR));
    }

    @Test
    void testJdbcPrinter() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        final CSVFormat csvFormat = CSVFormat.DEFAULT;
        try (Connection connection = getH2Connection()) {
            setUpTable(connection);
            try (Statement stmt = connection.createStatement();
                    CSVPrinter printer = new CSVPrinter(sw, csvFormat);
                    ResultSet resultSet = stmt.executeQuery("select ID, NAME, TEXT, BIN_DATA from TEST")) {
                assertInitialState(printer);
                printer.printRecords(resultSet);
                assertEquals(TABLE_RECORD_COUNT, printer.getRecordCount());
            }
        }
        final String csv = sw.toString();
        assertEquals("1,r1,\"long text 1\",\"YmluYXJ5IGRhdGEgMQ==\r\n\"" + RECORD_SEPARATOR + "2,r2,\"" + longText2 + "\",\"YmluYXJ5IGRhdGEgMg==\r\n\"" +
                RECORD_SEPARATOR, csv);
        // Round trip the data
        try (StringReader reader = new StringReader(csv);
                CSVParser csvParser = csvFormat.parse(reader)) {
            // Row 1
            CSVRecord record = csvParser.nextRecord();
            assertEquals("1", record.get(0));
            assertEquals("r1", record.get(1));
            assertEquals("long text 1", record.get(2));
            assertEquals("YmluYXJ5IGRhdGEgMQ==\r\n", record.get(3));
            // Row 2
            record = csvParser.nextRecord();
            assertEquals("2", record.get(0));
            assertEquals("r2", record.get(1));
            assertEquals("YmluYXJ5IGRhdGEgMg==\r\n", record.get(3));
        }
    }

    @Test
    void testJdbcPrinterWithFirstEmptyValue2() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        try (Connection connection = getH2Connection()) {
            try (Statement stmt = connection.createStatement();
                    ResultSet resultSet = stmt.executeQuery("select '' AS EMPTYVALUE from DUAL");
                    CSVPrinter printer = CSVFormat.DEFAULT.withHeader(resultSet).print(sw)) {
                printer.printRecords(resultSet);
            }
        }
        assertEquals("EMPTYVALUE" + RECORD_SEPARATOR + "\"\"" + RECORD_SEPARATOR, sw.toString());
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1, 2, 3, 4, Long.MAX_VALUE })
    void testJdbcPrinterWithResultSet(final long maxRows) throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get();
        try (Connection connection = getH2Connection()) {
            setUpTable(connection);
            try (Statement stmt = connection.createStatement();
                    ResultSet resultSet = stmt.executeQuery("select ID, NAME, TEXT from TEST");
                    CSVPrinter printer = format.withHeader(resultSet).print(sw)) {
                printer.printRecords(resultSet);
            }
        }
        final String resultString = sw.toString();
        final String header = "ID,NAME,TEXT";
        final String headerRow1 = header + RECORD_SEPARATOR + "1,r1,\"long text 1\"" + RECORD_SEPARATOR;
        final String allRows = headerRow1 + "2,r2,\"" + longText2 + "\"" + RECORD_SEPARATOR;
        final int expectedRowsWithHeader;
        if (maxRows == 1) {
            assertEquals(headerRow1, resultString);
            expectedRowsWithHeader = 2;
        } else {
            assertEquals(allRows, resultString);
            expectedRowsWithHeader = TABLE_AND_HEADER_RECORD_COUNT;
        }
        assertRowCount(CSVFormat.DEFAULT, resultString, expectedRowsWithHeader);
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 3, 4, Long.MAX_VALUE })
    void testJdbcPrinterWithResultSetHeader(final long maxRows) throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        try (Connection connection = getH2Connection()) {
            setUpTable(connection);
            final CSVFormat format = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get();
            try (Statement stmt = connection.createStatement();
                    CSVPrinter printer = new CSVPrinter(sw, format)) {
                try (ResultSet resultSet = stmt.executeQuery("select ID, NAME from TEST")) {
                    printer.printRecords(resultSet, true);
                    assertEquals(TABLE_RECORD_COUNT, printer.getRecordCount());
                    assertEquals("ID,NAME" + RECORD_SEPARATOR + "1,r1" + RECORD_SEPARATOR + "2,r2" + RECORD_SEPARATOR, sw.toString());
                }
                assertRowCount(format, sw.toString(), TABLE_AND_HEADER_RECORD_COUNT);
                try (ResultSet resultSet = stmt.executeQuery("select ID, NAME from TEST")) {
                    printer.printRecords(resultSet, false);
                    assertEquals(TABLE_RECORD_COUNT * 2, printer.getRecordCount());
                    assertNotEquals("ID,NAME" + RECORD_SEPARATOR + "1,r1" + RECORD_SEPARATOR + "2,r2" + RECORD_SEPARATOR, sw.toString());
                }
                assertRowCount(CSVFormat.DEFAULT, sw.toString(), TABLE_AND_HEADER_RECORD_COUNT + TABLE_RECORD_COUNT);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 3, 4, Long.MAX_VALUE })
    void testJdbcPrinterWithResultSetMetaData(final long maxRows) throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        try (Connection connection = getH2Connection()) {
            setUpTable(connection);
            final CSVFormat format = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get();
            try (Statement stmt = connection.createStatement();
                    ResultSet resultSet = stmt.executeQuery("select ID, NAME, TEXT from TEST");
                    CSVPrinter printer = format.withHeader(resultSet.getMetaData()).print(sw)) {
                // The header is the first record.
                assertEquals(1, printer.getRecordCount());
                printer.printRecords(resultSet);
                assertEquals(3, printer.getRecordCount());
                assertEquals("ID,NAME,TEXT" + RECORD_SEPARATOR + "1,r1,\"long text 1\"" + RECORD_SEPARATOR + "2,r2,\"" + longText2 + "\"" + RECORD_SEPARATOR,
                        sw.toString());
            }
            assertRowCount(format, sw.toString(), TABLE_AND_HEADER_RECORD_COUNT);
        }
    }

    @Test
    void testJira135_part1() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\"");
            printer.printRecord(list);
        }
        final String expected = "\"\\\"\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    @Disabled
    void testJira135_part2() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\n");
            printer.printRecord(list);
        }
        final String expected = "\"\\n\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    void testJira135_part3() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\\");
            printer.printRecord(list);
        }
        final String expected = "\"\\\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    @Disabled
    void testJira135All() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\"");
            list.add("\n");
            list.add("\\");
            printer.printRecord(list);
        }
        final String expected = "\"\\\"\",\"\\n\",\"\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    void testMongoDbCsvBasic() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_CSV)) {
            printer.printRecord("a", "b");
            assertEquals("a,b" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbCsvCommaInValue() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_CSV)) {
            printer.printRecord("a,b", "c");
            assertEquals("\"a,b\",c" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbCsvDoubleQuoteInValue() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_CSV)) {
            printer.printRecord("a \"c\" b", "d");
            assertEquals("\"a \"\"c\"\" b\",d" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbCsvTabInValue() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_CSV)) {
            printer.printRecord("a\tb", "c");
            assertEquals("a\tb,c" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbTsvBasic() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_TSV)) {
            printer.printRecord("a", "b");
            assertEquals("a\tb" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbTsvCommaInValue() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_TSV)) {
            printer.printRecord("a,b", "c");
            assertEquals("a,b\tc" + RECORD_SEPARATOR, sw.toString());
            assertEquals(1, printer.getRecordCount());
        }
    }

    @Test
    void testMongoDbTsvTabInValue() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.MONGODB_TSV)) {
            printer.printRecord("a\tb", "c");
            assertEquals("\"a\tb\"\tc" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testMultiLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentMarker('#'))) {
            printer.printComment("This is a comment\non multiple lines");
            assertEquals("# This is a comment" + RECORD_SEPARATOR + "# on multiple lines" + RECORD_SEPARATOR, sw.toString());
            assertEquals(0, printer.getRecordCount());
        }
    }

    @Test
    void testMySqlNullOutput() throws IOException {
        Object[] s = new String[] { "NULL", null };
        CSVFormat format = CSVFormat.MYSQL.withQuote(DQUOTE_CHAR).withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        String expected = "\"NULL\"\tNULL\n";
        assertEquals(expected, writer.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(s, record0);

        s = new String[] { "\\N", null };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\n", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\n\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL.withNullString("NULL");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\tNULL\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "", "\u000e,\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\t\u000e,\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "NULL", "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "NULL\t\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);
    }

    @Test
    void testMySqlNullStringDefault() {
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }

    @Test
    void testNewCsvPrinterAppendableNullFormat() {
        assertThrows(NullPointerException.class, () -> new CSVPrinter(new StringWriter(), null));
    }

    @Test
    void testNewCsvPrinterNullAppendableFormat() {
        assertThrows(NullPointerException.class, () -> new CSVPrinter(null, CSVFormat.DEFAULT));
    }

    @Test
    void testNotFlushable() throws IOException {
        final Appendable out = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecord("a", "b", "c");
            assertEquals("a,b,c" + RECORD_SEPARATOR, out.toString());
            printer.flush();
        }
    }

    @Test
    void testParseCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printRecord("a", null, "b");
        }
        final String csvString = sw.toString();
        assertEquals("a,NULL,b" + RECORD_SEPARATOR, csvString);
        try (CSVParser iterable = format.parse(new StringReader(csvString))) {
            final Iterator<CSVRecord> iterator = iterable.iterator();
            final CSVRecord record = iterator.next();
            assertEquals("a", record.get(0));
            assertNull(record.get(1));
            assertEquals("b", record.get(2));
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void testPlainEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withEscape('!'))) {
            printer.print("abc");
            printer.print("xyz");
            assertEquals("abc,xyz", sw.toString());
        }
    }

    @Test
    void testPlainPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null))) {
            printer.print("abc");
            printer.print("xyz");
            assertEquals("abc,xyz", sw.toString());
        }
    }

    @Test
    void testPlainQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''))) {
            printer.print("abc");
            assertEquals("abc", sw.toString());
        }
    }

    @Test
    @Disabled
    void testPostgreSqlCsvNullOutput() throws IOException {
        Object[] s = new String[] { "NULL", null };
        CSVFormat format = CSVFormat.POSTGRESQL_CSV.withQuote(DQUOTE_CHAR).withNullString("NULL").withQuoteMode(QuoteMode.ALL_NON_NULL);
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        String expected = "\"NULL\",NULL\n";
        assertEquals(expected, writer.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(new Object[2], record0);

        s = new String[] { "\\N", null };
        format = CSVFormat.POSTGRESQL_CSV.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "A" };
        format = CSVFormat.POSTGRESQL_CSV.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\n", "A" };
        format = CSVFormat.POSTGRESQL_CSV.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\n\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.POSTGRESQL_CSV.withNullString("NULL");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\tNULL\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.POSTGRESQL_CSV;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "", "\u000e,\\\r" };
        format = CSVFormat.POSTGRESQL_CSV;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\t\u000e,\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "NULL", "\\\r" };
        format = CSVFormat.POSTGRESQL_CSV;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "NULL\t\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\\r" };
        format = CSVFormat.POSTGRESQL_CSV;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);
    }

    @Test
    @Disabled
    void testPostgreSqlCsvTextOutput() throws IOException {
        Object[] s = new String[] { "NULL", null };
        CSVFormat format = CSVFormat.POSTGRESQL_TEXT.withQuote(DQUOTE_CHAR).withNullString("NULL").withQuoteMode(QuoteMode.ALL_NON_NULL);
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        String expected = "\"NULL\"\tNULL\n";
        assertEquals(expected, writer.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(new Object[2], record0);

        s = new String[] { "\\N", null };
        format = CSVFormat.POSTGRESQL_TEXT.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "A" };
        format = CSVFormat.POSTGRESQL_TEXT.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\n", "A" };
        format = CSVFormat.POSTGRESQL_TEXT.withNullString("\\N");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\n\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.POSTGRESQL_TEXT.withNullString("NULL");
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\tNULL\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.POSTGRESQL_TEXT;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "", "\u000e,\\\r" };
        format = CSVFormat.POSTGRESQL_TEXT;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\N\t\t\u000e,\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "NULL", "\\\r" };
        format = CSVFormat.POSTGRESQL_TEXT;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "NULL\t\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\\r" };
        format = CSVFormat.POSTGRESQL_TEXT;
        writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(s);
        }
        expected = "\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);
    }

    @Test
    void testPostgreSqlNullStringDefaultCsv() {
        assertEquals("", CSVFormat.POSTGRESQL_CSV.getNullString());
    }

    @Test
    void testPostgreSqlNullStringDefaultText() {
        assertEquals("\\N", CSVFormat.POSTGRESQL_TEXT.getNullString());
    }

    @Test
    void testPrint() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(sw)) {
            assertInitialState(printer);
            printer.printRecord("a", "b\\c");
            assertEquals("a,b\\c" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrintCSVParser() throws IOException {
        // @formatter:off
        final String code = "a1,b1\n" + // 1)
                "a2,b2\n" + // 2)
                "a3,b3\n" + // 3)
                "a4,b4\n";  // 4)
        // @formatter:on
        final String[][] res = { { "a1", "b1" }, { "a2", "b2" }, { "a3", "b3" }, { "a4", "b4" } };
        final CSVFormat format = CSVFormat.DEFAULT;
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = format.print(sw);
                CSVParser parser = CSVParser.parse(code, format)) {
            assertInitialState(printer);
            printer.printRecords(parser);
        }
        try (CSVParser parser = CSVParser.parse(sw.toString(), format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Fail", res, records, -1);
        }
    }

    @Test
    void testPrintCSVRecord() throws IOException {
        // @formatter:off
        final String code = "a1,b1\n" + // 1)
                "a2,b2\n" +  // 2)
                "a3,b3\n" +  // 3)
                "a4,b4\n";   // 4)
        // @formatter:on
        final String[][] res = { { "a1", "b1" }, { "a2", "b2" }, { "a3", "b3" }, { "a4", "b4" } };
        final CSVFormat format = CSVFormat.DEFAULT;
        final StringWriter sw = new StringWriter();
        int row = 0;
        try (CSVPrinter printer = format.print(sw);
                CSVParser parser = CSVParser.parse(code, format)) {
            assertInitialState(printer);
            for (final CSVRecord record : parser) {
                printer.printRecord(record);
                assertEquals(++row, printer.getRecordCount());
            }
            assertEquals(row, printer.getRecordCount());
        }
        try (CSVParser parser = CSVParser.parse(sw.toString(), format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Fail", res, records, -1);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 3, 4, Long.MAX_VALUE })
    void testPrintCSVRecords(final long maxRows) throws IOException {
        // @formatter:off
        final String code = "a1,b1\n" + // 1)
                "a2,b2\n" + // 2)
                "a3,b3\n" + // 3)
                "a4,b4\n";  // 4)
        // @formatter:on
        final String[][] expected = { { "a1", "b1" }, { "a2", "b2" }, { "a3", "b3" }, { "a4", "b4" } };
        final CSVFormat format = CSVFormat.DEFAULT.builder().setMaxRows(maxRows).get();
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = format.print(sw);
                CSVParser parser = CSVParser.parse(code, format)) {
            assertInitialState(printer);
            printer.printRecords(parser.getRecords());
        }
        try (CSVParser parser = CSVParser.parse(sw.toString(), format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Fail", expected, records, maxRows);
        }
    }

    @Test
    void testPrintCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withNullString("NULL"))) {
            assertInitialState(printer);
            printer.printRecord("a", null, "b");
            assertEquals("a,NULL,b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", "b");
            assertEquals(1, printer.getRecordCount());
            assertEquals("a,b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a,b", "b");
            assertEquals("\"a,b\",b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter3() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a, b", "b ");
            assertEquals("\"a, b\",\"b \"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter4() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", "b\"c");
            assertEquals("a,\"b\"\"c\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter5() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", "b\nc");
            assertEquals("a,\"b\nc\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter6() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", "b\r\nc");
            assertEquals("a,\"b\r\nc\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrinter7() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", "b\\c");
            assertEquals("a,b\\c" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrintNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertInitialState(printer);
            printer.printRecord("a", null, "b");
            assertEquals("a,,b" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testPrintOnePositiveInteger() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL))) {
            assertInitialState(printer);
            printer.print(Integer.MAX_VALUE);
            assertEquals(String.valueOf(Integer.MAX_VALUE), sw.toString());
        }
    }

    /**
     * Test to target the use of {@link IOUtils#copy(java.io.Reader, Appendable)} which directly buffers the value from the Reader to the Appendable.
     *
     * <p>
     * Requires the format to have no quote or escape character, value to be a {@link Reader Reader} and the output <em>MUST NOT</em> be a {@link Writer Writer}
     * but some other Appendable.
     * </p>
     *
     * @throws IOException Not expected to happen
     */
    @Test
    void testPrintReaderWithoutQuoteToAppendable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final String content = "testValue";
        try (CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT.withQuote(null))) {
            assertInitialState(printer);
            final StringReader value = new StringReader(content);
            printer.print(value);
        }
        assertEquals(content, sb.toString());
    }

    /**
     * Test to target the use of {@link IOUtils#copyLarge(java.io.Reader, Writer)} which directly buffers the value from the Reader to the Writer.
     *
     * <p>
     * Requires the format to have no quote or escape character, value to be a {@link Reader Reader} and the output <em>MUST</em> be a {@link Writer Writer}.
     * </p>
     *
     * @throws IOException Not expected to happen
     */
    @Test
    void testPrintReaderWithoutQuoteToWriter() throws IOException {
        final StringWriter sw = new StringWriter();
        final String content = "testValue";
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null))) {
            final StringReader value = new StringReader(content);
            printer.print(value);
        }
        assertEquals(content, sw.toString());
    }

    @Test
    void testPrintRecordStream() throws IOException {
        // @formatter:off
        final String code = "a1,b1\n" + // 1)
                "a2,b2\n" + // 2)
                "a3,b3\n" + // 3)
                "a4,b4\n";  // 4)
        // @formatter:on
        final String[][] res = { { "a1", "b1" }, { "a2", "b2" }, { "a3", "b3" }, { "a4", "b4" } };
        final CSVFormat format = CSVFormat.DEFAULT;
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = format.print(sw);
                CSVParser parser = CSVParser.parse(code, format)) {
            long count = 0;
            for (final CSVRecord record : parser) {
                printer.printRecord(record.stream());
                assertEquals(++count, printer.getRecordCount());
            }
        }
        try (CSVParser parser = CSVParser.parse(sw.toString(), format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());
            Utils.compare("Fail", res, records, -1);
        }
    }

    @Test
    void testPrintRecordsWithCSVRecord() throws IOException {
        final String[] values = { "A", "B", "C" };
        final String rowData = StringUtils.join(values, ',');
        final CharArrayWriter charArrayWriter = new CharArrayWriter(0);
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(rowData));
                CSVPrinter printer = CSVFormat.INFORMIX_UNLOAD.print(charArrayWriter)) {
            long count = 0;
            for (final CSVRecord record : parser) {
                printer.printRecord(record);
                assertEquals(++count, printer.getRecordCount());
            }
        }
        assertEquals(6, charArrayWriter.size());
        assertEquals("A|B|C" + CSVFormat.INFORMIX_UNLOAD.getRecordSeparator(), charArrayWriter.toString());
    }

    @Test
    void testPrintRecordsWithEmptyVector() throws IOException {
        final PrintStream out = System.out;
        try {
            System.setOut(new PrintStream(NullOutputStream.INSTANCE));
            try (CSVPrinter printer = CSVFormat.POSTGRESQL_TEXT.printer()) {
                final Vector<CSVFormatTest.EmptyEnum> vector = new Vector<>();
                final int expectedCapacity = 23;
                vector.setSize(expectedCapacity);
                printer.printRecords(vector);
                assertEquals(expectedCapacity, vector.capacity());
                assertEquals(expectedCapacity, printer.getRecordCount());
            }
        } finally {
            System.setOut(out);
        }
    }

    @Test
    void testPrintRecordsWithObjectArray() throws IOException {
        final CharArrayWriter charArrayWriter = new CharArrayWriter(0);
        final Object[] objectArray = new Object[6];
        try (CSVPrinter printer = CSVFormat.INFORMIX_UNLOAD.print(charArrayWriter)) {
            final HashSet<BatchUpdateException> hashSet = new HashSet<>();
            objectArray[3] = hashSet;
            printer.printRecords(objectArray);
            assertEquals(objectArray.length, printer.getRecordCount());
        }
        assertEquals(6, charArrayWriter.size());
        assertEquals("\n\n\n\n\n\n", charArrayWriter.toString());
    }

    @Test
    void testPrintRecordsWithResultSetOneRow() throws IOException, SQLException {
        try (CSVPrinter printer = CSVFormat.MYSQL.printer()) {
            try (ResultSet resultSet = new SimpleResultSet()) {
                assertInitialState(printer);
                printer.printRecords(resultSet);
                assertInitialState(printer);
                assertEquals(0, resultSet.getRow());
            }
        }
    }

    @Test
    void testPrintToFileWithCharsetUtf16Be() throws IOException {
        final File file = createTempFile();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(file, StandardCharsets.UTF_16BE)) {
            printer.printRecord("a", "b\\c");
        }
        assertEquals("a,b\\c" + RECORD_SEPARATOR, FileUtils.readFileToString(file, StandardCharsets.UTF_16BE));
    }

    @Test
    void testPrintToFileWithDefaultCharset() throws IOException {
        final File file = createTempFile();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charset.defaultCharset())) {
            printer.printRecord("a", "b\\c");
        }
        assertEquals("a,b\\c" + RECORD_SEPARATOR, FileUtils.readFileToString(file, Charset.defaultCharset()));
    }

    @Test
    void testPrintToPathWithDefaultCharset() throws IOException {
        final Path file = createTempPath();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charset.defaultCharset())) {
            printer.printRecord("a", "b\\c");
        }
        assertEquals("a,b\\c" + RECORD_SEPARATOR, new String(Files.readAllBytes(file), Charset.defaultCharset()));
    }

    @Test
    void testQuoteAll() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL))) {
            printer.printRecord("a", "b\nc", "d");
            assertEquals("\"a\",\"b\nc\",\"d\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testQuoteCommaFirstChar() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
            printer.printRecord(",");
            assertEquals("\",\"" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testQuoteNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC))) {
            printer.printRecord("a", "b\nc", Integer.valueOf(1));
            assertEquals("\"a\",\"b\nc\",1" + RECORD_SEPARATOR, sw.toString());
        }
    }

    @Test
    void testRandomDefault() throws Exception {
        doRandom(CSVFormat.DEFAULT, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testRandomExcel() throws Exception {
        doRandom(CSVFormat.EXCEL, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    @Disabled
    void testRandomMongoDbCsv() throws Exception {
        doRandom(CSVFormat.MONGODB_CSV, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testRandomMySql() throws Exception {
        doRandom(CSVFormat.MYSQL, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    @Disabled
    void testRandomOracle() throws Exception {
        doRandom(CSVFormat.ORACLE, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    @Disabled
    void testRandomPostgreSqlCsv() throws Exception {
        doRandom(CSVFormat.POSTGRESQL_CSV, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testRandomPostgreSqlText() throws Exception {
        doRandom(CSVFormat.POSTGRESQL_TEXT, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testRandomRfc4180() throws Exception {
        doRandom(CSVFormat.RFC4180, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testRandomTdf() throws Exception {
        doRandom(CSVFormat.TDF, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    void testSingleLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentMarker('#'))) {
            printer.printComment("This is a comment");
            assertEquals("# This is a comment" + RECORD_SEPARATOR, sw.toString());
            assertEquals(0, printer.getRecordCount());
        }
    }

    @Test
    void testSingleQuoteQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''))) {
            printer.print("a'b'c");
            printer.print("xyz");
            assertEquals("'a''b''c',xyz", sw.toString());
        }
    }

    @Test
    void testSkipHeaderRecordFalse() throws IOException {
        // functionally identical to testHeader, used to test CSV-153
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3").withSkipHeaderRecord(false))) {
            printer.printRecord("a", "b", "c");
            printer.printRecord("x", "y", "z");
            assertEquals("C1,C2,C3\r\na,b,c\r\nx,y,z\r\n", sw.toString());
        }
    }

    @Test
    void testSkipHeaderRecordTrue() throws IOException {
        // functionally identical to testHeaderNotSet, used to test CSV-153
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3").withSkipHeaderRecord(true))) {
            printer.printRecord("a", "b", "c");
            printer.printRecord("x", "y", "z");
            assertEquals("a,b,c\r\nx,y,z\r\n", sw.toString());
        }
    }

    @Test
    void testTrailingDelimiterOnTwoColumns() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withTrailingDelimiter())) {
            printer.printRecord("A", "B");
            assertEquals("A,B,\r\n", sw.toString());
        }
    }

    @Test
    void testTrimOffOneColumn() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withTrim(false))) {
            printer.print(" A ");
            assertEquals("\" A \"", sw.toString());
        }
    }

    @Test
    void testTrimOnOneColumn() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withTrim())) {
            printer.print(" A ");
            assertEquals("A", sw.toString());
        }
    }

    @Test
    void testTrimOnTwoColumns() throws IOException {
        final StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withTrim())) {
            printer.print(" A ");
            printer.print(" B ");
            assertEquals("A,B", sw.toString());
        }
    }

    private String[] toFirstRecordValues(final String expected, final CSVFormat format) throws IOException {
        try (CSVParser parser = CSVParser.parse(expected, format)) {
            return parser.getRecords().get(0).values();
        }
    }

    private void tryFormat(final List<String> list, final Character quote, final Character escape, final String expected) throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote(quote).withEscape(escape).withRecordSeparator(null);
        final Appendable out = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printRecord(list);
        }
        assertEquals(expected, out.toString());
    }

}
