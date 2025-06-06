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

import static org.apache.commons.csv.Token.Type.TOKEN;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

/**
 * Parses CSV files according to the specified format.
 *
 * Because CSV appears in many different dialects, the parser supports many formats by allowing the
 * specification of a {@link CSVFormat}.
 *
 * The parser works record-wise. It is not possible to go back, once a record has been parsed from the input stream.
 *
 * <h2>Creating instances</h2>
 * <p>
 * There are several static factory methods that can be used to create instances for various types of resources:
 * </p>
 * <ul>
 *     <li>{@link #parse(java.io.File, Charset, CSVFormat)}</li>
 *     <li>{@link #parse(String, CSVFormat)}</li>
 *     <li>{@link #parse(java.net.URL, java.nio.charset.Charset, CSVFormat)}</li>
 * </ul>
 * <p>
 * Alternatively parsers can also be created by passing a {@link Reader} directly to the sole constructor.
 *
 * For those who like fluent APIs, parsers can be created using {@link CSVFormat#parse(java.io.Reader)} as a shortcut:
 * </p>
 * <pre>
 * for (CSVRecord record : CSVFormat.EXCEL.parse(in)) {
 *     ...
 * }
 * </pre>
 *
 * <h2>Parsing record wise</h2>
 * <p>
 * To parse a CSV input from a file, you write:
 * </p>
 *
 * <pre>{@code
 * File csvData = new File("/path/to/csv");
 * CSVParser parser = CSVParser.parse(csvData, CSVFormat.RFC4180);
 * for (CSVRecord csvRecord : parser) {
 *     ...
 * }}
 * </pre>
 *
 * <p>
 * This will read the parse the contents of the file using the
 * <a href="https://tools.ietf.org/html/rfc4180" target="_blank">RFC 4180</a> format.
 * </p>
 *
 * <p>
 * To parse CSV input in a format like Excel, you write:
 * </p>
 *
 * <pre>
 * CSVParser parser = CSVParser.parse(csvData, CSVFormat.EXCEL);
 * for (CSVRecord csvRecord : parser) {
 *     ...
 * }
 * </pre>
 *
 * <p>
 * If the predefined formats don't match the format at hand, custom formats can be defined. More information about
 * customizing CSVFormats is available in {@link CSVFormat CSVFormat Javadoc}.
 * </p>
 *
 * <h2>Parsing into memory</h2>
 * <p>
 * If parsing record-wise is not desired, the contents of the input can be read completely into memory.
 * </p>
 *
 * <pre>{@code
 * Reader in = new StringReader("a;b\nc;d");
 * CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
 * List<CSVRecord> list = parser.getRecords();
 * }</pre>
 *
 * <p>
 * There are two constraints that have to be kept in mind:
 * </p>
 *
 * <ol>
 *     <li>Parsing into memory starts at the current position of the parser. If you have already parsed records from
 *     the input, those records will not end up in the in-memory representation of your CSV data.</li>
 *     <li>Parsing into memory may consume a lot of system resources depending on the input. For example, if you're
 *     parsing a 150MB file of CSV data the contents will be read completely into memory.</li>
 * </ol>
 *
 * <h2>Notes</h2>
 * <p>
 * The internal parser state is completely covered by the format and the reader state.
 * </p>
 *
 * @see <a href="package-summary.html">package documentation for more details</a>
 */
public final class CSVParser implements Iterable<CSVRecord>, Closeable {

    /**
     * Builds a new {@link CSVParser}.
     *
     * @since 1.13.0
     */
    public static class Builder extends AbstractStreamBuilder<CSVParser, Builder> {

        private CSVFormat format;
        private long characterOffset;
        private long recordNumber = 1;
        private boolean trackBytes;

        /**
         * Constructs a new instance.
         */
        protected Builder() {
            // empty
        }

        @SuppressWarnings("resource")
        @Override
        public CSVParser get() throws IOException {
            return new CSVParser(getReader(), format != null ? format : CSVFormat.DEFAULT, characterOffset, recordNumber, getCharset(), trackBytes);
        }

        /**
         * Sets the lexer offset when the parser does not start parsing at the beginning of the source.
         *
         * @param characterOffset the lexer offset.
         * @return this instance.
         */
        public Builder setCharacterOffset(final long characterOffset) {
            this.characterOffset = characterOffset;
            return asThis();
        }

        /**
         * Sets the CSV format. A copy of the given format is kept.
         *
         * @param format the CSV format, {@code null} resets to {@link CSVFormat#DEFAULT}.
         * @return this instance.
         */
        public Builder setFormat(final CSVFormat format) {
            this.format = CSVFormat.copy(format);
            return asThis();
        }

        /**
         * Sets the next record number to assign, defaults to {@code 1}.
         *
         * @param recordNumber the next record number to assign.
         * @return this instance.
         */
        public Builder setRecordNumber(final long recordNumber) {
            this.recordNumber = recordNumber;
            return asThis();
        }

        /**
         * Sets whether to enable byte tracking for the parser.
         *
         * @param trackBytes {@code true} to enable byte tracking; {@code false} to disable it.
         * @return this instance.
         * @since 1.13.0
         */
        public Builder setTrackBytes(final boolean trackBytes) {
            this.trackBytes = trackBytes;
            return asThis();
        }

    }

    final class CSVRecordIterator implements Iterator<CSVRecord> {
        private CSVRecord current;

        /**
         * Gets the next record or null at the end of stream or max rows read.
         *
         * @throws IOException  on parse error or input read-failure
         * @throws CSVException on invalid input.
         * @return the next record, or {@code null} if the end of the stream has been reached.
         */
        private CSVRecord getNextRecord() {
            CSVRecord record = null;
            if (format.useRow(recordNumber + 1)) {
                record = Uncheck.get(CSVParser.this::nextRecord);
            }
            return record;
        }

        @Override
        public boolean hasNext() {
            if (isClosed()) {
                return false;
            }
            if (current == null) {
                current = getNextRecord();
            }
            return current != null;
        }

        @Override
        public CSVRecord next() {
            if (isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            CSVRecord next = current;
            current = null;
            if (next == null) {
                // hasNext() wasn't called before
                next = getNextRecord();
                if (next == null) {
                    throw new NoSuchElementException("No more CSV records available");
                }
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * Header information based on name and position.
     */
    private static final class Headers {

        /**
         * Header column positions (0-based)
         */
        final Map<String, Integer> headerMap;

        /**
         * Header names in column order
         */
        final List<String> headerNames;

        Headers(final Map<String, Integer> headerMap, final List<String> headerNames) {
            this.headerMap = headerMap;
            this.headerNames = headerNames;
        }
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder.
     * @since 1.13.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a parser for the given {@link File}.
     *
     * @param file
     *            a CSV file. Must not be null.
     * @param charset
     *            The Charset to decode the given file, {@code null} maps to the {@link Charset#defaultCharset() default Charset}.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new parser
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent.
     * @throws IOException
     *             If an I/O error occurs
     * @throws CSVException Thrown on invalid CSV input data.
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    public static CSVParser parse(final File file, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(file, "file");
        return parse(file.toPath(), charset, format);
    }

    /**
     * Creates a CSV parser using the given {@link CSVFormat}.
     *
     * <p>
     * If you do not read all records from the given {@code reader}, you should call {@link #close()} on the parser,
     * unless you close the {@code reader}.
     * </p>
     *
     * @param inputStream
     *            an InputStream containing CSV-formatted input, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @param charset
     *            The Charset to decode the given file, {@code null} maps to the {@link Charset#defaultCharset() default Charset}.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new CSVParser configured with the given reader and format.
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent or if either reader or format are null.
     * @throws IOException
     *             If there is a problem reading the header or skipping the first record
     * @throws CSVException Thrown on invalid CSV input data.
     * @since 1.5
     */
    public static CSVParser parse(final InputStream inputStream, final Charset charset, final CSVFormat format)
            throws IOException {
        return parse(new InputStreamReader(inputStream, Charsets.toCharset(charset)), format);
    }

    /**
     * Creates and returns a parser for the given {@link Path}, which the caller MUST close.
     *
     * @param path
     *            a CSV file. Must not be null.
     * @param charset
     *            The Charset to decode the given file, {@code null} maps to the {@link Charset#defaultCharset() default Charset}.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new parser
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent.
     * @throws IOException
     *             If an I/O error occurs
     * @throws CSVException Thrown on invalid CSV input data.
     * @throws NullPointerException if {@code path} is {@code null}.
     * @since 1.5
     */
    @SuppressWarnings("resource")
    public static CSVParser parse(final Path path, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(path, "path");
        return parse(Files.newInputStream(path), charset, format);
    }

    /**
     * Creates a CSV parser using the given {@link CSVFormat}
     *
     * <p>
     * If you do not read all records from the given {@code reader}, you should call {@link #close()} on the parser,
     * unless you close the {@code reader}.
     * </p>
     *
     * @param reader
     *            a Reader containing CSV-formatted input. Must not be null.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new CSVParser configured with the given reader and format.
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent or if either reader or format are null.
     * @throws IOException
     *             If there is a problem reading the header or skipping the first record
     * @throws CSVException Thrown on invalid CSV input data.
     * @since 1.5
     */
    public static CSVParser parse(final Reader reader, final CSVFormat format) throws IOException {
        return builder().setReader(reader).setFormat(format).get();
    }

    /**
     * Creates a parser for the given {@link String}.
     *
     * @param string
     *            a CSV string. Must not be null.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new parser
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent.
     * @throws IOException
     *             If an I/O error occurs
     * @throws CSVException Thrown on invalid CSV input data.
     * @throws NullPointerException if {@code string} is {@code null}.
     */
    public static CSVParser parse(final String string, final CSVFormat format) throws IOException {
        Objects.requireNonNull(string, "string");
        return parse(new StringReader(string), format);
    }

    /**
     * Creates and returns a parser for the given URL, which the caller MUST close.
     *
     * <p>
     * If you do not read all records from the given {@code url}, you should call {@link #close()} on the parser, unless
     * you close the {@code url}.
     * </p>
     *
     * @param url
     *            a URL. Must not be null.
     * @param charset
     *            the charset for the resource, {@code null} maps to the {@link Charset#defaultCharset() default Charset}.
     * @param format
     *            the CSVFormat used for CSV parsing, {@code null} maps to {@link CSVFormat#DEFAULT}.
     * @return a new parser
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent.
     * @throws IOException
     *             If an I/O error occurs
     * @throws CSVException Thrown on invalid CSV input data.
     * @throws NullPointerException if {@code url} is {@code null}.
     */
    @SuppressWarnings("resource")
    public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(url, "url");
        return parse(url.openStream(), charset, format);
    }

    private String headerComment;

    private String trailerComment;

    private final CSVFormat format;

    private final Headers headers;

    private final Lexer lexer;

    private final CSVRecordIterator csvRecordIterator;

    /** A record buffer for getRecord(). Grows as necessary and is reused. */
    private final List<String> recordList = new ArrayList<>();

    /**
     * The next record number to assign.
     */
    private long recordNumber;

    /**
     * Lexer offset when the parser does not start parsing at the beginning of the source. Usually used in combination
     * with {@link #recordNumber}.
     */
    private final long characterOffset;

    private final Token reusableToken = new Token();

    /**
     * Constructs a new instance using the given {@link CSVFormat}.
     *
     * <p>
     * If you do not read all records from the given {@code reader}, you should call {@link #close()} on the parser,
     * unless you close the {@code reader}.
     * </p>
     *
     * @param reader
     *            a Reader containing CSV-formatted input. Must not be null.
     * @param format
     *            the CSVFormat used for CSV parsing. Must not be null.
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent or if either reader or format are null.
     * @throws IOException
     *             If there is a problem reading the header or skipping the first record
     * @throws CSVException Thrown on invalid CSV input data.
     * @deprecated Will be removed in the next major version, use {@link Builder#get()}.
     */
    @Deprecated
    public CSVParser(final Reader reader, final CSVFormat format) throws IOException {
        this(reader, format, 0, 1);
    }

    /**
     * Constructs a new instance using the given {@link CSVFormat}.
     *
     * <p>
     * If you do not read all records from the given {@code reader}, you should call {@link #close()} on the parser,
     * unless you close the {@code reader}.
     * </p>
     *
     * @param reader
     *            a Reader containing CSV-formatted input. Must not be null.
     * @param format
     *            the CSVFormat used for CSV parsing. Must not be null.
     * @param characterOffset
     *            Lexer offset when the parser does not start parsing at the beginning of the source.
     * @param recordNumber
     *            The next record number to assign.
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent or if either the reader or format is null.
     * @throws IOException
     *             if there is a problem reading the header or skipping the first record
     * @throws CSVException on invalid input.
     * @since 1.1
     * @deprecated Will be removed in the next major version, use {@link Builder#get()}.
     */
    @Deprecated
    public CSVParser(final Reader reader, final CSVFormat format, final long characterOffset, final long recordNumber) throws IOException {
        this(reader, format, characterOffset, recordNumber, null, false);
    }

    /**
     * Constructs a new instance using the given {@link CSVFormat}.
     *
     * <p>
     * If you do not read all records from the given {@code reader}, you should call {@link #close()} on the parser,
     * unless you close the {@code reader}.
     * </p>
     *
     * @param reader
     *            a Reader containing CSV-formatted input. Must not be null.
     * @param format
     *            the CSVFormat used for CSV parsing. Must not be null.
     * @param characterOffset
     *            Lexer offset when the parser does not start parsing at the beginning of the source.
     * @param recordNumber
     *            The next record number to assign.
     * @param charset
     *            The character encoding to be used for the reader when enableByteTracking is true.
     * @param trackBytes
     *           {@code true} to enable byte tracking for the parser; {@code false} to disable it.
     * @throws IllegalArgumentException
     *             If the parameters of the format are inconsistent or if either the reader or format is null.
     * @throws IOException
     *             If there is a problem reading the header or skipping the first record.
     * @throws CSVException Thrown on invalid CSV input data.
     */
    @SuppressWarnings("resource") // reader is managed by lexer.
    private CSVParser(final Reader reader, final CSVFormat format, final long characterOffset, final long recordNumber, final Charset charset,
            final boolean trackBytes) throws IOException {
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(format, "format");
        this.format = format.copy();
        this.lexer = new Lexer(format, new ExtendedBufferedReader(reader, charset, trackBytes));
        this.csvRecordIterator = new CSVRecordIterator();
        this.headers = createHeaders();
        this.characterOffset = characterOffset;
        this.recordNumber = recordNumber - 1;
    }

    private void addRecordValue(final boolean lastRecord) {
        final String input = format.trim(reusableToken.content.toString());
        if (lastRecord && input.isEmpty() && format.getTrailingDelimiter()) {
            return;
        }
        recordList.add(handleNull(input));
    }

    /**
     * Closes resources.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        lexer.close();
    }

    private Map<String, Integer> createEmptyHeaderMap() {
        return format.getIgnoreHeaderCase() ?
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER) :
                new LinkedHashMap<>();
    }

    /**
     * Creates the name to index mapping if the format defines a header.
     *
     * @return null if the format has no header.
     * @throws IOException if there is a problem reading the header or skipping the first record
     * @throws CSVException on invalid input.
     */
    private Headers createHeaders() throws IOException {
        Map<String, Integer> headerMap = null;
        List<String> headerNames = null;
        final String[] formatHeader = format.getHeader();
        if (formatHeader != null) {
            headerMap = createEmptyHeaderMap();
            String[] headerRecord = null;
            if (formatHeader.length == 0) {
                // read the header from the first line of the file
                final CSVRecord nextRecord = nextRecord();
                if (nextRecord != null) {
                    headerRecord = nextRecord.values();
                    headerComment = nextRecord.getComment();
                }
            } else {
                if (format.getSkipHeaderRecord()) {
                    final CSVRecord nextRecord = nextRecord();
                    if (nextRecord != null) {
                        headerComment = nextRecord.getComment();
                    }
                }
                headerRecord = formatHeader;
            }
            // build the name to index mappings
            if (headerRecord != null) {
                // Track an occurrence of a null, empty or blank header.
                boolean observedMissing = false;
                for (int i = 0; i < headerRecord.length; i++) {
                    final String header = headerRecord[i];
                    final boolean blankHeader = CSVFormat.isBlank(header);
                    if (blankHeader && !format.getAllowMissingColumnNames()) {
                        throw new IllegalArgumentException("A header name is missing in " + Arrays.toString(headerRecord));
                    }
                    final boolean containsHeader = blankHeader ? observedMissing : headerMap.containsKey(header);
                    final DuplicateHeaderMode headerMode = format.getDuplicateHeaderMode();
                    final boolean duplicatesAllowed = headerMode == DuplicateHeaderMode.ALLOW_ALL;
                    final boolean emptyDuplicatesAllowed = headerMode == DuplicateHeaderMode.ALLOW_EMPTY;
                    if (containsHeader && !duplicatesAllowed && !(blankHeader && emptyDuplicatesAllowed)) {
                        throw new IllegalArgumentException(String.format(
                                "The header contains a duplicate name: \"%s\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().",
                                header, Arrays.toString(headerRecord)));
                    }
                    observedMissing |= blankHeader;
                    if (header != null) {
                        headerMap.put(header, Integer.valueOf(i)); // Explicit (un)boxing is intentional
                        if (headerNames == null) {
                            headerNames = new ArrayList<>(headerRecord.length);
                        }
                        headerNames.add(header);
                    }
                }
            }
        }
        // Make header names Collection immutable
        return new Headers(headerMap, headerNames == null ? Collections.emptyList() : Collections.unmodifiableList(headerNames));
    }

    /**
     * Gets the current line number in the input stream.
     *
     * <p>
     * <strong>Note:</strong> If your CSV input has multi-line values, the returned number does not correspond to
     * the record number.
     * </p>
     *
     * @return current line number.
     */
    public long getCurrentLineNumber() {
        return lexer.getCurrentLineNumber();
    }

    /**
     * Gets the first end-of-line string encountered.
     *
     * @return the first end-of-line string.
     * @since 1.5
     */
    public String getFirstEndOfLine() {
        return lexer.getFirstEol();
    }

    /**
     * Gets the header comment, if any.
     * The header comment appears before the header record.
     *
     * @return the header comment for this stream, or null if no comment is available.
     * @since 1.10.0
     */
    public String getHeaderComment() {
        return headerComment;
    }

    /**
     * Gets a copy of the header map as defined in the CSVFormat's header.
     * <p>
     * The map keys are column names. The map values are 0-based indices.
     * </p>
     * <p>
     * <strong>Note:</strong> The map can only provide a one-to-one mapping when the format did not
     * contain null or duplicate column names.
     * </p>
     *
     * @return a copy of the header map.
     */
    public Map<String, Integer> getHeaderMap() {
        if (headers.headerMap == null) {
            return null;
        }
        final Map<String, Integer> map = createEmptyHeaderMap();
        map.putAll(headers.headerMap);
        return map;
    }

    /**
     * Gets the underlying header map.
     *
     * @return the underlying header map.
     */
    Map<String, Integer> getHeaderMapRaw() {
        return headers.headerMap;
    }

    /**
     * Gets a read-only list of header names that iterates in column order as defined in the CSVFormat's header.
     * <p>
     * Note: The list provides strings that can be used as keys in the header map.
     * The list will not contain null column names if they were present in the input
     * format.
     * </p>
     *
     * @return read-only list of header names that iterates in column order.
     * @see #getHeaderMap()
     * @since 1.7
     */
    public List<String> getHeaderNames() {
        return Collections.unmodifiableList(headers.headerNames);
    }

    /**
     * Gets the current record number in the input stream.
     *
     * <p>
     * <strong>Note:</strong> If your CSV input has multi-line values, the returned number does not correspond to
     * the line number.
     * </p>
     *
     * @return current record number
     */
    public long getRecordNumber() {
        return recordNumber;
    }

    /**
     * Parses the CSV input according to the given format and returns the content as a list of
     * {@link CSVRecord CSVRecords}.
     *
     * <p>
     * The returned content starts at the current parse-position in the stream.
     * </p>
     * <p>
     * You can use {@link CSVFormat.Builder#setMaxRows(long)} to limit how many rows this method produces.
     * </p>
     *
     * @return list of {@link CSVRecord CSVRecords}, may be empty
     * @throws UncheckedIOException
     *             on parse error or input read-failure
     */
    public List<CSVRecord> getRecords() {
        return stream().collect(Collectors.toList());
    }

    /**
     * Gets the trailer comment, if any.
     * Trailer comments are located between the last record and EOF
     *
     * @return the trailer comment for this stream, or null if no comment is available.
     * @since 1.10.0
     */
    public String getTrailerComment() {
        return trailerComment;
    }

    /**
     * Handles whether the input is parsed as null
     *
     * @param input
     *           the cell data to further processed
     * @return null if input is parsed as null, or input itself if the input isn't parsed as null
     */
    private String handleNull(final String input) {
        final boolean isQuoted = reusableToken.isQuoted;
        final String nullString = format.getNullString();
        final boolean strictQuoteMode = isStrictQuoteMode();
        if (input.equals(nullString)) {
            // nullString = NULL(String), distinguish between "NULL" and NULL in ALL_NON_NULL or NON_NUMERIC quote mode
            return strictQuoteMode && isQuoted ? input : null;
        }
        // don't set nullString, distinguish between "" and ,, (absent values) in All_NON_NULL or NON_NUMERIC quote mode
        return strictQuoteMode && nullString == null && input.isEmpty() && !isQuoted ? null : input;
    }

    /**
     * Checks whether there is a header comment.
     * The header comment appears before the header record.
     * Note that if the parser's format has been given an explicit header
     * (with {@link CSVFormat.Builder#setHeader(String... )} or another overload)
     * and the header record is not being skipped
     * ({@link CSVFormat.Builder#setSkipHeaderRecord} is false) then any initial comments
     * will be associated with the first record, not the header.
     *
     * @return true if this parser has seen a header comment, false otherwise
     * @since 1.10.0
     */
    public boolean hasHeaderComment() {
        return headerComment != null;
    }

    /**
     * Checks whether there is a trailer comment.
     * Trailer comments are located between the last record and EOF.
     * The trailer comments will only be available after the parser has
     * finished processing this stream.
     *
     * @return true if this parser has seen a trailer comment, false otherwise
     * @since 1.10.0
     */
    public boolean hasTrailerComment() {
        return trailerComment != null;
    }

    /**
     * Tests whether this parser is closed.
     *
     * @return whether this parser is closed.
     */
    public boolean isClosed() {
        return lexer.isClosed();
    }

    /**
     * Tests whether the format's {@link QuoteMode} is {@link QuoteMode#ALL_NON_NULL} or {@link QuoteMode#NON_NUMERIC}.
     *
     * @return true if the format's {@link QuoteMode} is {@link QuoteMode#ALL_NON_NULL} or
     *         {@link QuoteMode#NON_NUMERIC}.
     */
    private boolean isStrictQuoteMode() {
        return format.getQuoteMode() == QuoteMode.ALL_NON_NULL ||
               format.getQuoteMode() == QuoteMode.NON_NUMERIC;
    }

    /**
     * Returns the record iterator.
     *
     * <p>
     * An {@link IOException} caught during the iteration is re-thrown as an
     * {@link IllegalStateException}.
     * </p>
     * <p>
     * If the parser is closed, the iterator will not yield any more records.
     * A call to {@link Iterator#hasNext()} will return {@code false} and
     * a call to {@link Iterator#next()} will throw a
     * {@link NoSuchElementException}.
     * </p>
     * <p>
     * If it is necessary to construct an iterator which is usable after the
     * parser is closed, one option is to extract all records as a list with
     * {@link #getRecords()}, and return an iterator to that list.
     * </p>
     * <p>
     * You can use {@link CSVFormat.Builder#setMaxRows(long)} to limit how many rows an Iterator produces.
     * </p>
     */
    @Override
    public Iterator<CSVRecord> iterator() {
        return csvRecordIterator;
    }

    /**
     * Parses the next record from the current point in the stream.
     *
     * @return the record as an array of values, or {@code null} if the end of the stream has been reached.
     * @throws IOException  on parse error or input read-failure.
     * @throws CSVException on invalid CSV input data.
     */
    CSVRecord nextRecord() throws IOException {
        CSVRecord result = null;
        recordList.clear();
        StringBuilder sb = null;
        final long startCharPosition = lexer.getCharacterPosition() + characterOffset;
        final long startBytePosition = lexer.getBytesRead() + characterOffset;
        do {
            reusableToken.reset();
            lexer.nextToken(reusableToken);
            switch (reusableToken.type) {
            case TOKEN:
                addRecordValue(false);
                break;
            case EORECORD:
                addRecordValue(true);
                break;
            case EOF:
                if (reusableToken.isReady) {
                    addRecordValue(true);
                } else if (sb != null) {
                    trailerComment = sb.toString();
                }
                break;
            case INVALID:
                throw new CSVException("(line %,d) invalid parse sequence", getCurrentLineNumber());
            case COMMENT: // Ignored currently
                if (sb == null) { // first comment for this record
                    sb = new StringBuilder();
                } else {
                    sb.append(Constants.LF);
                }
                sb.append(reusableToken.content);
                reusableToken.type = TOKEN; // Read another token
                break;
            default:
                throw new CSVException("Unexpected Token type: %s", reusableToken.type);
            }
        } while (reusableToken.type == TOKEN);
        if (!recordList.isEmpty()) {
            recordNumber++;
            result = new CSVRecord(this, recordList.toArray(Constants.EMPTY_STRING_ARRAY), Objects.toString(sb, null), recordNumber, startCharPosition,
                    startBytePosition);
        }
        return result;
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     * <p>
     * If the parser is closed, the stream will not produce any more values.
     * See the comments in {@link #iterator()}.
     * </p>
     * <p>
     * You can use {@link CSVFormat.Builder#setMaxRows(long)} to limit how many rows a Stream produces.
     * </p>
     *
     * @return a sequential {@code Stream} with this collection as its source.
     * @since 1.9.0
     */
    public Stream<CSVRecord> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

}
