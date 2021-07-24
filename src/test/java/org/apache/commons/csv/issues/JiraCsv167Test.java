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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

public class JiraCsv167Test {

    private Reader getTestReader() {
        return new InputStreamReader(
            ClassLoader.getSystemClassLoader().getResourceAsStream("org/apache/commons/csv/csv-167/sample1.csv"));
    }

    @Test
    public void parse() throws IOException {
        int totcomment = 0;
        int totrecs = 0;
        try (final Reader reader = getTestReader(); final BufferedReader br = new BufferedReader(reader)) {
            String s = null;
            boolean lastWasComment = false;
            while ((s = br.readLine()) != null) {
                if (s.startsWith("#")) {
                    if (!lastWasComment) { // comments are merged
                        totcomment++;
                    }
                    lastWasComment = true;
                } else {
                    totrecs++;
                    lastWasComment = false;
                }
            }
        }
        final CSVFormat format = CSVFormat.DEFAULT.builder()
        // @formatter:off
            .setAllowMissingColumnNames(false)
            .setCommentMarker('#')
            .setDelimiter(',')
            .setEscape('\\')
            .setHeader("author", "title", "publishDate")
            .setHeaderComments("headerComment")
            .setNullString("NULL")
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setQuote('"')
            .setQuoteMode(QuoteMode.ALL)
            .setRecordSeparator('\n')
            .setSkipHeaderRecord(false)
            .build();
        // @formatter:on
        int comments = 0;
        int records = 0;
        try (final Reader reader = getTestReader(); final CSVParser parser = format.parse(reader)) {
            for (final CSVRecord csvRecord : parser) {
                records++;
                if (csvRecord.hasComment()) {
                    comments++;
                }
            }
        }
        // Comment lines are concatenated, in this example 4 lines become 2 comments.
        assertEquals(totcomment, comments);
        assertEquals(totrecs, records); // records includes the header
    }
}
