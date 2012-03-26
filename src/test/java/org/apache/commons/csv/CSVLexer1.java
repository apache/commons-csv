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

import static org.apache.commons.csv.Token.Type.*;

class CSVLexer1 extends Lexer {

    private final StringBuilder wsBuf = new StringBuilder();
    
    // ctor needs to be public so can be called dynamically by PerformanceTest class
    public CSVLexer1(CSVFormat format, ExtendedBufferedReader in) {
        super(format, in);
    }
    
    /**
     * Returns the next token.
     * <p/>
     * A token corresponds to a term, a record change or an end-of-file indicator.
     *
     * @param tkn an existing Token object to reuse. The caller is responsible to initialize the Token.
     * @return the next token found
     * @throws java.io.IOException on stream access error
     */
    @Override
    Token nextToken(Token tkn) throws IOException {
        wsBuf.setLength(0); // reuse

        // get the last read char (required for empty line detection)
        int lastChar = in.readAgain();

        //  read the next char and set eol
        /* note: unfortunately isEndOfLine may consumes a character silently.
        *       this has no effect outside of the method. so a simple workaround
        *       is to call 'readAgain' on the stream...
        */
        int c = in.read();
        boolean eol = isEndOfLine(c);
        c = in.readAgain();

        //  empty line detection: eol AND (last char was EOL or beginning)
        if (format.isEmptyLinesIgnored()) {
            while (eol
                    && (lastChar == '\n' || lastChar == '\r' || lastChar == ExtendedBufferedReader.UNDEFINED)
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
        }

        // did we reach eof during the last iteration already ? EOF
        if (isEndOfFile(lastChar) || (lastChar != format.getDelimiter() && isEndOfFile(c))) {
            tkn.type = EOF;
            return tkn;
        }

        //  important: make sure a new char gets consumed in each iteration
        while (!tkn.isReady && tkn.type != EOF) {
            // ignore whitespaces at beginning of a token
            if (format.isSurroundingSpacesIgnored()) {
                while (isWhitespace(c) && !eol) {
                    wsBuf.append((char) c);
                    c = in.read();
                    eol = isEndOfLine(c);
                }
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
                if (!format.isSurroundingSpacesIgnored()) {
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
        while (true) {
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

        if (format.isSurroundingSpacesIgnored()) {
            trimTrailingSpaces(tkn.content);
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
        while (true) {
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
                    while (true) {
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

}