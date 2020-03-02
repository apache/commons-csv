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

package org.apache.commons.csv.pretty;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * Pretty printing utility
 *
 * @author Ali Ghanbari
 */
public class PrettyPrinter {
    protected final List<CSVRecord> records;
    protected final TableStyle style;
    protected final PrintStream out;
    protected final int[] spaces;

    public PrettyPrinter(final List<CSVRecord> records, final TableStyle style, final PrintStream out) {
        this.records = records;
        this.style = style;
        this.out = out;
        this.spaces = calculateSpaces(records);
    }

    public PrettyPrinter(final CSVParser parser, final TableStyle style) throws IOException {
        this.records = parser.getRecords();
        this.style = style;
        this.out = System.out;
        this.spaces = calculateSpaces(this.records);
    }

    public PrettyPrinter(final CSVParser parser, final TableStyle style, final PrintStream out) throws IOException {
        this.records = parser.getRecords();
        this.style = style;
        this.out = out;
        this.spaces = calculateSpaces(this.records);
    }

    private int[] calculateSpaces(final List<CSVRecord> records) {
        final int length = records.isEmpty() ? 0 : records.iterator().next().size();
        final int[] spaces = new int[length];
        for (int i = 0; i < length; i++) {
            int max = Integer.MIN_VALUE;
            for (final CSVRecord record : records) {
                max = Math.max(record.get(i).length(), max);
            }
            spaces[i] = 2 + max;
        }
        return spaces;
    }

    public List<CSVRecord> getRecords() {
        return this.records;
    }

    public TableStyle getStyle() {
        return this.style;
    }

    private void printUpperLine(final boolean thick, final char joint) {
        if (this.spaces.length == 0) {
            return;
        }
        out.print(thick ? this.style.THICK_UPPER_LEFT_CORNER_JOINT : this.style.THIN_UPPER_LEFT_CORNER_JOINT);
        final char line = thick ? this.style.THICK_HORIZONTAL_LINE : this.style.THIN_HORIZONTAL_LINE;
        final StringBuilder sb = new StringBuilder();
        for (final int sp : this.spaces) {
            sb.append(StringUtils.repeat(line, sp));
            sb.append(joint);
        }
        out.print(sb.substring(0, sb.length() - 1));
        out.print(thick ? this.style.THICK_UPPER_RIGHT_CORNER_JOINT : this.style.THIN_UPPER_RIGHT_CORNER_JOINT);
    }

    private void printMiddleLine(final boolean thickBorder, final boolean thickLine) {
        if (this.spaces.length == 0) {
            return;
        }
        out.print(thickBorder ? (thickLine ? this.style.THICK_THICK_LEFT_JOINT : this.style.THICK_LEFT_JOINT) : this.style.THIN_LEFT_JOINT);
        final char line = thickLine ? this.style.THICK_HORIZONTAL_LINE : this.style.THIN_HORIZONTAL_LINE;
        final char joint = thickLine ? this.style.THICK_MIDDLE_JOINT : this.style.THIN_MIDDLE_JOINT;
        final StringBuilder sb = new StringBuilder();
        for (final int sp : this.spaces) {
            sb.append(StringUtils.repeat(line, sp));
            sb.append(joint);
        }
        out.print(sb.substring(0, sb.length() - 1));
        out.print(thickBorder ? (thickLine ? this.style.THICK_THICK_RIGHT_JOINT : this.style.THICK_RIGHT_JOINT) : this.style.THIN_RIGHT_JOINT);
    }

    private void printLowerLine(final boolean thick) {
        if (this.spaces.length == 0) {
            return;
        }
        out.print(thick ? this.style.THICK_LOWER_LEFT_CORNER_JOINT : this.style.THIN_LOWER_LEFT_CORNER_JOINT);
        final char line = thick ? this.style.THICK_HORIZONTAL_LINE : this.style.THIN_HORIZONTAL_LINE;
        final char joint = thick ? this.style.THICK_UPWARD_JOINT : this.style.THIN_UPWARD_JOINT;
        final StringBuilder sb = new StringBuilder();
        for (final int sp : this.spaces) {
            sb.append(StringUtils.repeat(line, sp));
            sb.append(joint);
        }
        out.print(sb.substring(0, sb.length() - 1));
        out.print(thick ? this.style.THICK_LOWER_RIGHT_CORNER_JOINT : this.style.THIN_LOWER_RIGHT_CORNER_JOINT);
    }

    private void printRow(final CSVRecord record, final boolean thick, final char sep) {
        if (this.spaces.length == 0) {
            return;
        }
        out.print(thick ? this.style.THICK_VERTICAL_LINE : this.style.THIN_VERTICAL_LINE);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.spaces.length; i++) {
            final int sp = this.spaces[i];
            sb.append(StringUtils.center(record.get(i), sp, ' '));
            sb.append(sep);
        }
        out.print(sb.substring(0, sb.length() - 1));
        out.print(thick ? this.style.THICK_VERTICAL_LINE : this.style.THIN_VERTICAL_LINE);
    }

    public void print(final boolean thickBorder,
                      final boolean distinguishHeader,
                      final boolean distinguishRows,
                      final boolean cellBorder) {
        final Iterator<CSVRecord> recordIterator = this.records.iterator();
        if (!recordIterator.hasNext()) {
            return;
        }
        printUpperLine(thickBorder, cellBorder ? (thickBorder ? this.style.THICK_DOWNWARD_JOINT : this.style.THIN_DOWNWARD_JOINT) : this.style.THICK_HORIZONTAL_LINE);
        out.println();
        if (distinguishHeader) {
            final CSVRecord firstRow = recordIterator.next();
            printRow(firstRow, thickBorder, cellBorder ? this.style.THIN_VERTICAL_LINE : ' ');
            out.println();
            printMiddleLine(thickBorder, distinguishRows); // if we distinguish rows this should be thick
            out.println();
        }
        while (recordIterator.hasNext()) {
            final CSVRecord row = recordIterator.next();
            printRow(row, thickBorder, cellBorder ? this.style.THIN_VERTICAL_LINE : ' ');
            out.println();
            if (distinguishRows && recordIterator.hasNext()) {
                printMiddleLine(thickBorder, false); // if we distinguish rows this should be thick
                out.println();
            }
        }
        printLowerLine(thickBorder);
    }
}
