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
import java.io.StringReader;

import org.junit.Test;

import static org.apache.commons.csv.Token.Type.*;
import static org.junit.Assert.*;

public class CSVLexerTest {
    
    private Lexer getLexer(String input, CSVFormat format) {
        return new CSVLexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    private void assertTokenEquals(Token.Type expectedType, String expectedContent, Token token) {
        assertEquals("Token type", expectedType, token.type);
        assertEquals("Token content", expectedContent, token.content.toString());
    }
    
    // Single line (without comment)
    @Test
    public void testNextToken1() throws IOException {
        String code = "abc,def, hijk,  lmnop,   qrst,uv ,wxy   ,z , ,";
        Lexer parser = getLexer(code, CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        assertTokenEquals(TOKEN, "abc", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "def", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "hijk", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "lmnop", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "qrst", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "uv", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "wxy", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "z", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
    }

    // multiline including comments (and empty lines)
    @Test
    public void testNextToken2() throws IOException {
        final String code = 
                "1,2,3,\n"+                // 1
                "a,b x,c#no-comment\n"+    // 2
                "#foo\n"+                  // 3
                "\n"+                      // 4
                "d,e,#no-comment\n"+       // 5
                "# penultimate comment\n"+ // 6
                "# Final comment\n";       // 7
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        assertTrue("Should ignore empty lines", format.isEmptyLinesIgnored());
        
        Lexer parser = getLexer(code, format);


        assertTokenEquals(TOKEN, "1", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "2", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "3", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b x", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "c#no-comment", parser.nextToken(new Token())); // 2
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 3
        // 4 empty line, ignored                                                    // 4
        assertTokenEquals(TOKEN, "d", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "e", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "#no-comment", parser.nextToken(new Token()));  // 5
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 6
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 7
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));

    }

    // multiline including comments (and empty lines)
    @Test
    public void testNextToken2EmptyLines() throws IOException {
        final String code = 
                "1,2,3,\n"+                // 1
                "a,b x,c#no-comment\n"+    // 2
                "#foo\n"+                  // 3
                "\n"+                      // 4
                "d,e,#no-comment\n"+       // 5
                "# penultimate comment\n"+ // 6
                "# Final comment\n";       // 7
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#').withEmptyLinesIgnored(false);
        assertFalse("Should not ignore empty lines", format.isEmptyLinesIgnored());
        
        Lexer parser = getLexer(code, format);


        assertTokenEquals(TOKEN, "1", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "2", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "3", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b x", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "c#no-comment", parser.nextToken(new Token())); // 2
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 3
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 4
        assertTokenEquals(TOKEN, "d", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "e", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "#no-comment", parser.nextToken(new Token()));  // 5
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 6
        assertTokenEquals(COMMENT, "", parser.nextToken(new Token()));              // 7
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));

    }

    // simple token with escaping
    @Test
    public void testNextToken3() throws IOException {
        /* file: a,\,,b
        *       \,,
        */
        String code = "a,\\,,b\n\\,,";
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        Lexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        // an unquoted single backslash is not an escape char
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        // an unquoted single backslash is not an escape char
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
    }

    // encapsulator tokenizer (sinle line)
    @Test
    public void testNextToken4() throws IOException {
        /* file:  a,"foo",b
        *        a,   " foo",b
        *        a,"foo "   ,b     // whitespace after closing encapsulator
        *        a,  " foo " ,b
        */
        String code = "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
        Lexer parser = getLexer(code, CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, " foo", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo ", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, " foo ", parser.nextToken(new Token()));
//      assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "b", parser.nextToken(new Token()));
    }

    // encapsulator tokenizer (multi line, delimiter in string)
    @Test
    public void testNextToken5() throws IOException {
        String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        Lexer parser = getLexer(code, CSVFormat.DEFAULT);
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo\n", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "foo\n  baar ,,,", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "\n\t \n", parser.nextToken(new Token()));

    }

    // change delimiters, comment, encapsulater
    @Test
    public void testNextToken6() throws IOException {
        /* file: a;'b and \' more
        *       '
        *       !comment;;;;
        *       ;;
        */
        String code = "a;'b and '' more\n'\n!comment;;;;\n;;";
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withEncapsulator('\'').withCommentStart('!');
        Lexer parser = getLexer(code, format);
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b and ' more\n", parser.nextToken(new Token()));
    }

    // From CSV-1
    @Test
    public void testDelimiterIsWhitespace() throws IOException {
        String code = "one\ttwo\t\tfour \t five\t six";
        Lexer parser = getLexer(code, CSVFormat.TDF);
        assertTokenEquals(TOKEN, "one", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "two", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "four", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "five", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "six", parser.nextToken(new Token()));
    }
}
