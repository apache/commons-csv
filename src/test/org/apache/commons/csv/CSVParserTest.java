/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
  
  /**
   * TestCSVParser.
   */
  class TestCSVParser extends CSVParser {
    /**
     * Test parser to investigate the type of the internal Token.
     * @param in a Reader
     */
    TestCSVParser(Reader in) {
      super(in);
    }
    /**
     * Calls super.nextToken() and prints out a String representation of token
     * type and content.
     * @return String representation of token type and content
     * @throws IOException like {@link CSVParser#nextToken()}
     */
    public String testNextToken() throws IOException {
      Token t = super.nextToken();
      String tmp = Integer.toString(t.type) + ";" + t.content + ";";
      System.out.println("token=" + tmp);
      return tmp;
    }
  }
  
  /**
   * Constructor for JUnit.
   * @param name Name to be used in JUnit Test Environment
   */
  public CSVParserTest(String name) {
    super(name);
  }

  /**
   * Returns a Test suite for JUnit.
   * @return Test suite for JUnit
   */
  public static Test suite() {
    return new TestSuite(CSVParserTest.class);
  }


  // ======================================================
  //   lexer tests
  // ======================================================
  
  // Single line (without comment)
  public void testNextToken1() throws IOException {
    String code = "abc,def, hijk,  lmnop,   qrst,uv ,wxy   ,z , ,";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";abc;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";def;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";hijk;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";lmnop;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";qrst;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";uv;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";wxy;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";z;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());  
  }
  
  // multiline including comments (and empty lines)
  public void testNextToken2() throws IOException {
    /*   file:   1,2,3,
     *           a,b x,c
     *
     *           # this is a comment 
     *           d,e,
     * 
     */
    String code = "1,2,3,\na,b x,c\n#foo\n\nd,e,\n\n";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.getStrategy().setIgnoreEmptyLines(false);
    parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
    parser.getStrategy().setCommentStart('#');
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";1;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";2;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";3;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";b x;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";c;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";d;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";e;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());    
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());    
    
  }
 
  // simple token with escaping
  public void testNextToken3() throws IOException {
    /* file: a,\,,b
     *       \,,
     */
    String code = "a,\\,,b\n\\,,";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
    parser.getStrategy().setCommentStart('#');
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    // an unquoted single backslash is not an escape char
    assertEquals(CSVParser.TT_TOKEN + ";\\;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
    // an unquoted single backslash is not an escape char
    assertEquals(CSVParser.TT_TOKEN + ";\\;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());
  }
  
  // encapsulator tokenizer (sinle line)
  public void testNextToken4() throws IOException {
    /* file:  a,"foo",b
     *        a,   " foo",b
     *        a,"foo "   ,b     // whitespace after closing encapsulator
     *        a,  " foo " ,b
     */ 
     String code = 
      "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
     TestCSVParser parser = new TestCSVParser(new StringReader(code));
     parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
     System.out.println("---------\n" + code + "\n-------------");
     assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + ";foo;", parser.testNextToken());
     assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + "; foo;", parser.testNextToken());
     assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + ";foo ;", parser.testNextToken());
     assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
     assertEquals(CSVParser.TT_TOKEN + "; foo ;", parser.testNextToken());
//     assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
     assertEquals(CSVParser.TT_EOF + ";b;", parser.testNextToken());    
  }
  
  // encapsulator tokenizer (multi line, delimiter in string)
  public void testNextToken5() throws IOException {   
    String code = 
      "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\",\"\\\"\""
      + ",\"\\,\"" 
      + ",\"\"\"\"";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";foo\n;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";foo\n  baar ,,,;",
        parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";\n\t \n;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";\";", parser.testNextToken());
    // escape char in quoted input only escapes delimiter
    assertEquals(CSVParser.TT_TOKEN + ";\\,;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";\";", parser.testNextToken());
  }
  
  // change delimiters, comment, encapsulater
  public void testNextToken6() throws IOException {
    /* file: a;'b and \' more
     *       '
     *       !comment;;;;
     *       ;;
     */
    String code = "a;'b and \\' more\n'\n!comment;;;;\n;;";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setStrategy( new CSVStrategy(';', '\'', '!') );
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    assertEquals(
      CSVParser.TT_EORECORD + ";b and ' more\n;", 
      parser.testNextToken());
  }
  
  
  // ======================================================
  //   parser tests
  // ======================================================
  
  String code = 
    "a,b,c,d\n"
    + " a , b , 1 2 \n"
    + "\"foo baar\", b,\n"
    + "   \"foo\n,,\n\"\",,\n\\\"\",d,e\n";
  String[][] res = { 
    {"a", "b", "c", "d"},
    {"a", "b", "1 2"}, 
    {"foo baar", "b", ""}, 
    {"foo\n,,\n\",,\n\"", "d", "e"}
  };
  public void testGetLine() throws IOException {
    CSVParser parser = new CSVParser(new StringReader(code));
    System.out.println("---------\n" + code + "\n-------------");
    String[] tmp = null;
    for (int i = 0; i < res.length; i++) {
      tmp = parser.getLine();
      assertTrue(Arrays.equals(res[i], tmp));
    }
    tmp = parser.getLine();
    assertTrue(tmp == null);
  }
  
  public void testNextValue() throws IOException {
    CSVParser parser = new CSVParser(new StringReader(code));
    System.out.println("---------\n" + code + "\n-------------");
    String tmp = null;
    for (int i = 0; i < res.length; i++) {
      for (int j = 0; j < res[i].length; j++) {
        tmp = parser.nextValue();
        assertEquals(res[i][j], tmp);
      }
    }
    tmp = parser.nextValue();
    assertTrue(tmp == null);    
  }
  
  public void testGetAllValues() throws IOException {
    CSVParser parser = new CSVParser(new StringReader(code));
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
  public void testExcelStrategy1() throws IOException {
    String code = 
      "value1;value2;value3;value4\r\na;b;c;d\r\n  x;;;"
      + "\r\n\r\n\"\"\"hello\"\"\";\"  \"\"world\"\"\";\"abc\ndef\";\r\n";
    String[][] res = {
      {"value1", "value2", "value3", "value4"},
      {"a", "b", "c", "d"},
      {"  x", "", "", ""},
      {""},
      {"\"hello\"", "  \"world\"", "abc\ndef", ""}
    };
    CSVParser parser = new CSVParser(new StringReader(code));
    parser.setStrategy(CSVStrategy.EXCEL_STRATEGY);
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
  public void testExcelStrategy2() throws Exception {
    String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
    String[][] res = {
      {"foo", "baar"},
      {""},
      {"hello", ""},
      {""},
      {"world", ""}
    };
    CSVParser parser = new CSVParser(new StringReader(code));
    parser.setStrategy(CSVStrategy.EXCEL_STRATEGY);
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      for (int j = 0; j < tmp[i].length; j++) {
        System.out.println("'" + tmp[i][j] + "'");
      }
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
  public void testEndOfFileBehaviourExcel() throws Exception {
    String[] codes = {
        "hello;\r\n\r\nworld;\r\n",
        "hello;\r\n\r\nworld;",
        "hello;\r\n\r\nworld;\"\"\r\n",
        "hello;\r\n\r\nworld;\"\"",
        "hello;\r\n\r\nworld;\n",
        "hello;\r\n\r\nworld;",
        "hello;\r\n\r\nworld;\"\"\n",
        "hello;\r\n\r\nworld;\"\""
        };
    String[][] res = {
      {"hello", ""},
      {""},  // ExcelStrategy does not ignore empty lines
      {"world", ""}
    };
    String code;
    for (int codeIndex = 0; codeIndex < codes.length; codeIndex++) {
      code = codes[codeIndex];
      CSVParser parser = new CSVParser(new StringReader(code));
      parser.setStrategy(CSVStrategy.EXCEL_STRATEGY);
      System.out.println("---------\n" + code + "\n-------------");
      String[][] tmp = parser.getAllValues();
      assertEquals(res.length, tmp.length);
      assertTrue(tmp.length > 0);
      for (int i = 0; i < res.length; i++) {
        for (int j = 0; j < tmp[i].length; j++) {
          System.out.println("'" + tmp[i][j] + "'");
        }
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
      {"hello", ""},  // CSV Strategy ignores empty lines
      {"world", ""}
    };
    String code;
    for (int codeIndex = 0; codeIndex < codes.length; codeIndex++) {
      code = codes[codeIndex];
      CSVParser parser = new CSVParser(new StringReader(code));
      parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
      System.out.println("---------\n" + code + "\n-------------");
      String[][] tmp = parser.getAllValues();
      assertEquals(res.length, tmp.length);
      assertTrue(tmp.length > 0);
      for (int i = 0; i < res.length; i++) {
        for (int j = 0; j < tmp[i].length; j++) {
          System.out.println("'" + tmp[i][j] + "'");
        }
        assertTrue(Arrays.equals(res[i], tmp[i]));
      }
    }
  }
  
  public void testEmptyLineBehaviourExcel() throws Exception {
    String[] codes = {
        "hello;\r\n\r\n\r\n",
        "hello;\n\n\n",
        "hello;\"\"\r\n\r\n\r\n",
        "hello;\"\"\n\n\n"
        };
    String[][] res = {
      {"hello", ""},
      {""},  // ExcelStrategy does not ignore empty lines
      {""}
    };
    String code;
    for (int codeIndex = 0; codeIndex < codes.length; codeIndex++) {
      code = codes[codeIndex];
      CSVParser parser = new CSVParser(new StringReader(code));
      parser.setStrategy(CSVStrategy.EXCEL_STRATEGY);
      System.out.println("---------\n" + code + "\n-------------");
      String[][] tmp = parser.getAllValues();
      assertEquals(res.length, tmp.length);
      assertTrue(tmp.length > 0);
      for (int i = 0; i < res.length; i++) {
        for (int j = 0; j < tmp[i].length; j++) {
          System.out.println("'" + tmp[i][j] + "'");
        }
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
      {"hello", ""}  // CSV Strategy ignores empty lines
    };
    String code;
    for (int codeIndex = 0; codeIndex < codes.length; codeIndex++) {
      code = codes[codeIndex];
      CSVParser parser = new CSVParser(new StringReader(code));
      parser.setStrategy(CSVStrategy.DEFAULT_STRATEGY);
      System.out.println("---------\n" + code + "\n-------------");
      String[][] tmp = parser.getAllValues();
      assertEquals(res.length, tmp.length);
      assertTrue(tmp.length > 0);
      for (int i = 0; i < res.length; i++) {
        for (int j = 0; j < tmp[i].length; j++) {
          System.out.println("'" + tmp[i][j] + "'");
        }
        assertTrue(Arrays.equals(res[i], tmp[i]));
      }
    }
  }
  
  public void testBackslashEscaping() throws IOException {
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
        { "one", "two", "three" },
        { "on\\\"e", "two" },
        { "on\"e", "two" },
        { "one", "tw\"o" },
        { "one", "t\\,wo" },  // backslash in quotes only escapes a delimiter (",")
        { "one", "two", "th,ree" },
        { "a\\\\" },     // backslash in quotes only escapes a delimiter (",")
        { "a\\", "b" },  // a backslash must be returnd 
        { "a\\\\,b" }    // backslash in quotes only escapes a delimiter (",")
      };
    CSVParser parser = new CSVParser(new StringReader(code));
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      for (int j = 0; j < tmp[i].length; j++) {
        System.out.println("'" + tmp[i][j] + "'");
      }
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
    public void testUnicodeEscape() throws IOException {
      String code = "abc,\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063";
      CSVParser parser = new CSVParser(new StringReader(code));
      System.out.println("---------\n" + code + "\n-------------");
      parser.getStrategy().setUnicodeEscapeInterpretation(true);
      String[] data = parser.getLine();
      assertEquals(2, data.length);
      assertEquals("abc", data[0]);
      assertEquals("public", data[1]);
    }
    
    public void testCarriageReturnLineFeedEndings() throws IOException {
     String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
     CSVParser parser = new CSVParser(new StringReader(code));
     System.out.println("---------\n" + code + "\n-------------");
     String[][] data = parser.getAllValues();
     assertEquals(4, data.length);
    }
    
    public void testIgnoreEmptyLines() throws IOException {
      String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
      //String code = "world\r\n\n";
      //String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
      CSVParser parser = new CSVParser(new StringReader(code));
      System.out.println("---------\n" + code + "\n-------------");
      String[][] data = parser.getAllValues();
//      for (int i = 0; i < data.length; i++) {
//        if (i > 0) {
//          System.out.print('\n');
//        }
//        for (int j = 0; j < data[i].length; j++) {
//          System.out.print("(" + j + ")'" + data[i][j] + "'");
//        }
//      }
//      System.out.println("----------");
      assertEquals(3, data.length);
    }
    
    public void testLineTokenConsistency() throws IOException {
      String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
      CSVParser parser = new CSVParser(new StringReader(code));
      System.out.println("---------\n" + code + "\n-------------");
      String[][] data = parser.getAllValues();
      parser = new CSVParser(new StringReader(code));
      CSVParser parser1 = new CSVParser(new StringReader(code));
      for (int i = 0; i < data.length; i++) {
        assertTrue(Arrays.equals(parser1.getLine(), data[i]));
        for (int j = 0; j < data[i].length; j++) {
          assertEquals(parser.nextValue(), data[i][j]);
        }
      }
    }
}
