

/**
 * Test case for a bug with nested closures
 */
class NestedClosureBugTest extends GroovyTestCase {
    
    @Property b

    void testNestedClosureBug() {
        b = NodeBuilder.newInstance()
        
        root = b.root(['a':'xyz'], {
            b.child({
                b.grandChild()  
            })
        })

		println(root)
    }
}