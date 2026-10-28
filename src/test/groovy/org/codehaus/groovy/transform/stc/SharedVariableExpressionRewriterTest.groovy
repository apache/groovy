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
package org.codehaus.groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.SwitchExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.ast.stmt.YieldStatement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.classgen.BytecodeSequence
import org.codehaus.groovy.syntax.Token
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Drives every branch of the shared this/super rewriter. The sentinels are
 * process-wide, so these tests build AST directly instead of going through
 * the parser.
 */
final class SharedVariableExpressionRewriterTest {

    private static final Expression THIS = VariableExpression.THIS_EXPRESSION
    private static final Expression SUPER = VariableExpression.SUPER_EXPRESSION

    // GROOVY-12441
    @Test
    void testNullNodesAndParameterShapes() {
        SharedVariableExpressionRewriter.rewrite((MethodNode) null)
        SharedVariableExpressionRewriter.rewrite((FieldNode) null)
        SharedVariableExpressionRewriter.rewrite((PropertyNode) null)
        SharedVariableExpressionRewriter.rewriteInitializers(null)

        Parameter withDefault = new Parameter(OBJECT_TYPE, 'b', THIS)
        MethodNode bare = method('bare', null)
        setParameters(bare, [null, new Parameter(OBJECT_TYPE, 'a'), withDefault] as Parameter[])
        SharedVariableExpressionRewriter.rewrite(bare)
        assertCopiedThis(withDefault.initialExpression)
        assertNull(bare.code)

        ClassNode owner = new ClassNode('C', Modifier.PUBLIC, OBJECT_TYPE)
        FieldNode noInit = new FieldNode('a', Modifier.PUBLIC, OBJECT_TYPE, owner, null)
        ConstantExpression constantInit = new ConstantExpression('x')
        FieldNode sameInit = new FieldNode('b', Modifier.PUBLIC, OBJECT_TYPE, owner, constantInit)
        FieldNode sharedInit = new FieldNode('c', Modifier.PUBLIC, OBJECT_TYPE, owner, THIS)
        SharedVariableExpressionRewriter.rewrite(noInit)
        SharedVariableExpressionRewriter.rewrite(sameInit)
        SharedVariableExpressionRewriter.rewrite(sharedInit)
        assertNull(noInit.initialExpression)
        assertSame(constantInit, sameInit.initialExpression)
        assertCopiedThis(sharedInit.initialExpression)

        BlockStatement getter = new BlockStatement([new ExpressionStatement(THIS)], new VariableScope())
        BlockStatement setter = new BlockStatement([new ReturnStatement(SUPER)], new VariableScope())
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('p', Modifier.PUBLIC, OBJECT_TYPE, owner, null, null, null))
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('g', Modifier.PUBLIC, OBJECT_TYPE, owner, null, getter, null))
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('s', Modifier.PUBLIC, OBJECT_TYPE, owner, null, null, setter))
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('both', Modifier.PUBLIC, OBJECT_TYPE, owner, null, getter, setter))
        assertCopiedThis(((ExpressionStatement) getter.statements[0]).expression)
        assertCopiedSuper(((ReturnStatement) setter.statements[0]).expression)

        ExpressionStatement initializer = new ExpressionStatement(THIS)
        owner.objectInitializerStatements << null
        owner.objectInitializerStatements << initializer
        SharedVariableExpressionRewriter.rewriteInitializers(owner)
        assertCopiedThis(initializer.expression)
    }

    // GROOVY-12441
    @Test
    void testEveryExpressionAndStatementShape() {
        ConstantExpression keep = new ConstantExpression('k')
        VariableExpression local = new VariableExpression('local')
        ExpressionStatement nullExpr = new ExpressionStatement(keep)
        nullExpr.expression = null

        MethodCallExpression thisCall = call(THIS, keep)
        MethodCallExpression superCall = call(SUPER, keep)
        MethodCallExpression plainCall = call(local, keep)
        MethodCallExpression dynamicName = new MethodCallExpression(keep, THIS, new ArgumentListExpression())
        ArgumentListExpression args = new ArgumentListExpression()
        args.expressions << null
        args.addExpression(THIS)
        args.addExpression(keep)
        MethodCallExpression callWithArgs = new MethodCallExpression(local, new ConstantExpression('m'), args)

        ConstructorCallExpression ctor = new ConstructorCallExpression(OBJECT_TYPE, new ArgumentListExpression(THIS))
        StaticMethodCallExpression staticCall = new StaticMethodCallExpression(OBJECT_TYPE, 'valueOf', new ArgumentListExpression(THIS))
        StaticMethodCallExpression staticSame = new StaticMethodCallExpression(OBJECT_TYPE, 'valueOf', new ArgumentListExpression(keep))

        // getText() starting with "::" while the receiver is the this sentinel.
        // MethodReferenceExpression itself prints "this::m", so a subclass is required.
        MethodPointerExpression colonText = new MethodPointerExpression(null, new ConstantExpression('m')) {
            @Override
            String getText() { '::m' }
        }
        MethodReferenceExpression colonRef = new MethodReferenceExpression(null, THIS) {
            @Override
            String getText() { '::m' }
        }
        ExpressionStatement colonRefStmt = stmt(colonRef)
        MethodPointerExpression implicitPointer = new MethodPointerExpression(null, new ConstantExpression('m'))
        MethodReferenceExpression implicitReference = new MethodReferenceExpression(null, new ConstantExpression('m'))
        MethodPointerExpression storedPointer = new MethodPointerExpression(THIS, new ConstantExpression('m'))
        MethodReferenceExpression storedReference = new MethodReferenceExpression(THIS, new ConstantExpression('m'))
        MethodPointerExpression unchangedPointer = new MethodPointerExpression(local, new ConstantExpression('m'))
        MethodReferenceExpression unchangedReference = new MethodReferenceExpression(local, new ConstantExpression('m'))
        MethodPointerExpression implicitNamedThis = new MethodPointerExpression(null, THIS)
        MethodReferenceExpression implicitNamedSuper = new MethodReferenceExpression(null, SUPER)
        MethodPointerExpression receiverSameNameChanges = new MethodPointerExpression(local, THIS)
        ExpressionStatement storedPointerStmt = stmt(storedPointer)
        ExpressionStatement storedReferenceStmt = stmt(storedReference)
        ExpressionStatement implicitNamedThisStmt = stmt(implicitNamedThis)
        ExpressionStatement implicitNamedSuperStmt = stmt(implicitNamedSuper)
        ExpressionStatement receiverSameNameChangesStmt = stmt(receiverSameNameChanges)

        BinaryExpression leftShared = bin(THIS, keep)
        BinaryExpression rightShared = bin(keep, SUPER)
        BinaryExpression binarySame = bin(keep, local)

        PropertyExpression propertyReceiver = new PropertyExpression(THIS, 'name')
        PropertyExpression propertySame = new PropertyExpression(keep, 'name')
        PropertyExpression propertyNamedThis = flaggedProperty(false, true)
        AttributeExpression attributeNamedThis = (AttributeExpression) flaggedProperty(true, false)
        ExpressionStatement propertyNamedThisStmt = stmt(propertyNamedThis)
        ExpressionStatement attributeNamedThisStmt = stmt(attributeNamedThis)

        ElvisOperatorExpression elvisSame = new ElvisOperatorExpression(keep, local)
        ExpressionStatement elvisSameStmt = stmt(elvisSame)
        ExpressionStatement elvisTrue = stmt(new ElvisOperatorExpression(THIS, keep))
        ExpressionStatement elvisFalse = stmt(new ElvisOperatorExpression(keep, THIS))
        ExpressionStatement elvisBoth = stmt(new ElvisOperatorExpression(THIS, SUPER))

        TernaryExpression ternarySame = tern(new BooleanExpression(keep), keep, local)
        ExpressionStatement ternarySameStmt = stmt(ternarySame)
        ExpressionStatement ternCond = stmt(tern(new BooleanExpression(THIS), keep, local))
        ExpressionStatement ternTrue = stmt(tern(new BooleanExpression(keep), THIS, local))
        ExpressionStatement ternFalse = stmt(tern(new BooleanExpression(keep), local, THIS))

        ExpressionStatement notShared = stmt(new NotExpression(THIS))
        NotExpression notSame = new NotExpression(keep)
        ExpressionStatement notSameStmt = stmt(notSame)
        ExpressionStatement boolShared = stmt(new BooleanExpression(THIS))
        BooleanExpression boolSame = new BooleanExpression(keep)
        ExpressionStatement boolSameStmt = stmt(boolSame)

        CastExpression castShared = new CastExpression(STRING_TYPE, THIS)
        CastExpression castSame = new CastExpression(STRING_TYPE, keep)
        PrefixExpression prefixShared = new PrefixExpression(Token.newSymbol('++', -1, -1), THIS)
        PrefixExpression prefixSame = new PrefixExpression(Token.newSymbol('++', -1, -1), local)
        PostfixExpression postfixShared = new PostfixExpression(SUPER, Token.newSymbol('++', -1, -1))
        PostfixExpression postfixSame = new PostfixExpression(local, Token.newSymbol('++', -1, -1))

        ExpressionStatement minusShared = stmt(new UnaryMinusExpression(THIS))
        UnaryMinusExpression minusSame = new UnaryMinusExpression(keep)
        ExpressionStatement minusSameStmt = stmt(minusSame)
        ExpressionStatement plusShared = stmt(new UnaryPlusExpression(THIS))
        UnaryPlusExpression plusSame = new UnaryPlusExpression(keep)
        ExpressionStatement plusSameStmt = stmt(plusSame)
        ExpressionStatement negateShared = stmt(new BitwiseNegationExpression(THIS))
        BitwiseNegationExpression negateSame = new BitwiseNegationExpression(keep)
        ExpressionStatement negateSameStmt = stmt(negateSame)
        ExpressionStatement spreadShared = stmt(new SpreadExpression(THIS))
        SpreadExpression spreadSame = new SpreadExpression(keep)
        ExpressionStatement spreadSameStmt = stmt(spreadSame)
        ExpressionStatement spreadMapShared = stmt(new SpreadMapExpression(THIS))
        SpreadMapExpression spreadMapSame = new SpreadMapExpression(keep)
        ExpressionStatement spreadMapSameStmt = stmt(spreadMapSame)

        RangeExpression rangeSame = new RangeExpression(keep, local, true)
        ExpressionStatement rangeSameStmt = stmt(rangeSame)
        ExpressionStatement rangeFrom = stmt(new RangeExpression(THIS, keep, false, true))
        ExpressionStatement rangeTo = stmt(new RangeExpression(keep, SUPER, true, false))

        SwitchExpression switchFull = new SwitchExpression(THIS, [new CaseStatement(keep, new ReturnStatement(THIS))], new YieldStatement(SUPER))
        SwitchExpression switchNoDefault = new SwitchExpression(keep, [], null)

        GStringExpression gstringEmpty = new GStringExpression('only')
        GStringExpression gstring = new GStringExpression('${x}')
        gstring.values << null
        gstring.values << THIS
        gstring.values << keep

        TupleExpression emptyTuple = new TupleExpression()
        TupleExpression tuple = new TupleExpression()
        tuple.expressions << null
        tuple.addExpression(THIS)
        tuple.addExpression(keep)

        MapExpression map = new MapExpression()
        map.mapEntryExpressions << null
        map.addMapEntryExpression(new MapEntryExpression(THIS, keep))
        map.addMapEntryExpression(new MapEntryExpression(keep, SUPER))
        map.addMapEntryExpression(new MapEntryExpression(keep, local))

        ListExpression list = new ListExpression()
        list.expressions << null
        list.addExpression(THIS)
        list.addExpression(keep)

        ArrayExpression arrayInit = new ArrayExpression(STRING_TYPE, [keep, THIS])
        ArrayExpression arraySize = new ArrayExpression(int_TYPE, null, [THIS, keep])

        Parameter closureDefault = new Parameter(OBJECT_TYPE, 'b', THIS)
        ClosureExpression richClosure = new ClosureExpression(
                [null, new Parameter(OBJECT_TYPE, 'a'), closureDefault] as Parameter[],
                new BlockStatement([new ReturnStatement(THIS)], new VariableScope()))
        ClosureExpression implicitClosure = new ClosureExpression(null, new ExpressionStatement(SUPER))
        ClosureExpression emptyClosure = new ClosureExpression(Parameter.EMPTY_ARRAY, null)

        IfStatement ifStmt = new IfStatement(new BooleanExpression(THIS), new ExpressionStatement(SUPER), EmptyStatement.INSTANCE)
        WhileStatement whileStmt = new WhileStatement(new BooleanExpression(keep), new ExpressionStatement(THIS))
        DoWhileStatement doStmt = new DoWhileStatement(new BooleanExpression(THIS), EmptyStatement.INSTANCE)
        ForStatement forStmt = new ForStatement(new Parameter(OBJECT_TYPE, 's'), THIS, new ExpressionStatement(SUPER))
        SynchronizedStatement syncStmt = new SynchronizedStatement(THIS, new ExpressionStatement(keep))
        SwitchStatement switchStmt = new SwitchStatement(THIS, [new CaseStatement(THIS, new ExpressionStatement(keep))], EmptyStatement.INSTANCE)
        AssertStatement assertNoMessage = new AssertStatement(new BooleanExpression(THIS), null)
        AssertStatement assertWithMessage = new AssertStatement(new BooleanExpression(keep), THIS)

        List<Object> instructions = [
                nullExpr,
                stmt(new ClassExpression(OBJECT_TYPE)),
                stmt(new FieldExpression(new FieldNode('f', Modifier.PUBLIC, OBJECT_TYPE, new ClassNode('C', Modifier.PUBLIC, OBJECT_TYPE), null))),
                stmt(EmptyExpression.INSTANCE),
                stmt(BytecodeExpression.NOP),
                stmt(keep),
                stmt(local),
                stmt(THIS),
                stmt(SUPER),
                stmt(new MapEntryExpression(keep, local)),
                thisCall, superCall, plainCall, dynamicName, callWithArgs,
                ctor, staticCall, staticSame,
                storedPointerStmt, storedReferenceStmt, implicitNamedThisStmt, implicitNamedSuperStmt, receiverSameNameChangesStmt,
                implicitPointer, implicitReference, colonText, colonRefStmt, unchangedPointer, unchangedReference,
                stmt(leftShared), stmt(rightShared), stmt(binarySame),
                stmt(propertyReceiver), stmt(propertySame), propertyNamedThisStmt, attributeNamedThisStmt,
                elvisSameStmt, elvisTrue, elvisFalse, elvisBoth,
                ternarySameStmt, ternCond, ternTrue, ternFalse,
                notShared, notSameStmt, boolShared, boolSameStmt,
                stmt(castShared), stmt(castSame),
                stmt(prefixShared), stmt(prefixSame),
                stmt(postfixShared), stmt(postfixSame),
                minusShared, minusSameStmt, plusShared, plusSameStmt,
                negateShared, negateSameStmt, spreadShared, spreadSameStmt,
                spreadMapShared, spreadMapSameStmt,
                rangeSameStmt, rangeFrom, rangeTo,
                switchFull, switchNoDefault,
                gstringEmpty, gstring, emptyTuple, tuple, map, list, arrayInit, arraySize,
                richClosure, implicitClosure, emptyClosure,
                new ReturnStatement(THIS),
                new ThrowStatement(THIS),
                new YieldStatement(SUPER),
                assertNoMessage, assertWithMessage,
                ifStmt, whileStmt, doStmt, forStmt, syncStmt, switchStmt,
                // direct visits, so the expression-visitor overrides run as well
                call(THIS, keep),
                new ConstructorCallExpression(OBJECT_TYPE, new ArgumentListExpression(keep)),
                new StaticMethodCallExpression(STRING_TYPE, 'valueOf', new ArgumentListExpression()),
                bin(keep, local),
                tern(new BooleanExpression(keep), keep, keep),
                new ElvisOperatorExpression(keep, keep),
                new ClosureExpression(null, EmptyStatement.INSTANCE),
                new PropertyExpression(keep, 'n'),
                new AttributeExpression(keep, new ConstantExpression('n'), false),
                new CastExpression(OBJECT_TYPE, keep),
                new TupleExpression(),
                new ListExpression(),
                new ArrayExpression(OBJECT_TYPE, [keep]),
                new MapExpression(),
                new GStringExpression(''),
                new BooleanExpression(keep),
                new NotExpression(keep),
                new PrefixExpression(Token.newSymbol('++', -1, -1), local),
                new PostfixExpression(local, Token.newSymbol('++', -1, -1)),
                new MethodPointerExpression(local, new ConstantExpression('m')),
                new MethodReferenceExpression(local, new ConstantExpression('m')),
                new SwitchExpression(keep, [], EmptyStatement.INSTANCE)
        ]

        SharedVariableExpressionRewriter.rewrite(method('exercise', new BytecodeSequence(instructions)))

        assertNull(nullExpr.expression)
        assertCopiedThis(thisCall.objectExpression)
        assertCopiedSuper(superCall.objectExpression)
        assertSame(local, plainCall.objectExpression)
        assertCopiedThis(dynamicName.method)
        assertNull(args.expressions[0])
        assertCopiedThis(args.expressions[1])
        assertSame(keep, args.expressions[2])
        assertCopiedThis(((ArgumentListExpression) ctor.arguments).expressions[0])
        assertCopiedThis(((ArgumentListExpression) staticCall.arguments).expressions[0])
        assertSame(keep, ((ArgumentListExpression) staticSame.arguments).expressions[0])

        assertTrue(colonText.methodName instanceof ConstantExpression)
        assertCopiedThis(((MethodReferenceExpression) colonRefStmt.expression).methodName)
        assertSame(unchangedPointer.expression, local)
        assertSame(unchangedReference.expression, local)
        assertCopiedThis(((MethodPointerExpression) storedPointerStmt.expression).expression)
        assertCopiedThis(((MethodReferenceExpression) storedReferenceStmt.expression).expression)
        MethodPointerExpression namedPointer = (MethodPointerExpression) implicitNamedThisStmt.expression
        assertTrue(namedPointer.text.startsWith('&'), namedPointer.text)
        assertCopiedThis(namedPointer.methodName)
        MethodReferenceExpression namedReference = (MethodReferenceExpression) implicitNamedSuperStmt.expression
        assertCopiedThis(namedReference.expression)
        assertCopiedSuper(namedReference.methodName)
        assertCopiedThis(((MethodPointerExpression) receiverSameNameChangesStmt.expression).methodName)
        assertSame(local, ((MethodPointerExpression) receiverSameNameChangesStmt.expression).expression)

        assertCopiedThis(leftShared.leftExpression)
        assertCopiedSuper(rightShared.rightExpression)
        assertSame(keep, binarySame.leftExpression)
        assertSame(local, binarySame.rightExpression)
        assertCopiedThis(propertyReceiver.objectExpression)
        assertSame(keep, propertySame.objectExpression)
        assertCopiedThis(((PropertyExpression) propertyNamedThisStmt.expression).property)
        assertFalse(propertyNamedThisStmt.expression instanceof AttributeExpression)
        PropertyExpression rebuiltProperty = (PropertyExpression) propertyNamedThisStmt.expression
        assertTrue(rebuiltProperty.implicitThis)
        assertTrue(rebuiltProperty.spreadSafe)
        assertTrue(rebuiltProperty.isStatic())
        assertCopiedThis(((AttributeExpression) attributeNamedThisStmt.expression).property)
        assertFalse(((AttributeExpression) attributeNamedThisStmt.expression).implicitThis)

        assertSame(elvisSame, elvisSameStmt.expression)
        assertCopiedThis(((ElvisOperatorExpression) elvisTrue.expression).trueExpression)
        assertCopiedThis(((ElvisOperatorExpression) elvisFalse.expression).falseExpression)
        assertCopiedThis(((ElvisOperatorExpression) elvisBoth.expression).trueExpression)
        assertCopiedSuper(((ElvisOperatorExpression) elvisBoth.expression).falseExpression)
        assertSame(ternarySame, ternarySameStmt.expression)
        assertCopiedThis(((TernaryExpression) ternCond.expression).booleanExpression.expression)
        assertCopiedThis(((TernaryExpression) ternTrue.expression).trueExpression)
        assertCopiedThis(((TernaryExpression) ternFalse.expression).falseExpression)

        assertCopiedThis(((NotExpression) notShared.expression).expression)
        assertSame(notSame, notSameStmt.expression)
        assertCopiedThis(((BooleanExpression) boolShared.expression).expression)
        assertSame(boolSame, boolSameStmt.expression)
        assertCopiedThis(castShared.expression)
        assertSame(keep, castSame.expression)
        assertCopiedThis(prefixShared.expression)
        assertSame(local, prefixSame.expression)
        assertCopiedSuper(postfixShared.expression)
        assertSame(local, postfixSame.expression)
        assertCopiedThis(((UnaryMinusExpression) minusShared.expression).expression)
        assertSame(minusSame, minusSameStmt.expression)
        assertCopiedThis(((UnaryPlusExpression) plusShared.expression).expression)
        assertSame(plusSame, plusSameStmt.expression)
        assertCopiedThis(((BitwiseNegationExpression) negateShared.expression).expression)
        assertSame(negateSame, negateSameStmt.expression)
        assertCopiedThis(((SpreadExpression) spreadShared.expression).expression)
        assertSame(spreadSame, spreadSameStmt.expression)
        assertCopiedThis(((SpreadMapExpression) spreadMapShared.expression).expression)
        assertSame(spreadMapSame, spreadMapSameStmt.expression)

        assertSame(rangeSame, rangeSameStmt.expression)
        assertCopiedThis(((RangeExpression) rangeFrom.expression).from)
        assertTrue(((RangeExpression) rangeFrom.expression).exclusiveRight)
        assertCopiedSuper(((RangeExpression) rangeTo.expression).to)

        assertCopiedThis(switchFull.expression)
        assertCopiedThis(((ReturnStatement) switchFull.caseStatements[0].code).expression)
        assertCopiedSuper(((YieldStatement) switchFull.defaultStatement).expression)
        assertNull(switchNoDefault.defaultStatement)

        assertTrue(gstringEmpty.values.empty)
        assertNull(gstring.values[0])
        assertCopiedThis(gstring.values[1])
        assertSame(keep, gstring.values[2])
        assertTrue(emptyTuple.expressions.empty)
        assertNull(tuple.expressions[0])
        assertCopiedThis(tuple.expressions[1])
        assertNull(map.mapEntryExpressions[0])
        assertCopiedThis(map.mapEntryExpressions[1].keyExpression)
        assertCopiedSuper(map.mapEntryExpressions[2].valueExpression)
        assertSame(local, map.mapEntryExpressions[3].valueExpression)
        assertNull(list.expressions[0])
        assertCopiedThis(list.expressions[1])
        assertCopiedThis(arrayInit.expressions[1])
        assertNull(arrayInit.sizeExpression)
        assertCopiedThis(arraySize.sizeExpression[0])
        assertSame(keep, arraySize.sizeExpression[1])

        assertCopiedThis(richClosure.parameters[2].initialExpression)
        assertCopiedThis(((ReturnStatement) ((BlockStatement) richClosure.code).statements[0]).expression)
        assertCopiedSuper(((ExpressionStatement) implicitClosure.code).expression)
        assertNull(emptyClosure.code)

        assertCopiedThis(ifStmt.booleanExpression.expression)
        assertCopiedSuper(((ExpressionStatement) ifStmt.ifBlock).expression)
        assertCopiedThis(((ExpressionStatement) whileStmt.loopBlock).expression)
        assertCopiedThis(doStmt.booleanExpression.expression)
        assertCopiedThis(forStmt.collectionExpression)
        assertCopiedSuper(((ExpressionStatement) forStmt.loopBlock).expression)
        assertCopiedThis(syncStmt.expression)
        assertCopiedThis(switchStmt.expression)
        assertCopiedThis(switchStmt.caseStatements[0].expression)
        assertNull(assertNoMessage.messageExpression)
        assertCopiedThis(assertNoMessage.booleanExpression.expression)
        assertCopiedThis(assertWithMessage.messageExpression)
    }

    private static MethodNode method(String name, Statement code) {
        new MethodNode(name, Modifier.PUBLIC, VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code)
    }

    private static void setParameters(MethodNode method, Parameter[] parameters) {
        def field = MethodNode.getDeclaredField('parameters')
        field.accessible = true
        field.set(method, parameters)
    }

    private static ExpressionStatement stmt(Expression expression) {
        new ExpressionStatement(expression)
    }

    private static MethodCallExpression call(Expression receiver, Expression name) {
        new MethodCallExpression(receiver, name, new ArgumentListExpression())
    }

    private static BinaryExpression bin(Expression left, Expression right) {
        new BinaryExpression(left, Token.newSymbol('==', -1, -1), right)
    }

    private static TernaryExpression tern(BooleanExpression condition, Expression whenTrue, Expression whenFalse) {
        new TernaryExpression(condition, whenTrue, whenFalse)
    }

    private static PropertyExpression flaggedProperty(boolean attribute, boolean flags) {
        PropertyExpression property = attribute
                ? new AttributeExpression(new ConstantExpression('o'), THIS, true)
                : new PropertyExpression(new ConstantExpression('o'), THIS, true)
        property.implicitThis = flags
        property.spreadSafe = flags
        property.static = flags
        property
    }

    private static void assertCopiedThis(Expression expression) {
        assertTrue(expression instanceof VariableExpression)
        assertNotSame(THIS, expression)
        assertTrue(((VariableExpression) expression).thisExpression)
        assertFalse(((VariableExpression) expression).superExpression)
    }

    private static void assertCopiedSuper(Expression expression) {
        assertTrue(expression instanceof VariableExpression)
        assertNotSame(SUPER, expression)
        assertTrue(((VariableExpression) expression).superExpression)
    }
}
