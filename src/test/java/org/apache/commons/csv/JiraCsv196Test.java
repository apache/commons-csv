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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class JiraCsv196Test {

    private Reader getTestInput(final String path) {
        return new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path));
    }

    @Test
    public void testParseFourBytes() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setDelimiter(',').setQuote('\'').get();
        // @formatter:off
        try (CSVParser parser = new CSVParser.Builder()
                .setFormat(format)
                .setReader(getTestInput("org/apache/commons/csv/CSV-196/emoji.csv"))
                .setCharset(StandardCharsets.UTF_8)
                .setTrackBytes(true).get()) {
            // @formatter:on
            final long[] charByteKey = { 0, 84, 701, 1318, 1935 };
            int idx = 0;
            for (final CSVRecord record : parser) {
                assertEquals(charByteKey[idx++], record.getBytePosition(), "index " + idx);
            }
        }
    }

    @Test
    public void testParseThreeBytes() throws IOException {
        final CSVFormat format = CSVFormat.Builder.create().setDelimiter(',').setQuote('\'').get();
        // @formatter:off
        try (CSVParser parser = new CSVParser.Builder()
                .setFormat(format)
                .setReader(getTestInput("org/apache/commons/csv/CSV-196/japanese.csv"))
                .setCharset(StandardCharsets.UTF_8)
                .setTrackBytes(true).get()) {
            // @formatter:on
            final long[] charByteKey = { 0, 89, 242, 395 };
            int idx = 0;
            for (final CSVRecord record : parser) {
                assertEquals(charByteKey[idx++], record.getBytePosition(), "index " + idx);
            }
        }
    }
}
