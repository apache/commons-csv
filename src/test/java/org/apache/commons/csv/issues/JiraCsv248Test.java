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

package org.apache.commons.csv.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

class JiraCsv248Test {

    private static InputStream getTestInput() {
        return ClassLoader.getSystemClassLoader().getResourceAsStream("org/apache/commons/csv/CSV-248/csvRecord.bin");
    }

    /**
     * Test deserialization of a CSVRecord created using version 1.6.
     *
     * <p>
     * This test asserts that serialization from 1.8 onwards is consistent with previous versions. Serialization was
     * broken in version 1.7.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException If the CSVRecord cannot be deserialized
     */
    @Test
    void testJiraCsv248() throws IOException, ClassNotFoundException {
        // Record was originally created using CSV version 1.6 with the following code:
        // try (CSVParser parser = CSVParser.parse("A,B\n#my comment\nOne,Two",
        // CSVFormat.DEFAULT.builder().setHeader().setCommentMarker('#'))) {
        // CSVRecord rec = parser.iterator().next();
        // }
        try (InputStream in = getTestInput(); ObjectInputStream ois = new ObjectInputStream(in)) {
            final Object object = ois.readObject();
            assertInstanceOf(CSVRecord.class, object);
            final CSVRecord rec = (CSVRecord) object;
            assertEquals(1L, rec.getRecordNumber());
            assertEquals("One", rec.get(0));
            assertEquals("Two", rec.get(1));
            assertEquals(2, rec.size());
            // The comment and whitespace are ignored so this is not 17 but 4
            assertEquals(4, rec.getCharacterPosition());
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
}
