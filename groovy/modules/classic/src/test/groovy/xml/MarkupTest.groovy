package groovy.xml

/**
 * This test uses the concise syntax to test the building of 
 * textual markup (XML or HTML) using GroovyMarkup
 */
class MarkupTest extends TestXmlSupport {
    
    void testSmallTree() {
        b = new MarkupBuilder()
        
        b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
    }
    
    void testTree() {
        b = new MarkupBuilder()
        
        b.root2(a:5, b:7) {
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

    void testContentAndDataInMarkup() {
        b = new MarkupBuilder()

        b.a(href:"http://groovy.codehaus.org", "groovy")
    }
}