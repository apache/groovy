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
package org.codehaus.groovy.runtime

import groovy.test.GroovyAssert
import groovy.test.GroovyTestCase

class IOGroovyMethodsTest extends GroovyTestCase {

    void testWithAutoCloseable() {
        def closeable = new DummyAutoCloseable()
        def closeableParam = null
        def result = closeable.withCloseable {
            closeableParam = it
            123
        }
        assert closeableParam == closeable
        assert result == 123
        assert closeable.closed
    }

    void testWithAutoCloseableDoesNotSuppressException() {
        def closeable = new DummyAutoCloseable(new Exception('close exception'))
        def throwable = GroovyAssert.shouldFail(UnsupportedOperationException) {
            closeable.withCloseable {
                throw new UnsupportedOperationException('not a close exception')
            }
        }
        assert closeable.closed
        assert throwable.message == 'not a close exception'
        assert throwable.suppressed.find { it.message == 'close exception' }
    }

    void testWithAutoCloseableAndException() {
        def closeable = new DummyAutoCloseable(new Exception('close exception'))
        def result = null
        def message = shouldFail(Exception) {
            closeable.withCloseable {
                result = 123
            }
        }
        assert result == 123
        assert message == 'close exception'
    }

    void testWithCloseable() {
        def closeable = new DummyCloseable()
        def closeableParam = null
        def result = closeable.withCloseable {
            closeableParam = it
            123
        }
        assert closeableParam == closeable
        assert result == 123
        assert closeable.closed
    }

    void testWithCloseableDoesNotSuppressException() {
        def closeable = new DummyCloseable(new IOException('close ioexception'))
        def throwable = GroovyAssert.shouldFail(Exception) {
            closeable.withCloseable {
                throw new Exception('not a close ioexception')
            }
        }
        assert closeable.closed
        assert throwable.message == 'not a close ioexception'
        assert throwable.suppressed.find { it.message == 'close ioexception' }
    }

    void testWithCloseableAndException() {
        def closeable = new DummyCloseable(new IOException('close ioexception'))
        def result = null
        def message = shouldFail(IOException) {
            closeable.withCloseable {
                result = 123
            }
        }
        assert result == 123
        assert message == 'close ioexception'
    }

    // --------------------------------------------------------------------
    // Helper Classes

    static class DummyAutoCloseable implements AutoCloseable {
        Exception throwOnClose
        boolean closed
        DummyAutoCloseable(Exception throwOnClose=null) {
            this.throwOnClose = throwOnClose
        }
        @Override
        void close() throws Exception {
            closed = true
            if (throwOnClose) {
                throw throwOnClose
            }
        }
    }

    static class DummyCloseable implements Closeable {
        Exception throwOnClose
        boolean closed
        DummyCloseable(Exception throwOnClose=null) {
            this.throwOnClose = throwOnClose
        }
        @Override
        void close() throws IOException {
            closed = true
            if (throwOnClose) {
                throw throwOnClose
            }
        }
    }

}
