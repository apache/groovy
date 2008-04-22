package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.objectweb.asm.Opcodes;

import junit.framework.TestCase;

/**
 * Tests the VariableExpressionNode
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */

public class MethodNodeTest extends TestCase implements Opcodes {

	public void testIsDynamicReturnTypeExplizitObject() {
    	MethodNode methodNode = new MethodNode("foo", ACC_PUBLIC, new ClassNode(Object.class), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        assertFalse(methodNode.isDynamicReturnType());
    }
	
	public void testIsDynamicReturnTypeDYNAMIC_TYPE() {
    	MethodNode methodNode = new MethodNode("foo", ACC_PUBLIC, ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        assertTrue(methodNode.isDynamicReturnType());
    }
	
	public void testIsDynamicReturnTypeVoid() {
    	MethodNode methodNode = new MethodNode("foo", ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        assertFalse(methodNode.isDynamicReturnType());
    }
	
	public void testIsDynamicReturnTypNull() {
    	MethodNode methodNode = new MethodNode("foo", ACC_PUBLIC, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
        assertFalse(methodNode.isDynamicReturnType());
        assertNotNull(methodNode.getReturnType());
    }
}
