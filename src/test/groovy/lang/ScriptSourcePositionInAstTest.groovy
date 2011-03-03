package groovy.lang

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases

/**
 * Check that scripts have proper source position in the AST
 *
 * @author Guillaume Laforge
 */
class ScriptSourcePositionInAstTest extends GroovyTestCase {

    private positionsForScript(String text) {
        CompilationUnit cu = new CompilationUnit()
        cu.addSource("scriptSourcePosition.groovy", text)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        
        def node = cu.getAST().getClass("scriptSourcePosition")

        [[node.getLineNumber(), node.getColumnNumber()], [node.getLastLineNumber(), node.getLastColumnNumber()]]
    }

    void testEmptyScript() {
        assert positionsForScript("") == [[-1, -1], [-1, -1]]
    }

    void testSingleStatementScript() {
        assert positionsForScript("println 'hello'") == [[1, 1], [1, 16]]
    }

    void testDoubleStatementScript() {
        assert positionsForScript("""\
            println 'hello'
            println 'bye'
        """.stripIndent()) == [[1, 1], [2, 14]]
    }

    void testScriptWithClasses() {
        assert positionsForScript("""\
            class Bar {}
            println 'hello'
            println 'bye'
            class Baz{}
        """.stripIndent()) == [[2, 1], [3, 14]]
    }
}
