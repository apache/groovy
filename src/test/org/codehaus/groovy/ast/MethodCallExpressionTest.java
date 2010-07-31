package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;


/**
 * Tests the MethodCallExpression
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */

public class MethodCallExpressionTest extends ASTTest {
    
    private boolean isImplicitThis;
    
    /*
     * To make sure the MethodCallExpression is visited and we do not test against
     * the default value of isImplicitThis
     */
    private boolean visited; 
    
    private List<String> defaultScriptMethods = new ArrayList<String>();
    
    private ClassCodeVisitorSupport MethodCallVisitor = new ClassCodeVisitorSupport() {
        
        public void visitMethodCallExpression(MethodCallExpression methodCall) {
            if (defaultScriptMethods.contains(methodCall.getMethodAsString())) {
                visited = true;
                isImplicitThis = methodCall.isImplicitThis();
            }
        }
        
        protected SourceUnit getSourceUnit() {
            return null;
        }
    };
    
    public MethodCallExpressionTest() {
        defaultScriptMethods.add("substring");
        defaultScriptMethods.add("println");
    }
    
    protected void setUp() throws Exception {
        visited = false;
    }
    
    public void testIsImplicitThisOnObject() {
        ModuleNode root = getAST("string.substring(2)", Phases.SEMANTIC_ANALYSIS);
        MethodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertFalse(isImplicitThis);
    }
    
    public void testIsImplicitThisExplicitThis() {
        ModuleNode root = getAST("this.println()", Phases.SEMANTIC_ANALYSIS);
        MethodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertFalse(isImplicitThis);
    }
    
    public void testIsImplicitThisNoObject() {
        ModuleNode root = getAST("println()", Phases.SEMANTIC_ANALYSIS);
        MethodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertTrue(isImplicitThis);
    }
}
