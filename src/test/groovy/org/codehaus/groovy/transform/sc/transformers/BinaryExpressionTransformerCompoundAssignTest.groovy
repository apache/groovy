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
package org.codehaus.groovy.transform.sc.transformers

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor
import org.codehaus.groovy.transform.stc.ExtensionMethodNode
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

/**
 * Branch coverage for extension-property compound assignment that the
 * compiler does not reach from ordinary scripts.
 */
final class BinaryExpressionTransformerCompoundAssignTest {

    private final StaticCompilationTransformer transformer = newTransformer(new GroovyClassLoader())

    @Test
    void testExtensionSetterWithUnexpectedArityIsLeftAlone() {
        PropertyExpression left = property('file', 'text')
        left.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, extension('setText', ClassHelper.VOID_TYPE, params(ClassHelper.STRING_TYPE, ClassHelper.STRING_TYPE), ClassHelper.make(File)))
        def result = transformer.transform(compound(left, Types.PLUS_EQUAL, constX('b')))
        assert result instanceof BinaryExpression
    }

    @Test
    void testImplicitReceiverNames() {
        ['owner', 'delegate', 'outer'].each { String receiver ->
            VariableExpression left = new VariableExpression('text', ClassHelper.STRING_TYPE)
            left.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, receiver)
            left.putNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER, ClassHelper.make(File))
            armExtensionProperty(left)
            assert transformer.transform(compound(left, Types.PLUS_EQUAL, constX('b'))) != null
        }
        VariableExpression sameName = new VariableExpression('text', ClassHelper.STRING_TYPE)
        sameName.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, 'text')
        armExtensionProperty(sameName)
        assert transformer.transform(compound(sameName, Types.PLUS_EQUAL, constX('b'))) != null
    }

    @Test
    void testDynamicPropertyNameSkipsGetterLookup() {
        PropertyExpression left = new PropertyExpression(new VariableExpression('file', ClassHelper.make(File)), new VariableExpression('prop'))
        assert left.propertyAsString == null
        left.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, setter(ClassHelper.make(File)))
        left.putNodeMetaData(StaticCompilationMetadataKeys.EXTENSION_PROPERTY_GETTER, setter(ClassHelper.make(File)))
        def result = transformer.transform(compound(left, Types.PLUS_EQUAL, constX('b')))
        assert result instanceof BinaryExpression
    }

    @Test
    void testPrimitiveReceiverAlsoSearchesItsWrapper() {
        PropertyExpression left = property('n', 'groovy12524Raw')
        left.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, setter(ClassHelper.int_TYPE))
        def result = transformer.transform(compound(left, Types.PLUS_EQUAL, constX(1)))
        assert result instanceof BinaryExpression
    }

    @Test
    void testOperatorCallWithoutInferredType() {
        PropertyExpression left = property('file', 'text')
        armExtensionProperty(left)
        BinaryExpression bin = compound(left, Types.PLUS_EQUAL, constX('b'))
        MethodNode plus = new MethodNode('plus', Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE, params(ClassHelper.OBJECT_TYPE), ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        plus.declaringClass = ClassHelper.STRING_TYPE
        bin.putNodeMetaData(StaticCompilationMetadataKeys.BINARY_EXP_TARGET, [plus, 'plus'] as Object[])
        assert transformer.transform(bin) != null
    }

    @Test
    void testUnmappedAssignmentTokenDoesNotRecurse() {
        def method = BinaryExpressionTransformer.getDeclaredMethod('compoundValue', BinaryExpression, int, MethodCallExpression, Expression)
        method.accessible = true
        BinaryExpressionTransformer dispatcher = new BinaryExpressionTransformer(transformer)
        PropertyExpression left = property('file', 'text')
        BinaryExpression bin = compound(left, Types.EQUAL, constX('b'))
        MethodCallExpression getter = new MethodCallExpression(new VariableExpression('file'), 'getText', MethodCallExpression.NO_ARGUMENTS)
        assert method.invoke(dispatcher, bin, Types.EQUAL, getter, constX('b')) == null
    }

    @Test
    void testZeroArgExtensionFilter() {
        def method = BinaryExpressionTransformer.getDeclaredMethod('addZeroArgExtensions', Set, Iterable, boolean)
        method.accessible = true
        ExtensionMethodNode instanceGetter = extension('getLabel', ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassHelper.OBJECT_TYPE, false)
        ExtensionMethodNode staticGetter = extension('getLabel', ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassHelper.OBJECT_TYPE, true)
        ExtensionMethodNode withArgument = extension('getLabel', ClassHelper.STRING_TYPE, params(ClassHelper.STRING_TYPE), ClassHelper.OBJECT_TYPE, false)
        MethodNode plain = new MethodNode('getLabel', Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        Set<MethodNode> kept = new LinkedHashSet<>()
        method.invoke(null, kept, [plain, withArgument, staticGetter, instanceGetter], false)
        assert kept == [instanceGetter] as Set
    }

    @Test
    void testExtensionGetterWithArgumentsIsNotRecorded() {
        def method = StaticTypeCheckingVisitor.getDeclaredMethod('rememberExtensionPropertyGetter', Expression, Expression)
        method.accessible = true
        VariableExpression target = new VariableExpression('list')
        VariableExpression read = new VariableExpression('list')
        read.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, extension('getAt', ClassHelper.OBJECT_TYPE, params(ClassHelper.int_TYPE), ClassHelper.LIST_TYPE))
        method.invoke(null, target, read)
        assert target.getNodeMetaData(StaticCompilationMetadataKeys.EXTENSION_PROPERTY_GETTER) == null

        VariableExpression zeroArg = new VariableExpression('file')
        read = new VariableExpression('file')
        ExtensionMethodNode getter = extension('getText', ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassHelper.make(File))
        read.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, getter)
        method.invoke(null, zeroArg, read)
        assert zeroArg.getNodeMetaData(StaticCompilationMetadataKeys.EXTENSION_PROPERTY_GETTER) == getter
    }

    private static void armExtensionProperty(final Expression left) {
        left.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, setter(ClassHelper.make(File)))
        left.putNodeMetaData(StaticCompilationMetadataKeys.EXTENSION_PROPERTY_GETTER, extension('getText', ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassHelper.make(File)))
    }

    private static ExtensionMethodNode setter(final ClassNode owner) {
        extension('setText', ClassHelper.VOID_TYPE, params(ClassHelper.STRING_TYPE), owner)
    }

    private static ExtensionMethodNode extension(final String name, final ClassNode returnType, final Parameter[] params, final ClassNode owner, final boolean staticExtension = false) {
        Parameter[] actual = new Parameter[params.length + 1]
        actual[0] = new Parameter(owner, 'self')
        System.arraycopy(params, 0, actual, 1, params.length)
        MethodNode implementation = new MethodNode(name, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, returnType, actual, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        implementation.declaringClass = ClassHelper.OBJECT_TYPE
        ExtensionMethodNode node = new ExtensionMethodNode(implementation, name, Opcodes.ACC_PUBLIC, returnType, params, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE, staticExtension)
        node.declaringClass = owner
        node
    }

    private static Parameter[] params(ClassNode... types) {
        Parameter[] parameters = new Parameter[types.length]
        for (int i = 0; i < types.length; i++) {
            parameters[i] = new Parameter(types[i], 'p' + i)
        }
        parameters
    }

    private static PropertyExpression property(final String object, final String name) {
        new PropertyExpression(new VariableExpression(object), name)
    }

    private static ConstantExpression constX(final Object value) {
        new ConstantExpression(value)
    }

    private static BinaryExpression compound(final Expression left, final int operation, final Expression right) {
        new BinaryExpression(left, Token.newSymbol(operation, 1, 1), right)
    }

    private static StaticCompilationTransformer newTransformer(final GroovyClassLoader loader) {
        CompilerConfiguration conf = new CompilerConfiguration()
        SourceUnit unit = new SourceUnit('CompoundAssign.groovy', '', conf, loader, new ErrorCollector(conf))
        ClassNode owner = new ClassNode('CompoundAssign', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        new StaticCompilationTransformer(unit, new StaticCompilationVisitor(unit, owner))
    }
}
