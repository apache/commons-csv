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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

public class CSVPrinterTest {

    String lineSeparator = CSVFormat.DEFAULT.getLineSeparator();

    @Test
    public void testPrinter1() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b");
        assertEquals("a,b" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter2() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a,b", "b");
        assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter3() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a, b", "b ");
        assertEquals("\"a, b\",\"b \"" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter4() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\"c");
        assertEquals("a,\"b\"\"c\"" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter5() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\nc");
        assertEquals("a,\"b\nc\"" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter6() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\r\nc");
        assertEquals("a,\"b\r\nc\"" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrinter7() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\\c");
        assertEquals("a,b\\c" + lineSeparator, sw.toString());
    }

    @Test
    public void testExcelPrinter1() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.println("a", "b");
        assertEquals("a,b" + lineSeparator, sw.toString());
    }

    @Test
    public void testExcelPrinter2() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.println("a,b", "b");
        assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
    }

    @Test
    public void testPrintNullValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", null, "b");
        assertEquals("a,,b" + lineSeparator, sw.toString());
    }

    @Test
    public void testDisabledComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This is a comment");
        
        assertEquals("", sw.toString());
    }

    @Test
    public void testSingleLineComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment");
        
        assertEquals("# This is a comment" + lineSeparator, sw.toString());
    }

    @Test
    public void testMultiLineComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment\non multiple lines");
        
        assertEquals("# This is a comment" + lineSeparator + "# on multiple lines" + lineSeparator, sw.toString());
    }

    @Test
    public void testRandom() throws Exception {
        int iter = 10000;
        doRandom(CSVFormat.DEFAULT, iter);
        doRandom(CSVFormat.EXCEL, iter);
        doRandom(CSVFormat.MYSQL, iter);
    }

    public void doRandom(CSVFormat format, int iter) throws Exception {
        for (int i = 0; i < iter; i++) {
            doOneRandom(format);
        }
    }

    public void doOneRandom(CSVFormat format) throws Exception {
        Random r = new Random();
        
        int nLines = r.nextInt(4) + 1;
        int nCol = r.nextInt(3) + 1;
        // nLines=1;nCol=2;
        String[][] lines = new String[nLines][];
        for (int i = 0; i < nLines; i++) {
            String[] line = new String[nCol];
            lines[i] = line;
            for (int j = 0; j < nCol; j++) {
                line[j] = randStr();
            }
        }

        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);

        for (int i = 0; i < nLines; i++) {
            // for (int j=0; j<lines[i].length; j++) System.out.println("### VALUE=:" + printable(lines[i][j]));
            printer.println(lines[i]);
        }

        printer.flush();
        String result = sw.toString();
        // System.out.println("### :" + printable(result));

        CSVParser parser = new CSVParser(result, format);
        List<CSVRecord> parseResult = parser.getRecords();

        if (!equals(lines, parseResult)) {
            System.out.println("Printer output :" + printable(result));
            assertTrue(false);
        }
    }

    public static boolean equals(String[][] a, List<CSVRecord> b) {
        if (a.length != b.size()) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            String[] linea = a[i];
            String[] lineb = b.get(i).values();
            if (linea.length != lineb.length) {
                return false;
            }
            for (int j = 0; j < linea.length; j++) {
                String aval = linea[j];
                String bval = lineb[j];
                if (!aval.equals(bval)) {
                    System.out.println("expected  :" + printable(aval));
                    System.out.println("got       :" + printable(bval));
                    return false;
                }
            }
        }
        return true;
    }

    public static String printable(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= ' ' || ch >= 128) {
                sb.append("(").append((int) ch).append(")");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public String randStr() {
        Random r = new Random();
        
        int sz = r.nextInt(20);
        // sz = r.nextInt(3);
        char[] buf = new char[sz];
        for (int i = 0; i < sz; i++) {
            // stick in special chars with greater frequency
            char ch;
            int what = r.nextInt(20);
            switch (what) {
                case 0:
                    ch = '\r';
                    break;
                case 1:
                    ch = '\n';
                    break;
                case 2:
                    ch = '\t';
                    break;
                case 3:
                    ch = '\f';
                    break;
                case 4:
                    ch = ' ';
                    break;
                case 5:
                    ch = ',';
                    break;
                case 6:
                    ch = '"';
                    break;
                case 7:
                    ch = '\'';
                    break;
                case 8:
                    ch = '\\';
                    break;
                default:
                    ch = (char) r.nextInt(300);
                    break;
                // default: ch = 'a'; break;
            }
            buf[i] = ch;
        }
        return new String(buf);
    }

}
