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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests https://issues.apache.org/jira/browse/CSV-312
 */
class JiraCsv312Test {

    private static String csv(final String headerLineBreak) {
        return "\"SYMBOL" + headerLineBreak + "\",\"COMPANY" + headerLineBreak + "\"\r\n" +
                "\"AURIONPRO\",\"Aurionpro Solutions Limited\"\r\n" +
                "\"CUPID\",\"Cupid Limited\"\r\n";
    }

    @ParameterizedTest
    @ValueSource(strings = { "\n", "\r\n" })
    void testAutomaticHeader(final String headerLineBreak) throws IOException {
        final CSVFormat format = CSVFormat.RFC4180.builder().setHeader().get();
        try (CSVParser parser = CSVParser.parse(csv(headerLineBreak), format)) {
            assertEquals(Arrays.asList("SYMBOL" + headerLineBreak, "COMPANY" + headerLineBreak), parser.getHeaderNames());
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("AURIONPRO", records.get(0).get("SYMBOL" + headerLineBreak));
            assertEquals("Aurionpro Solutions Limited", records.get(0).get("COMPANY" + headerLineBreak));
            assertEquals("CUPID", records.get(1).get("SYMBOL" + headerLineBreak));
            assertEquals("Cupid Limited", records.get(1).get("COMPANY" + headerLineBreak));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "\n", "\r\n" })
    void testWithoutHeaderConfiguration(final String headerLineBreak) throws IOException {
        try (CSVParser parser = CSVParser.parse(csv(headerLineBreak), CSVFormat.RFC4180)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(3, records.size());
            assertEquals(Arrays.asList("SYMBOL" + headerLineBreak, "COMPANY" + headerLineBreak), records.get(0).toList());
            assertEquals(Arrays.asList("AURIONPRO", "Aurionpro Solutions Limited"), records.get(1).toList());
            assertEquals(Arrays.asList("CUPID", "Cupid Limited"), records.get(2).toList());
        }
    }
}
