/** 
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
class ClosureDefaultParameterTest extends GroovyTestCase {

    void testClosureWithDefaultParams() {

        block = {a = 123, b = 456 :: println "value of a = $a and b = $b" }
        
        block = {Integer a = 123, String b = "abc" :: println "value of a = $a and b = $b" }


        // TODO AST bugs...

        //block.call(456, "def")
        //block.call()
        //block(456)
        //block(456, "def")
    }

}

