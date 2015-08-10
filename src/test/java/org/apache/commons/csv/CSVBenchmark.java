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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.generationjava.io.CsvReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, jvmArgs = {"-server", "-Xms1024M", "-Xmx1024M"})
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 20)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class CSVBenchmark {

    private String data;

    /**
     * Load the data in memory before running the benchmarks, this takes out IO from the results.
     */
    @Setup
    public void init() throws IOException {
        final File file = new File("src/test/resources/perf/worldcitiespop.txt.gz");
        final InputStream in = new GZIPInputStream(new FileInputStream(file));
        this.data = IOUtils.toString(in, "ISO-8859-1");
        in.close();
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(data));
    }

    @Benchmark
    public int read(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
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
    public int split(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        int count = 0;
        String line;
        while ((line = in.readLine()) != null) {
            final String[] values = StringUtils.split(line, ',');
            count += values.length;
        }
        
        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseCommonsCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();

        int count = 0;
        for (final CSVRecord record : format.parse(in)) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseGenJavaCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final CsvReader reader = new CsvReader(in);
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
    public int parseJavaCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final com.csvreader.CsvReader reader = new com.csvreader.CsvReader(in, ',');
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
    public int parseOpenCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final com.opencsv.CSVReader reader = new com.opencsv.CSVReader(in, ',');

        int count = 0;
        while (reader.readNext() != null) {
            count++;
        }

        bh.consume(count);
        in.close();
        return count;
    }

    @Benchmark
    public int parseSkifeCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final org.skife.csv.CSVReader reader = new org.skife.csv.SimpleReader();
        reader.setSeperator(',');
        
        final CountingReaderCallback callback = new CountingReaderCallback();
        reader.parse(in, callback);

        bh.consume(callback);
        in.close();
        return callback.count;
    }

    private static class CountingReaderCallback implements org.skife.csv.ReaderCallback {
        public int count = 0;

        @Override
        public void onRow(final String[] fields) {
            count++;
        }
    }

    @Benchmark
    public int parseSuperCSV(final Blackhole bh) throws Exception {
        final BufferedReader in = getReader();
        
        final CsvListReader reader = new CsvListReader(in, CsvPreference.STANDARD_PREFERENCE);

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
