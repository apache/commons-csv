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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

public class JiraCsv149Test {

    private static final String CR_LF = "\r\n";

    @Test
    public void testJiraCsv149EndWithEOL() throws IOException {
        testJiraCsv149EndWithEolAtEof(true);
    }

    private void testJiraCsv149EndWithEolAtEof(final boolean eolAtEof) throws IOException {
        String source = "A,B,C,D" + CR_LF + "a1,b1,c1,d1" + CR_LF + "a2,b2,c2,d2";
        if (eolAtEof) {
            source += CR_LF;
        }
        final StringReader records = new StringReader(source);
        // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setQuote('"')
            .build();
        // @formatter:on
        int lineCounter = 2;
        try (final CSVParser parser = new CSVParser(records, format)) {
            for (final CSVRecord record : parser) {
                assertNotNull(record);
                assertEquals(lineCounter++, parser.getCurrentLineNumber());
            }
        }
    }

    @Test
    public void testJiraCsv149EndWithoutEOL() throws IOException {
        testJiraCsv149EndWithEolAtEof(false);
    }
}
