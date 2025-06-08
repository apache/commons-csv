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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CSVRecordTest {

    private enum EnumFixture {
        UNKNOWN_COLUMN
    }

    /** This enum overrides toString() but it's the names that matter. */
    public enum EnumHeader {
        FIRST("first"), SECOND("second"), THIRD("third");

        private final String number;

        EnumHeader(final String number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return number;
        }
    }

    private Map<String, Integer> headerMap;
    private CSVRecord record;
    private CSVRecord recordWithHeader;
    private String[] values;

    @BeforeEach
    public void setUp() throws Exception {
        values = new String[] { "A", "B", "C" };
        final String rowData = StringUtils.join(values, ',');
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(rowData))) {
            record = parser.iterator().next();
        }
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader(EnumHeader.class).get().parse(new StringReader(rowData))) {
            recordWithHeader = parser.iterator().next();
            headerMap = parser.getHeaderMap();
        }
    }

    @Test
    void testCSVRecordNULLValues() throws IOException {
        try (CSVParser parser = CSVParser.parse("A,B\r\nONE,TWO", CSVFormat.DEFAULT.withHeader())) {
            final CSVRecord csvRecord = new CSVRecord(parser, null, null, 0L, 0L, 0L);
            assertEquals(0, csvRecord.size());
            assertThrows(IllegalArgumentException.class, () -> csvRecord.get("B"));
        }
    }

    @Test
    void testDuplicateHeaderGet() throws IOException {
        final String csv = "A,A,B,B\n1,2,5,6\n";
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().get();

        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final CSVRecord record = parser.nextRecord();

            assertAll("Test that it gets the last instance of a column when there are duplicate headings",
                () -> assertEquals("2", record.get("A")),
                () -> assertEquals("6", record.get("B"))
            );
        }
    }

    @Test
    void testDuplicateHeaderToMap() throws IOException {
        final String csv = "A,A,B,B\n1,2,5,6\n";
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().get();

        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final CSVRecord record = parser.nextRecord();
            final Map<String, String> map = record.toMap();

            assertAll("Test that it gets the last instance of a column when there are duplicate headings",
                () -> assertEquals("2", map.get("A")),
                () -> assertEquals("6", map.get("B"))
            );
        }
    }

    @Test
    void testGetInt() {
        assertEquals(values[0], record.get(0));
        assertEquals(values[1], record.get(1));
        assertEquals(values[2], record.get(2));
    }

    @Test
    void testGetNullEnum() {
        assertThrows(IllegalArgumentException.class, () -> recordWithHeader.get((Enum<?>) null));
    }

    @Test
    void testGetString() {
        assertEquals(values[0], recordWithHeader.get(EnumHeader.FIRST.name()));
        assertEquals(values[1], recordWithHeader.get(EnumHeader.SECOND.name()));
        assertEquals(values[2], recordWithHeader.get(EnumHeader.THIRD.name()));
    }

    @Test
    void testGetStringInconsistentRecord() {
        headerMap.put("fourth", Integer.valueOf(4));
        assertThrows(IllegalArgumentException.class, () -> recordWithHeader.get("fourth"));
    }

    @Test
    void testGetStringNoHeader() {
        assertThrows(IllegalStateException.class, () -> record.get("first"));
    }

    @Test
    void testGetUnmappedEnum() {
        assertThrows(IllegalArgumentException.class, () -> recordWithHeader.get(EnumFixture.UNKNOWN_COLUMN));
    }

    @Test
    void testGetUnmappedName() {
        assertThrows(IllegalArgumentException.class, () -> assertNull(recordWithHeader.get("fourth")));
    }

    @Test
    void testGetUnmappedNegativeInt() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> recordWithHeader.get(Integer.MIN_VALUE));
    }

    @Test
    void testGetUnmappedPositiveInt() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> recordWithHeader.get(Integer.MAX_VALUE));
    }

    @Test
    void testGetWithEnum() {
        assertEquals(recordWithHeader.get("FIRST"), recordWithHeader.get(EnumHeader.FIRST));
        assertEquals(recordWithHeader.get("SECOND"), recordWithHeader.get(EnumHeader.SECOND));
        assertThrows(IllegalArgumentException.class, () -> recordWithHeader.get(EnumFixture.UNKNOWN_COLUMN));
    }

    @Test
    void testIsConsistent() {
        assertTrue(record.isConsistent());
        assertTrue(recordWithHeader.isConsistent());
        final Map<String, Integer> map = recordWithHeader.getParser().getHeaderMap();
        map.put("fourth", Integer.valueOf(4));
        // We are working on a copy of the map, so the record should still be OK.
        assertTrue(recordWithHeader.isConsistent());
    }

    @Test
    void testIsInconsistent() throws IOException {
        final String[] headers = { "first", "second", "third" };
        final String rowData = StringUtils.join(values, ',');
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader(headers).parse(new StringReader(rowData))) {
            final Map<String, Integer> map = parser.getHeaderMapRaw();
            final CSVRecord record1 = parser.iterator().next();
            map.put("fourth", Integer.valueOf(4));
            assertFalse(record1.isConsistent());
        }
    }

    @Test
    void testIsMapped() {
        assertFalse(record.isMapped("first"));
        assertTrue(recordWithHeader.isMapped(EnumHeader.FIRST.name()));
        assertFalse(recordWithHeader.isMapped("fourth"));
    }

    @Test
    void testIsSetInt() {
        assertFalse(record.isSet(-1));
        assertTrue(record.isSet(0));
        assertTrue(record.isSet(2));
        assertFalse(record.isSet(3));
        assertTrue(recordWithHeader.isSet(1));
        assertFalse(recordWithHeader.isSet(1000));
    }

    @Test
    void testIsSetString() {
        assertFalse(record.isSet("first"));
        assertTrue(recordWithHeader.isSet(EnumHeader.FIRST.name()));
        assertFalse(recordWithHeader.isSet("DOES NOT EXIST"));
    }

    @Test
    void testIterator() {
        int i = 0;
        for (final String value : record) {
            assertEquals(values[i], value);
            i++;
        }
    }

    @Test
    void testPutInMap() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        this.recordWithHeader.putIn(map);
        validateMap(map, false);
        // Test that we can compile with assignment to the same map as the param.
        final TreeMap<String, String> map2 = recordWithHeader.putIn(new TreeMap<>());
        validateMap(map2, false);
    }

    @Test
    void testRemoveAndAddColumns() throws IOException {
        // do:
        try (CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)) {
            final Map<String, String> map = recordWithHeader.toMap();
            map.remove("OldColumn");
            map.put("ZColumn", "NewValue");
            // check:
            final ArrayList<String> list = new ArrayList<>(map.values());
            list.sort(null);
            printer.printRecord(list);
            assertEquals("A,B,C,NewValue" + CSVFormat.DEFAULT.getRecordSeparator(), printer.getOut().toString());
        }
    }

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        final CSVRecord shortRec;
        try (CSVParser parser = CSVParser.parse("A,B\n#my comment\nOne,Two", CSVFormat.DEFAULT.withHeader().withCommentMarker('#'))) {
            shortRec = parser.iterator().next();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(shortRec);
        }
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            final Object object = ois.readObject();
            assertInstanceOf(CSVRecord.class, object);
            final CSVRecord rec = (CSVRecord) object;
            assertEquals(1L, rec.getRecordNumber());
            assertEquals("One", rec.get(0));
            assertEquals("Two", rec.get(1));
            assertEquals(2, rec.size());
            assertEquals(shortRec.getCharacterPosition(), rec.getCharacterPosition());
            assertEquals("my comment", rec.getComment());
            // The parser is not serialized
            assertNull(rec.getParser());
            // Check all header map functionality is absent
            assertTrue(rec.isConsistent());
            assertFalse(rec.isMapped("A"));
            assertFalse(rec.isSet("A"));
            assertEquals(0, rec.toMap().size());
            // This will throw
            assertThrows(IllegalStateException.class, () -> rec.get("A"));
        }
    }

    @Test
    void testStream() {
        final AtomicInteger i = new AtomicInteger();
        record.stream().forEach(value -> {
            assertEquals(values[i.get()], value);
            i.incrementAndGet();
        });
    }

    @Test
    void testToListAdd() {
        final String[] expected = values.clone();
        final List<String> list = record.toList();
        list.add("Last");
        assertEquals("Last", list.get(list.size() - 1));
        assertEquals(list.size(), values.length + 1);
        assertArrayEquals(expected, values);
    }

    @Test
    void testToListFor() {
        int i = 0;
        for (final String value : record.toList()) {
            assertEquals(values[i], value);
            i++;
        }
    }

    @Test
    void testToListForEach() {
        final AtomicInteger i = new AtomicInteger();
        record.toList().forEach(e -> {
            assertEquals(values[i.getAndIncrement()], e);
        });
    }

    @Test
    void testToListSet() {
        final String[] expected = values.clone();
        final List<String> list = record.toList();
        list.set(list.size() - 1, "Last");
        assertEquals("Last", list.get(list.size() - 1));
        assertEquals(list.size(), values.length);
        assertArrayEquals(expected, values);
    }

    @Test
    void testToMap() {
        final Map<String, String> map = this.recordWithHeader.toMap();
        validateMap(map, true);
    }

    @Test
    void testToMapWithNoHeader() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b", CSVFormat.newFormat(','))) {
            final CSVRecord shortRec = parser.iterator().next();
            final Map<String, String> map = shortRec.toMap();
            assertNotNull(map, "Map is not null.");
            assertTrue(map.isEmpty(), "Map is empty.");
        }
    }

    @Test
    void testToMapWithShortRecord() throws Exception {
        try (CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final CSVRecord shortRec = parser.iterator().next();
            shortRec.toMap();
        }
    }

    @Test
    void testToString() {
        assertNotNull(recordWithHeader.toString());
        assertTrue(recordWithHeader.toString().contains("comment="));
        assertTrue(recordWithHeader.toString().contains("recordNumber="));
        assertTrue(recordWithHeader.toString().contains("values="));
    }

    private void validateMap(final Map<String, String> map, final boolean allowsNulls) {
        assertTrue(map.containsKey(EnumHeader.FIRST.name()));
        assertTrue(map.containsKey(EnumHeader.SECOND.name()));
        assertTrue(map.containsKey(EnumHeader.THIRD.name()));
        assertFalse(map.containsKey("fourth"));
        if (allowsNulls) {
            assertFalse(map.containsKey(null));
        }
        assertEquals("A", map.get(EnumHeader.FIRST.name()));
        assertEquals("B", map.get(EnumHeader.SECOND.name()));
        assertEquals("C", map.get(EnumHeader.THIRD.name()));
        assertNull(map.get("fourth"));
    }
}
