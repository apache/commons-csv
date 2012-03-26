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
    private final boolean surroundingSpacesIgnored; // Should leading/trailing spaces be ignored around values?
    private final boolean emptyLinesIgnored;
    private final String lineSeparator; // for outputs
    private final String[] header;


    /**
     * Constant char to be used for disabling comments, escapes and encapsulation.
     * The value -2 is used because it won't be confused with an EOF signal (-1),
     * and because the unicode value FFFE would be encoded as two chars (using surrogates)
     * and thus there should never be a collision with a real text char.
     */
    static final char DISABLED = '\ufffe';

    /**
     * Starting format with no settings defined; used for creating other formats from scratch.
     */
    private static CSVFormat PRISTINE = new CSVFormat(DISABLED, DISABLED, DISABLED, DISABLED, false, false, null, null);

    /** 
     * Standard comma separated format, as for {@link #RFC4180} but allowing blank lines. 
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withEncapsulator('"')</li>
     * <li>withEmptyLinesIgnored(true)</li>
     * <li>withLineSeparator(CRLF)</li>
     * </ul> 
     */
    public static final CSVFormat DEFAULT =
            PRISTINE.
            withDelimiter(',')
            .withEncapsulator('"')
            .withEmptyLinesIgnored(true)
            .withLineSeparator(CRLF);

    /**
     * Comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>.
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withEncapsulator('"')</li>
     * <li>withLineSeparator(CRLF)</li>
     * <li></li>
     * </ul> 
     */
    public static final CSVFormat RFC4180 =
            PRISTINE.
            withDelimiter(',')
            .withEncapsulator('"')
            .withLineSeparator(CRLF);

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
    public static final CSVFormat EXCEL =
            PRISTINE
            .withDelimiter(',')
            .withEncapsulator('"')
            .withLineSeparator(CRLF);

    /** Tab-delimited format, with quote; leading and trailing spaces ignored. */
    public static final CSVFormat TDF =
            PRISTINE
            .withDelimiter('\t')
            .withEncapsulator('"')
            .withSurroundingSpacesIgnored(true)
            .withEmptyLinesIgnored(true)
            .withLineSeparator(CRLF);

    /**
     * Default MySQL format used by the <tt>SELECT INTO OUTFILE</tt> and
     * <tt>LOAD DATA INFILE</tt> operations. This is a tab-delimited
     * format with a LF character as the line separator. Values are not quoted
     * and special characters are escaped with '\'.
     * 
     * @see <a href="http://dev.mysql.com/doc/refman/5.1/en/load-data.html">http://dev.mysql.com/doc/refman/5.1/en/load-data.html</a>
     */
    public static final CSVFormat MYSQL =
            PRISTINE
            .withDelimiter('\t')
            .withEscape('\\')
            .withLineSeparator("\n");


    /**
     * Creates a customized CSV format.
     *
     * @param delimiter                 the char used for value separation
     * @param encapsulator              the char used as value encapsulation marker
     * @param commentStart              the char used for comment identification
     * @param escape                    the char used to escape special characters in values
     * @param surroundingSpacesIgnored  <tt>true</tt> when whitespaces enclosing values should be ignored
     * @param emptyLinesIgnored         <tt>true</tt> when the parser should skip emtpy lines
     * @param lineSeparator             the line separator to use for output
     * @param header                    the header
     */
    CSVFormat(
            char delimiter,
            char encapsulator,
            char commentStart,
            char escape,
            boolean surroundingSpacesIgnored,
            boolean emptyLinesIgnored,
            String lineSeparator,
            String[] header) {
        this.delimiter = delimiter;
        this.encapsulator = encapsulator;
        this.commentStart = commentStart;
        this.escape = escape;
        this.surroundingSpacesIgnored = surroundingSpacesIgnored;
        this.emptyLinesIgnored = emptyLinesIgnored;
        this.lineSeparator = lineSeparator;
        this.header = header;
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

        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
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
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
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
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
    }

    /**
     * Specifies whether comments are supported by this format.
     * 
     * @return <tt>true</tt> is comments are supported, <tt>false</tt> otherwise
     */
    public boolean isCommentingEnabled() {
        return this.commentStart != DISABLED;
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
        
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
    }

    boolean isEscaping() {
        return this.escape != DISABLED;
    }

    /**
     * Specifies whether spaces around values are ignored when parsing input.
     * 
     * @return <tt>true</tt> if spaces around values are ignored, <tt>false</tt> if they are treated as part of the value.
     */
    public boolean isSurroundingSpacesIgnored() {
        return surroundingSpacesIgnored;
    }

    /**
     * Returns a copy of this format with the specified trimming behavior.
     *
     * @param surroundingSpacesIgnored the trimming behavior, <tt>true</tt> to remove the surrounding spaces,
     *                                 <tt>false</tt> to leave the spaces as is.
     * @return A copy of this format with the specified trimming behavior.
     */
    public CSVFormat withSurroundingSpacesIgnored(boolean surroundingSpacesIgnored) {
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
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
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
    }

    String[] getHeader() {
        return header;
    }

    /**
     * Returns a copy of this format using the specified header. The header can
     * either be parsed automatically from the input file with:
     *
     * <pre>CSVFormat format = CSVFormat.DEFAULT.withHeader();</pre>
     *
     * or specified manually with:
     *
     * <pre>CSVFormat format = CSVFormat.DEFAULT.withHeader("name", "email", "phone");</pre>
     *
     * @param header the header, <tt>null</tt> if disabled, empty if parsed automatically, user specified otherwise.
     *
     * @return A copy of this format using the specified header
     */
    public CSVFormat withHeader(String... header) {
        return new CSVFormat(delimiter, encapsulator, commentStart, escape, surroundingSpacesIgnored, emptyLinesIgnored, lineSeparator, header);
    }

    /**
     * Parses the specified content.
     * 
     * @param in the input stream
     */
    public Iterable<CSVRecord> parse(Reader in) throws IOException {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Delimiter=<").append(delimiter).append('>');
        if (isEscaping()) {
            sb.append(' ');
            sb.append("Escape=<").append(escape).append('>');
        }
        if (isEncapsulating()) {
            sb.append(' ');
            sb.append("Encapsulator=<").append(encapsulator).append('>');            
        }
        if (isCommentingEnabled()) {
            sb.append(' ');
            sb.append("CommentStart=<").append(commentStart).append('>');
        }
        if (isEmptyLinesIgnored()) {
            sb.append(" EmptyLines:ignored");            
        }
        if (isSurroundingSpacesIgnored()) {
            sb.append(" SurroundingSpaces:ignored");            
        }
        return sb.toString();
    }
    
}
