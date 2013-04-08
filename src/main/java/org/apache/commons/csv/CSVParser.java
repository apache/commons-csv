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

import static org.apache.commons.csv.Token.Type.TOKEN;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Parses CSV files according to the specified configuration.
 *
 * Because CSV appears in many different dialects, the parser supports many configuration settings by allowing the
 * specification of a {@link CSVFormat}.
 *
 * <p>
 * To parse a CSV input with tabs as separators, '"' (double-quote) as an optional value encapsulator,
 * and comments starting with '#', you write:
 * </p>
 *
 * <pre>
 * Reader in = new StringReader(&quot;a\tb\nc\td&quot;);
 * Iterable&lt;CSVRecord&gt; parser = CSVFormat.newBuilder()
 *     .withCommentStart('#')
 *     .withDelimiter('\t')
 *     .withQuoteChar('"').parse(in);
 *  for (CSVRecord csvRecord : parse) {
 *     ...
 *  }
 * </pre>
 *
 * <p>
 * To parse CSV input in a given format like Excel, you write:
 * </p>
 *
 * <pre>
 * Reader in = new StringReader("a;b\nc;d");
 * Iterable&lt;CSVRecord&gt; parser = CSVFormat.EXCEL.parse(in);
 * for (CSVRecord record : parser) {
 *     ...
 * }
 * </pre>
 * <p>
 * You may also get a List of records:
 * </p>
 * <pre>
 * Reader in = new StringReader("a;b\nc;d");
 * CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
 * List&lt;CSVRecord&gt; list = parser.getRecords();
 * </pre>
 * <p>
 * Internal parser state is completely covered by the format and the reader-state.
 * </p>
 *
 * <p>
 * see <a href="package-summary.html">package documentation</a> for more details
 * </p>
 *
 * @version $Id$
 */
public class CSVParser implements Iterable<CSVRecord> {

    private final Lexer lexer;
    private final Map<String, Integer> headerMap;
    private long recordNumber;

    // the following objects are shared to reduce garbage

    /** A record buffer for getRecord(). Grows as necessary and is reused. */
    private final List<String> record = new ArrayList<String>();
    private final Token reusableToken = new Token();

    /**
     * CSV parser using the default {@link CSVFormat}.
     *
     * @param input
     *            a Reader containing "csv-formatted" input
     * @throws IllegalArgumentException
     *             thrown if the parameters of the format are inconsistent
     * @throws IOException
     *             If an I/O error occurs
     */
    public CSVParser(final Reader input) throws IOException {
        this(input, CSVFormat.DEFAULT);
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input
     *            a Reader containing CSV-formatted input
     * @param format
     *            the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException
     *             thrown if the parameters of the format are inconsistent
     * @throws IOException
     *             If an I/O error occurs
     */
    public CSVParser(final Reader input, final CSVFormat format) throws IOException {
        this.lexer = new CSVLexer(format, new ExtendedBufferedReader(input));
        this.headerMap = initializeHeader(format);
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input
     *            a String containing "csv-formatted" input
     * @param format
     *            the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException
     *             thrown if the parameters of the format are inconsistent
     * @throws IOException
     *             If an I/O error occurs
     */
    public CSVParser(final String input, final CSVFormat format) throws IOException {
        this(new StringReader(input), format);
    }

    /**
     * Returns a copy of the header map that iterates in column order.
     * <p>
     * The map keys are column names.
     * The map values are 0-based indices.
     *
     * @return a copy of the header map that iterates in column order.
     */
    public Map<String, Integer> getHeaderMap() {
        return new LinkedHashMap<String, Integer>(headerMap);
    }

    /**
     * Returns the current line number in the input stream.
     * <p/>
     * ATTENTION: If your CSV input has multi-line values, the returned number does not correspond to the record number.
     *
     * @return current line number
     */
    public long getLineNumber() {
        return lexer.getLineNumber();
    }

    /**
     * Returns the current record number in the input stream.
     * <p/>
     * ATTENTION: If your CSV input has multi-line values, the returned number does not correspond to the line number.
     *
     * @return current line number
     */
    public long getRecordNumber() {
        return recordNumber;
    }

    /**
     * Parses the next record from the current point in the stream.
     *
     * @return the record as an array of values, or <tt>null</tt> if the end of the stream has been reached
     * @throws IOException
     *             on parse error or input read-failure
     */
    CSVRecord nextRecord() throws IOException {
        CSVRecord result = null;
        record.clear();
        StringBuilder sb = null;
        do {
            reusableToken.reset();
            lexer.nextToken(reusableToken);
            switch (reusableToken.type) {
            case TOKEN:
                record.add(reusableToken.content.toString());
                break;
            case EORECORD:
                record.add(reusableToken.content.toString());
                break;
            case EOF:
                if (reusableToken.isReady) {
                    record.add(reusableToken.content.toString());
                }
                break;
            case INVALID:
                throw new IOException("(line " + getLineNumber() + ") invalid parse sequence");
            case COMMENT: // Ignored currently
                if (sb == null) { // first comment for this record
                    sb = new StringBuilder();
                } else {
                    sb.append("\n");
                }
                sb.append(reusableToken.content);
                reusableToken.type = TOKEN; // Read another token
                break;
            }
        } while (reusableToken.type == TOKEN);

        if (!record.isEmpty()) {
            recordNumber++;
            final String comment = sb == null ? null : sb.toString();
            result = new CSVRecord(record.toArray(new String[record.size()]), headerMap, comment, this.recordNumber);
        }
        return result;
    }

    /**
     * Parses the CSV input according to the given format and returns the content as an array of {@link CSVRecord}
     * entries.
     * <p/>
     * The returned content starts at the current parse-position in the stream.
     *
     * @return list of {@link CSVRecord} entries, may be empty
     * @throws IOException
     *             on parse error or input read-failure
     */
    public List<CSVRecord> getRecords() throws IOException {
        final List<CSVRecord> records = new ArrayList<CSVRecord>();
        CSVRecord rec;
        while ((rec = nextRecord()) != null) {
            records.add(rec);
        }
        return records;
    }

    /**
     * Initializes the name to index mapping if the format defines a header.
     */
    private Map<String, Integer> initializeHeader(final CSVFormat format) throws IOException {
        Map<String, Integer> hdrMap = null;
        if (format.getHeader() != null) {
            hdrMap = new LinkedHashMap<String, Integer>();

            String[] header = null;
            if (format.getHeader().length == 0) {
                // read the header from the first line of the file
                final CSVRecord record = nextRecord();
                if (record != null) {
                    header = record.values();
                }
            } else {
                header = format.getHeader();
            }

            // build the name to index mappings
            if (header != null) {
                for (int i = 0; i < header.length; i++) {
                    hdrMap.put(header[i], Integer.valueOf(i));
                }
            }
        }
        return hdrMap;
    }

    /**
     * Returns an iterator on the records. IOExceptions occurring during the iteration are wrapped in a
     * RuntimeException.
     */
    public Iterator<CSVRecord> iterator() {
        return new Iterator<CSVRecord>() {
            private CSVRecord current;

            private CSVRecord getNextRecord() {
                try {
                    return nextRecord();
                } catch (final IOException e) {
                    // TODO: This is not great, throw an ISE instead?
                    throw new RuntimeException(e);
                }
            }

            public boolean hasNext() {
                if (current == null) {
                    current = getNextRecord();
                }

                return current != null;
            }

            public CSVRecord next() {
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

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
