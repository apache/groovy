package groovy.bugs

class Groovy3498Bug extends GroovyTestCase {
    void testClosureExpressionFiltering() {
        new GroovyShell().evaluate """
        { -> assert false, 'This statement should not have been executed' }
        println 'Ok'
        """
    }
}
