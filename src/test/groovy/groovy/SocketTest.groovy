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
package groovy

import groovy.test.GroovyTestCase

/**
 * check that groovy Socket methods do their job.
 */
class SocketTest extends GroovyTestCase {
    def mySocket

    void setUp() {
        mySocket = new MockSocket()
    }

    void testSocketAppendBytes() {
        def myBytes = "mooky".getBytes()
        mySocket << myBytes
        def result = mySocket.outputStream.toByteArray()
        assert result != null
        assert Arrays.equals(myBytes, result)
    }

    void testSocketAppendTwoByteArrays() {
        def myBytes1 = "foo".getBytes()
        def myBytes2 = "bar".getBytes()
        mySocket << myBytes1 << myBytes2
        def result = mySocket.outputStream.toByteArray()
        assert result != null
        assert result.size() == myBytes1.size() + myBytes2.size()
    }

    void testSocketAppend() {
        mySocket << "mooky"
        assert "mooky" == mySocket.outputStream.toString()
    }

    void testSocketWithStreamsClosure() {
        mySocket.withStreams { i, o ->
            assert i instanceof InputStream
            assert i != null
            assert o instanceof OutputStream
            assert o != null
        }
    }

    void tearDown() {
        mySocket.close()
    }
}

/**
 * simple, unconnected Socket, used purely for test cases
 */
class MockSocket extends Socket {
    private def i
    private def o

    public MockSocket() {
        i = new ByteArrayInputStream(new Byte[0])
        o = new ByteArrayOutputStream()
    }

    public InputStream getInputStream() { return i }

    public OutputStream getOutputStream() { return o }
}
