package groovy.xml

/**
 * This test uses the concise syntax to test the building of 
 * W3C DOM trees using GroovyMarkup
 */
class DOMTest extends TestXmlSupport {
    
    void testSmallTree() {
        def b = DOMBuilder.newInstance()
        
        def root = b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
        
        assert root != null
        
        dump(root)
    }
    
    void testTree() {
        def b = DOMBuilder.newInstance()
        
        def root = b.root2(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            nestedElem(x:'abc', y:'def') {
                child(z:'def')
                child2()  
            }
            
            nestedElem2(z:'zzz') {
                child(z:'def')
                child2("hello")  
            }
        }
        
        assert root != null
        
        dump(root)

/*
        def elem1 = root.elem1
        assert elem1.value() := 'hello1'
        
        def elem2 = root.elem2
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
        
        def list = root.value()
        assert list.size() := 4
        
        assert root.attributes().a := 5
        assert root.attributes().b := 7

        assert root.nestedElem.attributes().x := 'abc'
        assert root.nestedElem.attributes().y := 'def'
        assert root.nestedElem2.attributes().z := 'zzz'
        assert root.nestedElem2.child.attributes().z := 'def'
*/        
        /** @todo parser add .@ as an operation
                assert root.@a := 5
                assert root.@b := 7
        */        
    }
    
}