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
package org.codehaus.groovy.tools.shell.util

/**
 * In order to modify JLine Behavior, we need to wrap the IO streams so we can hack into them
 * This allows autoindent and redisplaying the chars typed so far after exceptions during completion
 */
@Deprecated
class WrappedInputStream extends InputStream implements Closeable {

    final InputStream wrapped
    // observed sometimes ClassNotFoundException when not qualifying with java.io
    java.io.ByteArrayInputStream inserted = new java.io.ByteArrayInputStream()


    /**
     * Construct a new IO container using system streams.
     */
    WrappedInputStream(final InputStream wrapped) {
        super()
        this.wrapped = wrapped
    }

    @Override
    int read() throws java.io.IOException {
        if (inserted != null && inserted.available() > 0) {
            return inserted.read()
        }
        return wrapped.read()
    }

    void insert(String chars) {
        inserted.close()
        inserted = new java.io.ByteArrayInputStream(chars.getBytes('UTF-8'))
    }

    @Override
    int read(byte[] b) throws java.io.IOException {
        def insertb = inserted.read(b)
        if (insertb > 0) {
            return insertb
        }
        return wrapped.read(b)
    }

    @Override
    int read(byte[] b, int off, int len) throws java.io.IOException {
        def insertb = inserted.read(b, off, len)
        if (insertb > 0) {
            return insertb
        }
        return wrapped.read(b, off, len)
    }

    @Override
    long skip(long n) throws java.io.IOException {
        def skipb = inserted.skip(n)
        if (skipb > 0) {
            return skipb
        }
        return wrapped.skip(n)
    }

    @Override
    int available() throws java.io.IOException {
        int x = inserted.available()
        if (x > 0) {
            return x
        }
        return wrapped.available()
    }

    @Override
    void close() throws java.io.IOException {
        wrapped.close()
        inserted.close()
    }

    @Override
    synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException()
    }

    @Override
    synchronized void reset() throws java.io.IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean markSupported() {
        return false
    }
}
