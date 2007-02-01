package groovy

/**
 * @version $Revision: 1.4 $
 */
class OverloadInvokeMethodTest extends GroovyTestCase {
    
    void testBug() {
        def value = foo(123)
        assert value == 246
    }

    /**
     * Lets overload the invokeMethod() mechanism to provide an alias
     * to an existing method
     */
    def invokeMethod(String name, Object args) {
        try {
            return metaClass.invokeMethod(this, name, args)
        }
        catch (MissingMethodException e) {
            if (name == 'foo') {
                return metaClass.invokeMethod(this, 'bar', args)
            }
            else {
                throw e
            }
        }
    }
    
    def bar(param) {
        return param * 2
    }

}