/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOStream;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/projects/CSV/issues/CSV-318?filter=allopenissues
 *
 * @see CSVPrinter
 */
class JiraCsv318Test {

    private void checkOutput(final ByteArrayOutputStream baos) {
        checkOutput(baos.toString());
    }

    private void checkOutput(final String string) {
        assertEquals("col a,col b,col c", string.trim());
    }

    private Stream<String> newParallelStream() {
        // returned stream is intermediate
        return newStream().parallel();
    }

    private CSVPrinter newPrinter(final ByteArrayOutputStream baos) throws IOException {
        return new CSVPrinter(new PrintWriter(baos), CSVFormat.DEFAULT);
    }

    private Stream<String> newSequentialStream() {
        // returned stream is intermediate
        return newStream().sequential();
    }

    private Stream<String> newStream() {
        return Stream.of("col a", "col b", "col c");
    }

    @Test
    void testDefaultStream() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = newPrinter(baos)) {
            printer.printRecord(newStream());
        }
        checkOutput(baos);
    }

    @SuppressWarnings("resource")
    @Test
    void testParallelIOStream() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = newPrinter(baos)) {
            IOStream.adapt(newParallelStream()).forEachOrdered(printer::print);
        }
        // No EOR marker in this test intentionally, so checkOutput will trim.
        checkOutput(baos);
    }

    @SuppressWarnings("resource")
    @Test
    void testParallelIOStreamSynchronizedPrinterNotUsed() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = newPrinter(baos)) {
            synchronized (printer) {
                IOStream.adapt(newParallelStream()).forEachOrdered(IOConsumer.noop());
            }
        }
        final List<String> list = new ArrayList<>();
        try (CSVPrinter printer = newPrinter(baos)) {
            synchronized (printer) {
                IOStream.adapt(newParallelStream()).forEachOrdered(list::add);
            }
        }
        // No EOR marker in this test intentionally, so checkOutput will trim.
        checkOutput(String.join(",", list.toArray(ArrayUtils.EMPTY_STRING_ARRAY)));
    }

    @Test
    void testParallelStream() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = newPrinter(baos)) {
            printer.printRecord(newParallelStream());
        }
        checkOutput(baos);
    }

    @Test
    void testSequentialStream() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = newPrinter(baos)) {
            printer.printRecord(newSequentialStream());
        }
        checkOutput(baos);
    }
}
