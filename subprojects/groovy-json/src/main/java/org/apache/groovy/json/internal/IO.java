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
package org.apache.groovy.json.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Reader and writer helpers used by the JSON parser internals.
 */
public class IO {

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Reads all characters from a reader into a recyclable buffer and closes the reader.
     *
     * @param input reader supplying characters
     * @param charBuf existing buffer to reuse, or {@code null} to allocate one
     * @param bufSize initial buffer size when a new buffer is allocated
     * @return buffer containing the reader contents
     */
    public static CharBuf read(Reader input, CharBuf charBuf, final int bufSize) {
        if (charBuf == null) {
            charBuf = CharBuf.create(bufSize);
        } else {
            charBuf.readForRecycle();
        }

        try {
            char[] buffer = charBuf.toCharArray();
            int size = input.read(buffer);
            if (size != -1) {
                charBuf._len(size);
            }
            if (size < 0) {
                return charBuf;
            }

            copy(input, charBuf);
        } catch (IOException e) {
            Exceptions.handle(e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                Exceptions.handle(e);
            }
        }

        return charBuf;
    }

    /**
     * Copies all characters from a reader to a writer.
     *
     * @param input reader supplying characters
     * @param output writer receiving characters
     * @return number of copied characters, or {@code -1} when the count exceeds {@link Integer#MAX_VALUE}
     */
    public static int copy(Reader input, Writer output) {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Copies all characters from a reader to a writer using the default buffer size.
     *
     * @param reader reader supplying characters
     * @param writer writer receiving characters
     * @return number of copied characters
     */
    public static long copyLarge(Reader reader, Writer writer) {
        return copyLarge(reader, writer, new char[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copies all characters from a reader to a writer using the supplied buffer.
     *
     * @param reader reader supplying characters
     * @param writer writer receiving characters
     * @param buffer temporary buffer to reuse during copying
     * @return number of copied characters
     */
    public static long copyLarge(Reader reader, Writer writer, char[] buffer) {
        long count = 0;
        int n;

        try {
            while (EOF != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
                count += n;
            }
        } catch (IOException e) {
            Exceptions.handle(e);
        }
        return count;
    }
}
