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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.Closeable;
import java.io.IOException;

/**
 * Lexical analyzer.
 */
final class Lexer implements Closeable {

    private static final String CR_STRING = Character.toString(Constants.CR);
    private static final String LF_STRING = Character.toString(Constants.LF);

    /**
     * Constant char to use for disabling comments, escapes, and encapsulation. The value -2 is used because it
     * won't be confused with an EOF signal (-1), and because the Unicode value {@code FFFE} would be encoded as two
     * chars (using surrogates) and thus there should never be a collision with a real text char.
     */
    private static final char DISABLED = '\ufffe';

    private final char[] delimiter;
    private final char[] delimiterBuf;
    private final char[] escapeDelimiterBuf;
    private final char escape;
    private final char quoteChar;
    private final char commentStart;
    private final boolean ignoreSurroundingSpaces;
    private final boolean ignoreEmptyLines;
    private final boolean lenientEof;
    private final boolean trailingData;

    /** The input stream */
    private final ExtendedBufferedReader reader;
    private String firstEol;

    private boolean isLastTokenDelimiter;

    Lexer(final CSVFormat format, final ExtendedBufferedReader reader) {
        this.reader = reader;
        this.delimiter = format.getDelimiterCharArray();
        this.escape = mapNullToDisabled(format.getEscapeCharacter());
        this.quoteChar = mapNullToDisabled(format.getQuoteCharacter());
        this.commentStart = mapNullToDisabled(format.getCommentMarker());
        this.ignoreSurroundingSpaces = format.getIgnoreSurroundingSpaces();
        this.ignoreEmptyLines = format.getIgnoreEmptyLines();
        this.lenientEof = format.getLenientEof();
        this.trailingData = format.getTrailingData();
        this.delimiterBuf = new char[delimiter.length - 1];
        this.escapeDelimiterBuf = new char[2 * delimiter.length - 1];
    }

    /**
     * Closes resources.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Returns the current character position
     *
     * @return the current character position
     */
    long getCharacterPosition() {
        return reader.getPosition();
    }

    /**
     * Returns the current line number
     *
     * @return the current line number
     */
    long getCurrentLineNumber() {
        return reader.getCurrentLineNumber();
    }

    String getFirstEol() {
        return firstEol;
    }

    boolean isClosed() {
        return reader.isClosed();
    }

    boolean isCommentStart(final int ch) {
        return ch == commentStart;
    }

    /**
     * Determine whether the next characters constitute a delimiter through {@link ExtendedBufferedReader#lookAhead(char[])}.
     *
     * @param ch
     *             the current character.
     * @return true if the next characters constitute a delimiter.
     * @throws IOException If an I/O error occurs.
     */
    boolean isDelimiter(final int ch) throws IOException {
        isLastTokenDelimiter = false;
        if (ch != delimiter[0]) {
            return false;
        }
        if (delimiter.length == 1) {
            isLastTokenDelimiter = true;
            return true;
        }
        reader.lookAhead(delimiterBuf);
        for (int i = 0; i < delimiterBuf.length; i++) {
            if (delimiterBuf[i] != delimiter[i + 1]) {
                return false;
            }
        }
        final int count = reader.read(delimiterBuf, 0, delimiterBuf.length);
        isLastTokenDelimiter = count != EOF;
        return isLastTokenDelimiter;
    }

    /**
     * Tests if the given character indicates the end of the file.
     *
     * @return true if the given character indicates the end of the file.
     */
    boolean isEndOfFile(final int ch) {
        return ch == EOF;
    }

    /**
     * Tests if the given character is the escape character.
     *
     * @return true if the given character is the escape character.
     */
    boolean isEscape(final int ch) {
        return ch == escape;
    }

    /**
     * Tests if the next characters constitute a escape delimiter through {@link ExtendedBufferedReader#lookAhead(char[])}.
     *
     * For example, for delimiter "[|]" and escape '!', return true if the next characters constitute "![!|!]".
     *
     * @return true if the next characters constitute an escape delimiter.
     * @throws IOException If an I/O error occurs.
     */
    boolean isEscapeDelimiter() throws IOException {
        reader.lookAhead(escapeDelimiterBuf);
        if (escapeDelimiterBuf[0] != delimiter[0]) {
            return false;
        }
        for (int i = 1; i < delimiter.length; i++) {
            if (escapeDelimiterBuf[2 * i] != delimiter[i] || escapeDelimiterBuf[2 * i - 1] != escape) {
                return false;
            }
        }
        final int count = reader.read(escapeDelimiterBuf, 0, escapeDelimiterBuf.length);
        return count != EOF;
    }

    private boolean isMetaChar(final int ch) {
        return ch == escape || ch == quoteChar || ch == commentStart;
    }

    boolean isQuoteChar(final int ch) {
        return ch == quoteChar;
    }

    /**
     * Tests if the current character represents the start of a line: a CR, LF, or is at the start of the file.
     *
     * @param ch the character to check
     * @return true if the character is at the start of a line.
     */
    boolean isStartOfLine(final int ch) {
        return ch == Constants.LF || ch == Constants.CR || ch == Constants.UNDEFINED;
    }

    private char mapNullToDisabled(final Character c) {
        return c == null ? DISABLED : c.charValue();
    }

    /**
     * Returns the next token.
     * <p>
     * A token corresponds to a term, a record change or an end-of-file indicator.
     * </p>
     *
     * @param token
     *            an existing Token object to reuse. The caller is responsible for initializing the Token.
     * @return the next token found.
     * @throws IOException on stream access error.
     */
    Token nextToken(final Token token) throws IOException {
        // Get the last read char (required for empty line detection)
        int lastChar = reader.getLastChar();
        // read the next char and set eol
        int c = reader.read();
        // Note: The following call will swallow LF if c == CR. But we don't need to know if the last char was CR or LF - they are equivalent here.
        boolean eol = readEndOfLine(c);
        // empty line detection: eol AND (last char was EOL or beginning)
        if (ignoreEmptyLines) {
            while (eol && isStartOfLine(lastChar)) {
                // Go on char ahead ...
                lastChar = c;
                c = reader.read();
                eol = readEndOfLine(c);
                // reached the end of the file without any content (empty line at the end)
                if (isEndOfFile(c)) {
                    token.type = Token.Type.EOF;
                    // don't set token.isReady here because no content
                    return token;
                }
            }
        }
        // Did we reach EOF during the last iteration already? EOF
        if (isEndOfFile(lastChar) || !isLastTokenDelimiter && isEndOfFile(c)) {
            token.type = Token.Type.EOF;
            // don't set token.isReady here because no content
            return token;
        }
        if (isStartOfLine(lastChar) && isCommentStart(c)) {
            final String line = reader.readLine();
            if (line == null) {
                token.type = Token.Type.EOF;
                // don't set token.isReady here because no content
                return token;
            }
            final String comment = line.trim();
            token.content.append(comment);
            token.type = Token.Type.COMMENT;
            return token;
        }
        // Important: make sure a new char gets consumed in each iteration
        while (token.type == Token.Type.INVALID) {
            // ignore whitespaces at beginning of a token
            if (ignoreSurroundingSpaces) {
                while (Character.isWhitespace((char) c) && !isDelimiter(c) && !eol) {
                    c = reader.read();
                    eol = readEndOfLine(c);
                }
            }
            // ok, start of token reached: encapsulated, or token
            if (isDelimiter(c)) {
                // empty token return TOKEN("")
                token.type = Token.Type.TOKEN;
            } else if (eol) {
                // empty token return EORECORD("")
                // noop: token.content.append("");
                token.type = Token.Type.EORECORD;
            } else if (isQuoteChar(c)) {
                // consume encapsulated token
                parseEncapsulatedToken(token);
            } else if (isEndOfFile(c)) {
                // end of file return EOF()
                // noop: token.content.append("");
                token.type = Token.Type.EOF;
                token.isReady = true; // there is data at EOF
            } else {
                // next token must be a simple token
                // add removed blanks when not ignoring whitespace chars...
                parseSimpleToken(token, c);
            }
        }
        return token;
    }

    /**
     * Parses an encapsulated token.
     * <p>
     * Encapsulated tokens are surrounded by the given encapsulating string. The encapsulator itself might be included
     * in the token using a doubling syntax (as "", '') or using escaping (as in \", \'). Whitespaces before and after
     * an encapsulated token is ignored. The token is finished when one of the following conditions becomes true:
     * </p>
     * <ul>
     * <li>An unescaped encapsulator has been reached and is followed by optional whitespace then:</li>
     * <ul>
     * <li>delimiter (TOKEN)</li>
     * <li>end of line (EORECORD)</li>
     * </ul>
     * <li>end of stream has been reached (EOF)</li> </ul>
     *
     * @param token
     *            the current token
     * @return a valid token object
     * @throws IOException
     *             Thrown when in an invalid state: EOF before closing encapsulator or invalid character before
     *             delimiter or EOL.
     */
    private Token parseEncapsulatedToken(final Token token) throws IOException {
        token.isQuoted = true;
        // Save current line number in case needed for IOE
        final long startLineNumber = getCurrentLineNumber();
        int c;
        while (true) {
            c = reader.read();

            if (isQuoteChar(c)) {
                if (isQuoteChar(reader.lookAhead())) {
                    // double or escaped encapsulator -> add single encapsulator to token
                    c = reader.read();
                    token.content.append((char) c);
                } else {
                    // token finish mark (encapsulator) reached: ignore whitespace till delimiter
                    while (true) {
                        c = reader.read();
                        if (isDelimiter(c)) {
                            token.type = Token.Type.TOKEN;
                            return token;
                        }
                        if (isEndOfFile(c)) {
                            token.type = Token.Type.EOF;
                            token.isReady = true; // There is data at EOF
                            return token;
                        }
                        if (readEndOfLine(c)) {
                            token.type = Token.Type.EORECORD;
                            return token;
                        }
                        if (trailingData) {
                            token.content.append((char) c);
                        } else if (!Character.isWhitespace((char) c)) {
                            // error invalid char between token and next delimiter
                            throw new IOException(String.format("Invalid char between encapsulated token and delimiter at line: %,d, position: %,d",
                                    getCurrentLineNumber(), getCharacterPosition()));
                        }
                    }
                }
            } else if (isEscape(c)) {
                if (isEscapeDelimiter()) {
                    token.content.append(delimiter);
                } else {
                    final int unescaped = readEscape();
                    if (unescaped == EOF) { // unexpected char after escape
                        token.content.append((char) c).append((char) reader.getLastChar());
                    } else {
                        token.content.append((char) unescaped);
                    }
                }
            } else if (isEndOfFile(c)) {
                if (lenientEof) {
                    token.type = Token.Type.EOF;
                    token.isReady = true; // There is data at EOF
                    return token;
                }
                // error condition (end of file before end of token)
                throw new IOException("(startline " + startLineNumber +
                        ") EOF reached before encapsulated token finished");
            } else {
                // consume character
                token.content.append((char) c);
            }
        }
    }

    /**
     * Parses a simple token.
     * <p>
     * Simple tokens are tokens that are not surrounded by encapsulators. A simple token might contain escaped
     * delimiters (as \, or \;). The token is finished when one of the following conditions becomes true:
     * </p>
     * <ul>
     * <li>The end of line has been reached (EORECORD)</li>
     * <li>The end of stream has been reached (EOF)</li>
     * <li>An unescaped delimiter has been reached (TOKEN)</li>
     * </ul>
     *
     * @param token
     *            the current token
     * @param ch
     *            the current character
     * @return the filled token
     * @throws IOException
     *             on stream access error
     */
    private Token parseSimpleToken(final Token token, int ch) throws IOException {
        // Faster to use while(true)+break than while(token.type == INVALID)
        while (true) {
            if (readEndOfLine(ch)) {
                token.type = Token.Type.EORECORD;
                break;
            }
            if (isEndOfFile(ch)) {
                token.type = Token.Type.EOF;
                token.isReady = true; // There is data at EOF
                break;
            }
            if (isDelimiter(ch)) {
                token.type = Token.Type.TOKEN;
                break;
            }
            // continue
            if (isEscape(ch)) {
                if (isEscapeDelimiter()) {
                    token.content.append(delimiter);
                } else {
                    final int unescaped = readEscape();
                    if (unescaped == EOF) { // unexpected char after escape
                        token.content.append((char) ch).append((char) reader.getLastChar());
                    } else {
                        token.content.append((char) unescaped);
                    }
                }
            } else {
                token.content.append((char) ch);
            }
            ch = reader.read(); // continue
        }

        if (ignoreSurroundingSpaces) {
            trimTrailingSpaces(token.content);
        }

        return token;
    }

    /**
     * Greedily accepts \n, \r and \r\n This checker consumes silently the second control-character...
     *
     * @return true if the given or next character is a line-terminator
     */
    boolean readEndOfLine(int ch) throws IOException {
        // check if we have \r\n...
        if (ch == Constants.CR && reader.lookAhead() == Constants.LF) {
            // note: does not change ch outside of this method!
            ch = reader.read();
            // Save the EOL state
            if (firstEol == null) {
                this.firstEol = Constants.CRLF;
            }
        }
        // save EOL state here.
        if (firstEol == null) {
            if (ch == Constants.LF) {
                this.firstEol = LF_STRING;
            } else if (ch == Constants.CR) {
                this.firstEol = CR_STRING;
            }
        }

        return ch == Constants.LF || ch == Constants.CR;
    }

    // TODO escape handling needs more work
    /**
     * Handle an escape sequence.
     * The current character must be the escape character.
     * On return, the next character is available by calling {@link ExtendedBufferedReader#getLastChar()}
     * on the input stream.
     *
     * @return the unescaped character (as an int) or {@link Constants#EOF} if char following the escape is
     *      invalid.
     * @throws IOException if there is a problem reading the stream or the end of stream is detected:
     *      the escape character is not allowed at end of stream
     */
    int readEscape() throws IOException {
        // the escape char has just been read (normally a backslash)
        final int ch = reader.read();
        switch (ch) {
        case 'r':
            return Constants.CR;
        case 'n':
            return Constants.LF;
        case 't':
            return Constants.TAB;
        case 'b':
            return Constants.BACKSPACE;
        case 'f':
            return Constants.FF;
        case Constants.CR:
        case Constants.LF:
        case Constants.FF: // TODO is this correct?
        case Constants.TAB: // TODO is this correct? Do tabs need to be escaped?
        case Constants.BACKSPACE: // TODO is this correct?
            return ch;
        case EOF:
            throw new IOException("EOF whilst processing escape sequence");
        default:
            // Now check for meta-characters
            if (isMetaChar(ch)) {
                return ch;
            }
            // indicate unexpected char - available from in.getLastChar()
            return EOF;
        }
    }

    void trimTrailingSpaces(final StringBuilder buffer) {
        int length = buffer.length();
        while (length > 0 && Character.isWhitespace(buffer.charAt(length - 1))) {
            length--;
        }
        if (length != buffer.length()) {
            buffer.setLength(length);
        }
    }
}
