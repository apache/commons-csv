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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.junit.Assert;

public class JiraCsv196Test {

    @Test
    public void parse() throws IOException {
        final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'');
        CSVParser parser = new CSVParser(getTestInput(), format, 0L, 1L, "UTF-8");
        long[] charByteKey = {0, 89, 242, 395};
        int idx = 0;
        for (CSVRecord record : parser) {
            Assert.assertEquals(charByteKey[idx++], record.getCharacterByte());
        }
        parser.close();
    }

    private Reader getTestInput() {
        final InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("CSV-196/sample1.csv");
        return new InputStreamReader(is);
    }

}
