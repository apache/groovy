package org.codehaus.groovy.transform.vm5
/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 23, 2008
 * Time: 7:49:09 PM
 * To change this template use File | Settings | File Templates.
 */
class GlobalTransformTest extends GroovyTestCase {

      URL transformRoot = new File(getClass().classLoader.
          getResource("org/codehaus/groovy/transform/META-INF/services/org.codehaus.groovy.transform.ASTTransformation").
          toURI()).parentFile.parentFile.parentFile.toURL()

    void testGlobalTransform() {
        GroovyShell shell = new GroovyShell()
        shell.classLoader.addURL(transformRoot)
        shell.evaluate("""
            import org.codehaus.groovy.control.CompilePhase

            if (org.codehaus.groovy.transform.TestTransform.phases == [CompilePhase.CONVERSION, CompilePhase.CLASS_GENERATION]) {
               println "Phase sync bug fixed"
            } else if (org.codehaus.groovy.transform.TestTransform.phases == [CompilePhase.CONVERSION, CompilePhase.INSTRUCTION_SELECTION]) {
               println "Phase sync bug still present"
            } else {
               assert false, "FAIL"
            }
        """)
    }

}