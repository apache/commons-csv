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

import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.UNDEFINED;
import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedReader;

/**
 * A special buffered reader which supports sophisticated read access.
 * <p>
 * In particular the reader supports a look-ahead option, which allows you to see the next char returned by {@link #read()}. This reader also tracks how many
 * characters have been read with {@link #getPosition()}.
 * </p>
 */
final class ExtendedBufferedReader extends UnsynchronizedBufferedReader {

    /**
     * Measures the byte-order mark that {@code encoder} prepends to every {@link CharsetEncoder#encode(CharBuffer)} call. Charsets such as {@code UTF-16} emit
     * a BOM each time, which would otherwise be counted once per character. The BOM is the constant prefix shared by encoding one and two characters.
     *
     * @param encoder The encoder to measure.
     * @return The byte-order mark length in bytes, or 0 when the encoder writes none.
     */
    private static int measureBomLength(final CharsetEncoder encoder) {
        try {
            final int one = encoder.encode(CharBuffer.wrap(new char[] { 'a' })).limit();
            final int two = encoder.encode(CharBuffer.wrap(new char[] { 'a', 'a' })).limit();
            return Math.max(0, 2 * one - two);
        } catch (final CharacterCodingException e) {
            return 0;
        }
    }

    /** The last char returned */
    private int lastChar = UNDEFINED;

    private int lastCharMark = UNDEFINED;

    /** The count of EOLs (CR/LF/CRLF) seen so far */
    private long lineNumber;

    private long lineNumberMark;

    /** The position, which is the number of characters read so far */
    private long position;

    private long positionMark;

    /** The number of bytes read so far. */
    private long bytesRead;

    private long bytesReadMark;

    /** Encoder for calculating the number of bytes for each character read. */
    private final CharsetEncoder encoder;

    /** Bytes {@link #encoder} emits as a byte-order mark on every {@link CharsetEncoder#encode(CharBuffer)} call (for example {@code UTF-16}). */
    private final int bomLength;

    /**
     * Constructs a new instance using the default buffer size.
     */
    ExtendedBufferedReader(final Reader reader) {
        this(reader, null, false);
    }

    /**
     * Constructs a new instance with the specified reader, character set, and byte tracking option. Initializes an encoder if byte tracking is enabled and a
     * character set is provided.
     *
     * @param reader     The reader supports a look-ahead option.
     * @param charset    The character set for encoding, or {@code null} if not applicable.
     * @param trackBytes {@code true} to enable byte tracking; {@code false} to disable it.
     */
    ExtendedBufferedReader(final Reader reader, final Charset charset, final boolean trackBytes) {
        super(reader);
        encoder = charset != null && trackBytes ? charset.newEncoder() : null;
        bomLength = encoder != null ? measureBomLength(encoder) : 0;
    }

    /**
     * Closes the stream.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // Set ivars before calling super close() in case close() throws an IOException.
        lastChar = EOF;
        super.close();
    }

    /**
     * Gets the number of bytes read by the reader.
     *
     * @return The number of bytes read by the read
     */
    long getBytesRead() {
        return this.bytesRead;
    }

    private long getEncodedCharLength(final char[] buf, final int offset, final int length) throws CharacterCodingException {
        long len = 0;
        int previous = lastChar;
        for (int i = offset; i < offset + length; i++) {
            len += getEncodedCharLength(previous, buf[i]);
            previous = buf[i];
        }
        return len;
    }

    /**
     * Gets the byte length of the given character based on the original Unicode specification, which defined characters as fixed-width 16-bit entities.
     * <p>
     * The Unicode characters are divided into two main ranges:
     * <ul>
     * <li><strong>U+0000 to U+FFFF (Basic Multilingual Plane, BMP):</strong>
     * <ul>
     * <li>Represented using a single 16-bit {@code char}.</li>
     * <li>Includes UTF-8 encodings of 1-byte, 2-byte, and some 3-byte characters.</li>
     * </ul>
     * </li>
     * <li><strong>U+10000 to U+10FFFF (Supplementary Characters):</strong>
     * <ul>
     * <li>Represented as a pair of {@code char}s:</li>
     * <li>The first {@code char} is from the high-surrogates range (\uD800-\uDBFF).</li>
     * <li>The second {@code char} is from the low-surrogates range (\uDC00-\uDFFF).</li>
     * <li>Includes UTF-8 encodings of some 3-byte characters and all 4-byte characters.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param current The current character to process.
     * @return The byte length of the character.
     * @throws CharacterCodingException if the character cannot be encoded.
     */
    private int getEncodedCharLength(final int current) throws CharacterCodingException {
        return getEncodedCharLength(lastChar, current);
    }

    private int getEncodedCharLength(final int previous, final int current) throws CharacterCodingException {
        final char cChar = (char) current;
        final char lChar = (char) previous;
        if (!Character.isSurrogate(cChar)) {
            return encoder.encode(CharBuffer.wrap(new char[] { cChar })).limit() - bomLength;
        }
        if (Character.isHighSurrogate(cChar)) {
            // Move on to the next char (low surrogate)
            return 0;
        }
        if (Character.isSurrogatePair(lChar, cChar)) {
            return encoder.encode(CharBuffer.wrap(new char[] { lChar, cChar })).limit() - bomLength;
        }
        throw new CharacterCodingException();
    }

    /**
     * Returns the last character that was read as an integer (0 to 65535). This will be the last character returned by any of the read methods. This will not
     * include a character read using the {@link #peek()} method. If no character has been read then this will return {@link Constants#UNDEFINED}. If the end of
     * the stream was reached on the last read then this will return {@link IOUtils#EOF}.
     *
     * @return The last character that was read
     */
    int getLastChar() {
        return lastChar;
    }

    /**
     * Returns the current line number
     *
     * @return The current line number
     */
    long getLineNumber() {
        // Check if we are at EOL or EOF or just starting
        if (lastChar == CR || lastChar == LF || lastChar == UNDEFINED || lastChar == EOF) {
            return lineNumber; // counter is accurate
        }
        return lineNumber + 1; // Allow for counter being incremented only at EOL
    }

    /**
     * Gets the character position in the reader.
     *
     * @return The current position in the reader (counting characters, not bytes since this is a Reader)
     */
    long getPosition() {
        return this.position;
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        lineNumberMark = lineNumber;
        lastCharMark = lastChar;
        positionMark = position;
        bytesReadMark = bytesRead;
        super.mark(readAheadLimit);
    }

    /**
     * Fills {@code array} with the characters that follow the current position without consuming them.
     * <p>
     * Overridden because the inherited implementation stops at the first short read, which leaves the tail of the array holding stale content when the source
     * delivers data in chunks. Callers compare the whole array against a multi-character delimiter, so a partial fill makes them miss a delimiter that is
     * really there.
     * </p>
     *
     * @param array the buffer to fill.
     * @return the number of characters peeked, or {@link IOUtils#EOF} at the end of the stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int peek(final char[] array) throws IOException {
        final int length = array.length;
        if (length == 0) {
            return 0;
        }
        super.mark(length);
        int len = 0;
        while (len < length) {
            final int more = super.read(array, len, length - len);
            if (more == EOF) {
                break;
            }
            len += more;
        }
        super.reset();
        return len == 0 ? EOF : len;
    }

    @Override
    public int read() throws IOException {
        final int current = super.read();
        if (current == CR || current == LF && lastChar != CR || current == EOF && lastChar != CR && lastChar != LF && lastChar != EOF) {
            lineNumber++;
        }
        if (encoder != null && current != EOF) {
            this.bytesRead += getEncodedCharLength(current);
        }
        lastChar = current;
        position++;
        return lastChar;
    }

    @Override
    public int read(final char[] buf, final int offset, final int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        int len = super.read(buf, offset, length);
        // The underlying buffered reader stops early once the source reports it is not ready, so a stream that delivers data in chunks (a socket or a pipe)
        // yields a short read. Callers match multi-character sequences against this buffer, so keep reading until it is full or the source is exhausted.
        while (len > 0 && len < length) {
            final int more = super.read(buf, offset + len, length - len);
            if (more == EOF) {
                break;
            }
            len += more;
        }
        if (encoder != null && len > 0) {
            this.bytesRead += getEncodedCharLength(buf, offset, len);
        }
        if (len > 0) {
            for (int i = offset; i < offset + len; i++) {
                final char ch = buf[i];
                if (ch == LF) {
                    if (CR != (i > offset ? buf[i - 1] : lastChar)) {
                        lineNumber++;
                    }
                } else if (ch == CR) {
                    lineNumber++;
                }
            }
            lastChar = buf[offset + len - 1];
        } else if (len == EOF) {
            lastChar = EOF;
        }
        position += len;
        return len;
    }

    /**
     * Gets the next line, dropping the line terminator(s). This method should only be called when processing a comment, otherwise, information can be lost.
     * <p>
     * Increments {@link #lineNumber} and updates {@link #position}.
     * </p>
     * <p>
     * Sets {@link #lastChar} to {@code Constants.EOF} at EOF, otherwise the last EOL character.
     * </p>
     *
     * @return The line that was read, or null if reached EOF.
     */
    @Override
    public String readLine() throws IOException {
        if (peek() == EOF) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        while (true) {
            final int current = read();
            if (current == CR) {
                final int next = peek();
                if (next == LF) {
                    read();
                }
            }
            if (current == EOF || current == LF || current == CR) {
                break;
            }
            buffer.append((char) current);
        }
        return buffer.toString();
    }

    @Override
    public void reset() throws IOException {
        lineNumber = lineNumberMark;
        lastChar = lastCharMark;
        position = positionMark;
        bytesRead = bytesReadMark;
        super.reset();
    }
}
