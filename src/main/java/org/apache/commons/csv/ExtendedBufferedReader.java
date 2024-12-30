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
 * In particular the reader supports a look-ahead option, which allows you to see the next char returned by
 * {@link #read()}. This reader also tracks how many characters have been read with {@link #getPosition()}.
 * </p>
 */
final class ExtendedBufferedReader extends UnsynchronizedBufferedReader {

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
    private CharsetEncoder encoder;

    /**
     * Constructs a new instance using the default buffer size.
     */
    ExtendedBufferedReader(final Reader reader) {
        super(reader);
    }

    /**
     * Constructs a new instance with the specified reader, character set,
     * and byte tracking option. Initializes an encoder if byte tracking is enabled
     * and a character set is provided.
     *
     * @param reader the reader supports a look-ahead option.
     * @param charset the character set for encoding, or {@code null} if not applicable.
     * @param enableByteTracking {@code true} to enable byte tracking; {@code false} to disable it.
     */
    ExtendedBufferedReader(final Reader reader, Charset charset, boolean enableByteTracking) {
        super(reader);
        if (charset != null && enableByteTracking) {
            encoder = charset.newEncoder();
        }
    }

    /**
     * Closes the stream.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // Set ivars before calling super close() in case close() throws an IOException.
        lastChar = EOF;
        super.close();
    }

    /**
     * Returns the last character that was read as an integer (0 to 65535). This will be the last character returned by
     * any of the read methods. This will not include a character read using the {@link #peek()} method. If no
     * character has been read then this will return {@link Constants#UNDEFINED}. If the end of the stream was reached
     * on the last read then this will return {@link IOUtils#EOF}.
     *
     * @return the last character that was read
     */
    int getLastChar() {
        return lastChar;
    }

    /**
     * Returns the current line number
     *
     * @return the current line number
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
     * @return the current position in the reader (counting characters, not bytes since this is a Reader)
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

    @Override
    public int read() throws IOException {
        final int current = super.read();
        if (current == CR || current == LF && lastChar != CR ||
            current == EOF && lastChar != CR && lastChar != LF && lastChar != EOF) {
            lineNumber++;
        }
        if (encoder != null) {
            this.bytesRead += getEncodedCharLength(current);
        }
        lastChar = current;
        position++;
        return lastChar;
    }

    /**
     * Gets the byte length of the given character based on the the original Unicode
     * specification, which defined characters as fixed-width 16-bit entities.
     * <p>
     * The Unicode characters are divided into two main ranges:
     * <ul>
     *   <li><b>U+0000 to U+FFFF (Basic Multilingual Plane, BMP):</b>
     *     <ul>
     *       <li>Represented using a single 16-bit {@code char}.</li>
     *       <li>Includes UTF-8 encodings of 1-byte, 2-byte, and some 3-byte characters.</li>
     *     </ul>
     *   </li>
     *   <li><b>U+10000 to U+10FFFF (Supplementary Characters):</b>
     *     <ul>
     *       <li>Represented as a pair of {@code char}s:</li>
     *       <li>The first {@code char} is from the high-surrogates range (\uD800-\uDBFF).</li>
     *       <li>The second {@code char} is from the low-surrogates range (\uDC00-\uDFFF).</li>
     *       <li>Includes UTF-8 encodings of some 3-byte characters and all 4-byte characters.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param current the current character to process.
     * @return the byte length of the character.
     * @throws CharacterCodingException if the character cannot be encoded.
     */
    private int getEncodedCharLength(int current) throws CharacterCodingException {
        final char cChar = (char) current;
        final char lChar = (char) lastChar;
        if (!Character.isSurrogate(cChar)) {
            return encoder.encode(
                CharBuffer.wrap(new char[] {cChar})).limit();
        } else {
            if (Character.isHighSurrogate(cChar)) {
                // Move on to the next char (low surrogate)
                return 0;
            } else if (Character.isSurrogatePair(lChar, cChar)) {
                return encoder.encode(
                    CharBuffer.wrap(new char[] {lChar, cChar})).limit();
            } else throw new CharacterCodingException();
        }
    }

    @Override
    public int read(final char[] buf, final int offset, final int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        final int len = super.read(buf, offset, length);
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
     * Gets the next line, dropping the line terminator(s). This method should only be called when processing a
     * comment, otherwise, information can be lost.
     * <p>
     * Increments {@link #lineNumber} and updates {@link #position}.
     * </p>
     * <p>
     * Sets {@link #lastChar} to {@code Constants.EOF} at EOF, otherwise the last EOL character.
     * </p>
     *
     * @return the line that was read, or null if reached EOF.
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

    /**
     * Gets the number of bytes read by the reader.
     *
     * @return the number of bytes read by the read
     */
    long getBytesRead() {
        return this.bytesRead;
    }

}
