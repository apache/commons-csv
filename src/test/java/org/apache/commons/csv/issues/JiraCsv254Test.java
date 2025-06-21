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

import static org.apache.commons.csv.CsvAssertions.assertValuesEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/CSV-254.
 */
class JiraCsv254Test {

    @Test
    void test() throws IOException {
        final CSVFormat csvFormat = CSVFormat.POSTGRESQL_CSV;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("src/test/resources/org/apache/commons/csv/CSV-254/csv-254.csv"),
                StandardCharsets.UTF_8); CSVParser parser = csvFormat.parse(reader)) {
            final Iterator<CSVRecord> csvRecords = parser.iterator();
            assertValuesEquals(new String[] { "AA", "33", null }, csvRecords.next());
            assertValuesEquals(new String[] { "AA", null, "" }, csvRecords.next());
            assertValuesEquals(new String[] { null, "33", "CC" }, csvRecords.next());
        }
    }
}
