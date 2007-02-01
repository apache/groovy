package groovy

/**
 * test to ensure that private instance variables are visible to 
 * other instance variables of the same class
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class PrivateVariableAccessFromAnotherInstanceTest extends GroovyTestCase implements Cloneable { 
    def foo
    private def bar
              
    public PrivateVariableAccessFromAnotherInstanceTest() {
        super()
        foo = "foo"
        bar = "bar"
    }
              
    public Object clone() {
        def result = new PrivateVariableAccessFromAnotherInstanceTest()
        result.foo = foo
        result.bar = bar
        return result
    }
    
    void testClone() {
        def fred = new PrivateVariableAccessFromAnotherInstanceTest()
        //@todo fails due to private access to 'bar'
        //barney = fred.clone()

        // TODO identity comparison
        //assert !(barney === fred)
    }
} 
