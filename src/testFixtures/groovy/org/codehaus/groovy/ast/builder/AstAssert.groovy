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

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

/**
 * Some useful AST assertion methods.
 */
final class AstAssert {

    private AstAssert() {}

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
                assertEquals(expected.type, actual.type, "Wrong type")
            },
            ClosureExpression : { expected, actual ->
                assertSyntaxTree([expected.parameters], [actual.parameters])
                assertSyntaxTree([expected.code], [actual.code])
            },
            ConstantExpression : { expected, actual ->
                assertEquals(expected.value, actual.value, "Wrong constant")
            },
            ArrayExpression : { expected, actual ->
                assertEquals(expected.elementType, actual.elementType, "Wrong array type")
                assertEquals(expected.expressions.size(), actual.expressions.size(), "Wrong node count")
                expected.expressions.eachWithIndex { element, index ->
                    assertSyntaxTree([element], [actual.expressions[index]])
                }
            },
            ListExpression : { expected, actual ->
                assertEquals(expected.expressions.size(), actual.expressions.size(), "Wrong node count")
                expected.expressions.eachWithIndex { element, index ->
                    assertSyntaxTree([element], [actual.expressions[index]])
                }
            },
            DeclarationExpression : { expected, actual ->
                assertEquals(expected.operation.text, actual.operation.text, "Wrong token")
                assertSyntaxTree([expected.leftExpression], [actual.leftExpression])
                assertSyntaxTree([expected.rightExpression], [actual.rightExpression])
            },
            VariableExpression : { expected, actual ->
                assertEquals(expected.variable, actual.variable, "Wrong variable")
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
                assertEquals(expected.members.keySet(), actual.members.keySet(), "Wrong keyset")

                assertEquals(expected.members.size(), actual.members.size(), "Wrong member count")
                expected.members.each { key, value ->
                    assertTrue(actual.members.containsKey(key), "Missing key $key")
                    assertSyntaxTree([expected.members[key]], [actual.members[key]])
                }
            },
            ClassNode : { expected, actual ->
                if (expected.superClass) {
                    assertEquals(expected.superClass.getName(), actual.superClass.getName(), "Wrong superClass type")
                } else {
                    assertEquals(expected.class.getName(), actual.class.getName(), "Wrong class type")
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
                assertEquals(expected.type, actual.type, "Wrong token type")
                assertEquals(expected.text, actual.text, "Wrong token text")
            },
            Parameter : { expected, actual ->
                assertSyntaxTree([expected.type], [actual.type])
                assertSyntaxTree([expected.defaultValue], [actual.defaultValue])
                assertEquals(expected.name, actual.name, "Wrong parameter name")
                assertEquals(expected.hasDefaultValue, actual.hasDefaultValue, "Wrong 'hasDefaultValue'")
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
                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
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
                assertEquals(expected.verbatimText, actual.verbatimText, "Wrong text")
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
                assertEquals(expected.inclusive, actual.inclusive, "Wrong inclusive")
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
                assertEquals(expected.label, actual.label, "Wrong label")
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
                assertEquals(expected.method, actual.method, "Wrong method")
                assertSyntaxTree([expected.ownerType], [actual.ownerType])
                assertSyntaxTree([expected.arguments], [actual.arguments])
            },
            ForStatement : { expected, actual ->
                assert expected.valueVariable.asBoolean() == actual.valueVariable.asBoolean()
                if (expected.valueVariable) {
                    assertSyntaxTree([expected.valueVariable], [actual.valueVariable])
                    assert expected.indexVariable.asBoolean() == actual.indexVariable.asBoolean()
                    if (expected.indexVariable) {
                        assertSyntaxTree([expected.indexVariable], [actual.indexVariable])
                    }
                }
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
                assertEquals(expected.label, actual.label, "Wrong label")
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
                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.field], [actual.field])
                assertSyntaxTree([expected.getterBlock], [actual.getterBlock])
                assertSyntaxTree([expected.setterBlock], [actual.setterBlock])
            },
            NullObject : { expected, actual ->
                assertNull(expected)
                assertNull(actual)
            },
            MethodNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.returnType], [actual.returnType])
                assertSyntaxTree([expected.code], [actual.code])
                assertSyntaxTree(expected.parameters, actual.parameters)
                assertSyntaxTree(expected.exceptions, actual.exceptions)

                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
            },
            ConstructorNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.returnType], [actual.returnType])
                assertSyntaxTree([expected.code], [actual.code])
                assertSyntaxTree(expected.parameters, actual.parameters)
                assertSyntaxTree(expected.exceptions, actual.exceptions)

                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
            },
            ImportNode : { expected, actual ->
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree([expected.type], [actual.type])
                assertEquals(expected.alias, actual.alias, "Wrong alias")
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
                assertEquals(expected.name, actual.name, "Wrong wildcard")
                assertEquals(expected.wildcard, actual.wildcard, "Wrong wildcard")
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

                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
            },
            InnerClassNode : { expected, actual ->
                assertSyntaxTree([expected.outerClass], [actual.outerClass])
                assertSyntaxTree([expected.superClass], [actual.superClass])
                assertSyntaxTree([expected.interfaces], [actual.interfaces])
                assertSyntaxTree([expected.mixins], [actual.mixins])
                assertSyntaxTree([expected.module], [actual.module])
                assertSyntaxTree(expected.annotations, actual.annotations)
                assertSyntaxTree(expected.genericsTypes, actual.genericsTypes)

                assertEquals(expected.name, actual.name, "Wrong name")
                assertEquals(expected.modifiers, actual.modifiers, "Wrong modifiers")
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
            fail("AST comparison failure. \nExpected $expected \nReceived $actual")
        }
        expected.eachWithIndex { item, index ->
            if (item.getClass().isArray() && actual[index].getClass().isArray()) {
                assertSyntaxTree(item, actual[index])
            } else {
                assertEquals(item.getClass(), actual[index].getClass(), "Wrong type in AST Node")

                Class itemType = item.getClass()
                if (itemType.isAnonymousClass()) {
                    itemType = itemType.getSuperclass()
                }
                if (ASSERTION_MAP.containsKey(itemType.getSimpleName())) {
                    Closure assertion = ASSERTION_MAP.get(itemType.getSimpleName())
                    assertion(item, actual[index])
                } else {
                    fail("Unexpected type: ${itemType} Update the unit test!")
                }
            }
        }
    }
}
