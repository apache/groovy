

/**
 * Simple test of tree walking
 */
class NavigationTest extends GroovyTestCase {
    
    void testDepthFirst() {
        tree = createTree()
        
        System.out.println(tree.depthFirst())
        
        /** @todo
        names = tree.depthFirst().collect { n | print(n); return n.name }
        
        assert names == ['b1', 'b2', 'c1', 'c2', 'b3', 'b4', 'c3', 'c4', 'b5']
        */
    }
    
    void testBredthFirst() {
        tree = createTree()
        
        System.out.println(tree.bredthFirst())
        
        /** @todo
        names = tree.bredthFirst().collect { n | print(n); return n.name }
        
        assert names == ['b1', 'b2', 'b3', 'b4', 'b5', 'c1', 'c2', 'c3', 'c4']
        */
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
