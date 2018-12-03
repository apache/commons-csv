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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CSVRecordTest {

    private enum EnumFixture { first, second, third, UNKNOWN_COLUMN }

    private String[] values;
    private CSVRecord record, recordWithHeader;
    private Map<String, Integer> header;

    @Before
    public void setUp() throws Exception {
        values = new String[] { "A", "B", "C" };
        record = new CSVRecord(values, null, null, 0, -1);
        header = new HashMap<>();
        header.put("first", Integer.valueOf(0));
        header.put("second", Integer.valueOf(1));
        header.put("third", Integer.valueOf(2));
        recordWithHeader = new CSVRecord(values, header, null, 0, -1);
    }

    @Test
    public void testGetInt() {
        assertEquals(values[0], record.get(0));
        assertEquals(values[1], record.get(1));
        assertEquals(values[2], record.get(2));
    }

    @Test
    public void testGetString() {
        assertEquals(values[0], recordWithHeader.get("first"));
        assertEquals(values[1], recordWithHeader.get("second"));
        assertEquals(values[2], recordWithHeader.get("third"));
    }

    @Test
    public void testGetEnum() {
        assertEquals(values[0], recordWithHeader.get(EnumFixture.first));
        assertEquals(values[1], recordWithHeader.get(EnumFixture.second));
        assertEquals(values[2], recordWithHeader.get(EnumFixture.third));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringInconsistentRecord() {
        header.put("fourth", Integer.valueOf(4));
        recordWithHeader.get("fourth");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetStringNoHeader() {
        record.get("first");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnmappedEnum() {
        assertNull(recordWithHeader.get(EnumFixture.UNKNOWN_COLUMN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnmappedName() {
        assertNull(recordWithHeader.get("fourth"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetUnmappedNegativeInt() {
        assertNull(recordWithHeader.get(Integer.MIN_VALUE));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetUnmappedPositiveInt() {
        assertNull(recordWithHeader.get(Integer.MAX_VALUE));
    }

    @Test
    public void testIsConsistent() {
        assertTrue(record.isConsistent());
        assertTrue(recordWithHeader.isConsistent());

        header.put("fourth", Integer.valueOf(4));
        assertFalse(recordWithHeader.isConsistent());
    }

    @Test
    public void testIsMapped() {
        assertFalse(record.isMapped("first"));
        assertTrue(recordWithHeader.isMapped("first"));
        assertFalse(recordWithHeader.isMapped("fourth"));
    }

    @Test
    public void testIsSet() {
        assertFalse(record.isSet("first"));
        assertTrue(recordWithHeader.isSet("first"));
        assertFalse(recordWithHeader.isSet("fourth"));
    }

    @Test
    public void testIterator() {
        int i = 0;
        for (final String value : record) {
            assertEquals(values[i], value);
            i++;
        }
    }

    @Test
    public void testPutInMap() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        this.recordWithHeader.putIn(map);
        this.validateMap(map, false);
        // Test that we can compile with assigment to the same map as the param.
        final TreeMap<String, String> map2 = recordWithHeader.putIn(new TreeMap<String, String>());
        this.validateMap(map2, false);
    }

    @Test
    public void testRemoveAndAddColumns() throws IOException {
        // do:
        try (final CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)) {
            final Map<String, String> map = recordWithHeader.toMap();
            map.remove("OldColumn");
            map.put("ZColumn", "NewValue");
            // check:
            final ArrayList<String> list = new ArrayList<>(map.values());
            Collections.sort(list);
            printer.printRecord(list);
            Assert.assertEquals("A,B,C,NewValue" + CSVFormat.DEFAULT.getRecordSeparator(), printer.getOut().toString());
        }
    }

    @Test
    public void testToMap() {
        final Map<String, String> map = this.recordWithHeader.toMap();
        this.validateMap(map, true);
    }

    @Test
    public void testToMapWithShortRecord() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final CSVRecord shortRec = parser.iterator().next();
            shortRec.toMap();
        }
    }

    @Test
    public void testToMapWithNoHeader() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", CSVFormat.newFormat(','))) {
            final CSVRecord shortRec = parser.iterator().next();
            final Map<String, String> map = shortRec.toMap();
            assertNotNull("Map is not null.", map);
            assertTrue("Map is empty.", map.isEmpty());
        }
    }

    private void validateMap(final Map<String, String> map, final boolean allowsNulls) {
        assertTrue(map.containsKey("first"));
        assertTrue(map.containsKey("second"));
        assertTrue(map.containsKey("third"));
        assertFalse(map.containsKey("fourth"));
        if (allowsNulls) {
            assertFalse(map.containsKey(null));
        }
        assertEquals("A", map.get("first"));
        assertEquals("B", map.get("second"));
        assertEquals("C", map.get("third"));
        assertEquals(null, map.get("fourth"));
    }

    @Test
    public void testToString() {
        assertEquals("CSVRecord [comment=null, mapping=null, recordNumber=0, values=[A, B, C]]", record.toString());
    }
}
