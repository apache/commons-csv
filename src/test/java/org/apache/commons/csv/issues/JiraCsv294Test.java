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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

public class JiraCsv294Test {

    private static void testInternal(final CSVFormat format, final String expectedSubstring) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(bos, StandardCharsets.UTF_8), format)) {
            printer.printRecord("a", "b \"\"", "c");
        }
        final byte[] written = bos.toByteArray();
        final String writtenString = new String(written, StandardCharsets.UTF_8);
        assertTrue(writtenString.contains(expectedSubstring));
        try (CSVParser parser = CSVParser.builder().setReader(new InputStreamReader(new ByteArrayInputStream(written), StandardCharsets.UTF_8))
                .setFormat(format).get()) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            final CSVRecord record = records.get(0);
            assertEquals("a", record.get(0));
            assertEquals("b \"\"", record.get(1));
            assertEquals("c", record.get(2));
        }
    }

    @Test
    void testDefaultCsvFormatWithBackslashEscapeWorks() throws IOException {
        testInternal(CSVFormat.Builder.create().setEscape('\\').get(), ",\"b \\\"\\\"\",");
    }

    @Test
    void testDefaultCsvFormatWithNullEscapeWorks() throws IOException {
        testInternal(CSVFormat.Builder.create().setEscape(null).get(), ",\"b \"\"\"\"\",");
    }

    @Test
    void testDefaultCsvFormatWithQuoteEscapeWorks() throws IOException {
        // this one doesn't actually work but should behave like setEscape(null)
        // Printer is writing the expected content but Parser is unable to consume it
        testInternal(CSVFormat.Builder.create().setEscape('"').get(), ",\"b \"\"\"\"\",");
    }

    @Test
    void testDefaultCsvFormatWorks() throws IOException {
        testInternal(CSVFormat.Builder.create().get(), ",\"b \"\"\"\"\",");
    }
}
