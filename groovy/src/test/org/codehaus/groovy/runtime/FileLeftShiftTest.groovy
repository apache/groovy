package org.codehaus.groovy.runtime

class FileLeftShiftTest extends GroovyTestCase {
    void testFileLeftShift() {
        new File("target/test-classes/MyFileLeftShiftTest.txt").delete()
        new File("target/test-classes/MyFileLeftShiftTest.txt") << "This is " << "groovy"
        assertEquals(new File("target/test-classes/MyFileLeftShiftTest.txt").text, "This is groovy")
        new File("target/test-classes/MyFileLeftShiftTest.txt").delete()
    }
}
