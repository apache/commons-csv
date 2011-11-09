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

import java.io.Serializable;

/**
 * The format specification of a CSV file.
 *
 * This class is immutable.
 */
public class CSVFormat implements Cloneable, Serializable {

    private char delimiter = ',';
    private char encapsulator = '"';
    private char commentStart = COMMENTS_DISABLED;
    private char escape = ESCAPE_DISABLED;
    private boolean leadingSpacesIgnored = true;
    private boolean trailingSpacesIgnored = true;
    private boolean unicodeEscapesInterpreted = false;
    private boolean emptyLinesIgnored = true;
    private String lineSeparator = "\n";

    // -2 is used to signal disabled, because it won't be confused with
    // an EOF signal (-1), and because \ufffe in UTF-16 would be
    // encoded as two chars (using surrogates) and thus there should never
    // be a collision with a real text char.
    public static final char COMMENTS_DISABLED = (char) -2;
    public static final char ESCAPE_DISABLED = (char) -2;
    public static final char ENCAPSULATOR_DISABLED = (char) -2;

    /** Standard comma separated format. */
    public static final CSVFormat DEFAULT = new CSVFormat(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);
    
    /** Excel file format (using a comma as the value delimiter). */
    public static final CSVFormat EXCEL = new CSVFormat(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, false, false, false, false);
    
    /** Tabulation delimited format. */
    public static final CSVFormat TDF = new CSVFormat('\t', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);


    /**
     * Creates a CSVFormat with the default parameters.
     */
    public CSVFormat() {
    }

    public CSVFormat(char delimiter, char encapsulator, char commentStart) {
        this(delimiter, encapsulator, commentStart, ESCAPE_DISABLED, true, true, false, true);
    }

    /**
     * Customized CSV format constructor.
     *
     * @param delimiter                 a char used for value separation
     * @param encapsulator              a char used as value encapsulation marker
     * @param commentStart              a char used for comment identification
     * @param escape                    a char used to escape special characters in values
     * @param leadingSpacesIgnored      TRUE when leading whitespaces should be ignored
     * @param trailingSpacesIgnored     TRUE when trailing whitespaces should be ignored
     * @param unicodeEscapesInterpreted TRUE when unicode escapes should be interpreted
     * @param emptyLinesIgnored         TRUE when the parser should skip emtpy lines
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
        CSVFormat format = (CSVFormat) clone();
        this.delimiter = delimiter;
        return format;
    }

    public char getEncapsulator() {
        return encapsulator;
    }

    public CSVFormat withEncapsulator(char encapsulator) {
        CSVFormat format = (CSVFormat) clone();
        format.encapsulator = encapsulator;
        return format;
    }

    public char getCommentStart() {
        return commentStart;
    }

    public CSVFormat withCommentStart(char commentStart) {
        CSVFormat format = (CSVFormat) clone();
        format.commentStart = commentStart;
        return format;
    }

    public boolean isCommentingDisabled() {
        return this.commentStart == COMMENTS_DISABLED;
    }

    public char getEscape() {
        return escape;
    }

    public CSVFormat withEscape(char escape) {
        CSVFormat format = (CSVFormat) clone();
        format.escape = escape;
        return format;
    }

    public boolean isLeadingSpacesIgnored() {
        return leadingSpacesIgnored;
    }

    public CSVFormat withLeadingSpacesIgnored(boolean leadingSpacesIgnored) {
        CSVFormat format = (CSVFormat) clone();
        format.leadingSpacesIgnored = leadingSpacesIgnored;
        return format;
    }

    public boolean isTrailingSpacesIgnored() {
        return trailingSpacesIgnored;
    }

    public CSVFormat withTrailingSpacesIgnored(boolean trailingSpacesIgnored) {
        CSVFormat format = (CSVFormat) clone();
        format.trailingSpacesIgnored = trailingSpacesIgnored;
        return format;
    }

    public boolean isUnicodeEscapesInterpreted() {
        return unicodeEscapesInterpreted;
    }

    public CSVFormat withUnicodeEscapesInterpreted(boolean unicodeEscapesInterpreted) {
        CSVFormat format = (CSVFormat) clone();
        format.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        return format;
    }

    public boolean isEmptyLinesIgnored() {
        return emptyLinesIgnored;
    }

    public CSVFormat withEmptyLinesIgnored(boolean emptyLinesIgnored) {
        CSVFormat format = (CSVFormat) clone();
        format.emptyLinesIgnored = emptyLinesIgnored;
        return format;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public CSVFormat withLineSeparator(String lineSeparator) {
        CSVFormat format = (CSVFormat) clone();
        format.lineSeparator = lineSeparator;
        return format;
    }

    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw (Error) new InternalError().initCause(e);
        }
    }
}
