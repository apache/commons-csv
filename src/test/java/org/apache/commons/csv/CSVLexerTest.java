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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.apache.commons.csv.TokenMatchers.hasContent;
import static org.apache.commons.csv.TokenMatchers.matches;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 * @version $Id$
 */
public class CSVLexerTest {

    private CSVFormat formatWithEscaping;

    @Before
    public void setUp() {
        formatWithEscaping = CSVFormat.newBuilder().withEscape('\\').build();
    }

    private Lexer getLexer(final String input, final CSVFormat format) {
        return new CSVLexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    @Test
    public void testIgnoreSurroundingSpacesAreDeleted() throws IOException {
        final String code = "noSpaces,  leadingSpaces,trailingSpaces  ,  surroundingSpaces  ,  ,,";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "noSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "leadingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "trailingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "surroundingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

    @Test
    public void testIgnoreEmptyLines() throws IOException {
        final String code =
                "first,line,\n"+
                "\n"+
                "\n"+
                "second,line\n"+
                "\n"+
                "\n"+
                "third line \n"+
                "\n"+
                "\n"+
                "last, line \n"+
                "\n"+
                "\n"+
                "\n";
        final CSVFormat format = CSVFormat.newBuilder().withIgnoreEmptyLines(true).build();
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "first"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "second"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "third line "));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "last"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, " line "));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

    @Test
    public void testComments() throws IOException {
        final String code =
                "first,line,\n"+
                "second,line,tokenWith#no-comment\n"+
                "# comment line \n"+
                "third,line,#no-comment\n"+
                "# penultimate comment\n"+
                "# Final comment\n";
        final CSVFormat format = CSVFormat.newBuilder().withCommentStart('#').build();
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "first"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "second"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "tokenWith#no-comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "comment line"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "third"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "#no-comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "penultimate comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "Final comment"));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
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


        assertThat(parser.nextToken(new Token()), matches(TOKEN, "1"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "2"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "3"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 1
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 1b
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 1c
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "b x"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "c#no-comment")); // 2
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "foo"));           // 3
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 4
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 4b
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "d"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "e"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "#no-comment"));  // 5
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 5b
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 5c
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "penultimate comment"));              // 6
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 6b
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             // 6c
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "Final comment"));              // 7
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));

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

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        // an unquoted single backslash is not an escape char
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b\\"));
        // an unquoted single backslash is not an escape char
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

    // simple token with escaping enabled
    @Test
    public void testNextToken3Escaping() throws IOException {
        /* file: a,\,,b
        *       \,,
        */
        final String code = "a,\\,,b\\\\\n\\,,\\\nc,d\\\r\ne";
        final CSVFormat format = formatWithEscaping.toBuilder().withIgnoreEmptyLines(false).build();
        assertTrue(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ","));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ","));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\nc"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "d\r"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "e"));
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
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, " foo"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo "));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, " foo "));
//      assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertThat(parser.nextToken(new Token()), matches(EOF, "b"));
    }

    // encapsulator tokenizer (multi line, delimiter in string)
    @Test
    public void testNextToken5() throws IOException {
        final String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        final Lexer parser = getLexer(code, CSVFormat.DEFAULT);
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo\n"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "foo\n  baar ,,,"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "\n\t \n"));

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
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b and ' more\n"));
    }

    // From CSV-1
    @Test
    public void testDelimiterIsWhitespace() throws IOException {
        final String code = "one\ttwo\t\tfour \t five\t six";
        final Lexer parser = getLexer(code, CSVFormat.TDF);
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "one"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "two"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "four"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "five"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "six"));
    }

    @Test
    public void testEscapedCR() throws Exception {
        final Lexer lexer = getLexer("character\\" + CR + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + CR + "Escaped"));
    }

    @Test
    public void testEscapedLF() throws Exception {
        final Lexer lexer = getLexer("character\\" + LF + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + LF + "Escaped"));
    }

    @Test
    public void testEscapedTab() throws Exception {
        final Lexer lexer = getLexer("character\\" + TAB + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + TAB + "Escaped"));
    }

    @Test
    public void testEscapeBackspace() throws Exception {
        final Lexer lexer = getLexer("character\\" + BACKSPACE + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + BACKSPACE + "Escaped"));
    }

    @Test
    public void testEscapeFF() throws Exception {
        final Lexer lexer = getLexer("character\\" + FF + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + FF + "Escaped"));
    }

    @Test
    public void testEscapedMySqlNullValue() throws Exception {
        // MySQL uses \N to symbolize null values. We have to restore this
        final Lexer lexer = getLexer("character\\\\NEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\NEscaped"));
    }

    // FIXME this should work after CSV-58 is resolved. Currently the result will be "characteraEscaped"
    @Test
    @Ignore
    public void testEscapedCharacter() throws Exception {
        final Lexer lexer = getLexer("character\\aEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\aEscaped"));
    }

    // FIXME this should work after CSV-58 is resolved. Currently the result will be "characterCREscaped"
    @Test
    @Ignore
    public void testEscapedControlCharacter() throws Exception {
        // we are explicitly using an escape different from \ here, because \r is the character sequence for CR
        final Lexer lexer = getLexer("character!rEscaped", CSVFormat.newBuilder().withEscape('!').build());
        assertThat(lexer.nextToken(new Token()), hasContent("character!rEscaped"));
    }

    @Test
    public void testEscapedControlCharacter2() throws Exception {
        final Lexer lexer = getLexer("character\\rEscaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertThat(lexer.nextToken(new Token()), hasContent("character" + CR + "Escaped"));
    }

    @Test(expected = IOException.class)
    public void testEscapingAtEOF() throws Exception {
        final String code = "escaping at EOF is evil\\";
        final Lexer lexer = getLexer(code, formatWithEscaping);

        lexer.nextToken(new Token());
    }
}
