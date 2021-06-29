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
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

public class JiraCsv150Test {

    private void testDisable(CSVFormat csvFormat, StringReader stringReader) throws IOException {
        CSVParser csvParser = null;
        try {
            csvParser = new CSVParser(stringReader, csvFormat);
            final List<CSVRecord> records = csvParser.getRecords();
            assertEquals(1, records.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (csvParser != null) {
                csvParser.close();
            }
        }
    }

    @Test
    public void testDisableEscaping() throws IOException {
        final StringReader stringReader = new StringReader("\"66\u2441\",,\"\",\"DeutscheBK\ufffe\",\"000\"\r\n");
        testDisable(CSVFormat.DEFAULT.withEscape(null), stringReader);
    }

    @Test
    public void testDisableComment() throws IOException {
        final StringReader stringReader = new StringReader("\"66\u2441\",,\"\",\"DeutscheBK\ufffe\",\"000\"\r\n");
        testDisable(CSVFormat.DEFAULT.withCommentMarker(null), stringReader);
    }

    @Test
    public void testDisableEncapsulation() throws IOException {
        final StringReader stringReader = new StringReader("66\u2441,,\"\",\ufffeDeutscheBK,\"000\"\r\n");
        testDisable(CSVFormat.DEFAULT.withQuote(null), stringReader);
    }
}
