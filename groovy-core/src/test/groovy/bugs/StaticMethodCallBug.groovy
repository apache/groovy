package groovy.bugs

/** 
 * @version $Revision$
 */
class StaticMethodCallBug extends GroovyTestCase {

    void testBug() {
        def value = TestSupport.mockStaticMethod()
        assert value == "cheese"
    }
    
    void testStaticProperty() {
        def value = TestSupport.mockStaticProperty
        assert value == "cheese"
    }
}
