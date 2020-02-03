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
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.builder.AstBuilder as FactoryAlias
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

/**
 * Test case to show an ASTBuilder working off of a code block.
 *
 * The field declarations, static initializers, instance initializers, 
 * and most of the comments are all meaningful and tested. 
 */
@WithAstBuilder
class AstBuilderFromCodeTest extends GroovyTestCase {

    List<ASTNode> normalField = new AstBuilder().buildFromCode { "constant#1" }

    static List<ASTNode> staticInitializedField
    static {
        staticInitializedField = new AstBuilder().buildFromCode { "constant#3" }
    }
    List<ASTNode> constructorInializedField

    AstBuilderFromCodeTest() {
        constructorInializedField = new AstBuilder().buildFromCode { "constant#4" }
    }

    List<ASTNode> normalProperty = new AstBuilder().buildFromCode { "constant#5" }


    void testImportedClassName() {

        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testFullyQualifiedClassName() {

        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        // it is important for this to remain a method invocation on a fully qualified class name
        def result = new org.codehaus.groovy.ast.builder.AstBuilder().buildFromCode {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testAliasedClassName() {

        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new FactoryAlias().buildFromCode {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testVariableInvocation() {

        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        AstBuilder factory = new AstBuilder()  //temporary variable is important to test
        def result = factory.buildFromCode {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testMethodReturnTypeInvocation() {
        shouldFail(IllegalStateException) {
            //todo: is there any way to make this work?
            makeAstFactory().buildFromCode {    // typed as Object in AST :(
                println "Hello World"
            }
        }
    }

    /**
     * Factory method used in testing.
     */
    private AstBuilder makeAstFactory() {
        return new AstBuilder()
    }


    void testPhase_SemanticAnalysis() {

        def expected = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, """ println "Hello World" """)

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testPhase_Conversion() {

        def expected = new AstBuilder().buildFromString(CompilePhase.CONVERSION, """ println "Hello World" """)

        def result = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testStatementsOnly_ReturnsScriptClass() {
        def expected = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, """ println "Hello World" """)

        def result = new AstBuilder().buildFromCode(CompilePhase.CONVERSION, false) {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testSingleLineClosure() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode { println "Hello World" }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testSingleLineClosure_WithComment() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode { println "Hello World" } // a comment DO NOT REMOVE
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testSingleLineClosure_MultipleStatements() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode { println "Hello World" }; 4 + 5
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testMultilineClosure_WithComments() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode {//comment1
            println "Hello World"//comment2
        }//comment3
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testSingleLineClosure_WithCStyleComment() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode { println "Hello World" } /* a comment DO NOT REMOVE */
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testSingleLineClosure_WithMultipleCStyleComments() {
        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder()./*comment1*/ buildFromCode/*comment2*/ {/*comment3*/
            println/*comment4*/ "Hello World"/*comment5*/
        }/*comment6*/
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testThreeLineClosure() {
        def expected = new AstBuilder().buildFromString(""" println "I"; println "Love"; println "Groovy" """)

        def result = new AstBuilder().buildFromCode {
            println "I"
            println "Love"
            println "Groovy"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

    void testInitializationInFieldDeclaration() {
        def expected = new AstBuilder().buildFromString(""" "constant#1" """)
        AstAssert.assertSyntaxTree(expected, normalField)
    }

    void testInitializationInstaticInialization() {
        def expected = new AstBuilder().buildFromString(""" "constant#3" """)
        AstAssert.assertSyntaxTree(expected, staticInitializedField)
    }

    void testInitializationInConstructor() {
        def expected = new AstBuilder().buildFromString(""" "constant#4" """)
        AstAssert.assertSyntaxTree(expected, constructorInializedField)
    }

    void testInitializationInPropertyDeclaration() {
        def expected = new AstBuilder().buildFromString(""" "constant#5" """)
        AstAssert.assertSyntaxTree(expected, normalProperty)
    }

    void testNamedArgumentListExpression() {

        def result = new AstBuilder().buildFromCode {
            new String(foo: 'bar')
        }

        def expected = new BlockStatement(
                [new ReturnStatement(
                        new ConstructorCallExpression(
                                new ClassNode(String),
                                new TupleExpression(
                                        new NamedArgumentListExpression(
                                                [
                                                        new MapEntryExpression(
                                                                new ConstantExpression('foo'),
                                                                new ConstantExpression('bar'),
                                                        )
                                                ]
                                        )
                                )
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testElvisOperatorExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            name ?: 'Anonymous'
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new ElvisOperatorExpression(
                                new VariableExpression('name'),
                                new ConstantExpression('Anonymous')
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testWhileStatementAndContinue() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            while (true) {
                x++
                continue
            }
        }

        def expected = new BlockStatement(
                [new WhileStatement(
                        new BooleanExpression(
                                new ConstantExpression(true)
                        ),
                        new BlockStatement(
                                [
                                        new ExpressionStatement(
                                                new PostfixExpression(
                                                        new VariableExpression("x"),
                                                        new Token(Types.PLUS_PLUS, "++", -1, -1),
                                                )
                                        ),
                                        new ContinueStatement()
                                ],
                                new VariableScope()
                        )
                )], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testTernaryExpression() {
        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            true ? 'male' : 'female'
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new TernaryExpression(
                                new BooleanExpression(
                                        new ConstantExpression(true)
                                ),
                                new ConstantExpression('male'),
                                new ConstantExpression('female')
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testSpreadMapExpression() {
        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            func(*: m)
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new MethodCallExpression(
                                new VariableExpression('this', ClassHelper.OBJECT_TYPE),
                                'func',
                                new NamedArgumentListExpression(
                                        [new MapEntryExpression(
                                                new SpreadMapExpression(
                                                        new VariableExpression('m', ClassHelper.OBJECT_TYPE)
                                                ),
                                                new VariableExpression('m', ClassHelper.OBJECT_TYPE)
                                        )]
                                )
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testForStatementAndClosureListExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            for (int x = 0; x < 10; x++) {
                println x
            }
        }

        def expected = new BlockStatement([new ForStatement(
                new Parameter(ClassHelper.OBJECT_TYPE, "forLoopDummyParameter"),
                new ClosureListExpression(
                        [
                                new DeclarationExpression(
                                        new VariableExpression("x"),
                                        new Token(Types.EQUALS, "=", -1, -1),
                                        new ConstantExpression(0)
                                ),
                                new BinaryExpression(
                                        new VariableExpression("x"),
                                        new Token(Types.COMPARE_LESS_THAN, "<", -1, -1),
                                        new ConstantExpression(10)
                                ),
                                new PostfixExpression(
                                        new VariableExpression("x"),
                                        new Token(Types.PLUS_PLUS, "++", -1, -1)
                                )
                        ]
                ),
                new BlockStatement(
                        [
                                new ExpressionStatement(
                                        new MethodCallExpression(
                                                new VariableExpression("this"),
                                                new ConstantExpression("println"),
                                                new ArgumentListExpression(
                                                        new VariableExpression("x"),
                                                )
                                        )
                                )
                        ],
                        new VariableScope()
                )
        )], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testFinallyStatement() {
        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            try {
                return 1
            } finally {
                x.close()
            }
        }

        def expected = new BlockStatement(
                [new TryCatchStatement(
                        new BlockStatement(
                                [new ReturnStatement(
                                        new ConstantExpression(1)
                                )],
                                new VariableScope()
                        ),
                        new BlockStatement(
                                [
                                        new BlockStatement(
                                                [
                                                        new ExpressionStatement(
                                                                new MethodCallExpression(
                                                                        new VariableExpression('x'),
                                                                        'close',
                                                                        new ArgumentListExpression()
                                                                )
                                                        )
                                                ],
                                                new VariableScope())
                                ],
                                new VariableScope()
                        )
                )], new VariableScope())
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testReturnAndSynchronizedStatement() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            synchronized (this) {
                return 1
            }
        }

        def expected = new BlockStatement(
                [new SynchronizedStatement(
                        new VariableExpression("this"),
                        new BlockStatement(
                                [new ReturnStatement(
                                        new ConstantExpression(1)
                                )],
                                new VariableScope()
                        )
                )], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testAssertStatement() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            assert true: "should always be true"
            assert 1 == 2
        }

        def expected = new BlockStatement(
                [
                        new AssertStatement(
                                new BooleanExpression(
                                        new ConstantExpression(true)
                                ),
                                new ConstantExpression("should always be true")
                        ),
                        new AssertStatement(
                                new BooleanExpression(
                                        new BinaryExpression(
                                                new ConstantExpression(1),
                                                new Token(Types.COMPARE_EQUAL, "==", -1, -1),
                                                new ConstantExpression(2)
                                        )
                                )
                        ),
                ],
                new VariableScope()
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testSwitchAndCaseAndBreakStatements() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
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
        }

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


    void testRangeExpression_SimpleForm() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            (0..10)
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new RangeExpression(
                                new ConstantExpression(0),
                                new ConstantExpression(10),
                                true
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMethodPointerExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            Integer.&toString
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new MethodPointerExpression(
                                new ClassExpression(ClassHelper.Integer_TYPE),
                                new ConstantExpression("toString")
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testGStringExpression() {
        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            "$foo"
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new GStringExpression('$foo',
                                [new ConstantExpression(''), new ConstantExpression('')],
                                [new VariableExpression('foo')])
                )], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMapAndMapEntryExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            [foo: 'bar', baz: 'buz']
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new MapExpression(
                                [
                                        new MapEntryExpression(
                                                new ConstantExpression('foo'),
                                                new ConstantExpression('bar')
                                        ),
                                        new MapEntryExpression(
                                                new ConstantExpression('baz'),
                                                new ConstantExpression('buz')
                                        ),
                                ]
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testClassExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            def foo = String
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new DeclarationExpression(
                                new VariableExpression("foo"),
                                new Token(Types.EQUALS, "=", -1, -1),
                                new ClassExpression(ClassHelper.STRING_TYPE)
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testUnaryPlusExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            (+foo)
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new UnaryPlusExpression(
                                new VariableExpression("foo")
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testUnaryMinusExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            (-foo)
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new UnaryMinusExpression(
                                new VariableExpression("foo")
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testPrefixExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            ++1
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new PrefixExpression(
                                new Token(Types.PLUS_PLUS, "++", -1, -1),
                                new ConstantExpression(1)
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testPostfixExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            1++
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new PostfixExpression(
                                new ConstantExpression(1),
                                new Token(Types.PLUS_PLUS, "++", -1, -1)
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testNotExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            !true
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new NotExpression(
                                new ConstantExpression(true)
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testConstructorCallExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            new Integer(4)
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new ConstructorCallExpression(
                                new ClassNode(Integer),
                                new ArgumentListExpression(
                                        new ConstantExpression(4)
                                )
                        ))], new VariableScope())

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testClosureExpression_MultipleParameters() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            { x, y, z -> println z }
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new ClosureExpression(
                                [
                                        new Parameter(ClassHelper.OBJECT_TYPE, "x"),
                                        new Parameter(ClassHelper.OBJECT_TYPE, "y"),
                                        new Parameter(ClassHelper.OBJECT_TYPE, "z")] as Parameter[],
                                new BlockStatement(
                                        [new ExpressionStatement(
                                                new MethodCallExpression(
                                                        new VariableExpression("this"),
                                                        new ConstantExpression("println"),
                                                        new ArgumentListExpression(
                                                                new VariableExpression("z")
                                                        )
                                                )
                                        )],
                                        new VariableScope()
                                )
                        ))], new VariableScope())
        AstAssert.assertSyntaxTree([expected], result)
    }


    void testCastExpression() {
        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            (Integer) ""
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new CastExpression(
                                new ClassNode(Integer),
                                new ConstantExpression("")
                        ))], new VariableScope())
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testDeclarationAndListExpression() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            def foo = [1, 2, 3]
        }

        def expected = new BlockStatement(
                [new ExpressionStatement(
                        new DeclarationExpression(
                                new VariableExpression("foo"),
                                new Token(Types.EQUALS, "=", -1, -1),
                                new ListExpression(
                                        [new ConstantExpression(1),
                                         new ConstantExpression(2),
                                         new ConstantExpression(3),]
                                )
                        ))], new VariableScope())
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testIfStatement() {

        def result = new AstBuilder().buildFromCode(CompilePhase.SEMANTIC_ANALYSIS) {
            if (foo == bar) println "Hello" else println "World"
        }

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

}
