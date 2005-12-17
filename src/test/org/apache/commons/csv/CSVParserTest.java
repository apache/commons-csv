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
   * TestCSVParser
   */
  class TestCSVParser extends CSVParser {
    TestCSVParser(Reader in) {
      super(in);
    }
    public String testNextToken() throws IOException {
      Token t = super.nextToken();
      String tmp = Integer.toString(t.type) + ";" + t.content + ";";
      System.out.println("token=" + tmp);
      return tmp;
    }
  }
  
  /**
   * Constructor for CSVParserTest.
   * @param arg0
   */
  public CSVParserTest(String arg0) {
    super(arg0);
  }

  public static Test suite() {
    return new TestSuite(CSVParserTest.class);
  }


  // ======================================================
  //   getters / setters
  // ======================================================
  public void testGetSetCommentStart() {
    CSVParser parser = new CSVParser(new StringReader("hello world"));
    parser.setCommentStart('#');
    assertEquals(parser.getCommentStart(), '#');
    parser.setCommentStart('!');
    assertEquals(parser.getCommentStart(), '!');
  }

  public void testGetSetEncapsulator() {
    CSVParser parser = new CSVParser(new StringReader("hello world"));
    parser.setEncapsulator('"');
    assertEquals(parser.getEncapsulator(), '"');
    parser.setEncapsulator('\'');
    assertEquals(parser.getEncapsulator(), '\'');
  }

  public void testGetSetDelimiter() {
    CSVParser parser = new CSVParser(new StringReader("hello world"));
    parser.setDelimiter(';');
    assertEquals(parser.getDelimiter(), ';');
    parser.setDelimiter(',');
    assertEquals(parser.getDelimiter(), ',');
    parser.setDelimiter('\t');
    assertEquals(parser.getDelimiter(), '\t');
  }

  public void testSetCSVStrategy() {
    CSVParser parser = new CSVParser(new StringReader("hello world"));
    // default settings
    assertEquals(parser.getCommentStart(), '\0');
    assertEquals(parser.getEncapsulator(), '"');
    assertEquals(parser.getDelimiter(), ',');
    // explicit csv settings
    parser.setCSVStrategy();
    assertEquals(parser.getCommentStart(), '\0');
    assertEquals(parser.getEncapsulator(), '"');
    assertEquals(parser.getDelimiter(), ',');
  }
  
  
  
  // ======================================================
  //   lexer tests
  // ======================================================
  
  // single line (without comment)
  public void testNextToken1() throws IOException {
    String code = "abc,def, hijk,  lmnop,   qrst,uv ,wxy   ,z , ,";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setCSVStrategy();
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
    assertEquals(CSVParser.TT_EORECORD + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());  
  }
  
  // multiline including comments (and empty lines)
  public void testNextToken2() throws IOException {
    /*   file:   1,2,3,
     *           a,b,c
     *
     *           # this is a comment 
     *           d,e,
     * 
     */
    String code = "1,2,3,\na,b x,c\n#foo\n\nd,e,\n\n";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setIgnoreEmptyLines(false);
    parser.setCSVStrategy();
    parser.setCommentStart('#');
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
    parser.setCSVStrategy();
    parser.setCommentStart('#');
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";,;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";,;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";;", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());
  }
  
  // encapsulator tokenizer (sinle line)
  public void testNextToken4() throws IOException {
    /* file:  a,"foo",b
     *        a,   " foo",b
     *        a,"foo "   ,b
     *        a,  " foo " ,b
     */ 
     String code = 
      "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
     TestCSVParser parser = new TestCSVParser(new StringReader(code));
     parser.setCSVStrategy();
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
     assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
     assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());    
  }
  
  // encapsulator tokenizer (multi line, delimiter in string)
  public void testNextToken5() throws IOException {   
    String code = 
      "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\",\"\\\"\",\"\"\"\"";
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setCSVStrategy();
    System.out.println("---------\n" + code + "\n-------------");
    assertEquals(CSVParser.TT_TOKEN + ";a;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";foo\n;", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";b;", parser.testNextToken());
    assertEquals(
      CSVParser.TT_EORECORD + ";foo\n  baar ,,,;", 
      parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";\n\t \n;", parser.testNextToken());
    assertEquals(CSVParser.TT_TOKEN + ";\";", parser.testNextToken());
    assertEquals(CSVParser.TT_EORECORD + ";\";", parser.testNextToken());
    assertEquals(CSVParser.TT_EOF + ";;", parser.testNextToken());
    
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
    parser.setDelimiter(';');
    parser.setEncapsulator('\'');
    parser.setCommentStart('!');
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
    {"foo\n,,\n\",,\n\"", "d", "e"},
    {""}
  };
  public void testGetLine() throws IOException {
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
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
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
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
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
  public void testExcelStrategyTest() throws IOException {
    String code = 
      "value1;value2;value3;value4\r\na;b;c;d\r\n  x;;;"
      + "\r\n\r\n\"\"\"hello\"\"\";\"  \"\"world\"\"\";\"abc\ndef\";\r\n";
    String[][] res = {
      {"value1", "value2", "value3", "value4"},
      {"a", "b", "c", "d"},
      {"  x", "", "", ""},
      {""},
      {"\"hello\"", "  \"world\"", "abc\ndef", ""},
      {""}
    };
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setExcelStrategy();
    System.out.println("---------\n" + code + "\n-------------");
    String[][] tmp = parser.getAllValues();
    assertEquals(res.length, tmp.length);
    assertTrue(tmp.length > 0);
    for (int i = 0; i < res.length; i++) {
      assertTrue(Arrays.equals(res[i], tmp[i])); 
    }
  }
  
  public void testExcelStrategyTest2() throws Exception {
    String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
    String[][] res = {
      {"foo", "baar"},
      {""},
      {"hello", ""},
      {""},
      {"world", ""},
      {""} 
    };
    TestCSVParser parser = new TestCSVParser(new StringReader(code));
    parser.setExcelStrategy();
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
    //assertTrue(false);
  }
  
  // ======================================================
  //   static parser tests
  // ======================================================
  public void testParse1() throws IOException {
      String[][] data = CSVParser.parse("abc\ndef");
      assertEquals(2, data.length);
      assertEquals(1, data[0].length);
      assertEquals(1, data[1].length);
      assertEquals("abc", data[0][0]);
      assertEquals("def", data[1][0]);
    }

    public void testParse2() throws IOException {
      String[][] data = CSVParser.parse("abc,def,\"ghi,jkl\"\ndef");
      assertEquals(2, data.length);
      assertEquals(3, data[0].length);
      assertEquals(1, data[1].length);
      assertEquals("abc", data[0][0]);
      assertEquals("def", data[0][1]);
      assertEquals("ghi,jkl", data[0][2]);
      assertEquals("def", data[1][0]);
    }

    public void testParse3() throws IOException {
      String[][] data = CSVParser.parse("abc,\"def\nghi\"\njkl");
      assertEquals(2, data.length);
      assertEquals(2, data[0].length);
      assertEquals(1, data[1].length);
      assertEquals("abc", data[0][0]);
      assertEquals("def\nghi", data[0][1]);
      assertEquals("jkl", data[1][0]);
    }

    public void testParse4() throws IOException {
      String[][] data = CSVParser.parse("abc,\"def\\\\nghi\"\njkl");
      assertEquals(2, data.length);
      assertEquals(2, data[0].length);
      assertEquals(1, data[1].length);
      assertEquals("abc", data[0][0]);
      assertEquals("def\\nghi", data[0][1]);
      assertEquals("jkl", data[1][0]);
    }

    public void testParse5() throws IOException {
      String[][] data = CSVParser.parse("abc,def\\nghi\njkl");
      assertEquals(2, data.length);
      assertEquals(2, data[0].length);
      assertEquals(1, data[1].length);
      assertEquals("abc", data[0][0]);
      assertEquals("def\\nghi", data[0][1]);
      assertEquals("jkl", data[1][0]);
    }
    
    public void testParse6() throws IOException {
      String[][] data = CSVParser.parse("");
      assertEquals(1, data.length);
      assertEquals(1, data[0].length);
      assertEquals("", data[0][0]);  
    }
    
    public void testParse7() throws IOException {
      boolean io = false;
      try {
        CSVParser.parse(null);
      } catch (IllegalArgumentException e) {
        io = true;
      }
      assertTrue(io);
    }
    
    public void testParseLine1() throws IOException {
      String[] data = CSVParser.parseLine("abc,def,ghi");
      assertEquals(3, data.length);
      assertEquals("abc", data[0]);
      assertEquals("def", data[1]);
      assertEquals("ghi", data[2]);
    }

    public void testParseLine2() throws IOException {
      String[] data = CSVParser.parseLine("abc,def,ghi\n");
      assertEquals(3, data.length);
      assertEquals("abc", data[0]);
      assertEquals("def", data[1]);
      assertEquals("ghi", data[2]);
    }

    public void testParseLine3() throws IOException {
      String[] data = CSVParser.parseLine("abc,\"def,ghi\"");
      assertEquals(2, data.length);
      assertEquals("abc", data[0]);
      assertEquals("def,ghi", data[1]);
    }

    public void testParseLine4() throws IOException {
      String[] data = CSVParser.parseLine("abc,\"def\nghi\"");
      assertEquals(2, data.length);
      assertEquals("abc", data[0]);
      assertEquals("def\nghi", data[1]);
    }
    
    public void testParseLine5() throws IOException {
      String[] data = CSVParser.parseLine("");
      assertEquals(0, data.length);
      // assertEquals("", data[0]);
    }
    
    public void testParseLine6() throws IOException {
      boolean io = false;
      try {
        CSVParser.parseLine(null);
      } catch (IllegalArgumentException e) {
        io = true;
      }
      assertTrue(io);
    }
    
    public void testParseLine7() throws IOException {
      String[] res = CSVParser.parseLine("");
      assertNotNull(res);
      assertEquals(0, res.length);  
    }
      
    public void testUnicodeEscape() throws IOException {
      String code = "abc,\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063";
      TestCSVParser parser = new TestCSVParser(new StringReader(code));
      System.out.println("---------\n" + code + "\n-------------");
      parser.setUnicodeEscapeInterpretation(true);
      String[] data = parser.getLine();
      assertEquals(2, data.length);
      assertEquals("abc", data[0]);
      assertEquals("public", data[1]);
    }
    
    public void testCarriageReturnLineFeedEndings() throws IOException {
     String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
     TestCSVParser parser = new TestCSVParser(new StringReader(code));
     System.out.println("---------\n" + code + "\n-------------");
     String[][] data = parser.getAllValues();
     assertEquals(4, data.length);
    }
    
    public void testIgnoreEmptyLines() throws IOException {
      String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
      //String code = "world\r\n\n";
      //String code = "foo;baar\r\n\r\nhello;\r\n\r\nworld;\r\n";
      TestCSVParser parser = new TestCSVParser(new StringReader(code));
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
      TestCSVParser parser = new TestCSVParser(new StringReader(code));
      System.out.println("---------\n" + code + "\n-------------");
      String[][] data = parser.getAllValues();
      parser = new TestCSVParser(new StringReader(code));
      TestCSVParser parser1 = new TestCSVParser(new StringReader(code));
      for (int i = 0; i < data.length; i++) {
        assertTrue(Arrays.equals(parser1.getLine(), data[i]));
        for (int j = 0; j < data[i].length; j++) {
          assertEquals(parser.nextValue(), data[i][j]);
        }
      }
    }
}
