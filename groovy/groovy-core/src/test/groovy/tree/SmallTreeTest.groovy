package groovy.tree;

class SmallTreeTest extends GroovyTestCase {
    
    property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( {|
            elem1('hello1')
        })
        
        //assert root != null
        
        root.print();
    }
}