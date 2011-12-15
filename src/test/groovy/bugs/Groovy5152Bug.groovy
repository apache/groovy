package groovy.bugs

class Groovy5152Bug extends GroovyTestCase {
    void testShouldNotThrowClassFormatError() {
        assertScript '''
            class MyObject{}
            assert !("" instanceof MyObject[])
        '''
    }
}
