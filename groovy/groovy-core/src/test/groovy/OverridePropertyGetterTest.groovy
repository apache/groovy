/**
 * test to ensure that overriding getter doesn't throw a NPE on access
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class OverridePropertyGetterTest extends GroovyTestCase { 
    property cheese
       
    void testSimpleMethodParameterAccess() { 
        o = new OverridePropertyGetterTest()
        p = new OverridePropertyGetterTest()
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
