/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.control.CompilePhase

/**
 * Test aspects of dynamic typing, where it applies and where not.<br />
 * Some aspects might be covered by other tests, too.
 * @see MethodNodeTest
 * @see org.codehaus.groovy.classgen.ClassCompletionVerifierTest#testDetectsInvalidCatchType()
 */
class DynamicTypeTest extends GroovyTestCase {

    void testGenericProperties() {
        def ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            class C<T> {
               private T x;
               private def y;
               private Object z;
            }/$.stripIndent())
        def fields = getClassNode(ast).fields
        assertEquals(3, fields.size())
        assertFalse(ClassHelper.isDynamicTyped(getFieldType(fields, 'x')))
        assertTrue(ClassHelper.isDynamicTyped(getFieldType(fields, 'y')))
        assertFalse(ClassHelper.isDynamicTyped(getFieldType(fields, 'z')))
    }

    void testCatchType() {
        def ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            class C<T> {
               // this is allowed in phase SEMANTIC_ANALYSIS, but CLASS_GENERATION would fail 
               def catching1() {
                  try {
                     42 / 0
                  } catch (T ex) {
                     ex.printStackTrace()
                  }
               }
               def catching2() {
                  try {
                     42 / 0
                  } catch (Throwable t) {
                     t.printStackTrace()
                  }
               }
            }
            /$.stripIndent())
        def methods = getClassNode(ast).methods
        assertEquals(2, methods.size())
        assertFalse(ClassHelper.isDynamicTyped(getOneCatchStatement(methods, 'catching1').variable.type))
        assertFalse(ClassHelper.isDynamicTyped(getOneCatchStatement(methods, 'catching2').variable.type))
        ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            class C<T extends Throwable> {
               def catching1() {
                  try {
                     42 / 0
                  } catch (T ex) {
                     ex.printStackTrace()
                  }
               }
               def catching2() {
                  try {
                     42 / 0
                  } catch (t) {
                     t.printStackTrace()
                  }
               }
            }
            /$.stripIndent())
        methods = getClassNode(ast).methods
        assertEquals(2, methods.size())
        assertFalse(ClassHelper.isDynamicTyped(getOneCatchStatement(methods, 'catching1').variable.type))
        // the type is unspecified, but coerced to java.lang.Exception, hence it is not dynamic
        assertFalse(ClassHelper.isDynamicTyped(getOneCatchStatement(methods, 'catching2').variable.type))
        // only in the CONVERSION phase it is dynamic, before type coercion happened
        // REMARK: this is an implementation specific behaviour and not strictly necessary for a correct result
        // --> if this check fails it does not necessarily mean that parsing is broken
        ast = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, $/\
            class C<T extends Throwable> {
               def catching() {
                  try {
                     42 / 0
                  } catch (t) {
                     t.printStackTrace()
                  }
               }
            }
            /$.stripIndent())
        methods = getClassNode(ast).methods
        assertEquals(1, methods.size())
        assertTrue(ClassHelper.isDynamicTyped(getOneCatchStatement(methods, 'catching').variable.type))
    }

    void testLambdaParameters() {
        def ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            import java.util.function.Function
            Function f = (s) -> { s.toUpperCase() }
            /$.stripIndent())
        def expression = getOneExpression(getClassNode(ast).methods, 'run')
        assertTrue(expression instanceof DeclarationExpression)
        assertTrue((expression as DeclarationExpression).rightExpression instanceof LambdaExpression)
        def lambda = (expression as DeclarationExpression).rightExpression as LambdaExpression
        assertTrue(ClassHelper.isDynamicTyped(lambda.parameters[0].type))
        ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            import java.util.function.Function
            Function f = (String s) -> { s.toUpperCase() }
            /$.stripIndent())
        expression = getOneExpression(getClassNode(ast).methods, 'run')
        assertTrue(expression instanceof DeclarationExpression)
        assertTrue((expression as DeclarationExpression).rightExpression instanceof LambdaExpression)
        lambda = (expression as DeclarationExpression).rightExpression as LambdaExpression
        assertFalse(ClassHelper.isDynamicTyped(lambda.parameters[0].type))
    }

    void testDynamicReturnType() {
        def ast = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, $/\
            class C<T> {
               def m(p = 'x') {}
               Object n() {}
               T o() {}
            }
            /$.stripIndent())
        def methods = getClassNode(ast).methods
        assertEquals(3, methods.size())
        assertTrue(ClassHelper.isDynamicTyped(methods.get(0).returnType))
        assertFalse(ClassHelper.isDynamicTyped(methods.get(1).returnType))
        assertFalse(ClassHelper.isDynamicTyped(methods.get(2).returnType))
        assertTrue(methods.get(0).dynamicReturnType)
        assertFalse(methods.get(1).dynamicReturnType)
        assertFalse(methods.get(2).dynamicReturnType)
    }

    private static ClassNode getClassNode(List<ASTNode> ast) {
        // the first node is an empty BlockStatement, the class is the second node
        assertTrue(ast.size() > 1)
        def classNode = ast.get(1)
        assertTrue(classNode instanceof ClassNode)
        classNode as ClassNode
    }

    private static def getFieldType(List<FieldNode> fields, String name) {
        for (FieldNode field : fields) {
            if (name == field.name) {
                return field.getType()
            }
        }
    }

    private static def getOneCatchStatement(List<MethodNode> methods, String name) {
        for (MethodNode method : methods) {
            if (name == method.name) {
                assertTrue(method.firstStatement instanceof TryCatchStatement)
                def catchStatements = (method.firstStatement as TryCatchStatement).catchStatements
                assertEquals(1, catchStatements.size())
                return catchStatements.get(0)
            }
        }
    }

    private static def getOneExpression(List<MethodNode> methods, String name) {
        for (MethodNode method : methods) {
            if (name == method.name) {
                assertTrue(method.firstStatement instanceof ExpressionStatement)
                return (method.firstStatement as ExpressionStatement).expression
            }
        }
    }
}