package groovy.bugs

import groovy.xml.MarkupBuilder

class StaticMarkupBug extends GroovyTestCase {

    void testBug() {
        doMarkup(new MarkupBuilder())
    }
    
    static void doMarkup(builder) {
        builder.html {
            head {
                title("Groovy")
            }
        }
    }
}