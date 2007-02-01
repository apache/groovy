package groovy

/**
 * test to ensure that overriding getter doesn't throw a NPE on access
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class OverridePropertyGetterTest extends GroovyTestCase { 
    def cheese
       
    void testSimpleMethodParameterAccess() { 
        def o = new OverridePropertyGetterTest()
        def p = new OverridePropertyGetterTest()
        try {          
            //@todo
            //p.cheese = o.cheese
        } catch (Exception e) {
            fail(e.getMessage())
        }
    }
    
    public String getCheese() {
        return cheese
    }
} 
