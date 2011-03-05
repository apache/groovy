package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.objectweb.asm.Opcodes;

import junit.framework.TestCase
import org.codehaus.groovy.control.CompilePhase;

/**
 * Tests the VariableExpressionNode
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 * @author Hamlet D'Arcy
 *
 */

public class MethodNodeTest extends TestCase implements Opcodes {

    public void testGetTextSimple() {
        def ast = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, false, '''

        def myMethod() {
        }
'''
        assert ast[1].@methods.get('myMethod')[0].text ==
                    'public java.lang.Object myMethod()  { ... }'
    }
    
    public void testGetTextAdvanced() {
        def ast = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, false, '''

        private static final <T> T myMethod(String p1, int p2 = 1) throws Exception, IOException {
        }
'''
        assert ast[1].@methods.get('myMethod')[0].text ==
                    'private static final java.lang.Object myMethod(java.lang.String p1, int p2 = 1) throws java.lang.Exception, java.io.IOException { ... }'
    }

    public void testIsDynamicReturnTypeExplicitObject() {
        def methodNode = new MethodNode('foo', ACC_PUBLIC, new ClassNode(Object.class), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
    }
    
    public void testIsDynamicReturnTypeDYNAMIC_TYPE() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert methodNode.isDynamicReturnType()
    }
    
    public void testIsDynamicReturnTypeVoid() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
    }
    
    public void testIsDynamicReturnTypNull() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
        assertNotNull(methodNode.getReturnType())
    }
}
