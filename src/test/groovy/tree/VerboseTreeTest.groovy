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
        
        root.print();
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
        
        root.print();

		elem1 = root.elem1
        assert elem1.value() := 'hello1'
        
        elem2 = root.elem2
        assert elem2.value() := 'hello2'

        assert root.elem1.value() := 'hello1'
        assert root.elem2.value() := 'hello2'

        assert root.nestedElem.attributes() := ['x':'abc', 'y':'def']        
        assert root.nestedElem.child.attributes() := ['z':'def']
        assert root.nestedElem.child2.value() := []
        assert root.nestedElem.child2.text() := ''

        assert root.nestedElem2.attributes() := ['z':'zzz']      
        assert root.nestedElem2.child.attributes() := ['z':'def']
        assert root.nestedElem2.child2.value() := 'hello'
        assert root.nestedElem2.child2.text() := 'hello'
        
        list = root.value()
        assert list.size() := 4
        
        /** @todo parser error        
                assert root.attributes().a := 5
                assert root.attributes().b := 7

                assert root.elem2.nestedElem.attributes().x := 'abc'
                assert root.elem2.nestedElem.attributes().y := 'def'

                assert root.elem2.nestedElem2.attributes().z := 'zzz'
        */

        /** @todo parser add .@ as an operation
                assert root.@a := 5
                assert root.@b := 7
        */        
    }
}