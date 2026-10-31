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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC

/**
 * Branch coverage for the anonymous-initializer enclosing-member rewrite.
 * Several walks only matter for a declaring class that normal resolution
 * never produces; those are checked directly.
 */
final class EnclosingMemberRewriterTest {

    private final SourceUnit source = SourceUnit.create('EnclosingMemberRewriterTest', 'class Unused {}')
    private final ClassNode base = new ClassNode('Base', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
    private final ClassNode outer = new ClassNode('Outer', ACC_PUBLIC, base)
    private final ClassNode api = new ClassNode('Api', ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
    private final ClassNode foreign = new ClassNode('Foreign', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
    private final InnerClassNode aic = new InnerClassNode(outer, 'Outer$1', ACC_PUBLIC, ClassHelper.make(HashMap))
    private final Object rewriter
    private final Method getSourceUnit
    private final Method isEnclosingMember
    private final Method enclosingOwner
    private final Method enclosingMemberAccess
    private final Method transform

    EnclosingMemberRewriterTest() {
        outer.addInterface(api)
        aic.anonymous = true

        Class<?> type = VariableScopeVisitor.declaredClasses.find { it.simpleName == 'EnclosingMemberRewriter' }
        def ctor = type.getDeclaredConstructor(VariableScopeVisitor, InnerClassNode)
        ctor.accessible = true
        rewriter = ctor.newInstance(new VariableScopeVisitor(source), aic)
        getSourceUnit = method('getSourceUnit')
        isEnclosingMember = method('isEnclosingMember', ClassNode)
        enclosingOwner = method('enclosingOwner', ClassNode)
        enclosingMemberAccess = method('enclosingMemberAccess', VariableExpression)
        transform = method('transform', Expression)
    }

    private Method method(String name, Class... params) {
        Method found = rewriter.class.getDeclaredMethod(name, params)
        found.accessible = true
        return found
    }

    @Test
    void testSourceUnitIsTheCompilationUnit() {
        assert getSourceUnit.invoke(rewriter).is(source)
    }

    @Test
    void testForeignDeclaringClassIsNotEnclosing() {
        assert isEnclosingMember.invoke(rewriter, foreign) == false
        assert enclosingOwner.invoke(rewriter, foreign).is(foreign)
        assert enclosingMemberAccess.invoke(rewriter, variable('other', field('other', ACC_PUBLIC, foreign))) == null

        // Interface the anonymous class and its outer class do not implement.
        def otherApi = new ClassNode('OtherApi', ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
        assert isEnclosingMember.invoke(rewriter, otherApi) == false
        assert enclosingOwner.invoke(rewriter, otherApi).is(otherApi)
    }

    @Test
    void testAnonymousClassMembersAreNotEnclosing() {
        assert isEnclosingMember.invoke(rewriter, aic) == false
        assert isEnclosingMember.invoke(rewriter, ClassHelper.make(HashMap)) == false
        assert isEnclosingMember.invoke(rewriter, ClassHelper.MAP_TYPE) == false
    }

    @Test
    void testOuterClassSuperAndInterfaceAreEnclosing() {
        assert isEnclosingMember.invoke(rewriter, outer) == true
        assert enclosingOwner.invoke(rewriter, outer).is(outer)

        assert isEnclosingMember.invoke(rewriter, base) == true
        assert enclosingOwner.invoke(rewriter, base).is(outer)

        assert isEnclosingMember.invoke(rewriter, api) == true
        assert enclosingOwner.invoke(rewriter, api).is(outer)
    }

    @Test
    void testStaticFieldReadsDirectlyAndInstancePropertyUsesOuterThis() {
        def staticField = field('VALUE', ACC_PUBLIC | ACC_STATIC, outer)
        def read = enclosingMemberAccess.invoke(rewriter, variable('VALUE', staticField))
        assert read instanceof FieldExpression
        assert read.field.is(staticField)

        def instanceField = field('name', ACC_PUBLIC, outer)
        def property = new PropertyNode(instanceField, ACC_PUBLIC, null, null)
        property.declaringClass = outer
        def access = enclosingMemberAccess.invoke(rewriter, variable('name', property))
        assert access instanceof PropertyExpression
        assert access.propertyAsString == 'name'
        assert access.objectExpression instanceof PropertyExpression
        assert access.objectExpression.propertyAsString == 'this'
        assert access.objectExpression.objectExpression instanceof ClassExpression

        def synthetic = field('label', ACC_PUBLIC | ACC_STATIC, outer)
        synthetic.synthetic = true
        def getter = new PropertyNode(synthetic, ACC_PUBLIC | ACC_STATIC, null, null)
        getter.declaringClass = outer
        def staticGetter = enclosingMemberAccess.invoke(rewriter, variable('label', getter))
        assert staticGetter instanceof PropertyExpression
        assert staticGetter.objectExpression instanceof ClassExpression

        def noField = new PropertyNode(instanceField, ACC_PUBLIC, null, null)
        noField.declaringClass = outer
        PropertyNode.getDeclaredField('field').with {
            accessible = true
            set(noField, null)
        }
        assert enclosingMemberAccess.invoke(rewriter, variable('name', noField)) instanceof PropertyExpression
    }

    @Test
    void testAssignmentToEnclosingNameKeepsTheLeftHandSide() {
        def left = variable('label', field('label', ACC_PUBLIC | ACC_STATIC, outer))
        def right = variable('VALUE', field('VALUE', ACC_PUBLIC | ACC_STATIC, outer))
        def assignment = new BinaryExpression(
                left, Token.newSymbol(Types.ASSIGN, -1, -1), right)
        def result = transform.invoke(rewriter, assignment)
        assert result.is(assignment)
        assert assignment.leftExpression.is(left)
        assert assignment.rightExpression instanceof FieldExpression

        def local = new VariableExpression('local')
        local.accessedVariable = local
        def localAssign = new BinaryExpression(
                local, Token.newSymbol(Types.ASSIGN, -1, -1), new ConstantExpression('temp'))
        assert !transform.invoke(rewriter, localAssign).is(localAssign)

        def plus = new BinaryExpression(
                variable('VALUE', field('VALUE', ACC_PUBLIC | ACC_STATIC, outer)),
                Token.newSymbol(Types.PLUS, -1, -1),
                new ConstantExpression('!'))
        assert transform.invoke(rewriter, plus) instanceof BinaryExpression

        def closure = new ClosureExpression(null, EmptyStatement.INSTANCE)
        assert transform.invoke(rewriter, closure).is(closure)

        def propertyAssign = new BinaryExpression(
                new PropertyExpression(new VariableExpression('this'), 'label'),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstantExpression('x'))
        assert transform.invoke(rewriter, propertyAssign) instanceof BinaryExpression
    }

    private static FieldNode field(String name, int modifiers, ClassNode owner) {
        def field = new FieldNode(name, modifiers, ClassHelper.STRING_TYPE, owner, null)
        field.declaringClass = owner
        return field
    }

    private static VariableExpression variable(String name, Variable accessed) {
        def variable = new VariableExpression(name)
        variable.accessedVariable = accessed
        return variable
    }
}
