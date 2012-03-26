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

import java.io.StringReader;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExtendedBufferedReaderTest {

    @Test
    public void testEmptyInput() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertNull(br.readLine());
        assertEquals(0, br.read(new char[10], 0, 0));
    }

    @Test
    public void testReadLookahead1() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("1\n2\r3\n");
        assertEquals('1', br.lookAhead());
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        assertEquals('1', br.read());
        assertEquals('1', br.readAgain());

        assertEquals(0, br.getLineNumber());
        assertEquals('\n', br.lookAhead());
        assertEquals(0, br.getLineNumber());
        assertEquals('1', br.readAgain());
        assertEquals('\n', br.read());
        assertEquals(1, br.getLineNumber());
        assertEquals('\n', br.readAgain());
        assertEquals(1, br.getLineNumber());

        assertEquals('2', br.lookAhead());
        assertEquals(1, br.getLineNumber());
        assertEquals('\n', br.readAgain());
        assertEquals(1, br.getLineNumber());
        assertEquals('2', br.read());
        assertEquals('2', br.readAgain());

        assertEquals('\r', br.lookAhead());
        assertEquals('2', br.readAgain());
        assertEquals('\r', br.read());
        assertEquals('\r', br.readAgain());

        assertEquals('3', br.lookAhead());
        assertEquals('\r', br.readAgain());
        assertEquals('3', br.read());
        assertEquals('3', br.readAgain());

        assertEquals('\n', br.lookAhead());
        assertEquals(1, br.getLineNumber()); // will need fixing for CSV-75
        assertEquals('3', br.readAgain());
        assertEquals('\n', br.read());
        assertEquals(2, br.getLineNumber()); // will need fixing for CSV-75
        assertEquals('\n', br.readAgain());
        assertEquals(2, br.getLineNumber()); // will need fixing for CSV-75

        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        assertEquals('\n', br.readAgain());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());

    }

    @Test
    public void testReadLookahead2() throws Exception {
        char[] ref = new char[5];
        char[] res = new char[5];
        
        ExtendedBufferedReader br = getBufferedReader("abcdefg");
        ref[0] = 'a';
        ref[1] = 'b';
        ref[2] = 'c';
        assertEquals(3, br.read(res, 0, 3));
        assertArrayEquals(ref, res);
        assertEquals('c', br.readAgain());

        assertEquals('d', br.lookAhead());
        ref[4] = 'd';
        assertEquals(1, br.read(res, 4, 1));
        assertArrayEquals(ref, res);
        assertEquals('d', br.readAgain());
    }

    @Test
    public void testReadLine() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        assertNull(br.readLine());

        br = getBufferedReader("\n");
        assertEquals("",br.readLine());
        assertNull(br.readLine());

        br = getBufferedReader("foo\n\nhello");
        assertEquals(0, br.getLineNumber());
        assertEquals("foo",br.readLine());
        assertEquals(1, br.getLineNumber());
        assertEquals("",br.readLine());
        assertEquals(2, br.getLineNumber());
        assertEquals("hello",br.readLine());
        assertEquals(3, br.getLineNumber());
        assertNull(br.readLine());
        assertEquals(3, br.getLineNumber());

        br = getBufferedReader("foo\n\nhello");
        assertEquals('f', br.read());
        assertEquals('o', br.lookAhead());
        assertEquals("oo",br.readLine());
        assertEquals(1, br.getLineNumber());
        assertEquals('\n', br.lookAhead());
        assertEquals("",br.readLine());
        assertEquals(2, br.getLineNumber());
        assertEquals('h', br.lookAhead());
        assertEquals("hello",br.readLine());
        assertNull(br.readLine());
        assertEquals(3, br.getLineNumber());


        br = getBufferedReader("foo\rbaar\r\nfoo");
        assertEquals("foo",br.readLine());
        assertEquals('b', br.lookAhead());
        assertEquals("baar",br.readLine());
        assertEquals('f', br.lookAhead());
        assertEquals("foo",br.readLine());
        assertNull(br.readLine());
    }

    /*
     * Test to illustrate  https://issues.apache.org/jira/browse/CSV-75
     * 
     * TODO fix checks when code is fixed
     */
    @Test
    public void testReadChar() throws Exception {
        String LF="\n"; String CR="\r"; String CRLF=CR+LF; String LFCR=LF+CR;// easier to read the string below
        String test="a" + LF + "b" + CR + "c" + LF + LF + "d" + CR + CR + "e" + LFCR + "f "+ CRLF;
        //                EOL        eol        EOL  EOL        eol  eol        EOL+CR        EOL
        // EOL = current EOL behaviour with read() methods
        // eol = additional behaviour with readLine()
        final int EOLct=5;
        final int EOLeolct=9;
        ExtendedBufferedReader br;
        
        br = getBufferedReader(test);
        assertEquals(0, br.getLineNumber());
        while(br.readLine()!=null) {}
        assertEquals(EOLeolct, br.getLineNumber());

        br = getBufferedReader(test);
        assertEquals(0, br.getLineNumber());
        while(br.read()!=-1) {}
        assertEquals(EOLct, br.getLineNumber()); // will need fixing for CSV-75

        br = getBufferedReader(test);
        assertEquals(0, br.getLineNumber());
        char[] buff = new char[10];
        while(br.read(buff ,0, 3)!=-1) {}
        assertEquals(EOLct, br.getLineNumber()); // will need fixing for CSV-75
    }

    private ExtendedBufferedReader getBufferedReader(String s) {
        return new ExtendedBufferedReader(new StringReader(s));
    }
}
