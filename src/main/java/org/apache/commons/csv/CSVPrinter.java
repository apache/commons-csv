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

import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.SP;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Prints values in a {@link CSVFormat CSV format}.
 *
 * <p>Values can be appended to the output by calling the {@link #print(Object)} method.
 * Values are printed according to {@link String#valueOf(Object)}.
 * To complete a record the {@link #println()} method has to be called.
 * Comments can be appended by calling {@link #printComment(String)}.
 * However a comment will only be written to the output if the {@link CSVFormat} supports comments.
 * </p>
 *
 * <p>The printer also supports appending a complete record at once by calling {@link #printRecord(Object...)}
 * or {@link #printRecord(Iterable)}.
 * Furthermore {@link #printRecords(Object...)}, {@link #printRecords(Iterable)} and {@link #printRecords(ResultSet)}
 * methods can be used to print several records at once.
 * </p>
 *
 * <p>Example:</p>
 *
 * <pre>
 * try (CSVPrinter printer = new CSVPrinter(new FileWriter("csv.txt"), CSVFormat.EXCEL)) {
 *     printer.printRecord("id", "userName", "firstName", "lastName", "birthday");
 *     printer.printRecord(1, "john73", "John", "Doe", LocalDate.of(1973, 9, 15));
 *     printer.println();
 *     printer.printRecord(2, "mary", "Mary", "Meyer", LocalDate.of(1985, 3, 29));
 * } catch (IOException ex) {
 *     ex.printStackTrace();
 * }
 * </pre>
 *
 * <p>This code will write the following to csv.txt:</p>
 * <pre>
 * id,userName,firstName,lastName,birthday
 * 1,john73,John,Doe,1973-09-15
 *
 * 2,mary,Mary,Meyer,1985-03-29
 * </pre>
 */
public final class CSVPrinter implements Flushable, Closeable {

    /** The place that the values get written. */
    private final Appendable appendable;

    private final CSVFormat format;

    /** True if we just began a new record. */
    private boolean newRecord = true;

    /**
     * Creates a printer that will print values to the given stream following the CSVFormat.
     * <p>
     * Currently, only a pure encapsulation format or a pure escaping format is supported. Hybrid formats (encapsulation
     * and escaping with a different character) are not supported.
     * </p>
     *
     * @param appendable
     *            stream to which to print. Must not be null.
     * @param format
     *            the CSV format. Must not be null.
     * @throws IOException
     *             thrown if the optional header cannot be printed.
     * @throws IllegalArgumentException
     *             thrown if the parameters of the format are inconsistent or if either out or format are null.
     */
    public CSVPrinter(final Appendable appendable, final CSVFormat format) throws IOException {
        Objects.requireNonNull(appendable, "appendable");
        Objects.requireNonNull(format, "format");

        this.appendable = appendable;
        this.format = format.copy();
        // TODO: Is it a good idea to do this here instead of on the first call to a print method?
        // It seems a pain to have to track whether the header has already been printed or not.
        final String[] headerComments = format.getHeaderComments();
        if (headerComments != null) {
            for (final String line : headerComments) {
                this.printComment(line);
            }
        }
        if (format.getHeader() != null && !format.getSkipHeaderRecord()) {
            this.printRecord((Object[]) format.getHeader());
        }
    }

    @Override
    public void close() throws IOException {
        close(false);
    }

    /**
     * Closes the underlying stream with an optional flush first.
     * @param flush whether to flush before the actual close.
     *
     * @throws IOException
     *             If an I/O error occurs
     * @since 1.6
     */
    public void close(final boolean flush) throws IOException {
        if (flush || format.getAutoFlush()) {
            flush();
        }
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }

    /**
     * Flushes the underlying stream.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    /**
     * Gets the target Appendable.
     *
     * @return the target Appendable.
     */
    public Appendable getOut() {
        return this.appendable;
    }

    /**
     * Prints the string as the next value on the line. The value will be escaped or encapsulated as needed.
     *
     * @param value
     *            value to be output.
     * @throws IOException
     *             If an I/O error occurs
     */
    public synchronized void print(final Object value) throws IOException {
        format.print(value, appendable, newRecord);
        newRecord = false;
    }

    /**
     * Prints a comment on a new line among the delimiter separated values.
     *
     * <p>
     * Comments will always begin on a new line and occupy at least one full line. The character specified to start
     * comments and a space will be inserted at the beginning of each new line in the comment.
     * </p>
     *
     * <p>
     * If comments are disabled in the current CSV format this method does nothing.
     * </p>
     *
     * <p>This method detects line breaks inside the comment string and inserts {@link CSVFormat#getRecordSeparator()}
     * to start a new line of the comment. Note that this might produce unexpected results for formats that do not use
     * line breaks as record separator.</p>
     *
     * @param comment
     *            the comment to output
     * @throws IOException
     *             If an I/O error occurs
     */
    public synchronized void printComment(final String comment) throws IOException {
        if (comment == null || !format.isCommentMarkerSet()) {
            return;
        }
        if (!newRecord) {
            println();
        }
        appendable.append(format.getCommentMarker().charValue());
        appendable.append(SP);
        for (int i = 0; i < comment.length(); i++) {
            final char c = comment.charAt(i);
            switch (c) {
            case CR:
                if (i + 1 < comment.length() && comment.charAt(i + 1) == LF) {
                    i++;
                }
                //$FALL-THROUGH$ break intentionally excluded.
            case LF:
                println();
                appendable.append(format.getCommentMarker().charValue());
                appendable.append(SP);
                break;
            default:
                appendable.append(c);
                break;
            }
        }
        println();
    }

    /**
     * Prints headers for a result set based on its metadata.
     *
     * @param resultSet The result set to query for metadata.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If a database access error occurs or this method is called on a closed result set.
     * @since 1.9.0
     */
    public synchronized void printHeaders(final ResultSet resultSet) throws IOException, SQLException {
        printRecord((Object[]) format.builder().setHeader(resultSet).build().getHeader());
    }

    /**
     * Outputs the record separator.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    public synchronized void println() throws IOException {
        format.println(appendable);
        newRecord = true;
    }

    /**
     * Prints the given values as a single record of delimiter separated values followed by the record separator.
     *
     * <p>
     * The values will be quoted if needed. Quotes and newLine characters will be escaped. This method adds the record
     * separator to the output after printing the record, so there is no need to call {@link #println()}.
     * </p>
     *
     * @param values
     *            values to output.
     * @throws IOException
     *             If an I/O error occurs
     */
    public synchronized void printRecord(final Iterable<?> values) throws IOException {
        for (final Object value : values) {
            print(value);
        }
        println();
    }

    /**
     * Prints the given values as a single record of delimiter separated values followed by the record separator.
     *
     * <p>
     * The values will be quoted if needed. Quotes and newLine characters will be escaped. This method adds the record
     * separator to the output after printing the record, so there is no need to call {@link #println()}.
     * </p>
     *
     * @param values
     *            values to output.
     * @throws IOException
     *             If an I/O error occurs
     */
    public void printRecord(final Object... values) throws IOException {
        printRecord(Arrays.asList(values));
    }

    /**
     * Prints the given values as a single record of delimiter separated values followed by the record separator.
     *
     * <p>
     * The values will be quoted if needed. Quotes and newLine characters will be escaped. This method adds the record
     * separator to the output after printing the record, so there is no need to call {@link #println()}.
     * </p>
     *
     * @param values
     *            values to output.
     * @throws IOException
     *             If an I/O error occurs
     * @since 1.10.0
     */
    public synchronized void printRecord(final Stream<?> values) throws IOException {
        values.forEachOrdered(t -> {
            try {
                print(t);
            } catch (final IOException e) {
                throw IOUtils.rethrow(e);
            }
        });
        println();
    }

    private void printRecordObject(final Object value) throws IOException {
        if (value instanceof Object[]) {
            this.printRecord((Object[]) value);
        } else if (value instanceof Iterable) {
            this.printRecord((Iterable<?>) value);
        } else {
            this.printRecord(value);
        }
    }

    /**
     * Prints all the objects in the given {@link Iterable} handling nested collections/arrays as records.
     *
     * <p>
     * If the given Iterable only contains simple objects, this method will print a single record like
     * {@link #printRecord(Iterable)}. If the given Iterable contains nested collections/arrays those nested elements
     * will each be printed as records using {@link #printRecord(Object...)}.
     * </p>
     *
     * <p>
     * Given the following data structure:
     * </p>
     *
     * <pre>
     * <code>
     * List&lt;String[]&gt; data = new ArrayList&lt;&gt;();
     * data.add(new String[]{ "A", "B", "C" });
     * data.add(new String[]{ "1", "2", "3" });
     * data.add(new String[]{ "A1", "B2", "C3" });
     * </code>
     * </pre>
     *
     * <p>
     * Calling this method will print:
     * </p>
     *
     * <pre>
     * <code>
     * A, B, C
     * 1, 2, 3
     * A1, B2, C3
     * </code>
     * </pre>
     *
     * @param values
     *            the values to print.
     * @throws IOException
     *             If an I/O error occurs
     */
    public void printRecords(final Iterable<?> values) throws IOException {
        for (final Object value : values) {
            printRecordObject(value);
        }
    }

    /**
     * Prints all the objects in the given array handling nested collections/arrays as records.
     *
     * <p>
     * If the given array only contains simple objects, this method will print a single record like
     * {@link #printRecord(Object...)}. If the given collections contains nested collections/arrays those nested
     * elements will each be printed as records using {@link #printRecord(Object...)}.
     * </p>
     *
     * <p>
     * Given the following data structure:
     * </p>
     *
     * <pre>
     * <code>
     * String[][] data = new String[3][]
     * data[0] = String[]{ "A", "B", "C" };
     * data[1] = new String[]{ "1", "2", "3" };
     * data[2] = new String[]{ "A1", "B2", "C3" };
     * </code>
     * </pre>
     *
     * <p>
     * Calling this method will print:
     * </p>
     *
     * <pre>
     * <code>
     * A, B, C
     * 1, 2, 3
     * A1, B2, C3
     * </code>
     * </pre>
     *
     * @param values
     *            the values to print.
     * @throws IOException
     *             If an I/O error occurs
     */
    public void printRecords(final Object... values) throws IOException {
        printRecords(Arrays.asList(values));
    }

    /**
     * Prints all the objects in the given JDBC result set.
     *
     * @param resultSet
     *            result set the values to print.
     * @throws IOException
     *             If an I/O error occurs
     * @throws SQLException
     *             if a database access error occurs
     */
    public void printRecords(final ResultSet resultSet) throws SQLException, IOException {
        final int columnCount = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                final Object object = resultSet.getObject(i);
                // TODO Who manages the Clob? The JDBC driver or must we close it? Is it driver-dependent?
                print(object instanceof Clob ? ((Clob) object).getCharacterStream() : object);
            }
            println();
        }
    }

    /**
     * Prints all the objects with metadata in the given JDBC result set based on the header boolean.
     *
     * @param resultSet source of row data.
     * @param printHeader whether to print headers.
     * @throws IOException If an I/O error occurs
     * @throws SQLException if a database access error occurs
     * @since 1.9.0
     */
    public void printRecords(final ResultSet resultSet, final boolean printHeader) throws SQLException, IOException {
        if (printHeader) {
            printHeaders(resultSet);
        }
        printRecords(resultSet);
    }

    /**
     * Prints all the objects in the given {@link Stream} handling nested collections/arrays as records.
     *
     * <p>
     * If the given Stream only contains simple objects, this method will print a single record like
     * {@link #printRecord(Iterable)}. If the given Stream contains nested collections/arrays those nested elements
     * will each be printed as records using {@link #printRecord(Object...)}.
     * </p>
     *
     * <p>
     * Given the following data structure:
     * </p>
     *
     * <pre>
     * <code>
     * List&lt;String[]&gt; data = new ArrayList&lt;&gt;();
     * data.add(new String[]{ "A", "B", "C" });
     * data.add(new String[]{ "1", "2", "3" });
     * data.add(new String[]{ "A1", "B2", "C3" });
     * Stream&lt;String[]&gt; stream = data.stream();
     * </code>
     * </pre>
     *
     * <p>
     * Calling this method will print:
     * </p>
     *
     * <pre>
     * <code>
     * A, B, C
     * 1, 2, 3
     * A1, B2, C3
     * </code>
     * </pre>
     *
     * @param values
     *            the values to print.
     * @throws IOException
     *             If an I/O error occurs
     * @since 1.10.0
     */
    @SuppressWarnings("unused") // rethrow() throws IOException
    public void printRecords(final Stream<?> values) throws IOException {
        values.forEachOrdered(t -> {
            try {
                printRecordObject(t);
            } catch (final IOException e) {
                throw IOUtils.rethrow(e);
            }
        });
    }
}
