package org.apache.commons.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

/** Copied from Apache Commons IO. */
class IOUtils {

    /**
     * <p>
     * Copied from Apache Commons IO.
     * </p>
     * The default buffer size ({@value}).
     */
    static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * <p>
     * Copied from Apache Commons IO.
     * </p>
     * Represents the end-of-file (or stream).
     * @since 2.5 (made public)
     */
    private static final int EOF = -1;

    /**
     * Copies chars from a large (over 2GB) <code>Reader</code> to an <code>Appendable</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     * </p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Appendable</code> to append to
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.7
     */
    static long copy(final Reader input, final Appendable output) throws IOException {
        return copy(input, output, CharBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    /**
     * Copies chars from a large (over 2GB) <code>Reader</code> to an <code>Appendable</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedReader</code>.
     * </p>
     *
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Appendable</code> to write to
     * @param buffer the buffer to be used for the copy
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.7
     */
    static long copy(final Reader input, final Appendable output, final CharBuffer buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            buffer.flip();
            output.append(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * <p>
     * Copied from Apache Commons IO.
     * </p>
     * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Writer</code> to write to
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.3
     */
    static long copyLarge(final Reader input, final Writer output) throws IOException {
        return copyLarge(input, output, new char[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * <p>
     * Copied from Apache Commons IO.
     * </p>
     * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedReader</code>.
     * <p>
     *
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Writer</code> to write to
     * @param buffer the buffer to be used for the copy
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    static long copyLarge(final Reader input, final Writer output, final char[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
