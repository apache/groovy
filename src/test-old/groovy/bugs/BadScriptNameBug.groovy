/**
 * @author Sergey Udovenko 
 * @version $Revision$
 */
class BadScriptNameBug extends GroovyTestCase {
    
    void testBug() {
		GroovyClassLoader cl = new GroovyClassLoader(); 
		cl.parseClass("println 'oops!'", "/script.groovy");
    }
}