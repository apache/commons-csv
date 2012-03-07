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
import java.io.Serializable;
import java.io.StringWriter;

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
    static final char DISABLED = '\ufffe';

    /** Standard comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>. */
    public static final CSVFormat DEFAULT = new CSVFormat(',', '"', DISABLED, DISABLED, true, true, false, true);

    /**
     * Excel file format (using a comma as the value delimiter).
     * Note that the actual value delimiter used by Excel is locale dependent,
     * it might be necessary to customize this format to accomodate to your
     * regional settings.
     * <p/>
     * For example for parsing or generating a CSV file on a French system
     * the following format will be used:
     * 
     * <pre>CSVFormat fmt = CSVFormat.EXCEL.withDelimiter(';');</pre>
     */
    public static final CSVFormat EXCEL = new CSVFormat(',', '"', DISABLED, DISABLED, false, false, false, false);

    /** Tabulation delimited format. */
    public static final CSVFormat TDF = new CSVFormat('\t', '"', DISABLED, DISABLED, true, true, false, true);
    
    /**
     * Default MySQL format used by the <tt>SELECT INTO OUTFILE</tt> and
     * <tt>LOAD DATA INFILE</tt> operations. This is a tabulation delimited
     * format with a LF character as the line separator. Values are not quoted
     * and special characters are escaped with '\'.
     * 
     * @see <a href="http://dev.mysql.com/doc/refman/5.1/en/load-data.html">http://dev.mysql.com/doc/refman/5.1/en/load-data.html</a>
     */
    public static final CSVFormat MYSQL = new CSVFormat('\t', DISABLED, DISABLED, '\\', false, false, false, false).withLineSeparator("\n");


    /**
     * Creates a CSV format with the default parameters.
     */
    CSVFormat() {
    }

    /**
     * Creates a customized CSV format.
     * 
     * @param delimiter                 the char used for value separation
     * @param encapsulator              the char used as value encapsulation marker
     * @param commentStart              the char used for comment identification
     */
    CSVFormat(char delimiter, char encapsulator, char commentStart) {
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
    CSVFormat(
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

    /**
     * Returns the character delimiting the values (typically ';', ',' or '\t').
     * 
     * @return the delimiter character
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Returns a copy of this format using the specified delimiter character.
     * 
     * @param delimiter the delimiter character
     * @return A copy of this format using the specified delimiter character
     */
    public CSVFormat withDelimiter(char delimiter) {
        CSVFormat format = clone();
        format.delimiter = delimiter;
        return format;
    }

    /**
     * Returns the character used to encapsulate values containing special characters.
     * 
     * @return the encapsulator character
     */
    public char getEncapsulator() {
        return encapsulator;
    }

    /**
     * Returns a copy of this format using the specified encapsulator character.
     * 
     * @param encapsulator the encapsulator character
     * @return A copy of this format using the specified encapsulator character
     */
    public CSVFormat withEncapsulator(char encapsulator) {
        CSVFormat format = clone();
        format.encapsulator = encapsulator;
        return format;
    }

    boolean isEncapsulating() {
        return this.encapsulator != DISABLED;
    }

    /**
     * Returns the character marking the start of a line comment.
     * 
     * @return the comment start marker.
     */
    public char getCommentStart() {
        return commentStart;
    }

    /**
     * Returns a copy of this format using the specified character as the comment start marker.
     * 
     * @param commentStart the comment start marker
     * @return A copy of this format using the specified character as the comment start marker
     */
    public CSVFormat withCommentStart(char commentStart) {
        CSVFormat format = clone();
        format.commentStart = commentStart;
        return format;
    }

    /**
     * Tells if comments are supported by this format.
     * 
     * @return <tt>true</tt> is comments are supported, <tt>false</tt> otherwise
     */
    public boolean isCommentingDisabled() {
        return this.commentStart == DISABLED;
    }

    /**
     * Returns the escape character.
     * 
     * @return the escape character
     */
    public char getEscape() {
        return escape;
    }

    /**
     * Returns a copy of this format using the specified escape character.
     * 
     * @param escape the escape character
     * @return A copy of this format using the specified escape character
     */
    public CSVFormat withEscape(char escape) {
        CSVFormat format = clone();
        format.escape = escape;
        return format;
    }

    boolean isEscaping() {
        return this.escape != DISABLED;
    }

    /**
     * Tells if the spaces characters at the beginning of the values are ignored when parsing a file.
     * 
     * @return <tt>true</tt> if leading spaces are removed, <tt>false</tt> if they are preserved.
     */
    public boolean isLeadingSpacesIgnored() {
        return leadingSpacesIgnored;
    }

    /**
     * Returns a copy of this format with the specified left trimming behavior.
     *
     * @param leadingSpacesIgnored the left trimming behavior, <tt>true</tt> to remove the leading spaces,
     *                             <tt>false</tt> to leave the spaces as is.
     * @return A copy of this format with the specified left trimming behavior.
     */
    public CSVFormat withLeadingSpacesIgnored(boolean leadingSpacesIgnored) {
        CSVFormat format = clone();
        format.leadingSpacesIgnored = leadingSpacesIgnored;
        return format;
    }

    /**
     * Tells if the spaces characters at the end of the values are ignored when parsing a file.
     * 
     * @return <tt>true</tt> if trailing spaces are removed, <tt>false</tt> if they are preserved.
     */
    public boolean isTrailingSpacesIgnored() {
        return trailingSpacesIgnored;
    }

    /**
     * Returns a copy of this format with the specified right trimming behavior.
     *
     * @param trailingSpacesIgnored the right trimming behavior, <tt>true</tt> to remove the trailing spaces,
     *                              <tt>false</tt> to leave the spaces as is.
     * @return A copy of this format with the specified right trimming behavior.
     */
    public CSVFormat withTrailingSpacesIgnored(boolean trailingSpacesIgnored) {
        CSVFormat format = clone();
        format.trailingSpacesIgnored = trailingSpacesIgnored;
        return format;
    }

    /**
     * Returns a copy of this format with the specified trimming behavior.
     *
     * @param surroundingSpacesIgnored the trimming behavior, <tt>true</tt> to remove the surrounding spaces,
     *                                 <tt>false</tt> to leave the spaces as is.
     * @return A copy of this format with the specified trimming behavior.
     */
    public CSVFormat withSurroundingSpacesIgnored(boolean surroundingSpacesIgnored) {
        CSVFormat format = clone();
        format.leadingSpacesIgnored = surroundingSpacesIgnored;
        format.trailingSpacesIgnored = surroundingSpacesIgnored;
        return format;
    }

    /**
     * Tells if unicode escape sequences (i.e <span>\</span>u1234) are turned into their corresponding character.
     * 
     * @return <tt>true</tt> if unicode escape sequences are interpreted, <tt>false</tt> if they are left as is.
     */
    public boolean isUnicodeEscapesInterpreted() {
        return unicodeEscapesInterpreted;
    }

    /**
     * Returns a copy of this format with the specified unicode escaping behavior.
     *
     * @param unicodeEscapesInterpreted the escaping behavior, <tt>true</tt> to interpret unicode escape sequences,
     *                                  <tt>false</tt> to leave the escape sequences as is.
     * @return A copy of this format with the specified unicode escaping behavior.
     */
    public CSVFormat withUnicodeEscapesInterpreted(boolean unicodeEscapesInterpreted) {
        CSVFormat format = clone();
        format.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        return format;
    }

    /**
     * Tells if the empty lines between the records are ignored.
     * 
     * @return <tt>true</tt> if empty lines between records are ignore, <tt>false</tt> if they are turned into empty records.
     */
    public boolean isEmptyLinesIgnored() {
        return emptyLinesIgnored;
    }

    /**
     * Returns a copy of this format with the specified empty line skipping behavior.
     *
     * @param emptyLinesIgnored the empty line skipping behavior, <tt>true</tt> to ignore the empty lines
     *                          between the records, <tt>false</tt> to translate empty lines to empty records.
     * @return A copy of this format  with the specified empty line skipping behavior.
     */
    public CSVFormat withEmptyLinesIgnored(boolean emptyLinesIgnored) {
        CSVFormat format = clone();
        format.emptyLinesIgnored = emptyLinesIgnored;
        return format;
    }

    /**
     * Returns the line separator delimiting the records.
     * 
     * @return the line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns a copy of this format using the specified line separator.
     * 
     * @param lineSeparator the line separator
     * @return A copy of this format using the specified line separator
     */
    public CSVFormat withLineSeparator(String lineSeparator) {
        CSVFormat format = clone();
        format.lineSeparator = lineSeparator;
        return format;
    }

    /**
     * Parses the specified content.
     * 
     * @param in the input stream
     */
    public Iterable<String[]> parse(Reader in) {
        return new CSVParser(in, this);
    }

    /**
     * Format the specified values.
     * 
     * @param values the values to format
     */
    public String format(String... values) {
        StringWriter out = new StringWriter();
        try {
            new CSVPrinter(out, this).println(values);
        } catch (IOException e) {
            // should not happen
        }
        
        return out.toString().trim();
    }

    protected CSVFormat clone() {
        try {
            return (CSVFormat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw (Error) new InternalError().initCause(e);
        }
    }
}
