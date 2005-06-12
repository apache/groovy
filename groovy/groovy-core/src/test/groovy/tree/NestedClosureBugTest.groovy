

/**
 * Test case for a bug with nested closures
 */
class NestedClosureBugTest extends GroovyTestCase {
    
    def b

    void testNestedClosureBug() {
        b = NodeBuilder.newInstance()
        
        def root = b.root(['a':'xyz'], {
            b.child({
                b.grandChild()  
            })
        })

        println(root)
    }
}