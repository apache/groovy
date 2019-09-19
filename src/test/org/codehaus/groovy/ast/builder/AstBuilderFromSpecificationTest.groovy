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
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC

/**
 * Unit test for the AST from Psuedo-specification feature.
 */
class AstBuilderFromSpecificationTest extends GroovyTestCase {

    void testSimpleMethodCall() {

        def result = new AstBuilder().buildFromSpec {
            methodCall {
                variable "this"
                constant "println"
                argumentList {
                    constant "Hello"
                }
            }
        }

        def expected = new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                        [new ConstantExpression("Hello")]
                )
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testErrorHandling_TooManyArguments() {

        def message = shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                methodCall {
                    constant "four arguments"
                    constant "is too"
                    constant "many for"
                    constant "this method"
                }
            }
        }
        assertTrue("Unhelpful error message: $message", message.contains('methodCall could not be invoked'))
        assertTrue("Unhelpful error message: $message", message.contains('Expected to receive'))
        assertTrue("Unhelpful error message: $message", message.contains('but found'))
    }

    void testErrorHandling_WrongArgumentTypes() {

        def message = shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                methodCall {
                    returnStatement {
                        constant 1
                    }
                    constant "ignored"
                    empty()
                }
            }
        }
        assertTrue("Unhelpful error message: $message", message.contains('methodCall could not be invoked'))
        assertTrue("Unhelpful error message: $message", message.contains('Expected to receive'))
        assertTrue("Unhelpful error message: $message", message.contains('but found'))
    }

    void testAnnotationConstantExpression() {

        def result = new AstBuilder().buildFromSpec {
            annotationConstant {
                annotation Override
            }
        }

        def expected = new AnnotationConstantExpression(
                new AnnotationNode(
                        ClassHelper.make(Override.class, false)
                )
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testArgumentListExpression_NoArgs() {

        def result = new AstBuilder().buildFromSpec {
            argumentList()
        }

        def expected = new ArgumentListExpression()
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testArgumentListExpression_OneListArg() {

        def result = new AstBuilder().buildFromSpec {
            argumentList {
                constant "constant1"
                constant "constant2"
                constant "constant3"
                constant "constant4"
            }
        }

        def expected = new ArgumentListExpression(
                [new ConstantExpression("constant1"),
                        new ConstantExpression("constant2"),
                        new ConstantExpression("constant3"),
                        new ConstantExpression("constant4"),
                ]
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testAttributeExpression() {

        // represents foo.bar attribute invocation
        def result = new AstBuilder().buildFromSpec {
            attribute {
                variable "foo"
                constant "bar"
            }
        }

        def expected = new AttributeExpression(
                new VariableExpression("foo"),
                new ConstantExpression("bar")
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    /**
     * Test for code:
     * if (foo == bar) println "Hello" else println "World"
     */
    void testIfStatement() {
        // if (foo == bar) println "Hello" else println "World"
        def result = new AstBuilder().buildFromSpec {
            ifStatement {
                booleanExpression {
                    binary {
                        variable "foo"
                        token "=="
                        variable "bar"
                    }
                }
                //if block
                expression {        // NOTE: if block and else block are order dependent and same type
                    methodCall {
                        variable "this"
                        constant "println"
                        argumentList {
                            constant "Hello"
                        }
                    }
                }
                //else block
                expression {
                    methodCall {
                        variable "this"
                        constant "println"
                        argumentList {
                            constant "World"
                        }
                    }
                }
            }
        }

        def expected = new IfStatement(
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
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testDeclarationAndListExpression() {

        // represents def foo = [1, 2, 3]
        def result = new AstBuilder().buildFromSpec {
            declaration {
                variable "foo"
                token "="
                list {
                    constant 1
                    constant 2
                    constant 3
                }
            }
        }

        def expected = new DeclarationExpression(
                new VariableExpression("foo"),
                new Token(Types.EQUALS, "=", -1, -1),
                new ListExpression(
                        [new ConstantExpression(1),
                                new ConstantExpression(2),
                                new ConstantExpression(3),]
                )
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testArrayExpression() {

        // new Integer[]{1, 2, 3}
        def result = new AstBuilder().buildFromSpec {
            array(Integer) {
                constant 1
                constant 2
                constant 3
            }
        }

        def expected = new ArrayExpression(
                ClassHelper.make(Integer, false),
                [
                        new ConstantExpression(1),
                        new ConstantExpression(2),
                        new ConstantExpression(3),]
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testBitwiseNegationExpression() {
        def result = new AstBuilder().buildFromSpec {
            bitwiseNegation {
                constant 1
            }
        }

        def expected = new BitwiseNegationExpression(
                new ConstantExpression(1)
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testCastExpression() {
        def result = new AstBuilder().buildFromSpec {
            cast(Integer) {
                constant ""
            }
        }

        def expected = new CastExpression(
                ClassHelper.make(Integer, false),
                new ConstantExpression("")
        )
        AstAssert.assertSyntaxTree([expected], result)
    }


    void testClosureExpression() {

        // { parm -> println parm }
        def result = new AstBuilder().buildFromSpec {
            closure {
                parameters {
                    parameter 'parm': Object.class
                }
                block {
                    expression {
                        methodCall {
                            variable "this"
                            constant "println"
                            argumentList {
                                variable "parm"
                            }
                        }
                    }
                }
            }
        }

        def expected = new ClosureExpression(
                [new Parameter(
                        ClassHelper.make(Object, false), "parm"
                )] as Parameter[],
                new BlockStatement(
                        [new ExpressionStatement(
                                new MethodCallExpression(
                                        new VariableExpression("this"),
                                        new ConstantExpression("println"),
                                        new ArgumentListExpression(
                                                new VariableExpression("parm")
                                        )
                                )
                        )],
                        new VariableScope()
                )

        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testClosureExpression_MultipleParameters() {

        // { x,y,z -> println z }
        def result = new AstBuilder().buildFromSpec {
            closure {
                parameters {
                    parameter 'x': Object.class
                    parameter 'y': Object.class
                    parameter 'z': Object.class
                }
                block {
                    expression {
                        methodCall {
                            variable "this"
                            constant "println"
                            argumentList {
                                variable "z"
                            }
                        }
                    }
                }
            }
        }

        def expected = new ClosureExpression(
                [
                        new Parameter(ClassHelper.make(Object, false), "x"),
                        new Parameter(ClassHelper.make(Object, false), "y"),
                        new Parameter(ClassHelper.make(Object, false), "z")] as Parameter[],
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

        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testConstructorCallExpression() {

        // new Integer(4)
        def result = new AstBuilder().buildFromSpec {
            constructorCall(Integer) {
                argumentList {
                    constant 4
                }
            }
        }

        def expected = new ConstructorCallExpression(
                ClassHelper.make(Integer, false),
                new ArgumentListExpression(
                        new ConstantExpression(4)
                )
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testNotExpression() {
        // !true
        def result = new AstBuilder().buildFromSpec {
            not {
                constant true
            }
        }

        def expected = new NotExpression(
                new ConstantExpression(true)
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testPostfixExpression() {
        // 1++
        def result = new AstBuilder().buildFromSpec {
            postfix {
                constant 1
                token "++"
            }
        }

        def expected = new PostfixExpression(
                new ConstantExpression(1),
                new Token(Types.PLUS_PLUS, "++", -1, -1)
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testPrefixExpression() {
        // ++1
        def result = new AstBuilder().buildFromSpec {
            prefix {
                token "++"
                constant 1
            }
        }

        def expected = new PrefixExpression(
                new Token(Types.PLUS_PLUS, "++", -1, -1),
                new ConstantExpression(1)
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testUnaryMinusExpression() {
        // (-foo)
        def result = new AstBuilder().buildFromSpec {
            unaryMinus {
                variable "foo"
            }
        }

        def expected = new UnaryMinusExpression(
                new VariableExpression("foo")
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testUnaryPlusExpression() {
        // (+foo)
        def result = new AstBuilder().buildFromSpec {
            unaryPlus {
                variable "foo"
            }
        }

        def expected = new UnaryPlusExpression(
                new VariableExpression("foo")
        )

        AstAssert.assertSyntaxTree([expected], result)
    }


    void testClassExpression() {
        // def foo = String
        def result = new AstBuilder().buildFromSpec {
            declaration {
                variable "foo"
                token "="
                classExpression String
            }
        }

        def expected = new DeclarationExpression(
                new VariableExpression("foo"),
                new Token(Types.EQUALS, "=", -1, -1),
                new ClassExpression(ClassHelper.make(String, false))
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testFieldExpression() {
        // public static String foo = "a value"
        def result = new AstBuilder().buildFromSpec {
            field {
                fieldNode "foo", ACC_PUBLIC | ACC_STATIC, String, this.class, {
                    constant "a value"
                    annotations {
                        annotation Deprecated
                    }
                }
            }
        }

        def fieldNode = new FieldNode(
                "foo",
                ACC_PUBLIC | ACC_STATIC,
                ClassHelper.make(String, false),
                ClassHelper.make(this.class, false),
                new ConstantExpression("a value")
        )
        fieldNode.addAnnotation(new AnnotationNode(ClassHelper.make(Deprecated, false)))
        def expected = new FieldExpression(fieldNode)

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMapAndMapEntryExpression() {

        // [foo: 'bar', baz: 'buz']
        def result = new AstBuilder().buildFromSpec {
            map {
                mapEntry {
                    constant 'foo'
                    constant 'bar'
                }
                mapEntry {
                    constant 'baz'
                    constant 'buz'
                }
            }
        }

        def expected = new MapExpression([
                new MapEntryExpression(new ConstantExpression('foo'), new ConstantExpression('bar')),
                new MapEntryExpression(new ConstantExpression('baz'), new ConstantExpression('buz')),
        ])

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMapAndMapEntryExpression_SimpleCase() {

        // [foo: 'bar', baz: 'buz']
        def result = new AstBuilder().buildFromSpec {
            map {
                mapEntry 'foo': 'bar'       // NOTE: this really only works for constants. 
                mapEntry 'baz': 'buz'
                mapEntry 'qux': 'quux', 'corge': 'grault'
            }
        }

        def expected = new MapExpression([
                new MapEntryExpression(new ConstantExpression('foo'), new ConstantExpression('bar')),
                new MapEntryExpression(new ConstantExpression('baz'), new ConstantExpression('buz')),
                new MapEntryExpression(new ConstantExpression('qux'), new ConstantExpression('quux')),
                new MapEntryExpression(new ConstantExpression('corge'), new ConstantExpression('grault')),
        ])

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testGStringExpression() {
        // "$foo"
        def result = new AstBuilder().buildFromSpec {
            gString '$foo astring $bar', {
                strings {
                    constant ''
                    constant ' astring '
                    constant ''
                }
                values {
                    variable 'foo'
                    variable 'bar'
                }
            }
        }

        def expected = new GStringExpression('$foo astring $bar',
                [new ConstantExpression(''), new ConstantExpression(' astring '), new ConstantExpression('')],
                [new VariableExpression('foo'), new VariableExpression('bar')])


        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMethodPointerExpression() {
        // Integer.&toString
        def result = new AstBuilder().buildFromSpec {
            methodPointer {
                classExpression Integer
                constant "toString"
            }
        }

        def expected = new MethodPointerExpression(
                new ClassExpression(ClassHelper.make(Integer, false)),
                new ConstantExpression("toString")
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testRangeExpression() {
        // (0..10)
        def result = new AstBuilder().buildFromSpec {
            range {
                constant 0
                constant 10
                inclusive true
            }
        }

        def expected = new RangeExpression(
                new ConstantExpression(0),
                new ConstantExpression(10),
                true
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testRangeExpression_Exclusive() {
        // (0..10)
        def result = new AstBuilder().buildFromSpec {
            range {
                constant 0
                constant 10
                inclusive false
            }
        }

        def expected = new RangeExpression(
                new ConstantExpression(0),
                new ConstantExpression(10),
                false
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testRangeExpression_SimpleForm() {
        // (0..10)
        def result = new AstBuilder().buildFromSpec {
            range(0..10)
        }

        def expected = new RangeExpression(
                new ConstantExpression(0),
                new ConstantExpression(10),
                true
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testPropertyExpression() {
        // foo.bar
        def result = new AstBuilder().buildFromSpec {
            property {                  // this name conflicts with PropertyNode. 
                variable "foo"
                constant "bar"
            }
        }

        def expected = new PropertyExpression(
                new VariableExpression("foo"),
                new ConstantExpression("bar")
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testSwitchAndCaseAndBreakStatements() {
        /*
                  switch (foo) {
                      case 0: break "some label"
                      case 1:
                      case 2:
                          println "<3"
                          break;
                      default:
                          println ">2"
                  }
                   */
        def result = new AstBuilder().buildFromSpec {
            switchStatement {           //NOTE: switchStatement is abnormal pattern
                variable "foo"
                defaultCase {       // NOTE: this creates a block statement
                    expression {        //NOTE: default branch is problematic b/c it is order dependent
                        methodCall {
                            variable "this"
                            constant "println"
                            argumentList {
                                constant ">2"
                            }
                        }
                    }
                }
                caseStatement {
                    constant 0
                    breakStatement "some label"  // label parameter is optional
                }
                caseStatement {
                    constant 1
                    empty()
                }
                caseStatement {
                    constant 2
                    block {
                        expression {
                            methodCall {
                                variable "this"
                                constant "println"
                                argumentList {
                                    constant "<3"
                                }
                            }
                        }
                        breakStatement()
                    }
                }
            }
        }

        def expected = new SwitchStatement(
                new VariableExpression("foo"),
                [
                        new CaseStatement(
                                new ConstantExpression(0),
                                new BreakStatement("some label")
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
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testAssertStatement() {
        /*
                  assert true : "should always be true"
                  assert 1 == 2
                  */
        def result = new AstBuilder().buildFromSpec {
            block {
                assertStatement {
                    booleanExpression {
                        constant true
                    }
                    constant "should always be true"
                }
                assertStatement {
                    booleanExpression {
                        binary {
                            constant 1
                            token "=="
                            constant 2
                        }
                    }
                }
            }
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


    void testReturnAndSynchronizedStatement() {
        /*
                  synchronized (this) {
                      return 1
                  }
          */
        def result = new AstBuilder().buildFromSpec {
            synchronizedStatement {
                variable "this"
                block {
                    returnStatement {
                        constant 1
                    }
                }
            }
        }

        def expected = new SynchronizedStatement(
                new VariableExpression("this"),
                new BlockStatement(
                        [new ReturnStatement(
                                new ConstantExpression(1)
                        )],
                        new VariableScope()
                )
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testTryCatchAndCatchAndThrowStatements() {
        /*
                  try {
                      return 1
                  } catch (Exception e) {
                       throw e
                  }
          */
        def result = new AstBuilder().buildFromSpec {
            tryCatch {
                block {
                    returnStatement {
                        constant 1
                    }
                }
                empty() //finally block must be specified?
                catchStatement {
                    parameter 'e': Exception.class
                    block {
                        throwStatement {
                            variable "e"
                        }
                    }
                }
            }
        }

        TryCatchStatement expected = new TryCatchStatement(
                new BlockStatement(
                        [new ReturnStatement(
                                new ConstantExpression(1)
                        )],
                        new VariableScope()
                ),
                EmptyStatement.INSTANCE
        )
        expected.addCatch(
                new CatchStatement(
                        new Parameter(
                                ClassHelper.make(Exception, false), "e"
                        ),
                        new BlockStatement(
                                [new ThrowStatement(
                                        new VariableExpression("e")
                                )],
                                new VariableScope()
                        )
                )
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testFinallyStatement() {
        /*
                  try {
                      return 1
                  } finally {
                       x.close()
                  }
          */
        def result = new AstBuilder().buildFromSpec {
            tryCatch {
                block {
                    returnStatement {
                        constant 1
                    }
                }
                block {
                    block {
                        expression {
                            methodCall {
                                variable 'x'
                                constant 'close'
                                argumentList()
                            }
                        }
                    }
                }
            }
        }

        TryCatchStatement expected = new TryCatchStatement(
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
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testForStatementAndClosureListExpression() {
        /*
              for (int x = 0; x < 10; x++) {
                  println x
              }
          */

        def result = new AstBuilder().buildFromSpec {
            forStatement {
                parameter 'forLoopDummyParameter': Object.class
                closureList {
                    declaration {
                        variable 'x'
                        token '='
                        constant 0
                    }
                    binary {
                        variable 'x'
                        token '<'
                        constant 10
                    }
                    postfix {
                        variable 'x'
                        token '++'
                    }
                }
                block {
                    expression {
                        methodCall {
                            variable 'this'
                            constant 'println'
                            argumentList {
                                variable 'x'
                            }
                        }
                    }
                }
            }
        }

        def expected = new ForStatement(
                new Parameter(ClassHelper.make(Object, false), "forLoopDummyParameter"),
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
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testStaticMethodCallExpression_MethodAsString() {
        // Math.min(1,2)
        def result = new AstBuilder().buildFromSpec {
            staticMethodCall(Math, "min") {
                argumentList {
                    constant 1
                    constant 2
                }
            }
        }

        def expected = new StaticMethodCallExpression(
                ClassHelper.make(Math, false),
                "min",
                new ArgumentListExpression(
                        new ConstantExpression(1),
                        new ConstantExpression(2)
                )
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testStaticMethodCallExpression_PassingMethodPointer() {
        // Math.min(1,2)
        def result = new AstBuilder().buildFromSpec {
            staticMethodCall(Math.&min) {      // more terse way to call existing methods
                argumentList {
                    constant 1
                    constant 2
                }
            }
        }

        def expected = new StaticMethodCallExpression(
                ClassHelper.make(Math, false),
                "min",
                new ArgumentListExpression(
                        new ConstantExpression(1),
                        new ConstantExpression(2)
                )
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testSpreadExpression() {
        // ['foo', *['bar','baz']]
        def result = new AstBuilder().buildFromSpec {
            list {
                constant 'foo'
                spread {
                    list {
                        constant 'bar'
                        constant 'baz'
                    }
                }
            }
        }

        def expected = new ListExpression([
            new ConstantExpression('foo'),
            new SpreadExpression(
                new ListExpression([
                        new ConstantExpression('bar'),
                        new ConstantExpression('baz'),
                ])
            )
        ])

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testSpreadMapExpression() {
        // func (*:m)
        def result = new AstBuilder().buildFromSpec {
            methodCall {
                variable 'this'
                constant 'func'
                mapEntry {
                    spreadMap {
                        variable 'm'
                    }
                    variable 'm'
                }
            }
        }

        def expected = new MethodCallExpression(
                new VariableExpression('this', ClassHelper.make(Object, false)),
                'func',
                new MapEntryExpression(
                        new SpreadMapExpression(new VariableExpression('m', ClassHelper.make(Object, false))),
                        new VariableExpression('m', ClassHelper.make(Object, false))
                )

        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testTernaryExpression() {
        // true ? "male" : "female"
        def result = new AstBuilder().buildFromSpec {
            ternary {
                booleanExpression {
                    constant true
                }
                constant 'male'
                constant 'female'
            }
        }

        def expected = new TernaryExpression(
                new BooleanExpression(new ConstantExpression(true)),
                new ConstantExpression('male'),
                new ConstantExpression('female')
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testDoWhileStatement() {
        // DoWhileStatement is not currently supported by Groovy syntax but is allowed in the AST
    }

    void testStatement() {
        // Statement is used as an abstract class within the groovy source and is never instantiated
    }

    void testWhileStatementAndContinueStatement() {
        /*
              while (true) {
                  x++
                  continue
              }
          */
        def result = new AstBuilder().buildFromSpec {
            whileStatement {
                booleanExpression {
                    constant true
                }
                block {
                    expression {
                        postfix {
                            variable 'x'
                            token '++'
                        }
                    }
                    continueStatement()
                }
            }
        }

        def expected = new WhileStatement(
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
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testWhileStatementAndContinueToLabelStatement() {
        /*
              while (true) {
                  x++
                  continue "some label"
              }
          */
        def result = new AstBuilder().buildFromSpec {
            whileStatement {
                booleanExpression {
                    constant true
                }
                block {
                    expression {
                        postfix {
                            variable 'x'
                            token '++'
                        }
                    }
                    continueStatement {
                        label "some label"
                    }
                }
            }
        }

        def expected = new WhileStatement(
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
                                new ContinueStatement("some label")
                        ],
                        new VariableScope()
                )
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testElvisOperatorExpression() {
        // name ?: 'Anonymous'
        def result = new AstBuilder().buildFromSpec {
            elvisOperator {
                variable 'name'
                constant 'Anonymous'
            }
        }

        def expected = new ElvisOperatorExpression(
                new VariableExpression('name'),
                new ConstantExpression('Anonymous')
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testNamedArgumentListExpression() {
        // new String(foo: 'bar')

        def result = new AstBuilder().buildFromSpec {
            constructorCall(String) {
                tuple {
                    namedArgumentList {
                        mapEntry {
                            constant 'foo'
                            constant 'bar'
                        }
                    }
                }
            }
        }

        def expected = new ConstructorCallExpression(
                ClassHelper.make(String),
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
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testParameters_DefaultValues() {
        /*
          public String myMethod(String parameter = null) {
            'some result'
          }
         */

        def result = new AstBuilder().buildFromSpec {
            method('myMethod', ACC_PUBLIC, String) {
                parameters {
                    parameter 'parameter': String.class, {
                        constant null
                    }
                }
                exceptions {}
                block {
                    returnStatement {
                        constant 'some result'
                    }
                }
                annotations {}
            }
        }

        def expected = new MethodNode(
                "myMethod",
                ACC_PUBLIC,
                ClassHelper.make(String.class, false),
                [new Parameter(ClassHelper.make(String, false), "parameter", new ConstantExpression(null))] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                        [new ReturnStatement(
                                new ConstantExpression('some result')
                        )],
                        new VariableScope()
                ))
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testParameters_VarArgs() {
        /*
          public String myMethod(String... parameters) {
            'some result'
          }
         */
        // vararg methods are just array methods. 
        def result = new AstBuilder().buildFromSpec {
            method('myMethod', ACC_PUBLIC, String) {
                parameters {
                    parameter 'parameters': String[].class
                }
                exceptions {}
                block {
                    returnStatement {
                        constant 'some result'
                    }
                }
            }
        }

        def expected = new MethodNode(
                "myMethod",
                ACC_PUBLIC,
                ClassHelper.make(String.class, false),
                [new Parameter(ClassHelper.make(String[], false), "parameters")] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                        [new ReturnStatement(
                                new ConstantExpression('some result')
                        )],
                        new VariableScope()
                ))
        AstAssert.assertSyntaxTree([expected], result)

    }

    void testInnerClassNode() {
        /*
            class Foo {
              static class Bar {
              }
            }
        */
        def result = new AstBuilder().buildFromSpec {
            innerClass 'Foo$Bar', ACC_PUBLIC, {
                //outer class
                classNode 'Foo', ACC_PUBLIC, {
                    classNode Object        //superclass
                    interfaces {
                        classNode GroovyObject
                    }
                    mixins {}
                }
                classNode Object            //superclass
                interfaces {
                    classNode GroovyObject
                }
                mixins {}
            }
        }

        def expected = new InnerClassNode(
                new ClassNode(
                        "Foo",
                        ACC_PUBLIC,
                        ClassHelper.make(Object, false),
                        [ClassHelper.make(GroovyObject, false)] as ClassNode[],
                        [] as MixinNode[]
                ),
                'Foo$Bar',
                ACC_PUBLIC,
                ClassHelper.make(Object, false),
                [ClassHelper.make(GroovyObject, false)] as ClassNode[],
                [] as MixinNode[]
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testAnnotatedNode() {
        // this class is never instantiated. It is used as an abstract class but not marked as such. 
    }

    void testConstructorNode() {

        // public <init>(String foo, Integer bar) throws IOException, Exception {}
        def result = new AstBuilder().buildFromSpec {
            constructor(ACC_PUBLIC) {
                parameters {
                    parameter 'foo': String.class
                    parameter 'bar': Integer.class
                }
                exceptions {
                    classNode Exception
                    classNode IOException
                }
                block {

                }
            }
        }

        def expected = new ConstructorNode(
                ACC_PUBLIC,
                [
                        new Parameter(ClassHelper.make(String, false), "foo"),
                        new Parameter(ClassHelper.make(Integer, false), "bar")
                ] as Parameter[],
                [
                        ClassHelper.make(Exception, false),
                        ClassHelper.make(IOException, false)
                ] as ClassNode[],
                new BlockStatement()
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testGenericsType() {
        // class MyClass<T, U extends Number> {}

        def result = new AstBuilder().buildFromSpec {
            classNode 'MyClass', ACC_PUBLIC, {
                classNode Object        //superclass
                interfaces {
                    classNode GroovyObject
                }
                mixins {}
                genericsTypes {
                    genericsType Object
                    genericsType Number, {      
                        upperBound {
                            classNode Number
                        }
                    }
                }
            }
        }

        def expected = new ClassNode(
                "MyClass", ACC_PUBLIC, ClassHelper.make(Object, false)
        )
        expected.setGenericsTypes(
                [
                        new GenericsType(ClassHelper.make(Object, false)),
                        new GenericsType(ClassHelper.make(Number, false), [ClassHelper.make(Number, false)] as ClassNode[], null),
                ] as GenericsType[]
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testClassWithMethods() {
        // class MyClass {
        //   String myProp = 'foo'
        //   String myMethod(String parameter) throws IOException { 'some result' }
        //   String myOtherMethod() { 'some other result' }
        // }

        def result = new AstBuilder().buildFromSpec {
            classNode 'MyClass', ACC_PUBLIC, {
                classNode Object        //superclass
                interfaces {
                    classNode GroovyObject
                }
                mixins {}
                methods {
                    method('myMethod', ACC_PUBLIC, String) {
                        parameters {
                            parameter 'parameter': String
                        }
                        exceptions {
                            classNode IOException
                        }
                        block {
                            returnStatement {
                                constant 'some result'
                            }
                        }
                    }
                    method('myOtherMethod', ACC_PUBLIC, String) {
                        parameters {}
                        exceptions {}
                        block {
                            returnStatement {
                                constant 'some other result'
                            }
                        }
                    }
                }
                properties {
                    propertyNode "myProp", ACC_PUBLIC, String, this.class, {
                        constant "foo"
                        annotations {
                            annotation Deprecated
                        }
                    }
                }
                annotations {
                    annotation Deprecated
                }
            }
        }

        def expected = new ClassNode("MyClass", ACC_PUBLIC, ClassHelper.make(Object, false))
        expected.addAnnotation(new AnnotationNode(ClassHelper.make(Deprecated, false)))
        def pNode = new PropertyNode("myProp", ACC_PUBLIC, ClassHelper.make(String, false),
                ClassHelper.make(this.class, false), new ConstantExpression("foo"), null, null)
        pNode.addAnnotation(new AnnotationNode(ClassHelper.make(Deprecated, false)))
        expected.addProperty(pNode)
        expected.addMethod(new MethodNode(
                "myMethod",
                ACC_PUBLIC,
                ClassHelper.make(String, false),
                [new Parameter(ClassHelper.make(String, false), "parameter")] as Parameter[],
                [ClassHelper.make(IOException, false)] as ClassNode[],
                new BlockStatement(
                        [new ReturnStatement(new ConstantExpression('some result'))], new VariableScope()
                )))
        expected.addMethod(new MethodNode(
                "myOtherMethod",
                ACC_PUBLIC,
                ClassHelper.make(String, false),
                [] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                        [new ReturnStatement(new ConstantExpression('some other result'))], new VariableScope()
                )))
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testGenericsType_WithLowerBounds() {
        // class MyClass<T, U extends Number> {}

        def result = new AstBuilder().buildFromSpec {
            classNode 'MyClass', ACC_PUBLIC, {
                classNode Object        //superclass
                interfaces {
                    classNode GroovyObject
                }
                mixins {}
                genericsTypes {
                    genericsType Object
                    genericsType Number, {
                        upperBound {
                            classNode Number        //upper bound 1
                            classNode Comparable    //upper bound 2
                        }
                        lowerBound Integer
                    }
                }
            }
        }

        def expected = new ClassNode(
                "MyClass", ACC_PUBLIC, ClassHelper.make(Object, false)
        )
        expected.setGenericsTypes(
                [
                        new GenericsType(ClassHelper.make(Object, false)),
                        new GenericsType(
                                ClassHelper.make(Number, false),
                                [ClassHelper.make(Number, false), ClassHelper.make(Comparable, false)] as ClassNode[],
                                ClassHelper.make(Integer, false)),
                ] as GenericsType[]
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testImportNode() {
        // what source will trigger this node?
        def result = new AstBuilder().buildFromSpec {
            importNode String, "string"
            importNode Integer
        }

        def expected = [
                new ImportNode(ClassHelper.make(String, false), "string"),
                new ImportNode(ClassHelper.make(Integer, false), null)
        ]

        AstAssert.assertSyntaxTree(expected, result)
    }

    void testMethodNode() {
        /*
          @Override
          public String myMethod(String parameter) throws Exception, IOException {
            'some result'
          }
        }
         */

        def result = new AstBuilder().buildFromSpec {
            method('myMethod', ACC_PUBLIC, String) {
                parameters {
                    parameter 'parameter': String.class
                }
                exceptions {
                    classNode Exception
                    classNode IOException
                }
                block {
                    returnStatement {
                        constant 'some result'
                    }
                }
                annotations {
                    annotation Override
                }
            }
        }

        def expected = new MethodNode(
                "myMethod",
                ACC_PUBLIC,
                ClassHelper.make(String, false),
                [new Parameter(ClassHelper.make(String, false), "parameter")] as Parameter[],
                [ClassHelper.make(Exception, false), ClassHelper.make(IOException, false)] as ClassNode[],
                new BlockStatement(
                        [new ReturnStatement(
                                new ConstantExpression('some result')
                        )],
                        new VariableScope()
                ))
        expected.addAnnotation(new AnnotationNode(ClassHelper.make(Override, false)))
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testAnnotation_WithParameter() {
        // @org.junit.Test(timeout=50L) def myMethod() {}
        def result = new AstBuilder().buildFromSpec {
            method 'myMethod', ACC_PUBLIC, Object, {
                parameters {}
                exceptions {}
                block { }
                annotations {
                    annotation(Override) {
                        member 'timeout', {
                            constant 50L
                        }
                    }
                }
            }
        }

        def expected = new MethodNode(
                "myMethod",
                ACC_PUBLIC,
                ClassHelper.make(Object, false),
                [] as Parameter[],
                [] as ClassNode[],
                new BlockStatement([], new VariableScope()))

        def annotation = new AnnotationNode(ClassHelper.make(Override, false))
        annotation.setMember('timeout', new ConstantExpression(50L))
        expected.addAnnotation(annotation)

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMixinNode() {

        // todo: what source code will generate a MixinNode?
        def result = new AstBuilder().buildFromSpec {
            classNode 'MyClass', ACC_PUBLIC, {
                classNode Object        //superclass
                interfaces {
                    classNode GroovyObject
                }
                mixins {
                    mixin "ClassA", ACC_PUBLIC, {
                        classNode String
                    }
                    mixin "ClassB", ACC_PUBLIC, {
                        classNode String
                        interfaces {
                            classNode GroovyObject
                        }
                    }
                }
            }
        }

        def expected = new ClassNode(
                "MyClass", ACC_PUBLIC,
                ClassHelper.make(Object, false),
                [ClassHelper.make(GroovyObject, false)] as ClassNode[],
                [
                        new MixinNode("ClassA", ACC_PUBLIC, ClassHelper.make(String, false)),
                        new MixinNode(
                                "ClassB",
                                ACC_PUBLIC,
                                ClassHelper.make(String, false),
                                [ClassHelper.make(GroovyObject, false)] as ClassNode[]), // interfaces
                ] as MixinNode[]
        )

        AstAssert.assertSyntaxTree([expected], result)
    }

    void testModuleNode() {
        // todo: what source code creates a ModuleNode? ModuleNode has a ton of setters that aren't currently being tested. Should this even be part of the DSL? 
    }

    void testPropertyNode() {
        //  def myField = "foo"
        def result = new AstBuilder().buildFromSpec {
            propertyNode "MY_VALUE", ACC_PUBLIC, String, this.class, {
                constant "foo"
            }
        }

        def expected = new PropertyNode(
                "MY_VALUE",
                ACC_PUBLIC,
                ClassHelper.make(String, false),
                ClassHelper.make(this.class, false),
                new ConstantExpression("foo"),
                null,
                null        //todo: do we need to support getter and setter blocks?
        )
        AstAssert.assertSyntaxTree([expected], result)
    }

    void testMethodCallContract_TooManyArguments() {

        def msg = shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                methodCall {
                    variable "this"
                    constant "println"
                    argumentList {
                        constant "Hello"
                    }
                    constant "illegal value"
                }
            }
        }
        assertEquals("Wrong exception message",
                "methodCall could not be invoked. Expected to receive parameters [class org.codehaus.groovy.ast.expr.Expression, class org.codehaus.groovy.ast.expr.Expression, class org.codehaus.groovy.ast.expr.Expression] but found [class org.codehaus.groovy.ast.expr.VariableExpression, class org.codehaus.groovy.ast.expr.ConstantExpression, class org.codehaus.groovy.ast.expr.ArgumentListExpression, class org.codehaus.groovy.ast.expr.ConstantExpression]",
                msg)
    }

    void testMethodCallContract_TooFewArguments() {

        def msg = shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                methodCall {
                    variable "this"
                    constant "println"
                    // missing argument list!
                }
            }
        }
        assertEquals("Wrong exception message",
                "methodCall could not be invoked. Expected to receive parameters [class org.codehaus.groovy.ast.expr.Expression, class org.codehaus.groovy.ast.expr.Expression, class org.codehaus.groovy.ast.expr.Expression] but found [class org.codehaus.groovy.ast.expr.VariableExpression, class org.codehaus.groovy.ast.expr.ConstantExpression]",
                msg)
    }

    void testAnnotationConstantExpressionContract_TooFewArguments() {

        shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                annotationConstant {
                    // missing argument
                }
            }
        }
    }

    void testAnnotationConstantExpressionContract_TooManyArguments() {

        shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                annotationConstant {
                    annotation Override
                    constant 'illegal parameter'
                }
            }
        }
    }

    void testConstructorCallExpressionContract_TooFewParameters() {

        shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                constructorCall(Integer) {
                    // missing argument list
                }
            }
        }
    }

    void testConstructorCallExpressionContract_TooManyParameters() {

        shouldFail(IllegalArgumentException) {
            new AstBuilder().buildFromSpec {
                constructorCall(Integer) {
                    argumentList {
                        constant 4
                    }
                    constant 'illegal argument'
                }
            }
        }
    }
}
