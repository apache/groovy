package groovy.bugs

import groovy.xml.MarkupBuilder

/**
 * @author Merrick Schincariol 
 * @version $Revision$
 */
class Groovy249_Bug extends GroovyTestCase {

    void testBug() {
		def t = new Bean249()
		t.b = "hello"
		println t.b
		println "test: ${t.b}"
		
		def xml = new MarkupBuilder()
		def root = xml.foo {
			bar {
				// works
				baz("test")
				// fails
				baz(t.b)
				// fails
				baz("${t.b}")
			}
		} 
	}
	
/** @todo don't know why this fails

    void testBugInScript() {
    	assertScript <<<EOF
			import groovy.xml.MarkupBuilder;
			
			class Bean {
				String b
			};
			
			def t = new Bean()
			t.b = "hello"
			println t.b
			println "test: ${t.b}"
			
			def xml = new MarkupBuilder()
			root = xml.foo {
				bar {
					// works
					baz("test")
					// fails
					baz(t.b)
					// fails
					baz("${t.b}")
				}
			} 

EOF    	
	}
*/
   
}

class Bean249 {
	String b
}
