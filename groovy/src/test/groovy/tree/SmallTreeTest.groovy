package groovy.tree

class SmallTreeTest extends GroovyTestCase {
    def b
    def EXPECTED = 'root1[attributes={}; value=[elem1[attributes={}; value=hello1]]]'

    void testTree() {
        b = NodeBuilder.newInstance()
        
        def root = b.root1( {
            elem1('hello1')
        })
        
        assert root != null
        
        assert EXPECTED == root.toString()
    }
}