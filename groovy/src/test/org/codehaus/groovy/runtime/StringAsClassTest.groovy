package org.codehaus.groovy.runtime

class StringAsClassTest extends GroovyTestCase{
    void testStringAsClass  () {
        assertEquals "java.util.ArrayList" as Class, ArrayList
    }

    void testStringBuffer () {
        assertEquals "${ArrayList.'package'.name}.ArrayList" as Class, ArrayList
    }

    void testFails () {
        shouldFail {
            assertEquals "NOSUCHCLASS" as Class, ArrayList
        }
    }
}