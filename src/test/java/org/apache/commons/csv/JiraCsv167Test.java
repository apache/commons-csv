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
package org.apache.commons.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class JiraCsv167Test {

    @Test
    public void parse() throws IOException {
        final File csvData = new File("src/test/resources/csv-167/sample1.csv");
        final BufferedReader br = new BufferedReader(new FileReader(csvData));
        String s = null;
        int totcomment = 0;
        int totrecs = 0;
        boolean lastWasComment = false;
        while((s=br.readLine()) != null) {
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
        br.close();
        CSVFormat format = CSVFormat.DEFAULT;
        //
        format = format.withAllowMissingColumnNames(false);
        format = format.withCommentMarker('#');
        format = format.withDelimiter(',');
        format = format.withEscape('\\');
        format = format.withHeader("author", "title", "publishDate");
        format = format.withHeaderComments("headerComment");
        format = format.withNullString("NULL");
        format = format.withIgnoreEmptyLines(true);
        format = format.withIgnoreSurroundingSpaces(true);
        format = format.withQuote('"');
        format = format.withQuoteMode(QuoteMode.ALL);
        format = format.withRecordSeparator('\n');
        format = format.withSkipHeaderRecord(false);
        //
        final CSVParser parser = CSVParser.parse(csvData, Charset.defaultCharset(), format);
        int comments = 0;
        int records = 0;
        for (final CSVRecord csvRecord : parser) {
//            System.out.println(csvRecord.isComment() + "[" + csvRecord.toString() + "]");
            records++;
            if (csvRecord.hasComment()) {
                comments++;
            }
        }
        // Comment lines are concatenated, in this example 4 lines become 2 comments.
        Assert.assertEquals(totcomment, comments);
        Assert.assertEquals(totrecs, records); // records includes the header
    }
}
