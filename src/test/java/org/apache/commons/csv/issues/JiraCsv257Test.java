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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/CSV-257
 */
class JiraCsv257Test {

    private static final String INPUT = ",";

    @Test
    void testHeaderBuilder() throws IOException {
        // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180.builder()
                .setDelimiter(INPUT.charAt(0))
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .get();
        // @formatter:on
        // Document the current behavior: Throw a IllegalArgumentException is a header name is missing.
        assertThrows(IllegalArgumentException.class, () -> {
            try (CSVParser parser = CSVParser.parse(INPUT, format)) {
                // empty
            }
        });
    }

    @Test
    void testHeaderDepreacted() throws IOException {
        // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180
                .withDelimiter(INPUT.charAt(0))
                .withFirstRecordAsHeader()
                .withIgnoreSurroundingSpaces();
        // @formatter:on
        // Document the current behavior: Throw a IllegalArgumentException is a header name is missing.
        assertThrows(IllegalArgumentException.class, () -> {
            try (CSVParser parser = new CSVParser(new StringReader(INPUT), format)) {
                // empty
            }
        });
    }

    @Test
    void testNoHeaderBuilder() throws IOException {
        // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180.builder()
                .setDelimiter(INPUT.charAt(0))
                .setIgnoreSurroundingSpaces(true)
                .get();
        // @formatter:on
        try (CSVParser parser = CSVParser.parse(INPUT, format)) {
            // empty
        }
    }
}
