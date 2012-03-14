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
public class CSVFormat implements Serializable {

    /** According to RFC 4180, line breaks are delimited by CRLF */
    private static final String CRLF = "\r\n";
    private final char delimiter;
    private final char encapsulator;
    private final char commentStart;
    private final char escape;
    private final boolean leadingSpacesIgnored;
    private final boolean trailingSpacesIgnored;
    private final boolean unicodeEscapesInterpreted;
    private final boolean emptyLinesIgnored;
    private final String lineSeparator;


    /**
     * Constant char to be used for disabling comments, escapes and encapsulation.
     * The value -2 is used because it won't be confused with an EOF signal (-1),
     * and because the unicode value FFFE would be encoded as two chars (using surrogates)
     * and thus there should never be a collision with a real text char.
     */
    static final char DISABLED = '\ufffe';

    /** Standard comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>. */
    public static final CSVFormat DEFAULT = new CSVFormat(',', '"', DISABLED, DISABLED, true, true, false, true, CRLF);

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
    public static final CSVFormat EXCEL = new CSVFormat(',', '"', DISABLED, DISABLED, false, false, false, false, CRLF);

    /** Tab-delimited format, with quote; leading and trailing spaces ignored. */
    public static final CSVFormat TDF = new CSVFormat('\t', '"', DISABLED, DISABLED, true, true, false, true, CRLF);

    /**
     * Default MySQL format used by the <tt>SELECT INTO OUTFILE</tt> and
     * <tt>LOAD DATA INFILE</tt> operations. This is a tab-delimited
     * format with a LF character as the line separator. Values are not quoted
     * and special characters are escaped with '\'.
     * 
     * @see <a href="http://dev.mysql.com/doc/refman/5.1/en/load-data.html">http://dev.mysql.com/doc/refman/5.1/en/load-data.html</a>
     */
    public static final CSVFormat MYSQL = new CSVFormat('\t', DISABLED, DISABLED, '\\', false, false, false, false, "\n");


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
     * @param lineSeparator             the line separator to use.
     */
    CSVFormat(
            char delimiter,
            char encapsulator,
            char commentStart,
            char escape,
            boolean leadingSpacesIgnored,
            boolean trailingSpacesIgnored,
            boolean unicodeEscapesInterpreted,
            boolean emptyLinesIgnored,
            String lineSeparator) {
        this.delimiter = delimiter;
        this.encapsulator = encapsulator;
        this.commentStart = commentStart;
        this.escape = escape;
        this.leadingSpacesIgnored = leadingSpacesIgnored;
        this.trailingSpacesIgnored = trailingSpacesIgnored;
        this.unicodeEscapesInterpreted = unicodeEscapesInterpreted;
        this.emptyLinesIgnored = emptyLinesIgnored;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns true if the given character is a line break character.
     * 
     * @param c the character to check
     * 
     * @return true if <code>c</code> is a line break character
     */
    private static boolean isLineBreak(char c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Verifies the consistency of the parameters and throws an IllegalArgumentException if necessary.
     */
    void validate() throws IllegalArgumentException {
        if (delimiter == encapsulator) {
            throw new IllegalArgumentException("The encapsulator character and the delimiter cannot be the same (\"" + encapsulator + "\")");
        }
        
        if (delimiter == escape) {
            throw new IllegalArgumentException("The escape character and the delimiter cannot be the same (\"" + escape + "\")");
        }
        
        if (delimiter == commentStart) {
            throw new IllegalArgumentException("The comment start character and the delimiter cannot be the same (\"" + commentStart + "\")");
        }
        
        if (encapsulator != DISABLED && encapsulator == commentStart) {
            throw new IllegalArgumentException("The comment start character and the encapsulator cannot be the same (\"" + commentStart + "\")");
        }
        
        if (escape != DISABLED && escape == commentStart) {
            throw new IllegalArgumentException("The comment start and the escape character cannot be the same (\"" + commentStart + "\")");
        }
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
     * @throws IllegalArgumentException thrown if the specified character is a line break
     */
    public CSVFormat withDelimiter(char delimiter) {
        if (isLineBreak(delimiter)) {
            throw new IllegalArgumentException("The delimiter cannot be a line break");
        }

        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
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
     * @throws IllegalArgumentException thrown if the specified character is a line break
     */
    public CSVFormat withEncapsulator(char encapsulator) {
        if (isLineBreak(encapsulator)) {
            throw new IllegalArgumentException("The encapsulator cannot be a line break");
        }
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
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
     * @throws IllegalArgumentException thrown if the specified character is a line break
     */
    public CSVFormat withCommentStart(char commentStart) {
        if (isLineBreak(commentStart)) {
            throw new IllegalArgumentException("The comment start character cannot be a line break");
        }
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
    }

    /**
     * Specifies whether comments are supported by this format.
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
     * @throws IllegalArgumentException thrown if the specified character is a line break
     */
    public CSVFormat withEscape(char escape) {
        if (isLineBreak(escape)) {
            throw new IllegalArgumentException("The escape character cannot be a line break");
        }
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
    }

    /**
     * Returns a copy of this format with the specified trimming behavior.
     *
     * @param surroundingSpacesIgnored the trimming behavior, <tt>true</tt> to remove the surrounding spaces,
     *                                 <tt>false</tt> to leave the spaces as is.
     * @return A copy of this format with the specified trimming behavior.
     */
    public CSVFormat withSurroundingSpacesIgnored(boolean surroundingSpacesIgnored) {
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, surroundingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
    }

    /**
     * Tells if unicode escape sequences (e.g. {@literal \u1234}) are turned into their corresponding character
     * when parsing input.
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
    }

    /**
     * Specifies whether empty lines between records are ignored when parsing input.
     * 
     * @return <tt>true</tt> if empty lines between records are ignored, <tt>false</tt> if they are turned into empty records.
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
    }

    /**
     * Returns the line separator delimiting output records.
     * 
     * @return the line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns a copy of this format using the specified output line separator.
     * 
     * @param lineSeparator the line separator to be used for output.
     * 
     * @return A copy of this format using the specified output line separator
     */
    public CSVFormat withLineSeparator(String lineSeparator) {
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, leadingSpacesIgnored, trailingSpacesIgnored, unicodeEscapesInterpreted, emptyLinesIgnored, lineSeparator);
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
}
