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

import java.io.Reader;
import java.io.Serializable;

/**
 * The format specification of a CSV file.
 *
 * This class is immutable.
 */
public class CSVFormat implements Cloneable, Serializable {

    private char delimiter = ',';
    private char encapsulator = '"';
    private char commentStart = DISABLED;
    private char escape = DISABLED;
    private boolean leadingSpacesIgnored = true;
    private boolean trailingSpacesIgnored = true;
    private boolean unicodeEscapesInterpreted = false;
    private boolean emptyLinesIgnored = true;
    private String lineSeparator = "\r\n";


    /**
     * Constant char to be used for disabling comments, escapes and encapsulation.
     * The value -2 is used because it won't be confused with an EOF signal (-1),
     * and because the unicode value FFFE would be encoded as two chars (using surrogates)
     * and thus there should never be a collision with a real text char.
     */
    public static final char DISABLED = '\ufffe';

    /** Standard comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>. */
    public static final CSVFormat DEFAULT = new CSVFormat(',', '"', DISABLED, DISABLED, true, true, false, true);

    /** Excel file format (using a comma as the value delimiter). */
    public static final CSVFormat EXCEL = new CSVFormat(',', '"', DISABLED, DISABLED, false, false, false, false);

    /** Tabulation delimited format. */
    public static final CSVFormat TDF = new CSVFormat('\t', '"', DISABLED, DISABLED, true, true, false, true);


    /**
     * Creates a CSV format with the default parameters.
     */
    public CSVFormat() {
    }

    /**
     * Creates a customized CSV format.
     * 
     * @param delimiter                 the char used for value separation
     * @param encapsulator              the char used as value encapsulation marker
     * @param commentStart              the char used for comment identification
     */
    public CSVFormat(char delimiter, char encapsulator, char commentStart) {
        this(delimiter, encapsulator, commentStart, DISABLED, true, true, false, true);
    }

    /**
     * Creates a customized CSV format.
     *
     * @param delimiter                 the char used for value separation
     * @param encapsulator              the char used as value encapsulation marker
     * @param commentStart              the char used for comment identification
     * @param escape                    the char used to escape special characters in values
     * @param leadingSpacesIgnored      <tt>true</tt> when leading whitespaces should be ignored
     * @param trailingSpacesIgnored     <tt>true</tt> when trailing whitespaces should be ignored
     * @param unicodeEscapesInterpreted <tt>true</tt> when unicode escapes should be interpreted
     * @param emptyLinesIgnored         <tt>true</tt> when the parser should skip emtpy lines
     */
    public CSVFormat(
            char delimiter,
            char encapsulator,
            char commentStart,
            char escape,
            boolean leadingSpacesIgnored,
            boolean trailingSpacesIgnored,
            boolean unicodeEscapesInterpreted,
            boolean emptyLinesIgnored) {
        this.delimiter = delimiter;
        this.encapsulator = encapsulator;
        this.commentStart = commentStart;
        this.escape = escape;
        this.leadingSpacesIgnored = leadingSpacesIgnored;
        this.trailingSpacesIgnored = trailingSpacesIgnored;
        this.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        this.emptyLinesIgnored = emptyLinesIgnored;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public CSVFormat withDelimiter(char delimiter) {
        CSVFormat format = clone();
        format.delimiter = delimiter;
        return format;
    }

    public char getEncapsulator() {
        return encapsulator;
    }

    public CSVFormat withEncapsulator(char encapsulator) {
        CSVFormat format = clone();
        format.encapsulator = encapsulator;
        return format;
    }

    boolean isEncapsulating() {
        return this.encapsulator != DISABLED;
    }

    public char getCommentStart() {
        return commentStart;
    }

    public CSVFormat withCommentStart(char commentStart) {
        CSVFormat format = clone();
        format.commentStart = commentStart;
        return format;
    }

    public boolean isCommentingDisabled() {
        return this.commentStart == DISABLED;
    }

    public char getEscape() {
        return escape;
    }

    public CSVFormat withEscape(char escape) {
        CSVFormat format = clone();
        format.escape = escape;
        return format;
    }

    boolean isEscaping() {
        return this.escape != DISABLED;
    }

    public boolean isLeadingSpacesIgnored() {
        return leadingSpacesIgnored;
    }

    public CSVFormat withLeadingSpacesIgnored(boolean leadingSpacesIgnored) {
        CSVFormat format = clone();
        format.leadingSpacesIgnored = leadingSpacesIgnored;
        return format;
    }

    public boolean isTrailingSpacesIgnored() {
        return trailingSpacesIgnored;
    }

    public CSVFormat withTrailingSpacesIgnored(boolean trailingSpacesIgnored) {
        CSVFormat format = clone();
        format.trailingSpacesIgnored = trailingSpacesIgnored;
        return format;
    }

    public boolean isUnicodeEscapesInterpreted() {
        return unicodeEscapesInterpreted;
    }

    public CSVFormat withUnicodeEscapesInterpreted(boolean unicodeEscapesInterpreted) {
        CSVFormat format = clone();
        format.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        return format;
    }

    public boolean isEmptyLinesIgnored() {
        return emptyLinesIgnored;
    }

    public CSVFormat withEmptyLinesIgnored(boolean emptyLinesIgnored) {
        CSVFormat format = clone();
        format.emptyLinesIgnored = emptyLinesIgnored;
        return format;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public CSVFormat withLineSeparator(String lineSeparator) {
        CSVFormat format = clone();
        format.lineSeparator = lineSeparator;
        return format;
    }

    /**
     * Parses the specified content.
     * 
     * @param in
     */
    public Iterable<String[]> parse(Reader in) {
        return new CSVParser(in, this);
    }

    protected CSVFormat clone() {
        try {
            return (CSVFormat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw (Error) new InternalError().initCause(e);
        }
    }
}
