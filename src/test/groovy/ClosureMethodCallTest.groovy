package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodCallTest extends GroovyTestCase {

    void testCallingClosureWithMultipleArguments() {
        def foo
        def closure = { a, b -> foo = "hello ${a} and ${b}".toString() }
        
        closure("james", "bob")

        assert foo == "hello james and bob"

        closure.call("sam", "james")

        assert foo == "hello sam and james"
    }
    
    void testClosureCallMethodWithObjectArray() {
        // GROOVY-2266
        def args = [1] as Object[]
        def closure = {x -> x[0]}
        assert closure.call(args) == 1
    }
    
    void testClosureWithStringArrayCastet() {
        def doSomething={ list ->  list }

        String[] x=["hello", "world"]
        String[] y=["hello", "world"]

        assert doSomething(x as String[]) == x
        assert doSomething( y ) == y
    }
    
    void testClosureAsLocalVar() {
        def local = { Map params -> params.x * params.y  }
        assert local(x : 2, y : 3) == 6
    }
    
    void testClosureDirectly() {
        assert { Map params -> params.x * params.y }(x : 2, y : 3) == 6
    }
    
    def attribute
    
    void testClosureAsAttribute() {
        attribute = { Map params ->  params.x * params.y  } 
        assert attribute(x : 2, y : 3) == 6
    }
    
    void testSystemOutPrintlnAsAClosure() {
        def closure = System.out.&println
        closure("Hello world")
    }
}
