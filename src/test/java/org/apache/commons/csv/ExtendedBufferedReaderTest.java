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

import static org.apache.commons.csv.Constants.UNDEFINED;
import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * Test {@link ExtendedBufferedReader}.
 */
public class ExtendedBufferedReaderTest {

    static final String LF = "\n";
    static final String CR = "\r";
    static final String CRLF = CR + LF;
    static final String LFCR = LF + CR; // easier to read the string below

    private ExtendedBufferedReader createBufferedReader(final String s) {
        return new ExtendedBufferedReader(new StringReader(s));
    }

    @Test
    public void testEmptyInput() throws Exception {
        try (ExtendedBufferedReader br = createBufferedReader("")) {
            assertEquals(EOF, br.read());
            assertEquals(EOF, br.peek());
            assertEquals(EOF, br.getLastChar());
            assertNull(br.readLine());
            assertEquals(0, br.read(new char[10], 0, 0));
        }
    }

    /*
     * Test to illustrate https://issues.apache.org/jira/browse/CSV-75
     */
    @Test
    public void testReadChar() throws Exception {
        final String test = "a" + LF + "b" + CR + "c" + LF + LF + "d" + CR + CR + "e" + LFCR + "f " + CRLF;
        // EOL eol EOL EOL eol eol EOL+CR EOL
        final int eolCount = 9;

        try (ExtendedBufferedReader br = createBufferedReader(test)) {
            assertEquals(0, br.getLineNumber());
            int lineCount = 0;
            while (br.readLine() != null) {
                // consume all
                lineCount++;
            }
            assertEquals(eolCount, br.getLineNumber());
            assertEquals(lineCount, br.getLineNumber());
        }
        try (ExtendedBufferedReader br = createBufferedReader(test)) {
            assertEquals(0, br.getLineNumber());
            int readCount = 0;
            while (br.read() != EOF) {
                // consume all
                readCount++;
            }
            assertEquals(eolCount, br.getLineNumber());
            assertEquals(readCount, test.length());
        }
        try (ExtendedBufferedReader br = createBufferedReader(test)) {
            assertEquals(0, br.getLineNumber());
            final char[] buff = new char[10];
            while (br.read(buff, 0, 3) != EOF) {
                // consume all
            }
            assertEquals(eolCount, br.getLineNumber());
        }
    }

    @Test
    public void testReadingInDifferentBuffer() throws Exception {
        final char[] tmp1 = new char[2], tmp2 = new char[4];
        try (ExtendedBufferedReader reader = createBufferedReader("1\r\n2\r\n")) {
            reader.read(tmp1, 0, 2);
            reader.read(tmp2, 2, 2);
            assertEquals(2, reader.getLineNumber());
        }
    }

    @Test
    public void testReadLine() throws Exception {
        try (ExtendedBufferedReader br = createBufferedReader("")) {
            assertNull(br.readLine());
        }
        try (ExtendedBufferedReader br = createBufferedReader("\n")) {
            assertEquals("", br.readLine());
            assertNull(br.readLine());
        }
        try (ExtendedBufferedReader br = createBufferedReader("foo\n\nhello")) {
            assertEquals(0, br.getLineNumber());
            assertEquals("foo", br.readLine());
            assertEquals(1, br.getLineNumber());
            assertEquals("", br.readLine());
            assertEquals(2, br.getLineNumber());
            assertEquals("hello", br.readLine());
            assertEquals(3, br.getLineNumber());
            assertNull(br.readLine());
            assertEquals(3, br.getLineNumber());
        }
        try (ExtendedBufferedReader br = createBufferedReader("foo\n\nhello")) {
            assertEquals('f', br.read());
            assertEquals('o', br.peek());
            assertEquals("oo", br.readLine());
            assertEquals(1, br.getLineNumber());
            assertEquals('\n', br.peek());
            assertEquals("", br.readLine());
            assertEquals(2, br.getLineNumber());
            assertEquals('h', br.peek());
            assertEquals("hello", br.readLine());
            assertNull(br.readLine());
            assertEquals(3, br.getLineNumber());
        }
        try (ExtendedBufferedReader br = createBufferedReader("foo\rbaar\r\nfoo")) {
            assertEquals("foo", br.readLine());
            assertEquals('b', br.peek());
            assertEquals("baar", br.readLine());
            assertEquals('f', br.peek());
            assertEquals("foo", br.readLine());
            assertNull(br.readLine());
        }
    }

    @Test
    public void testReadLookahead1() throws Exception {
        try (ExtendedBufferedReader br = createBufferedReader("1\n2\r3\n")) {
            assertEquals(0, br.getLineNumber());
            assertEquals('1', br.peek());
            assertEquals(UNDEFINED, br.getLastChar());
            assertEquals(0, br.getLineNumber());
            assertEquals('1', br.read()); // Start line 1
            assertEquals('1', br.getLastChar());

            assertEquals(1, br.getLineNumber());
            assertEquals('\n', br.peek());
            assertEquals(1, br.getLineNumber());
            assertEquals('1', br.getLastChar());
            assertEquals('\n', br.read());
            assertEquals(1, br.getLineNumber());
            assertEquals('\n', br.getLastChar());
            assertEquals(1, br.getLineNumber());

            assertEquals('2', br.peek());
            assertEquals(1, br.getLineNumber());
            assertEquals('\n', br.getLastChar());
            assertEquals(1, br.getLineNumber());
            assertEquals('2', br.read()); // Start line 2
            assertEquals(2, br.getLineNumber());
            assertEquals('2', br.getLastChar());

            assertEquals('\r', br.peek());
            assertEquals(2, br.getLineNumber());
            assertEquals('2', br.getLastChar());
            assertEquals('\r', br.read());
            assertEquals('\r', br.getLastChar());
            assertEquals(2, br.getLineNumber());

            assertEquals('3', br.peek());
            assertEquals('\r', br.getLastChar());
            assertEquals('3', br.read()); // Start line 3
            assertEquals('3', br.getLastChar());
            assertEquals(3, br.getLineNumber());

            assertEquals('\n', br.peek());
            assertEquals(3, br.getLineNumber());
            assertEquals('3', br.getLastChar());
            assertEquals('\n', br.read());
            assertEquals(3, br.getLineNumber());
            assertEquals('\n', br.getLastChar());
            assertEquals(3, br.getLineNumber());

            assertEquals(EOF, br.peek());
            assertEquals('\n', br.getLastChar());
            assertEquals(EOF, br.read());
            assertEquals(EOF, br.getLastChar());
            assertEquals(EOF, br.read());
            assertEquals(EOF, br.peek());
            assertEquals(3, br.getLineNumber());

        }
    }

    @Test
    public void testReadLookahead2() throws Exception {
        final char[] ref = new char[5];
        final char[] res = new char[5];

        try (ExtendedBufferedReader br = createBufferedReader("abcdefg")) {
            ref[0] = 'a';
            ref[1] = 'b';
            ref[2] = 'c';
            assertEquals(3, br.read(res, 0, 3));
            assertArrayEquals(ref, res);
            assertEquals('c', br.getLastChar());

            assertEquals('d', br.peek());
            ref[4] = 'd';
            assertEquals(1, br.read(res, 4, 1));
            assertArrayEquals(ref, res);
            assertEquals('d', br.getLastChar());
        }
    }
}
