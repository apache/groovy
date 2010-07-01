package groovy.bugs

import org.objectweb.asm.Opcodes

import org.codehaus.groovy.ast.ASTNode 
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode 
import org.codehaus.groovy.ast.MethodNode 
import org.codehaus.groovy.ast.Parameter 
import org.codehaus.groovy.ast.builder.AstBuilder 
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit 
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class MyConstantsASTTransformation4272 implements ASTTransformation, Opcodes {
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode classNode = nodes[1]

        classNode.addMethod(new MethodNode("willSucceed", ACC_PUBLIC, ClassHelper.boolean_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromCode { return new Integer("1") }[0]))
                
        classNode.addMethod(new MethodNode("willNotFail", ACC_PUBLIC, ClassHelper.int_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromCode { return 1 }[0]))

        classNode.addMethod(new MethodNode("willAlsoNotFail", ACC_PUBLIC, ClassHelper.boolean_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromString("return 1")[0]))
    }
}
