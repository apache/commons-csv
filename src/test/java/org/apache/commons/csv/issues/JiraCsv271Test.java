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
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

public class JiraCsv271Test {

    @Test
    public void testJiraCsv271_withArray() throws IOException {
        final CSVFormat csvFormat = CSVFormat.DEFAULT;
        final StringWriter stringWriter = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(stringWriter, csvFormat)) {
            printer.print("a");
            printer.printRecord("b","c");
        }
        assertEquals("a,b,c\r\n", stringWriter.toString());
    }

    @Test
    public void testJiraCsv271_withList() throws IOException {
        final CSVFormat csvFormat = CSVFormat.DEFAULT;
        final StringWriter stringWriter = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(stringWriter, csvFormat)) {
            printer.print("a");
            printer.printRecord(Arrays.asList("b","c"));
        }
        assertEquals("a,b,c\r\n", stringWriter.toString());
    }

}
