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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.generationjava.io.CsvReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

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
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(
            "org/apache/commons/csv/perf/worldcitiespop.txt.gz");
        try (final InputStream gzin = new GZIPInputStream(in, 8192)) {
            this.data = IOUtils.toString(gzin, StandardCharsets.ISO_8859_1);
        }
    }

    private Reader getReader() {
        return new StringReader(data);
    }

    @Benchmark
    public int read(final Blackhole bh) throws Exception {
        int count = 0;

        try (BufferedReader reader = new BufferedReader(getReader())) {
            while (reader.readLine() != null) {
              count++;
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int scan(final Blackhole bh) throws Exception {
        int count = 0;

        try (Scanner scanner = new Scanner(getReader())) {
            while (scanner.hasNextLine()) {
              scanner.nextLine();
              count++;
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int split(final Blackhole bh) throws Exception {
      int count = 0;

      try (BufferedReader reader = new BufferedReader(getReader())) {
          String line;
          while ((line = reader.readLine()) != null) {
            final String[] values = StringUtils.split(line, ',');
            count += values.length;
          }
      }

      bh.consume(count);
      return count;
    }

    @Benchmark
    public int parseCommonsCSV(final Blackhole bh) throws Exception {
        int count = 0;

        try (final Reader in = getReader()) {
            final CSVFormat format = CSVFormat.Builder.create().setSkipHeaderRecord(true).build();
            Iterator<CSVRecord> iter = format.parse(in).iterator();
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int parseGenJavaCSV(final Blackhole bh) throws Exception {
        int count = 0;

        try (final Reader in = getReader()) {
            final CsvReader reader = new CsvReader(in);
            reader.setFieldDelimiter(',');
            while (reader.readLine() != null) {
                count++;
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int parseJavaCSV(final Blackhole bh) throws Exception {
        int count = 0;

        try (final Reader in = getReader()) {
            final com.csvreader.CsvReader reader = new com.csvreader.CsvReader(in, ',');
            reader.setRecordDelimiter('\n');
            while (reader.readRecord()) {
                count++;
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int parseOpenCSV(final Blackhole bh) throws Exception {
        int count = 0;

        final com.opencsv.CSVParser parser = new CSVParserBuilder()
          .withSeparator(',').withIgnoreQuotations(true).build();

        try (final Reader in = getReader()) {
            final com.opencsv.CSVReader reader = new CSVReaderBuilder(in).withSkipLines(1).withCSVParser(parser).build();
            while (reader.readNext() != null) {
                count++;
            }
        }

        bh.consume(count);
        return count;
    }

    @Benchmark
    public int parseSkifeCSV(final Blackhole bh) throws Exception {
        final org.skife.csv.CSVReader reader = new org.skife.csv.SimpleReader();
        reader.setSeperator(',');
        final CountingReaderCallback callback = new CountingReaderCallback();

        try (final Reader in = getReader()) {
          reader.parse(in, callback);
        }

        bh.consume(callback);
        return callback.count;
    }

    private static class CountingReaderCallback implements org.skife.csv.ReaderCallback {
        public int count;

        @Override
        public void onRow(final String[] fields) {
            count++;
        }
    }

    @Benchmark
    public int parseSuperCSV(final Blackhole bh) throws Exception {
        int count = 0;

        try (final CsvListReader reader = new CsvListReader(getReader(), CsvPreference.STANDARD_PREFERENCE)) {
            while (reader.read() != null) {
                count++;
            }
        }

        bh.consume(count);
        return count;
    }
}
