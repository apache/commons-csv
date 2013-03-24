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

import static org.apache.commons.csv.Constants.BACKSPACE;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.FF;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;
import static org.apache.commons.csv.Token.Type.COMMENT;
import static org.apache.commons.csv.Token.Type.EOF;
import static org.apache.commons.csv.Token.Type.EORECORD;
import static org.apache.commons.csv.Token.Type.TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 * @version $Id$
 */
public class CSVLexerTest {

    private Lexer getLexer(final String input, final CSVFormat format) {
        return new CSVLexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    private void assertTokenEquals(final Token.Type expectedType, final String expectedContent, final Token token) {
        assertEquals("Token type", expectedType, token.type);
        assertEquals("Token content", expectedContent, token.content.toString());
    }

    // Single line (without comment)
    @Test
    public void testNextToken1() throws IOException {
        final String code = "abc,def, hijk,  lmnop,   qrst,uv ,wxy   ,z , ,";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
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
                "\n"+
                "\n"+
                "a,b x,c#no-comment\n"+    // 2
                "\n"+
                "\n"+
                "# foo \n"+                // 3
                "\n"+                      // 4
                "d,e,#no-comment\n"+       // 5
                "\n"+
                "\n"+
                "# penultimate comment\n"+ // 6
                "\n"+
                "\n"+
                "# Final comment\n";       // 7
        final CSVFormat format = CSVFormat.newBuilder().withCommentStart('#').build();
        assertTrue("Should ignore empty lines", format.getIgnoreEmptyLines());

        final Lexer parser = getLexer(code, format);


        assertTokenEquals(TOKEN, "1", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "2", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "3", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b x", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "c#no-comment", parser.nextToken(new Token())); // 2
        assertTokenEquals(COMMENT, "foo", parser.nextToken(new Token()));              // 3
        // 4 empty line, ignored                                                    // 4
        assertTokenEquals(TOKEN, "d", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "e", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "#no-comment", parser.nextToken(new Token()));  // 5
        assertTokenEquals(COMMENT, "penultimate comment", parser.nextToken(new Token()));              // 6
        assertTokenEquals(COMMENT, "Final comment", parser.nextToken(new Token()));              // 7
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));

    }

    // multiline including comments (and empty lines)
    @Test
    public void testNextToken2EmptyLines() throws IOException {
        final String code =
                "1,2,3,\n"+                // 1
                "\n"+                      // 1b
                "\n"+                      // 1c
                "a,b x,c#no-comment\n"+    // 2
                "#foo\n"+                  // 3
                "\n"+                      // 4
                "\n"+                      // 4b
                "d,e,#no-comment\n"+       // 5
                "\n"+                      // 5b
                "\n"+                      // 5c
                "# penultimate comment\n"+ // 6
                "\n"+                      // 6b
                "\n"+                      // 6c
                "# Final comment\n";       // 7
        final CSVFormat format = CSVFormat.newBuilder().withCommentStart('#').withIgnoreEmptyLines(false).build();
        assertFalse("Should not ignore empty lines", format.getIgnoreEmptyLines());

        final Lexer parser = getLexer(code, format);


        assertTokenEquals(TOKEN, "1", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "2", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "3", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1b
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 1c
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b x", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "c#no-comment", parser.nextToken(new Token())); // 2
        assertTokenEquals(COMMENT, "foo", parser.nextToken(new Token()));           // 3
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 4
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 4b
        assertTokenEquals(TOKEN, "d", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "e", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "#no-comment", parser.nextToken(new Token()));  // 5
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 5b
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 5c
        assertTokenEquals(COMMENT, "penultimate comment", parser.nextToken(new Token()));              // 6
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 6b
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));             // 6c
        assertTokenEquals(COMMENT, "Final comment", parser.nextToken(new Token()));              // 7
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));

    }

    // simple token with escaping not enabled
    @Test
    public void testNextToken3() throws IOException {
        /* file: a,\,,b
        *       \,,
        */
        final String code = "a,\\,,b\\\n\\,,";
        final CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        // an unquoted single backslash is not an escape char
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b\\", parser.nextToken(new Token()));
        // an unquoted single backslash is not an escape char
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
    }

    // simple token with escaping enabled
    @Test
    public void testNextToken3Escaping() throws IOException {
        /* file: a,\,,b
        *       \,,
        */
        final String code = "a,\\,,b\\\\\n\\,,\\\nc,d\\\r\ne";
        final CSVFormat format = CSVFormat.newBuilder().withEscape('\\').withIgnoreEmptyLines(false).build();
        assertTrue(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, ",", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, ",", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "\nc", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "d\r", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "e", parser.nextToken(new Token()));
    }

    // simple token with escaping enabled
    @Test
    public void testNextToken3BadEscaping() throws IOException {
        final String code = "a,b,c\\";
        final CSVFormat format = CSVFormat.newBuilder().withEscape('\\').build();
        assertTrue(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b", parser.nextToken(new Token()));
        try {
            final Token tkn = parser.nextToken(new Token());
            fail("Expected IOE, found "+tkn);
        } catch (final IOException e) {
        }
    }

    // encapsulator tokenizer (single line)
    @Test
    public void testNextToken4() throws IOException {
        /* file:  a,"foo",b
        *        a,   " foo",b
        *        a,"foo "   ,b     // whitespace after closing encapsulator
        *        a,  " foo " ,b
        */
        final String code = "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
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
        final String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        final Lexer parser = getLexer(code, CSVFormat.DEFAULT);
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
        final String code = "a;'b and '' more\n'\n!comment;;;;\n;;";
        final CSVFormat format = CSVFormat.newBuilder().withDelimiter(';').withQuoteChar('\'').withCommentStart('!').build();
        final Lexer parser = getLexer(code, format);
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b and ' more\n", parser.nextToken(new Token()));
    }

    // From CSV-1
    @Test
    public void testDelimiterIsWhitespace() throws IOException {
        final String code = "one\ttwo\t\tfour \t five\t six";
        final Lexer parser = getLexer(code, CSVFormat.TDF);
        assertTokenEquals(TOKEN, "one", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "two", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "four", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "five", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "six", parser.nextToken(new Token()));
    }

    @Test
    public void testEscapedCR() throws Exception {
        final Lexer lexer = getLexer("character\\" + CR + "Escaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character" + CR + "Escaped", lexer.nextToken(new Token()));
    }

    @Test
    public void testEscapedLF() throws Exception {
        final Lexer lexer = getLexer("character\\" + LF + "Escaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character" + LF + "Escaped", lexer.nextToken(new Token()));
    }

    @Test
    public void testEscapedTab() throws Exception {
        final Lexer lexer = getLexer("character\\" + TAB + "Escaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character" + TAB + "Escaped", lexer.nextToken(new Token()));
    }

    @Test
    public void testEscapeBackspace() throws Exception {
        final Lexer lexer = getLexer("character\\" + BACKSPACE + "Escaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character" + BACKSPACE + "Escaped", lexer.nextToken(new Token()));
    }

    @Test
    public void testEscapeFF() throws Exception {
        final Lexer lexer = getLexer("character\\" + FF + "Escaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character" + FF + "Escaped", lexer.nextToken(new Token()));
    }

    @Test
    public void testEscapedMySqlNullValue() throws Exception {
        // MySQL uses \N to symbolize null values. We have to restore this
        final Lexer lexer = getLexer("character\\\\NEscaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character\\NEscaped", lexer.nextToken(new Token()));
    }

    // FIXME this should work after CSV-58 is resolved. Currently the result will be "characteraEscaped"
    @Test
    @Ignore
    public void testEscapedCharacter() throws Exception {
        final Lexer lexer = getLexer("character\\aEscaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertTokenEquals(EOF, "character\\aEscaped", lexer.nextToken(new Token()));
    }

    // FIXME this should work after CSV-58 is resolved. Currentyl the result will be "characterCREscaped"
    @Test
    @Ignore
    public void testEscapedControlCharacter() throws Exception {
        final Lexer lexer = getLexer("character!rEscaped", CSVFormat.newBuilder().withEscape('!').build());
        assertTokenEquals(EOF, "character!rEscaped", lexer.nextToken(new Token()));
    }

    @Test(expected = IOException.class)
    public void testEscapingAtEOF() throws Exception {
        final String code = "escaping at EOF is evil!";
        final Lexer lexer = getLexer(code, CSVFormat.newBuilder().withEscape('!').build());

        lexer.nextToken(new Token());
    }
}
