package groovy.xml

/**
 * This test uses the concise syntax to test the generation
 * of SAX events using GroovyMarkup
 */
class SAXTest extends TestXmlSupport {
    
    void testSmallTree() {
        def b = createSAXBuilder()
        
        def root = b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
    }
    
    void testTree() {
        def b = createSAXBuilder()
        
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
    }
}