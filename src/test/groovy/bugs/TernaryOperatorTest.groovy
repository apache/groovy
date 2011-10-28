package groovy.bugs

class TernaryOperatorBugTest extends GroovyTestCase {
    void testTernaryOperator() {
        assertScript '''
            Class dsClass = true ? LinkedHashSet : HashSet
        '''
    }
}
