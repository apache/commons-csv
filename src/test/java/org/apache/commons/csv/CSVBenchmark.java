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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.generationjava.io.CsvReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, jvmArgs = "-server")
@Threads(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CSVBenchmark {

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(new FileReader("worldcitiespop.txt"));
    }

    @Benchmark
    public int baseline(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        int count = 0;
        String line;
        while ((line = in.readLine()) != null) {
            count++;
        }
        
        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseCommonsCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        CSVFormat format = CSVFormat.DEFAULT.withHeader();

        int count = 0;
        for (CSVRecord record : format.parse(in)) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseGenJavaCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        CsvReader reader = new CsvReader(in);
        reader.setFieldDelimiter(',');

        int count = 0;
        String[] record = null;
        while ((record = reader.readLine()) != null) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseJavaCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        com.csvreader.CsvReader reader = new com.csvreader.CsvReader(in, ',');
        reader.setRecordDelimiter('\n');

        int count = 0;
        while (reader.readRecord()) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseOpenCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        com.opencsv.CSVReader reader = new com.opencsv.CSVReader(in, ',');

        int count = 0;
        while (reader.readNext() != null) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseSkifeCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        org.skife.csv.CSVReader reader = new org.skife.csv.SimpleReader();
        reader.setSeperator(',');
        
        CountingReaderCallback callback = new CountingReaderCallback();
        reader.parse(in, callback);

        bh.consume(callback);
        in.close();
        return callback.count;
    }

    private static class CountingReaderCallback implements org.skife.csv.ReaderCallback {
        public int count = 0;

        @Override
        public void onRow(String[] fields) {
            count++;
        }
    }

    @Benchmark
    public int parseSuperCSV(Blackhole bh) throws Exception {
        BufferedReader in = getReader();
        
        CsvListReader reader = new CsvListReader(in, CsvPreference.STANDARD_PREFERENCE);

        int count = 0;
        List<String> record = null;
        while ((record = reader.read()) != null) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }
}
