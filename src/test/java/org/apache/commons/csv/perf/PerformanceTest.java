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

package org.apache.commons.csv.perf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests performance.
 *
 * Only enable for your own development.
 */
public class PerformanceTest {

    private final int max = 10;

    private BufferedReader getBufferedReader() throws IOException {
        return new BufferedReader(new FileReader("src/test/resources/worldcitiespop.txt"));
    }

    private long parse(Reader in) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        long count = 0;
        for (Object record : format.parse(in)) {
            count++;
        }
        return count;
    }

    private void println() {
        System.out.println();
    }

    private void println(String s) {
        System.out.println(s);
    }

    private long readAll(BufferedReader in) throws IOException {
        long count = 0;
        while (in.readLine() != null) {
            count++;
        }
        return count;
    }

    @Test
    @Ignore
    public void testParseBigFile() throws Exception {
        long t0 = System.currentTimeMillis();
        long count = this.parse(this.getBufferedReader());
        this.println("File parsed in " + (System.currentTimeMillis() - t0) + "ms with Commons CSV" + " " + count
                + " lines");
        this.println();
    }

    @Test
    @Ignore
    public void testParseBigFileRepeat() throws Exception {
        for (int i = 0; i < this.max; i++) {
            this.testParseBigFile();
        }
        this.println();
    }

    @Test
    @Ignore
    public void testReadBigFile() throws Exception {
        for (int i = 0; i < this.max; i++) {
            BufferedReader in = this.getBufferedReader();
            long t0 = System.currentTimeMillis();
            long count = this.readAll(in);
            in.close();
            this.println("File read in " + (System.currentTimeMillis() - t0) + "ms" + " " + count + " lines");
        }
        this.println();
    }
}