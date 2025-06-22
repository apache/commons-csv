/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOStream;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.output.AppendableOutputStream;

/**
 * Specifies the format of a CSV file for parsing and writing.
 *
 * <h2>Using predefined formats</h2>
 *
 * <p>
 * You can use one of the predefined formats:
 * </p>
 *
 * <ul>
 * <li>{@link #DEFAULT}</li>
 * <li>{@link #EXCEL}</li>
 * <li>{@link #INFORMIX_UNLOAD}</li>
 * <li>{@link #INFORMIX_UNLOAD_CSV}</li>
 * <li>{@link #MONGODB_CSV}</li>
 * <li>{@link #MONGODB_TSV}</li>
 * <li>{@link #MYSQL}</li>
 * <li>{@link #ORACLE}</li>
 * <li>{@link #POSTGRESQL_CSV}</li>
 * <li>{@link #POSTGRESQL_TEXT}</li>
 * <li>{@link #RFC4180}</li>
 * <li>{@link #TDF}</li>
 * </ul>
 *
 * <p>
 * For example:
 * </p>
 *
 * <pre>
 * CSVParser parser = CSVFormat.EXCEL.parse(reader);
 * </pre>
 *
 * <p>
 * The {@link CSVParser} provides static methods to parse other input types, for example:
 * </p>
 *
 * <pre>
 * CSVParser parser = CSVParser.parse(file, StandardCharsets.US_ASCII, CSVFormat.EXCEL);
 * </pre>
 *
 * <h2>Defining formats</h2>
 *
 * <p>
 * You can extend a format by calling the {@code set} methods. For example:
 * </p>
 *
 * <pre>{@code
 * CSVFormat.EXCEL.builder().setNullString("N/A").setIgnoreSurroundingSpaces(true).get();
 * }</pre>
 *
 * <h2>Defining column names</h2>
 *
 * <p>
 * To define the column names you want to use to access records, write:
 * </p>
 *
 * <pre>{@code
 * CSVFormat.EXCEL.builder().setHeader("Col1", "Col2", "Col3").get();
 * }</pre>
 *
 * <p>
 * Calling {@link Builder#setHeader(String...)} lets you use the given names to address values in a {@link CSVRecord}, and assumes that your CSV source does not
 * contain a first record that also defines column names.
 *
 * If it does, then you are overriding this metadata with your names and you should skip the first record by calling
 * {@link Builder#setSkipHeaderRecord(boolean)} with {@code true}.
 * </p>
 *
 * <h2>Parsing</h2>
 *
 * <p>
 * You can use a format directly to parse a reader. For example, to parse an Excel file with columns header, write:
 * </p>
 *
 * <pre>{@code
 * Reader in = ...;
 * CSVFormat.EXCEL.builder().setHeader("Col1", "Col2", "Col3").get().parse(in);
 * }</pre>
 *
 * <p>
 * For other input types, like resources, files, and URLs, use the static methods on {@link CSVParser}.
 * </p>
 *
 * <h2>Referencing columns safely</h2>
 *
 * <p>
 * If your source contains a header record, you can simplify your code and safely reference columns, by using {@link Builder#setHeader(String...)} with no
 * arguments:
 * </p>
 *
 * <pre>
 * CSVFormat.EXCEL.builder().setHeader().get();
 * </pre>
 *
 * <p>
 * This causes the parser to read the first record and use its values as column names.
 *
 * Then, call one of the {@link CSVRecord} get method that takes a String column name argument:
 * </p>
 *
 * <pre>{@code
 * String value = record.get("Col1");
 * }</pre>
 *
 * <p>
 * This makes your code impervious to changes in column order in the CSV file.
 * </p>
 *
 * <h2>Serialization</h2>
 * <p>
 * This class implements the {@link Serializable} interface with the following caveats:
 * </p>
 * <ul>
 * <li>This class will no longer implement Serializable in 2.0.</li>
 * <li>Serialization is not supported from one version to the next.</li>
 * </ul>
 * <p>
 * The {@code serialVersionUID} values are:
 * </p>
 * <ul>
 * <li>Version 1.10.0: {@code 2L}</li>
 * <li>Version 1.9.0 through 1.0: {@code 1L}</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <p>
 * This class is immutable.
 * </p>
 * <p>
 * Not all settings are used for both parsing and writing.
 * </p>
 */
public final class CSVFormat implements Serializable {

    /**
     * Builds CSVFormat instances.
     *
     * @since 1.9.0
     */
    public static class Builder implements Supplier<CSVFormat> {

        /**
         * Creates a new default builder, as for {@link #RFC4180} but allowing empty lines.
         *
         * <p>
         * The {@link Builder} settings are:
         * </p>
         * <ul>
         * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
         * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
         * <li>{@link Builder#setRecordSeparator(String) setRecordSeparator}{@code ("\r\n")}</li>
         * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (true)}</li>
         * <li>{@link Builder#setDuplicateHeaderMode(DuplicateHeaderMode) setDuplicateHeaderMode}{@code (DuplicateHeaderMode.ALLOW_ALL)}</li>
         * <li>All other values take their Java defaults, {@code false} for booleans, {@code null} for object references.</li>
         * </ul>
         *
         * @see Predefined#Default
         * @see DuplicateHeaderMode#ALLOW_ALL
         *
         * @return a copy of the builder
         */
        public static Builder create() {
            // @formatter:off
            return new Builder()
                    .setDelimiter(Constants.COMMA)
                    .setQuote(Constants.DOUBLE_QUOTE_CHAR)
                    .setRecordSeparator(Constants.CRLF)
                    .setIgnoreEmptyLines(true)
                    .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL);
                    // @formatter:on
        }

        /**
         * Creates a new builder from the given format.
         *
         * @param csvFormat the source format.
         * @return a new builder.
         */
        public static Builder create(final CSVFormat csvFormat) {
            return new Builder(csvFormat);
        }

        private boolean allowMissingColumnNames;

        private boolean autoFlush;

        private Character commentMarker;

        private String delimiter;

        private DuplicateHeaderMode duplicateHeaderMode;

        private Character escapeCharacter;

        private String[] headerComments;

        private String[] headers;

        private boolean ignoreEmptyLines;

        private boolean ignoreHeaderCase;

        private boolean ignoreSurroundingSpaces;

        private String nullString;

        private Character quoteCharacter;

        private String quotedNullString;

        private QuoteMode quoteMode;

        private String recordSeparator;

        private boolean skipHeaderRecord;

        private boolean lenientEof;

        private boolean trailingData;

        private boolean trailingDelimiter;

        private boolean trim;

        /** The maximum number of rows to process, excluding the header row. */
        private long maxRows;

        private Builder() {
            // empty
        }

        private Builder(final CSVFormat csvFormat) {
            this.allowMissingColumnNames = csvFormat.allowMissingColumnNames;
            this.autoFlush = csvFormat.autoFlush;
            this.commentMarker = csvFormat.commentMarker;
            this.delimiter = csvFormat.delimiter;
            this.duplicateHeaderMode = csvFormat.duplicateHeaderMode;
            this.escapeCharacter = csvFormat.escapeCharacter;
            this.headerComments = csvFormat.headerComments;
            this.headers = csvFormat.headers;
            this.ignoreEmptyLines = csvFormat.ignoreEmptyLines;
            this.ignoreHeaderCase = csvFormat.ignoreHeaderCase;
            this.ignoreSurroundingSpaces = csvFormat.ignoreSurroundingSpaces;
            this.lenientEof = csvFormat.lenientEof;
            this.maxRows = csvFormat.maxRows;
            this.nullString = csvFormat.nullString;
            this.quoteCharacter = csvFormat.quoteCharacter;
            this.quoteMode = csvFormat.quoteMode;
            this.quotedNullString = csvFormat.quotedNullString;
            this.recordSeparator = csvFormat.recordSeparator;
            this.skipHeaderRecord = csvFormat.skipHeaderRecord;
            this.trailingData = csvFormat.trailingData;
            this.trailingDelimiter = csvFormat.trailingDelimiter;
            this.trim = csvFormat.trim;
        }

        /**
         * Builds a new CSVFormat instance.
         *
         * @return a new CSVFormat instance.
         * @deprecated Use {@link #get()}.
         */
        @Deprecated
        public CSVFormat build() {
            return get();
        }

        /**
         * Builds a new CSVFormat instance.
         *
         * @return a new CSVFormat instance.
         * @since 1.13.0
         */
        @Override
        public CSVFormat get() {
            return new CSVFormat(this);
        }

        /**
         * Sets the duplicate header names behavior, true to allow, false to disallow.
         *
         * @param allowDuplicateHeaderNames the duplicate header names behavior, true to allow, false to disallow.
         * @return This instance.
         * @deprecated Use {@link #setDuplicateHeaderMode(DuplicateHeaderMode)}.
         */
        @Deprecated
        public Builder setAllowDuplicateHeaderNames(final boolean allowDuplicateHeaderNames) {
            setDuplicateHeaderMode(allowDuplicateHeaderNames ? DuplicateHeaderMode.ALLOW_ALL : DuplicateHeaderMode.ALLOW_EMPTY);
            return this;
        }

        /**
         * Sets the parser missing column names behavior, {@code true} to allow missing column names in the header line, {@code false} to cause an
         * {@link IllegalArgumentException} to be thrown.
         *
         * @param allowMissingColumnNames the missing column names behavior, {@code true} to allow missing column names in the header line, {@code false} to
         *                                cause an {@link IllegalArgumentException} to be thrown.
         * @return This instance.
         */
        public Builder setAllowMissingColumnNames(final boolean allowMissingColumnNames) {
            this.allowMissingColumnNames = allowMissingColumnNames;
            return this;
        }

        /**
         * Sets whether to flush on close.
         *
         * @param autoFlush whether to flush on close.
         * @return This instance.
         */
        public Builder setAutoFlush(final boolean autoFlush) {
            this.autoFlush = autoFlush;
            return this;
        }

        /**
         * Sets the comment marker character, use {@code null} to disable comments.
         * <p>
         * The comment start character is only recognized at the start of a line.
         * </p>
         * <p>
         * Comments are printed first, before headers.
         * </p>
         * <p>
         * Use {@link #setCommentMarker(char)} or {@link #setCommentMarker(Character)} to set the comment marker written at the start of each comment line.
         * </p>
         * <p>
         * If the comment marker is not set, then the header comments are ignored.
         * </p>
         * <p>
         * For example:
         * </p>
         *
         * <pre>
         * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0));
         * </pre>
         * <p>
         * writes:
         * </p>
         *
         * <pre>
         * # Generated by Apache Commons CSV.
         * # 1970-01-01T00:00:00Z
         * </pre>
         *
         * @param commentMarker the comment start marker, use {@code null} to disable.
         * @return This instance.
         * @throws IllegalArgumentException thrown if the specified character is a line break
         */
        public Builder setCommentMarker(final char commentMarker) {
            setCommentMarker(Character.valueOf(commentMarker));
            return this;
        }

        /**
         * Sets the comment marker character, use {@code null} to disable comments.
         * <p>
         * The comment start character is only recognized at the start of a line.
         * </p>
         * <p>
         * Comments are printed first, before headers.
         * </p>
         * <p>
         * Use {@link #setCommentMarker(char)} or {@link #setCommentMarker(Character)} to set the comment marker written at the start of each comment line.
         * </p>
         * <p>
         * If the comment marker is not set, then the header comments are ignored.
         * </p>
         * <p>
         * For example:
         * </p>
         *
         * <pre>
         * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0));
         * </pre>
         * <p>
         * writes:
         * </p>
         *
         * <pre>
         * # Generated by Apache Commons CSV.
         * # 1970-01-01T00:00:00Z
         * </pre>
         *
         * @param commentMarker the comment start marker, use {@code null} to disable.
         * @return This instance.
         * @throws IllegalArgumentException thrown if the specified character is a line break
         */
        public Builder setCommentMarker(final Character commentMarker) {
            if (isLineBreak(commentMarker)) {
                throw new IllegalArgumentException("The comment start marker character cannot be a line break");
            }
            this.commentMarker = commentMarker;
            return this;
        }

        /**
         * Sets the delimiter character.
         *
         * @param delimiter the delimiter character.
         * @return This instance.
         */
        public Builder setDelimiter(final char delimiter) {
            return setDelimiter(String.valueOf(delimiter));
        }

        /**
         * Sets the delimiter character.
         *
         * @param delimiter the delimiter character.
         * @return This instance.
         */
        public Builder setDelimiter(final String delimiter) {
            if (containsLineBreak(delimiter)) {
                throw new IllegalArgumentException("The delimiter cannot be a line break");
            }
            if (delimiter.isEmpty()) {
                throw new IllegalArgumentException("The delimiter cannot be empty");
            }
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Sets the duplicate header names behavior.
         *
         * @param duplicateHeaderMode the duplicate header names behavior
         * @return This instance.
         * @since 1.10.0
         */
        public Builder setDuplicateHeaderMode(final DuplicateHeaderMode duplicateHeaderMode) {
            this.duplicateHeaderMode = Objects.requireNonNull(duplicateHeaderMode, "duplicateHeaderMode");
            return this;
        }

        /**
         * Sets the escape character.
         *
         * @param escapeCharacter the escape character.
         * @return This instance.
         * @throws IllegalArgumentException thrown if the specified character is a line break
         */
        public Builder setEscape(final char escapeCharacter) {
            setEscape(Character.valueOf(escapeCharacter));
            return this;
        }

        /**
         * Sets the escape character.
         *
         * @param escapeCharacter the escape character.
         * @return This instance.
         * @throws IllegalArgumentException thrown if the specified character is a line break
         */
        public Builder setEscape(final Character escapeCharacter) {
            if (isLineBreak(escapeCharacter)) {
                throw new IllegalArgumentException("The escape character cannot be a line break");
            }
            this.escapeCharacter = escapeCharacter;
            return this;
        }

        /**
         * Sets the header defined by the given {@link Enum} class.
         *
         * <p>
         * Example:
         * </p>
         *
         * <pre>
         * public enum HeaderEnum {
         *     Name, Email, Phone
         * }
         *
         * Builder builder = builder.setHeader(HeaderEnum.class);
         * </pre>
         * <p>
         * The header is also used by the {@link CSVPrinter}.
         * </p>
         *
         * @param headerEnum the enum defining the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
         * @return This instance.
         */
        public Builder setHeader(final Class<? extends Enum<?>> headerEnum) {
            String[] header = null;
            if (headerEnum != null) {
                final Enum<?>[] enumValues = headerEnum.getEnumConstants();
                header = new String[enumValues.length];
                Arrays.setAll(header, i -> enumValues[i].name());
            }
            return setHeader(header);
        }

        /**
         * Sets the header from the result set metadata. The header can be parsed automatically from the input file with:
         *
         * <pre>
         * builder.setHeader();
         * </pre>
         *
         * or specified manually with:
         *
         * <pre>
         * builder.setHeader(resultSet);
         * </pre>
         * <p>
         * The header is also used by the {@link CSVPrinter}.
         * </p>
         *
         * @param resultSet the resultSet for the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
         * @return This instance.
         * @throws SQLException SQLException if a database access error occurs or this method is called on a closed result set.
         */
        public Builder setHeader(final ResultSet resultSet) throws SQLException {
            return setHeader(resultSet != null ? resultSet.getMetaData() : null);
        }

        /**
         * Sets the header from the result set metadata. The header can be parsed automatically from the input file with:
         *
         * <pre>
         * builder.setHeader();
         * </pre>
         *
         * or specified manually with:
         *
         * <pre>
         * builder.setHeader(resultSetMetaData);
         * </pre>
         * <p>
         * The header is also used by the {@link CSVPrinter}.
         * </p>
         *
         * @param resultSetMetaData the metaData for the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
         * @return This instance.
         * @throws SQLException SQLException if a database access error occurs or this method is called on a closed result set.
         */
        public Builder setHeader(final ResultSetMetaData resultSetMetaData) throws SQLException {
            String[] labels = null;
            if (resultSetMetaData != null) {
                final int columnCount = resultSetMetaData.getColumnCount();
                labels = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    labels[i] = resultSetMetaData.getColumnLabel(i + 1);
                }
            }
            return setHeader(labels);
        }

        /**
         * Sets the header to the given values. The header can be parsed automatically from the input file with:
         *
         * <pre>
         * builder.setHeader();
         * </pre>
         *
         * or specified manually with:
         *
         * <pre>{@code
         * builder.setHeader("name", "email", "phone");
         * }</pre>
         * <p>
         * The header is also used by the {@link CSVPrinter}.
         * </p>
         * <p>
         * This method keeps a copy of the input array.
         * </p>
         * @param header the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
         * @return This instance.
         */
        public Builder setHeader(final String... header) {
            this.headers = CSVFormat.clone(header);
            return this;
        }

        /**
         * Sets the header comments to write before the CSV data.
         * <p>
         * This setting is ignored by the parser.
         * </p>
         * <p>
         * Comments are printed first, before headers.
         * </p>
         * <p>
         * Use {@link #setCommentMarker(char)} or {@link #setCommentMarker(Character)} to set the comment marker written at the start of each comment line.
         * </p>
         * <p>
         * If the comment marker is not set, then the header comments are ignored.
         * </p>
         * <p>
         * For example:
         * </p>
         *
         * <pre>
         * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0));
         * </pre>
         * <p>
         * writes:
         * </p>
         *
         * <pre>
         * # Generated by Apache Commons CSV.
         * # 1970-01-01T00:00:00Z
         * </pre>
         * <p>
         * This method keeps a copy of the input array.
         * </p>
         *
         * @param headerComments the headerComments which will be printed by the Printer before the CSV data.
         * @return This instance.
         */
        public Builder setHeaderComments(final Object... headerComments) {
            this.headerComments = CSVFormat.clone(toStringArray(headerComments));
            return this;
        }

        /**
         * Sets the header comments to write before the CSV data.
         * <p>
         * This setting is ignored by the parser.
         * </p>
         * <p>
         * Comments are printed first, before headers.
         * </p>
         * <p>
         * Use {@link #setCommentMarker(char)} or {@link #setCommentMarker(Character)} to set the comment marker written at the start of each comment line.
         * </p>
         * <p>
         * If the comment marker is not set, then the header comments are ignored.
         * </p>
         * <p>
         * For example:
         * </p>
         *
         * <pre>
         * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0).toString());
         * </pre>
         * <p>
         * writes:
         * </p>
         *
         * <pre>
         * # Generated by Apache Commons CSV.
         * # 1970-01-01T00:00:00Z
         * </pre>
         * <p>
         * This method keeps a copy of the input array.
         * </p>
         * @param headerComments the headerComments which will be printed by the Printer before the CSV data.
         * @return This instance.
         */
        public Builder setHeaderComments(final String... headerComments) {
            this.headerComments = CSVFormat.clone(headerComments);
            return this;
        }

        /**
         * Sets the empty line skipping behavior, {@code true} to ignore the empty lines between the records, {@code false} to translate empty lines to empty
         * records.
         *
         * @param ignoreEmptyLines the empty line skipping behavior, {@code true} to ignore the empty lines between the records, {@code false} to translate
         *                         empty lines to empty records.
         * @return This instance.
         */
        public Builder setIgnoreEmptyLines(final boolean ignoreEmptyLines) {
            this.ignoreEmptyLines = ignoreEmptyLines;
            return this;
        }

        /**
         * Sets the parser case mapping behavior, {@code true} to access name/values, {@code false} to leave the mapping as is.
         *
         * @param ignoreHeaderCase the case mapping behavior, {@code true} to access name/values, {@code false} to leave the mapping as is.
         * @return This instance.
         */
        public Builder setIgnoreHeaderCase(final boolean ignoreHeaderCase) {
            this.ignoreHeaderCase = ignoreHeaderCase;
            return this;
        }

        /**
         * Sets the parser trimming behavior, {@code true} to remove the surrounding spaces, {@code false} to leave the spaces as is.
         *
         * @param ignoreSurroundingSpaces the parser trimming behavior, {@code true} to remove the surrounding spaces, {@code false} to leave the spaces as is.
         * @return This instance.
         */
        public Builder setIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
            this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
            return this;
        }

        /**
         * Sets whether reading end-of-file is allowed even when input is malformed, helps Excel compatibility.
         *
         * @param lenientEof whether reading end-of-file is allowed even when input is malformed, helps Excel compatibility.
         * @return This instance.
         * @since 1.11.0
         */
        public Builder setLenientEof(final boolean lenientEof) {
            this.lenientEof = lenientEof;
            return this;
        }

        /**
         * Sets the maximum number of rows to process, excluding the header row.
         * <p>
         * Values less than or equal to 0 mean no limit.
         * </p>
         *
         * @param maxRows the maximum number of rows to process, excluding the header row.
         * @return This instance.
         * @since 1.14.0
         */
        public Builder setMaxRows(final long maxRows) {
            this.maxRows = maxRows;
            return this;
        }

        /**
         * Sets the String to convert to and from {@code null}. No substitution occurs if {@code null}.
         *
         * <ul>
         * <li><strong>Reading:</strong> Converts strings equal to the given {@code nullString} to {@code null} when reading records.</li>
         * <li><strong>Writing:</strong> Writes {@code null} as the given {@code nullString} when writing records.</li>
         * </ul>
         *
         * @param nullString the String to convert to and from {@code null}. No substitution occurs if {@code null}.
         * @return This instance.
         */
        public Builder setNullString(final String nullString) {
            this.nullString = nullString;
            this.quotedNullString = quoteCharacter + nullString + quoteCharacter;
            return this;
        }

        /**
         * Sets the quote character.
         *
         * @param quoteCharacter the quote character.
         * @return This instance.
         */
        public Builder setQuote(final char quoteCharacter) {
            setQuote(Character.valueOf(quoteCharacter));
            return this;
        }

        /**
         * Sets the quote character, use {@code null} to disable.
         *
         * @param quoteCharacter the quote character, use {@code null} to disable.
         * @return This instance.
         */
        public Builder setQuote(final Character quoteCharacter) {
            if (isLineBreak(quoteCharacter)) {
                throw new IllegalArgumentException("The quoteCharacter cannot be a line break");
            }
            this.quoteCharacter = quoteCharacter;
            return this;
        }

        /**
         * Sets the quote policy to use for output.
         *
         * @param quoteMode the quote policy to use for output.
         * @return This instance.
         */
        public Builder setQuoteMode(final QuoteMode quoteMode) {
            this.quoteMode = quoteMode;
            return this;
        }

        /**
         * Sets the record separator to use for output.
         *
         * <p>
         * <strong>Note:</strong> This setting is only used during printing and does not affect parsing. Parsing currently only works for inputs with '\n', '\r'
         * and "\r\n"
         * </p>
         *
         * @param recordSeparator the record separator to use for output.
         * @return This instance.
         */
        public Builder setRecordSeparator(final char recordSeparator) {
            this.recordSeparator = String.valueOf(recordSeparator);
            return this;
        }

        /**
         * Sets the record separator to use for output.
         *
         * <p>
         * <strong>Note:</strong> This setting is only used during printing and does not affect parsing. Parsing currently only works for inputs with '\n', '\r'
         * and "\r\n"
         * </p>
         *
         * @param recordSeparator the record separator to use for output.
         * @return This instance.
         */
        public Builder setRecordSeparator(final String recordSeparator) {
            this.recordSeparator = recordSeparator;
            return this;
        }

        /**
         * Sets whether to skip the header record.
         *
         * @param skipHeaderRecord whether to skip the header record.
         * @return This instance.
         */
        public Builder setSkipHeaderRecord(final boolean skipHeaderRecord) {
            this.skipHeaderRecord = skipHeaderRecord;
            return this;
        }

        /**
         * Sets whether reading trailing data is allowed in records, helps Excel compatibility.
         *
         * @param trailingData whether reading trailing data is allowed in records, helps Excel compatibility.
         * @return This instance.
         * @since 1.11.0
         */
        public Builder setTrailingData(final boolean trailingData) {
            this.trailingData = trailingData;
            return this;
        }

        /**
         * Sets whether to add a trailing delimiter.
         *
         * @param trailingDelimiter whether to add a trailing delimiter.
         * @return This instance.
         */
        public Builder setTrailingDelimiter(final boolean trailingDelimiter) {
            this.trailingDelimiter = trailingDelimiter;
            return this;
        }


        /**
         * Sets whether to trim leading and trailing blanks.
         *
         * @param trim whether to trim leading and trailing blanks.
         * @return This instance.
         */
        public Builder setTrim(final boolean trim) {
            this.trim = trim;
            return this;
        }
    }

    /**
     * Predefines formats.
     *
     * @since 1.2
     */
    public enum Predefined {

        /**
         * The DEFAULT predefined format.
         *
         * @see CSVFormat#DEFAULT
         */
        Default(DEFAULT),

        /**
         * The EXCEL predefined format.
         *
         * @see CSVFormat#EXCEL
         */
        Excel(EXCEL),

        /**
         * The INFORMIX_UNLOAD predefined format.
         *
         * @see CSVFormat#INFORMIX_UNLOAD
         * @since 1.3
         */
        InformixUnload(INFORMIX_UNLOAD),

        /**
         * The INFORMIX_UNLOAD_CSV predefined format.
         *
         * @see CSVFormat#INFORMIX_UNLOAD_CSV
         * @since 1.3
         */
        InformixUnloadCsv(INFORMIX_UNLOAD_CSV),

        /**
         * The MONGODB_CSV predefined format.
         *
         * @see CSVFormat#MONGODB_CSV
         * @since 1.7
         */
        MongoDBCsv(MONGODB_CSV),

        /**
         * The MONGODB_TSV predefined format.
         *
         * @see CSVFormat#MONGODB_TSV
         * @since 1.7
         */
        MongoDBTsv(MONGODB_TSV),

        /**
         * The MYSQL predefined format.
         *
         * @see CSVFormat#MYSQL
         */
        MySQL(MYSQL),

        /**
         * The ORACLE predefined format.
         *
         * @see CSVFormat#ORACLE
         */
        Oracle(ORACLE),

        /**
         * The POSTGRESQL_CSV predefined format.
         *
         * @see CSVFormat#POSTGRESQL_CSV
         * @since 1.5
         */
        PostgreSQLCsv(POSTGRESQL_CSV),

        /**
         * The POSTGRESQL_TEXT predefined format.
         *
         * @see CSVFormat#POSTGRESQL_TEXT
         */
        PostgreSQLText(POSTGRESQL_TEXT),

        /**
         * The RFC4180 predefined format.
         *
         * @see CSVFormat#RFC4180
         */
        RFC4180(CSVFormat.RFC4180),

        /**
         * The TDF predefined format.
         *
         * @see CSVFormat#TDF
         */
        TDF(CSVFormat.TDF);

        private final CSVFormat format;

        Predefined(final CSVFormat format) {
            this.format = format;
        }

        /**
         * Gets the format.
         *
         * @return the format.
         */
        public CSVFormat getFormat() {
            return format;
        }
    }

    /**
     * Standard Comma Separated Value format, as for {@link #RFC4180} but allowing empty lines.
     *
     * <p>
     * The {@link Builder} settings are:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setRecordSeparator(String) setRecordSeparator}{@code ("\r\n")}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (true)}</li>
     * <li>{@link Builder#setDuplicateHeaderMode(DuplicateHeaderMode) setDuplicateHeaderMode}{@code (DuplicateHeaderMode.ALLOW_ALL)}</li>
     * </ul>
     *
     * @see Predefined#Default
     * @see DuplicateHeaderMode#ALLOW_ALL
     */
    public static final CSVFormat DEFAULT = new CSVFormat(Builder.create());

    /**
     * <a href="https://support.microsoft.com/en-us/office/import-or-export-text-txt-or-csv-files-5250ac4c-663c-47ce-937b-339e391393ba">Microsoft Excel</a> file
     * format (using a comma as the value delimiter). Note that the actual value delimiter used by Excel is locale-dependent, it might be necessary to customize
     * this format to accommodate your regional settings.
     *
     * <p>
     * For example for parsing or generating a CSV file on a French system the following format will be used:
     * </p>
     *
     * <pre>
     * CSVFormat format = CSVFormat.EXCEL.builder().setDelimiter(';').get();
     * </pre>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setRecordSeparator(String) setRecordSeparator}{@code ("\r\n")}</li>
     * <li>{@link Builder#setDuplicateHeaderMode(DuplicateHeaderMode) setDuplicateHeaderMode}{@code (DuplicateHeaderMode.ALLOW_ALL)}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * <li>{@link Builder#setAllowMissingColumnNames(boolean) setAllowMissingColumnNames}{@code (true)}</li>
     * <li>{@link Builder#setTrailingData(boolean) setTrailingData}{@code (true)}</li>
     * <li>{@link Builder#setLenientEof(boolean) setLenientEof}{@code (true)}</li>
     * </ul>
     * <p>
     * Note: This is currently like {@link #RFC4180} plus {@link Builder#setAllowMissingColumnNames(boolean) Builder#setAllowMissingColumnNames(true)} and
     * {@link Builder#setIgnoreEmptyLines(boolean) Builder#setIgnoreEmptyLines(false)}.
     * </p>
     *
     * @see Predefined#Excel
     * @see DuplicateHeaderMode#ALLOW_ALL
     * @see <a href="https://support.microsoft.com/en-us/office/import-or-export-text-txt-or-csv-files-5250ac4c-663c-47ce-937b-339e391393ba">Microsoft Excel
     *      </a>
     */
    // @formatter:off
    public static final CSVFormat EXCEL = DEFAULT.builder()
            .setIgnoreEmptyLines(false)
            .setAllowMissingColumnNames(true)
            .setTrailingData(true)
            .setLenientEof(true)
            .get();
    // @formatter:on

    /**
     * Default <a href="https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-unload-statement">Informix CSV UNLOAD</a>
     * format used by the {@code UNLOAD TO file_name} operation.
     *
     * <p>
     * This is a comma-delimited format with an LF character as the line separator. Values are not quoted and special characters are escaped with {@code '\'}.
     * The default NULL string is {@code "\\N"}.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('\\')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('\"')}</li>
     * <li>{@link Builder#setRecordSeparator(char) setRecordSeparator}{@code ('\n')}</li>
     * </ul>
     *
     * @see Predefined#MySQL
     * @see <a href="https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-unload-statement">Informix CSV UNLOAD</a>
     * @since 1.3
     */
    // @formatter:off
    public static final CSVFormat INFORMIX_UNLOAD = DEFAULT.builder()
            .setDelimiter(Constants.PIPE)
            .setEscape(Constants.BACKSLASH)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(Constants.LF)
            .get();
    // @formatter:on

    /**
     * Default <a href="https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-unload-statement">Informix CSV UNLOAD</a>
     * format used by the {@code UNLOAD TO file_name} operation (escaping is disabled.)
     *
     * <p>
     * This is a comma-delimited format with an LF character as the line separator. Values are not quoted and special characters are escaped with {@code '\'}.
     * The default NULL string is {@code "\\N"}.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('\"')}</li>
     * <li>{@link Builder#setRecordSeparator(char) setRecordSeparator}{@code ('\n')}</li>
     * </ul>
     *
     * @see Predefined#MySQL
     * @see <a href= "http://www.ibm.com/support/knowledgecenter/SSBJG3_2.5.0/com.ibm.gen_busug.doc/c_fgl_InOutSql_UNLOAD.htm">
     *      http://www.ibm.com/support/knowledgecenter/SSBJG3_2.5.0/com.ibm.gen_busug.doc/c_fgl_InOutSql_UNLOAD.htm</a>
     * @since 1.3
     */
    // @formatter:off
    public static final CSVFormat INFORMIX_UNLOAD_CSV = DEFAULT.builder()
            .setDelimiter(Constants.COMMA)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(Constants.LF)
            .get();
    // @formatter:on

    /**
     * Default MongoDB CSV format used by the {@code mongoexport} operation.
     * <p>
     * <strong>Parsing is not supported yet.</strong>
     * </p>
     *
     * <p>
     * This is a comma-delimited format. Values are double quoted only if needed and special characters are escaped with {@code '"'}. A header line with field
     * names is expected.
     * </p>
     * <p>
     * As of 2024-04-05, the MongoDB documentation for {@code mongoimport} states:
     * </p>
     * <blockquote>The csv parser accepts that data that complies with RFC <a href="https://tools.ietf.org/html/4180">RFC-4180</a>. As a result, backslashes are
     * not a valid escape character. If you use double-quotes to enclose fields in the CSV data, you must escape internal double-quote marks by prepending
     * another double-quote. </blockquote>
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('"')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.MINIMAL)}</li>
     * </ul>
     *
     * @see Predefined#MongoDBCsv
     * @see QuoteMode#ALL_NON_NULL
     * @see <a href="https://docs.mongodb.com/manual/reference/program/mongoexport/">MongoDB mongoexport command documentation</a>
     * @since 1.7
     */
    // @formatter:off
    public static final CSVFormat MONGODB_CSV = DEFAULT.builder()
            .setDelimiter(Constants.COMMA)
            .setEscape(Constants.DOUBLE_QUOTE_CHAR)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setQuoteMode(QuoteMode.MINIMAL)
            .get();
    // @formatter:off

    /**
     * Default MongoDB TSV format used by the {@code mongoexport} operation.
     * <p>
     * <strong>Parsing is not supported yet.</strong>
     * </p>
     *
     * <p>
     * This is a tab-delimited format. Values are double quoted only if needed and special
     * characters are escaped with {@code '"'}. A header line with field names is expected.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code ('\t')}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('"')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.MINIMAL)}</li>
     * <li>{@link Builder#setSkipHeaderRecord(boolean) setSkipHeaderRecord}{@code (false)}</li>
     * </ul>
     *
     * @see Predefined#MongoDBCsv
     * @see QuoteMode#ALL_NON_NULL
     * @see <a href="https://docs.mongodb.com/manual/reference/program/mongoexport/">MongoDB mongoexport command
     *          documentation</a>
     * @since 1.7
     */
    // @formatter:off
    public static final CSVFormat MONGODB_TSV = DEFAULT.builder()
            .setDelimiter(Constants.TAB)
            .setEscape(Constants.DOUBLE_QUOTE_CHAR)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setQuoteMode(QuoteMode.MINIMAL)
            .setSkipHeaderRecord(false)
            .get();
    // @formatter:off

    /**
     * Default <a href="https://dev.mysql.com/doc/refman/8.0/en/mysqldump-delimited-text.html">MySQL</a>
     * format used by the {@code SELECT INTO OUTFILE} and {@code LOAD DATA INFILE} operations.
     *
     * <p>
     * This is a tab-delimited format with an LF character as the line separator. Values are not quoted and special
     * characters are escaped with {@code '\'}. The default NULL string is {@code "\\N"}.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code ('\t')}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('\\')}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * <li>{@link Builder#setQuote(Character) setQuote}{@code (null)}</li>
     * <li>{@link Builder#setRecordSeparator(char) setRecordSeparator}{@code ('\n')}</li>
     * <li>{@link Builder#setNullString(String) setNullString}{@code ("\\N")}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.ALL_NON_NULL)}</li>
     * </ul>
     *
     * @see Predefined#MySQL
     * @see QuoteMode#ALL_NON_NULL
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mysqldump-delimited-text.html">MySQL</a>
     */
    // @formatter:off
    public static final CSVFormat MYSQL = DEFAULT.builder()
            .setDelimiter(Constants.TAB)
            .setEscape(Constants.BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(null)
            .setRecordSeparator(Constants.LF)
            .setNullString(Constants.SQL_NULL_STRING)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .get();
    // @formatter:off

    /**
     * Default
     * <a href="https://docs.oracle.com/en/database/oracle/oracle-database/23/sutil/oracle-sql-loader-control-file-contents.html#GUID-D1762699-8154-40F6-90DE-EFB8EB6A9AB0">Oracle</a>
     * format used by the SQL*Loader utility.
     *
     * <p>
     * This is a comma-delimited format with the system line separator character as the record separator. Values are
     * double quoted when needed and special characters are escaped with {@code '"'}. The default NULL string is
     * {@code ""}. Values are trimmed.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')} // default is {@code FIELDS TERMINATED BY ','}}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('\\')}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')} // default is {@code OPTIONALLY ENCLOSED BY '"'}}</li>
     * <li>{@link Builder#setNullString(String) setNullString}{@code ("\\N")}</li>
     * <li>{@link Builder#setTrim(boolean) setTrim}{@code (true)}</li>
     * <li>{@link Builder#setRecordSeparator(String) setRecordSeparator}{@code (System.lineSeparator())}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.MINIMAL)}</li>
     * </ul>
     *
     * @see Predefined#Oracle
     * @see QuoteMode#MINIMAL
     * @see <a href="https://docs.oracle.com/en/database/oracle/oracle-database/23/sutil/oracle-sql-loader-control-file-contents.html#GUID-D1762699-8154-40F6-90DE-EFB8EB6A9AB0">Oracle CSV Format Specification</a>
     * @since 1.6
     */
    // @formatter:off
    public static final CSVFormat ORACLE = DEFAULT.builder()
            .setDelimiter(Constants.COMMA)
            .setEscape(Constants.BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setNullString(Constants.SQL_NULL_STRING)
            .setTrim(true)
            .setRecordSeparator(System.lineSeparator())
            .setQuoteMode(QuoteMode.MINIMAL)
            .get();
    // @formatter:off

    /**
     * Default <a href="https://www.postgresql.org/docs/current/static/sql-copy.html">PostgreSQL CSV</a> format used by the {@code COPY} operation.
     *
     * <p>
     * This is a comma-delimited format with an LF character as the line separator. Values are double quoted and special
     * characters are not escaped. The default NULL string is {@code ""}.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setEscape(Character) setEscape}{@code (null)}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setRecordSeparator(char) setRecordSeparator}{@code ('\n')}</li>
     * <li>{@link Builder#setNullString(String) setNullString}{@code ("")}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.ALL_NON_NULL)}</li>
     * </ul>
     *
     * @see Predefined#MySQL
     * @see QuoteMode#ALL_NON_NULL
     * @see <a href="https://www.postgresql.org/docs/current/static/sql-copy.html">PostgreSQL CSV</a>
     * @since 1.5
     */
    // @formatter:off
    public static final CSVFormat POSTGRESQL_CSV = DEFAULT.builder()
            .setDelimiter(Constants.COMMA)
            .setEscape(null)
            .setIgnoreEmptyLines(false)
            .setQuote(Constants.DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(Constants.LF)
            .setNullString(Constants.EMPTY)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .get();
    // @formatter:off

    /**
     * Default <a href="https://www.postgresql.org/docs/current/static/sql-copy.html">PostgreSQL Text</a> format used by the {@code COPY} operation.
     *
     * <p>
     * This is a tab-delimited format with an LF character as the line separator. Values are not quoted and special
     * characters are escaped with {@code '\\'}. The default NULL string is {@code "\\N"}.
     * </p>
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code ('\t')}</li>
     * <li>{@link Builder#setEscape(char) setEscape}{@code ('\\')}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * <li>{@link Builder#setQuote(Character) setQuote}{@code (null)}</li>
     * <li>{@link Builder#setRecordSeparator(char) setRecordSeparator}{@code ('\n')}</li>
     * <li>{@link Builder#setNullString(String) setNullString}{@code ("\\N")}</li>
     * <li>{@link Builder#setQuoteMode(QuoteMode) setQuoteMode}{@code (QuoteMode.ALL_NON_NULL)}</li>
     * </ul>
     *
     * @see Predefined#MySQL
     * @see QuoteMode#ALL_NON_NULL
     * @see <a href="https://www.postgresql.org/docs/current/static/sql-copy.html">PostgreSQL Text</a>
     * @since 1.5
     */
    // @formatter:off
    public static final CSVFormat POSTGRESQL_TEXT = DEFAULT.builder()
            .setDelimiter(Constants.TAB)
            .setEscape(Constants.BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(null)
            .setRecordSeparator(Constants.LF)
            .setNullString(Constants.SQL_NULL_STRING)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .get();
    // @formatter:off

    /**
     * Comma separated format as defined by <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>.
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code (',')}</li>
     * <li>{@link Builder#setQuote(char) setQuote}{@code ('"')}</li>
     * <li>{@link Builder#setRecordSeparator(String) setRecordSeparator}{@code ("\r\n")}</li>
     * <li>{@link Builder#setIgnoreEmptyLines(boolean) setIgnoreEmptyLines}{@code (false)}</li>
     * </ul>
     *
     * @see Predefined#RFC4180
     * @see <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>
     */
    public static final CSVFormat RFC4180 = DEFAULT.builder().setIgnoreEmptyLines(false).get();

    private static final long serialVersionUID = 2L;

    /**
     * Tab-delimited format (<a href="https://en.wikipedia.org/wiki/Tab-separated_values">TDF</a>).
     *
     * <p>
     * The {@link Builder} settings are the {@link #DEFAULT} <em>with</em>:
     * </p>
     * <ul>
     * <li>{@link Builder#setDelimiter(char) setDelimiter}{@code ('\t')}</li>
     * <li>{@link Builder#setIgnoreSurroundingSpaces(boolean) setIgnoreSurroundingSpaces}{@code (true)}</li>
     * </ul>
     *
     * @see Predefined#TDF
     * @see <a href="https://en.wikipedia.org/wiki/Tab-separated_values">TDF</a>
     */
    // @formatter:off
    public static final CSVFormat TDF = DEFAULT.builder()
            .setDelimiter(Constants.TAB)
            .setIgnoreSurroundingSpaces(true)
            .get();
    // @formatter:on

    /**
     * Null-safe clone of an array.
     *
     * @param <T>    The array element type.
     * @param values the source array
     * @return the cloned array.
     */
    @SafeVarargs
    static <T> T[] clone(final T... values) {
        return values == null ? null : values.clone();
    }

    /**
     * Returns true if the given string contains the search char.
     *
     * @param source   the string to check.
     * @param searchCh the character to search.
     * @return true if {@code c} contains a line break character
     */
    private static boolean contains(final String source, final char searchCh) {
        return Objects.requireNonNull(source, "source").indexOf(searchCh) >= 0;
    }

    /**
     * Returns true if the given string contains a line break character.
     *
     * @param source the string to check.
     * @return true if {@code c} contains a line break character.
     */
    private static boolean containsLineBreak(final String source) {
        return contains(source, Constants.CR) || contains(source, Constants.LF);
    }

    /**
     * Creates a null-safe copy of the given instance.
     *
     * @return a copy of the given instance or null if the input is null.
     */
    static CSVFormat copy(final CSVFormat format) {
        return format != null ? format.copy() : null;
    }

    static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns true if the given character is a line break character.
     *
     * @param c the character to check.
     * @return true if {@code c} is a line break character.
     */
    private static boolean isLineBreak(final char c) {
        return c == Constants.LF || c == Constants.CR;
    }

    /**
     * Returns true if the given character is a line break character.
     *
     * @param c the character to check, may be null.
     * @return true if {@code c} is a line break character (and not null).
     */
    private static boolean isLineBreak(final Character c) {
        return c != null && isLineBreak(c.charValue()); // Explicit (un)boxing is intentional
    }

    /** Same test as in as {@link String#trim()}. */
    private static boolean isTrimChar(final char ch) {
        return ch <= Constants.SP;
    }

    /** Same test as in as {@link String#trim()}. */
    private static boolean isTrimChar(final CharSequence charSequence, final int pos) {
        return isTrimChar(charSequence.charAt(pos));
    }

    /**
     * Creates a new CSV format with the specified delimiter.
     *
     * <p>
     * Use this method if you want to create a CSVFormat from scratch. All fields but the delimiter will be initialized with null/false.
     * </p>
     *
     * @param delimiter the char used for value separation, must not be a line break character
     * @return a new CSV format.
     * @throws IllegalArgumentException if the delimiter is a line break character
     * @see #DEFAULT
     * @see #RFC4180
     * @see #MYSQL
     * @see #EXCEL
     * @see #TDF
     */
    public static CSVFormat newFormat(final char delimiter) {
        return new CSVFormat(new Builder().setDelimiter(delimiter));
    }

    static String[] toStringArray(final Object[] values) {
        if (values == null) {
            return null;
        }
        final String[] strings = new String[values.length];
        Arrays.setAll(strings, i -> Objects.toString(values[i], null));
        return strings;
    }

    static CharSequence trim(final CharSequence charSequence) {
        if (charSequence instanceof String) {
            return ((String) charSequence).trim();
        }
        final int count = charSequence.length();
        int len = count;
        int pos = 0;

        while (pos < len && isTrimChar(charSequence, pos)) {
            pos++;
        }
        while (pos < len && isTrimChar(charSequence, len - 1)) {
            len--;
        }
        return pos > 0 || len < count ? charSequence.subSequence(pos, len) : charSequence;
    }

    /**
     * Gets one of the predefined formats from {@link CSVFormat.Predefined}.
     *
     * @param format name
     * @return one of the predefined formats
     * @since 1.2
     */
    public static CSVFormat valueOf(final String format) {
        return CSVFormat.Predefined.valueOf(format).getFormat();
    }

    /** How duplicate headers are handled. */
    private final DuplicateHeaderMode duplicateHeaderMode;

    /** Whether missing column names are allowed when parsing the header line. */
    private final boolean allowMissingColumnNames;

    /** Whether to flush on close. */
    private final boolean autoFlush;

    /** Set to null if commenting is disabled. */
    private final Character commentMarker;

    /** The character delimiting the values (typically ";", "," or "\t"). */
    private final String delimiter;

    /** Set to null if escaping is disabled. */
    private final Character escapeCharacter;

    /** Array of header column names. */
    private final String[] headers;

    /** Array of header comment lines. */
    private final String[] headerComments;

    /** Whether empty lines between records are ignored when parsing input. */
    private final boolean ignoreEmptyLines;

    /** Should ignore header names case. */
    private final boolean ignoreHeaderCase;

    /** Should leading/trailing spaces be ignored around values?. */
    private final boolean ignoreSurroundingSpaces;

    /** The string to be used for null values. */
    private final String nullString;

    /** Set to null if quoting is disabled. */
    private final Character quoteCharacter;

    /** Set to {@code quoteCharacter + nullString + quoteCharacter} */
    private final String quotedNullString;

    /** The quote policy output fields. */
    private final QuoteMode quoteMode;

    /** For output. */
    private final String recordSeparator;

    /** Whether to skip the header record. */
    private final boolean skipHeaderRecord;

    /** Whether reading end-of-file is allowed even when input is malformed, helps Excel compatibility. */
    private final boolean lenientEof;

    /** Whether reading trailing data is allowed in records, helps Excel compatibility. */
    private final boolean trailingData;

    /** Whether to add a trailing delimiter. */
    private final boolean trailingDelimiter;

    /** Whether to trim leading and trailing blanks. */
    private final boolean trim;

    /** The maximum number of rows to process, excluding the header row. */
    private final long maxRows;

    private CSVFormat(final Builder builder) {
        this.allowMissingColumnNames = builder.allowMissingColumnNames;
        this.autoFlush = builder.autoFlush;
        this.commentMarker = builder.commentMarker;
        this.delimiter = builder.delimiter;
        this.duplicateHeaderMode = builder.duplicateHeaderMode;
        this.escapeCharacter = builder.escapeCharacter;
        this.headerComments = builder.headerComments;
        this.headers = builder.headers;
        this.ignoreEmptyLines = builder.ignoreEmptyLines;
        this.ignoreHeaderCase = builder.ignoreHeaderCase;
        this.ignoreSurroundingSpaces = builder.ignoreSurroundingSpaces;
        this.lenientEof = builder.lenientEof;
        this.maxRows = builder.maxRows;
        this.nullString = builder.nullString;
        this.quoteCharacter = builder.quoteCharacter;
        this.quoteMode = builder.quoteMode;
        this.quotedNullString = builder.quotedNullString;
        this.recordSeparator = builder.recordSeparator;
        this.skipHeaderRecord = builder.skipHeaderRecord;
        this.trailingData = builder.trailingData;
        this.trailingDelimiter = builder.trailingDelimiter;
        this.trim = builder.trim;
        validate();
    }

    private void append(final char c, final Appendable appendable) throws IOException {
        // try {
        appendable.append(c);
        // } catch (final IOException e) {
        // throw new UncheckedIOException(e);
        // }
    }

    private void append(final CharSequence csq, final Appendable appendable) throws IOException {
        // try {
        appendable.append(csq);
        // } catch (final IOException e) {
        // throw new UncheckedIOException(e);
        // }
    }

    /**
     * Creates a new Builder for this instance.
     *
     * @return a new Builder.
     */
    public Builder builder() {
        return Builder.create(this);
    }

    /**
     * Creates a copy of this instance.
     *
     * @return a copy of this instance.
     */
    CSVFormat copy() {
        return builder().get();
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
        return allowMissingColumnNames == other.allowMissingColumnNames && autoFlush == other.autoFlush &&
                Objects.equals(commentMarker, other.commentMarker) && Objects.equals(delimiter, other.delimiter) &&
                duplicateHeaderMode == other.duplicateHeaderMode && Objects.equals(escapeCharacter, other.escapeCharacter) &&
                Arrays.equals(headerComments, other.headerComments) && Arrays.equals(headers, other.headers) &&
                ignoreEmptyLines == other.ignoreEmptyLines && ignoreHeaderCase == other.ignoreHeaderCase &&
                ignoreSurroundingSpaces == other.ignoreSurroundingSpaces && lenientEof == other.lenientEof &&
                Objects.equals(nullString, other.nullString) && Objects.equals(quoteCharacter, other.quoteCharacter) &&
                quoteMode == other.quoteMode && Objects.equals(quotedNullString, other.quotedNullString) &&
                Objects.equals(recordSeparator, other.recordSeparator) && skipHeaderRecord == other.skipHeaderRecord &&
                trailingData == other.trailingData && trailingDelimiter == other.trailingDelimiter && trim == other.trim;
    }

    private void escape(final char c, final Appendable appendable) throws IOException {
        append(escapeCharacter.charValue(), appendable); // Explicit (un)boxing is intentional
        append(c, appendable);
    }

    /**
     * Formats the specified values as a CSV record string.
     *
     * @param values the values to format.
     * @return the formatted values.
     */
    public String format(final Object... values) {
        return Uncheck.get(() -> format_(values));
    }

    private String format_(final Object... values) throws IOException {
        final StringWriter out = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(out, this)) {
            csvPrinter.printRecord(values);
            final String res = out.toString();
            final int len = recordSeparator != null ? res.length() - recordSeparator.length() : res.length();
            return res.substring(0, len);
        }
    }

    /**
     * Gets whether duplicate names are allowed in the headers.
     *
     * @return whether duplicate header names are allowed
     * @since 1.7
     * @deprecated Use {@link #getDuplicateHeaderMode()}.
     */
    @Deprecated
    public boolean getAllowDuplicateHeaderNames() {
        return duplicateHeaderMode == DuplicateHeaderMode.ALLOW_ALL;
    }

    /**
     * Gets whether missing column names are allowed when parsing the header line.
     *
     * @return {@code true} if missing column names are allowed when parsing the header line, {@code false} to throw an {@link IllegalArgumentException}.
     */
    public boolean getAllowMissingColumnNames() {
        return allowMissingColumnNames;
    }

    /**
     * Gets whether to flush on close.
     *
     * @return whether to flush on close.
     * @since 1.6
     */
    public boolean getAutoFlush() {
        return autoFlush;
    }

    /**
     * Gets the comment marker character, {@code null} disables comments.
     * <p>
     * The comment start character is only recognized at the start of a line.
     * </p>
     * <p>
     * Comments are printed first, before headers.
     * </p>
     * <p>
     * Use {@link Builder#setCommentMarker(char)} or {@link Builder#setCommentMarker(Character)} to set the comment marker written at the start of each comment
     * line.
     * </p>
     * <p>
     * If the comment marker is not set, then the header comments are ignored.
     * </p>
     * <p>
     * For example:
     * </p>
     *
     * <pre>
     * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0));
     * </pre>
     * <p>
     * writes:
     * </p>
     *
     * <pre>
     * # Generated by Apache Commons CSV.
     * # 1970-01-01T00:00:00Z
     * </pre>
     *
     * @return the comment start marker, may be {@code null}
     */
    public Character getCommentMarker() {
        return commentMarker;
    }

    /**
     * Gets the first character delimiting the values (typically ';', ',' or '\t').
     *
     * @return the first delimiter character.
     * @deprecated Use {@link #getDelimiterString()}.
     */
    @Deprecated
    public char getDelimiter() {
        return delimiter.charAt(0);
    }

    /**
     * Gets the character delimiting the values (typically ";", "," or "\t").
     *
     * @return the delimiter.
     */
    char[] getDelimiterCharArray() {
        return delimiter.toCharArray();
    }

    /**
     * Gets the character delimiting the values (typically ";", "," or "\t").
     *
     * @return the delimiter.
     * @since 1.9.0
     */
    public String getDelimiterString() {
        return delimiter;
    }

    /**
     * Gets how duplicate headers are handled.
     *
     * @return if duplicate header values are allowed, allowed conditionally, or disallowed.
     * @since 1.10.0
     */
    public DuplicateHeaderMode getDuplicateHeaderMode() {
        return duplicateHeaderMode;
    }

    /**
     * Gets the escape character.
     *
     * @return the escape character, may be {@code 0}
     */
    char getEscapeChar() {
        return escapeCharacter != null ? escapeCharacter.charValue() : 0; // Explicit (un)boxing is intentional
    }

    /**
     * Gets the escape character.
     *
     * @return the escape character, may be {@code null}
     */
    public Character getEscapeCharacter() {
        return escapeCharacter;
    }

    /**
     * Gets a copy of the header array.
     *
     * @return a copy of the header array; {@code null} if disabled, the empty array if to be read from the file
     */
    public String[] getHeader() {
        return headers != null ? headers.clone() : null;
    }

    /**
     * Gets a copy of the header comment array to write before the CSV data.
     * <p>
     * This setting is ignored by the parser.
     * </p>
     * <p>
     * Comments are printed first, before headers.
     * </p>
     * <p>
     * Use {@link Builder#setCommentMarker(char)} or {@link Builder#setCommentMarker(Character)} to set the comment marker written at the start of each comment
     * line.
     * </p>
     * <p>
     * If the comment marker is not set, then the header comments are ignored.
     * </p>
     * <p>
     * For example:
     * </p>
     *
     * <pre>
     * builder.setCommentMarker('#').setHeaderComments("Generated by Apache Commons CSV", Instant.ofEpochMilli(0));
     * </pre>
     * <p>
     * writes:
     * </p>
     *
     * <pre>
     * # Generated by Apache Commons CSV.
     * # 1970-01-01T00:00:00Z
     * </pre>
     *
     * @return a copy of the header comment array; {@code null} if disabled.
     */
    public String[] getHeaderComments() {
        return headerComments != null ? headerComments.clone() : null;
    }

    /**
     * Gets whether empty lines between records are ignored when parsing input.
     *
     * @return {@code true} if empty lines between records are ignored, {@code false} if they are turned into empty records.
     */
    public boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    /**
     * Gets whether header names will be accessed ignoring case when parsing input.
     *
     * @return {@code true} if header names cases are ignored, {@code false} if they are case-sensitive.
     * @since 1.3
     */
    public boolean getIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    /**
     * Gets whether spaces around values are ignored when parsing input.
     *
     * @return {@code true} if spaces around values are ignored, {@code false} if they are treated as part of the value.
     */
    public boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    /**
     * Gets whether reading end-of-file is allowed even when input is malformed, helps Excel compatibility.
     *
     * @return whether reading end-of-file is allowed even when input is malformed, helps Excel compatibility.
     * @since 1.11.0
     */
    public boolean getLenientEof() {
        return lenientEof;
    }

    /**
     * Gets the maximum number of rows to process, excluding the header row.
     * <p>
     * Values less than or equal to 0 mean no limit.
     * </p>
     *
     * @return The maximum number of rows to process, excluding the header row.
     * @since 1.14.0
     */
    public long getMaxRows() {
        return maxRows;
    }

    /**
     * Gets the String to convert to and from {@code null}.
     * <ul>
     * <li><strong>Reading:</strong> Converts strings equal to the given {@code nullString} to {@code null} when reading records.</li>
     * <li><strong>Writing:</strong> Writes {@code null} as the given {@code nullString} when writing records.</li>
     * </ul>
     *
     * @return the String to convert to and from {@code null}. No substitution occurs if {@code null}
     */
    public String getNullString() {
        return nullString;
    }

    /**
     * Gets the character used to encapsulate values containing special characters.
     *
     * @return the quoteChar character, may be {@code null}
     */
    public Character getQuoteCharacter() {
        return quoteCharacter;
    }

    /**
     * Gets the quote policy output fields.
     *
     * @return the quote policy
     */
    public QuoteMode getQuoteMode() {
        return quoteMode;
    }

    /**
     * Gets the record separator delimiting output records.
     *
     * @return the record separator
     */
    public String getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * Gets whether to skip the header record.
     *
     * @return whether to skip the header record.
     */
    public boolean getSkipHeaderRecord() {
        return skipHeaderRecord;
    }

    /**
     * Gets whether reading trailing data is allowed in records, helps Excel compatibility.
     *
     * @return whether reading trailing data is allowed in records, helps Excel compatibility.
     * @since 1.11.0
     */
    public boolean getTrailingData() {
        return trailingData;
    }

    /**
     * Gets whether to add a trailing delimiter.
     *
     * @return whether to add a trailing delimiter.
     * @since 1.3
     */
    public boolean getTrailingDelimiter() {
        return trailingDelimiter;
    }

    /**
     * Gets whether to trim leading and trailing blanks. This is used by {@link #print(Object, Appendable, boolean)} Also by {CSVParser#addRecordValue(boolean)}
     *
     * @return whether to trim leading and trailing blanks.
     */
    public boolean getTrim() {
        return trim;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(headerComments);
        result = prime * result + Arrays.hashCode(headers);
        return prime * result + Objects.hash(allowMissingColumnNames, autoFlush, commentMarker, delimiter, duplicateHeaderMode, escapeCharacter,
                ignoreEmptyLines, ignoreHeaderCase, ignoreSurroundingSpaces, lenientEof, nullString, quoteCharacter, quoteMode, quotedNullString,
                recordSeparator, skipHeaderRecord, trailingData, trailingDelimiter, trim);
    }

    /**
     * Tests whether comments are supported by this format.
     *
     * Note that the comment introducer character is only recognized at the start of a line.
     *
     * @return {@code true} is comments are supported, {@code false} otherwise
     */
    public boolean isCommentMarkerSet() {
        return commentMarker != null;
    }

    /**
     * Tests whether the next characters constitute a delimiter
     *
     * @param ch0             the first char (index 0).
     * @param charSeq         the match char sequence
     * @param startIndex      where start to match
     * @param delimiter       the delimiter
     * @param delimiterLength the delimiter length
     * @return true if the match is successful
     */
    private boolean isDelimiter(final char ch0, final CharSequence charSeq, final int startIndex, final char[] delimiter, final int delimiterLength) {
        if (ch0 != delimiter[0]) {
            return false;
        }
        final int len = charSeq.length();
        if (startIndex + delimiterLength > len) {
            return false;
        }
        for (int i = 1; i < delimiterLength; i++) {
            if (charSeq.charAt(startIndex + i) != delimiter[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether escapes are being processed.
     *
     * @return {@code true} if escapes are processed
     */
    public boolean isEscapeCharacterSet() {
        return escapeCharacter != null;
    }

    /**
     * Tests whether a null string has been defined.
     *
     * @return {@code true} if a nullString is defined
     */
    public boolean isNullStringSet() {
        return nullString != null;
    }

    /**
     * Tests whether a quoteChar has been defined.
     *
     * @return {@code true} if a quoteChar is defined
     */
    public boolean isQuoteCharacterSet() {
        return quoteCharacter != null;
    }

    <T> IOStream<T> limit(final IOStream<T> stream) {
        return useMaxRows() ? stream.limit(getMaxRows()) : stream;
    }

    /**
     * Parses the specified content.
     *
     * <p>
     * See also the various static parse methods on {@link CSVParser}.
     * </p>
     *
     * @param reader the input stream
     * @return a parser over a stream of {@link CSVRecord}s.
     * @throws IOException  If an I/O error occurs
     * @throws CSVException Thrown on invalid input.
     */
    public CSVParser parse(final Reader reader) throws IOException {
        return CSVParser.builder().setReader(reader).setFormat(this).get();
    }

    /**
     * Prints to the specified output.
     *
     * <p>
     * See also {@link CSVPrinter}.
     * </p>
     *
     * @param out the output.
     * @return a printer to an output.
     * @throws IOException thrown if the optional header cannot be printed.
     */
    public CSVPrinter print(final Appendable out) throws IOException {
        return new CSVPrinter(out, this);
    }

    /**
     * Prints to the specified {@code File} with given {@code Charset}.
     *
     * <p>
     * See also {@link CSVPrinter}.
     * </p>
     *
     * @param out     the output.
     * @param charset A charset.
     * @return a printer to an output.
     * @throws IOException thrown if the optional header cannot be printed.
     * @since 1.5
     */
    public CSVPrinter print(final File out, final Charset charset) throws IOException {
        return print(out.toPath(), charset);
    }

    private void print(final InputStream inputStream, final Appendable out, final boolean newRecord) throws IOException {
        // InputStream is never null here
        // There is nothing to escape when quoting is used which is the default.
        if (!newRecord) {
            append(getDelimiterString(), out);
        }
        final boolean quoteCharacterSet = isQuoteCharacterSet();
        if (quoteCharacterSet) {
            append(getQuoteCharacter().charValue(), out); // Explicit (un)boxing is intentional
        }
        // Stream the input to the output without reading or holding the whole value in memory.
        // AppendableOutputStream cannot "close" an Appendable.
        try (OutputStream outputStream = new Base64OutputStream(new AppendableOutputStream<>(out))) {
            IOUtils.copy(inputStream, outputStream);
        }
        if (quoteCharacterSet) {
            append(getQuoteCharacter().charValue(), out); // Explicit (un)boxing is intentional
        }
    }

    /**
     * Prints the {@code value} as the next value on the line to {@code out}. The value will be escaped or encapsulated as needed. Useful when one wants to
     * avoid creating CSVPrinters. Trims the value if {@link #getTrim()} is true.
     *
     * @param value     value to output.
     * @param out       where to print the value.
     * @param newRecord if this a new record.
     * @throws IOException If an I/O error occurs.
     * @since 1.4
     */
    public synchronized void print(final Object value, final Appendable out, final boolean newRecord) throws IOException {
        // null values are considered empty
        // Only call CharSequence.toString() if you have to, helps GC-free use cases.
        CharSequence charSequence;
        if (value == null) {
            // https://issues.apache.org/jira/browse/CSV-203
            if (null == nullString) {
                charSequence = Constants.EMPTY;
            } else if (QuoteMode.ALL == quoteMode) {
                charSequence = quotedNullString;
            } else {
                charSequence = nullString;
            }
        } else if (value instanceof CharSequence) {
            charSequence = (CharSequence) value;
        } else if (value instanceof Reader) {
            print((Reader) value, out, newRecord);
            return;
        } else if (value instanceof InputStream) {
            print((InputStream) value, out, newRecord);
            return;
        } else {
            charSequence = value.toString();
        }
        charSequence = getTrim() ? trim(charSequence) : charSequence;
        print(value, charSequence, out, newRecord);
    }

    private synchronized void print(final Object object, final CharSequence value, final Appendable out, final boolean newRecord) throws IOException {
        final int offset = 0;
        final int len = value.length();
        if (!newRecord) {
            out.append(getDelimiterString());
        }
        if (object == null) {
            out.append(value);
        } else if (isQuoteCharacterSet()) {
            // The original object is needed so can check for Number
            printWithQuotes(object, value, out, newRecord);
        } else if (isEscapeCharacterSet()) {
            printWithEscapes(value, out);
        } else {
            out.append(value, offset, len);
        }
    }

    /**
     * Prints to the specified {@code Path} with given {@code Charset}, returns a {@code CSVPrinter} which the caller MUST close.
     *
     * <p>
     * See also {@link CSVPrinter}.
     * </p>
     *
     * @param out     the output.
     * @param charset A charset.
     * @return a printer to an output.
     * @throws IOException thrown if the optional header cannot be printed.
     * @since 1.5
     */
    @SuppressWarnings("resource")
    public CSVPrinter print(final Path out, final Charset charset) throws IOException {
        return print(Files.newBufferedWriter(out, charset));
    }

    private void print(final Reader reader, final Appendable out, final boolean newRecord) throws IOException {
        // Reader is never null here
        if (!newRecord) {
            append(getDelimiterString(), out);
        }
        if (isQuoteCharacterSet()) {
            printWithQuotes(reader, out);
        } else if (isEscapeCharacterSet()) {
            printWithEscapes(reader, out);
        } else if (out instanceof Writer) {
            IOUtils.copyLarge(reader, (Writer) out);
        } else {
            IOUtils.copy(reader, out);
        }
    }

    /**
     * Prints to the {@link System#out}.
     *
     * <p>
     * See also {@link CSVPrinter}.
     * </p>
     *
     * @return a printer to {@link System#out}.
     * @throws IOException thrown if the optional header cannot be printed.
     * @since 1.5
     */
    public CSVPrinter printer() throws IOException {
        return new CSVPrinter(System.out, this);
    }

    /**
     * Outputs the trailing delimiter (if set) followed by the record separator (if set).
     *
     * @param appendable where to write
     * @throws IOException If an I/O error occurs.
     * @since 1.4
     */
    public synchronized void println(final Appendable appendable) throws IOException {
        if (getTrailingDelimiter()) {
            append(getDelimiterString(), appendable);
        }
        if (recordSeparator != null) {
            append(recordSeparator, appendable);
        }
    }

    /**
     * Prints the given {@code values} to {@code out} as a single record of delimiter-separated values followed by the record separator.
     *
     * <p>
     * The values will be quoted if needed. Quotes and new-line characters will be escaped. This method adds the record separator to the output after printing
     * the record, so there is no need to call {@link #println(Appendable)}.
     * </p>
     *
     * @param appendable where to write.
     * @param values     values to output.
     * @throws IOException If an I/O error occurs.
     * @since 1.4
     */
    public synchronized void printRecord(final Appendable appendable, final Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            print(values[i], appendable, i == 0);
        }
        println(appendable);
    }

    /*
     * This method must only be called if escaping is enabled, otherwise can throw exceptions.
     */
    private void printWithEscapes(final CharSequence charSeq, final Appendable appendable) throws IOException {
        int start = 0;
        int pos = 0;
        final int end = charSeq.length();
        final char[] delimArray = getDelimiterCharArray();
        final int delimLength = delimArray.length;
        final char escape = getEscapeChar();
        while (pos < end) {
            char c = charSeq.charAt(pos);
            final boolean isDelimiterStart = isDelimiter(c, charSeq, pos, delimArray, delimLength);
            final boolean isCr = c == Constants.CR;
            final boolean isLf = c == Constants.LF;
            if (isCr || isLf || c == escape || isDelimiterStart) {
                // write out segment up until this char
                if (pos > start) {
                    appendable.append(charSeq, start, pos);
                }
                if (isLf) {
                    c = 'n';
                } else if (isCr) {
                    c = 'r';
                }
                escape(c, appendable);
                if (isDelimiterStart) {
                    for (int i = 1; i < delimLength; i++) {
                        pos++;
                        escape(charSeq.charAt(pos), appendable);
                    }
                }
                start = pos + 1; // start on the current char after this one
            }
            pos++;
        }

        // write last segment
        if (pos > start) {
            appendable.append(charSeq, start, pos);
        }
    }

    /*
     * This method must only be called if escaping is enabled, otherwise can throw exceptions.
     */
    private void printWithEscapes(final Reader reader, final Appendable appendable) throws IOException {
        int start = 0;
        int pos = 0;
        @SuppressWarnings("resource") // Temp reader on input reader.
        final ExtendedBufferedReader bufferedReader = new ExtendedBufferedReader(reader);
        final char[] delimArray = getDelimiterCharArray();
        final int delimLength = delimArray.length;
        final char escape = getEscapeChar();
        final StringBuilder builder = new StringBuilder(IOUtils.DEFAULT_BUFFER_SIZE);
        int c;
        final char[] lookAheadBuffer = new char[delimLength - 1];
        while (EOF != (c = bufferedReader.read())) {
            builder.append((char) c);
            Arrays.fill(lookAheadBuffer, (char) 0);
            bufferedReader.peek(lookAheadBuffer);
            final String test = builder.toString() + new String(lookAheadBuffer);
            final boolean isDelimiterStart = isDelimiter((char) c, test, pos, delimArray, delimLength);
            final boolean isCr = c == Constants.CR;
            final boolean isLf = c == Constants.LF;
            if (isCr || isLf || c == escape || isDelimiterStart) {
                // write out segment up until this char
                if (pos > start) {
                    append(builder.substring(start, pos), appendable);
                    builder.setLength(0);
                    pos = -1;
                }
                if (isLf) {
                    c = 'n';
                } else if (isCr) {
                    c = 'r';
                }
                escape((char) c, appendable);
                if (isDelimiterStart) {
                    for (int i = 1; i < delimLength; i++) {
                        escape((char) bufferedReader.read(), appendable);
                    }
                }
                start = pos + 1; // start on the current char after this one
            }
            pos++;
        }
        // write last segment
        if (pos > start) {
            appendable.append(builder, start, pos);
        }
    }

    /*
     * This method must only be called if quoting is enabled, otherwise will generate NPE.
     * The original object is needed so can check for Number
     */
    private void printWithQuotes(final Object object, final CharSequence charSeq, final Appendable out, final boolean newRecord) throws IOException {
        boolean quote = false;
        int start = 0;
        int pos = 0;
        final int len = charSeq.length();
        final char[] delim = getDelimiterCharArray();
        final int delimLength = delim.length;
        final char quoteChar = getQuoteCharacter().charValue(); // Explicit (un)boxing is intentional
        // If escape char not specified, default to the quote char
        // This avoids having to keep checking whether there is an escape character
        // at the cost of checking against quote twice
        final char escapeChar = isEscapeCharacterSet() ? getEscapeChar() : quoteChar;
        QuoteMode quoteModePolicy = getQuoteMode();
        if (quoteModePolicy == null) {
            quoteModePolicy = QuoteMode.MINIMAL;
        }
        switch (quoteModePolicy) {
        case ALL:
        case ALL_NON_NULL:
            quote = true;
            break;
        case NON_NUMERIC:
            quote = !(object instanceof Number);
            break;
        case NONE:
            // Use the existing escaping code
            printWithEscapes(charSeq, out);
            return;
        case MINIMAL:
            if (len <= 0) {
                // Always quote an empty token that is the first
                // on the line, as it may be the only thing on the
                // line. If it were not quoted in that case,
                // an empty line has no tokens.
                if (newRecord) {
                    quote = true;
                }
            } else {
                char c = charSeq.charAt(pos);
                if (c <= Constants.COMMENT) {
                    // Some other chars at the start of a value caused the parser to fail, so for now
                    // encapsulate if we start in anything less than '#'. We are being conservative
                    // by including the default comment char too.
                    quote = true;
                } else {
                    while (pos < len) {
                        c = charSeq.charAt(pos);
                        if (c == Constants.LF || c == Constants.CR || c == quoteChar || c == escapeChar || isDelimiter(c, charSeq, pos, delim, delimLength)) {
                            quote = true;
                            break;
                        }
                        pos++;
                    }

                    if (!quote) {
                        pos = len - 1;
                        c = charSeq.charAt(pos);
                        // Some other chars at the end caused the parser to fail, so for now
                        // encapsulate if we end in anything less than ' '
                        if (isTrimChar(c)) {
                            quote = true;
                        }
                    }
                }
            }
            if (!quote) {
                // No encapsulation needed - write out the original value
                out.append(charSeq, start, len);
                return;
            }
            break;
        default:
            throw new IllegalStateException("Unexpected Quote value: " + quoteModePolicy);
        }
        if (!quote) {
            // No encapsulation needed - write out the original value
            out.append(charSeq, start, len);
            return;
        }
        // We hit something that needed encapsulation
        out.append(quoteChar);
        // Pick up where we left off: pos should be positioned on the first character that caused
        // the need for encapsulation.
        while (pos < len) {
            final char c = charSeq.charAt(pos);
            if (c == quoteChar || c == escapeChar) {
                // write out the chunk up until this point
                out.append(charSeq, start, pos);
                out.append(escapeChar); // now output the escape
                start = pos; // and restart with the matched char
            }
            pos++;
        }
        // Write the last segment
        out.append(charSeq, start, pos);
        out.append(quoteChar);
    }

    /**
     * Always use quotes unless QuoteMode is NONE, so we do not have to look ahead.
     *
     * @param reader     What to print
     * @param appendable Where to print it
     * @throws IOException If an I/O error occurs
     */
    private void printWithQuotes(final Reader reader, final Appendable appendable) throws IOException {
        if (getQuoteMode() == QuoteMode.NONE) {
            printWithEscapes(reader, appendable);
            return;
        }
        final char quote = getQuoteCharacter().charValue(); // Explicit (un)boxing is intentional
        // (1) Append opening quote
        append(quote, appendable);
        // (2) Append Reader contents, doubling quotes
        int c;
        while (EOF != (c = reader.read())) {
            append((char) c, appendable);
            if (c == quote) {
                append(quote, appendable);
            }
        }
        // (3) Append closing quote
        append(quote, appendable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Delimiter=<").append(delimiter).append('>');
        if (isEscapeCharacterSet()) {
            sb.append(Constants.SP);
            sb.append("Escape=<").append(escapeCharacter).append('>');
        }
        if (isQuoteCharacterSet()) {
            sb.append(Constants.SP);
            sb.append("QuoteChar=<").append(quoteCharacter).append('>');
        }
        if (quoteMode != null) {
            sb.append(Constants.SP);
            sb.append("QuoteMode=<").append(quoteMode).append('>');
        }
        if (isCommentMarkerSet()) {
            sb.append(Constants.SP);
            sb.append("CommentStart=<").append(commentMarker).append('>');
        }
        if (isNullStringSet()) {
            sb.append(Constants.SP);
            sb.append("NullString=<").append(nullString).append('>');
        }
        if (recordSeparator != null) {
            sb.append(Constants.SP);
            sb.append("RecordSeparator=<").append(recordSeparator).append('>');
        }
        if (getIgnoreEmptyLines()) {
            sb.append(" EmptyLines:ignored");
        }
        if (getIgnoreSurroundingSpaces()) {
            sb.append(" SurroundingSpaces:ignored");
        }
        if (getIgnoreHeaderCase()) {
            sb.append(" IgnoreHeaderCase:ignored");
        }
        sb.append(" SkipHeaderRecord:").append(skipHeaderRecord);
        if (headerComments != null) {
            sb.append(Constants.SP);
            sb.append("HeaderComments:").append(Arrays.toString(headerComments));
        }
        if (headers != null) {
            sb.append(Constants.SP);
            sb.append("Header:").append(Arrays.toString(headers));
        }
        return sb.toString();
    }

    String trim(final String value) {
        return getTrim() ? value.trim() : value;
    }

    boolean useMaxRows() {
        return getMaxRows() > 0;
    }

    boolean useRow(final long rowNum) {
        return !useMaxRows() || rowNum <= getMaxRows();
    }

    /**
     * Verifies the validity and consistency of the attributes, and throws an {@link IllegalArgumentException} if necessary.
     * <p>
     * Because an instance can be used for both writing and parsing, not all conditions can be tested here. For example, allowMissingColumnNames is only used
     * for parsing, so it cannot be used here.
     * </p>
     *
     * @throws IllegalArgumentException Throw when any attribute is invalid or inconsistent with other attributes.
     */
    private void validate() throws IllegalArgumentException {
        if (quoteCharacter != null && contains(delimiter, quoteCharacter.charValue())) { // Explicit (un)boxing is intentional
            throw new IllegalArgumentException("The quoteChar character and the delimiter cannot be the same ('" + quoteCharacter + "')");
        }
        if (escapeCharacter != null && contains(delimiter, escapeCharacter.charValue())) { // Explicit (un)boxing is intentional
            throw new IllegalArgumentException("The escape character and the delimiter cannot be the same ('" + escapeCharacter + "')");
        }
        if (commentMarker != null && contains(delimiter, commentMarker.charValue())) { // Explicit (un)boxing is intentional
            throw new IllegalArgumentException("The comment start character and the delimiter cannot be the same ('" + commentMarker + "')");
        }
        if (quoteCharacter != null && quoteCharacter.equals(commentMarker)) {
            throw new IllegalArgumentException("The comment start character and the quoteChar cannot be the same ('" + commentMarker + "')");
        }
        if (escapeCharacter != null && escapeCharacter.equals(commentMarker)) {
            throw new IllegalArgumentException("The comment start and the escape character cannot be the same ('" + commentMarker + "')");
        }
        if (escapeCharacter == null && quoteMode == QuoteMode.NONE) {
            throw new IllegalArgumentException("Quote mode set to NONE but no escape character is set");
        }
        // Validate headers
        if (headers != null && duplicateHeaderMode != DuplicateHeaderMode.ALLOW_ALL) {
            final Set<String> dupCheckSet = new HashSet<>(headers.length);
            final boolean emptyDuplicatesAllowed = duplicateHeaderMode == DuplicateHeaderMode.ALLOW_EMPTY;
            for (final String header : headers) {
                final boolean blank = isBlank(header);
                // Sanitize all empty headers to the empty string "" when checking duplicates
                final boolean containsHeader = !dupCheckSet.add(blank ? "" : header);
                if (containsHeader && !(blank && emptyDuplicatesAllowed)) {
                    throw new IllegalArgumentException(String.format(
                            "The header contains a duplicate name: \"%s\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().", header,
                            Arrays.toString(headers)));
                }
            }
        }
    }

    /**
     * Builds a new {@code CSVFormat} that allows duplicate header names.
     *
     * @return a new {@code CSVFormat} that allows duplicate header names
     * @since 1.7
     * @deprecated Use {@link Builder#setAllowDuplicateHeaderNames(boolean) Builder#setAllowDuplicateHeaderNames(true)}
     */
    @Deprecated
    public CSVFormat withAllowDuplicateHeaderNames() {
        return builder().setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL).get();
    }

    /**
     * Builds a new {@code CSVFormat} with duplicate header names behavior set to the given value.
     *
     * @param allowDuplicateHeaderNames the duplicate header names behavior, true to allow, false to disallow.
     * @return a new {@code CSVFormat} with duplicate header names behavior set to the given value.
     * @since 1.7
     * @deprecated Use {@link Builder#setAllowDuplicateHeaderNames(boolean)}
     */
    @Deprecated
    public CSVFormat withAllowDuplicateHeaderNames(final boolean allowDuplicateHeaderNames) {
        final DuplicateHeaderMode mode = allowDuplicateHeaderNames ? DuplicateHeaderMode.ALLOW_ALL : DuplicateHeaderMode.ALLOW_EMPTY;
        return builder().setDuplicateHeaderMode(mode).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the missing column names behavior of the format set to {@code true}.
     *
     * @return A new CSVFormat that is equal to this but with the specified missing column names behavior.
     * @see Builder#setAllowMissingColumnNames(boolean)
     * @since 1.1
     * @deprecated Use {@link Builder#setAllowMissingColumnNames(boolean) Builder#setAllowMissingColumnNames(true)}
     */
    @Deprecated
    public CSVFormat withAllowMissingColumnNames() {
        return builder().setAllowMissingColumnNames(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the missing column names behavior of the format set to the given value.
     *
     * @param allowMissingColumnNames the missing column names behavior, {@code true} to allow missing column names in the header line, {@code false} to cause
     *                                an {@link IllegalArgumentException} to be thrown.
     * @return A new CSVFormat that is equal to this but with the specified missing column names behavior.
     * @deprecated Use {@link Builder#setAllowMissingColumnNames(boolean)}
     */
    @Deprecated
    public CSVFormat withAllowMissingColumnNames(final boolean allowMissingColumnNames) {
        return builder().setAllowMissingColumnNames(allowMissingColumnNames).get();
    }

    /**
     * Builds a new {@code CSVFormat} with whether to flush on close.
     *
     * @param autoFlush whether to flush on close.
     * @return A new CSVFormat that is equal to this but with the specified autoFlush setting.
     * @since 1.6
     * @deprecated Use {@link Builder#setAutoFlush(boolean)}
     */
    @Deprecated
    public CSVFormat withAutoFlush(final boolean autoFlush) {
        return builder().setAutoFlush(autoFlush).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the comment start marker of the format set to the specified character.
     *
     * Note that the comment start character is only recognized at the start of a line.
     *
     * @param commentMarker the comment start marker
     * @return A new CSVFormat that is equal to this one but with the specified character as the comment start marker
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setCommentMarker(char)}
     */
    @Deprecated
    public CSVFormat withCommentMarker(final char commentMarker) {
        return builder().setCommentMarker(commentMarker).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the comment start marker of the format set to the specified character.
     *
     * Note that the comment start character is only recognized at the start of a line.
     *
     * @param commentMarker the comment start marker, use {@code null} to disable
     * @return A new CSVFormat that is equal to this one but with the specified character as the comment start marker
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setCommentMarker(Character)}
     */
    @Deprecated
    public CSVFormat withCommentMarker(final Character commentMarker) {
        return builder().setCommentMarker(commentMarker).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the delimiter of the format set to the specified character.
     *
     * @param delimiter the delimiter character
     * @return A new CSVFormat that is equal to this with the specified character as a delimiter
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setDelimiter(char)}
     */
    @Deprecated
    public CSVFormat withDelimiter(final char delimiter) {
        return builder().setDelimiter(delimiter).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the escape character of the format set to the specified character.
     *
     * @param escape the escape character
     * @return A new CSVFormat that is equal to this but with the specified character as the escape character
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setEscape(char)}
     */
    @Deprecated
    public CSVFormat withEscape(final char escape) {
        return builder().setEscape(escape).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the escape character of the format set to the specified character.
     *
     * @param escape the escape character, use {@code null} to disable
     * @return A new CSVFormat that is equal to this but with the specified character as the escape character
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setEscape(Character)}
     */
    @Deprecated
    public CSVFormat withEscape(final Character escape) {
        return builder().setEscape(escape).get();
    }

    // @formatter:off
    /**
     * Builds a new {@code CSVFormat} using the first record as header.
     *
     * <p>
     * Calling this method is equivalent to calling:
     * </p>
     *
     * <pre>
     * CSVFormat format = aFormat.builder()
     *                           .setHeader()
     *                           .setSkipHeaderRecord(true)
     *                           .get();
     * </pre>
     *
     * @return A new CSVFormat that is equal to this but using the first record as header.
     * @see Builder#setSkipHeaderRecord(boolean)
     * @see Builder#setHeader(String...)
     * @since 1.3
     * @deprecated Use {@link Builder#setHeader(String...) Builder#setHeader()}.{@link Builder#setSkipHeaderRecord(boolean) setSkipHeaderRecord(true)}.
     */
    // @formatter:on
    @Deprecated
    public CSVFormat withFirstRecordAsHeader() {
        // @formatter:off
        return builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();
        // @formatter:on
    }

    /**
     * Builds a new {@code CSVFormat} with the header of the format defined by the enum class.
     *
     * <p>
     * Example:
     * </p>
     *
     * <pre>
     * public enum MyHeader {
     *     Name, Email, Phone
     * }
     * ...
     * CSVFormat format = aFormat.builder().setHeader(MyHeader.class).get();
     * </pre>
     * <p>
     * The header is also used by the {@link CSVPrinter}.
     * </p>
     *
     * @param headerEnum the enum defining the header, {@code null} if disabled, empty if parsed automatically, user specified otherwise.
     * @return A new CSVFormat that is equal to this but with the specified header
     * @see Builder#setHeader(String...)
     * @see Builder#setSkipHeaderRecord(boolean)
     * @since 1.3
     * @deprecated Use {@link Builder#setHeader(Class)}
     */
    @Deprecated
    public CSVFormat withHeader(final Class<? extends Enum<?>> headerEnum) {
        return builder().setHeader(headerEnum).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the header of the format set from the result set metadata. The header can either be parsed automatically from the
     * input file with:
     *
     * <pre>
     * CSVFormat format = aFormat.builder().setHeader().get();
     * </pre>
     *
     * or specified manually with:
     *
     * <pre>
     * CSVFormat format = aFormat.builder().setHeader(resultSet).get();
     * </pre>
     * <p>
     * The header is also used by the {@link CSVPrinter}.
     * </p>
     *
     * @param resultSet the resultSet for the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
     * @return A new CSVFormat that is equal to this but with the specified header
     * @throws SQLException SQLException if a database access error occurs or this method is called on a closed result set.
     * @since 1.1
     * @deprecated Use {@link Builder#setHeader(ResultSet)}
     */
    @Deprecated
    public CSVFormat withHeader(final ResultSet resultSet) throws SQLException {
        return builder().setHeader(resultSet).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the header of the format set from the result set metadata. The header can either be parsed automatically from the
     * input file with:
     *
     * <pre>
     * CSVFormat format = aFormat.builder().setHeader().get()
     * </pre>
     *
     * or specified manually with:
     *
     * <pre>
     * CSVFormat format = aFormat.builder().setHeader(resultSetMetaData).get()
     * </pre>
     * <p>
     * The header is also used by the {@link CSVPrinter}.
     * </p>
     *
     * @param resultSetMetaData the metaData for the header, {@code null} if disabled, empty if parsed automatically, user specified otherwise.
     * @return A new CSVFormat that is equal to this but with the specified header
     * @throws SQLException SQLException if a database access error occurs or this method is called on a closed result set.
     * @since 1.1
     * @deprecated Use {@link Builder#setHeader(ResultSetMetaData)}
     */
    @Deprecated
    public CSVFormat withHeader(final ResultSetMetaData resultSetMetaData) throws SQLException {
        return builder().setHeader(resultSetMetaData).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the header of the format set to the given values. The header can either be parsed automatically from the input file
     * with:
     *
     * <pre>
     * CSVFormat format = aFormat.builder().setHeader().get();
     * </pre>
     *
     * or specified manually with:
     *
     * <pre>{@code
     * CSVFormat format = aFormat.builder().setHeader("name", "email", "phone").get();
     * }</pre>
     * <p>
     * The header is also used by the {@link CSVPrinter}.
     * </p>
     *
     * @param header the header, {@code null} if disabled, empty if parsed automatically, user-specified otherwise.
     * @return A new CSVFormat that is equal to this but with the specified header
     * @see Builder#setSkipHeaderRecord(boolean)
     * @deprecated Use {@link Builder#setHeader(String...)}
     */
    @Deprecated
    public CSVFormat withHeader(final String... header) {
        return builder().setHeader(header).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the header comments of the format set to the given values. The comments will be printed first, before the headers.
     * This setting is ignored by the parser.
     *
     * <pre>{@code
     * CSVFormat format = aFormat.builder().setHeaderComments("Generated by Apache Commons CSV.", Instant.now()).get();
     * }</pre>
     *
     * @param headerComments the headerComments which will be printed by the Printer before the actual CSV data.
     * @return A new CSVFormat that is equal to this but with the specified header
     * @see Builder#setSkipHeaderRecord(boolean)
     * @since 1.1
     * @deprecated Use {@link Builder#setHeaderComments(Object...)}
     */
    @Deprecated
    public CSVFormat withHeaderComments(final Object... headerComments) {
        return builder().setHeaderComments(headerComments).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the empty line skipping behavior of the format set to {@code true}.
     *
     * @return A new CSVFormat that is equal to this but with the specified empty line skipping behavior.
     * @see Builder#setIgnoreEmptyLines(boolean)
     * @since 1.1
     * @deprecated Use {@link Builder#setIgnoreEmptyLines(boolean) Builder#setIgnoreEmptyLines(true)}
     */
    @Deprecated
    public CSVFormat withIgnoreEmptyLines() {
        return builder().setIgnoreEmptyLines(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the empty line skipping behavior of the format set to the given value.
     *
     * @param ignoreEmptyLines the empty line skipping behavior, {@code true} to ignore the empty lines between the records, {@code false} to translate empty
     *                         lines to empty records.
     * @return A new CSVFormat that is equal to this but with the specified empty line skipping behavior.
     * @deprecated Use {@link Builder#setIgnoreEmptyLines(boolean)}
     */
    @Deprecated
    public CSVFormat withIgnoreEmptyLines(final boolean ignoreEmptyLines) {
        return builder().setIgnoreEmptyLines(ignoreEmptyLines).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the header ignore case behavior set to {@code true}.
     *
     * @return A new CSVFormat that will ignore the new case header name behavior.
     * @see Builder#setIgnoreHeaderCase(boolean)
     * @since 1.3
     * @deprecated Use {@link Builder#setIgnoreHeaderCase(boolean) Builder#setIgnoreHeaderCase(true)}
     */
    @Deprecated
    public CSVFormat withIgnoreHeaderCase() {
        return builder().setIgnoreHeaderCase(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with whether header names should be accessed ignoring case.
     *
     * @param ignoreHeaderCase the case mapping behavior, {@code true} to access name/values, {@code false} to leave the mapping as is.
     * @return A new CSVFormat that will ignore case header name if specified as {@code true}
     * @since 1.3
     * @deprecated Use {@link Builder#setIgnoreHeaderCase(boolean)}
     */
    @Deprecated
    public CSVFormat withIgnoreHeaderCase(final boolean ignoreHeaderCase) {
        return builder().setIgnoreHeaderCase(ignoreHeaderCase).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the parser trimming behavior of the format set to {@code true}.
     *
     * @return A new CSVFormat that is equal to this but with the specified parser trimming behavior.
     * @see Builder#setIgnoreSurroundingSpaces(boolean)
     * @since 1.1
     * @deprecated Use {@link Builder#setIgnoreSurroundingSpaces(boolean) Builder#setIgnoreSurroundingSpaces(true)}
     */
    @Deprecated
    public CSVFormat withIgnoreSurroundingSpaces() {
        return builder().setIgnoreSurroundingSpaces(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the parser trimming behavior of the format set to the given value.
     *
     * @param ignoreSurroundingSpaces the parser trimming behavior, {@code true} to remove the surrounding spaces, {@code false} to leave the spaces as is.
     * @return A new CSVFormat that is equal to this but with the specified trimming behavior.
     * @deprecated Use {@link Builder#setIgnoreSurroundingSpaces(boolean)}
     */
    @Deprecated
    public CSVFormat withIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
        return builder().setIgnoreSurroundingSpaces(ignoreSurroundingSpaces).get();
    }

    /**
     * Builds a new {@code CSVFormat} with conversions to and from null for strings on input and output.
     * <ul>
     * <li><strong>Reading:</strong> Converts strings equal to the given {@code nullString} to {@code null} when reading records.</li>
     * <li><strong>Writing:</strong> Writes {@code null} as the given {@code nullString} when writing records.</li>
     * </ul>
     *
     * @param nullString the String to convert to and from {@code null}. No substitution occurs if {@code null}
     * @return A new CSVFormat that is equal to this but with the specified null conversion string.
     * @deprecated Use {@link Builder#setNullString(String)}
     */
    @Deprecated
    public CSVFormat withNullString(final String nullString) {
        return builder().setNullString(nullString).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the quoteChar of the format set to the specified character.
     *
     * @param quoteChar the quote character
     * @return A new CSVFormat that is equal to this but with the specified character as quoteChar
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setQuote(char)}
     */
    @Deprecated
    public CSVFormat withQuote(final char quoteChar) {
        return builder().setQuote(quoteChar).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the quoteChar of the format set to the specified character.
     *
     * @param quoteChar the quote character, use {@code null} to disable.
     * @return A new CSVFormat that is equal to this but with the specified character as quoteChar
     * @throws IllegalArgumentException thrown if the specified character is a line break
     * @deprecated Use {@link Builder#setQuote(Character)}
     */
    @Deprecated
    public CSVFormat withQuote(final Character quoteChar) {
        return builder().setQuote(quoteChar).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the output quote policy of the format set to the specified value.
     *
     * @param quoteMode the quote policy to use for output.
     * @return A new CSVFormat that is equal to this but with the specified quote policy
     * @deprecated Use {@link Builder#setQuoteMode(QuoteMode)}
     */
    @Deprecated
    public CSVFormat withQuoteMode(final QuoteMode quoteMode) {
        return builder().setQuoteMode(quoteMode).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the record separator of the format set to the specified character.
     *
     * <p>
     * <strong>Note:</strong> This setting is only used during printing and does not affect parsing. Parsing currently only works for inputs with '\n', '\r' and
     * "\r\n"
     * </p>
     *
     * @param recordSeparator the record separator to use for output.
     * @return A new CSVFormat that is equal to this but with the specified output record separator
     * @deprecated Use {@link Builder#setRecordSeparator(char)}
     */
    @Deprecated
    public CSVFormat withRecordSeparator(final char recordSeparator) {
        return builder().setRecordSeparator(recordSeparator).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the record separator of the format set to the specified String.
     *
     * <p>
     * <strong>Note:</strong> This setting is only used during printing and does not affect parsing. Parsing currently only works for inputs with '\n', '\r' and
     * "\r\n"
     * </p>
     *
     * @param recordSeparator the record separator to use for output.
     * @return A new CSVFormat that is equal to this but with the specified output record separator
     * @throws IllegalArgumentException if recordSeparator is none of CR, LF or CRLF
     * @deprecated Use {@link Builder#setRecordSeparator(String)}
     */
    @Deprecated
    public CSVFormat withRecordSeparator(final String recordSeparator) {
        return builder().setRecordSeparator(recordSeparator).get();
    }

    /**
     * Builds a new {@code CSVFormat} with skipping the header record set to {@code true}.
     *
     * @return A new CSVFormat that is equal to this but with the specified skipHeaderRecord setting.
     * @see Builder#setSkipHeaderRecord(boolean)
     * @see Builder#setHeader(String...)
     * @since 1.1
     * @deprecated Use {@link Builder#setSkipHeaderRecord(boolean) Builder#setSkipHeaderRecord(true)}
     */
    @Deprecated
    public CSVFormat withSkipHeaderRecord() {
        return builder().setSkipHeaderRecord(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with whether to skip the header record.
     *
     * @param skipHeaderRecord whether to skip the header record.
     * @return A new CSVFormat that is equal to this but with the specified skipHeaderRecord setting.
     * @see Builder#setHeader(String...)
     * @deprecated Use {@link Builder#setSkipHeaderRecord(boolean)}
     */
    @Deprecated
    public CSVFormat withSkipHeaderRecord(final boolean skipHeaderRecord) {
        return builder().setSkipHeaderRecord(skipHeaderRecord).get();
    }

    /**
     * Builds a new {@code CSVFormat} with the record separator of the format set to the operating system's line separator string, typically CR+LF on Windows
     * and LF on Linux.
     *
     * <p>
     * <strong>Note:</strong> This setting is only used during printing and does not affect parsing. Parsing currently only works for inputs with '\n', '\r' and
     * "\r\n"
     * </p>
     *
     * @return A new CSVFormat that is equal to this but with the operating system's line separator string.
     * @since 1.6
     * @deprecated Use {@link Builder#setRecordSeparator(String) setRecordSeparator(System.lineSeparator())}
     */
    @Deprecated
    public CSVFormat withSystemRecordSeparator() {
        return builder().setRecordSeparator(System.lineSeparator()).get();
    }

    /**
     * Builds a new {@code CSVFormat} to add a trailing delimiter.
     *
     * @return A new CSVFormat that is equal to this but with the trailing delimiter setting.
     * @since 1.3
     * @deprecated Use {@link Builder#setTrailingDelimiter(boolean) Builder#setTrailingDelimiter(true)}
     */
    @Deprecated
    public CSVFormat withTrailingDelimiter() {
        return builder().setTrailingDelimiter(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with whether to add a trailing delimiter.
     *
     * @param trailingDelimiter whether to add a trailing delimiter.
     * @return A new CSVFormat that is equal to this but with the specified trailing delimiter setting.
     * @since 1.3
     * @deprecated Use {@link Builder#setTrailingDelimiter(boolean)}
     */
    @Deprecated
    public CSVFormat withTrailingDelimiter(final boolean trailingDelimiter) {
        return builder().setTrailingDelimiter(trailingDelimiter).get();
    }

    /**
     * Builds a new {@code CSVFormat} to trim leading and trailing blanks. See {@link #getTrim()} for details of where this is used.
     *
     * @return A new CSVFormat that is equal to this but with the trim setting on.
     * @since 1.3
     * @deprecated Use {@link Builder#setTrim(boolean) Builder#setTrim(true)}
     */
    @Deprecated
    public CSVFormat withTrim() {
        return builder().setTrim(true).get();
    }

    /**
     * Builds a new {@code CSVFormat} with whether to trim leading and trailing blanks. See {@link #getTrim()} for details of where this is used.
     *
     * @param trim whether to trim leading and trailing blanks.
     * @return A new CSVFormat that is equal to this but with the specified trim setting.
     * @since 1.3
     * @deprecated Use {@link Builder#setTrim(boolean)}
     */
    @Deprecated
    public CSVFormat withTrim(final boolean trim) {
        return builder().setTrim(trim).get();
    }

}
