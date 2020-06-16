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
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Add more tests about null value.
 * <p>
 * QuoteMode:ALL_NON_NULL (Quotes all non-null fields, null will not be quoted but not null will be quoted). when
 * withNullString("NULL"), NULL String value ("NULL") and null value (null) will be formatted as '"NULL",NULL'. So it
 * also should be parsed as NULL String value and null value (["NULL", null]), It should be distinguish in parsing.
 * And when don't set nullString in CSVFormat, String '"",' should be parsed as "" and null (["", null]) according to
 * null will not be quoted but not null will be quoted.
 * QuoteMode:NON_NUMERIC, same as ALL_NON_NULL.
 * </p>
 * <p>
 * This can solve the problem of distinguishing between empty string columns and absent value columns which just like Jira
 * CSV-253 to a certain extent
 * </p>
 */
public class JiraCsv93Test {
    private static Object[] objects1 = new Object[]{"abc", "", null, "a,b,c", 123};

    private static Object[] objects2 = new Object[]{"abc", "NULL", null, "a,b,c", 123};

    @Test
    public void testWithNotSetNullString() throws IOException {
        every(CSVFormat.DEFAULT,
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withEscape('?').withQuoteMode(QuoteMode.NONE),
                objects1,
                "abc,,,a?,b?,c,123",
                new String[]{"abc", "", "", "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",123",
                new String[]{"abc", "", null, "a,b,c", "123"});
    }

    @Test
    public void testWithSetNullStringEmptyString() throws IOException {
        every(CSVFormat.DEFAULT.withNullString(""),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("").withQuoteMode(QuoteMode.ALL),
                objects1,
                "\"abc\",\"\",\"\",\"a,b,c\",\"123\"",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("").withQuoteMode(QuoteMode.ALL_NON_NULL),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",\"123\"",
                new String[]{"abc", "", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("").withQuoteMode(QuoteMode.MINIMAL),
                objects1,
                "abc,,,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("").withEscape('?').withQuoteMode(QuoteMode.NONE),
                objects1,
                "abc,,,a?,b?,c,123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("").withQuoteMode(QuoteMode.NON_NUMERIC),
                objects1,
                "\"abc\",\"\",,\"a,b,c\",123",
                new String[]{"abc", "", null, "a,b,c", "123"});
    }

    @Test
    public void testWithSetNullStringNULL() throws IOException {
        every(CSVFormat.DEFAULT.withNullString("NULL"),
                objects2,
                "abc,NULL,NULL,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.ALL),
                objects2,
                "\"abc\",\"NULL\",\"NULL\",\"a,b,c\",\"123\"",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.ALL_NON_NULL),
                objects2,
                "\"abc\",\"NULL\",NULL,\"a,b,c\",\"123\"",
                new String[]{"abc", "NULL", null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.MINIMAL),
                objects2,
                "abc,NULL,NULL,\"a,b,c\",123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("NULL").withEscape('?').withQuoteMode(QuoteMode.NONE),
                objects2,
                "abc,NULL,NULL,a?,b?,c,123",
                new String[]{"abc", null, null, "a,b,c", "123"});
        every(CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC),
                objects2,
                "\"abc\",\"NULL\",NULL,\"a,b,c\",123",
                new String[]{"abc", "NULL", null, "a,b,c", "123"});
    }

    private void every(CSVFormat csvFormat, Object[] objects, String format, String[] data) throws IOException {
        String source = csvFormat.format(objects);
        assertEquals(format, csvFormat.format(objects));
        CSVParser csvParser = csvFormat.parse(new StringReader(source));
        CSVRecord csvRecord = csvParser.iterator().next();
        for (int i = 0; i < data.length; i++) {
            assertEquals(csvRecord.get(i), data[i]);
        }
    }
}
