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

import java.io.Serializable;
import java.util.Objects;

/**
 * This class packs all the information about the style of a table.
 * We have prepared two pre-defined styles:
 * <ul>
 *     <li><code>SIMPLE</code> uses simple ASCII characters to draw the table</li>
 *     <li><code>FANCY</code> uses so-called Extended ASCII characters to draw fancier tables</li>
 * </ul>
 *
 *
 * @author Ali Ghanbari
 */
public final class TableStyle implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final TableStyle SIMPLE = new TableStyle('-',
            '|',
            '=',
            '|',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+',
            '+');

    public static final TableStyle FANCY = new TableStyle('─',
            '│',
            '═',
            '║',
            '┬',
            '╤',
            '┴',
            '╧',
            '┼',
            '╪',
            '├',
            '┤',
            '╟',
            '╢',
            '╠',
            '╣',
            '┌',
            '╔',
            '┐',
            '╗',
            '└',
            '╚',
            '┘',
            '╝');

    public final char THIN_HORIZONTAL_LINE;
    public final char THIN_VERTICAL_LINE;
    public final char THICK_HORIZONTAL_LINE;
    public final char THICK_VERTICAL_LINE;
    public final char THIN_DOWNWARD_JOINT;
    public final char THICK_DOWNWARD_JOINT;
    public final char THIN_UPWARD_JOINT;
    public final char THICK_UPWARD_JOINT;
    public final char THIN_MIDDLE_JOINT;
    public final char THICK_MIDDLE_JOINT;
    public final char THIN_LEFT_JOINT;
    public final char THIN_RIGHT_JOINT;
    public final char THICK_LEFT_JOINT;
    public final char THICK_RIGHT_JOINT;
    public final char THICK_THICK_LEFT_JOINT;
    public final char THICK_THICK_RIGHT_JOINT;
    public final char THIN_UPPER_LEFT_CORNER_JOINT;
    public final char THICK_UPPER_LEFT_CORNER_JOINT;
    public final char THIN_UPPER_RIGHT_CORNER_JOINT;
    public final char THICK_UPPER_RIGHT_CORNER_JOINT;
    public final char THIN_LOWER_LEFT_CORNER_JOINT;
    public final char THICK_LOWER_LEFT_CORNER_JOINT;
    public final char THIN_LOWER_RIGHT_CORNER_JOINT;
    public final char THICK_LOWER_RIGHT_CORNER_JOINT;

    public TableStyle(final char THIN_HORIZONTAL_LINE,
                      final char THIN_VERTICAL_LINE,
                      final char THICK_HORIZONTAL_LINE,
                      final char THICK_VERTICAL_LINE,
                      final char THIN_DOWNWARD_JOINT,
                      final char THICK_DOWNWARD_JOINT,
                      final char THIN_UPWARD_JOINT,
                      final char THICK_UPWARD_JOINT,
                      final char THIN_MIDDLE_JOINT,
                      final char THICK_MIDDLE_JOINT,
                      final char THIN_LEFT_JOINT,
                      final char THIN_RIGHT_JOINT,
                      final char THICK_LEFT_JOINT,
                      final char THICK_RIGHT_JOINT,
                      final char THICK_THICK_LEFT_JOINT,
                      final char THICK_THICK_RIGHT_JOINT,
                      final char THIN_UPPER_LEFT_CORNER_JOINT,
                      final char THICK_UPPER_LEFT_CORNER_JOINT,
                      final char THIN_UPPER_RIGHT_CORNER_JOINT,
                      final char THICK_UPPER_RIGHT_CORNER_JOINT,
                      final char THIN_LOWER_LEFT_CORNER_JOINT,
                      final char THICK_LOWER_LEFT_CORNER_JOINT,
                      final char THIN_LOWER_RIGHT_CORNER_JOINT,
                      final char THICK_LOWER_RIGHT_CORNER_JOINT) {
        this.THIN_HORIZONTAL_LINE = THIN_HORIZONTAL_LINE;
        this.THIN_VERTICAL_LINE = THIN_VERTICAL_LINE;
        this.THICK_HORIZONTAL_LINE = THICK_HORIZONTAL_LINE;
        this.THICK_VERTICAL_LINE = THICK_VERTICAL_LINE;
        this.THIN_DOWNWARD_JOINT = THIN_DOWNWARD_JOINT;
        this.THICK_DOWNWARD_JOINT = THICK_DOWNWARD_JOINT;
        this.THIN_UPWARD_JOINT = THIN_UPWARD_JOINT;
        this.THICK_UPWARD_JOINT = THICK_UPWARD_JOINT;
        this.THIN_MIDDLE_JOINT = THIN_MIDDLE_JOINT;
        this.THICK_MIDDLE_JOINT = THICK_MIDDLE_JOINT;
        this.THIN_LEFT_JOINT = THIN_LEFT_JOINT;
        this.THIN_RIGHT_JOINT = THIN_RIGHT_JOINT;
        this.THICK_LEFT_JOINT = THICK_LEFT_JOINT;
        this.THICK_RIGHT_JOINT = THICK_RIGHT_JOINT;
        this.THICK_THICK_LEFT_JOINT = THICK_THICK_LEFT_JOINT;
        this.THICK_THICK_RIGHT_JOINT = THICK_THICK_RIGHT_JOINT;
        this.THIN_UPPER_LEFT_CORNER_JOINT = THIN_UPPER_LEFT_CORNER_JOINT;
        this.THICK_UPPER_LEFT_CORNER_JOINT = THICK_UPPER_LEFT_CORNER_JOINT;
        this.THIN_UPPER_RIGHT_CORNER_JOINT = THIN_UPPER_RIGHT_CORNER_JOINT;
        this.THICK_UPPER_RIGHT_CORNER_JOINT = THICK_UPPER_RIGHT_CORNER_JOINT;
        this.THIN_LOWER_LEFT_CORNER_JOINT = THIN_LOWER_LEFT_CORNER_JOINT;
        this.THICK_LOWER_LEFT_CORNER_JOINT = THICK_LOWER_LEFT_CORNER_JOINT;
        this.THIN_LOWER_RIGHT_CORNER_JOINT = THIN_LOWER_RIGHT_CORNER_JOINT;
        this.THICK_LOWER_RIGHT_CORNER_JOINT = THICK_LOWER_RIGHT_CORNER_JOINT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableStyle that = (TableStyle) o;
        return THIN_HORIZONTAL_LINE == that.THIN_HORIZONTAL_LINE &&
                THIN_VERTICAL_LINE == that.THIN_VERTICAL_LINE &&
                THICK_HORIZONTAL_LINE == that.THICK_HORIZONTAL_LINE &&
                THICK_VERTICAL_LINE == that.THICK_VERTICAL_LINE &&
                THIN_DOWNWARD_JOINT == that.THIN_DOWNWARD_JOINT &&
                THICK_DOWNWARD_JOINT == that.THICK_DOWNWARD_JOINT &&
                THIN_UPWARD_JOINT == that.THIN_UPWARD_JOINT &&
                THICK_UPWARD_JOINT == that.THICK_UPWARD_JOINT &&
                THIN_MIDDLE_JOINT == that.THIN_MIDDLE_JOINT &&
                THICK_MIDDLE_JOINT == that.THICK_MIDDLE_JOINT &&
                THIN_LEFT_JOINT == that.THIN_LEFT_JOINT &&
                THIN_RIGHT_JOINT == that.THIN_RIGHT_JOINT &&
                THICK_LEFT_JOINT == that.THICK_LEFT_JOINT &&
                THICK_RIGHT_JOINT == that.THICK_RIGHT_JOINT &&
                THIN_UPPER_LEFT_CORNER_JOINT == that.THIN_UPPER_LEFT_CORNER_JOINT &&
                THICK_UPPER_LEFT_CORNER_JOINT == that.THICK_UPPER_LEFT_CORNER_JOINT &&
                THIN_UPPER_RIGHT_CORNER_JOINT == that.THIN_UPPER_RIGHT_CORNER_JOINT &&
                THICK_UPPER_RIGHT_CORNER_JOINT == that.THICK_UPPER_RIGHT_CORNER_JOINT &&
                THIN_LOWER_LEFT_CORNER_JOINT == that.THIN_LOWER_LEFT_CORNER_JOINT &&
                THICK_LOWER_LEFT_CORNER_JOINT == that.THICK_LOWER_LEFT_CORNER_JOINT &&
                THIN_LOWER_RIGHT_CORNER_JOINT == that.THIN_LOWER_RIGHT_CORNER_JOINT &&
                THICK_LOWER_RIGHT_CORNER_JOINT == that.THICK_LOWER_RIGHT_CORNER_JOINT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(THIN_HORIZONTAL_LINE,
                THIN_VERTICAL_LINE,
                THICK_HORIZONTAL_LINE,
                THICK_VERTICAL_LINE,
                THIN_DOWNWARD_JOINT,
                THICK_DOWNWARD_JOINT,
                THIN_UPWARD_JOINT,
                THICK_UPWARD_JOINT,
                THIN_MIDDLE_JOINT,
                THICK_MIDDLE_JOINT,
                THIN_LEFT_JOINT,
                THIN_RIGHT_JOINT,
                THICK_LEFT_JOINT,
                THICK_RIGHT_JOINT,
                THIN_UPPER_LEFT_CORNER_JOINT,
                THICK_UPPER_LEFT_CORNER_JOINT,
                THIN_UPPER_RIGHT_CORNER_JOINT,
                THICK_UPPER_RIGHT_CORNER_JOINT,
                THIN_LOWER_LEFT_CORNER_JOINT,
                THICK_LOWER_LEFT_CORNER_JOINT,
                THIN_LOWER_RIGHT_CORNER_JOINT,
                THICK_LOWER_RIGHT_CORNER_JOINT);
    }
}
