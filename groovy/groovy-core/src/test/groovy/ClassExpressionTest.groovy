/** 
 * Tests the use of classes as variable expressions
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClassExpressionTest extends GroovyTestCase {

    void testUseOfClass() {
        def x = String
        
        System.out.println("x: " + x)
        
        assert x != null

        assert x.getName().endsWith('String')
        assert x.name.endsWith('String')

        x = Integer
        
        assert x != null
        assert x.name.endsWith('Integer')
        
        x = GroovyTestCase
        
        assert x != null
        assert x.name.endsWith('GroovyTestCase')
        
        x = ClassExpressionTest
        
        assert x != null

        System.out.println("x: " + x)
    }

    void testClassPsuedoProperty() {

        def x = "cheese";

        assert x.class != null

        assert x.class == x.getClass();

        System.err.println( "x.class: " + x.class );
    }
}
