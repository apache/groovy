package transforms.local

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.stmt.Statement

/**
* This transformation finds all the methods defined in a script that have
* the @WithLogging annotation on them, and then weaves in a start and stop 
* message that is logged using println. 
*
* @author Hamlet D'Arcy
*/ 
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
public class LoggingASTTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        List methods = sourceUnit.getAST()?.getMethods()
        // find all methods annotated with @WithLogging
        methods.findAll { MethodNode method ->
            method.getAnnotations(new ClassNode(WithLogging))
        }.each { MethodNode method ->
            Statement startMessage = createPrintlnAst("Starting $method.name")
            Statement endMessage = createPrintlnAst("Ending $method.name")

            List existingStatements = method.getCode().getStatements()
            existingStatements.add(0, startMessage)
            existingStatements.add(endMessage)
        }
    }

    /**
    * This creates the ASTNode for a println statement. 
    */ 
    private Statement createPrintlnAst(String message) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                    new ConstantExpression(message)
                )
            )
        )
    }
}