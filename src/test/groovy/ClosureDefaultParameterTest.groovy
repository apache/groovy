package groovy

/** 
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
class ClosureDefaultParameterTest extends GroovyTestCase {

    void testClosureWithDefaultParams() {

        def block = {a = 123, b = 456 -> println "value of a = $a and b = $b" }

        block = { Integer a = 123, String b = "abc" ->
                  println "value of a = $a and b = $b"; return "$a $b".toString() }

        assert block.call(456, "def") == "456 def"
        assert block.call() == "123 abc"
        assert block(456) == "456 abc"
        assert block(456, "def") == "456 def"
    }
    
    void testClosureWithDefaultParamFromOuterScope() {
        def y = 555
        def boo = {x = y -> x}
        assert boo() == y
        assert boo(1) == 1
    }

}

