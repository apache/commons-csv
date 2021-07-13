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
import static org.apache.commons.csv.Constants.END_OF_STREAM;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.UNDEFINED;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * A special buffered reader which supports sophisticated read access.
 * <p>
 * In particular the reader supports a look-ahead option, which allows you to see the next char returned by
 * {@link #read()}. This reader also tracks how many characters have been read with {@link #getPosition()}.
 * </p>
 */
final class ExtendedPushbackReader extends PushbackReader {

    /** The last char returned */
    private int lastChar = UNDEFINED;

    /** The count of EOLs (CR/LF/CRLF) seen so far */
    private long eolCounter;

    /** The position, which is number of characters read so far */
    private long position;

    private boolean closed;

    /**
     * Created extended buffered reader using default buffer-size
     */
    private ExtendedPushbackReader(final Reader reader, final int delimiterSize) {
        super(reader, Math.max(1, 2 * delimiterSize));
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
        closed = true;
        lastChar = END_OF_STREAM;
        super.close();
    }

    /**
     * Returns the current line number
     *
     * @return the current line number
     */
    long getCurrentLineNumber() {
        // Check if we are at EOL or EOF or just starting
        if (lastChar == CR || lastChar == LF || lastChar == UNDEFINED || lastChar == END_OF_STREAM) {
            return eolCounter; // counter is accurate
        }
        return eolCounter + 1; // Allow for counter being incremented only at EOL
    }

    /**
     * Returns the last character that was read as an integer (0 to 65535). This will be the last character returned by
     * any of the read methods. This will not include a character read using the {@link #lookAhead()} method. If no
     * character has been read then this will return {@link Constants#UNDEFINED}. If the end of the stream was reached
     * on the last read then this will return {@link Constants#END_OF_STREAM}.
     *
     * @return the last character that was read
     */
    int getLastChar() {
        return lastChar;
    }

    /**
     * Gets the character position in the reader.
     *
     * @return the current position in the reader (counting characters, not bytes since this is a Reader)
     */
    long getPosition() {
        return this.position;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public int read() throws IOException {
        final int current = super.read();
        if (current == CR || current == LF && lastChar != CR ||
            current == END_OF_STREAM && lastChar != CR && lastChar != LF && lastChar != END_OF_STREAM) {
            eolCounter++;
        }
        lastChar = current;
        position++;
        return lastChar;
    }

    int peek() throws IOException {
      final int current = super.read();
      if (current != END_OF_STREAM) {
          super.unread(current);
      }
      return current;
    }

    char[] peek(int n) throws IOException {
        final char[] buf = new char[n];
        int count = super.read(buf);
        super.unread(buf, 0, count);
        return (count == buf.length) ? buf : Arrays.copyOf(buf, count);
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
                        eolCounter++;
                    }
                } else if (ch == CR) {
                    eolCounter++;
                }
            }

            lastChar = buf[offset + len - 1];

        } else if (len == -1) {
            lastChar = END_OF_STREAM;
        }

        position += len;
        return len;
    }

    /**
     * Read the next line of input, which drops the line terminator(s). This method should only be called
     * when processing a comment, otherwise information can be lost.
     * <p>
     * Increments {@link #eolCounter}.
     * </p>
     * <p>
     * Sets {@link #lastChar} to {@link Constants#END_OF_STREAM} at EOF, otherwise to LF.
     * </p>
     *
     * @return the line that was read, or null if reached EOF.
     */
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder(64);
        final long startEolCounter = eolCounter;

        int c = this.read();

        // First read was EOS
        if (c == END_OF_STREAM) {
          return null;
        }
        // First read was EOL
        if (eolCounter != startEolCounter) {
          if (c == CR && peek() == LF) {
            this.read();
          }
          return "";
        }
        // Read until new line is hit
        do {
          sb.append((char)c);
          c = this.read();
        }
        while (eolCounter == startEolCounter);

        // If the line is terminated with CR+LF, trim the LF
        if (c == CR && peek() == LF) {
          this.read();
        }

        return sb.toString();
    }

    /**
     * Create an ExtendedPushbackReader for the given {@code Reader} with space
     * for a delimiter with {@code delimiterSize} characters.
     */
    static ExtendedPushbackReader create(Reader reader, int delimiterSize) {
        return new ExtendedPushbackReader(reader, delimiterSize);
    }

    /**
     * Create an ExtendedPushbackReader for the given string with space for a
     * delimiter with {@code delimiterSize} characters.
     */
    static ExtendedPushbackReader create(String string, int delimiterSize) {
        return create(new StringReader(string), delimiterSize);
    }

    /**
     * Create an ExtendedPushbackReader for the given string with space for a
     * single delimiter character.
     */
    static ExtendedPushbackReader create(String string) {
      return create(string, 1);
  }

}
