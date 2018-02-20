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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

    private enum EnumFixture { UNKNOWN_COLUMN }

    protected String[] values;
    protected CSVRecord record, recordWithHeader;
    protected Map<String, Integer> header;

    @Before
    public void setUp() throws Exception {
        values = new String[] { "A", "B", "C" };
        record = newRecord();
        header = new HashMap<>();
        header.put("first", Integer.valueOf(0));
        header.put("second", Integer.valueOf(1));
        header.put("third", Integer.valueOf(2));
        recordWithHeader = newRecordWithHeader();
        validate(recordWithHeader);
    }

    protected CSVRecord newRecord() {
        return new CSVRecord(values, null, null, 0, -1);
    }

    protected void validate(final CSVRecord anyRecord) {
        Assert.assertEquals(CSVRecord.class, anyRecord.getClass());
    }

    protected CSVRecord newRecordWithHeader() {
        return new CSVRecord(values, header, null, 0, -1);
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
        try (final CSVPrinter printer = new CSVPrinter(new StringBuilder(), createDefaultFormat())) {
            final Map<String, String> map = recordWithHeader.toMap();
            map.remove("OldColumn");
            map.put("ZColumn", "NewValue");
            // check:
            final ArrayList<String> list = new ArrayList<>(map.values());
            Collections.sort(list);
            printer.printRecord(list);
            Assert.assertEquals("A,B,C,NewValue" + createDefaultFormat().getRecordSeparator(), printer.getOut().toString());
        }
    }

    protected CSVFormat createDefaultFormat() {
        return CSVFormat.DEFAULT;
    }

    @Test
    public void testToMap() {
        final Map<String, String> map = this.recordWithHeader.toMap();
        this.validateMap(map, true);
    }

    @Test
    public void testToMapWithShortRecord() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", createDefaultFormat().withHeader("A", "B", "C"))) {
            final CSVRecord shortRec = parser.iterator().next();
            validate(shortRec);
            shortRec.toMap();
        }
    }

    @Test
    public void isMutable() { 
    	assertFalse(record.isMutable());
    	assertFalse(recordWithHeader.isMutable());
    }
    
    @Test
    public void testMutable() throws Exception {
    	CSVRecord mutable = record.mutable();
    	assertTrue(mutable.isMutable());
    	if (record.isMutable()) { 
    		assertSame(record, mutable);
    	} else {    	
    		assertNotSame(record, mutable);
    	}    	
    }
    
    @Test
    public void testImmutable() throws Exception {
    	CSVRecord immutable = record.immutable();
    	assertFalse(immutable.isMutable());
    	assertSame(immutable, immutable.immutable());
    	assertNotSame(immutable, immutable.mutable());
    	if (record.isMutable()) { 
    		assertNotSame(record, immutable);
    	} else {    	
    		assertSame(record, immutable);
    	}
    }
    
    @Test
    public void testWithValue() throws Exception {
    	assertEquals("A", record.get(0));
    	CSVRecord newR = record.withValue(0, "X");
    	assertEquals("X", newR.get(0));    	
    	if (record.isMutable()) {
    		assertSame(record, newR);
    	} else {
    		// unchanged
    		assertEquals("A", record.get(0));
    	}
    }
    
    @Test
    public void testWithValueName() throws Exception {
    	assertEquals("B", recordWithHeader.get("second"));
    	CSVRecord newR = recordWithHeader.withValue("second", "Y");
    	assertEquals("Y", newR.get("second"));    	
    	if (record.isMutable()) {
    		assertSame(recordWithHeader, newR);
    	} else {
    		// unchanged
    		assertEquals("B", recordWithHeader.get("second"));
    	}
    }
    
    @Test
    public void testToMapWithNoHeader() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", createCommaFormat())) {
            final CSVRecord shortRec = parser.iterator().next();
            validate(shortRec);
            final Map<String, String> map = shortRec.toMap();
            assertNotNull("Map is not null.", map);
            assertTrue("Map is empty.", map.isEmpty());
        }
    }

    protected CSVFormat createCommaFormat() {
        return CSVFormat.newFormat(',');
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

}
