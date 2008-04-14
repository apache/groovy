package groovy.bugs

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * @author Jason Thomas
 * @version $Revision$
 */
class ConstructorBug extends GroovyTestCase {
    
    void testBug() {
        def type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestBase.groovy"))
        assert type != null

        println "created type: ${type}"
        
        type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestDerived.groovy"))
        assert type != null

        println "created type: ${type} of type: ${type.class}"

        def mytest = InvokerHelper.invokeConstructorOf(type, ["Hello"] as Object[])
        assert mytest.foo == "Hello"
        /** @todo fix bug
        */
        
        /*
        def test = type.newInstance()
        assert test.foo == null
        */
        
//foo = new type('hello')
        /*
        */
        mytest = new TestDerived("Hello")
        assert mytest.foo == "Hello"
    }
}