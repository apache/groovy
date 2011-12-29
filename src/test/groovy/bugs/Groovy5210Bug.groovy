package groovy.bugs

class Groovy5210Bug extends GroovyTestCase {
    void testAssignPrimitiveArrayToSet() {
        assertScript '''
            int[] array = [1,2,3] as int[]
            Set set = array
        '''
    }

    void testConvertPrimitiveArrayToSet() {
        assertScript '''
            int[] array = [1,2,3] as int[]
            def set = array as Set
        '''
    }
}
