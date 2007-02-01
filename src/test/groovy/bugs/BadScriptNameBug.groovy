package groovy.bugs

/**
 * @author Sergey Udovenko 
 * @version $Revision: 1.3 $
 */
class BadScriptNameBug extends GroovyTestCase {
    
    void testBug() {
		GroovyClassLoader cl = new GroovyClassLoader(); 
		cl.parseClass("println 'oops!'", "/script.groovy");
    }
}