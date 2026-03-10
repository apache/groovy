// Do not remove this line: it is used in test below
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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull


/**
 * Test to ensure that readLine() method works on Reader/InputStream
 */
class ReadLineTest {
    def file

    @BeforeEach
    void setUp() {
        file = new File("src/test/groovy/groovy/ReadLineTest.groovy")
    }

    @Test
    void testReadOneLineFromReader() {
        def line
        file.withReader() { line = it.readLine() }
        assert line == "// Do not remove this line: it is used in test below"
    }

    static testString = " �\n �\n\n �\r\n 5\r\r 7\n\r 9"
    static expectedLines = [" �", " �", "", " �", " 5", "", " 7", "", " 9"]
    static String[] expectedLinesSlow = [" �", " �", " �", " 5", " 7"]
    static int[] expectedChars = [' ', '9', -1]

    void readFromReader(Reader reader) throws IOException {
        expectedLines.each { expected ->
            def line = reader.readLine()
            assertEquals(expected, line, "Readline should return correct line")
        }
        assertNull(reader.readLine(), "Readline should return null")
    }

    @Test
    void testBufferedReader() throws IOException {
        Reader reader = new BufferedReader(new StringReader(testString))
        readFromReader(reader)
    }

    @Test
    void testReaderSupportingMark() throws IOException {
        Reader reader = new StringReader(testString)
        readFromReader(reader)
    }

    /*
     * In this case we cannot read more than one line separator
     * Thus empty lines can be returned if line separation is \r\n.
     */

    @Test
    void testReaderSlow() throws IOException {
        Reader reader = new SlowStringReader(testString)
        expectedLinesSlow.each { expected ->
            String line = reader.readLine()
            while (line != null && line.length() == 0) {
                line = reader.readLine()
            }
            assertEquals(expected, line, "Readline should return correct line")
        }
        assertEquals("", reader.readLine(), "Readline should return empty string")

        expectedChars.each { expected ->
            assertEquals(expected, reader.read(), "Remaining characters incorrect")
        }
        assertNull(reader.readLine())
    }
}

class SlowStringReader extends StringReader {
    SlowStringReader(String s) { super(s) }

    boolean markSupported() { return false }
}
