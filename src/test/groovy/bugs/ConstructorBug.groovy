import org.codehaus.groovy.runtime.InvokerHelper
import java.io.File

/**
 * @author Jason Thomas
 * @version $Revision$
 */
class ConstructorBug extends GroovyTestCase {
    
    void testBug() {
        type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestBase.groovy"))
        assert type != null

        println "created type: ${type}"
        
        type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestDerived.groovy"))
        assert type != null

        println "created type: ${type} of type: ${type.class}"

        test = InvokerHelper.invokeConstructorOf(type, new Object[] { "Hello" })
        assert test.foo == "Hello"
        /** @todo fix bug
        */
        
        /*
        test = type.newInstance()
        asert test.foo == null
        */
        
//foo = new type('hello')
        /*
        */
        test = new TestDerived("Hello")
        assert test.foo == "Hello"
    }
}