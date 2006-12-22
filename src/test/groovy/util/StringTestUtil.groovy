package groovy.util

import junit.framework.Assert

class StringTestUtil {
    static void assertMultilineStringsEqual(String a, String b) {
        def aLines = a.trim().replaceAll('\r','').split('\n')
        def bLines = b.trim().replaceAll('\r','').split('\n')
        assert aLines.size() == bLines.size()
        for (i in 0..<aLines.size()) {
            Assert.assertEquals(aLines[i].trim(), bLines[i].trim())
        }
    }
}