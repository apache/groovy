package transforms.global

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.transform.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import java.lang.annotation.*
import org.codehaus.groovy.ast.builder.AstBuilder

/**
* This ASTTransformation adds a static getCompiledTime() : String method to every class.  
*
* @author Hamlet D'Arcy
*/ 
@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class CompiledAtASTTransformation implements ASTTransformation {

    private final static compileTime = new Date().toString()

    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

        List classes = sourceUnit.ast?.classes
        classes?.each { ClassNode clazz ->
            clazz.addMethod(makeMethod())
        }
    }

    /**
    *  OpCodes should normally be referenced, but in a standalone example I don't want to have to include
    * the jar at compile time. 
    */ 
    MethodNode makeMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('getCompiledTime', /*OpCodes.ACC_PUBLIC*/1 | /*OpCodes.ACC_STATIC*/8, String) {
                parameters {}
                exceptions {}
                block { 
                    returnStatement {
                        constant(compileTime) 
                    }
                }
                annotations {}
            }
        }
        ast[0]
    }
}
