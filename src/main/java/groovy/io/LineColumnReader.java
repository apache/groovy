/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * The <code>LineColumnReader</code> is an extension to <code>BufferedReader</code>
 * that keeps track of the line and column information of where the cursor is.
 *
 * @since 1.8.0
 */
public class LineColumnReader extends BufferedReader {

    /**
     * The current line position
     */
    private long line = 1;

    /**
     * The current column position
     */
    private long column = 1;

    /**
     * The latest marked line position
     */
    private long lineMark = 1;

    /**
     * The latest marked line position
     */
    private long columnMark = 1;

    private boolean newLineWasRead = false;

    /**
     * Constructor wrapping a <code>Reader</code>
     * (<code>FileReader</code>, <code>FileReader</code>, <code>InputStreamReader</code>, etc.)
     *
     * @param reader the reader to wrap
     */
    public LineColumnReader(Reader reader) {
        super(reader);
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
    public void mark(int readAheadLimit) throws IOException {
        lineMark = line;
        columnMark = column;
        super.mark(readAheadLimit);
    }

    /**
     * Resets the stream to the most recent mark.
     */
    @Override
    public void reset() throws IOException {
        line = lineMark;
        column = columnMark;
        super.reset();
    }

    /**
     * Reads a single character.
     *
     * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff),
     *      or -1 if the end of the stream has been reached
     */
    @Override
    public int read() throws IOException {
        if (newLineWasRead) {
            line += 1;
            column = 1;
            newLineWasRead = false;
        }

        int charRead = super.read();
        if (charRead > -1) {
            char c = (char)charRead;
            // found a \r or \n, like on Mac or Unix
            // could also be Windows' \r\n
            if (c == '\r' || c == '\n') {
                newLineWasRead = true;
                if (c == '\r') {
                    mark(1);
                    c = (char)super.read();
                    // check if we have \r\n like on Windows
                    // if it's not \r\n we reset, otherwise, the \n is just consumed
                    if (c != '\n') {
                        reset();
                    }
                }
            } else {
                column += 1;
            }
        }

        return charRead;
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param chars Destination array of char
     * @param startOffset Offset at which to start storing characters
     * @param length Maximum number of characters to read
     * @return an exception if an error occurs
     */
    @Override
    public int read(char[] chars, int startOffset, int length) throws IOException {
        for (int i = startOffset; i <= startOffset + length; i++) {
            int readInt = read();
            if (readInt == -1) return i - startOffset;
            chars[i] = (char)readInt;
        }
        return length;
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'),
     * a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including any line-termination characters,
     *      or null if the end of the stream has been reached
     */
    @Override
    public String readLine() throws IOException {
        StringBuilder result = new StringBuilder();
        for (;;) {
            int intRead = read();
            if (intRead == -1) {
                return result.length() == 0 ? null : result.toString();
            }

            char c = (char)intRead;
            if (c == '\n' || c == '\r') break;
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Skips characters.
     *
     * @param toSkip the number of characters to skip
     * @return The number of characters actually skipped
     */
    @Override
    public long skip(long toSkip) throws IOException {
        for (long i = 0; i < toSkip; i++) {
            int intRead = read();
            if (intRead == -1) return i;
        }
        return toSkip;
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
    public int read(char[] chars) throws IOException {
        return read(chars, 0, chars.length - 1);
    }

    /**
     * Not implemented.
     *
     * @param buffer Destination buffer
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @throws UnsupportedOperationException as the method is not implemented
     */
    @Override
    public int read(CharBuffer buffer) {
        throw new UnsupportedOperationException("read(CharBuffer) not yet implemented");
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Once the stream has been closed, further read(), ready(), mark(), reset(), or skip() invocations
     * will throw an IOException. Closing a previously closed stream has no effect.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }

    public long getColumn() {
        return column;
    }

    public void setColumn(long column) {
        this.column = column;
    }

    public long getColumnMark() {
        return columnMark;
    }

    public void setColumnMark(long columnMark) {
        this.columnMark = columnMark;
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line) {
        this.line = line;
    }

    public long getLineMark() {
        return lineMark;
    }

    public void setLineMark(long lineMark) {
        this.lineMark = lineMark;
    }
}
