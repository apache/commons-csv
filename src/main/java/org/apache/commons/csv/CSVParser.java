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
import java.util.ArrayList;
import java.util.List;


/**
 * Parses CSV files according to the specified configuration.
 *
 * Because CSV appears in many different dialects, the parser supports many
 * configuration settings by allowing the specification of a {@link CSVFormat}.
 *
 * <p>Parsing of a csv-string having tabs as separators,
 * '"' as an optional value encapsulator, and comments starting with '#':</p>
 * <pre>
 *  String[][] data =
 *   (new CSVParser(new StringReader("a\tb\nc\td"), new CSVFormat('\t','"','#'))).getAllValues();
 * </pre>
 *
 * <p>Parsing of a csv-string in Excel CSV format</p>
 * <pre>
 *  String[][] data =
 *   (new CSVParser(new StringReader("a;b\nc;d"), CSVFormat.EXCEL)).getAllValues();
 * </pre>
 *
 * <p>
 * Internal parser state is completely covered by the format
 * and the reader-state.</p>
 *
 * <p>see <a href="package-summary.html">package documentation</a>
 * for more details</p>
 */
public class CSVParser {

    /** length of the initial token (content-)buffer */
    private static final int INITIAL_TOKEN_LENGTH = 50;

    // the token types
    /** Token has no valid content, i.e. is in its initialized state. */
    static final int TT_INVALID = -1;
    
    /** Token with content, at beginning or in the middle of a line. */
    static final int TT_TOKEN = 0;
    
    /** Token (which can have content) when end of file is reached. */
    static final int TT_EOF = 1;
    
    /** Token with content when end of a line is reached. */
    static final int TT_EORECORD = 2;

    /** Immutable empty String array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    // the input stream
    private final ExtendedBufferedReader in;

    private final CSVFormat format;

    // the following objects are shared to reduce garbage
    /**
     * A record buffer for getLine(). Grows as necessary and is reused.
     */
    private final List<String> record = new ArrayList<String>();
    private final Token reusableToken = new Token();
    private final CharBuffer wsBuf = new CharBuffer();
    private final CharBuffer code = new CharBuffer(4);


    /**
     * Token is an internal token representation.
     * <p/>
     * It is used as contract between the lexer and the parser.
     */
    static class Token {
        /**
         * Token type, see TT_xxx constants.
         */
        int type = TT_INVALID;
        /**
         * The content buffer.
         */
        CharBuffer content = new CharBuffer(INITIAL_TOKEN_LENGTH);
        /**
         * Token ready flag: indicates a valid token with content (ready for the parser).
         */
        boolean isReady;

        Token reset() {
            content.clear();
            type = TT_INVALID;
            isReady = false;
            return this;
        }
    }

    // ======================================================
    //  the constructor
    // ======================================================

    /**
     * CSV parser using the default {@link CSVFormat}.
     *
     * @param input a Reader containing "csv-formatted" input
     */
    public CSVParser(Reader input) {
        this(input, CSVFormat.DEFAULT);
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input    a Reader containing "csv-formatted" input
     * @param format the CSVFormat used for CSV parsing
     */
    public CSVParser(Reader input, CSVFormat format) {
        this.in = new ExtendedBufferedReader(input);
        this.format = format;
    }

    // ======================================================
    //  the parser
    // ======================================================

    /**
     * Parses the CSV according to the given format
     * and returns the content as an array of records
     * (whereas records are arrays of single values).
     * <p/>
     * The returned content starts at the current parse-position in
     * the stream.
     *
     * @return matrix of records x values ('null' when end of file)
     * @throws IOException on parse error or input read-failure
     */
    public String[][] getAllValues() throws IOException {
        List<String[]> records = new ArrayList<String[]>();
        String[] values;
        String[][] ret = null;
        while ((values = getLine()) != null) {
            records.add(values);
        }
        if (records.size() > 0) {
            ret = new String[records.size()][];
            records.toArray(ret);
        }
        return ret;
    }

    /**
     * Parses from the current point in the stream til
     * the end of the current line.
     *
     * @return array of values til end of line
     *         ('null' when end of file has been reached)
     * @throws IOException on parse error or input read-failure
     */
    public String[] getLine() throws IOException {
        String[] ret = EMPTY_STRING_ARRAY;
        record.clear();
        while (true) {
            reusableToken.reset();
            nextToken(reusableToken);
            switch (reusableToken.type) {
                case TT_TOKEN:
                    record.add(reusableToken.content.toString());
                    break;
                case TT_EORECORD:
                    record.add(reusableToken.content.toString());
                    break;
                case TT_EOF:
                    if (reusableToken.isReady) {
                        record.add(reusableToken.content.toString());
                    } else {
                        ret = null;
                    }
                    break;
                case TT_INVALID:
                default:
                    // error: throw IOException
                    throw new IOException("(line " + getLineNumber() + ") invalid parse sequence");
                    // unreachable: break;
            }
            if (reusableToken.type != TT_TOKEN) {
                break;
            }
        }
        if (!record.isEmpty()) {
            ret = (String[]) record.toArray(new String[record.size()]);
        }
        return ret;
    }

    /**
     * Returns the current line number in the input stream.
     * <p/>
     * ATTENTION: in case your csv has multiline-values the returned
     * number does not correspond to the record-number
     *
     * @return current line number
     */
    public int getLineNumber() {
        return in.getLineNumber();
    }

    // ======================================================
    //  the lexer(s)
    // ======================================================

    /**
     * Convenience method for <code>nextToken(null)</code>.
     */
    Token nextToken() throws IOException {
        return nextToken(new Token());
    }

    /**
     * Returns the next token.
     * <p/>
     * A token corresponds to a term, a record change or an
     * end-of-file indicator.
     *
     * @param tkn an existing Token object to reuse. The caller is responsible to initialize the
     *            Token.
     * @return the next token found
     * @throws IOException on stream access error
     */
    Token nextToken(Token tkn) throws IOException {
        wsBuf.clear(); // reuse

        // get the last read char (required for empty line detection)
        int lastChar = in.readAgain();

        //  read the next char and set eol
        /* note: unfortunately isEndOfLine may consumes a character silently.
        *       this has no effect outside of the method. so a simple workaround
        *       is to call 'readAgain' on the stream...
        *       uh: might using objects instead of base-types (jdk1.5 autoboxing!)
        */
        int c = in.read();
        boolean eol = isEndOfLine(c);
        c = in.readAgain();

        //  empty line detection: eol AND (last char was EOL or beginning)
        while (format.isEmptyLinesIgnored() && eol
                && (lastChar == '\n'
                || lastChar == '\r'
                || lastChar == ExtendedBufferedReader.UNDEFINED)
                && !isEndOfFile(lastChar)) {
            // go on char ahead ...
            lastChar = c;
            c = in.read();
            eol = isEndOfLine(c);
            c = in.readAgain();
            // reached end of file without any content (empty line at the end)
            if (isEndOfFile(c)) {
                tkn.type = TT_EOF;
                return tkn;
            }
        }

        // did we reach eof during the last iteration already ? TT_EOF
        if (isEndOfFile(lastChar) || (lastChar != format.getDelimiter() && isEndOfFile(c))) {
            tkn.type = TT_EOF;
            return tkn;
        }

        //  important: make sure a new char gets consumed in each iteration
        while (!tkn.isReady && tkn.type != TT_EOF) {
            // ignore whitespaces at beginning of a token
            while (format.isLeadingSpacesIgnored() && isWhitespace(c) && !eol) {
                wsBuf.append((char) c);
                c = in.read();
                eol = isEndOfLine(c);
            }
            // ok, start of token reached: comment, encapsulated, or token
            if (c == format.getCommentStart()) {
                // ignore everything till end of line and continue (incr linecount)
                in.readLine();
                tkn = nextToken(tkn.reset());
            } else if (c == format.getDelimiter()) {
                // empty token return TT_TOKEN("")
                tkn.type = TT_TOKEN;
                tkn.isReady = true;
            } else if (eol) {
                // empty token return TT_EORECORD("")
                //noop: tkn.content.append("");
                tkn.type = TT_EORECORD;
                tkn.isReady = true;
            } else if (c == format.getEncapsulator()) {
                // consume encapsulated token
                encapsulatedTokenLexer(tkn, c);
            } else if (isEndOfFile(c)) {
                // end of file return TT_EOF()
                //noop: tkn.content.append("");
                tkn.type = TT_EOF;
                tkn.isReady = true;
            } else {
                // next token must be a simple token
                // add removed blanks when not ignoring whitespace chars...
                if (!format.isLeadingSpacesIgnored()) {
                    tkn.content.append(wsBuf);
                }
                simpleTokenLexer(tkn, c);
            }
        }
        return tkn;
    }

    /**
     * A simple token lexer
     * <p/>
     * Simple token are tokens which are not surrounded by encapsulators.
     * A simple token might contain escaped delimiters (as \, or \;). The
     * token is finished when one of the following conditions become true:
     * <ul>
     * <li>end of line has been reached (TT_EORECORD)</li>
     * <li>end of stream has been reached (TT_EOF)</li>
     * <li>an unescaped delimiter has been reached (TT_TOKEN)</li>
     * </ul>
     *
     * @param tkn the current token
     * @param c   the current character
     * @return the filled token
     * @throws IOException on stream access error
     */
    private Token simpleTokenLexer(Token tkn, int c) throws IOException {
        for (; ;) {
            if (isEndOfLine(c)) {
                // end of record
                tkn.type = TT_EORECORD;
                tkn.isReady = true;
                break;
            } else if (isEndOfFile(c)) {
                // end of file
                tkn.type = TT_EOF;
                tkn.isReady = true;
                break;
            } else if (c == format.getDelimiter()) {
                // end of token
                tkn.type = TT_TOKEN;
                tkn.isReady = true;
                break;
            } else if (c == '\\' && format.isUnicodeEscapesInterpreted() && in.lookAhead() == 'u') {
                // interpret unicode escaped chars (like \u0070 -> p)
                tkn.content.append((char) unicodeEscapeLexer(c));
            } else if (c == format.getEscape()) {
                tkn.content.append((char) readEscape(c));
            } else {
                tkn.content.append((char) c);
            }

            c = in.read();
        }

        if (format.isTrailingSpacesIgnored()) {
            tkn.content.trimTrailingWhitespace();
        }

        return tkn;
    }


    /**
     * An encapsulated token lexer
     * <p/>
     * Encapsulated tokens are surrounded by the given encapsulating-string.
     * The encapsulator itself might be included in the token using a
     * doubling syntax (as "", '') or using escaping (as in \", \').
     * Whitespaces before and after an encapsulated token are ignored.
     *
     * @param tkn the current token
     * @param c   the current character
     * @return a valid token object
     * @throws IOException on invalid state
     */
    private Token encapsulatedTokenLexer(Token tkn, int c) throws IOException {
        // save current line
        int startLineNumber = getLineNumber();
        // ignore the given delimiter
        // assert c == delimiter;
        for (; ;) {
            c = in.read();

            if (c == '\\' && format.isUnicodeEscapesInterpreted() && in.lookAhead() == 'u') {
                tkn.content.append((char) unicodeEscapeLexer(c));
            } else if (c == format.getEscape()) {
                tkn.content.append((char) readEscape(c));
            } else if (c == format.getEncapsulator()) {
                if (in.lookAhead() == format.getEncapsulator()) {
                    // double or escaped encapsulator -> add single encapsulator to token
                    c = in.read();
                    tkn.content.append((char) c);
                } else {
                    // token finish mark (encapsulator) reached: ignore whitespace till delimiter
                    for (; ;) {
                        c = in.read();
                        if (c == format.getDelimiter()) {
                            tkn.type = TT_TOKEN;
                            tkn.isReady = true;
                            return tkn;
                        } else if (isEndOfFile(c)) {
                            tkn.type = TT_EOF;
                            tkn.isReady = true;
                            return tkn;
                        } else if (isEndOfLine(c)) {
                            // ok eo token reached
                            tkn.type = TT_EORECORD;
                            tkn.isReady = true;
                            return tkn;
                        } else if (!isWhitespace(c)) {
                            // error invalid char between token and next delimiter
                            throw new IOException(
                                    "(line " + getLineNumber()
                                            + ") invalid char between encapsulated token end delimiter"
                            );
                        }
                    }
                }
            } else if (isEndOfFile(c)) {
                // error condition (end of file before end of token)
                throw new IOException(
                        "(startline " + startLineNumber + ")"
                                + "eof reached before encapsulated token finished"
                );
            } else {
                // consume character
                tkn.content.append((char) c);
            }
        }
    }


    /**
     * Decodes Unicode escapes.
     * <p/>
     * Interpretation of "\\uXXXX" escape sequences
     * where XXXX is a hex-number.
     *
     * @param c current char which is discarded because it's the "\\" of "\\uXXXX"
     * @return the decoded character
     * @throws IOException on wrong unicode escape sequence or read error
     */
    private int unicodeEscapeLexer(int c) throws IOException {
        int ret = 0;
        // ignore 'u' (assume c==\ now) and read 4 hex digits
        c = in.read();
        code.clear();
        try {
            for (int i = 0; i < 4; i++) {
                c = in.read();
                if (isEndOfFile(c) || isEndOfLine(c)) {
                    throw new NumberFormatException("number too short");
                }
                code.append((char) c);
            }
            ret = Integer.parseInt(code.toString(), 16);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "(line " + getLineNumber() + ") Wrong unicode escape sequence found '"
                            + code.toString() + "'" + e.toString());
        }
        return ret;
    }

    private int readEscape(int c) throws IOException {
        // assume c is the escape char (normally a backslash)
        c = in.read();
        int out;
        switch (c) {
            case 'r':
                out = '\r';
                break;
            case 'n':
                out = '\n';
                break;
            case 't':
                out = '\t';
                break;
            case 'b':
                out = '\b';
                break;
            case 'f':
                out = '\f';
                break;
            default:
                out = c;
        }
        return out;
    }

    // ======================================================
    //  strategies
    // ======================================================

    /**
     * Obtain the specified CSV format.
     *
     * @return format currently being used
     */
    public CSVFormat getFormat() {
        return this.format;
    }

    // ======================================================
    //  Character class checker
    // ======================================================

    /**
     * @return true if the given char is a whitespace character
     */
    private boolean isWhitespace(int c) {
        return Character.isWhitespace((char) c) && (c != format.getDelimiter());
    }

    /**
     * Greedy - accepts \n, \r and \r\n
     * This checker consumes silently the second control-character...
     *
     * @return true if the given character is a line-terminator
     */
    private boolean isEndOfLine(int c) throws IOException {
        // check if we have \r\n...
        if (c == '\r') {
            if (in.lookAhead() == '\n') {
                // note: does not change c outside of this method !!
                c = in.read();
            }
        }
        return (c == '\n' || c == '\r');
    }

    /**
     * @return true if the given character indicates end of file
     */
    private boolean isEndOfFile(int c) {
        return c == ExtendedBufferedReader.END_OF_STREAM;
    }
}
