package groovy.tree;

/**
 * This test uses the verbose syntax to test the building of trees
 * using GroovySyntax
 */
class VerboseTreeTest extends GroovyTestCase {
    
    property b

    void testTree() {
        b = NodeBuilder.newInstance()
        
        root = b.root1(['a':5, 'b':7], { i |
            b.elem1('hello1')
            b.elem2('hello2')
            b.elem3(['x':7])
        })
        
        assert root != null
        
        System.out.println(root)
        System.out.println(root.attributes)
    }
    
    void testTree2() {
        b = NodeBuilder.newInstance()
        
        root = b.root2(['a':5, 'b':7], { i |
            b.elem1('hello1')
            b.elem2('hello2')
            b.nestedElem(['x':'abc', 'y':'def'], { i |
                b.child(['z':'def'])
                b.child2()  
            })
            
            b.nestedElem2(['z':'zzz'], { i |
                b.child(['z':'def'])
                b.child2("hello")  
            })
        })
        
        assert root != null
        
        System.out.println(root)
        //System.out.println(root.attributes)
    }
}