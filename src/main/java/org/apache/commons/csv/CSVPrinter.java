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

import static org.apache.commons.csv.Constants.COMMENT;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.EMPTY;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.SP;

import java.io.Flushable;
import java.io.IOException;

/**
 * Prints values in a CSV format.
 */
public class CSVPrinter {
    
    /** The place that the values get written. */
    private final Appendable out;
    private final CSVFormat format;

    /** True if we just began a new line. */
    private boolean newLine = true;

    /**
     * Creates a printer that will print values to the given stream following the CSVFormat.
     * <p/>
     * Currently, only a pure encapsulation format or a pure escaping format is supported. Hybrid formats
     * (encapsulation and escaping with a different character) are not supported.
     *
     * @param out
     *            stream to which to print.
     * @param format
     *            the CSV format. If null the default format is used ({@link CSVFormat#DEFAULT})
     * @throws IllegalArgumentException
     *             thrown if the parameters of the format are inconsistent
     */
    public CSVPrinter(final Appendable out, final CSVFormat format) {
        this.out = out;
        this.format = format == null ? CSVFormat.DEFAULT : format;
        this.format.validate();
    }

    // ======================================================
    // printing implementation
    // ======================================================

    /**
     * Outputs a blank line
     */
    public void println() throws IOException {
        out.append(format.getLineSeparator());
        newLine = true;
    }

    /**
     * Flushes the underlying stream.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        if (out instanceof Flushable) {
            ((Flushable) out).flush();
        }
    }

    /**
     * Prints a single line of comma separated values. The values will be quoted if needed. Quotes and newLine
     * characters will be escaped.
     *
     * @param values
     *            values to be outputted.
     */
    public void println(final String... values) throws IOException {
        for (final String value : values) {
            print(value);
        }
        println();
    }

    /**
     * Prints a comment on a new line among the comma separated values. Comments will always begin on a new line and
     * occupy a least one full line. The character specified to start comments and a space will be inserted at the
     * beginning of each new line in the comment.
     * <p/>
     * If comments are disabled in the current CSV format this method does nothing.
     *
     * @param comment
     *            the comment to output
     */
    public void printComment(final String comment) throws IOException {
        if (!format.isCommentingEnabled()) {
            return;
        }
        if (!newLine) {
            println();
        }
        out.append(format.getCommentStart());
        out.append(SP);
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
                out.append(format.getCommentStart());
                out.append(SP);
                break;
            default:
                out.append(c);
                break;
            }
        }
        println();
    }

    private void print(final CharSequence value, final int offset, final int len) throws IOException {
        if (format.isEncapsulating()) {
            printAndEncapsulate(value, offset, len);
        } else if (format.isEscaping()) {
            printAndEscape(value, offset, len);
        } else {
            printDelimiter();
            out.append(value, offset, offset + len);
        }
    }

    void printDelimiter() throws IOException {
        if (newLine) {
            newLine = false;
        } else {
            out.append(format.getDelimiter());
        }
    }

    void printAndEscape(final CharSequence value, final int offset, final int len) throws IOException {
        int start = offset;
        int pos = offset;
        final int end = offset + len;

        printDelimiter();

        final char delim = format.getDelimiter();
        final char escape = format.getEscape();

        while (pos < end) {
            char c = value.charAt(pos);
            if (c == CR || c == LF || c == delim || c == escape) {
                // write out segment up until this char
                if (pos > start) {
                    out.append(value, start, pos);
                }
                if (c == LF) {
                    c = 'n';
                } else if (c == CR) {
                    c = 'r';
                }

                out.append(escape);
                out.append(c);

                start = pos + 1; // start on the current char after this one
            }

            pos++;
        }

        // write last segment
        if (pos > start) {
            out.append(value, start, pos);
        }
    }

    void printAndEncapsulate(final CharSequence value, final int offset, final int len) throws IOException {
        final boolean first = newLine; // is this the first value on this line?
        boolean quote = false;
        int start = offset;
        int pos = offset;
        final int end = offset + len;

        printDelimiter();

        final char delim = format.getDelimiter();
        final char encapsulator = format.getEncapsulator();

        if (len <= 0) {
            // always quote an empty token that is the first
            // on the line, as it may be the only thing on the
            // line. If it were not quoted in that case,
            // an empty line has no tokens.
            if (first) {
                quote = true;
            }
        } else {
            char c = value.charAt(pos);

            // Hmmm, where did this rule come from?
            if (first && (c < '0' || (c > '9' && c < 'A') || (c > 'Z' && c < 'a') || (c > 'z'))) {
                quote = true;
                // } else if (c == ' ' || c == '\f' || c == '\t') {
            } else if (c <= COMMENT) {
                // Some other chars at the start of a value caused the parser to fail, so for now
                // encapsulate if we start in anything less than '#'. We are being conservative
                // by including the default comment char too.
                quote = true;
            } else {
                while (pos < end) {
                    c = value.charAt(pos);
                    if (c == LF || c == CR || c == encapsulator || c == delim) {
                        quote = true;
                        break;
                    }
                    pos++;
                }

                if (!quote) {
                    pos = end - 1;
                    c = value.charAt(pos);
                    // if (c == ' ' || c == '\f' || c == '\t') {
                    // Some other chars at the end caused the parser to fail, so for now
                    // encapsulate if we end in anything less than ' '
                    if (c <= SP) {
                        quote = true;
                    }
                }
            }
        }

        if (!quote) {
            // no encapsulation needed - write out the original value
            out.append(value, start, end);
            return;
        }

        // we hit something that needed encapsulation
        out.append(encapsulator);

        // Pick up where we left off: pos should be positioned on the first character that caused
        // the need for encapsulation.
        while (pos < end) {
            final char c = value.charAt(pos);
            if (c == encapsulator) {
                // write out the chunk up until this point

                // add 1 to the length to write out the encapsulator also
                out.append(value, start, pos + 1);
                // put the next starting position on the encapsulator so we will
                // write it out again with the next string (effectively doubling it)
                start = pos;
            }
            pos++;
        }

        // write the last segment
        out.append(value, start, pos);
        out.append(encapsulator);
    }

    /**
     * Prints the string as the next value on the line. The value will be escaped or encapsulated as needed if
     * checkForEscape==true
     *
     * @param value
     *            value to output.
     */
    public void print(String value, final boolean checkForEscape) throws IOException {
        if (value == null) {
            // null values are considered empty
            value = EMPTY;
        }

        if (!checkForEscape) {
            // write directly from string
            printDelimiter();
            out.append(value);
        } else {
            print(value, 0, value.length());
        }
    }

    /**
     * Prints the string as the next value on the line. The value will be escaped or encapsulated as needed.
     *
     * @param value
     *            value to be outputted.
     */
    public void print(final String value) throws IOException {
        print(value, true);
    }
}
