

/**
 * Test case for a bug with nested closures
 */
class NestedClosureBugTest extends GroovyTestCase {
    
    property b

    void testNestedClosureBug() {
        b = NodeBuilder.newInstance()
        
        root = b.root({|
            b.child({|
                b.grandChild()  
            })
        })

		root.println()
    }
}