

class ClosureClassLoaderBug extends GroovyTestCase {
    
    @Property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1( {
            b.elem1('hello1')
        })
        
        print(root)
    }
}