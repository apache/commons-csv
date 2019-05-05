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

package org.apache.commons.csv.pretty;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Ali Ghanbari
 */
public class PrettyPrinterTest {
    private List<CSVRecord> loadRecords(final String fileName) {
        final URL url = ClassLoader.getSystemClassLoader().getResource(fileName);
        try (final Reader reader = new FileReader(url.getFile());
             final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            return parser.getRecords();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        return Collections.emptyList(); // unreachable
    }

    @Test
    public void printSimpleTest1() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(true, true, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest2() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(false, true, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest3() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(true, true, true, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest4() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file2.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(true, true, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest5() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(true, false, true, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest6() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.SIMPLE, System.out);
        System.out.println();
        prettyPrinter.print(true, false, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest7() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.FANCY, System.out);
        System.out.println();
        prettyPrinter.print(false, false, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest8() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.FANCY, System.out);
        System.out.println();
        prettyPrinter.print(true, false, false, true);
        System.out.println();
    }

    @Test
    public void printSimpleTest9() {
        final List<CSVRecord> records = loadRecords("csv-pretty/file1.csv");
        PrettyPrinter prettyPrinter = new PrettyPrinter(records, TableStyle.FANCY, System.out);
        System.out.println();
        prettyPrinter.print(true, true, false, true);
        System.out.println();
    }
}