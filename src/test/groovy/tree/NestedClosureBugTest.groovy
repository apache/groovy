package groovy.tree;

/**
 * Test case for a bug with nested closures
 */
class NestedClosureBugTest extends GroovyTestCase {
    
    property b

    void testNestedClosureBug() {
        b = NodeBuilder.newInstance()
        
        root = b.root({ i |
            b.child({ i |
                b.grandChild()  
            })
        })

        System.out.println(root)
    }
}