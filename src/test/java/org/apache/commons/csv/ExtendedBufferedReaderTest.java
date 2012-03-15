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
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ExtendedBufferedReaderTest {

    @Test
    public void testEmptyInput() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        Assert.assertNull(br.readLine());
        Assert.assertEquals(0, br.read(new char[10], 0, 0));
    }

    @Test
    public void testReadLookahead1() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("1\n2\r3\n");
        Assert.assertEquals('1', br.lookAhead());
        Assert.assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        Assert.assertEquals('1', br.read());
        Assert.assertEquals('1', br.readAgain());

        Assert.assertEquals(0, br.getLineNumber());
        Assert.assertEquals('\n', br.lookAhead());
        Assert.assertEquals(0, br.getLineNumber());
        Assert.assertEquals('1', br.readAgain());
        Assert.assertEquals('\n', br.read());
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertEquals('\n', br.readAgain());
        Assert.assertEquals(1, br.getLineNumber());

        Assert.assertEquals('2', br.lookAhead());
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertEquals('\n', br.readAgain());
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertEquals('2', br.read());
        Assert.assertEquals('2', br.readAgain());

        Assert.assertEquals('\r', br.lookAhead());
        Assert.assertEquals('2', br.readAgain());
        Assert.assertEquals('\r', br.read());
        Assert.assertEquals('\r', br.readAgain());

        Assert.assertEquals('3', br.lookAhead());
        Assert.assertEquals('\r', br.readAgain());
        Assert.assertEquals('3', br.read());
        Assert.assertEquals('3', br.readAgain());

        Assert.assertEquals('\n', br.lookAhead());
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertEquals('3', br.readAgain());
        Assert.assertEquals('\n', br.read());
        Assert.assertEquals(2, br.getLineNumber());
        Assert.assertEquals('\n', br.readAgain());
        Assert.assertEquals(2, br.getLineNumber());

        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        Assert.assertEquals('\n', br.readAgain());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());

    }

    @Test
    public void testReadLookahead2() throws Exception {
        char[] ref = new char[5];
        char[] res = new char[5];
        
        ExtendedBufferedReader br = getBufferedReader("abcdefg");
        ref[0] = 'a';
        ref[1] = 'b';
        ref[2] = 'c';
        Assert.assertEquals(3, br.read(res, 0, 3));
        Assert.assertTrue(Arrays.equals(res, ref));
        Assert.assertEquals('c', br.readAgain());

        Assert.assertEquals('d', br.lookAhead());
        ref[4] = 'd';
        Assert.assertEquals(1, br.read(res, 4, 1));
        Assert.assertTrue(Arrays.equals(res, ref));
        Assert.assertEquals('d', br.readAgain());
    }

    @Test
    public void testReadLine() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        Assert.assertTrue(br.readLine() == null);

        br = getBufferedReader("\n");
        Assert.assertTrue(br.readLine().equals(""));
        Assert.assertTrue(br.readLine() == null);

        br = getBufferedReader("foo\n\nhello");
        Assert.assertEquals(0, br.getLineNumber());
        Assert.assertTrue(br.readLine().equals("foo"));
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertTrue(br.readLine().equals(""));
        Assert.assertEquals(2, br.getLineNumber());
        Assert.assertTrue(br.readLine().equals("hello"));
        Assert.assertEquals(3, br.getLineNumber());
        Assert.assertTrue(br.readLine() == null);
        Assert.assertEquals(3, br.getLineNumber());

        br = getBufferedReader("foo\n\nhello");
        Assert.assertEquals('f', br.read());
        Assert.assertEquals('o', br.lookAhead());
        Assert.assertTrue(br.readLine().equals("oo"));
        Assert.assertEquals(1, br.getLineNumber());
        Assert.assertEquals('\n', br.lookAhead());
        Assert.assertTrue(br.readLine().equals(""));
        Assert.assertEquals(2, br.getLineNumber());
        Assert.assertEquals('h', br.lookAhead());
        Assert.assertTrue(br.readLine().equals("hello"));
        Assert.assertTrue(br.readLine() == null);
        Assert.assertEquals(3, br.getLineNumber());


        br = getBufferedReader("foo\rbaar\r\nfoo");
        Assert.assertTrue(br.readLine().equals("foo"));
        Assert.assertEquals('b', br.lookAhead());
        Assert.assertTrue(br.readLine().equals("baar"));
        Assert.assertEquals('f', br.lookAhead());
        Assert.assertTrue(br.readLine().equals("foo"));
        Assert.assertTrue(br.readLine() == null);
    }

    private ExtendedBufferedReader getBufferedReader(String s) {
        return new ExtendedBufferedReader(new StringReader(s));
    }
}
