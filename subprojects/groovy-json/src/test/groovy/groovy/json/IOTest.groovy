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
package groovy.json

import org.apache.groovy.json.internal.CharBuf
import org.apache.groovy.json.internal.IO

/**
 * Test the internal IO class
 */
class IOTest extends GroovyTestCase {

    public static final String TEST_STRING = '{"results":[{"columns":["n"],"data":[{"row":[{"name":"Alin Coen Band","type":"group"}]}]}],"errors":[]}'

    int bufSize = 256

    void testReadProper() {
        IO io = new IO()
        ProperReader reader = new ProperReader();
        CharBuf buffer = io.read(reader, null, bufSize)
        int len = buffer.len()
        char[] rbuf = buffer.readForRecycle()
        String result = new String(rbuf, 0, len)
        assertEquals(TEST_STRING, result)
    }

    /**
     * See https://issues.apache.org/jira/browse/GROOVY-7132
     */
    void testReadBumpy() {
        IO io = new IO()
        BumpyReader reader = new BumpyReader();
        CharBuf buffer = io.read(reader, null, bufSize)
        int len = buffer.len()
        char[] rbuf = buffer.readForRecycle()
        String result = new String(rbuf, 0, len)
        assertEquals(TEST_STRING, result)
    }
}

/**
 * This reader fills the char array at certain points only partially
 * and returns a value < char_array.length
 */
class BumpyReader extends Reader {

    int index = 0;
    def stopIndex = [69, 84, 500]
    int nextStop = 0;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (index >= IOTest.TEST_STRING.size()) {
            return -1
        }
        int num = 0
        while (num < len && index < stopIndex[nextStop] && index < IOTest.TEST_STRING.size()) {
            cbuf[off + num] = IOTest.TEST_STRING[index]
            num++
            index++
        }
        if (index == stopIndex[nextStop]) {
            nextStop++
        }
        return num;
    }

    @Override
    public void close() throws IOException {
    }
}

/**
 * This reader fills always the char array completely until reaching the end
 * of the string.
 */
class ProperReader extends Reader {

    int index = 0;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (index >= IOTest.TEST_STRING.size()) {
            return -1
        }
        int num = 0
        while (num < len && index < IOTest.TEST_STRING.size()) {
            cbuf[off + num] = IOTest.TEST_STRING[index]
            num++
            index++
        }
        return num;
    }

    @Override
    public void close() throws IOException {
    }
}
