package groovy.inspect.swingui

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.Phases
import javax.swing.tree.TreeNode
import java.util.Map.Entry


/**
 * Unit test for ScriptToTreeNodeAdapter.
 *
 * @author Hamlet D'Arcy
 */

public class ScriptToTreeNodeAdapterTest extends GroovyTestCase {

    ScriptToTreeNodeAdapter adapter

    protected void setUp() {
        adapter = new ScriptToTreeNodeAdapter()
    }

    public void testCompile_HelloWorld() {

        def script = "\"Hello World\""
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        printnode(root)
        def result = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'ExpressionStatement'
        }?.children()?.find {
            it.toString() == 'Constant - Hello World : java.lang.String'
        }
        assertNotNull('Could not locate ConstantExpression in AST', result)
    }

    public void testCompile_SimpleClass() {
        def script = " class Foo { } "
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        printnode(root)
        def result = root.children()?.find {
            it.toString() == 'ClassNode - Foo'
        }?.children()?.find {
            it.toString() == 'Fields'
        }?.children()?.find {
            it.toString() == 'FieldNode - $ownClass : java.lang.Class'
        }?.children()?.find {
            it.toString() == 'Class - Foo'
        }?.children()?.find {
            it.toString() == 'Class - Foo'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }

    /**
     * Helper method to print out the TreeNode to a test form in systme out.
     * Warning, this uses recursion. 
     */
    def printnode(TreeNode node, String prefix = "") {
        println prefix + node
        node.children().each {
            printnode(it, prefix + "  ")
        }
    }
}