

class ClosureClassLoaderBug extends GroovyTestCase {
    
    property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( {
            b.elem1('hello1')
        })
        
        root.print()
    }
}