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
package org.codehaus.groovy.ast.builder

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.objectweb.asm.Opcodes

/**
 * Unit test for the AstBuilder class. Shows the usage of how to create AST from string input.  
 */
class AstBuilderFromStringTest extends GroovyTestCase {

    private AstBuilder factory

    protected void setUp() {
        super.setUp()
        factory = new AstBuilder()
    }


    /**
     * This shows how to compile a simple script that returns a constant,
     * discarding the generated Script subclass. 
     */
    void testSimpleConstant() {
        List<ASTNode> result = factory.buildFromString(CompilePhase.CONVERSION, " \"Some String\" ")

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new ConstantExpression("Some String")
                )],
                new VariableScope()
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    /**
     * This shows how to compile a script that includes declarations,
     * discarding the generated Script subclass.
     */
    void testAssignment() {
        List<ASTNode> result = factory.buildFromString(CompilePhase.CONVERSION, " def x = 2; def y = 4 ")

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new DeclarationExpression(
                                new VariableExpression("x"),
                                Token.newSymbol(Types.EQUAL, 0, 0),
                                new ConstantExpression(2)
                        )
                ),
                 new ExpressionStatement(
                         new DeclarationExpression(
                                 new VariableExpression("y"),
                                 Token.newSymbol(Types.EQUAL, 0, 0),
                                 new ConstantExpression(4)
                         )
                 )],
                new VariableScope()
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    /**
     * This shows how to compile a script that includes method calls,
     * discarding the generated Script subclass.
     */
    void testMethodCall() {
        List<ASTNode> result = factory.buildFromString(CompilePhase.CONVERSION, """ println "Hello World" """)

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new MethodCallExpression(
                                new VariableExpression("this"),
                                new ConstantExpression("println"),
                                new ArgumentListExpression(
                                        [new ConstantExpression("Hello World")]
                                )
                        )
                )],
                new VariableScope()
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    /**
     * This shows how to get the Script subclass off of the compiled result, compiling
     * all the way to CLASS_GENERATION.
     */
    void testWithScriptClassAndClassGeneration() {
        List<ASTNode> result = factory.buildFromString(CompilePhase.CLASS_GENERATION, false, " \"Some String\" ")

        def expectedScriptBody = new BlockStatement(
                [new ReturnStatement(
                        new ConstantExpression("Some String")
                )],
                new VariableScope()
        )

        def expectedClassNode = new ClassNode("synthesized", 1024, ClassHelper.make(Script))

        AstAssert.assertSyntaxTree([expectedScriptBody, expectedClassNode], result)
    }

    /**
     * Proves default value is CLASS_GENERATION and statementsOnly = true. 
     */
    void testDefaultValues() {
        List<ASTNode> result = factory.buildFromString(" \"Some String\" ")

        def expectedScriptBody = new BlockStatement(
                [new ReturnStatement(
                        new ConstantExpression("Some String")
                )],
                new VariableScope()
        )

        AstAssert.assertSyntaxTree([expectedScriptBody], result)
    }

    /**
     * This tests the contract of the build method, trying to pass null
     * arguments when those arguments are required.
     */
    void testContract() {

        // source is required
        shouldFail(IllegalArgumentException) {
            factory.buildFromString((String) null)
        }

        // source must not be empty
        shouldFail(IllegalArgumentException) {
            factory.buildFromString(" ")
        }
    }


    void testIfStatement() {

        def result = factory.buildFromString(
                CompilePhase.SEMANTIC_ANALYSIS,
                """ if (foo == bar) println "Hello" else println "World" """)

        def expected = new BlockStatement(
                [new IfStatement(
                        new BooleanExpression(
                                new BinaryExpression(
                                        new VariableExpression("foo"),
                                        new Token(Types.COMPARE_EQUAL, "==", -1, -1),
                                        new VariableExpression("bar")
                                )
                        ),
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new VariableExpression("this"),
                                        new ConstantExpression("println"),
                                        new ArgumentListExpression(
                                                [new ConstantExpression("Hello")]
                                        )
                                )
                        ),
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        new VariableExpression("this"),
                                        new ConstantExpression("println"),
                                        new ArgumentListExpression(
                                                [new ConstantExpression("World")]
                                        )
                                )
                        )
                )], new VariableScope())
        AstAssert.assertSyntaxTree([expected], result)
    }


    void testSwitchAndCaseAndBreakStatements() {

        def result = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, """
            switch (foo) {
                case 0:
                    break
                case 1:
                case 2:
                    println "<3"
                    break
                default:
                    println ">2"
            }
        """)

        def expected = new BlockStatement(
                [new SwitchStatement(
                        new VariableExpression("foo"),
                        [
                                new CaseStatement(
                                        new ConstantExpression(0),
                                        new BlockStatement(
                                                [new BreakStatement()], new VariableScope())
                                ),
                                new CaseStatement(
                                        new ConstantExpression(1),
                                        EmptyStatement.INSTANCE
                                ),
                                new CaseStatement(
                                        new ConstantExpression(2),
                                        new BlockStatement(
                                                [
                                                        new ExpressionStatement(
                                                                new MethodCallExpression(
                                                                        new VariableExpression("this"),
                                                                        new ConstantExpression("println"),
                                                                        new ArgumentListExpression(
                                                                                [new ConstantExpression("<3")]
                                                                        )
                                                                )
                                                        ),
                                                        new BreakStatement()
                                                ], new VariableScope()
                                        )
                                )
                        ],
                        new BlockStatement(
                                [new ExpressionStatement(
                                        new MethodCallExpression(
                                                new VariableExpression("this"),
                                                new ConstantExpression("println"),
                                                new ArgumentListExpression(
                                                        [new ConstantExpression(">2")]
                                                )
                                        )
                                )],
                                new VariableScope()
                        )
                )], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testCreatingClassAndMethods() {
        def result = factory.buildFromString(CompilePhase.SEMANTIC_ANALYSIS, """
            class MyClass {
                private String myField = "a field value"
                MyClass() {
                    println "In constructor!"
                }

                def myMethod() {
                    println "In method!"
                }
            }
        """)

        def expected = [
                new BlockStatement(),
                new ClassNode("MyClass", Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        ]
        AstAssert.assertSyntaxTree(expected, result)

        def classNode = result[1]
        def field = classNode?.fields?.find { FieldNode f -> f.name == 'myField' }
        def expectedField = new FieldNode(
                'myField',
                Opcodes.ACC_PRIVATE,
                ClassHelper.STRING_TYPE,
                classNode,
                new ConstantExpression('a field value')
        )
        AstAssert.assertSyntaxTree([expectedField], [field])

        def method = classNode?.methods?.find { MethodNode m -> m.name == 'myMethod' }
        def expectedMethod = new MethodNode(
                'myMethod',
                Opcodes.ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                [] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                        [new ExpressionStatement(
                                new MethodCallExpression(
                                        new VariableExpression('this'),
                                        'println',
                                        new ArgumentListExpression(
                                                new ConstantExpression('In method!')
                                        )
                                ))], new VariableScope())
        )
        AstAssert.assertSyntaxTree([expectedMethod], [method])

        def ctor = classNode?.constructors?.get(0)
        def expectedCtor = new ConstructorNode(
                Opcodes.ACC_PUBLIC,
                [] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                        [new ExpressionStatement(
                                new MethodCallExpression(
                                        new VariableExpression('this'),
                                        'println',
                                        new ArgumentListExpression(
                                                new ConstantExpression('In constructor!')
                                        )
                                ))], new VariableScope())
        )
        AstAssert.assertSyntaxTree([expectedCtor], [ctor])
    }
}
