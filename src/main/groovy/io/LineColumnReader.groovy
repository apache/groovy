/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.io

import java.nio.CharBuffer

/**
 * The <code>LineColumnReader</code> is an extension to <code>BufferedReader</code>
 * that keeps track of the line and column information of where the cursor is.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class LineColumnReader extends BufferedReader {

    /**
     * The current line position
     */
    long line = 1

    /**
     * The current column position
     */
    long column = 1

    /**
     * The latest marked line position
     */
    long lineMark = 1

    /**
     * The latest marked line position
     */
    long columnMark = 1

    private boolean newLineWasRead = false

    /**
     * Constructor wrapping a <code>Reader</code>
     * (<code>FileReader</code>, <code>FileReader</code>, <code>InputStreamReader</code>, etc.)
     *
     * @param reader the reader to wrap
     */
    LineColumnReader(Reader reader) {
        super(reader)
    }

    /**
     * Marks the present position in the stream. Subsequent calls to reset() will attempt to reposition the stream to this point.
     *
     * @param readAheadLimit  Limit on the number of characters that may be read while still preserving the mark.
     *      An attempt to reset the stream after reading characters up to this limit or beyond may fail.
     *      A limit value larger than the size of the input buffer will cause a new buffer to be allocated whose size is no smaller than limit.
     *      Therefore large values should be used with care.
     */
    @Override
    void mark(int readAheadLimit) {
        lineMark = line
        columnMark = column
        super.mark(readAheadLimit)
    }

    /**
     * Resets the stream to the most recent mark.
     */
    @Override
    void reset() {
        line = lineMark
        column = columnMark
        super.reset()
    }

    /**
     * Reads a single character.
     *
     * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff),
     *      or -1 if the end of the stream has been reached
     */
    @Override
    int read() {
        if (newLineWasRead) {
            line += 1
            column = 1
            newLineWasRead = false
        }

        def charRead = super.read()
        if (charRead > -1) {
            char c = (char)charRead
            // found a \r or \n, like on Mac or Unix
            // could also be Windows' \r\n
            if (c == '\r' || c == '\n') {
                newLineWasRead = true
                if (c == '\r') {
                    mark(1)
                    c = (char)super.read()
                    // check if we have \r\n like on Windows
                    // if it's not \r\n we reset, otherwise, the \n is just consummed
                    if (c != '\n') {
                        reset()
                    }
                }
            } else {
                column += 1
            }
        }

        return charRead
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param chars Destination array of char
     * @param startOffset Offset at which to start storing characters
     * @param length Maximum number of characters to read
     * @return
     */
    @Override
    int read(char[] chars, int startOffset, int length) {
        for (int i = startOffset; i <= startOffset + length; i++) {
            int readInt = read()
            if (readInt == -1) return i - startOffset
            chars[i] = (char)readInt
        }
        return length
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'),
     * a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including any line-termination characters,
     *      or null if the end of the stream has been reached
     */
    @Override
    String readLine() {
        StringBuilder result = new StringBuilder()
        for (;;) {
            int intRead = read()
            if (intRead == -1) {
                return result.length() == 0 ? null : result.toString()
            }
            
            def c = (char)intRead
            if (c == '\n' || c == '\r') break
            result.append((char)c)
        }
        return result.toString()
    }

    /**
     * Skips characters.
     *
     * @param toSkip the number of characters to skip
     * @return The number of characters actually skipped
     */
    @Override
    long skip(long toSkip) {
        for (long i = 0; i < toSkip; i++) {
            int intRead = read()
            if (intRead == -1) return i
        }
        return toSkip
    }

    /**
     * Reads characters into an array.
     * This method will block until some input is available, an I/O error occurs,
     *  or the end of the stream is reached.
     *
     * @param chars Destination buffer
     * @return The number of characters read, or -1 if the end of the stream has been reached
     */
    @Override
    int read(char[] chars) {
        return read(chars, 0, chars.length - 1)
    }

    /**
     * Not implemented.
     *
     * @param buffer Destination buffer
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @throws UnsupportedOperationException as the method is not implemented
     */
    @Override
    int read(CharBuffer buffer) {
        throw new UnsupportedOperationException("read(CharBuffer) not yet implemented")
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Once the stream has been closed, further read(), ready(), mark(), reset(), or skip() invocations
     * will throw an IOException. Closing a previously closed stream has no effect.
     */
    @Override
    void close() {
        super.close()
    }
}
