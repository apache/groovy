

class ClosureClassLoaderBug extends GroovyTestCase {
    
    def b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        def root = b.root1( {
            b.elem1('hello1')
        })
        
        print(root)
    }
}