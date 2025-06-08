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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

class JiraCsv154Test {

    @Test
    void testJiraCsv154_withCommentMarker() throws IOException {
        final String comment = "This is a header comment";
        // @formatter:off
        final CSVFormat format = CSVFormat.EXCEL.builder()
            .setHeader("H1", "H2")
            .setCommentMarker('#')
            .setHeaderComments(comment)
            .get();
        // @formatter:on
        final StringBuilder out = new StringBuilder();
        try (CSVPrinter printer = format.print(out)) {
            printer.print("A");
            printer.print("B");
        }
        final String s = out.toString();
        assertTrue(s.contains(comment), s);
    }

    @Test
    void testJiraCsv154_withHeaderComments() throws IOException {
        final String comment = "This is a header comment";
        // @formatter:off
        final CSVFormat format = CSVFormat.EXCEL.builder()
            .setHeader("H1", "H2")
            .setHeaderComments(comment)
            .setCommentMarker('#')
            .get();
        // @formatter:on
        final StringBuilder out = new StringBuilder();
        try (CSVPrinter printer = format.print(out)) {
            printer.print("A");
            printer.print("B");
        }
        final String s = out.toString();
        assertTrue(s.contains(comment), s);
    }

}
