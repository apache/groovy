package groovy.tree;

class SmallVerboseTreeTest extends GroovyTestCase {
    
    property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( { i |
            b.elem1('hello1')
        })
        
        //assert root != null
        
        root.print();
    }
}