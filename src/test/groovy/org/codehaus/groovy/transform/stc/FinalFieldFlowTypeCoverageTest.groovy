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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.ClassHelper.dynamicType
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.objectweb.asm.Opcodes.ACC_FINAL
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

/**
 * Drives the final-field flow-typing edges that ordinary scripts never reach:
 * a null or non-primary owner, a null or void recorded type, a spread read,
 * and a {@code Class<T>} receiver.
 */
final class FinalFieldFlowTypeCoverageTest {

    private static final String FLOW_TYPE = 'stc.finalFieldFlowType'
    private static final String FLOW_READY = 'stc.finalFieldFlowReady'

    @Test
    void testNullAndNonPrimaryOwnersSkipFlowSetup() {
        StaticTypeCheckingVisitor visitor = visitor()

        FieldNode unattached = new FieldNode('bar', ACC_PUBLIC | ACC_FINAL, dynamicType(), null, null)
        visitor.visitField(unattached)

        ClassNode reflected = new ClassNode(String)
        FieldNode foreign = new FieldNode('bar', ACC_PUBLIC | ACC_FINAL, dynamicType(), reflected, null)
        reflected.addField(foreign)
        visitor.visitField(foreign)

        assert invoke(visitor, 'prepareFinalFieldFlow', [ClassNode] as Class[], [null] as Object[]) == null
        assert invoke(visitor, 'prepareFinalFieldFlow', [ClassNode] as Class[], [reflected] as Object[]) == null
        assert invoke(visitor, 'pullStaticInitializer', [ClassNode] as Class[], [reflected] as Object[]) == null
    }

    @Test
    void testUnpinnedAndNullRecordedTypes() {
        StaticTypeCheckingVisitor visitor = visitor()
        ClassNode owner = enclosing(visitor)
        FieldNode field = finalField(owner, 'bar')

        assert invoke(visitor, 'recordFinalFieldFlowType', [FieldNode, ClassNode] as Class[], [field, null] as Object[]) == null
        assert field.getNodeMetaData(FLOW_TYPE) == null

        invoke(visitor, 'recordFinalFieldFlowType', [FieldNode, ClassNode] as Class[], [field, VOID_TYPE] as Object[])
        assert field.getNodeMetaData(FLOW_TYPE) == null

        invoke(visitor, 'recordFinalFieldFlowType', [FieldNode, ClassNode] as Class[], [field, StaticTypeCheckingSupport.UNKNOWN_PARAMETER_TYPE] as Object[])
        assert field.getNodeMetaData(FLOW_TYPE) == null

        invoke(visitor, 'recordFinalFieldFlowType', [FieldNode, ClassNode] as Class[], [field, dynamicType()] as Object[])
        def blocked = field.getNodeMetaData(FLOW_TYPE)
        assert blocked != null
        assert !(blocked instanceof ClassNode)

        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [null] as Object[]) == false
        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [VOID_TYPE] as Object[]) == false
        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [StaticTypeCheckingSupport.UNKNOWN_PARAMETER_TYPE] as Object[]) == false
        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [dynamicType()] as Object[]) == false
        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [new UnionTypeClassNode(STRING_TYPE, make(StringBuilder))] as Object[]) == true

        ClassNode genericObject = OBJECT_TYPE.getPlainNodeReference()
        genericObject.genericsTypes = [new GenericsType(STRING_TYPE)] as GenericsType[]
        assert invoke(null, 'isPinnedFlowType', [ClassNode] as Class[], [genericObject] as Object[]) == true
    }

    @Test
    void testNullFieldAndUnpinnedFlowAreNotVisibleTypes() {
        StaticTypeCheckingVisitor visitor = visitor()
        assert invoke(visitor, 'finalFieldFlowType', [FieldNode] as Class[], [null] as Object[]) == null

        FieldNode field = finalField(enclosing(visitor), 'bar')
        field.putNodeMetaData(FLOW_TYPE, OBJECT_TYPE)
        assert invoke(visitor, 'finalFieldFlowType', [FieldNode] as Class[], [field] as Object[]) == null

        FieldNode unattached = new FieldNode('orphan', ACC_PUBLIC | ACC_FINAL, dynamicType(), null, null)
        assert invoke(visitor, 'isFinalFieldFlowVisible', [FieldNode] as Class[], [unattached] as Object[]) == false
        assert invoke(visitor, 'isFinalFieldFlowBlocked', [FieldNode] as Class[], [unattached] as Object[]) == true

        visitor.typeCheckingContext.popEnclosingClassNode()
        assert visitor.typeCheckingContext.enclosingClassNode == null
        assert invoke(visitor, 'isFinalFieldFlowVisible', [FieldNode] as Class[], [field] as Object[]) == false
    }

    @Test
    void testClassLiteralReceiverAndSpreadRead() {
        StaticTypeCheckingVisitor visitor = visitor()
        ClassNode owner = enclosing(visitor)
        FieldNode field = finalField(owner, 'bar')
        field.putNodeMetaData(FLOW_TYPE, STRING_TYPE)
        owner.putNodeMetaData(FLOW_READY, Boolean.TRUE)

        ClassNode classOfOwner = ClassHelper.CLASS_Type.getPlainNodeReference()
        classOfOwner.genericsTypes = [new GenericsType(owner)] as GenericsType[]
        ClassExpression wrapped = new ClassExpression(classOfOwner)
        assert invoke(visitor, 'isOwnReceiver', [ClassNode, Expression] as Class[], [owner, wrapped] as Object[]) == true

        GenericsType empty = new GenericsType(STRING_TYPE)
        def typeField = GenericsType.getDeclaredField('type')
        typeField.accessible = true
        typeField.set(empty, null)
        ClassNode classOfNothing = ClassHelper.CLASS_Type.getPlainNodeReference()
        classOfNothing.genericsTypes = [empty] as GenericsType[]
        assert invoke(visitor, 'isOwnReceiver', [ClassNode, Expression] as Class[], [owner, new ClassExpression(classOfNothing)] as Object[]) == false

        PropertyExpression spread = new PropertyExpression(new VariableExpression('f', owner), 'bar')
        spread.spreadSafe = true
        PropertyNode property = new PropertyNode(field, ACC_PUBLIC, null, null)
        assert invoke(visitor, 'storeProperty', storePropertyTypes(),
                [property, spread, Receiver.make(owner), null, Boolean.FALSE] as Object[]) == true

        PropertyExpression plain = new PropertyExpression(new VariableExpression('f', owner), 'bar')
        assert invoke(visitor, 'storeProperty', storePropertyTypes(),
                [property, plain, Receiver.make(owner), null, Boolean.FALSE] as Object[]) == true

        boolean[] blockAll = [false] as boolean[]
        FieldNode orphan = new FieldNode('bar', ACC_PUBLIC | ACC_FINAL, dynamicType(), null, null)
        assert invoke(visitor, 'finalFieldWriteName', [ClassNode, Expression, boolean[]] as Class[],
                [owner, new FieldExpression(orphan), blockAll] as Object[]) == null

        PropertyExpression byClass = new PropertyExpression(wrapped, 'bar')
        assert invoke(visitor, 'assignedFinalDynamicField', [Expression] as Class[], [byClass] as Object[]) == field

        PropertyExpression byThis = new PropertyExpression(VariableExpression.THIS_EXPRESSION, 'bar')
        assert invoke(visitor, 'assignedFinalDynamicField', [Expression] as Class[], [byThis] as Object[]) == field

        PropertyExpression bySuper = new PropertyExpression(VariableExpression.SUPER_EXPRESSION, 'bar')
        assert invoke(visitor, 'assignedFinalDynamicField', [Expression] as Class[], [bySuper] as Object[]) == field
    }

    private static Class[] storePropertyTypes() {
        [PropertyNode, PropertyExpression, Receiver, ClassCodeVisitorSupport, boolean] as Class[]
    }

    private static StaticTypeCheckingVisitor visitor() {
        SourceUnit source = SourceUnit.create('FinalFieldFlowTypeCoverage.groovy', '')
        ClassNode cn = new ClassNode('FinalFieldFlowTypeCoverage', ACC_PUBLIC, OBJECT_TYPE)
        new StaticTypeCheckingVisitor(source, cn)
    }

    private static ClassNode enclosing(StaticTypeCheckingVisitor visitor) {
        visitor.typeCheckingContext.enclosingClassNode
    }

    private static FieldNode finalField(ClassNode owner, String name) {
        FieldNode field = new FieldNode(name, ACC_PUBLIC | ACC_FINAL, dynamicType(), owner, null)
        owner.addField(field)
        field
    }

    private static Object invoke(Object target, String name, Class[] types, Object[] args) {
        Method method = StaticTypeCheckingVisitor.getDeclaredMethod(name, types)
        method.accessible = true
        method.invoke(target, args)
    }
}
