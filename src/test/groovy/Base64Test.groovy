package groovy

class Base64Test extends GroovyTestCase {
    String testString = '\u00A71234567890-=\u00B1!@\u00A3\$%^&*()_+qwertyuiop[]QWERTYUIOP{}asdfghjkl;\'\\ASDFGHJKL:"|`zxcvbnm,./~ZXCVBNM<>?\u0003\u00ff\u00f0\u000f'
    byte[] testBytes = testString.getBytes("ISO-8859-1")

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
        assert decodedString.equals(specialString)
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
    }

    void testNonChunked() {
        def encodedBytes = testBytes.encodeBase64().toString()
        assert encodedBytes == 'pzEyMzQ1Njc4OTAtPbEhQKMkJV4mKigpXytxd2VydHl1aW9wW11RV0VSVFlVSU9Qe31hc2RmZ2hqa2w7J1xBU0RGR0hKS0w6InxgenhjdmJubSwuL35aWENWQk5NPD4/A//wDw=='
    }
}
