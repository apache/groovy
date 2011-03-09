package examples.astbuilder

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.SourceUnit
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
        if (astNodes[0].classNode?.name != Main.class.name) return
        if (!(astNodes[1] instanceof MethodNode)) return 

        MethodNode annotatedMethod = astNodes[1]
        ClassNode declaringClass = annotatedMethod.declaringClass
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
        def className = source.declaringClass.name
        def methodName = source.name

        def ast = new AstBuilder().buildFromString(CompilePhase.INSTRUCTION_SELECTION, false, """
            package $source.declaringClass.packageName
            
            class $source.declaringClass.nameWithoutPackage {
                public static void main(String[] args) {
                    new $className().$methodName()
                }
            }
        """)
        ast[1].methods.find { it.name == 'main' }
    }
}
