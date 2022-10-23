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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

// psql (14.5 (Homebrew))
//
// create table COMMONS_CSV_PSQL_TEST (ID INTEGER, COL1 VARCHAR, COL2 VARCHAR, COL3 VARCHAR, COL4 VARCHAR);
// insert into COMMONS_CSV_PSQL_TEST select 1, 'abc', 'test line 1' || chr(10) || 'test line 2', null, '';
// insert into COMMONS_CSV_PSQL_TEST select 2, 'xyz', '\b:' || chr(8) || ' \t:' || chr(9) || ' \n:' || chr(10) || ' \r:' || chr(13), 'a', 'b';
// insert into COMMONS_CSV_PSQL_TEST values (3, 'a', 'b,c,d', '"quoted"', 'e');
// copy COMMONS_CSV_PSQL_TEST TO '/tmp/psql.csv' WITH (FORMAT CSV);
// copy COMMONS_CSV_PSQL_TEST TO '/tmp/psql.tsv';
//
// cat /tmp/psql.csv
// 1,abc,"test line 1
// test line 2",,""
// 2,xyz,"\b:^H \t:         \n:
//  \r:^M",a,b
// 3,a,"b,c,d","""quoted""",e
//
// cat /tmp/psql.tsv
// 1    abc    test line 1\ntest line 2                  \N
// 2    xyz    \\b:\b \\t:\t \\n:\n \\r:\r    a          b
// 3    a      b,c,d                         "quoted"    e
//
public class JiraCsv290Test {

    private void testHelper(final String filename, final CSVFormat format) throws Exception {
        List<List<String>> content = new ArrayList<>();
        try (CSVParser csvParser = CSVParser.parse(new InputStreamReader(this.getClass().getResourceAsStream("/org/apache/commons/csv/CSV-290/" + filename)),
                format)) {
            content = csvParser.stream().collect(Collectors.mapping(CSVRecord::toList, Collectors.toList()));
        }

        assertEquals(3, content.size());

        assertEquals("1", content.get(0).get(0));
        assertEquals("abc", content.get(0).get(1));
        assertEquals("test line 1\ntest line 2", content.get(0).get(2)); // new line
        assertEquals(null, content.get(0).get(3)); // null
        assertEquals("", content.get(0).get(4));

        assertEquals("2", content.get(1).get(0));
        assertEquals("\\b:\b \\t:\t \\n:\n \\r:\r", content.get(1).get(2)); // \b, \t, \n, \r

        assertEquals("3", content.get(2).get(0));
        assertEquals("b,c,d", content.get(2).get(2)); // value has comma
        assertEquals("\"quoted\"", content.get(2).get(3)); // quoted
    }

    @Test
    public void testPostgresqlCsv() throws Exception {
        testHelper("psql.csv", CSVFormat.POSTGRESQL_CSV);
    }

    @Test
    public void testPostgresqlText() throws Exception {
        testHelper("psql.tsv", CSVFormat.POSTGRESQL_TEXT);
    }

    @Test
    public void testWriteThenRead() throws Exception {
        final StringWriter sw = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.POSTGRESQL_CSV.builder().setHeader().setSkipHeaderRecord(true).build())) {

            printer.printRecord("column1", "column2");
            printer.printRecord("v11", "v12");
            printer.printRecord("v21", "v22");
            printer.close();

            final CSVParser parser = new CSVParser(new StringReader(sw.toString()),
                    CSVFormat.POSTGRESQL_CSV.builder().setHeader().setSkipHeaderRecord(true).build());

            assertArrayEquals(new Object[] { "column1", "column2" }, parser.getHeaderNames().toArray());

            final Iterator<CSVRecord> i = parser.iterator();
            assertArrayEquals(new String[] { "v11", "v12" }, i.next().toList().toArray());
            assertArrayEquals(new String[] { "v21", "v22" }, i.next().toList().toArray());
        }
    }
}