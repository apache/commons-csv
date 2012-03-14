/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

/**
 * A simple StringBuffer replacement that aims to reduce copying as much as possible.
 * The buffer grows as necessary. This class is not thread safe.
 *
 * @author Ortwin Glück
 */
class CharBuffer {

    private char[] c;

    /**
     * Actually used number of characters in the array.
     * It is also the index at which a new character will be inserted into <code>c</code>.
     */
    private int length;

    /**
     * Creates a new CharBuffer with an initial capacity of 32 characters.
     */
    CharBuffer() {
        this(32);
    }

    /**
     * Creates a new CharBuffer with an initial capacity
     * of <code>length</code> characters.
     */
    CharBuffer(int length) {
        if (length == 0) {
            throw new IllegalArgumentException("Can't create an empty CharBuffer");
        }
        this.c = new char[length];
    }

    /**
     * Empties the buffer. The capacity still remains the same, so no memory is freed.
     */
    void clear() {
        length = 0;
    }

    /**
     * Returns the number of characters in the buffer.
     *
     * @return the number of characters
     */
    int length() {
        return length;
    }

    /**
     * Returns the current capacity of the buffer.
     *
     * @return the maximum number of characters that can be stored in this buffer without resizing it.
     */
    int capacity() {
        return c.length;
    }


    /**
     * Appends the contents of <code>cb</code> to the end of this CharBuffer.
     *
     * @param cb the CharBuffer to append or null
     */
    void append(CharBuffer cb) {
        if (cb != null) {
            ensureCapacity(length + cb.length);
            System.arraycopy(cb.c, 0, c, length, cb.length);
            length += cb.length;
        }
    }

    /**
     * Appends <code>s</code> to the end of this CharBuffer.
     * This method involves copying the new data once!
     *
     * @param s the String to append or null
     */
    void append(String s) {
        if (s != null) {
            append(s.toCharArray());
        }
    }

    /**
     * Appends <code>data</code> to the end of this CharBuffer.
     * This method involves copying the new data once!
     *
     * @param data the char[] to append or null
     */
    void append(char[] data) {
        if (data != null) {
            ensureCapacity(length + data.length);
            System.arraycopy(data, 0, c, length, data.length);
            length += data.length;
        }
    }

    /**
     * Appends a single character to the end of this CharBuffer.
     * This method involves copying the new data once!
     *
     * @param data the char to append
     */
    void append(char data) {
        ensureCapacity(length + 1);
        c[length] = data;
        length++;
    }

    /**
     * Removes trailing whitespace.
     */
    void trimTrailingWhitespace() {
        while (length > 0 && Character.isWhitespace(c[length - 1])) {
            length--;
        }
    }

    /**
     * Converts the contents of the buffer into a StringBuffer.
     * This method involves copying the new data once!
     *
     * @return the contents of the character buffer as a String
     */
    @Override
    public String toString() {
        return new String(c, 0, length);
    }

    /**
     * Copies the data into a new array of at least <code>capacity</code> size.
     *
     * @param capacity
     */
    void ensureCapacity(int capacity) {
        if (c.length < capacity) {
            int newcapacity = ((capacity * 3) >> 1) + 1;
            char[] newc = new char[newcapacity];
            System.arraycopy(c, 0, newc, 0, length);
            c = newc;
        }
    }
}
