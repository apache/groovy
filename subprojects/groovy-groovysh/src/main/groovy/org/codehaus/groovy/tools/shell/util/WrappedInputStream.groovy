/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.tools.shell.util

/**
 * In order to modify JLine Behavior, we need to wrap the IO streams so we can hack into them
 * This allows autoindent and redisplaying the chars typed so far after exceptions during completion
 */
class WrappedInputStream extends InputStream {

    InputStream wrapped;
    // observed sometimes ClassNotFoundException when not qualifying with java.io
    java.io.ByteArrayInputStream inserted = new java.io.ByteArrayInputStream();
    protected final Logger log = Logger.create(WrappedInputStream.class)

    /**
     * Construct a new IO container using system streams.
     */
    public WrappedInputStream(InputStream wrapped) {
        super()
        this.wrapped = wrapped
    }

    @Override
    int read() throws IOException {
        if (inserted != null && inserted.available() > 0) {
            return inserted.read()
        }
        return wrapped.read()
    }

    public void insert(String chars) {
        inserted.close()
        inserted = new ByteArrayInputStream(chars.getBytes("UTF-8"))
    }

    @Override
    public int read(byte[] b) throws java.io.IOException {
        def insertb = inserted.read(b)
        if (insertb > 0) {
            return insertb
        }
        return wrapped.read(b)
    }

    @Override
    public int read(byte[] b, int off, int len) throws java.io.IOException {
        def insertb = inserted.read(b, off, len)
        if (insertb > 0) {
            return insertb
        }
        return wrapped.read(b, off, len)
    }

    @Override
    public long skip(long n) throws java.io.IOException {
        def skipb = inserted.skip(n)
        if (skipb > 0) {
            return skipb
        }
        return wrapped.skip(n)
    }

    @Override
    public int available() throws java.io.IOException {
        int x = inserted.available()
        if (x > 0) {
            return x
        }
        return wrapped.available()
    }

    @Override
    public void close() throws java.io.IOException {
        wrapped.close()
        inserted.close()
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException()
    }

    @Override
    public synchronized void reset() throws java.io.IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    public boolean markSupported() {
        return false
    }
}
