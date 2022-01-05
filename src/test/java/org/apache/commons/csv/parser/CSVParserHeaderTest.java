package org.apache.commons.csv.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserHeaderTest {

    @Test
    public void testHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

    @Test
    public void testHeaderComment() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withCommentMarker('#').withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

    @Test
    public void testHeaderMissing() throws Exception {
        final Reader in = new StringReader("a,,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames().parse(in)
                .iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

    @Test
    public void testHeaderMissingWithNull() throws Exception {
        final Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
        CSVFormat.DEFAULT.withHeader().withNullString("").withAllowMissingColumnNames().parse(in).iterator();
    }

    @Test
    public void testHeadersMissing() throws Exception {
        final Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
        CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames().parse(in).iterator();
    }

    @Test
    public void testHeadersMissingException() {
        final Reader in = new StringReader("a,,c,,e\n1,2,3,4,5\nv,w,x,y,z");
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withHeader().parse(in).iterator());
    }

    @Test
    public void testHeadersMissingOneColumnException() throws Exception {
        final Reader in = new StringReader("a,,c,d,e\n1,2,3,4,5\nv,w,x,y,z");
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withHeader().parse(in).iterator());
    }

    @Test
    public void testHeadersWithNullColumnName() throws IOException {
        final Reader in = new StringReader("header1,null,header3\n1,2,3\n4,5,6");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().withNullString("null")
                .withAllowMissingColumnNames().parse(in).iterator();
        final CSVRecord record = records.next();
        // Expect the null header to be missing
        assertEquals(Arrays.asList("header1", "header3"), record.getParser().getHeaderNames());
        assertEquals(2, record.getParser().getHeaderMap().size());
    }

    @Test
    public void testIgnoreCaseHeaderMapping() throws Exception {
        final Reader reader = new StringReader("1,2,3");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("One", "TWO", "three").withIgnoreHeaderCase()
                .parse(reader).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("one"));
        assertEquals("2", record.get("two"));
        assertEquals("3", record.get("THREE"));
    }


    @Test
    public void testDuplicateHeadersAllowedByDefault() throws Exception {
        CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader());
    }

    @Test
    public void testDuplicateHeadersNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> CSVParser.parse("a,b,a\n1,2,3\nx,y,z",
                CSVFormat.DEFAULT.withHeader().withAllowDuplicateHeaderNames(false)));
    }


    @Test
    public void testGetHeaderMap() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z",
                CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
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
    public void testGetHeaderNames() throws IOException {
        try (final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z",
                CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
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
    public void testGetHeaderNamesReadOnly() throws IOException {
        try (final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z",
                CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final List<String> headerNames = parser.getHeaderNames();
            assertNotNull(headerNames);
            assertThrows(UnsupportedOperationException.class, () -> headerNames.add("This is a read-only list."));
        }
    }


    @Test
    public void testProvidedHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("A", "B", "C").parse(in).iterator();

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

    @Test
    public void testProvidedHeaderAuto() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();

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

    @Test
    public void testRepeatedHeadersAreReturnedInCSVRecordHeaderNames() throws IOException {
        final Reader in = new StringReader("header1,header2,header1\n1,2,3\n4,5,6");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals(Arrays.asList("header1", "header2", "header1"), record.getParser().getHeaderNames());
    }


    @Test
    public void testSkipAutoHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

    @Test
    public void testSkipHeaderOverrideDuplicateHeaders() throws Exception {
        final Reader in = new StringReader("a,a,a\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
    }

    @Test
    public void testSkipSetAltHeaders() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
    }

    @Test
    public void testSkipSetHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("a", "b", "c").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

    @Test
    @Disabled
    public void testStartWithEmptyLinesThenHeaders() throws Exception {
        final String[] codes = {"\r\n\r\n\r\nhello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"};
        final String[][] res = {{"hello", ""}, {""}, // Excel format does not ignore empty lines
                {""}};
        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertFalse(records.isEmpty());
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

}
