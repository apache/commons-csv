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

    private char delimiter;
    private char encapsulator;
    private char commentStart;
    private char escape;
    private boolean ignoreLeadingWhitespaces;
    private boolean ignoreTrailingWhitespaces;
    private boolean interpretUnicodeEscapes;
    private boolean ignoreEmptyLines;

    // controls for output
    private String printerNewline = "\n";

    // -2 is used to signal disabled, because it won't be confused with
    // an EOF signal (-1), and because \ufffe in UTF-16 would be
    // encoded as two chars (using surrogates) and thus there should never
    // be a collision with a real text char.
    public static final char COMMENTS_DISABLED = (char) -2;
    public static final char ESCAPE_DISABLED = (char) -2;
    public static final char ENCAPSULATOR_DISABLED = (char) -2;

    public static final CSVStrategy DEFAULT_STRATEGY = new CSVStrategy(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);
    public static final CSVStrategy EXCEL_STRATEGY = new CSVStrategy(',', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, false, false, false, false);
    public static final CSVStrategy TDF_STRATEGY = new CSVStrategy('\t', '"', COMMENTS_DISABLED, ESCAPE_DISABLED, true, true, false, true);


    public CSVStrategy(char delimiter, char encapsulator, char commentStart) {
        this(delimiter, encapsulator, commentStart, true, false, true);
    }

    /**
     * Customized CSV strategy constructor.
     *
     * @param delimiter                 a char used for value separation
     * @param encapsulator              a char used as value encapsulation marker
     * @param commentStart              a char used for comment identification
     * @param escape                    a char used to escape special characters in values
     * @param ignoreLeadingWhitespaces  TRUE when leading whitespaces should be ignored
     * @param ignoreTrailingWhitespaces TRUE when trailing whitespaces should be ignored
     * @param interpretUnicodeEscapes   TRUE when unicode escapes should be interpreted
     * @param ignoreEmptyLines          TRUE when the parser should skip emtpy lines
     */
    public CSVStrategy(
            char delimiter,
            char encapsulator,
            char commentStart,
            char escape,
            boolean ignoreLeadingWhitespaces,
            boolean ignoreTrailingWhitespaces,
            boolean interpretUnicodeEscapes,
            boolean ignoreEmptyLines) {
        this.delimiter = delimiter;
        this.encapsulator = encapsulator;
        this.commentStart = commentStart;
        this.escape = escape;
        this.ignoreLeadingWhitespaces = ignoreLeadingWhitespaces;
        this.ignoreTrailingWhitespaces = ignoreTrailingWhitespaces;
        this.interpretUnicodeEscapes = interpretUnicodeEscapes;
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    /**
     * @deprecated
     */
    public CSVStrategy(
            char delimiter,
            char encapsulator,
            char commentStart,
            boolean ignoreLeadingWhitespaces,
            boolean interpretUnicodeEscapes,
            boolean ignoreEmptyLines) {
        this(delimiter, encapsulator, commentStart, CSVStrategy.ESCAPE_DISABLED, ignoreLeadingWhitespaces,
                true, interpretUnicodeEscapes, ignoreEmptyLines);
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getDelimiter() {
        return this.delimiter;
    }

    public void setEncapsulator(char encapsulator) {
        this.encapsulator = encapsulator;
    }

    public char getEncapsulator() {
        return this.encapsulator;
    }

    public void setCommentStart(char commentStart) {
        this.commentStart = commentStart;
    }

    public char getCommentStart() {
        return this.commentStart;
    }

    public boolean isCommentingDisabled() {
        return this.commentStart == COMMENTS_DISABLED;
    }

    public void setEscape(char escape) {
        this.escape = escape;
    }

    public char getEscape() {
        return this.escape;
    }

    public void setIgnoreLeadingWhitespaces(boolean ignoreLeadingWhitespaces) {
        this.ignoreLeadingWhitespaces = ignoreLeadingWhitespaces;
    }

    public boolean getIgnoreLeadingWhitespaces() {
        return this.ignoreLeadingWhitespaces;
    }

    public void setIgnoreTrailingWhitespaces(boolean ignoreTrailingWhitespaces) {
        this.ignoreTrailingWhitespaces = ignoreTrailingWhitespaces;
    }

    public boolean getIgnoreTrailingWhitespaces() {
        return this.ignoreTrailingWhitespaces;
    }

    public void setUnicodeEscapeInterpretation(boolean interpretUnicodeEscapes) {
        this.interpretUnicodeEscapes = interpretUnicodeEscapes;
    }

    public boolean getUnicodeEscapeInterpretation() {
        return this.interpretUnicodeEscapes;
    }

    public boolean getIgnoreEmptyLines() {
        return this.ignoreEmptyLines;
    }

    public String getPrinterNewline() {
        return this.printerNewline;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);  // impossible
        }
    }
}
