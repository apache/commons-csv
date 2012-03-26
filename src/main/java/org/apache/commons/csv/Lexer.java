/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.commons.csv;

import java.io.IOException;

/**
 * Abstract lexer class; contains common utility routines shared by lexers
 */
abstract class Lexer {

    private final boolean isEncapsulating;
    private final boolean isEscaping;
    private final boolean isCommentEnabled;
    
    private final char delimiter;
    private final char escape;
    private final char encapsulator;
    private final char commmentStart;
    
    final boolean surroundingSpacesIgnored;
    final boolean emptyLinesIgnored;
    
    final CSVFormat format;
    
    /** The input stream */
    final ExtendedBufferedReader in;

    Lexer(CSVFormat format, ExtendedBufferedReader in) {
        this.format = format;
        this.in = in;
        this.isEncapsulating = format.isEncapsulating();
        this.isEscaping = format.isEscaping();
        this.isCommentEnabled = format.isCommentingEnabled();
        this.delimiter = format.getDelimiter();
        this.escape = format.getEscape();
        this.encapsulator = format.getEncapsulator();
        this.commmentStart = format.getCommentStart();
        this.surroundingSpacesIgnored = format.isSurroundingSpacesIgnored();
        this.emptyLinesIgnored = format.isEmptyLinesIgnored();
    }

    int getLineNumber() {
        return in.getLineNumber();
    }

    int readEscape(int c) throws IOException {
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

    void trimTrailingSpaces(StringBuilder buffer) {
        int length = buffer.length();
        while (length > 0 && Character.isWhitespace(buffer.charAt(length - 1))) {
            length = length - 1;
        }
        if (length != buffer.length()) {
            buffer.setLength(length);
        }
    }

    /**
     * @return true if the given char is a whitespace character
     */
    boolean isWhitespace(int c) {
        return (c != format.getDelimiter()) && Character.isWhitespace((char) c);
    }

    /**
     * Greedy - accepts \n, \r and \r\n
     * This checker consumes silently the second control-character...
     *
     * @return true if the given character is a line-terminator
     */
    boolean isEndOfLine(int c) throws IOException {
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
    boolean isEndOfFile(int c) {
        return c == ExtendedBufferedReader.END_OF_STREAM;
    }

    abstract Token nextToken(Token reusableToken) throws IOException;
    
    boolean isDelimiter(int c) {
        return c == delimiter;
    }

    boolean isEscape(int c) {
        return isEscaping && c == escape;
    }

    boolean isEncapsulator(int c) {
        return isEncapsulating && c == encapsulator;
    }

    boolean isCommentStart(int c) {
        return isCommentEnabled && c == commmentStart;
    }
}
