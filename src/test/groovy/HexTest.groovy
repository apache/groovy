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
 * Test cases for encodeHex methods in DefaultGroovyMethods
 */
class HexTest extends GroovyTestCase {

    void testEncodeHex() {
        // test with some arbitrary bytes
        def testBytes = [0x00, 0x01, 0x0f, 0xa0, 0x11, 0xf0, 0x00, 0xff] as byte[]
        assert testBytes instanceof byte[]
        assert testBytes.encodeHex().toString() == "00010fa011f000ff"

        // test with empty array
        assert ([] as byte[]).encodeHex().toString() == ""

        // test with single byte
        ([0] as byte[]).encodeHex().toString() == "00"
        ([10] as byte[]).encodeHex().toString() == "0a"
        ([255] as byte[]).encodeHex().toString() == "ff"

        // test using Byte[] (Byte objects)
        def testByteObjects = [0x00, 0x01, 0x0f, 0xa0, 0x11, 0xf0, 0x00, 0xff] as Byte[]
        assert testByteObjects instanceof Byte[]
        assert !(testByteObjects instanceof byte[])
        assert testByteObjects.encodeHex().toString() == "00010fa011f000ff"
    }

    void testDecodeHex() {
        // test with empty string
        def bytes = "".decodeHex()
        assert bytes instanceof byte[]
        assert bytes.length == 0

        // test with odd number of characters
        assert "odd number of characters in hex string" == shouldFail(NumberFormatException) {
            "abcdefg".decodeHex()
        }

        // test with invalid characters
        shouldFail(NumberFormatException) {
            "1g".decodeHex()
        }

        // test to make sure a leading zero is handled correctly
        bytes = "0a".decodeHex()
        assert bytes.length == 1
        assert bytes[0] == (byte) 10

        // test with mix of upper case and lower case
        bytes = "0b0A".decodeHex()
        assert bytes.length == 2
        assert bytes[0] == (byte) 11
        assert bytes[1] == (byte) 10
    }

    void testEncodeAndDecode() {
        // test with an arbitary string
        def testString = "00010fa011f000ff"
        assert testString.decodeHex().encodeHex().toString() == testString

        // test with a string containing all possible byte values
        def testBytes = (0..255).collect {(byte) it}
        def encoded = (testBytes as byte[]).encodeHex().toString()
        assert encoded.length() == 512
        assert encoded.decodeHex() == testBytes
    }
}
