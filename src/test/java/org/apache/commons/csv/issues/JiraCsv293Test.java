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

package org.apache.commons.csv.issues;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Add support for multiple null String values
public class JiraCsv293Test {

    @Test
    public void setMultipleNullStrings() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setNullStrings(new String[]{"Aaa", "Bbb"}).build();

        final String code = "Aaa,Bbb,Ccc,Ddd";
        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNull(records.get(0).values()[0]);
            assertNull(records.get(0).values()[1]);
            assertNotNull(records.get(0).values()[2]);
            assertNotNull(records.get(0).values()[3]);
        }
    }

    @Test
    public void setOneNullStringByUsingArray() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setNullStrings(new String[]{"Aaa"}).build();

        final String code = "Aaa,Bbb,Ccc,Ddd";
        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNull(records.get(0).values()[0]);
            assertNotNull(records.get(0).values()[1]);
            assertNotNull(records.get(0).values()[2]);
            assertNotNull(records.get(0).values()[3]);
        }
    }

    @Test
    public void setNullStrings() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setNullStrings(null).build();

        final String code = "Aaa,Bbb,Ccc,Ddd";
        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNotNull(records.get(0).values()[0]);
            assertNotNull(records.get(0).values()[1]);
            assertNotNull(records.get(0).values()[2]);
            assertNotNull(records.get(0).values()[3]);
        }
    }

    @Test
    public void setOneNullString() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setNullString("Aaa").build();

        final String code = "Aaa,Bbb,Ccc,Ddd";
        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNull(records.get(0).values()[0]);
            assertNotNull(records.get(0).values()[1]);
            assertNotNull(records.get(0).values()[2]);
            assertNotNull(records.get(0).values()[3]);
        }
    }

    @Test
    public void getNullStrings() {
        final CSVFormat format = CSVFormat.Builder.create().build();
        assertNull(format.getNullStrings());

        final CSVFormat format2 = CSVFormat.Builder.create().setNullString("null").build();
        assertNotNull(format2.getNullStrings());
        assertArrayEquals(new String[]{"null"}, format2.getNullStrings());

        final CSVFormat format3 = CSVFormat.Builder.create().setNullStrings(new String[]{"null"}).build();
        assertNotNull(format3.getNullStrings());
        assertArrayEquals(new String[]{"null"}, format3.getNullStrings());
    }
}
