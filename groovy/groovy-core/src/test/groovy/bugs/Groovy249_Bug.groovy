import groovy.xml.MarkupBuilder

/**
 * @author Merrick Schincariol 
 * @version $Revision$
 */
class Groovy249_Bug extends GroovyTestCase {

    void testBug() {
		t = new Bean()
		t.b = "hello"
		println t.b
		println "test: ${t.b}"
		
		xml = new MarkupBuilder()
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
	}
	
/*
    void testBugInScript() {
    	assertScript <<<EOF
			import groovy.xml.MarkupBuilder;
			
			class Bean {
				String b
			};
			
			t = new Bean()
			t.b = "hello"
			println t.b
			println "test: ${t.b}"
			
			xml = new MarkupBuilder()
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
   
}

class Bean {
	String b
}
