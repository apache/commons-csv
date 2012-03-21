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

import java.io.Flushable;
import java.io.IOException;

/**
 * Print values as a comma separated list.
 */
public class CSVPrinter {

    /** The place that the values get written. */
    private final Appendable out;
    private final CSVFormat format;

    /** True if we just began a new line. */
    private boolean newLine = true;

    /**
     * Create a printer that will print values to the given stream following the CSVFormat.
     * <p/>
     * Currently, only a pure encapsulation format or a pure escaping format
     * is supported. Hybrid formats (encapsulation and escaping with a different character) are not supported.
     *
     * @param out    stream to which to print.
     * @param format the CSV format. If null the default format is used ({@link CSVFormat#DEFAULT})
     * @throws IllegalArgumentException thrown if the parameters of the format are inconsistent
     */
    public CSVPrinter(Appendable out, CSVFormat format) {
        this.out = out;
        this.format = format == null ? CSVFormat.DEFAULT : format;
        
        this.format.validate();
    }

    // ======================================================
    //  printing implementation
    // ======================================================

    /**
     * Output a blank line
     */
    public void println() throws IOException {
        out.append(format.getLineSeparator());
        newLine = true;
    }

    /**
     * Flush the underlying stream.
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        if (out instanceof Flushable) {
            ((Flushable) out).flush();
        }
    }

    /**
     * Print a single line of comma separated values.
     * The values will be quoted if needed.  Quotes and
     * newLine characters will be escaped.
     *
     * @param values values to be outputted.
     */
    public void println(String... values) throws IOException {
        for (String value : values) {
            print(value);
        }
        println();
    }


    /**
     * Put a comment on a new line among the comma separated values. Comments
     * will always begin on a new line and occupy a least one full line. The
     * character specified to start comments and a space will be inserted at
     * the beginning of each new line in the comment.
     * <p/>
     * If comments are disabled in the current CSV format this method does nothing.
     *
     * @param comment the comment to output
     */
    public void printComment(String comment) throws IOException {
        if (!format.isCommentingEnabled()) {
            return;
        }
        if (!newLine) {
            println();
        }
        out.append(format.getCommentStart());
        out.append(' ');
        for (int i = 0; i < comment.length(); i++) {
            char c = comment.charAt(i);
            switch (c) {
                case '\r':
                    if (i + 1 < comment.length() && comment.charAt(i + 1) == '\n') {
                        i++;
                    }
                //$FALL-THROUGH$ break intentionally excluded.
            case '\n':
                    println();
                    out.append(format.getCommentStart());
                    out.append(' ');
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        println();
    }


    private void print(CharSequence value, int offset, int len) throws IOException {        
        if (format.isEncapsulating()) {
            printAndEncapsulate(value, offset, len);
        } else if (format.isEscaping()) {
            printAndEscape(value, offset, len);
        } else {
            printSep();
            out.append(value, offset, offset + len);
        }
    }

    void printSep() throws IOException {
        if (newLine) {
            newLine = false;
        } else {
            out.append(format.getDelimiter());
        }
    }

    void printAndEscape(CharSequence value, int offset, int len) throws IOException {
        int start = offset;
        int pos = offset;
        int end = offset + len;

        printSep();

        char delim = format.getDelimiter();
        char escape = format.getEscape();

        while (pos < end) {
            char c = value.charAt(pos);
            if (c == '\r' || c == '\n' || c == delim || c == escape) {
                // write out segment up until this char
                if (pos > start) {
                    out.append(value, start, pos);
                }
                if (c == '\n') {
                    c = 'n';
                } else if (c == '\r') {
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

    void printAndEncapsulate(CharSequence value, int offset, int len) throws IOException {
        boolean first = newLine;  // is this the first value on this line?
        boolean quote = false;
        int start = offset;
        int pos = offset;
        int end = offset + len;

        printSep();

        char delim = format.getDelimiter();
        char encapsulator = format.getEncapsulator();

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
            if (first
                    && (c < '0'
                    || (c > '9' && c < 'A')
                    || (c > 'Z' && c < 'a')
                    || (c > 'z'))) {
                quote = true;
                // } else if (c == ' ' || c == '\f' || c == '\t') {
            } else if (c <= '#') {
                // Some other chars at the start of a value caused the parser to fail, so for now
                // encapsulate if we start in anything less than '#'.  We are being conservative
                // by including the default comment char too.
                quote = true;
            } else {
                while (pos < end) {
                    c = value.charAt(pos);
                    if (c == '\n' || c == '\r' || c == encapsulator || c == delim) {
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
                    if (c <= ' ') {
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
            char c = value.charAt(pos);
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
     * Print the string as the next value on the line. The value
     * will be escaped or encapsulated as needed if checkForEscape==true
     *
     * @param value value to be outputted.
     */
    public void print(String value, boolean checkForEscape) throws IOException {
        if (value == null) {
            // null values are considered empty
            value = "";
        }
        
        if (!checkForEscape) {
            // write directly from string
            printSep();
            out.append(value);
        } else {
            print(value, 0, value.length());
        }
    }

    /**
     * Print the string as the next value on the line. The value
     * will be escaped or encapsulated as needed.
     *
     * @param value value to be outputted.
     */
    public void print(String value) throws IOException {
        print(value, true);
    }
}
