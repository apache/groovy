package examples.astbuilder

import org.codehaus.groovy.tools.ast.TransformTestHelper
import org.codehaus.groovy.control.CompilePhase

/**
 *
 * This TestCase shows how to invoke an AST Transformation from a unit test.
 * An IDE will let you step through the AST Transformation using this approach. 
 *
 * @author Hamlet D'Arcy
 */

class MainIntegrationTest extends GroovyTestCase {

     public void testInvokeUnitTest() {
        def invoker = new TransformTestHelper(new MainTransformation(), CompilePhase.CANONICALIZATION)

        def file = new File('./MainExample.groovy')
        assert file.exists()

        def clazz = invoker.parse(file)
        def tester = clazz.newInstance()
        tester.main(null)       // main method added with AST transform
    }
}
