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
 * Unit tests for the {@link MessageSource} class.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class WrappedInputStreamTest
    extends GroovyTestCase
{
    void testWrapEmpty() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream()
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        assertEquals(0, stream.available())
        assertEquals(false, stream.markSupported())
    }

    void testWrapStream() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream("abc".getBytes())
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        assertEquals(3, stream.available())
        assertEquals(false, stream.markSupported())
        assertEquals('a', stream.read())
        assertEquals(2, stream.available())
        byte[] bytes = [0, 0, 0, 0] as byte[]
        assertEquals(2, stream.read(bytes))
        assertEquals(['b', 'c', 0, 0], bytes)
        assertEquals(0, stream.available())
    }

    void testWrapInserted() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream()
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        stream.insert("xyz")
        assertEquals(3, stream.available())
        assertEquals(false, stream.markSupported())
        assertEquals('x', stream.read())
        assertEquals(2, stream.available())
        byte[] bytes = [0, 0, 0, 0] as byte[]
        assertEquals(2, stream.read(bytes))
        assertEquals(['y', 'z', 0, 0], bytes)
        assertEquals(0, stream.available())
    }

    void testWrapBoth() {
        ByteArrayInputStream mockStream = new ByteArrayInputStream("abc".getBytes())
        WrappedInputStream stream = new WrappedInputStream(mockStream)
        stream.insert("xyz")
        // wrapped stream first counts inserted chars, which will be read
        assertEquals(3, stream.available())
        assertEquals(false, stream.markSupported())
        assertEquals('x', stream.read())
        byte[] bytes = [0, 0, 0] as byte[]
        // wrapped stream reads wrapped stream first
        assertEquals(2, stream.read(bytes))
        assertEquals(['y', 'z', 0], bytes)
        assertEquals(3, stream.read(bytes))
        assertEquals(['a', 'b', 'c'], bytes)
        assertEquals(0, stream.available())
    }
}