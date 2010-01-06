package examples.local

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.builder.AstBuilder

/**
 * If there is a method in a class with the @Main annotation on it, then this 
 * transformation adds a real main(String[]) method to the class with the same
 * method body as the annotated class. 
 *
 * @author Hamlet D'Arcy
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class MainTransformation implements ASTTransformation {

    // normally defined in org.objectweb.asm.Opcodes, but there duplicated
    // here to make the build script simpler. 
    static int PUBLIC = 1
    static int STATIC = 8
    
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

        // use guard clauses as a form of defensive programming. 
        if (!astNodes) return 
        if (!astNodes[0]) return 
        if (!astNodes[1]) return 
        if (!(astNodes[0] instanceof AnnotationNode)) return
        if (astNodes[0].classNode?.name != Main.class.getName()) return
        if (!(astNodes[1] instanceof MethodNode)) return 

        MethodNode annotatedMethod = astNodes[1]
        ClassNode declaringClass = astNodes[1].declaringClass
        MethodNode mainMethod = makeMainMethod(annotatedMethod)
        declaringClass.addMethod(mainMethod)
    }

    /**
    * Uses the AstBuilder to synthesize a main method, and then sets the body of
    * the method to that of the source method. Notice how Void.TYPE is used as
    * a return value instead of Void.class. This is required so that resulting method
    * is void and not Void. 
    */ 
    MethodNode makeMainMethod(MethodNode source) {
        def ast = new AstBuilder().buildFromSpec {
            method('main', PUBLIC | STATIC, Void.TYPE) {
                parameters {
                    parameter 'args': String[].class
                }
                exceptions {}
                block { }
            }
        }
        MethodNode target = ast[0]
        target.code = source.code
        target
    }
}
