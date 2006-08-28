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

/**
 * CSVStrategy
 * 
 * Represents the strategy for a CSV.
 */
public class CSVStrategy {

    private char delimiter;
    private char encapsulator;
    private char commentStart;
    private boolean ignoreLeadingWhitespaces;
    private boolean interpretUnicodeEscapes;
    private boolean ignoreEmptyLines;

    public static char COMMENTS_DISABLED       = (char) 0;

    public static CSVStrategy DEFAULT_STRATEGY = new CSVStrategy(',', '"', COMMENTS_DISABLED, true,  false, true);
    public static CSVStrategy EXCEL_STRATEGY   = new CSVStrategy(';', '"', COMMENTS_DISABLED, false, false, false);
    public static CSVStrategy TDF_STRATEGY     = new CSVStrategy('	', '"', COMMENTS_DISABLED, true,  false, true);


    public CSVStrategy(char delimiter, char encapsulator, char commentStart) {
        this(delimiter, encapsulator, commentStart, true, false, true);
    }
  
    /**
     * Customized CSV strategy setter.
     * 
     * @param delimiter a Char used for value separation
     * @param encapsulator a Char used as value encapsulation marker
     * @param commentStart a Char used for comment identification
     * @param ignoreLeadingWhitespace TRUE when leading whitespaces should be
     *                                ignored
     * @param interpretUnicodeEscapes TRUE when unicode escapes should be 
     *                                interpreted
     * @param ignoreEmptyLines TRUE when the parser should skip emtpy lines
     */
    public CSVStrategy(
        char delimiter, 
        char encapsulator, 
        char commentStart, 
        boolean ignoreLeadingWhitespace, 
        boolean interpretUnicodeEscapes,
        boolean ignoreEmptyLines) 
    {
        setDelimiter(delimiter);
        setEncapsulator(encapsulator);
        setCommentStart(commentStart);
        setIgnoreLeadingWhitespaces(ignoreLeadingWhitespace);
        setUnicodeEscapeInterpretation(interpretUnicodeEscapes);
        setIgnoreEmptyLines(ignoreEmptyLines);
    }

    public void setDelimiter(char delimiter) { this.delimiter = delimiter; }
    public char getDelimiter() { return this.delimiter; }

    public void setEncapsulator(char encapsulator) { this.encapsulator = encapsulator; }
    public char getEncapsulator() { return this.encapsulator; }

    public void setCommentStart(char commentStart) { this.commentStart = commentStart; }
    public char getCommentStart() { return this.commentStart; }
    public boolean isCommentingDisabled() { return this.commentStart == COMMENTS_DISABLED; }

    public void setIgnoreLeadingWhitespaces(boolean ignoreLeadingWhitespaces) { this.ignoreLeadingWhitespaces = ignoreLeadingWhitespaces; }
    public boolean getIgnoreLeadingWhitespaces() { return this.ignoreLeadingWhitespaces; }

    public void setUnicodeEscapeInterpretation(boolean interpretUnicodeEscapes) { this.interpretUnicodeEscapes = interpretUnicodeEscapes; }
    public boolean getUnicodeEscapeInterpretation() { return this.interpretUnicodeEscapes; }

    public void setIgnoreEmptyLines(boolean ignoreEmptyLines) { this.ignoreEmptyLines = ignoreEmptyLines; }
    public boolean getIgnoreEmptyLines() { return this.ignoreEmptyLines; }

}
