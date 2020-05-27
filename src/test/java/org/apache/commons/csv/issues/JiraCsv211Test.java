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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraCsv211Test {

    @Test
    public void testJiraCsv211Format() throws IOException {
        final String[] values = new String[] { "1", "Jane Doe", "USA", "" };

        final CSVFormat printFormat = CSVFormat.DEFAULT.withDelimiter('\t').withHeader("ID", "Name", "Country", "Age");
        String formatted = printFormat.format(values);
        assertEquals("ID\tName\tCountry\tAge\r\n1\tJane Doe\tUSA\t", formatted);

        final CSVFormat parseFormat = CSVFormat.DEFAULT.withDelimiter('\t').withFirstRecordAsHeader();
        CSVParser parser = parseFormat.parse(new StringReader(formatted));
        for (CSVRecord record : parser) {
            assertEquals("1", record.get(0));
            assertEquals("Jane Doe", record.get(1));
            assertEquals("USA", record.get(2));
            assertEquals("", record.get(3));
        }
    }
}
