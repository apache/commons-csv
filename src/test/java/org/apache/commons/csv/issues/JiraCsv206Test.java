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
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

public class JiraCsv206Test {

    @Test
    public void testJiraCsv206MultipleCharacterDelimiter() throws IOException {
        // Read with multiple character delimiter
        final String source = "FirstName[|]LastName[|]Address\r\nJohn[|]Smith[|]123 Main St.";
        final StringReader reader = new StringReader(source);
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter("[|]").build();
        CSVRecord record = null;
        try (final CSVParser csvParser = new CSVParser(reader, csvFormat)) {
            final Iterator<CSVRecord> iterator = csvParser.iterator();
            record = iterator.next();
            assertEquals("FirstName", record.get(0));
            assertEquals("LastName", record.get(1));
            assertEquals("Address", record.get(2));
            record = iterator.next();
            assertEquals("John", record.get(0));
            assertEquals("Smith", record.get(1));
            assertEquals("123 Main St.", record.get(2));
        }
        // Write with multiple character delimiter
        final String outString = "# Change delimiter to [I]\r\n" + "first name[I]last name[I]address\r\n"
            + "John[I]Smith[I]123 Main St.";
        final String comment = "Change delimiter to [I]";
        // @formatter:off
        final CSVFormat format = CSVFormat.EXCEL.builder()
                .setDelimiter("[I]").setHeader("first name", "last name", "address")
                .setCommentMarker('#')
                .setHeaderComments(comment).build();
        // @formatter:on
        final StringBuilder out = new StringBuilder();
        try (final CSVPrinter printer = format.print(out)) {
            printer.print(record.get(0));
            printer.print(record.get(1));
            printer.print(record.get(2));
        }
        final String s = out.toString();
        assertEquals(outString, s);
    }
}
