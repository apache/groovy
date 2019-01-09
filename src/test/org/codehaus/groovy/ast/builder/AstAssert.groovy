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

import org.junit.Assert

/**
 * Some useful AST assertion methods.
 */
class AstAssert {

    /**
    * Support for new assertion types can be added by adding a Map<String, Closure> entry. 
    */ 
    private static Map<Object, Closure> ASSERTION_MAP = [
            BlockStatement : { expected, actual ->
                assertSyntaxTree(expected.statements, actual.statements) 
            },
            AttributeExpression : { expected, actual ->
                assertSyntaxTree([expected.objectExpression], [actual.objectExpression])
                assertSyntaxTree([expected.property], [actual.property])
            },
            ExpressionStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            BitwiseNegationExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            CastExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                Assert.assertEquals("Wrong type", expected.type, actual.type)
            },
            ClosureExpression : { expected, actual ->
                assertSyntaxTree([expected.parameters], [actual.parameters])
                assertSyntaxTree([expected.code], [actual.code])
            },
            ConstantExpression : { expected, actual ->
                Assert.assertEquals("Wrong constant", expected.value, actual.value)
            },
            ArrayExpression : { expected, actual ->
                Assert.assertEquals("Wrong array type", expected.elementType, actual.elementType)
                Assert.assertEquals("Wrong # ast nodes", expected.expressions.size(), actual.expressions.size())
                expected.expressions.eachWithIndex { element, index ->
                    assertSyntaxTree([element], [actual.expressions[index]])
                }
            },
            ListExpression : { expected, actual ->
                Assert.assertEquals("Wrong # ast nodes", expected.expressions.size(), actual.expressions.size())
                expected.expressions.eachWithIndex { element, index ->
                    assertSyntaxTree([element], [actual.expressions[index]])
                }
            },
            DeclarationExpression : { expected, actual ->
                Assert.assertEquals("Wrong token", expected.operation.text, actual.operation.text)
                assertSyntaxTree([expected.leftExpression], [actual.leftExpression])
                assertSyntaxTree([expected.rightExpression], [actual.rightExpression])
            },
            VariableExpression : { expected, actual ->
                Assert.assertEquals("Wrong variable", expected.variable, actual.variable)
            },
            ReturnStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            ArgumentListExpression : { expected, actual ->
                assertSyntaxTree(expected.expressions, actual.expressions)
            },
            AnnotationConstantExpression : { expected, actual ->
                assertSyntaxTree([expected.value], [actual.value])
            },
            MethodCallExpression : { expected, actual ->
                assertSyntaxTree([expected.objectExpression], [actual.objectExpression])
                assertSyntaxTree([expected.method], [actual.method])
                assertSyntaxTree([expected.arguments], [actual.arguments])
            },
            AnnotationNode : { expected, actual ->
                assertSyntaxTree([expected.classNode], [actual.classNode])
                //    Map<String, Expression>
                Assert.assertEquals("Wrong keyset", expected.members.keySet(), actual.members.keySet())

                Assert.assertEquals("Wrong # members", expected.members.size(), actual.members.size())
                expected.members.each { key, value ->
                    Assert.assertTrue("Missing key $key", actual.members.containsKey(key))
                    assertSyntaxTree([expected.members[key]], [actual.members[key]])
                }
            },
            ClassNode : { expected, actual ->
                if (expected.superClass) {
                    Assert.assertEquals("Wrong superClass type", expected.superClass.getName(), actual.superClass.getName())
                } else {
                    Assert.assertEquals("Wrong class type", expected.class.getName(), actual.class.getName())
                }
                assertSyntaxTree([expected.mixins], [actual.mixins])

                // do NOT assert module property b/c it is a circular reference, causes stack overflow
                //assertSyntaxTree([expected.module], [actual.module])
                assertSyntaxTree(expected.genericsTypes, actual.genericsTypes)
            },
            IfStatement : { expected, actual ->
                assertSyntaxTree([expected.booleanExpression], [actual.booleanExpression])
                assertSyntaxTree([expected.ifBlock], [actual.ifBlock])
                assertSyntaxTree([expected.elseBlock], [actual.elseBlock])
            },
            BooleanExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            BinaryExpression : { expected, actual ->
                assertSyntaxTree([expected.leftExpression], [actual.leftExpression])
                assertSyntaxTree([expected.rightExpression], [actual.rightExpression])
                assertSyntaxTree([expected.operation], [actual.operation])
            },
            Token : { expected, actual ->
                Assert.assertEquals("Wrong token type", expected.type, actual.type)
                Assert.assertEquals("Wrong token text", expected.text, actual.text)
            },
            Parameter : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.defaultValue], [actual.defaultValue])
                Assert.assertEquals("Wrong parameter name", expected.name, actual.name)
                Assert.assertEquals("Wrong 'hasDefaultValue'", expected.hasDefaultValue, actual.hasDefaultValue)
                
            },
            ConstructorCallExpression : { expected, actual ->
                assertSyntaxTree([expected.arguments], [actual.arguments])
            },
            NotExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            PostfixExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.operation], [actual.operation])
            },
            PrefixExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.operation], [actual.operation])
            },
            UnaryPlusExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            UnaryMinusExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            ClassExpression : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
            },
            TupleExpression : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree(expected.expressions, actual.expressions)
            },
            FieldExpression : { expected, actual ->
                assertSyntaxTree([expected.field], [actual.field])
            },
            FieldNode : { expected, actual ->
                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.owner], [actual.owner])
                assertSyntaxTree([expected.initialValueExpression], [actual.initialValueExpression])
            },
            MapExpression : { expected, actual ->
                assertSyntaxTree(expected.mapEntryExpressions, actual.mapEntryExpressions)
            },
            MapEntryExpression : { expected, actual ->
                assertSyntaxTree([expected.keyExpression], [actual.keyExpression])
                assertSyntaxTree([expected.valueExpression], [actual.valueExpression])
            },
            GStringExpression : { expected, actual ->
                Assert.assertEquals("Wrong text", expected.verbatimText, actual.verbatimText)
                assertSyntaxTree(expected.strings, actual.strings)
                assertSyntaxTree(expected.values, actual.values)
            },
            MethodPointerExpression : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.methodName], [actual.methodName])
            },
            RangeExpression : { expected, actual ->
                assertSyntaxTree([expected.from], [actual.from])
                assertSyntaxTree([expected.to], [actual.to])
                Assert.assertEquals("Wrong inclusive", expected.inclusive, actual.inclusive)
            },
            PropertyExpression : { expected, actual ->
                assertSyntaxTree([expected.objectExpression], [actual.objectExpression])
                assertSyntaxTree([expected.property], [actual.property])
            },
            SwitchStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.defaultStatement], [actual.defaultStatement])
                assertSyntaxTree(expected.caseStatements, actual.caseStatements)
            },
            CaseStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.code], [actual.code])
            },
            EmptyStatement : { expected, actual ->
                // always successful
            },
            BreakStatement : { expected, actual ->
                Assert.assertEquals("Wrong label", expected.label, actual.label)
            },
            AssertStatement : { expected, actual ->
                assertSyntaxTree([expected.booleanExpression], [actual.booleanExpression])
                assertSyntaxTree([expected.messageExpression], [actual.messageExpression])
            },
            SynchronizedStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
                assertSyntaxTree([expected.code], [actual.code])
            },
            TryCatchStatement : { expected, actual ->
                assertSyntaxTree([expected.tryStatement], [actual.tryStatement])
                assertSyntaxTree([expected.finallyStatement], [actual.finallyStatement])
                assertSyntaxTree(expected.catchStatements, actual.catchStatements)
            },
            CatchStatement : { expected, actual ->
                assertSyntaxTree([expected.variable], [actual.variable])
                assertSyntaxTree([expected.code], [actual.code])
            },
            ThrowStatement : { expected, actual ->
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            StaticMethodCallExpression : { expected, actual ->
                Assert.assertEquals("Wrong method", expected.method, actual.method)
                assertSyntaxTree([expected.ownerType], [actual.ownerType])
                assertSyntaxTree([expected.arguments], [actual.arguments])
            },
            ForStatement : { expected, actual ->
                assertSyntaxTree([expected.variable], [actual.variable])
                assertSyntaxTree([expected.collectionExpression], [actual.collectionExpression])
                assertSyntaxTree([expected.loopBlock], [actual.loopBlock])
            },
            ClosureListExpression : { expected, actual ->
                assertSyntaxTree(expected.expressions, actual.expressions)
            },
            WhileStatement : { expected, actual ->
                assertSyntaxTree([expected.booleanExpression], [actual.booleanExpression])
                assertSyntaxTree([expected.loopBlock], [actual.loopBlock])
            },
            ContinueStatement : { expected, actual ->
                Assert.assertEquals("Wrong label", expected.label, actual.label)
            },
            TernaryExpression : { expected, actual ->
                assertSyntaxTree([expected.booleanExpression], [actual.booleanExpression])
                assertSyntaxTree([expected.trueExpression], [actual.trueExpression])
                assertSyntaxTree([expected.falseExpression], [actual.falseExpression])
            },
            ElvisOperatorExpression : { expected, actual ->
                assertSyntaxTree([expected.booleanExpression], [actual.booleanExpression])
                assertSyntaxTree([expected.trueExpression], [actual.trueExpression])
                assertSyntaxTree([expected.falseExpression], [actual.falseExpression])
            },
            PropertyNode : { expected, actual ->
                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.field], [actual.field])
                assertSyntaxTree([expected.getterBlock], [actual.getterBlock])
                assertSyntaxTree([expected.setterBlock], [actual.setterBlock])
            },
            NullObject : { expected, actual ->
                Assert.assertNull(expected)
                Assert.assertNull(actual)
            },
            MethodNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.returnType], [actual.returnType])
                assertSyntaxTree([expected.code], [actual.code])
                assertSyntaxTree(expected.parameters, actual.parameters)
                assertSyntaxTree(expected.exceptions, actual.exceptions)

                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
            },
            ConstructorNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.returnType], [actual.returnType])
                assertSyntaxTree([expected.code], [actual.code])
                assertSyntaxTree(expected.parameters, actual.parameters)
                assertSyntaxTree(expected.exceptions, actual.exceptions)

                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
            },
            ImportNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.type], [actual.type])
                Assert.assertEquals("Wrong alias", expected.alias, actual.alias)
            },
            RegexExpression : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.string], [actual.string])
            },
            SpreadExpression : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            SpreadMapExpression : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.expression], [actual.expression])
            },
            GenericsType : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.lowerBound], [actual.lowerBound])
                assertSyntaxTree(expected.upperBounds, actual.upperBounds)
                Assert.assertEquals("Wrong wildcard", expected.name, actual.name)
                Assert.assertEquals("Wrong wildcard", expected.wildcard, actual.wildcard)
            },
            NamedArgumentListExpression : { expected, actual ->
                assertSyntaxTree(expected.mapEntryExpressions, actual.mapEntryExpressions)
            },
            MixinNode : { expected, actual ->
                assertSyntaxTree([expected.superClass], [actual.superClass])
                assertSyntaxTree([expected.interfaces], [actual.interfaces])
                assertSyntaxTree([expected.mixins], [actual.mixins])
                assertSyntaxTree([expected.module], [actual.module])
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree(expected.genericsTypes, actual.genericsTypes)

                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
            },
            InnerClassNode : { expected, actual ->
                assertSyntaxTree([expected.outerClass], [actual.outerClass])
                assertSyntaxTree([expected.superClass], [actual.superClass])
                assertSyntaxTree([expected.interfaces], [actual.interfaces])
                assertSyntaxTree([expected.mixins], [actual.mixins])
                assertSyntaxTree([expected.module], [actual.module])
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree(expected.genericsTypes, actual.genericsTypes)

                Assert.assertEquals("Wrong name", expected.name, actual.name)
                Assert.assertEquals("Wrong modifiers", expected.modifiers, actual.modifiers)
            },
    ]

    /**
     * Assertion statement to compare abstract syntax trees.
     * @param expected
     *      the list or array of ASTNodes expected to be present
     * @param actual
     *      the actual list or array of ASTNodes received
     */
    static void assertSyntaxTree(expected, actual) {
        if (expected == null && actual == null) return
        
        if (actual == null || expected == null || expected.size() != actual?.size()) {
            Assert.fail("AST comparison failure. \nExpected $expected \nReceived $actual")
        }
        expected.eachWithIndex { item, index ->
            if (item.getClass().isArray() && actual[index].getClass().isArray()) {
                assertSyntaxTree(item, actual[index])
            } else {
                Assert.assertEquals("Wrong type in AST Node", item.getClass(), actual[index].getClass())

                if (ASSERTION_MAP.containsKey(item.getClass().getSimpleName())) {
                    Closure assertion = ASSERTION_MAP.get(item.getClass().getSimpleName())
                    assertion(item, actual[index])
                } else {
                    Assert.fail("Unexpected type: ${item.getClass()} Update the unit test!")
                }
            }
        }
    }

}
