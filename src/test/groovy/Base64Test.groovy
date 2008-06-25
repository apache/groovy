package groovy

class Base64Test extends GroovyTestCase {
    String testString = "§1234567890-=±!@£\$%^&*()_+qwertyuiop[]QWERTYUIOP{}asdfghjkl;'\\ASDFGHJKL:\"|`zxcvbnm,./~ZXCVBNM<>?\u0003\u00ff\u00f0\u000f"
    byte[] testBytes = testString.getBytes("ISO-8859-1")

    void testCodec() {
        // turn the bytes back into a string for later comparison
        def savedString = new String(testBytes, "ISO-8859-1")

        // encode the bytes as base64. This produces a Writable object convert it to a String
        def encodedBytes = testBytes.encodeBase64().toString()

        // decode the base64 back to a byte array
        def decodedBytes = encodedBytes.decodeBase64()

        // turn the byte array back to a String for comparison
        def decodedString = new String(decodedBytes, "ISO-8859-1")

//        assert decodedString.equals(testString)
        assert decodedString.equals(savedString)
    }

    // TODO - reinstate after platform issues fixed
    void _testChunking() {
        def encodedBytes = testBytes.encodeBase64(true).toString()

        // Make sure the encoded, chunked data ends with '\r\n', the chunk separator per RFC 2045
        assert encodedBytes.endsWith("\r\n")

        def lines = encodedBytes.split()
        def line0 = lines[0].trim()
        def line1 = lines[1].trim()

        // it's important that the data is chunked to 76 characters, per the spec
        assert line0.size() == 76
        assert line0 == 'wqcxMjM0NTY3ODkwLT3CsSFAwqMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31hc2Rm'
        assert line1 == 'Z2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4/A//wDw=='
    }

    // TODO - reinstate after platform issues fixed
    void _testNonChunked() {
        def encodedBytes = testBytes.encodeBase64().toString()
        assert encodedBytes == 'wqcxMjM0NTY3ODkwLT3CsSFAwqMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31hc2RmZ2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4/A//wDw=='
    }
}
