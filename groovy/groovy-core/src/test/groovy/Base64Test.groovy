class Base64Test extends GroovyTestCase {
    void testCodec() {
    		testString ="§1234567890-=±!@£$%^&*()_+qwertyuiop[]QWERTYUIOP{}asdfghjkl;'\\ASDFGHJKL:\"|`zxcvbnm,./~ZXCVBNM<>?\u0000\u00ff\u00f0\u000f"
    		
    		testBytes = testString.getBytes("ISO-8859-1")
    		
    		encodedBtyes = testBytes.encodeBase64().toString()
    		
    		decodedBytes = encodedBtyes.decodeBase64()
    		
    		decodedString = new String(decodedBytes, "ISO-8859-1")
    		
    		assert decodedString.equals(testString)
    }
}
