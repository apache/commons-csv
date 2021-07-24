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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests performance.
 *
 * To run this test, use: mvn test -Dtest=PerformanceTest
 */
@SuppressWarnings("boxing") // test code
public class PerformanceTest {

    private static final String TEST_RESRC = "org/apache/commons/csv/perf/worldcitiespop.txt.gz";

    private static final File BIG_FILE = new File(System.getProperty("java.io.tmpdir"), "worldcitiespop.txt");
    @BeforeAll
    public static void setUpClass() throws FileNotFoundException, IOException {
        if (BIG_FILE.exists()) {
            System.out.println(String.format("Found test fixture %s: %,d bytes.", BIG_FILE, BIG_FILE.length()));
            return;
        }
        System.out.println("Decompressing test fixture to: " + BIG_FILE + "...");
        try (
            final InputStream input = new GZIPInputStream(
                PerformanceTest.class.getClassLoader().getResourceAsStream(TEST_RESRC));
            final OutputStream output = new FileOutputStream(BIG_FILE)) {
            IOUtils.copy(input, output);
            System.out.println(String.format("Decompressed test fixture %s: %,d bytes.", BIG_FILE, BIG_FILE.length()));
        }
    }

    private final int max = 10;

    private BufferedReader createBufferedReader() throws IOException {
        return new BufferedReader(new FileReader(BIG_FILE));
    }

    private long parse(final Reader reader, final boolean traverseColumns) throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.builder().setIgnoreSurroundingSpaces(false).build();
        long recordCount = 0;
        try (final CSVParser parser = format.parse(reader)) {
            for (final CSVRecord record : parser) {
                recordCount++;
                if (traverseColumns) {
                    for (@SuppressWarnings("unused")
                    final String value : record) {
                        // do nothing for now
                    }
                }
            }
        }
        return recordCount;
    }

    private void println(final String s) {
        System.out.println(s);
    }

    private long readAll(final BufferedReader in) throws IOException {
        long count = 0;
        while (in.readLine() != null) {
            count++;
        }
        return count;
    }

    public long testParseBigFile(final boolean traverseColumns) throws Exception {
        final long startMillis = System.currentTimeMillis();
        try (final BufferedReader reader = this.createBufferedReader()) {
            final long count = this.parse(reader, traverseColumns);
            final long totalMillis = System.currentTimeMillis() - startMillis;
            this.println(
                String.format("File parsed in %,d milliseconds with Commons CSV: %,d lines.", totalMillis, count));
            return totalMillis;
        }
    }

    @Test
    public void testParseBigFileRepeat() throws Exception {
        long bestTime = Long.MAX_VALUE;
        for (int i = 0; i < this.max; i++) {
            bestTime = Math.min(this.testParseBigFile(false), bestTime);
        }
        this.println(String.format("Best time out of %,d is %,d milliseconds.", this.max, bestTime));
    }

    @Test
    public void testReadBigFile() throws Exception {
        long bestTime = Long.MAX_VALUE;
        long count;
        for (int i = 0; i < this.max; i++) {
            final long startMillis;
            try (final BufferedReader in = this.createBufferedReader()) {
                startMillis = System.currentTimeMillis();
                count = this.readAll(in);
            }
            final long totalMillis = System.currentTimeMillis() - startMillis;
            bestTime = Math.min(totalMillis, bestTime);
            this.println(String.format("File read in %,d milliseconds: %,d lines.", totalMillis, count));
        }
        this.println(String.format("Best time out of %,d is %,d milliseconds.", this.max, bestTime));
    }
}