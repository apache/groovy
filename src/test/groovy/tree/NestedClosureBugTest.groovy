package groovy.tree

/**
 * Test case for a bug with nested closures
 */
class NestedClosureBugTest extends GroovyTestCase {
    def b
    def EXPECTED = 'root[attributes={a=xyz}; value=[child[attributes={}; value=[grandChild[attributes={}; value=[]]]]]]'

    void testNestedClosureBug() {
        b = NodeBuilder.newInstance()
        
        def root = b.root(['a':'xyz'], {
            b.child({
                b.grandChild()  
            })
        })

        assert EXPECTED == root.toString()
    }
}