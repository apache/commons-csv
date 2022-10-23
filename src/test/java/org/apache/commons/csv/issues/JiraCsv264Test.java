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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.junit.jupiter.api.Test;

/**
 * When {@link CSVFormat#withHeader(String...)} is not null; duplicate headers
 * with empty strings should not be allowed.
 *
 * @see <a href="https://issues.apache.org/jira/browse/CSV-264">Jira Ticker</a>
 */
public class JiraCsv264Test {

    private static final String CSV_STRING = "\"\",\"B\",\"\"\n" +
                                             "\"1\",\"2\",\"3\"\n" +
                                             "\"4\",\"5\",\"6\"";

    /**
     * A CSV file with a random gap in the middle.
     */
    private static final String CSV_STRING_GAP = "\"A\",\"B\",\"\",\"\",\"E\"\n" +
                                                 "\"1\",\"2\",\"\",\"\",\"5\"\n" +
                                                 "\"6\",\"7\",\"\",\"\",\"10\"";

    @Test
    public void testJiraCsv264() {
        final CSVFormat csvFormat = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .setAllowMissingColumnNames(true)
            .build();

        try (StringReader reader = new StringReader(CSV_STRING)) {
            assertThrows(IllegalArgumentException.class, () -> csvFormat.parse(reader));
        }
    }

    @Test
    public void testJiraCsv264WithGapAllowEmpty() throws IOException {
        final CSVFormat csvFormat = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY)
            .setAllowMissingColumnNames(true)
            .build();

        try (StringReader reader = new StringReader(CSV_STRING_GAP); final CSVParser parser = csvFormat.parse(reader)) {
            // empty
        }
    }

    @Test
    public void testJiraCsv264WithGapDisallow() {
        final CSVFormat csvFormat = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .setAllowMissingColumnNames(true)
            .build();

        try (StringReader reader = new StringReader(CSV_STRING_GAP)) {
            assertThrows(IllegalArgumentException.class, () -> csvFormat.parse(reader));
        }
    }
}
