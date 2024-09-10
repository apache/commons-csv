/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.commons.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BufferedReaderFork}.
 */
public class BufferedReaderForkTest {

    BufferedReaderFork br;

    String testString =
        "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    /**
     * The spec says that BufferedReaderFork.readLine() considers only "\r", "\n"
     * and "\r\n" to be line separators. We must not permit additional separator
     * characters.
     */
    @Test
    public void test_readLine_IgnoresEbcdic85Characters() throws IOException {
        assertLines("A\u0085B", "A\u0085B");
    }

    @Test
    public void test_readLine_Separators() throws IOException {
        assertLines("A\nB\nC", "A", "B", "C");
        assertLines("A\rB\rC", "A", "B", "C");
        assertLines("A\r\nB\r\nC", "A", "B", "C");
        assertLines("A\n\rB\n\rC", "A", "", "B", "", "C");
        assertLines("A\n\nB\n\nC", "A", "", "B", "", "C");
        assertLines("A\r\rB\r\rC", "A", "", "B", "", "C");
        assertLines("A\n\n", "A", "");
        assertLines("A\n\r", "A", "");
        assertLines("A\r\r", "A", "");
        assertLines("A\r\n", "A");
        assertLines("A\r\n\r\n", "A", "");
    }

    private void assertLines(String in, String... lines) throws IOException {
        BufferedReaderFork bufferedReader = new BufferedReaderFork(new Support_StringReader(in));
        for (String line : lines) {
            assertEquals(line, bufferedReader.readLine());
        }
        assertNull(bufferedReader.readLine());
    }

    /**
     * @tests java.io.BufferedReaderFork#BufferedReader(java.io.Reader)
     */
    @Test
    public void test_ConstructorLjava_io_Reader() {
        // Test for method java.io.BufferedReader(java.io.Reader)
        assertTrue(true, "Used in tests");
    }

    /**
     * @tests java.io.BufferedReaderFork#BufferedReader(java.io.Reader, int)
     */
    @Test
    public void test_ConstructorLjava_io_ReaderI() {
        // Test for method java.io.BufferedReader(java.io.Reader, int)
        assertTrue(true, "Used in tests");
    }

    /**
     * @tests java.io.BufferedReaderFork#close()
     */
    @Test
    public void test_close() {
        // Test for method void java.io.BufferedReaderFork.close()
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            br.close();
            br.read();
            fail("Read on closed stream");
        } catch (IOException x) {
            return;
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#mark(int)
     */
    @Test
    public void test_markI() throws IOException {
        // Test for method void java.io.BufferedReaderFork.mark(int)
        char[] buf = null;
        br = new BufferedReaderFork(new Support_StringReader(testString));
        br.skip(500);
        br.mark(1000);
        br.skip(250);
        br.reset();
        buf = new char[testString.length()];
        br.read(buf, 0, 500);
        assertTrue(testString.substring(500,
            1000).equals(new String(buf, 0, 500)), "Failed to set mark properly");

        try {
            br = new BufferedReaderFork(new Support_StringReader(testString), 800);
            br.skip(500);
            br.mark(250);
            br.read(buf, 0, 1000);
            br.reset();
            fail("Failed to invalidate mark properly");
        } catch (IOException x) {
            // Expected
        }

        char[] chars = new char[256];
        for (int i = 0; i < 256; i++)
            chars[i] = (char)i;
        Reader in = new BufferedReaderFork(new Support_StringReader(new String(
            chars)), 12);

        in.skip(6);
        in.mark(14);
        in.read(new char[14], 0, 14);
        in.reset();
        assertTrue(in.read() == (char)6
            && in.read() == (char)7, "Wrong chars");

        in = new BufferedReaderFork(new Support_StringReader(new String(chars)), 12);
        in.skip(6);
        in.mark(8);
        in.skip(7);
        in.reset();
        assertTrue(in.read() == (char)6
            && in.read() == (char)7, "Wrong chars 2");

        BufferedReaderFork br = new BufferedReaderFork(new StringReader("01234"), 2);
        br.mark(3);
        char[] carray = new char[3];
        int result = br.read(carray);
        assertEquals(3, result);
        assertEquals('0', carray[0], "Assert 0:");
        assertEquals('1', carray[1], "Assert 1:");
        assertEquals('2', carray[2], "Assert 2:");
        assertEquals('3', br.read(), "Assert 3:");

        br = new BufferedReaderFork(new StringReader("01234"), 2);
        br.mark(3);
        carray = new char[4];
        result = br.read(carray);
        assertEquals(4, result, "Assert 4:");
        assertEquals('0', carray[0], "Assert 5:");
        assertEquals('1', carray[1], "Assert 6:");
        assertEquals('2', carray[2], "Assert 7:");
        assertEquals('3', carray[3], "Assert 8:");
        assertEquals('4', br.read(), "Assert 9:");
        assertEquals(-1, br.read(), "Assert 10:");

        BufferedReaderFork reader = new BufferedReaderFork(new StringReader("01234"));
        reader.mark(Integer.MAX_VALUE);
        reader.read();
        reader.close();
    }

    /**
     * @tests java.io.BufferedReaderFork#markSupported()
     */
    @Test
    public void test_markSupported() {
        // Test for method boolean java.io.BufferedReaderFork.markSupported()
        br = new BufferedReaderFork(new Support_StringReader(testString));
        assertTrue(br.markSupported(), "markSupported returned false");
    }

    /**
     * @tests java.io.BufferedReaderFork#read()
     */
    @Test
    public void test_read() throws IOException {
        // Test for method int java.io.BufferedReaderFork.read()
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            int r = br.read();
            assertTrue(testString.charAt(0) == r, "Char read improperly");
            br = new BufferedReaderFork(new Support_StringReader(new String(
                new char[] {'\u8765' })));
            assertTrue(br.read() == '\u8765', "Wrong double byte character");
        } catch (java.io.IOException e) {
            fail("Exception during read test");
        }

        char[] chars = new char[256];
        for (int i = 0; i < 256; i++)
            chars[i] = (char)i;
        Reader in = new BufferedReaderFork(new Support_StringReader(new String(
            chars)), 12);
        try {
            assertEquals(0, in.read(), "Wrong initial char"); // Fill the
            // buffer
            char[] buf = new char[14];
            in.read(buf, 0, 14); // Read greater than the buffer
            assertTrue(new String(buf)
                .equals(new String(chars, 1, 14)), "Wrong block read data");
            assertEquals(15, in.read(), "Wrong chars"); // Check next byte
        } catch (IOException e) {
            fail("Exception during read test 2:" + e);
        }

        // regression test for HARMONY-841
        assertTrue(new BufferedReaderFork(new CharArrayReader(new char[5], 1, 0), 2).read() == -1);
    }

    /**
     * @tests java.io.BufferedReaderFork#read(char[], int, int)
     */
    @Test
    public void test_read$CII() throws Exception {
        char[] ca = new char[2];
        BufferedReaderFork toRet = new BufferedReaderFork(new InputStreamReader(
            new ByteArrayInputStream(new byte[0])));

        /* Null buffer should throw NPE even when len == 0 */
        try {
            toRet.read(null, 1, 0);
            fail("null buffer reading zero bytes should throw NPE");
        } catch (NullPointerException e) {
            //expected
        }

        try {
            toRet.close();
        } catch (IOException e) {
            fail("unexpected 1: " + e);
        }

        try {
            toRet.read(null, 1, 0);
            fail("null buffer reading zero bytes on closed stream should throw IOException");
        } catch (IOException e) {
            //expected
        }

        /* Closed reader should throw IOException reading zero bytes */
        try {
            toRet.read(ca, 0, 0);
            fail("Reading zero bytes on a closed reader should not work");
        } catch (IOException e) {
            // expected
        }

        /*
         * Closed reader should throw IOException in preference to index out of
         * bounds
         */
        try {
            // Read should throw IOException before
            // ArrayIndexOutOfBoundException
            toRet.read(ca, 1, 5);
            fail("IOException should have been thrown");
        } catch (IOException e) {
            // expected
        }

        // Test to ensure that a drained stream returns 0 at EOF
        toRet = new BufferedReaderFork(new InputStreamReader(
            new ByteArrayInputStream(new byte[2])));
        try {
            assertEquals(2,
                toRet.read(ca, 0, 2), "Emptying the reader should return two bytes");
            assertEquals(-1, toRet.read(ca, 0,
                2), "EOF on a reader should be -1");
            assertEquals(0, toRet
                .read(ca, 0, 0), "Reading zero bytes at EOF should work");
        } catch (IOException ex) {
            fail("Unexpected IOException : " + ex.getLocalizedMessage());
        }

        // Test for method int java.io.BufferedReaderFork.read(char [], int, int)
        try {
            char[] buf = new char[testString.length()];
            br = new BufferedReaderFork(new Support_StringReader(testString));
            br.read(buf, 50, 500);
            assertTrue(new String(buf, 50, 500)
                .equals(testString.substring(0, 500)), "Chars read improperly");
        } catch (java.io.IOException e) {
            fail("Exception during read test");
        }

        BufferedReaderFork bufin = new BufferedReaderFork(new Reader() {

            int size = 2, pos = 0;

            char[] contents = new char[size];

            public int read() throws IOException {
                if (pos >= size)
                    throw new IOException("Read past end of data");
                return contents[pos++];
            }

            public int read(char[] buf, int off, int len) throws IOException {
                if (pos >= size)
                    throw new IOException("Read past end of data");
                int toRead = len;
                if (toRead > (size - pos))
                    toRead = size - pos;
                System.arraycopy(contents, pos, buf, off, toRead);
                pos += toRead;
                return toRead;
            }

            public boolean ready() throws IOException {
                return size - pos > 0;
            }

            public void close() throws IOException {
            }
        });
        try {
            bufin.read();
            int result = bufin.read(new char[2], 0, 2);
            assertTrue(result == 1, "Incorrect result: " + result);
        } catch (IOException e) {
            fail("Unexpected: " + e);
        }

        //regression for HARMONY-831
        try {
            new BufferedReaderFork(new PipedReader(), 9).read(new char[] {}, 7, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        // Regression for HARMONY-54
        char[] ch = {};
        BufferedReaderFork reader = new BufferedReaderFork(new CharArrayReader(ch));
        try {
            // Check exception thrown when the reader is open.
            reader.read(null, 1, 0);
            fail("Assert 0: NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }

        // Now check IOException is thrown in preference to
        // NullPointerexception when the reader is closed.
        reader.close();
        try {
            reader.read(null, 1, 0);
            fail("Assert 1: IOException expected");
        } catch (IOException e) {
            // Expected
        }

        try {
            // And check that the IOException is thrown before
            // ArrayIndexOutOfBoundException
            reader.read(ch, 0, 42);
            fail("Assert 2: IOException expected");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#read(char[], int, int)
     */
    @Test
    public void test_read_$CII_Exception() throws IOException {
        br = new BufferedReaderFork(new Support_StringReader(testString));
        char[] nullCharArray = null;
        char[] charArray = testString.toCharArray();

        try {
            br.read(nullCharArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            br.read(nullCharArray, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            br.read(nullCharArray, 0, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            br.read(nullCharArray, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            br.read(nullCharArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            br.read(charArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            br.read(charArray, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        br.read(charArray, 0, 0);
        br.read(charArray, 0, charArray.length);
        br.read(charArray, charArray.length, 0);

        try {
            br.read(charArray, charArray.length + 1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            br.read(charArray, charArray.length + 1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        br.close();

        try {
            br.read(nullCharArray, -1, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }

        try {
            br.read(charArray, -1, 0);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }

        try {
            br.read(charArray, 0, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#readLine()
     */
    @Test
    public void test_readLine() {
        // Test for method java.lang.String java.io.BufferedReaderFork.readLine()
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            String r = br.readLine();
            assertEquals("Test_All_Tests", r, "readLine returned incorrect string");
        } catch (java.io.IOException e) {
            fail("Exception during readLine test");
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#ready()
     */
    @Test
    public void test_ready() {
        // Test for method boolean java.io.BufferedReaderFork.ready()
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            assertTrue(br.ready(), "ready returned false");
        } catch (java.io.IOException e) {
            fail("Exception during ready test" + e.toString());
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#reset()
     */
    @Test
    public void test_reset() {
        // Test for method void java.io.BufferedReaderFork.reset()
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            br.skip(500);
            br.mark(900);
            br.skip(500);
            br.reset();
            char[] buf = new char[testString.length()];
            br.read(buf, 0, 500);
            assertTrue(testString.substring(500,
                1000).equals(new String(buf, 0, 500)), "Failed to reset properly");
        } catch (java.io.IOException e) {
            fail("Exception during reset test");
        }
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            br.skip(500);
            br.reset();
            fail("Reset succeeded on unmarked stream");
        } catch (IOException x) {
            return;

        }
    }

    @Test
    public void test_reset_IOException() throws Exception {
        int[] expected = new int[] {'1', '2', '3', '4', '5', '6', '7', '8',
            '9', '0', -1 };
        br = new BufferedReaderFork(new Support_StringReader("1234567890"), 9);
        br.mark(9);
        for (int i = 0; i < 11; i++) {
            assertEquals(expected[i], br.read());
        }
        try {
            br.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // Expected
        }
        for (int i = 0; i < 11; i++) {
            assertEquals(-1, br.read());
        }

        br = new BufferedReaderFork(new Support_StringReader("1234567890"));
        br.mark(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i], br.read());
        }
        br.reset();
        for (int i = 0; i < 11; i++) {
            assertEquals(expected[i], br.read());
        }
    }

    /**
     * @tests java.io.BufferedReaderFork#skip(long)
     */
    @Test
    public void test_skipJ() {
        // Test for method long java.io.BufferedReaderFork.skip(long)
        try {
            br = new BufferedReaderFork(new Support_StringReader(testString));
            br.skip(500);
            char[] buf = new char[testString.length()];
            br.read(buf, 0, 500);
            assertTrue(testString.substring(500,
                1000).equals(new String(buf, 0, 500)), "Failed to set skip properly");
        } catch (java.io.IOException e) {
            fail("Exception during skip test");
        }

    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @BeforeEach
    protected void tearDown() {
        try {
            br.close();
        } catch (Exception e) {
        }
    }
}
