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
package org.apache.groovy.groovysh.util

import groovy.test.GroovyTestCase

/**
 * Unit tests for the {@link WrappedInputStream} class.
 */
class WrappedInputStreamTest extends GroovyTestCase {
    void testWrapEmpty() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream()
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        assert 0 == stream.available()
        assert !stream.markSupported()
    }

    void testWrapStream() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream('abc'.bytes)
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        assert 3 == stream.available()
        assert !stream.markSupported()
        assert 'a' == stream.read()
        assert 2 == stream.available()
        byte[] bytes = [0, 0, 0, 0] as byte[]
        assert 2 == stream.read(bytes)
        assert ['b', 'c', 0, 0] == bytes
        assert 0 == stream.available()
    }

    void testWrapInserted() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream()
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        stream.insert('xyz')
        assert 3 == stream.available()
        assert !stream.markSupported()
        assert 'x' == stream.read()
        assert 2 == stream.available()
        byte[] bytes = [0, 0, 0, 0] as byte[]
        assert 2 == stream.read(bytes)
        assert ['y', 'z', 0, 0] == bytes
        assert 0 == stream.available()
    }

    void testWrapBoth() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream('abc'.bytes)
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        stream.insert('xyz')
        // wrapped stream first counts inserted chars, which will be read
        assert 3 == stream.available()
        assert !stream.markSupported()
        assert 'x' == stream.read()
        byte[] bytes = [0, 0, 0] as byte[]
        // wrapped stream reads wrapped stream first
        assert 2 == stream.read(bytes)
        assert ['y', 'z', 0] == bytes
        assert 3 == stream.read(bytes)
        assert ['a', 'b', 'c'] == bytes
        assert 0 == stream.available()
    }
}
