class Base64Test extends GroovyTestCase {

    void testCodec() {
   	    testString ="§1234567890-=±!@£\$%^&*()_+qwertyuiop[]QWERTYUIOP{}asdfghjkl;'\\ASDFGHJKL:\"|`zxcvbnm,./~ZXCVBNM<>?\u0000\u00ff\u00f0\u000f"

        // get a byte array using the least significant eigth bits of each caharacter
           testBytes = testString.getBytes("ISO-8859-1")

           // turn the bytes back into a string for later comparison
            testString = new String(testBytes, "ISO-8859-1")

            // encode the bytes as base64. This produces a Writable object convert it to a String
            encodedBytes = testBytes.encodeBase64().toString()

            // decode the base64 back to a byte array
            decodedBytes = encodedBytes.decodeBase64()

            // turn the byte array back to a String for caomparison
            decodedString = new String(decodedBytes, "ISO-8859-1")

            assert decodedString.equals(testString)
    }

}
