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

import static org.apache.commons.csv.Constants.COMMA;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE;
import static org.apache.commons.csv.Constants.ESCAPE;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * The format specification of a CSV file.
 *
 * This class is immutable.
 * 
 * @version $Id: $
 */
public class CSVFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constant char to use for disabling comments, escapes and encapsulation. The value -2 is used because it
     * won't be confused with an EOF signal (-1), and because the Unicode value {@code FFFE} would be encoded as two chars
     * (using surrogates) and thus there should never be a collision with a real text char.
     */
    private static final char DISABLED = '\ufffe';

    private final char delimiter;
    private final Character quoteChar;
    private final Quote quotePolicy;
    private final Character commentStart;
    private final Character escape;
    private final boolean ignoreSurroundingSpaces; // Should leading/trailing spaces be ignored around values?
    private final boolean ignoreEmptyLines;
    private final String lineSeparator; // for outputs
    private final String[] header;

    /**
     * Starting format; used for creating other formats.
     */
    static final CSVFormat PRISTINE = new CSVFormat(COMMA, null, null, null, null, false, false, null, null);

    /**
     * Standard comma separated format, as for {@link #RFC4180} but allowing blank lines.
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withEmptyLinesIgnored(true)</li>
     * <li>withLineSeparator(CRLF)</li>
     * </ul>
     */
    public static final CSVFormat DEFAULT =
            PRISTINE
            .withDelimiter(COMMA)
            .withQuoteChar(DOUBLE_QUOTE)
            .withIgnoreEmptyLines(true)
            .withLineSeparator(CRLF);

    /**
     * Comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>.
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withLineSeparator(CRLF)</li>
     * <li></li>
     * </ul>
     */
    public static final CSVFormat RFC4180 =
            PRISTINE
            .withDelimiter(COMMA)
            .withQuoteChar(DOUBLE_QUOTE)
            .withLineSeparator(CRLF);

    /**
     * Excel file format (using a comma as the value delimiter). Note that the actual value delimiter used by Excel is
     * locale dependent, it might be necessary to customize this format to accomodate to your regional settings.
     * <p/>
     * For example for parsing or generating a CSV file on a French system the following format will be used:
     *
     * <pre>
     * CSVFormat fmt = CSVFormat.EXCEL.withDelimiter(';');
     * </pre>
     */
    public static final CSVFormat EXCEL =
            PRISTINE
            .withDelimiter(COMMA)
            .withQuoteChar(DOUBLE_QUOTE)
            .withLineSeparator(CRLF);

    /** Tab-delimited format, with quote; leading and trailing spaces ignored. */
    public static final CSVFormat TDF =
            PRISTINE
            .withDelimiter(TAB)
            .withQuoteChar(DOUBLE_QUOTE)
            .withIgnoreSurroundingSpaces(true)
            .withIgnoreEmptyLines(true)
            .withLineSeparator(CRLF);

    /**
     * Default MySQL format used by the <tt>SELECT INTO OUTFILE</tt> and <tt>LOAD DATA INFILE</tt> operations. This is
     * a tab-delimited format with a LF character as the line separator. Values are not quoted and special characters
     * are escaped with '\'.
     *
     * @see <a href="http://dev.mysql.com/doc/refman/5.1/en/load-data.html">
     *      http://dev.mysql.com/doc/refman/5.1/en/load-data.html</a>
     */
    public static final CSVFormat MYSQL =
            PRISTINE
            .withDelimiter(TAB)
            .withEscape(ESCAPE)
            .withLineSeparator(LF);

    /**
     * Creates a basic CSV format.
     * 
     * @param delimiter 
     *            the char used for value separation, must not be a line break character
     * @throws IllegalArgumentException if the delimiter is a line break character
     */
    public CSVFormat(char delimiter){
        this(delimiter,  null, null, null, null, false, false, null, null);
    }

    /**
     * Creates a customized CSV format.
     *
     * @param delimiter
     *            the char used for value separation, must not be a line break character
     * @param quoteChar
     *            the char used as value encapsulation marker
     * @param quotePolicy 
     *            the quote policy
     * @param commentStart
     *            the char used for comment identification
     * @param escape
     *            the char used to escape special characters in values
     * @param ignoreSurroundingSpaces
     *            <tt>true</tt> when whitespaces enclosing values should be ignored
     * @param ignoreEmptyLines
     *            <tt>true</tt> when the parser should skip empty lines
     * @param lineSeparator
     *            the line separator to use for output
     * @param header
     *            the header
     * @throws IllegalArgumentException if the delimiter is a line break character
     */
    public CSVFormat(final char delimiter, final Character quoteChar, final Quote quotePolicy, final Character commentStart, final Character escape, final 
                    boolean ignoreSurroundingSpaces, final boolean ignoreEmptyLines, final String lineSeparator, 
            final String[] header) {
        if (isLineBreak(delimiter)) {
            throw new IllegalArgumentException("The delimiter cannot be a line break");
        }
        this.delimiter = delimiter;
        this.quoteChar = quoteChar;
        this.quotePolicy = quotePolicy;
        this.commentStart = commentStart;
        this.escape = escape;
        this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
        this.ignoreEmptyLines = ignoreEmptyLines;
        this.lineSeparator = lineSeparator;
        this.header = header;
    }

    /**
     * Returns true if the given character is a line break character.
     *
     * @param c
     *            the character to check
     *
     * @return true if <code>c</code> is a line break character
     */
    private static boolean isLineBreak(final Character c) {
        return c != null && isLineBreak(c.charValue());
    }

    /**
     * Returns true if the given character is a line break character.
     *
     * @param c
     *            the character to check
     *
     * @return true if <code>c</code> is a line break character
     */
    private static boolean isLineBreak(final char c) {
        return c == LF || c == CR;
    }

    /**
     * Verifies the consistency of the parameters and throws an IllegalStateException if necessary.
     * 
     * @throws IllegalStateException
     */
    void validate() throws IllegalStateException {
        if (quoteChar != null && delimiter == quoteChar.charValue()) {
            throw new IllegalStateException("The quoteChar character and the delimiter cannot be the same ('" + quoteChar + "')");
        }

        if (escape != null && delimiter == escape.charValue()) {
            throw new IllegalStateException("The escape character and the delimiter cannot be the same ('" + escape + "')");
        }

        if (commentStart != null && delimiter == commentStart.charValue()) {
            throw new IllegalStateException("The comment start character and the delimiter cannot be the same ('" + commentStart + 
                    "')");
        }

        if (quoteChar != null && quoteChar.equals(commentStart)) {
            throw new IllegalStateException("The comment start character and the quoteChar cannot be the same ('" + commentStart + 
                    "')");
        }

        if (escape != null && escape.equals(commentStart)) {
            throw new IllegalStateException("The comment start and the escape character cannot be the same ('" + commentStart + "')");
        }

        if (escape == null && quotePolicy == Quote.NONE) {
            throw new IllegalStateException("No quotes mode set but no escape character is set");
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
     * @param delimiter
     *            the delimiter character
     * @return A copy of this format using the specified delimiter character
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withDelimiter(final char delimiter) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Returns the character used to encapsulate values containing special characters.
     *
     * @return the quoteChar character
     */
    public Character getQuoteChar() {
        return quoteChar;
    }

    /**
     * Returns a copy of this format using the specified quoteChar character.
     *
     * @param quoteChar
     *            the quoteChar character
     * @return A copy of this format using the specified quoteChar character
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withQuoteChar(final char quoteChar) {
        return withQuoteChar(Character.valueOf(quoteChar));
    }

    /**
     * Returns a copy of this format using the specified quoteChar character.
     *
     * @param quoteChar
     *            the quoteChar character
     * @return A copy of this format using the specified quoteChar character
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withQuoteChar(final Character quoteChar) {
        if (isLineBreak(quoteChar)) {
            throw new IllegalArgumentException("The quoteChar cannot be a line break");
        }
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Returns whether an quoteChar has been defined.
     *
     * @return {@code true} if an quoteChar is defined
     */
    public boolean isQuoting() {
        return quoteChar != null;
    }

    /**
     * Returns the character marking the start of a line comment.
     *
     * @return the comment start marker.
     */
    public Character getCommentStart() {
        return commentStart;
    }

    /**
     * Returns a copy of this format using the specified character as the comment start marker.
     *
     * Note that the comment introducer character is only recognised at the start of a line.
     *
     * @param commentStart
     *            the comment start marker
     * @return A copy of this format using the specified character as the comment start marker
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withCommentStart(final char commentStart) {
        return withCommentStart(Character.valueOf(commentStart));
    }

    /**
     * Returns a copy of this format using the specified character as the comment start marker.
     *
     * Note that the comment introducer character is only recognised at the start of a line.
     *
     * @param commentStart
     *            the comment start marker
     * @return A copy of this format using the specified character as the comment start marker
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withCommentStart(final Character commentStart) {
        if (isLineBreak(commentStart)) {
            throw new IllegalArgumentException("The comment start character cannot be a line break");
        }
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Specifies whether comments are supported by this format.
     *
     * Note that the comment introducer character is only recognised at the start of a line.
     *
     * @return <tt>true</tt> is comments are supported, <tt>false</tt> otherwise
     */
    public boolean isCommentingEnabled() {
        return commentStart != null;
    }

    /**
     * Returns the escape character.
     *
     * @return the escape character
     */
    public Character getEscape() {
        return escape;
    }

    /**
     * Returns a copy of this format using the specified escape character.
     *
     * @param escape
     *            the escape character
     * @return A copy of this format using the specified escape character
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withEscape(final char escape) {
        return withEscape(Character.valueOf(escape));
    }

    /**
     * Returns a copy of this format using the specified escape character.
     *
     * @param escape
     *            the escape character
     * @return A copy of this format using the specified escape character
     * @throws IllegalArgumentException
     *             thrown if the specified character is a line break
     */
    public CSVFormat withEscape(final Character escape) {
        if (isLineBreak(escape)) {
            throw new IllegalArgumentException("The escape character cannot be a line break");
        }
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Returns whether escape are being processed.
     *
     * @return {@code true} if escapes are processed
     */
    public boolean isEscaping() {
        return escape != null;
    }

    /**
     * Specifies whether spaces around values are ignored when parsing input.
     *
     * @return <tt>true</tt> if spaces around values are ignored, <tt>false</tt> if they are treated as part of the
     *         value.
     */
    public boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    /**
     * Returns a copy of this format with the specified trimming behavior.
     *
     * @param ignoreSurroundingSpaces
     *            the trimming behavior, <tt>true</tt> to remove the surrounding spaces, <tt>false</tt> to leave the
     *            spaces as is.
     * @return A copy of this format with the specified trimming behavior.
     */
    public CSVFormat withIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Specifies whether empty lines between records are ignored when parsing input.
     *
     * @return <tt>true</tt> if empty lines between records are ignored, <tt>false</tt> if they are turned into empty
     *         records.
     */
    public boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    /**
     * Returns a copy of this format with the specified empty line skipping behavior.
     *
     * @param ignoreEmptyLines
     *            the empty line skipping behavior, <tt>true</tt> to ignore the empty lines between the records,
     *            <tt>false</tt> to translate empty lines to empty records.
     * @return A copy of this format with the specified empty line skipping behavior.
     */
    public CSVFormat withIgnoreEmptyLines(final boolean ignoreEmptyLines) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
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
     * @param lineSeparator
     *            the line separator to use for output.
     *
     * @return A copy of this format using the specified output line separator
     */
    public CSVFormat withLineSeparator(final char lineSeparator) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, String.valueOf(lineSeparator), header);
    }

    /**
     * Returns a copy of this format using the specified output line separator.
     *
     * @param lineSeparator
     *            the line separator to use for output.
     *
     * @return A copy of this format using the specified output line separator
     */
    public CSVFormat withLineSeparator(final String lineSeparator) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Returns a copy of this format using the specified output quote policy.
     *
     * @param quotePolicy
     *            the quote policy to use for output.
     *
     * @return A copy of this format using the specified output line separator
     */
    public CSVFormat withQuotePolicy(final Quote quotePolicy) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    String[] getHeader() {
        return header;
    }

    /**
     * Returns a copy of this format using the specified header. The header can either be parsed automatically from the
     * input file with:
     *
     * <pre>
     * CSVFormat format = aformat.withHeader();
     * </pre>
     *
     * or specified manually with:
     *
     * <pre>
     * CSVFormat format = aformat.withHeader(&quot;name&quot;, &quot;email&quot;, &quot;phone&quot;);
     * </pre>
     *
     * @param header
     *            the header, <tt>null</tt> if disabled, empty if parsed automatically, user specified otherwise.
     *
     * @return A copy of this format using the specified header
     */
    public CSVFormat withHeader(final String... header) {
        return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                ignoreSurroundingSpaces, ignoreEmptyLines, lineSeparator, header);
    }

    /**
     * Parses the specified content.
     *
     * @param in
     *            the input stream
     */
    public Iterable<CSVRecord> parse(final Reader in) throws IOException {
        return new CSVParser(in, this);
    }

    /**
     * Format the specified values.
     *
     * @param values
     *            the values to format
     */
    public String format(final Object... values) {
        final StringWriter out = new StringWriter();
        try {
            new CSVPrinter(out, this).printRecord(values);
            return out.toString().trim();
        } catch (final IOException e) {
            // should not happen because a StringWriter does not do IO.
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Delimiter=<").append(delimiter).append('>');
        if (isEscaping()) {
            sb.append(' ');
            sb.append("Escape=<").append(escape).append('>');
        }
        if (isQuoting()) {
            sb.append(' ');
            sb.append("QuoteChar=<").append(quoteChar).append('>');
        }
        if (isCommentingEnabled()) {
            sb.append(' ');
            sb.append("CommentStart=<").append(commentStart).append('>');
        }
        if (getIgnoreEmptyLines()) {
            sb.append(" EmptyLines:ignored");
        }
        if (getIgnoreSurroundingSpaces()) {
            sb.append(" SurroundingSpaces:ignored");
        }
        return sb.toString();
    }

    /**
     * Returns the quote policy output fields.
     *
     * @return the quote policy
     */
    public Quote getQuotePolicy() {
        return quotePolicy;
    }

}
