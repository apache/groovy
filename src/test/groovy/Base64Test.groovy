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

import java.nio.charset.StandardCharsets

class Base64Test extends GroovyTestCase {
    String testString = '\u00A71234567890-=\u00B1!@\u00A3\$%^&*()_+qwertyuiop[]QWERTYUIOP{}asdfghjkl;\'\\ASDFGHJKL:"|`zxcvbnm,./~ZXCVBNM<>?\u0003\u00ff\u00f0\u000f'
    byte[] testBytes = testString.getBytes("ISO-8859-1")

    // Test bytes that have both the 62nd and 63rd base64 alphabet in the encoded string
    static final byte[] testBytesChar62And63 = new BigInteger('4bf7ce5201fe239ab42ebead5acd8fa3', 16).toByteArray()

    void testCodec() {
        // turn the bytes back into a string for later comparison
        def savedString = new String(testBytes, "ISO-8859-1")
        def encodedBytes = testBytes.encodeBase64().toString()
        def decodedBytes = encodedBytes.decodeBase64()
        def decodedString = new String(decodedBytes, "ISO-8859-1")
        assert decodedString.equals(testString)
        assert decodedString.equals(savedString)
    }

    // embedding special characters in a string without unicode will yield platform
    // specific results but should still convert back to what it started with
    void testCodecSpecialCharacters() {
        def specialString = "§±£"
        def specialBytes = specialString.getBytes("ISO-8859-1")
        // turn the bytes back into a string for later comparison
        def savedString = new String(specialBytes, "ISO-8859-1")
        def encodedBytes = specialBytes.encodeBase64().toString()
        def decodedBytes = encodedBytes.decodeBase64()
        def decodedString = new String(decodedBytes, "ISO-8859-1")
        assert decodedString.equals(savedString)
    }

    void testChunking() {
        def encodedBytes = testBytes.encodeBase64(true).toString()
        // Make sure the encoded, chunked data ends with '\r\n', the chunk separator per RFC 2045 (see also RFC 4648)
        assert encodedBytes.endsWith("\r\n")
        def lines = encodedBytes.split()
        def line0 = lines[0].trim()
        def line1 = lines[1].trim()
        // it's important that the data is chunked to 76 characters, per the spec
        assert line0.size() == 76
        assert line0 == 'pzEyMzQ1Njc4OTAtPbEhQKMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31hc2RmZ2hq'
        assert line1 == 'a2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4/A//wDw=='
        // check we allow \n \r \t and space to be ignored when decoding for round-tripping purposes
        assert encodedBytes.decodeBase64() == testBytes
        assert (encodedBytes + '\t ').decodeBase64() == testBytes
    }

    void testNonChunked() {
        def encodedBytes = testBytes.encodeBase64().toString()
        assert encodedBytes == 'pzEyMzQ1Njc4OTAtPbEhQKMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31hc2RmZ2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4/A//wDw=='
    }

    void testRfc4648Section10Encoding() {
        assert b64('') == ''
        assert b64('f') == 'Zg=='
        assert b64('fo') == 'Zm8='
        assert b64('foo') == 'Zm9v'
        assert b64('foob') == 'Zm9vYg=='
        assert b64('fooba') == 'Zm9vYmE='
        assert b64('foobar') == 'Zm9vYmFy'
    }

    void testRfc4648Section10Decoding() {
        assert decodeB64('') == ''

        assert decodeB64('Zg') == 'f'
        assert decodeB64('Zg==') == 'f'

        assert decodeB64('Zm8') == 'fo'
        assert decodeB64('Zm8=') == 'fo'

        assert decodeB64('Zm9v') == 'foo'

        assert decodeB64('Zm9vYg') == 'foob'
        assert decodeB64('Zm9vYg==') == 'foob'

        assert decodeB64('Zm9vYmE') == 'fooba'
        assert decodeB64('Zm9vYmE=') == 'fooba'

        assert decodeB64('Zm9vYmFy') == 'foobar'
    }

    void testRfc4648Section10EncodingUrlSafe() {
        assert b64url('') == ''
        assert b64url('f') == 'Zg'
        assert b64url('fo') == 'Zm8'
        assert b64url('foo') == 'Zm9v'
        assert b64url('foob') == 'Zm9vYg'
        assert b64url('fooba') == 'Zm9vYmE'
        assert b64url('foobar') == 'Zm9vYmFy'
    }

    void testRfc4648Section10EncodingUrlSafeWithPadding() {
        assert b64url('', true) == ''
        assert b64url('f', true) == 'Zg=='
        assert b64url('fo', true) == 'Zm8='
        assert b64url('foo', true) == 'Zm9v'
        assert b64url('foob', true) == 'Zm9vYg=='
        assert b64url('fooba', true) == 'Zm9vYmE='
        assert b64url('foobar', true) == 'Zm9vYmFy'
    }

    void testRfc4648Section10DecodingUrlSafe() {
        assert decodeB64url('') == ''

        assert decodeB64url('Zg') == 'f'
        assert decodeB64url('Zg==') == 'f'

        assert decodeB64url('Zm8') == 'fo'
        assert decodeB64url('Zm8=') == 'fo'

        assert decodeB64url('Zm9v') == 'foo'

        assert decodeB64url('Zm9vYg') == 'foob'
        assert decodeB64url('Zm9vYg==') == 'foob'

        assert decodeB64url('Zm9vYmE') == 'fooba'
        assert decodeB64url('Zm9vYmE=') == 'fooba'

        assert decodeB64url('Zm9vYmFy') == 'foobar'
    }

    void testEncodingWithChar62And63() {
        assert testBytesChar62And63.encodeBase64().toString() == 'S/fOUgH+I5q0Lr6tWs2Pow=='
    }

    void testUrlSafeEncodingWithChar62And63() {
        assert testBytesChar62And63.encodeBase64Url().toString() == 'S_fOUgH-I5q0Lr6tWs2Pow'
        assert testBytesChar62And63.encodeBase64Url(true).toString() == 'S_fOUgH-I5q0Lr6tWs2Pow=='
    }

    void testDecodingWithChar62And63() {
        assert 'S/fOUgH+I5q0Lr6tWs2Pow=='.decodeBase64() == testBytesChar62And63
        assert 'S/fOUgH+I5q0Lr6tWs2Pow'.decodeBase64() == testBytesChar62And63
    }

    void testUrlSafeDecodingWithChar62And63() {
        assert 'S_fOUgH-I5q0Lr6tWs2Pow=='.decodeBase64Url() == testBytesChar62And63
        assert 'S_fOUgH-I5q0Lr6tWs2Pow'.decodeBase64Url() == testBytesChar62And63
    }

    void testUrlSafeEncodingByDefaultOmitsPadding() {
        assert testBytes.encodeBase64Url().toString() ==
                'pzEyMzQ1Njc4OTAtPbEhQKMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31h' +
                'c2RmZ2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4_A__wDw'
    }

    void testUrlSafeEncodingWithPadding() {
        assert testBytes.encodeBase64Url(true).toString() ==
                'pzEyMzQ1Njc4OTAtPbEhQKMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31h' +
                'c2RmZ2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4_A__wDw=='
    }

    void testDecodingNonBase64Alphabet() {
        shouldFail {
            decodeB64('S_fOUgH-I5q0Lr6tWs2Pow==')
        }
    }

    void testUrlSafeDecodingNonUrlSafeAlphabet() {
        shouldFail {
            decodeB64url('S/fOUgH+I5q0Lr6tWs2Pow==')
        }
    }

    void testDecodingWithInnerPad() {
        shouldFail {
            decodeB64('Zm9v=YmE=')
        }
    }

    void testUrlSafeDecodingWithInnerPad() {
        shouldFail {
            decodeB64url('Zm9v=YmE=')
        }
    }

    // Test helper methods
    private static String b64(String s) {
        s.getBytes(StandardCharsets.UTF_8).encodeBase64().toString()
    }

    private static String b64url(String s, boolean pad=false) {
        s.getBytes(StandardCharsets.UTF_8).encodeBase64Url(pad).toString()
    }

    private static String decodeB64(String s) {
        new String(s.decodeBase64(), StandardCharsets.UTF_8)
    }

    private static String decodeB64url(String s) {
        new String(s.decodeBase64Url(), StandardCharsets.UTF_8)
    }
}
