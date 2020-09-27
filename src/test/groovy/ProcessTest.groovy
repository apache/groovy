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
 * check that groovy Process methods do their job.
 */
class ProcessTest extends GroovyTestCase {
    def myProcess

    void setUp() {
        myProcess = new MockProcess()
    }

    void testProcessAppendBytes() {
        def myBytes = "mooky".getBytes()

        myProcess << myBytes

        def result = myProcess.outputStream.toByteArray()
        assert result != null
        assert Arrays.equals(myBytes, result)
    }

    void testProcessAppendTwoByteArrays() {
        def myBytes1 = "foo".getBytes()
        def myBytes2 = "bar".getBytes()

        myProcess << myBytes1 << myBytes2

        def result = myProcess.outputStream.toByteArray()
        assert result != null
        assert result.size() == myBytes1.size() + myBytes2.size()
    }

    void testProcessAppend() {
        myProcess << "mooky"
        assert "mooky" == myProcess.outputStream.toString()
    }

    void testProcessInputStream() {
        assert myProcess.in instanceof InputStream
        assert myProcess.in != null
    }

    void testProcessText() {
        assert "" == myProcess.text
    }

    void testProcessErrorStream() {
        assert myProcess.err instanceof InputStream
        assert myProcess.err != null
    }

    void testProcessOutputStream() {
        assert myProcess.out instanceof OutputStream
        assert myProcess.out != null
    }

    // @todo - ps.waitForOrKill(secs) creates its own thread, leave this out of test suite for now...

    void tearDown() {
        myProcess.destroy()
    }
}

/**
 * simple Process, used purely for test cases
 */
class MockProcess extends Process {
    private def e
    private def i
    private def o

    MockProcess() {
        e = new AnotherMockInputStream()
        i = new AnotherMockInputStream()
        o = new ByteArrayOutputStream()
    }

    void destroy() {}

    int exitValue() { return 0 }

    InputStream getErrorStream() { return e }

    InputStream getInputStream() { return i }

    OutputStream getOutputStream() { return o }

    int waitFor() { return 0 }
}

/**
 * only needed for workaround in groovy, 
 *     new ByteArrayInputStream(myByteArray) doesn't work at mo... (28-Sep-2004)
 */
class AnotherMockInputStream extends InputStream {
    int read() { return -1 }
}
