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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.input.BOMInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the user guide.
 */
public class UserGuideTest {

    @TempDir
    Path tempDir;

    /**
     * Creates a reader capable of handling BOMs.
     *
     * @param path The path to read.
     * @return a new InputStreamReader for UTF-8 bytes.
     * @throws IOException if an I/O error occurs.
     */
    public InputStreamReader newReader(final Path path) throws IOException {
        return new InputStreamReader(BOMInputStream.builder()
                .setPath(path)
                .get(), StandardCharsets.UTF_8);
    }

    @Test
    void testBomFull() throws UnsupportedEncodingException, IOException {
        final Path path = tempDir.resolve("test1.csv");
        Files.copy(Utils.createUtf8Input("ColumnA, ColumnB, ColumnC\r\nA, B, C\r\n".getBytes(StandardCharsets.UTF_8), true), path);
        // @formatter:off
        try (Reader reader = new InputStreamReader(BOMInputStream.builder()
                .setPath(path)
                .get(), "UTF-8");
                CSVParser parser = CSVFormat.EXCEL.builder()
                        .setHeader()
                        .get()
                        .parse(reader)) {
            // @formatter:off
            for (final CSVRecord record : parser) {
                final String string = record.get("ColumnA");
                assertEquals("A", string);
            }
        }
    }

    @Test
    void testBomUtil() throws UnsupportedEncodingException, IOException {
        final Path path = tempDir.resolve("test2.csv");
        Files.copy(Utils.createUtf8Input("ColumnA, ColumnB, ColumnC\r\nA, B, C\r\n".getBytes(StandardCharsets.UTF_8), true), path);
        try (Reader reader = newReader(path);
                // @formatter:off
                CSVParser parser = CSVFormat.EXCEL.builder()
                        .setHeader()
                        .get()
                        .parse(reader)) {
            // @formatter:off
            for (final CSVRecord record : parser) {
                final String string = record.get("ColumnA");
                assertEquals("A", string);
            }
        }
    }

}
