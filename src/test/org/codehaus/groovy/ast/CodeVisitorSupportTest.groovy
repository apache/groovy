package org.codehaus.groovy.ast;


import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilePhase


/**
 * Tests the CodeVisitorSupport.
 *
 * @author Hamlet D'Arcy
 */

public class CodeVisitorSupportTest extends GroovyTestCase {

    public void testIfElse() {
        def ast = new AstBuilder().buildFromCode { if (true) { 1 } else  { 2 } }
        def visitor = new RecordingCodeVisitorSupport()
        visitor.visitBlockStatement(ast[0]) // first element is always BlockStatement

        assert visitor.history[0] == BlockStatement
        assert visitor.history[1] == IfStatement
        assert visitor.history[2] == BooleanExpression
        assert visitor.history[3] == BlockStatement
        assert visitor.history[4] == BlockStatement
        assert visitor.history.size == 5
    }

    public void testEmptyStatementsOnIfElse() {
        def ast = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS, true, {
            if (true) { 1 }
        })
        def visitor = new RecordingCodeVisitorSupport()
        visitor.visitBlockStatement(ast[0]) // first element is always BlockStatement

        assert visitor.history[0] == BlockStatement
        assert visitor.history[1] == IfStatement
        assert visitor.history[2] == BooleanExpression
        assert visitor.history[3] == BlockStatement
        assert visitor.history[4] == EmptyStatement
        assert visitor.history.size == 5
    }

    public void testTryCatchFinally() {
        def ast = new AstBuilder().buildFromCode {
            def x
            try {
                x = 1
            } catch (IOException ei) {
                x = 2
            } finally {
                x = 4
            }
        }
        def visitor = new RecordingCodeVisitorSupport()
        visitor.visitBlockStatement(ast[0]) // first element is always BlockStatement

        assert visitor.history[0] == BlockStatement
        assert visitor.history[1] == TryCatchStatement
        assert visitor.history[2] == BlockStatement
        assert visitor.history[3] == CatchStatement
        assert visitor.history[4] == BlockStatement
    }

    public void testEmptyStatementsOnTryCatch() {
        def ast = new AstBuilder().buildFromCode {
            def x
            try {
                x = 1
            } catch (IOException ei) {
                x = 2
            }
        }
        def visitor = new RecordingCodeVisitorSupport()
        visitor.visitBlockStatement(ast[0]) // first element is always BlockStatement

        assert visitor.history[0] == BlockStatement
        assert visitor.history[1] == TryCatchStatement
        assert visitor.history[2] == BlockStatement
        assert visitor.history[3] == CatchStatement
        assert visitor.history[4] == BlockStatement
        assert visitor.history[5] == EmptyStatement
    }
}

/**
* Records the visit method that were called so that they can be queried and verified later.
* This would be better implemented using invokeMethod but it is called from Java so it
* won't dispatch correctly. 
*
* @author Hamlet D'Arcy
*/
@groovy.transform.PackageScope class RecordingCodeVisitorSupport extends CodeVisitorSupport implements GroovyInterceptable {
    def history = []

    public void visitBlockStatement(BlockStatement node) {
        history << node.getClass()
        super.visitBlockStatement(node)
    }

    public void visitIfElse(IfStatement node) {
        history << node.getClass()
        super.visitIfElse(node)
    }

    public void visitBooleanExpression(BooleanExpression node) {
        history << node.getClass()
        super.visitBooleanExpression(node)
    }

    protected void visitEmptyStatement(EmptyStatement node) {
        history << node.getClass()
        super.visitEmptyStatement(node)
    }

    public void visitTryCatchFinally(TryCatchStatement node) {
        history << node.getClass()
        super.visitTryCatchFinally(node);
    }

    public void visitCatchStatement(CatchStatement node) {
        history << node.getClass()
        super.visitCatchStatement(node);    
    }

}

