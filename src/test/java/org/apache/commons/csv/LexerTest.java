/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 */
class LexerTest {

    private static void assertContent(final String expectedContent, final Token actualToken) {
        assertEquals(expectedContent, actualToken.content.toString());
    }

    private static void assertNextToken(final String expectedContent, final Lexer lexer) throws IOException {
        assertContent(expectedContent, lexer.nextToken(new Token()));
    }

    private static void assertNextToken(final Token.Type expectedType, final String expectedContent, final Lexer lexer) throws IOException {
        final Token actualToken = lexer.nextToken(new Token());
        assertEquals(expectedType, actualToken.type);
        assertContent(expectedContent, actualToken);
    }

    private CSVFormat formatWithEscaping;

    @SuppressWarnings("resource")
    private Lexer createLexer(final String input, final CSVFormat format) {
        return new Lexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    @BeforeEach
    public void setUp() {
        formatWithEscaping = CSVFormat.DEFAULT.withEscape('\\');
    }

    // simple token with escaping enabled
    @Test
    void testBackslashWithEscaping() throws IOException {
        /*
         * file: a,\,,b \,,
         */
        final String code = "a,\\,,b\\\\\n\\,,\\\nc,d\\\r\ne";
        final CSVFormat format = formatWithEscaping.withIgnoreEmptyLines(false);
        assertTrue(format.isEscapeCharacterSet());
        try (Lexer lexer = createLexer(code, format)) {
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, ",", lexer);
            assertNextToken(EORECORD, "b\\", lexer);
            assertNextToken(TOKEN, ",", lexer);
            assertNextToken(TOKEN, "\nc", lexer);
            assertNextToken(EORECORD, "d\r", lexer);
            assertNextToken(EOF, "e", lexer);
        }
    }

    // simple token with escaping not enabled
    @Test
    void testBackslashWithoutEscaping() throws IOException {
        /*
         * file: a,\,,b \,,
         */
        final String code = "a,\\,,b\\\n\\,,";
        final CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isEscapeCharacterSet());
        try (Lexer lexer = createLexer(code, format)) {
            // parser.nextToken(new Token())
            assertNextToken(TOKEN, "a", lexer);
            // an unquoted single backslash is not an escape char
            assertNextToken(TOKEN, "\\", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(EORECORD, "b\\", lexer);
            // an unquoted single backslash is not an escape char
            assertNextToken(TOKEN, "\\", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testBackspace() throws Exception {
        try (Lexer lexer = createLexer("character" + BACKSPACE + "NotEscaped", formatWithEscaping)) {
            assertNextToken("character" + BACKSPACE + "NotEscaped", lexer);
        }
    }

    @Test
    void testComments() throws IOException {
        final String code = "first,line,\n" + "second,line,tokenWith#no-comment\n" + "# comment line \n" +
                "third,line,#no-comment\n" + "# penultimate comment\n" + "# Final comment\n";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (Lexer lexer = createLexer(code, format)) {
            assertNextToken(TOKEN, "first", lexer);
            assertNextToken(TOKEN, "line", lexer);
            assertNextToken(EORECORD, "", lexer);
            assertNextToken(TOKEN, "second", lexer);
            assertNextToken(TOKEN, "line", lexer);
            assertNextToken(EORECORD, "tokenWith#no-comment", lexer);
            assertNextToken(COMMENT, "comment line", lexer);
            assertNextToken(TOKEN, "third", lexer);
            assertNextToken(TOKEN, "line", lexer);
            assertNextToken(EORECORD, "#no-comment", lexer);
            assertNextToken(COMMENT, "penultimate comment", lexer);
            assertNextToken(COMMENT, "Final comment", lexer);
            assertNextToken(EOF, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testCommentsAndEmptyLines() throws IOException {
        final String code = "1,2,3,\n" + // 1
                "\n" + // 1b
                "\n" + // 1c
                "a,b x,c#no-comment\n" + // 2
                "#foo\n" + // 3
                "\n" + // 4
                "\n" + // 4b
                "d,e,#no-comment\n" + // 5
                "\n" + // 5b
                "\n" + // 5c
                "# penultimate comment\n" + // 6
                "\n" + // 6b
                "\n" + // 6c
                "# Final comment\n"; // 7
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines(), "Should not ignore empty lines");

        try (Lexer lexer = createLexer(code, format)) {
            assertNextToken(TOKEN, "1", lexer);
            assertNextToken(TOKEN, "2", lexer);
            assertNextToken(TOKEN, "3", lexer);
            assertNextToken(EORECORD, "", lexer); // 1
            assertNextToken(EORECORD, "", lexer); // 1b
            assertNextToken(EORECORD, "", lexer); // 1c
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, "b x", lexer);
            assertNextToken(EORECORD, "c#no-comment", lexer); // 2
            assertNextToken(COMMENT, "foo", lexer); // 3
            assertNextToken(EORECORD, "", lexer); // 4
            assertNextToken(EORECORD, "", lexer); // 4b
            assertNextToken(TOKEN, "d", lexer);
            assertNextToken(TOKEN, "e", lexer);
            assertNextToken(EORECORD, "#no-comment", lexer); // 5
            assertNextToken(EORECORD, "", lexer); // 5b
            assertNextToken(EORECORD, "", lexer); // 5c
            assertNextToken(COMMENT, "penultimate comment", lexer); // 6
            assertNextToken(EORECORD, "", lexer); // 6b
            assertNextToken(EORECORD, "", lexer); // 6c
            assertNextToken(COMMENT, "Final comment", lexer); // 7
            assertNextToken(EOF, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testCR() throws Exception {
        try (Lexer lexer = createLexer("character" + CR + "NotEscaped", formatWithEscaping)) {
            assertNextToken("character", lexer);
            assertNextToken("NotEscaped", lexer);
        }
    }

    // From CSV-1
    @Test
    void testDelimiterIsWhitespace() throws IOException {
        final String code = "one\ttwo\t\tfour \t five\t six";
        try (Lexer lexer = createLexer(code, CSVFormat.TDF)) {
            assertNextToken(TOKEN, "one", lexer);
            assertNextToken(TOKEN, "two", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(TOKEN, "four", lexer);
            assertNextToken(TOKEN, "five", lexer);
            assertNextToken(EOF, "six", lexer);
        }
    }

    @Test
    void testEOFWithoutClosingQuote() throws Exception {
        final String code = "a,\"b";
        try (Lexer lexer = createLexer(code, CSVFormat.Builder.create().setLenientEof(true).get())) {
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(EOF, "b", lexer);
        }
        try (Lexer lexer = createLexer(code, CSVFormat.Builder.create().setLenientEof(false).get())) {
            assertNextToken(TOKEN, "a", lexer);
            assertThrows(IOException.class, () -> lexer.nextToken(new Token()));
        }
    }

    @Test // TODO is this correct? Do we expect <esc>BACKSPACE to be unescaped?
    void testEscapedBackspace() throws Exception {
        try (Lexer lexer = createLexer("character\\" + BACKSPACE + "Escaped", formatWithEscaping)) {
            assertNextToken("character" + BACKSPACE + "Escaped", lexer);
        }
    }

    @Test
    void testEscapedCharacter() throws Exception {
        try (Lexer lexer = createLexer("character\\aEscaped", formatWithEscaping)) {
            assertNextToken("character\\aEscaped", lexer);
        }
    }

    @Test
    void testEscapedControlCharacter() throws Exception {
        // we are explicitly using an escape different from \ here
        try (Lexer lexer = createLexer("character!rEscaped", CSVFormat.DEFAULT.withEscape('!'))) {
            assertNextToken("character" + CR + "Escaped", lexer);
        }
    }

    @Test
    void testEscapedControlCharacter2() throws Exception {
        try (Lexer lexer = createLexer("character\\rEscaped", CSVFormat.DEFAULT.withEscape('\\'))) {
            assertNextToken("character" + CR + "Escaped", lexer);
        }
    }

    @Test
    void testEscapedCR() throws Exception {
        try (Lexer lexer = createLexer("character\\" + CR + "Escaped", formatWithEscaping)) {
            assertNextToken("character" + CR + "Escaped", lexer);
        }
    }

    @Test // TODO is this correct? Do we expect <esc>FF to be unescaped?
    void testEscapedFF() throws Exception {
        try (Lexer lexer = createLexer("character\\" + FF + "Escaped", formatWithEscaping)) {
            assertNextToken("character" + FF + "Escaped", lexer);
        }
    }

    @Test
    void testEscapedLF() throws Exception {
        try (Lexer lexer = createLexer("character\\" + LF + "Escaped", formatWithEscaping)) {
            assertNextToken("character" + LF + "Escaped", lexer);
        }
    }

    @Test
    void testEscapedMySqlNullValue() throws Exception {
        // MySQL uses \N to symbolize null values. We have to restore this
        try (Lexer lexer = createLexer("character\\NEscaped", formatWithEscaping)) {
            assertNextToken("character\\NEscaped", lexer);
        }
    }

    @Test // TODO is this correct? Do we expect <esc>TAB to be unescaped?
    void testEscapedTab() throws Exception {
        try (Lexer lexer = createLexer("character\\" + TAB + "Escaped", formatWithEscaping)) {
            assertNextToken("character" + TAB + "Escaped", lexer);
        }

    }

    @Test
    void testEscapingAtEOF() throws Exception {
        final String code = "escaping at EOF is evil\\";
        try (Lexer lexer = createLexer(code, formatWithEscaping)) {
            assertThrows(IOException.class, () -> lexer.nextToken(new Token()));
        }
    }

    @Test
    void testFF() throws Exception {
        try (Lexer lexer = createLexer("character" + FF + "NotEscaped", formatWithEscaping)) {
            assertNextToken("character" + FF + "NotEscaped", lexer);
        }
    }

    @Test
    void testIgnoreEmptyLines() throws IOException {
        final String code = "first,line,\n" + "\n" + "\n" + "second,line\n" + "\n" + "\n" + "third line \n" + "\n" +
                "\n" + "last, line \n" + "\n" + "\n" + "\n";
        final CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        try (Lexer lexer = createLexer(code, format)) {
            assertNextToken(TOKEN, "first", lexer);
            assertNextToken(TOKEN, "line", lexer);
            assertNextToken(EORECORD, "", lexer);
            assertNextToken(TOKEN, "second", lexer);
            assertNextToken(EORECORD, "line", lexer);
            assertNextToken(EORECORD, "third line ", lexer);
            assertNextToken(TOKEN, "last", lexer);
            assertNextToken(EORECORD, " line ", lexer);
            assertNextToken(EOF, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testIsMetaCharCommentStart() throws IOException {
        try (Lexer lexer = createLexer("#", CSVFormat.DEFAULT.withCommentMarker('#'))) {
            final int ch = lexer.readEscape();
            assertEquals('#', ch);
        }
    }

    @Test
    void testLF() throws Exception {
        try (Lexer lexer = createLexer("character" + LF + "NotEscaped", formatWithEscaping)) {
            assertNextToken("character", lexer);
            assertNextToken("NotEscaped", lexer);
        }
    }

    // encapsulator tokenizer (single line)
    @Test
    void testNextToken4() throws IOException {
        /*
         * file: a,"foo",b a, " foo",b a,"foo " ,b // whitespace after closing encapsulator a, " foo " ,b
         */
        final String code = "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
        try (Lexer lexer = createLexer(code, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, "foo", lexer);
            assertNextToken(EORECORD, "b", lexer);
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, " foo", lexer);
            assertNextToken(EORECORD, "b", lexer);
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, "foo ", lexer);
            assertNextToken(EORECORD, "b", lexer);
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, " foo ", lexer);
            // assertTokenEquals(EORECORD, "b", parser);
            assertNextToken(EOF, "b", lexer);
        }
    }

    // encapsulator tokenizer (multi line, delimiter in string)
    @Test
    void testNextToken5() throws IOException {
        final String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        try (Lexer lexer = createLexer(code, CSVFormat.DEFAULT)) {
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(TOKEN, "foo\n", lexer);
            assertNextToken(EORECORD, "b", lexer);
            assertNextToken(EORECORD, "foo\n  baar ,,,", lexer);
            assertNextToken(EOF, "\n\t \n", lexer);
        }
    }

    // change delimiters, comment, encapsulater
    @Test
    void testNextToken6() throws IOException {
        /*
         * file: a;'b and \' more ' !comment;;;; ;;
         */
        final String code = "a;'b and '' more\n'\n!comment;;;;\n;;";
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'').withCommentMarker('!').withDelimiter(';');
        try (Lexer lexer = createLexer(code, format)) {
            assertNextToken(TOKEN, "a", lexer);
            assertNextToken(EORECORD, "b and ' more\n", lexer);
        }
    }

    @Test
    void testReadEscapeBackspace() throws IOException {
        try (Lexer lexer = createLexer("b", CSVFormat.DEFAULT.withEscape('\b'))) {
            final int ch = lexer.readEscape();
            assertEquals(BACKSPACE, ch);
        }
    }

    @Test
    void testReadEscapeFF() throws IOException {
        try (Lexer lexer = createLexer("f", CSVFormat.DEFAULT.withEscape('\f'))) {
            final int ch = lexer.readEscape();
            assertEquals(FF, ch);
        }
    }

    @Test
    void testReadEscapeTab() throws IOException {
        try (Lexer lexer = createLexer("t", CSVFormat.DEFAULT.withEscape('\t'))) {
            final int ch = lexer.readEscape();
            assertNextToken(EOF, "", lexer);
            assertEquals(TAB, ch);
        }
    }

    @Test
    void testSurroundingSpacesAreDeleted() throws IOException {
        final String code = "noSpaces,  leadingSpaces,trailingSpaces  ,  surroundingSpaces  ,  ,,";
        try (Lexer lexer = createLexer(code, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            assertNextToken(TOKEN, "noSpaces", lexer);
            assertNextToken(TOKEN, "leadingSpaces", lexer);
            assertNextToken(TOKEN, "trailingSpaces", lexer);
            assertNextToken(TOKEN, "surroundingSpaces", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testSurroundingTabsAreDeleted() throws IOException {
        final String code = "noTabs,\tleadingTab,trailingTab\t,\tsurroundingTabs\t,\t\t,,";
        try (Lexer lexer = createLexer(code, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            assertNextToken(TOKEN, "noTabs", lexer);
            assertNextToken(TOKEN, "leadingTab", lexer);
            assertNextToken(TOKEN, "trailingTab", lexer);
            assertNextToken(TOKEN, "surroundingTabs", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(TOKEN, "", lexer);
            assertNextToken(EOF, "", lexer);
        }
    }

    @Test
    void testTab() throws Exception {
        try (Lexer lexer = createLexer("character" + TAB + "NotEscaped", formatWithEscaping)) {
            assertNextToken("character" + TAB + "NotEscaped", lexer);
        }
    }

    @Test
    void testTrailingTextAfterQuote() throws Exception {
        final String code = "\"a\" b,\"a\" \" b,\"a\" b \"\"";
        try (Lexer lexer = createLexer(code, CSVFormat.Builder.create().setTrailingData(true).get())) {
            assertNextToken(TOKEN, "a b", lexer);
            assertNextToken(TOKEN, "a \" b", lexer);
            assertNextToken(EOF, "a b \"\"", lexer);
        }
        try (Lexer parser = createLexer(code, CSVFormat.Builder.create().setTrailingData(false).get())) {
            assertThrows(IOException.class, () -> parser.nextToken(new Token()));
        }
    }

    @Test
    void testTrimTrailingSpacesZeroLength() throws Exception {
        final StringBuilder buffer = new StringBuilder("");
        try (Lexer lexer = createLexer(buffer.toString(), CSVFormat.DEFAULT)) {
            lexer.trimTrailingSpaces(buffer);
            assertNextToken(EOF, "", lexer);
        }
    }
}
