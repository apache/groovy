package groovy.tree;

class ClosureClassLoaderBug extends GroovyTestCase {
    
    property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( { i |
            b.elem1('hello1')
        })
        
        root.print();
    }
}