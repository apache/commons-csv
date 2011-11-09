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
 * CSVStrategy
 *
 * Represents the strategy for a CSV.
 */
public class CSVStrategy implements Cloneable, Serializable {

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
    public static final CSVStrategy DEFAULT_STRATEGY = new CSVStrategy(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);
    
    /** Excel file format (using a comma as the value delimiter). */
    public static final CSVStrategy EXCEL_STRATEGY = new CSVStrategy(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, false, false, false, false);
    
    /** Tabulation delimited format. */
    public static final CSVStrategy TDF_STRATEGY = new CSVStrategy('\t', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);


    /**
     * Creates a CSVStrategy with the default parameters.
     */
    public CSVStrategy() {
    }

    public CSVStrategy(char delimiter, char encapsulator, char commentStart) {
        this(delimiter, encapsulator, commentStart, ESCAPE_DISABLED, true, true, false, true);
    }

    /**
     * Customized CSV strategy constructor.
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
    public CSVStrategy(
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

    public CSVStrategy withDelimiter(char delimiter) {
        CSVStrategy strategy = (CSVStrategy) clone();
        this.delimiter = delimiter;
        return strategy;
    }

    public char getEncapsulator() {
        return encapsulator;
    }

    public CSVStrategy withEncapsulator(char encapsulator) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.encapsulator = encapsulator;
        return strategy;
    }

    public char getCommentStart() {
        return commentStart;
    }

    public CSVStrategy withCommentStart(char commentStart) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.commentStart = commentStart;
        return strategy;
    }

    public boolean isCommentingDisabled() {
        return this.commentStart == COMMENTS_DISABLED;
    }

    public char getEscape() {
        return escape;
    }

    public CSVStrategy withEscape(char escape) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.escape = escape;
        return strategy;
    }

    public boolean isLeadingSpacesIgnored() {
        return leadingSpacesIgnored;
    }

    public CSVStrategy withLeadingSpacesIgnored(boolean leadingSpacesIgnored) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.leadingSpacesIgnored = leadingSpacesIgnored;
        return strategy;
    }

    public boolean isTrailingSpacesIgnored() {
        return trailingSpacesIgnored;
    }

    public CSVStrategy withTrailingSpacesIgnored(boolean trailingSpacesIgnored) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.trailingSpacesIgnored = trailingSpacesIgnored;
        return strategy;
    }

    public boolean isUnicodeEscapesInterpreted() {
        return unicodeEscapesInterpreted;
    }

    public CSVStrategy withUnicodeEscapesInterpreted(boolean unicodeEscapesInterpreted) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        return strategy;
    }

    public boolean isEmptyLinesIgnored() {
        return emptyLinesIgnored;
    }

    public CSVStrategy withEmptyLinesIgnored(boolean emptyLinesIgnored) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.emptyLinesIgnored = emptyLinesIgnored;
        return strategy;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public CSVStrategy withLineSeparator(String lineSeparator) {
        CSVStrategy strategy = (CSVStrategy) clone();
        strategy.lineSeparator = lineSeparator;
        return strategy;
    }

    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw (Error) new InternalError().initCause(e);
        }
    }
}
