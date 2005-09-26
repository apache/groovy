package org.codehaus.groovy.sandbox.markup

/**
 * This test uses the concise syntax to test the building of 
 * textual markup (XML or HTML) using GroovyMarkup
 */
class StreamingMarkupTest extends groovy.xml.TestXmlSupport {
    
    void testSmallTree() {
        def b = new StreamingMarkupBuilder()
        
        def m = {
            delegate.mkp.pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
            root1(a:5, b:7) {
                elem1('hello1')
                elem2('hello2')
                elem3(x:7)
            }
        }

        System.out << b.bind(m)
    }
    
    void testTree() {
        def b = new StreamingMarkupBuilder()
        
        def m = {
            root2(a:5, b:7) {
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

        System.out << b.bind(m)
    }
}