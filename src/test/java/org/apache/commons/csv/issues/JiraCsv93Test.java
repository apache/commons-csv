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
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

/**
 * Add more tests about null value.
 * <p>
 * QuoteMode:ALL_NON_NULL (Quotes all non-null fields, null will not be quoted but not null will be quoted). when
 * withNullString("NULL"), NULL String value ("NULL") and null value (null) will be formatted as '"NULL",NULL'. So it
 * also should be parsed as NULL String value and null value (["NULL", null]), It should be distinguish in parsing. And
 * when don't set nullString in CSVFormat, String '"",' should be parsed as "" and null (["", null]) according to null
 * will not be quoted but not null will be quoted. QuoteMode:NON_NUMERIC, same as ALL_NON_NULL.
 * </p>
 * <p>
 * This can solve the problem of distinguishing between empty string columns and absent value columns which just like
 * Jira CSV-253 to a certain extent.
 * </p>
 */
public class JiraCsv93Test {
    private static Object[] objects1 = {"abc", "", null, "a,b,c", 123};

    private static Object[] objects2 = {"abc", "NULL", null, "a,b,c", 123};

    private void every(final CSVFormat csvFormat, final Object[] objects, final String format, final String[] data)
        throws IOException {
        final String source = csvFormat.format(objects);
        assertEquals(format, csvFormat.format(objects));
        try (final CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.iterator().next();
            for (int i = 0; i < data.length; i++) {
                assertEquals(csvRecord.get(i), data[i]);
            }
        }
    }

    @Test
    public void testWithNotSetNullString() throws IOException {
        // @formatter:off
        every(CSVFormat.DEFAULT,
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.ALL).build(),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.ALL_NON_NULL).build(),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.MINIMAL).build(),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setEscape('?').setQuoteMode(QuoteMode.NONE).build(),
                objects1,
                "abc,,,a?,b?,c,123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.NON_NUMERIC).build(),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",123",
                new String[]{"abc", "", null, "a,b,c", "123"});
        // @formatter:on
    }

    @Test
    public void testWithSetNullStringEmptyString() throws IOException {
        // @formatter:off
        every(CSVFormat.DEFAULT.builder().setNullString("").build(),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("").setQuoteMode(QuoteMode.ALL).build(),
                objects1,
                "\"abc\",\"\",\"\",\"a,b,c\",\"123\"",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("").setQuoteMode(QuoteMode.ALL_NON_NULL).build(),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("").setQuoteMode(QuoteMode.MINIMAL).build(),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("").setEscape('?').setQuoteMode(QuoteMode.NONE).build(),
                objects1,
                "abc,,,a?,b?,c,123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("").setQuoteMode(QuoteMode.NON_NUMERIC).build(),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",123",
                new String[]{"abc", "", null, "a,b,c", "123"});
        // @formatter:on
    }

    @Test
    public void testWithSetNullStringNULL() throws IOException {
        // @formatter:off
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").build(),
                objects2,
                "abc,NULL,NULL,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").setQuoteMode(QuoteMode.ALL).build(),
                objects2,
                "\"abc\",\"NULL\",\"NULL\",\"a,b,c\",\"123\"",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").setQuoteMode(QuoteMode.ALL_NON_NULL).build(),
                objects2,
                "\"abc\",\"NULL\",NULL,\"a,b,c\",\"123\"",
                new String[]{"abc", "NULL", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").setQuoteMode(QuoteMode.MINIMAL).build(),
                objects2,
                "abc,NULL,NULL,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").setEscape('?').setQuoteMode(QuoteMode.NONE).build(),
                objects2,
                "abc,NULL,NULL,a?,b?,c,123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.builder().setNullString("NULL").setQuoteMode(QuoteMode.NON_NUMERIC).build(),
                objects2,
                "\"abc\",\"NULL\",NULL,\"a,b,c\",123",
                new String[]{"abc", "NULL", null, "a,b,c", "123"});
        // @formatter:on
    }
}
