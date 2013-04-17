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
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE_CHAR;
import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Specifies the format of a CSV file and parses input.
 * <p>
 * This class is immutable.
 * </p>
 * You can extend a format through a builder. For example, to extend the Excel format with columns header, you write:
 * </p>
 * <pre>CSVFormat.EXCEL.toBuilder().withHeader(&quot;Col1&quot;, &quot;Col2&quot;, &quot;Col3&quot;).build();</pre>
 * <p>
 * You can parse through a format. For example, to parse an Excel file with columns header, you write:
 * </p>
 * <pre>Reader in = ...;
 *CSVFormat.EXCEL.toBuilder().withHeader(&quot;Col1&quot;, &quot;Col2&quot;, &quot;Col3&quot;).parse(in);</pre>
 * <p>
 *
 * @version $Id$
 */
public class CSVFormat implements Serializable {

    /**
     * Builds CSVFormat objects.
     */
    public static class CSVFormatBuilder {

        private char delimiter;
        private Character quoteChar;
        private Quote quotePolicy;
        private Character commentStart;
        private Character escape;
        private boolean ignoreSurroundingSpaces; // Should leading/trailing spaces be ignored around values?
        private boolean ignoreEmptyLines;
        private String recordSeparator; // for outputs
        private String nullToString; // for outputs
        private String[] header;

        /**
         * Creates a basic CSVFormatBuilder.
         *
         * @param delimiter
         *            the char used for value separation, must not be a line break character
         * @throws IllegalArgumentException if the delimiter is a line break character
         */
        // package protected to give access without needing a synthetic accessor
        CSVFormatBuilder(final char delimiter){
            this(delimiter, null, null, null, null, false, false, null, Constants.EMPTY, null);
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
         * @param nullToString TODO
         * @param header
         *            the header
         * @param recordSeparator
         *            the record separator to use for output
         * @throws IllegalArgumentException if the delimiter is a line break character
         */
        // package protected for use by test code
        CSVFormatBuilder(final char delimiter, final Character quoteChar,
                final Quote quotePolicy, final Character commentStart,
                final Character escape, final boolean ignoreSurroundingSpaces,
                final boolean ignoreEmptyLines, final String recordSeparator,
                String nullToString, final String[] header) {
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
            this.recordSeparator = recordSeparator;
            this.nullToString = nullToString;
            this.header = header;
        }

        /**
         * Creates a CSVFormatBuilder, using the values of the given CSVFormat.
         *
         * @param format
         *            The format to use values from
         */
        @SuppressWarnings("synthetic-access") // TODO fields could be made package-protected
        // package protected to give access without needing a synthetic accessor
        CSVFormatBuilder(final CSVFormat format) {
            this(format.delimiter, format.quoteChar, format.quotePolicy,
                    format.commentStart, format.escape,
                    format.ignoreSurroundingSpaces, format.ignoreEmptyLines,
                    format.recordSeparator, format.nullToString, format.header);
        }

        /**
         * Builds a new CSVFormat configured with the values from this builder.
         *
         * @return a new CSVFormat
         */
        public CSVFormat build() {
            validate();
            return new CSVFormat(delimiter, quoteChar, quotePolicy, commentStart, escape,
                                 ignoreSurroundingSpaces, ignoreEmptyLines, recordSeparator, nullToString, header);
        }

        /**
         * Parses the specified content. Short-hand for:
         * <pre>format.build().parse(in);</pre>
         *
         * @param in
         *            the input stream
         * @return a CSVRecord stream
         * @throws IOException
         *             If an I/O error occurs
         */
        public Iterable<CSVRecord> parse(final Reader in) throws IOException {
            return this.build().parse(in);
        }

        /**
         * Verifies the consistency of the parameters and throws an IllegalStateException if necessary.
         *
         * @throws IllegalStateException
         */
        private void validate() throws IllegalStateException {
            if (quoteChar != null && delimiter == quoteChar.charValue()) {
                throw new IllegalStateException(
                        "The quoteChar character and the delimiter cannot be the same ('" + quoteChar + "')");
            }

            if (escape != null && delimiter == escape.charValue()) {
                throw new IllegalStateException(
                        "The escape character and the delimiter cannot be the same ('" + escape + "')");
            }

            if (commentStart != null && delimiter == commentStart.charValue()) {
                throw new IllegalStateException(
                        "The comment start character and the delimiter cannot be the same ('" + commentStart + "')");
            }

            if (quoteChar != null && quoteChar.equals(commentStart)) {
                throw new IllegalStateException(
                        "The comment start character and the quoteChar cannot be the same ('" + commentStart + "')");
            }

            if (escape != null && escape.equals(commentStart)) {
                throw new IllegalStateException(
                        "The comment start and the escape character cannot be the same ('" + commentStart + "')");
            }

            if (escape == null && quotePolicy == Quote.NONE) {
                throw new IllegalStateException("No quotes mode set but no escape character is set");
            }
        }

        /**
         * Sets the comment start marker of the format to the specified character.
         *
         * Note that the comment introducer character is only recognised at the start of a line.
         *
         * @param commentStart
         *            the comment start marker
         * @return This builder with the specified character as the comment start marker
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withCommentStart(final char commentStart) {
            return withCommentStart(Character.valueOf(commentStart));
        }

        /**
         * Sets the comment start marker of the format to the specified character.
         *
         * Note that the comment introducer character is only recognised at the start of a line.
         *
         * @param commentStart
         *            the comment start marker
         * @return This builder with the specified character as the comment start marker
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withCommentStart(final Character commentStart) {
            if (isLineBreak(commentStart)) {
                throw new IllegalArgumentException("The comment start character cannot be a line break");
            }
            this.commentStart = commentStart;
            return this;
        }

        /**
         * Sets the delimiter of the format to the specified character.
         *
         * @param delimiter
         *            the delimiter character
         * @return This builder with the specified character as delimiter
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withDelimiter(final char delimiter) {
            if (isLineBreak(delimiter)) {
                throw new IllegalArgumentException("The delimiter cannot be a line break");
            }
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Sets the escape character of the format to the specified character.
         *
         * @param escape
         *            the escape character
         * @return This builder with the specified character as the escape character
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withEscape(final char escape) {
            return withEscape(Character.valueOf(escape));
        }

        /**
         * Sets the escape character of the format to the specified character.
         *
         * @param escape
         *            the escape character
         * @return This builder with the specified character as the escape character
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withEscape(final Character escape) {
            if (isLineBreak(escape)) {
                throw new IllegalArgumentException("The escape character cannot be a line break");
            }
            this.escape = escape;
            return this;
        }

        /**
         * Sets the header of the format. The header can either be parsed automatically from the
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
         * @return This builder with the specified header
         */
        public CSVFormatBuilder withHeader(final String... header) {
            this.header = header;
            return this;
        }

        /**
         * Sets the empty line skipping behavior of the format.
         *
         * @param ignoreEmptyLines
         *            the empty line skipping behavior, <tt>true</tt> to ignore the empty lines between the records,
         *            <tt>false</tt> to translate empty lines to empty records.
         * @return This builder with the specified empty line skipping behavior.
         */
        public CSVFormatBuilder withIgnoreEmptyLines(final boolean ignoreEmptyLines) {
            this.ignoreEmptyLines = ignoreEmptyLines;
            return this;
        }

        /**
         * Sets the trimming behavior of the format.
         *
         * @param ignoreSurroundingSpaces
         *            the trimming behavior, <tt>true</tt> to remove the surrounding spaces, <tt>false</tt> to leave the
         *            spaces as is.
         * @return This builder with the specified trimming behavior.
         */
        public CSVFormatBuilder withIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
            this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
            return this;
        }

        /**
         * Sets the String to use for null values for output.
         *
         * @param nullToString
         *            the String to use for null values for output.
         *
         * @return This builder with the the specified output record separator
         */
        public CSVFormatBuilder withNullToString(final String nullToString) {
            this.nullToString = nullToString;
            return this;
        }

        /**
         * Sets the quoteChar of the format to the specified character.
         *
         * @param quoteChar
         *            the quoteChar character
         * @return This builder with the specified character as quoteChar
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withQuoteChar(final char quoteChar) {
            return withQuoteChar(Character.valueOf(quoteChar));
        }

        /**
         * Sets the quoteChar of the format to the specified character.
         *
         * @param quoteChar
         *            the quoteChar character
         * @return This builder with the specified character as quoteChar
         * @throws IllegalArgumentException
         *             thrown if the specified character is a line break
         */
        public CSVFormatBuilder withQuoteChar(final Character quoteChar) {
            if (isLineBreak(quoteChar)) {
                throw new IllegalArgumentException("The quoteChar cannot be a line break");
            }
            this.quoteChar = quoteChar;
            return this;
        }

        /**
         * Sets the output quote policy of the format to the specified value.
         *
         * @param quotePolicy
         *            the quote policy to use for output.
         *
         * @return This builder with the specified quote policy
         */
        public CSVFormatBuilder withQuotePolicy(final Quote quotePolicy) {
            this.quotePolicy = quotePolicy;
            return this;
        }

        /**
         * Sets the record separator of the format to the specified character.
         *
         * @param recordSeparator
         *            the record separator to use for output.
         *
         * @return This builder with the the specified output record separator
         */
        public CSVFormatBuilder withRecordSeparator(final char recordSeparator) {
            return withRecordSeparator(String.valueOf(recordSeparator));
        }

        /**
         * Sets the record separator of the format to the specified String.
         *
         * @param recordSeparator
         *            the record separator to use for output.
         *
         * @return This builder with the the specified output record separator
         */
        public CSVFormatBuilder withRecordSeparator(final String recordSeparator) {
            this.recordSeparator = recordSeparator;
            return this;
        }
    }

    private static final long serialVersionUID = 1L;
    /**
     * Returns true if the given character is a line break character.
     *
     * @param c
     *            the character to check
     *
     * @return true if <code>c</code> is a line break character
     */
    // package protected to give access without needing a synthetic accessor
    static boolean isLineBreak(final Character c) {
        return c != null && isLineBreak(c.charValue());
    }
    /**
     * Creates a standard comma separated format builder, as for {@link #RFC4180} but allowing empty lines.
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withEmptyLinesIgnored(true)</li>
     * <li>withRecordSeparator(CRLF)</li>
     * </ul>
     *
     * Shortcut for {@code CSVFormat.newBuilder(CSVFormat.DEFAULT)}
     *
     * @return a standard comma separated format builder, as for {@link #RFC4180} but allowing empty lines.
     */
    public static CSVFormatBuilder newBuilder() {
        return new CSVFormatBuilder(COMMA, DOUBLE_QUOTE_CHAR, null, null, null, false, true, CRLF, Constants.EMPTY,
                null);
    }
    private final char delimiter;
    private final Character quoteChar;
    private final Quote quotePolicy;
    private final Character commentStart;
    private final Character escape;
    private final boolean ignoreSurroundingSpaces; // Should leading/trailing spaces be ignored around values?
    private final boolean ignoreEmptyLines;

    private final String recordSeparator; // for outputs

    private final String nullToString; // for outputs

    private final String[] header;

    /**
     * Comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>.
     * <h3>RFC 4180:</h3>
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withRecordSeparator(CRLF)</li>
     * </ul>
     */
    public static final CSVFormat RFC4180 =
            newBuilder()
            .withIgnoreEmptyLines(false)
            .build();

    /**
     * Standard comma separated format, as for {@link #RFC4180} but allowing empty lines.
     * <h3>RFC 4180:</h3>
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withRecordSeparator(CRLF)</li>
     * </ul>
     * <h3>Additional:</h3>
     * <ul>
     * <li>withIgnoreEmptyLines(true)</li>
     * </ul>
     */
    public static final CSVFormat DEFAULT =
            newBuilder()
            .build();

    /**
     * Excel file format (using a comma as the value delimiter). Note that the actual value delimiter used by Excel is
     * locale dependent, it might be necessary to customize this format to accommodate to your regional settings.
     * <p/>
     * For example for parsing or generating a CSV file on a French system the following format will be used:
     *
     * <pre>
     * CSVFormat fmt = CSVFormat.newBuilder(EXCEL).withDelimiter(';').build();
     * </pre>
     * Settings are:
     * <ul>
     * <li>withDelimiter(',')</li>
     * <li>withQuoteChar('"')</li>
     * <li>withRecordSeparator(CRLF)</li>
     * </ul>
     * Note: this is currently the same as RFC4180
     */
    public static final CSVFormat EXCEL =
            newBuilder()
            .withIgnoreEmptyLines(false)
            .build();

    /** Tab-delimited format, with quote; leading and trailing spaces ignored. */
    public static final CSVFormat TDF =
            newBuilder()
            .withDelimiter(TAB)
            .withIgnoreSurroundingSpaces(true)
            .build();

    /**
     * Default MySQL format used by the <tt>SELECT INTO OUTFILE</tt> and <tt>LOAD DATA INFILE</tt> operations. This is
     * a tab-delimited format with a LF character as the line separator. Values are not quoted and special characters
     * are escaped with '\'.
     *
     * @see <a href="http://dev.mysql.com/doc/refman/5.1/en/load-data.html">
     *      http://dev.mysql.com/doc/refman/5.1/en/load-data.html</a>
     */
    public static final CSVFormat MYSQL =
            newBuilder()
            .withDelimiter(TAB)
            .withQuoteChar(null)
            .withEscape(BACKSLASH)
            .withIgnoreEmptyLines(false)
            .withRecordSeparator(LF)
            .build();

    /**
     * Returns true if the given character is a line break character.
     *
     * @param c
     *            the character to check
     *
     * @return true if <code>c</code> is a line break character
     */
    // package protected to give access without needing a synthetic accessor
    static boolean isLineBreak(final char c) {
        return c == LF || c == CR;
    }

    /**
     * Creates a new CSV format builder.
     *
     * @param delimiter
     *            the char used for value separation, must not be a line break character
     * @return a new CSV format builder.
     * @throws IllegalArgumentException if the delimiter is a line break character
     */
    public static CSVFormatBuilder newBuilder(final char delimiter) {
        return new CSVFormatBuilder(delimiter);
    }

    /**
     * Creates a CSVFormatBuilder, using the values of the given CSVFormat.
     *
     * @param format
     *            The format to use values from
     * @return a new CSVFormatBuilder
     */
    public static CSVFormatBuilder newBuilder(final CSVFormat format) {
        return new CSVFormatBuilder(format);
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
     * @param recordSeparator
     *            the line separator to use for output
     * @param nullToString 
     *            the String to use to write <code>null</code> values.
     * @param header
     *            the header
     * @throws IllegalArgumentException if the delimiter is a line break character
     */
    // package protected to give access without needing a synthetic accessor
    CSVFormat(final char delimiter, final Character quoteChar,
            final Quote quotePolicy, final Character commentStart,
            final Character escape, final boolean ignoreSurroundingSpaces,
            final boolean ignoreEmptyLines, final String recordSeparator,
            String nullToString, final String[] header) {
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
        this.recordSeparator = recordSeparator;
        this.nullToString = nullToString;
        this.header = header == null ? null : header.clone();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final CSVFormat other = (CSVFormat) obj;
        if (delimiter != other.delimiter) {
            return false;
        }
        if (quotePolicy != other.quotePolicy) {
            return false;
        }
        if (quoteChar == null) {
            if (other.quoteChar != null) {
                return false;
            }
        } else if (!quoteChar.equals(other.quoteChar)) {
            return false;
        }
        if (commentStart == null) {
            if (other.commentStart != null) {
                return false;
            }
        } else if (!commentStart.equals(other.commentStart)) {
            return false;
        }
        if (escape == null) {
            if (other.escape != null) {
                return false;
            }
        } else if (!escape.equals(other.escape)) {
            return false;
        }
        if (!Arrays.equals(header, other.header)) {
            return false;
        }
        if (ignoreSurroundingSpaces != other.ignoreSurroundingSpaces) {
            return false;
        }
        if (ignoreEmptyLines != other.ignoreEmptyLines) {
            return false;
        }
        if (recordSeparator == null) {
            if (other.recordSeparator != null) {
                return false;
            }
        } else if (!recordSeparator.equals(other.recordSeparator)) {
            return false;
        }
        return true;
    }

    /**
     * Formats the specified values.
     *
     * @param values
     *            the values to format
     * @return the formatted values
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

    /**
     * Returns the character marking the start of a line comment.
     *
     * @return the comment start marker.
     */
    public Character getCommentStart() {
        return commentStart;
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
     * Returns the escape character.
     *
     * @return the escape character
     */
    public Character getEscape() {
        return escape;
    }

    String[] getHeader() {
        return header;
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
     * Specifies whether spaces around values are ignored when parsing input.
     *
     * @return <tt>true</tt> if spaces around values are ignored, <tt>false</tt> if they are treated as part of the
     *         value.
     */
    public boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    /**
     * Returns the value to use for writing null values.
     *
     * @return the value to use for writing null values.
     */
    public String getNullToString() {
        return nullToString;
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
     * Returns the quote policy output fields.
     *
     * @return the quote policy
     */
    public Quote getQuotePolicy() {
        return quotePolicy;
    }

    /**
     * Returns the line separator delimiting output records.
     *
     * @return the line separator
     */
    public String getRecordSeparator() {
        return recordSeparator;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + delimiter;
        result = prime * result + ((quotePolicy == null) ? 0 : quotePolicy.hashCode());
        result = prime * result + ((quoteChar == null) ? 0 : quoteChar.hashCode());
        result = prime * result + ((commentStart == null) ? 0 : commentStart.hashCode());
        result = prime * result + ((escape == null) ? 0 : escape.hashCode());
        result = prime * result + (ignoreSurroundingSpaces ? 1231 : 1237);
        result = prime * result + (ignoreEmptyLines ? 1231 : 1237);
        result = prime * result + ((recordSeparator == null) ? 0 : recordSeparator.hashCode());
        result = prime * result + Arrays.hashCode(header);
        return result;
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
     * Returns whether escape are being processed.
     *
     * @return {@code true} if escapes are processed
     */
    public boolean isEscaping() {
        return escape != null;
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
     * Parses the specified content.
     *
     * @param in
     *            the input stream
     * @return a stream of CSVRecord
     * @throws IOException
     *             If an I/O error occurs
     */
    public Iterable<CSVRecord> parse(final Reader in) throws IOException {
        return new CSVParser(in, this);
    }

    /**
     * Creates a builder based on this format.
     *
     * @return a new builder
     */
    public CSVFormatBuilder toBuilder() {
        return new CSVFormatBuilder(this);
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
}
