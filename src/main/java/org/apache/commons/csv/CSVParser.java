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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVLexer.Token;

import static org.apache.commons.csv.CSVLexer.Token.Type.*;

/**
 * Parses CSV files according to the specified configuration.
 *
 * Because CSV appears in many different dialects, the parser supports many
 * configuration settings by allowing the specification of a {@link CSVFormat}.
 *
 * <p>Parsing of a csv-string having tabs as separators,
 * '"' as an optional value encapsulator, and comments starting with '#':</p>
 * <pre>
 * CSVFormat format = new CSVFormat('\t', '"', '#');
 * Reader in = new StringReader("a\tb\nc\td");
 * String[][] records = new CSVParser(in, format).getRecords();
 * </pre>
 *
 * <p>Parsing of a csv-string in Excel CSV format, using a for-each loop:</p>
 * <pre>
 * Reader in = new StringReader("a;b\nc;d");
 * CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
 * for (String[] record : parser) {
 *     ...
 * }
 * </pre>
 *
 * <p>
 * Internal parser state is completely covered by the format
 * and the reader-state.</p>
 *
 * <p>see <a href="package-summary.html">package documentation</a>
 * for more details</p>
 */
public class CSVParser implements Iterable<String[]> {

    /** Immutable empty String array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final CSVLexer lexer;
    
    // the following objects are shared to reduce garbage
    
    /** A record buffer for getRecord(). Grows as necessary and is reused. */
    private final List<String> record = new ArrayList<String>();
    private final Token reusableToken = new Token();

    /**
     * CSV parser using the default {@link CSVFormat}.
     *
     * @param input a Reader containing "csv-formatted" input
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(Reader input) {
        this(input, CSVFormat.DEFAULT);
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input    a Reader containing "csv-formatted" input
     * @param format the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(Reader input, CSVFormat format) {
        format.validate();
        
        if (format.isUnicodeEscapesInterpreted()) {
            input = new UnicodeUnescapeReader(input);
        }
        
        this.lexer = new CSVLexer(format, new ExtendedBufferedReader(input));
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input    a String containing "csv-formatted" input
     * @param format the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(String input, CSVFormat format) {
        this(new StringReader(input), format);
    }


    /**
     * Parses the CSV input according to the given format and returns the content
     * as an array of records (whereas records are arrays of single values).
     * <p/>
     * The returned content starts at the current parse-position in the stream.
     *
     * @return matrix of records x values ('null' when end of file)
     * @throws IOException on parse error or input read-failure
     */
    public String[][] getRecords() throws IOException {
        List<String[]> records = new ArrayList<String[]>();
        String[] record;
        while ((record = getRecord()) != null) {
            records.add(record);
        }
        
        if (!records.isEmpty()) {
            return records.toArray(new String[records.size()][]);
        } else {
            return null;
        }
    }

    /**
     * Parses the next record from the current point in the stream.
     *
     * @return the record as an array of values, or <tt>null</tt> if the end of the stream has been reached
     * @throws IOException on parse error or input read-failure
     */
    String[] getRecord() throws IOException {
        String[] result = EMPTY_STRING_ARRAY;
        record.clear();
        while (true) {
            reusableToken.reset();
            lexer.nextToken(reusableToken);
            switch (reusableToken.type) {
                case TOKEN:
                    record.add(reusableToken.content.toString());
                    break;
                case EORECORD:
                    record.add(reusableToken.content.toString());
                    break;
                case EOF:
                    if (reusableToken.isReady) {
                        record.add(reusableToken.content.toString());
                    } else {
                        result = null;
                    }
                    break;
                case INVALID:
                    // error: throw IOException
                    throw new IOException("(line " + getLineNumber() + ") invalid parse sequence");
                    // unreachable: break;
            }
            if (reusableToken.type != TOKEN) {
                break;
            }
        }
        if (!record.isEmpty()) {
            result = record.toArray(new String[record.size()]);
        }
        return result;
    }

    /**
     * Returns an iterator on the records. IOExceptions occuring
     * during the iteration are wrapped in a RuntimeException.
     */
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            String[] current;
            
            public boolean hasNext() {
                if (current == null) {
                    current = getNextLine();
                }
                
                return current != null;
            }

            public String[] next() {
                String[] next = current;
                current = null;

                if (next == null) {
                    // hasNext() wasn't called before
                    next = getNextLine();
                    if (next == null) {
                        throw new NoSuchElementException("No more CSV records available");
                    }
                }
                
                return next;
            }
            
            private String[] getNextLine() {
                try {
                    return getRecord();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public void remove() { }
        };
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
        return lexer.getLineNumber();
    }
}


class CSVLexer {

    /** length of the initial token (content-)buffer */
    private static final int INITIAL_TOKEN_LENGTH = 50;
    
    private final CharBuffer wsBuf = new CharBuffer();
    
    private final CSVFormat format;
    
    /** The input stream */
    private final ExtendedBufferedReader in;

    /**
     * Token is an internal token representation.
     * <p/>
     * It is used as contract between the lexer and the parser.
     */
    static class Token {

        enum Type {
            /** Token has no valid content, i.e. is in its initialized state. */
            INVALID,
            
            /** Token with content, at beginning or in the middle of a line. */
            TOKEN,
            
            /** Token (which can have content) when end of file is reached. */
            EOF,
            
            /** Token with content when end of a line is reached. */
            EORECORD
        }
        
        /** Token type */
        Type type = INVALID;
        
        /** The content buffer. */
        CharBuffer content = new CharBuffer(INITIAL_TOKEN_LENGTH);
        
        /** Token ready flag: indicates a valid token with content (ready for the parser). */
        boolean isReady;

        Token reset() {
            content.clear();
            type = INVALID;
            isReady = false;
            return this;
        }
    }

    CSVLexer(CSVFormat format, ExtendedBufferedReader in) {
        this.format = format;
        this.in = in;
    }

    public int getLineNumber() {
        return in.getLineNumber();
    }

    /**
     * Returns the next token.
     * <p/>
     * A token corresponds to a term, a record change or an end-of-file indicator.
     *
     * @param tkn an existing Token object to reuse. The caller is responsible to initialize the Token.
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
                tkn.type = EOF;
                return tkn;
            }
        }

        // did we reach eof during the last iteration already ? EOF
        if (isEndOfFile(lastChar) || (lastChar != format.getDelimiter() && isEndOfFile(c))) {
            tkn.type = EOF;
            return tkn;
        }

        //  important: make sure a new char gets consumed in each iteration
        while (!tkn.isReady && tkn.type != EOF) {
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
                // empty token return TOKEN("")
                tkn.type = TOKEN;
                tkn.isReady = true;
            } else if (eol) {
                // empty token return EORECORD("")
                //noop: tkn.content.append("");
                tkn.type = EORECORD;
                tkn.isReady = true;
            } else if (c == format.getEncapsulator()) {
                // consume encapsulated token
                encapsulatedTokenLexer(tkn, c);
            } else if (isEndOfFile(c)) {
                // end of file return EOF()
                //noop: tkn.content.append("");
                tkn.type = EOF;
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
     *   <li>end of line has been reached (EORECORD)</li>
     *   <li>end of stream has been reached (EOF)</li>
     *   <li>an unescaped delimiter has been reached (TOKEN)</li>
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
                tkn.type = EORECORD;
                tkn.isReady = true;
                break;
            } else if (isEndOfFile(c)) {
                // end of file
                tkn.type = EOF;
                tkn.isReady = true;
                break;
            } else if (c == format.getDelimiter()) {
                // end of token
                tkn.type = TOKEN;
                tkn.isReady = true;
                break;
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
            
            if (c == format.getEscape()) {
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
                            tkn.type = TOKEN;
                            tkn.isReady = true;
                            return tkn;
                        } else if (isEndOfFile(c)) {
                            tkn.type = EOF;
                            tkn.isReady = true;
                            return tkn;
                        } else if (isEndOfLine(c)) {
                            // ok eo token reached
                            tkn.type = EORECORD;
                            tkn.isReady = true;
                            return tkn;
                        } else if (!isWhitespace(c)) {
                            // error invalid char between token and next delimiter
                            throw new IOException("(line " + getLineNumber() + ") invalid char between encapsulated token and delimiter");
                        }
                    }
                }
            } else if (isEndOfFile(c)) {
                // error condition (end of file before end of token)
                throw new IOException("(startline " + startLineNumber + ") EOF reached before encapsulated token finished");
            } else {
                // consume character
                tkn.content.append((char) c);
            }
        }
    }

    private int readEscape(int c) throws IOException {
        // assume c is the escape char (normally a backslash)
        c = in.read();
        switch (c) {
            case 'r':
                return '\r';
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            default:
                return c;
        }
    }

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
        if (c == '\r' && in.lookAhead() == '\n') {
            // note: does not change c outside of this method !!
            c = in.read();
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
