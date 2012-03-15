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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVLexer.Token;

import static org.apache.commons.csv.CSVLexer.Token.Type.*;

/**
 * Parses CSV files according to the specified configuration.
 *
 * Because CSV appears in many different dialects, the parser supports many
 * configuration settings by allowing the specification of a {@link CSVFormat}.
 *
 * <p>Parsing of a csv-string having tabs as separators,
 * '"' as an optional value encapsulator, and comments starting with '#':</p>
 * <pre>
 * CSVFormat format = new CSVFormat('\t', '"', '#');
 * Reader in = new StringReader("a\tb\nc\td");
 * String[][] records = new CSVParser(in, format).getRecords();
 * </pre>
 *
 * <p>Parsing of a csv-string in Excel CSV format, using a for-each loop:</p>
 * <pre>
 * Reader in = new StringReader("a;b\nc;d");
 * CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
 * for (String[] record : parser) {
 *     ...
 * }
 * </pre>
 *
 * <p>
 * Internal parser state is completely covered by the format
 * and the reader-state.</p>
 *
 * <p>see <a href="package-summary.html">package documentation</a>
 * for more details</p>
 */
public class CSVParser implements Iterable<String[]> {

    /** Immutable empty String array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final CSVLexer lexer;
    
    // the following objects are shared to reduce garbage
    
    /** A record buffer for getRecord(). Grows as necessary and is reused. */
    private final List<String> record = new ArrayList<String>();
    private final Token reusableToken = new Token();

    /**
     * CSV parser using the default {@link CSVFormat}.
     *
     * @param input a Reader containing "csv-formatted" input
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(Reader input) {
        this(input, CSVFormat.DEFAULT);
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input    a Reader containing "csv-formatted" input
     * @param format the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(Reader input, CSVFormat format) {
        format.validate();
        
        if (format.isUnicodeEscapesInterpreted()) {
            input = new UnicodeUnescapeReader(input);
        }
        
        this.lexer = new CSVLexer(format, new ExtendedBufferedReader(input));
    }

    /**
     * Customized CSV parser using the given {@link CSVFormat}
     *
     * @param input    a String containing "csv-formatted" input
     * @param format the CSVFormat used for CSV parsing
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVParser(String input, CSVFormat format) {
        this(new StringReader(input), format);
    }


    /**
     * Parses the CSV input according to the given format and returns the content
     * as an array of records (whereas records are arrays of single values).
     * <p/>
     * The returned content starts at the current parse-position in the stream.
     *
     * @return matrix of records x values ('null' when end of file)
     * @throws IOException on parse error or input read-failure
     */
    public String[][] getRecords() throws IOException {
        List<String[]> records = new ArrayList<String[]>();
        String[] record;
        while ((record = getRecord()) != null) {
            records.add(record);
        }
        
        if (!records.isEmpty()) {
            return records.toArray(new String[records.size()][]);
        } else {
            return null;
        }
    }

    /**
     * Parses the next record from the current point in the stream.
     *
     * @return the record as an array of values, or <tt>null</tt> if the end of the stream has been reached
     * @throws IOException on parse error or input read-failure
     */
    String[] getRecord() throws IOException {
        String[] result = EMPTY_STRING_ARRAY;
        record.clear();
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
                    } else {
                        result = null;
                    }
                    break;
                case INVALID:
                    // error: throw IOException
                    throw new IOException("(line " + getLineNumber() + ") invalid parse sequence");
                    // unreachable: break;
            }
        } while (reusableToken.type == TOKEN);
        
        if (!record.isEmpty()) {
            result = record.toArray(new String[record.size()]);
        }
        return result;
    }

    /**
     * Returns an iterator on the records. IOExceptions occuring
     * during the iteration are wrapped in a RuntimeException.
     */
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            private String[] current;
            
            public boolean hasNext() {
                if (current == null) {
                    current = getNextRecord();
                }
                
                return current != null;
            }

            public String[] next() {
                String[] next = current;
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
            
            private String[] getNextRecord() {
                try {
                    return getRecord();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns the current line number in the input stream.
     * <p/>
     * ATTENTION: in case your csv has multiline-values the returned
     * number does not correspond to the record-number
     *
     * @return current line number
     */
    public int getLineNumber() {
        return lexer.getLineNumber();
    }
}
