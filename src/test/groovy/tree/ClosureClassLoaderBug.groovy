package groovy.tree

class ClosureClassLoaderBug extends GroovyTestCase {
    def b
    def EXPECTED = 'root1[attributes={}; value=[elem1[attributes={}; value=hello1]]]'

    void testTree() {
        b = NodeBuilder.newInstance()
        
        def root = b.root1( {
            b.elem1('hello1')
        })
        
        assert EXPECTED == root.toString()
    }
}