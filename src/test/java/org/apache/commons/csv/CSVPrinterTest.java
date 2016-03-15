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

import static org.apache.commons.csv.Constants.CR;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 * @version $Id$
 */
public class CSVPrinterTest {

    private static final int ITERATIONS_FOR_RANDOM_TEST = 50000;
    
    private final String recordSeparator = CSVFormat.DEFAULT.getRecordSeparator();

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

    private void doOneRandom(final CSVFormat format) throws Exception {
        final Random r = new Random();

        final int nLines = r.nextInt(4) + 1;
        final int nCol = r.nextInt(3) + 1;
        // nLines=1;nCol=2;
        final String[][] lines = generateLines(nLines, nCol);

        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, format);

        for (int i = 0; i < nLines; i++) {
            // for (int j=0; j<lines[i].length; j++) System.out.println("### VALUE=:" + printable(lines[i][j]));
            printer.printRecord((Object[]) lines[i]);
        }

        printer.flush();
        printer.close();
        final String result = sw.toString();
        // System.out.println("### :" + printable(result));

        final CSVParser parser = CSVParser.parse(result, format);
        final List<CSVRecord> parseResult = parser.getRecords();

        String[][] expected = lines.clone();
        for (int i = 0; i < expected.length; i++) {
            expected[i] = expectNulls(expected[i], format);
        }
        Utils.compare("Printer output :" + printable(result), expected, parseResult);
        parser.close();
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

    private void doRandom(final CSVFormat format, final int iter) throws Exception {
        for (int i = 0; i < iter; i++) {
            doOneRandom(format);
        }
    }

    private String randStr() {
        final Random r = new Random();

        final int sz = r.nextInt(20);
        // sz = r.nextInt(3);
        final char[] buf = new char[sz];
        for (int i = 0; i < sz; i++) {
            // stick in special chars with greater frequency
            char ch;
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
                ch = '"';
                break;
            case 7:
                ch = '\'';
                break;
            case 8:
                ch = '\\';
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

    @Test
    public void testDisabledComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This is a comment");

        assertEquals("", sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrintAllArrayOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords((Object[]) new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } });
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrintAllArrayOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords((Object[]) new List[] { Arrays.asList("r1c1", "r1c2"), Arrays.asList("r2c1", "r2c2") });
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrintAllIterableOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(Arrays.asList(new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } }));
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrintAllIterableOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(
                Arrays.asList(new List[] { Arrays.asList("r1c1", "r1c2"), Arrays.asList("r2c1", "r2c2") }));
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecord("a", "b");
        assertEquals("a,b" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testExcelPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecord("a,b", "b");
        assertEquals("\"a,b\",b" + recordSeparator, sw.toString());
        printer.close();
    }

    private Connection geH2Connection() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:mem:my_test;", "sa", "");
    }

    @Test
    @Ignore
    public void testJira135All() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote('"').withEscape('\\');
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);
        List<String> list = new LinkedList<String>();
        list.add("\"");
        list.add("\n");
        list.add("\\");
        printer.printRecord(list);
        printer.close();
        final String expected = "\"\\\"\",\"\\n\",\"\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }
    
    @Test
    @Ignore
    public void testJira135_part3() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote('"').withEscape('\\');
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);
        List<String> list = new LinkedList<String>();
        list.add("\\");
        printer.printRecord(list);
        printer.close();
        final String expected = "\"\\\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }
    
    @Test
    @Ignore
    public void testJira135_part2() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote('"').withEscape('\\');
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);
        List<String> list = new LinkedList<String>();
        list.add("\n");
        printer.printRecord(list);
        printer.close();
        final String expected = "\"\\n\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }
    
    @Test
    @Ignore
    public void testJira135_part1() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote('"').withEscape('\\');
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);
        List<String> list = new LinkedList<String>();
        list.add("\"");
        printer.printRecord(list);
        printer.close();
        final String expected = "\"\\\"\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }
    
    @Test
    public void testJdbcPrinter() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        final Connection connection = geH2Connection();
        try {
            setUpTable(connection);
            final Statement stmt = connection.createStatement();
            final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
            printer.printRecords(stmt.executeQuery("select ID, NAME from TEST"));
            assertEquals("1,r1" + recordSeparator + "2,r2" + recordSeparator, sw.toString());
            printer.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testJdbcPrinterWithResultSet() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        Class.forName("org.h2.Driver");
        final Connection connection = geH2Connection();
        try {
            setUpTable(connection);
            @SuppressWarnings("resource")
            // Closed when the connection is closed.
            final Statement stmt = connection.createStatement();
            @SuppressWarnings("resource")
            // Closed when the connection is closed.
            final ResultSet resultSet = stmt.executeQuery("select ID, NAME from TEST");
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(resultSet).print(sw);
            printer.printRecords(resultSet);
            assertEquals("ID,NAME" + recordSeparator + "1,r1" + recordSeparator + "2,r2" + recordSeparator,
                    sw.toString());
            printer.close();
        } finally {
            connection.close();
        }
    }
    @Test
    public void testJdbcPrinterWithResultSetHeader() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        final Connection connection = geH2Connection();
        try {
            setUpTable(connection);
            final Statement stmt = connection.createStatement();
            final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
            printer.printRecords(stmt.executeQuery("select ID, NAME from TEST"),true);
            assertEquals("ID,NAME" + recordSeparator + "1,r1" + recordSeparator + "2,r2" + recordSeparator, sw.toString());
            printer.close();
        } finally {
            connection.close();
        }
    }
    @Test
    public void testJdbcPrinterWithResultSetMetaData() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        Class.forName("org.h2.Driver");
        final Connection connection = geH2Connection();
        try {
            setUpTable(connection);
            @SuppressWarnings("resource")
            // Closed when the connection is closed.
            final Statement stmt = connection.createStatement();
            @SuppressWarnings("resource")
            // Closed when the connection is closed.
            final ResultSet resultSet = stmt.executeQuery("select ID, NAME from TEST");
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(resultSet.getMetaData()).print(sw);
            printer.printRecords(resultSet);
            assertEquals("ID,NAME" + recordSeparator + "1,r1" + recordSeparator + "2,r2" + recordSeparator,
                    sw.toString());
            printer.close();
        } finally {
            connection.close();
        }
    }

    private void setUpTable(final Connection connection) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            statement.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            statement.execute("insert into TEST values(1, 'r1')");
            statement.execute("insert into TEST values(2, 'r2')");
        } finally {
            statement.close();
        }
    }

    @Test
    public void testMultiLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentMarker('#'));
        printer.printComment("This is a comment\non multiple lines");

        assertEquals("# This is a comment" + recordSeparator + "# on multiple lines" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testMySqlNullStringDefault() throws IOException {
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }

    @Test
    public void testMySqlNullOutput() throws IOException {
        Object[] s = new String[] { "NULL", null };
        CSVFormat format = CSVFormat.MYSQL.withQuote('"').withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        String expected = "\"NULL\"\tNULL\n";
        assertEquals(expected, writer.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(new Object[2], record0);

        s = new String[] { "\\N", null };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\n", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\n\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL.withNullString("NULL");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\tNULL\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "", "\u000e,\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\t\t\u000e,\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "NULL", "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "NULL\t\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);
    }

    /**
     * Converts an input CSV array into expected output values WRT NULLs. NULL strings are converted to null values
     * because the parser will convert these strings to null.
     */
    private <T> T[] expectNulls(T[] original, CSVFormat csvFormat) {
        T[] fixed = original.clone();
        for (int i = 0; i < fixed.length; i++) {
            if (ObjectUtils.equals(csvFormat.getNullString(), fixed[i])) {
                fixed[i] = null;
            }
        }
        return fixed;
    }

    private String[] toFirstRecordValues(final String expected, CSVFormat format) throws IOException {
        return CSVParser.parse(expected, format).getRecords().get(0).values();
    }

    @Test
    public void testPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b");
        assertEquals("a,b" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a,b", "b");
        assertEquals("\"a,b\",b" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter3() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a, b", "b ");
        assertEquals("\"a, b\",\"b \"" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter4() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\"c");
        assertEquals("a,\"b\"\"c\"" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter5() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\nc");
        assertEquals("a,\"b\nc\"" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter6() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\r\nc");
        assertEquals("a,\"b\r\nc\"" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrinter7() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\\c");
        assertEquals("a,b\\c" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrint() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = CSVFormat.DEFAULT.print(sw);
        printer.printRecord("a", "b\\c");
        assertEquals("a,b\\c" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrintNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", null, "b");
        assertEquals("a,,b" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testPrintCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withNullString("NULL"));
        printer.printRecord("a", null, "b");
        assertEquals("a,NULL,b" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testParseCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a", null, "b");
        printer.close();
        final String csvString = sw.toString();
        assertEquals("a,NULL,b" + recordSeparator, csvString);
        final Iterable<CSVRecord> iterable = format.parse(new StringReader(csvString));
        final Iterator<CSVRecord> iterator = iterable.iterator();
        final CSVRecord record = iterator.next();
        assertEquals("a", record.get(0));
        assertEquals(null, record.get(1));
        assertEquals("b", record.get(2));
        assertFalse(iterator.hasNext());
        ((CSVParser) iterable).close();
    }

    @Test
    public void testQuoteAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL));
        printer.printRecord("a", "b\nc", "d");
        assertEquals("\"a\",\"b\nc\",\"d\"" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testQuoteNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC));
        printer.printRecord("a", "b\nc", Integer.valueOf(1));
        assertEquals("\"a\",\"b\nc\",1" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testRandomDefault() throws Exception {
        doRandom(CSVFormat.DEFAULT, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    public void testRandomExcel() throws Exception {
        doRandom(CSVFormat.EXCEL, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    public void testRandomMySql() throws Exception {
        doRandom(CSVFormat.MYSQL, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    public void testRandomTdf() throws Exception {
        doRandom(CSVFormat.TDF, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    public void testRandomRfc4180() throws Exception {
        doRandom(CSVFormat.RFC4180, ITERATIONS_FOR_RANDOM_TEST);
    }

    @Test
    public void testPlainQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''));
        printer.print("abc");
        assertEquals("abc", sw.toString());
        printer.close();
    }

    @Test
    public void testSingleLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentMarker('#'));
        printer.printComment("This is a comment");

        assertEquals("# This is a comment" + recordSeparator, sw.toString());
        printer.close();
    }

    @Test
    public void testSingleQuoteQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''));
        printer.print("a'b'c");
        printer.print("xyz");
        assertEquals("'a''b''c',xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testDelimeterQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("'a,b,c',xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testDelimeterQuoteNONE() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuoteMode(QuoteMode.NONE);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a!,b!,c,xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testEOLQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote('\''));
        printer.print("a\rb\nc");
        printer.print("x\by\fz");
        assertEquals("'a\rb\nc',x\by\fz", sw.toString());
        printer.close();
    }

    @Test
    public void testEscapeBackslash() throws IOException {
        StringWriter sw = new StringWriter();
        final char quoteChar = '\'';
        final String eol = "\r\n";
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(quoteChar));
        printer.print("\\");
        printer.close();
        assertEquals("'\\'", sw.toString());

        sw = new StringWriter();
        printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(quoteChar));
        printer.print("\\\r");
        printer.close();
        assertEquals("'\\\r'", sw.toString());

        sw = new StringWriter();
        printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(quoteChar));
        printer.print("X\\\r");
        printer.close();
        assertEquals("'X\\\r'", sw.toString());

        sw = new StringWriter();
        printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(quoteChar));
        printer.printRecord(new Object[] { "\\\r" });
        printer.close();
        assertEquals("'\\\r'" + eol, sw.toString());

        sw = new StringWriter();
        printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(quoteChar));
        printer.print("\\\\");
        printer.close();
        assertEquals("'\\\\'", sw.toString());

    }

    @Test
    public void testPlainEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withEscape('!'));
        printer.print("abc");
        printer.print("xyz");
        assertEquals("abc,xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testDelimiterEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape('!').withQuote(null));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a!,b!,c,xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testEOLEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withEscape('!'));
        printer.print("a\rb\nc");
        printer.print("x\fy\bz");
        assertEquals("a!rb!nc,x\fy\bz", sw.toString());
        printer.close();
    }

    @Test
    public void testPlainPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null));
        printer.print("abc");
        printer.print("xyz");
        assertEquals("abc,xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testDelimiterPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a,b,c,xyz", sw.toString());
        printer.close();
    }

    @Test
    public void testHeader() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3"));
        printer.printRecord("a", "b", "c");
        printer.printRecord("x", "y", "z");
        assertEquals("C1,C2,C3\r\na,b,c\r\nx,y,z\r\n", sw.toString());
        printer.close();
    }

    @Test
    public void testHeaderNotSet() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null));
        printer.printRecord("a", "b", "c");
        printer.printRecord("x", "y", "z");
        assertEquals("a,b,c\r\nx,y,z\r\n", sw.toString());
        printer.close();
    }

    @Test
    public void testSkipHeaderRecordTrue() throws IOException {
        // functionally identical to testHeaderNotSet, used to test CSV-153
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw,
                CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3").withSkipHeaderRecord(true));
        printer.printRecord("a", "b", "c");
        printer.printRecord("x", "y", "z");
        assertEquals("a,b,c\r\nx,y,z\r\n", sw.toString());
        printer.close();
    }

    @Test
    public void testSkipHeaderRecordFalse() throws IOException {
        // functionally identical to testHeader, used to test CSV-153
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw,
                CSVFormat.DEFAULT.withQuote(null).withHeader("C1", "C2", "C3").withSkipHeaderRecord(false));
        printer.printRecord("a", "b", "c");
        printer.printRecord("x", "y", "z");
        assertEquals("C1,C2,C3\r\na,b,c\r\nx,y,z\r\n", sw.toString());
        printer.close();
    }

    @Test
    public void testHeaderCommentExcel() throws IOException {
        final StringWriter sw = new StringWriter();
        final Date now = new Date();
        final CSVFormat format = CSVFormat.EXCEL;
        final CSVPrinter csvPrinter = printWithHeaderComments(sw, now, format);
        assertEquals("# Generated by Apache Commons CSV 1.1\r\n# " + now + "\r\nCol1,Col2\r\nA,B\r\nC,D\r\n",
                sw.toString());
        csvPrinter.close();
    }

    @Test
    public void testHeaderCommentTdf() throws IOException {
        final StringWriter sw = new StringWriter();
        final Date now = new Date();
        final CSVFormat format = CSVFormat.TDF;
        final CSVPrinter csvPrinter = printWithHeaderComments(sw, now, format);
        assertEquals("# Generated by Apache Commons CSV 1.1\r\n# " + now + "\r\nCol1\tCol2\r\nA\tB\r\nC\tD\r\n",
                sw.toString());
        csvPrinter.close();
    }

    private CSVPrinter printWithHeaderComments(final StringWriter sw, final Date now, final CSVFormat baseFormat)
            throws IOException {
        CSVFormat format = baseFormat;
        // Use withHeaderComments first to test CSV-145
        format = format.withHeaderComments("Generated by Apache Commons CSV 1.1", now);
        format = format.withCommentMarker('#');
        format = format.withHeader("Col1", "Col2");
        final CSVPrinter csvPrinter = format.print(sw);
        csvPrinter.printRecord("A", "B");
        csvPrinter.printRecord("C", "D");
        csvPrinter.close();
        return csvPrinter;
    }

    @Test
    public void testEOLPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(null));
        printer.print("a\rb\nc");
        printer.print("x\fy\bz");
        assertEquals("a\rb\nc,x\fy\bz", sw.toString());
        printer.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() throws Exception {
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter(CR);
        new CSVPrinter(new StringWriter(), invalidFormat).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCSVPrinterNullAppendableFormat() throws Exception {
        new CSVPrinter(null, CSVFormat.DEFAULT).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCsvPrinterAppendableNullFormat() throws Exception {
        new CSVPrinter(new StringWriter(), null).close();
    }
}
