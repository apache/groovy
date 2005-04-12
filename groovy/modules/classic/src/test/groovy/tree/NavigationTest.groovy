

/**
 * Simple test of tree walking
 */
class NavigationTest extends GroovyTestCase {
    
    void testDepthFirst() {
        tree = createTree()
        
        names = tree.depthFirst().collect { it.name() }
        expected = ['a', 'b1', 'b2', 'c1', 'c2', 'b3', 'b4', 'c3', 'c4', 'b5']
        
        assert names == expected
    }
    
    void testBredthFirst() {
        tree = createTree()
        
        names = tree.breadthFirst().collect { it.name() }
        expected = ['a', 'b1', 'b2', 'b3', 'b4', 'b5', 'c1', 'c2', 'c3', 'c4']
        
        assert names == expected
    }
    
    protected createTree() {       
        b = NodeBuilder.newInstance()
        
        root = b.a(a:5, b:7) {
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
