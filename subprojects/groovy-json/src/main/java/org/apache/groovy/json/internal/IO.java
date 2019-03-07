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

public class IO {

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

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

    public static int copy(Reader input, Writer output) {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader reader, Writer writer) {
        return copyLarge(reader, writer, new char[DEFAULT_BUFFER_SIZE]);
    }

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
