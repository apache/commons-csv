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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

public class JiraCsv288Test {

    private void print(final CSVRecord csvRecord, final CSVPrinter csvPrinter) throws IOException {
        for (final String value : csvRecord) {
            csvPrinter.print(value);
        }
    }

    @Test
    // Before fix:
    // expected: <a,b,c,d,,f> but was: <a,b,c,d,|f>
    public void testParseWithABADelimiter() throws Exception {
        final Reader in = new StringReader("a|~|b|~|c|~|d|~||~|f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser parser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("|~|").build())) {
            for (final CSVRecord csvRecord : parser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Before fix:
    // expected: <a,b,c,d,,f> but was: <a,b|c,d,|f>
    public void testParseWithDoublePipeDelimiter() throws Exception {
        final Reader in = new StringReader("a||b||c||d||||f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("||").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix

    public void testParseWithDoublePipeDelimiterDoubleCharValue() throws Exception {
        final Reader in = new StringReader("a||bb||cc||dd||f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("||").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,bb,cc,dd,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Before fix:
    // expected: <a,b,c,d,,f,> but was: <a,b|c,d,|f>
    public void testParseWithDoublePipeDelimiterEndsWithDelimiter() throws Exception {
        final Reader in = new StringReader("a||b||c||d||||f||");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("||").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f,", stringBuilder.toString());
            }
        }
    }

    @Test
    // Before fix:
    // expected: <a,b||c,d,,f> but was: <a,b||c,d,|f>
    public void testParseWithDoublePipeDelimiterQuoted() throws Exception {
        final Reader in = new StringReader("a||\"b||c\"||d||||f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("||").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b||c,d,,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix
    public void testParseWithSinglePipeDelimiterEndsWithDelimiter() throws Exception {
        final Reader in = new StringReader("a|b|c|d||f|");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f,", stringBuilder.toString());
            }
        }
    }

    @Test
    // Before fix:
    // expected: <a,b,c,d,,f> but was: <a,b|c,d,|f>
    public void testParseWithTriplePipeDelimiter() throws Exception {
        final Reader in = new StringReader("a|||b|||c|||d||||||f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("|||").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix
    public void testParseWithTwoCharDelimiter1() throws Exception {
        final Reader in = new StringReader("a~|b~|c~|d~|~|f");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("~|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix
    public void testParseWithTwoCharDelimiter2() throws Exception {
        final Reader in = new StringReader("a~|b~|c~|d~|~|f~");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("~|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f~", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix
    public void testParseWithTwoCharDelimiter3() throws Exception {
        final Reader in = new StringReader("a~|b~|c~|d~|~|f|");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("~|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f|", stringBuilder.toString());
            }
        }
    }

    @Test
    // Regression, already passed before fix
    public void testParseWithTwoCharDelimiter4() throws Exception {
        final Reader in = new StringReader("a~|b~|c~|d~|~|f~~||g");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("~|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f~,|g", stringBuilder.toString());
            }
        }
    }

    @Test
    // Before fix:
    // expected: <a,b,c,d,,f,> but was: <a,b,c,d,,f>
    public void testParseWithTwoCharDelimiterEndsWithDelimiter() throws Exception {
        final Reader in = new StringReader("a~|b~|c~|d~|~|f~|");
        final StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
                CSVParser csvParser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("~|").build())) {
            for (final CSVRecord csvRecord : csvParser) {
                print(csvRecord, csvPrinter);
                assertEquals("a,b,c,d,,f,", stringBuilder.toString());
            }
        }
    }
}