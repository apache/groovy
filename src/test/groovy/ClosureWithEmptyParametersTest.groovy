/** 
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
class ClosureWithEmptyParametersTest extends GroovyTestCase {

    void testNoParams() {

        block = {-> println "hey I'm a closure!" }

        println "About to call closure"

        block.call()

        println "Done"
    }

}

