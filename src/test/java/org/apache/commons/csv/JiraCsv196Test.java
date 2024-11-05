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
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


import org.junit.jupiter.api.Test;


public class JiraCsv196Test {
    @Test
    public void parseThreeBytes() throws IOException {

        // final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'');
        final CSVFormat format = CSVFormat.Builder.create()
                               .setDelimiter(',')
                               .setQuote('\'')
                               .build();
        // CSVParser parser = new CSVParser(getTestInput(
            // "org/apache/commons/csv/CSV-196/japanese.csv"), format, 0L, 1L, "UTF-8");
        CSVParser parser =  format.parse(getTestInput(
            "org/apache/commons/csv/CSV-196/japanese.csv"), 0L, 1L, "UTF-8");
        long[] charByteKey = {0, 89, 242, 395};
        int idx = 0;
        for (CSVRecord record : parser) {
            assertEquals(charByteKey[idx++], record.getCharacterByte());
        }
        parser.close();
    }


    @Test
    public void parseFourBytes() throws IOException {
        // final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'');
        final CSVFormat format = CSVFormat.Builder.create()
            .setDelimiter(',')
            .setQuote('\'')
            .build();

        CSVParser parser =  format.parse(getTestInput(
                "org/apache/commons/csv/CSV-196/emoji.csv"), 0L, 1L, "UTF-8");

        long[] charByteKey = {0, 84, 701, 1318, 1935};
        int idx = 0;
        for (CSVRecord record : parser) {
            assertEquals(charByteKey[idx++], record.getCharacterByte());
        }
        parser.close();
    }


    private Reader getTestInput(String path) {
        return new InputStreamReader(
            ClassLoader.getSystemClassLoader().getResourceAsStream(path));
    }
}
