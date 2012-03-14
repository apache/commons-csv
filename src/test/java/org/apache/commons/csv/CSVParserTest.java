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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * CSVParserTest
 *
 * The test are organized in three different sections:
 * The 'setter/getter' section, the lexer section and finally the parser
 * section. In case a test fails, you should follow a top-down approach for
 * fixing a potential bug (its likely that the parser itself fails if the lexer
 * has problems...).
 */
public class CSVParserTest extends TestCase {

    String code = "a,b,c,d\n"
                    + " a , b , 1 2 \n"
                    + "\"foo baar\", b,\n"
                    // + "   \"foo\n,,\n\"\",,\n\\\"\",d,e\n";
                    + "   \"foo\n,,\n\"\",,\n\"\"\",d,e\n";   // changed to use standard CSV escaping
    String[][] res = {
            {"a", "b", "c", "d"},
            {"a", "b", "1 2"},
            {"foo baar", "b", ""},
            {"foo\n,,\n\",,\n\"", "d", "e"}
    };

    public void testGetLine() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(code));
        for (String[] re : res) {
            assertTrue(Arrays.equals(re, parser.getRecord()));
        }
        
        assertTrue(parser.getRecord() == null);
    }

    public void testGetRecords() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] tmp = parser.getRecords();
        assertEquals(res.length, tmp.length);
        assertTrue(tmp.length > 0);
        for (int i = 0; i < res.length; i++) {
            assertTrue(Arrays.equals(res[i], tmp[i]));
        }
    }

    public void testExcelFormat1() throws IOException {
        String code =
                "value1,value2,value3,value4\r\na,b,c,d\r\n  x,,,"
                        + "\r\n\r\n\"\"\"hello\"\"\",\"  \"\"world\"\"\",\"abc\ndef\",\r\n";
        String[][] res = {
                {"value1", "value2", "value3", "value4"},
                {"a", "b", "c", "d"},
                {"  x", "", "", ""},
                {""},
                {"\"hello\"", "  \"world\"", "abc\ndef", ""}
        };
        CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        String[][] tmp = parser.getRecords();
        assertEquals(res.length, tmp.length);
        assertTrue(tmp.length > 0);
        for (int i = 0; i < res.length; i++) {
            assertTrue(Arrays.equals(res[i], tmp[i]));
        }
    }

    public void testExcelFormat2() throws Exception {
        String code = "foo,baar\r\n\r\nhello,\r\n\r\nworld,\r\n";
        String[][] res = {
                {"foo", "baar"},
                {""},
                {"hello", ""},
                {""},
                {"world", ""}
        };
        CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        String[][] tmp = parser.getRecords();
        assertEquals(res.length, tmp.length);
        assertTrue(tmp.length > 0);
        for (int i = 0; i < res.length; i++) {
            assertTrue(Arrays.equals(res[i], tmp[i]));
        }
    }

    public void testEndOfFileBehaviourExcel() throws Exception {
        String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        String[][] res = {
                {"hello", ""},
                {""},  // Excel format does not ignore empty lines
                {"world", ""}
        };
        
        for (String code : codes) {
            CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            String[][] tmp = parser.getRecords();
            assertEquals(res.length, tmp.length);
            assertTrue(tmp.length > 0);
            for (int i = 0; i < res.length; i++) {
                assertTrue(Arrays.equals(res[i], tmp[i]));
            }
        }
    }

    public void testEndOfFileBehaviorCSV() throws Exception {
        String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        String[][] res = {
                {"hello", ""},  // CSV format ignores empty lines
                {"world", ""}
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(new StringReader(code));
            String[][] tmp = parser.getRecords();
            assertEquals(res.length, tmp.length);
            assertTrue(tmp.length > 0);
            for (int i = 0; i < res.length; i++) {
                assertTrue(Arrays.equals(res[i], tmp[i]));
            }
        }
    }

    public void testEmptyLineBehaviourExcel() throws Exception {
        String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        String[][] res = {
                {"hello", ""},
                {""},  // Excel format does not ignore empty lines
                {""}
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            String[][] tmp = parser.getRecords();
            assertEquals(res.length, tmp.length);
            assertTrue(tmp.length > 0);
            for (int i = 0; i < res.length; i++) {
                assertTrue(Arrays.equals(res[i], tmp[i]));
            }
        }
    }

    public void testEmptyLineBehaviourCSV() throws Exception {
        String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        String[][] res = {
                {"hello", ""}  // CSV format ignores empty lines
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(new StringReader(code));
            String[][] tmp = parser.getRecords();
            assertEquals(res.length, tmp.length);
            assertTrue(tmp.length > 0);
            for (int i = 0; i < res.length; i++) {
                assertTrue(Arrays.equals(res[i], tmp[i]));
            }
        }
    }

    public void OLDtestBackslashEscaping() throws IOException {
        String code =
                "one,two,three\n"
                        + "on\\\"e,two\n"
                        + "on\"e,two\n"
                        + "one,\"tw\\\"o\"\n"
                        + "one,\"t\\,wo\"\n"
                        + "one,two,\"th,ree\"\n"
                        + "\"a\\\\\"\n"
                        + "a\\,b\n"
                        + "\"a\\\\,b\"";
        String[][] res = {
                {"one", "two", "three"},
                {"on\\\"e", "two"},
                {"on\"e", "two"},
                {"one", "tw\"o"},
                {"one", "t\\,wo"},  // backslash in quotes only escapes a delimiter (",")
                {"one", "two", "th,ree"},
                {"a\\\\"},     // backslash in quotes only escapes a delimiter (",")
                {"a\\", "b"},  // a backslash must be returnd
                {"a\\\\,b"}    // backslash in quotes only escapes a delimiter (",")
        };
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] tmp = parser.getRecords();
        assertEquals(res.length, tmp.length);
        assertTrue(tmp.length > 0);
        for (int i = 0; i < res.length; i++) {
            assertTrue(Arrays.equals(res[i], tmp[i]));
        }
    }

    public void testBackslashEscaping() throws IOException {

        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        String code =
                "one,two,three\n" // 0
                        + "'',''\n"       // 1) empty encapsulators
                        + "/',/'\n"       // 2) single encapsulators
                        + "'/'','/''\n"   // 3) single encapsulators encapsulated via escape
                        + "'''',''''\n"   // 4) single encapsulators encapsulated via doubling
                        + "/,,/,\n"       // 5) separator escaped
                        + "//,//\n"       // 6) escape escaped
                        + "'//','//'\n"   // 7) escape escaped in encapsulation
                        + "   8   ,   \"quoted \"\" /\" // string\"   \n"     // don't eat spaces
                        + "9,   /\n   \n"  // escaped newline
                        + "";
        String[][] res = {
                {"one", "two", "three"}, // 0
                {"", ""},                // 1
                {"'", "'"},              // 2
                {"'", "'"},              // 3
                {"'", "'"},              // 4
                {",", ","},              // 5
                {"/", "/"},              // 6
                {"/", "/"},              // 7
                {"   8   ", "   \"quoted \"\" \" / string\"   "},
                {"9", "   \n   "},
        };


        CSVFormat format = new CSVFormat(',', '\'', CSVFormat.DISABLED, '/', false, false, true, true, "\r\n");

        CSVParser parser = new CSVParser(code, format);
        String[][] tmp = parser.getRecords();
        assertTrue(tmp.length > 0);
        for (int i = 0; i < res.length; i++) {
            assertTrue(Arrays.equals(res[i], tmp[i]));
        }
    }

    public void testBackslashEscaping2() throws IOException {

        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        String code = ""
                + " , , \n"           // 1)
                + " \t ,  , \n"       // 2)
                + " // , /, , /,\n"   // 3)
                + "";
        String[][] res = {
                {" ", " ", " "},         // 1
                {" \t ", "  ", " "},     // 2
                {" / ", " , ", " ,"},    // 3
        };


        CSVFormat format = new CSVFormat(',',  CSVFormat.DISABLED,  CSVFormat.DISABLED, '/', false, false, true, true, "\r\n");

        CSVParser parser = new CSVParser(code, format);
        String[][] tmp = parser.getRecords();
        assertTrue(tmp.length > 0);

        if (!CSVPrinterTest.equals(res, tmp)) {
            assertTrue(false);
        }

    }


    public void testDefaultFormat() throws IOException {

        String code = ""
                + "a,b\n"            // 1)
                + "\"\n\",\" \"\n"   // 2)
                + "\"\",#\n"   // 2)
                ;
        String[][] res = {
                {"a", "b"},
                {"\n", " "},
                {"", "#"},
        };

        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(CSVFormat.DISABLED, format.getCommentStart());

        CSVParser parser = new CSVParser(code, format);
        String[][] tmp = parser.getRecords();
        assertTrue(tmp.length > 0);

        if (!CSVPrinterTest.equals(res, tmp)) {
            assertTrue(false);
        }

        String[][] res_comments = {
                {"a", "b"},
                {"\n", " "},
                {""},
        };

        format = CSVFormat.DEFAULT.withCommentStart('#');
        parser = new CSVParser(code, format);
        tmp = parser.getRecords();

        if (!CSVPrinterTest.equals(res_comments, tmp)) {
            assertTrue(false);
        }
    }


    public void testUnicodeEscape() throws Exception {
        String code = "abc,\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063";
        CSVParser parser = new CSVParser(code, CSVFormat.DEFAULT.withUnicodeEscapesInterpreted(true));
        final Iterator<String[]> iterator = parser.iterator();
        String[] data = iterator.next();
        assertEquals(2, data.length);
        assertEquals("abc", data[0]);
        assertEquals("public", data[1]);
        assertFalse("Should not have any more records", iterator.hasNext());
    }

    public void testCarriageReturnLineFeedEndings() throws IOException {
        String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] data = parser.getRecords();
        assertEquals(4, data.length);
    }

    public void testCarriageReturnEndings() throws IOException {
        String code = "foo\rbaar,\rhello,world\r,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] data = parser.getRecords();
        assertEquals(4, data.length);
    }

    public void testLineFeedEndings() throws IOException {
        String code = "foo\nbaar,\nhello,world\n,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] data = parser.getRecords();
        assertEquals(4, data.length);
    }

    public void testIgnoreEmptyLines() throws IOException {
        String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        //String code = "world\r\n\n";
        //String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
        CSVParser parser = new CSVParser(new StringReader(code));
        String[][] data = parser.getRecords();
        assertEquals(3, data.length);
    }

    public void testForEach() {
        List<String[]> records = new ArrayList<String[]>();
        
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        
        for (String[] record : CSVFormat.DEFAULT.parse(in)) {
            records.add(record);
        }
        
        assertEquals(3, records.size());
        assertTrue(Arrays.equals(new String[]{"a", "b", "c"}, records.get(0)));
        assertTrue(Arrays.equals(new String[]{"1", "2", "3"}, records.get(1)));
        assertTrue(Arrays.equals(new String[]{"x", "y", "z"}, records.get(2)));
    }

    public void testIterator() {
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        
        Iterator<String[]> iterator = CSVFormat.DEFAULT.parse(in).iterator();
        
        assertTrue(iterator.hasNext());
        iterator.remove();
        assertTrue(Arrays.equals(new String[]{"a", "b", "c"}, iterator.next()));
        assertTrue(Arrays.equals(new String[]{"1", "2", "3"}, iterator.next()));
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(Arrays.equals(new String[]{"x", "y", "z"}, iterator.next()));
        assertFalse(iterator.hasNext());
        
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
}
