import transforms.global.CompiledAtASTTransformation 
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.transform.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.ast.*

/**
* This shows how to use the TransformTestHelper to test
* a global transformation. It is a little hard to invoke 
* because the CompiledAtASTTransformation must be on the 
* classpath but the JAR containing the transform must not
* or the transform gets applied twice. 
*
* @author Hamlet D'Arcy
*/ 
def transform = new CompiledAtASTTransformation()
def phase = CompilePhase.CONVERSION
def helper = new TransformTestHelper(transform, phase)
def clazz = helper.parse(' class MyClass {} ' )
assert clazz.getCompiledTime() != null
println 'compiled at' + clazz.getCompiledTime()
