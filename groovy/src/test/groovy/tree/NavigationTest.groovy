package groovy.tree

/**
 * Simple test of tree walking
 */
class NavigationTest extends GroovyTestCase {
    
    void testDepthFirst() {
        def tree = createTree()
        
        def names = tree.depthFirst().collect { it.name() }
        def expected = ['a', 'b1', 'b2', 'c1', 'c2', 'b3', 'b4', 'c3', 'c4', 'b5']
        
        assert names == expected
    }
    
    void testBreadthFirst() {
        def tree = createTree()
        
        def names = tree.breadthFirst().collect { it.name() }
        def expected = ['a', 'b1', 'b2', 'b3', 'b4', 'b5', 'c1', 'c2', 'c3', 'c4']
        
        assert names == expected
    }
    
    protected def createTree() {       
        def b = NodeBuilder.newInstance()
        
        def root = b.a(a:5, b:7) {
            b1()
            b2 {
                c1()
                c2()
            }
            b3()
            b4 {
                c3()
                c4()
            }
            b5()
        }
        
        assert root != null
        
        println(root)
        
        return root
    }
}
