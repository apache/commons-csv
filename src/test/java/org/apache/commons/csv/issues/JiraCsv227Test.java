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

package org.apache.commons.csv.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/CSV-227
 */
class JiraCsv227Test {

    @Test
    public void test() throws IOException {
        final StringBuilder out = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL))) {
            printer.printRecord("ㅁㅎㄷㄹ", "ㅁㅎㄷㄹ", "", "test2");
            printer.printRecord("한글3", "hello3", "3한글3", "test3");
            printer.printRecord("", "hello4", "", "test4");
        }
        // ㅁㅎㄷㄹ,ㅁㅎㄷㄹ,,test2
        // 한글3,hello3,3한글3,test3
        // "",hello4,,test4
        assertEquals("ㅁㅎㄷㄹ,ㅁㅎㄷㄹ,,test2\r\n한글3,hello3,3한글3,test3\r\n\"\",hello4,,test4\r\n", out.toString());
    }
}
