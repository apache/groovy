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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.Charset

import static java.nio.charset.StandardCharsets.ISO_8859_1
import static java.nio.charset.StandardCharsets.UTF_8

/**
 * check that groovy Process methods do their job.
 */
class ProcessTest {
    private static final String PROCESS_ENCODING = 'groovy.process.encoding'

    def myProcess
    private String previousProcessEncoding

    @BeforeEach
    void setUp() {
        previousProcessEncoding = System.getProperty(PROCESS_ENCODING)
        myProcess = new MockProcess()
    }

    @Test
    void testProcessAppendBytes() {
        def myBytes = "mooky".getBytes()

        myProcess << myBytes

        def result = myProcess.outputStream.toByteArray()
        assert result != null
        assert Arrays.equals(myBytes, result)
    }

    @Test
    void testProcessAppendTwoByteArrays() {
        def myBytes1 = "foo".getBytes()
        def myBytes2 = "bar".getBytes()

        myProcess << myBytes1 << myBytes2

        def result = myProcess.outputStream.toByteArray()
        assert result != null
        assert result.size() == myBytes1.size() + myBytes2.size()
    }

    @Test
    void testProcessAppend() {
        myProcess << "mooky"
        assert "mooky" == myProcess.outputStream.toString()
    }

    @Test
    void testProcessInputStream() {
        assert myProcess.in instanceof InputStream
        assert myProcess.in != null
    }

    @Test
    void testProcessText() {
        assert "" == myProcess.text
    }

    @Test
    void testProcessTextUsesSuppliedCharset() {
        // a single 0xE9 byte is 'é' in ISO-8859-1 but is not valid UTF-8
        def latin1 = new MockProcess([0xE9] as byte[])
        assert "é" == latin1.getText(ISO_8859_1)

        def utf8 = new MockProcess("é".getBytes(UTF_8))
        assert "é" == utf8.getText(UTF_8)
    }

    @Test
    void testProcessTextDefaultsToTheNativeEncoding() {
        def bytes = [0xE9] as byte[]
        def expected = new String(bytes, Charset.forName(System.getProperty("native.encoding")))
        assert expected == new MockProcess(bytes).text
    }

    @Test
    void testProcessEncodingPropertyOverridesTheNativeEncoding() {
        System.setProperty(PROCESS_ENCODING, "ISO-8859-1")
        assert "é" == new MockProcess([0xE9] as byte[]).text

        System.setProperty(PROCESS_ENCODING, "UTF-8")
        assert "é" == new MockProcess("é".getBytes(UTF_8)).text
    }

    @Test
    void testProcessEncodingPropertyIgnoredWhenNotAValidCharset() {
        System.setProperty(PROCESS_ENCODING, "no-such-charset")
        def bytes = [0xE9] as byte[]
        def expected = new String(bytes, Charset.forName(System.getProperty("native.encoding")))
        assert expected == new MockProcess(bytes).text
    }

    @Test
    void testConsumeProcessOutputUsesSuppliedCharset() {
        def out = new StringBuilder()
        def err = new StringBuilder()
        def proc = new MockProcess([0xE9] as byte[], [0xE8] as byte[])

        proc.waitForProcessOutput(out, err, ISO_8859_1)

        assert "é\n" == out.toString()
        assert "è\n" == err.toString()
    }

    @Test
    void testWaitForResultUsesSuppliedCharset() {
        def result = new MockProcess([0xE9] as byte[]).waitForResult(ISO_8859_1)

        assert "é\n" == result.out
        assert result.ok
    }

    @Test
    void testProcessAppendEncodesUsingTheProcessEncoding() {
        System.setProperty(PROCESS_ENCODING, "ISO-8859-1")

        myProcess << "é"

        assert [0xE9] as byte[] == myProcess.outputStream.toByteArray()
    }

    @Test
    void testProcessErrorStream() {
        assert myProcess.err instanceof InputStream
        assert myProcess.err != null
    }

    @Test
    void testProcessOutputStream() {
        assert myProcess.out instanceof OutputStream
        assert myProcess.out != null
    }

    // @todo - ps.waitForOrKill(secs) creates its own thread, leave this out of test suite for now...

    @AfterEach
    void tearDown() {
        if (previousProcessEncoding == null) {
            System.clearProperty(PROCESS_ENCODING)
        } else {
            System.setProperty(PROCESS_ENCODING, previousProcessEncoding)
        }
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
        this(new byte[0], new byte[0])
    }

    MockProcess(byte[] stdout, byte[] stderr = new byte[0]) {
        i = new ByteArrayInputStream(stdout)
        e = new ByteArrayInputStream(stderr)
        o = new ByteArrayOutputStream()
    }

    void destroy() {}

    int exitValue() { return 0 }

    InputStream getErrorStream() { return e }

    InputStream getInputStream() { return i }

    OutputStream getOutputStream() { return o }

    int waitFor() { return 0 }
}
