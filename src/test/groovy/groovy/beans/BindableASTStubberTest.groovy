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
package groovy.beans

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SimpleMessage
import org.codehaus.groovy.transform.StubberSupport
import org.junit.jupiter.api.Test

import java.beans.PropertyChangeListener

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_FINAL
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC

final class BindableASTStubberTest {

    private static final ClassNode LISTENER = ClassHelper.make(PropertyChangeListener)

    @Test
    void testClassAndPropertyGainListenerStubsOnce() {
        ClassNode type = new ClassNode('Bean', ACC_PUBLIC, OBJECT_TYPE)
        visit(type)
        visit(type)
        assert listenerMethods(type).count { it == 'addPropertyChangeListener' } == 2
        assert listenerMethods(type).count { it == 'removePropertyChangeListener' } == 2
        assert listenerMethods(type).count { it == 'firePropertyChange' } == 1
        assert listenerMethods(type).count { it == 'getPropertyChangeListeners' } == 2

        ClassNode owner = new ClassNode('FieldBean', ACC_PUBLIC, OBJECT_TYPE)
        owner.addProperty('label', ACC_PUBLIC, STRING_TYPE, null, null, null)
        visit(owner.getField('label'))
        assert listenerMethods(owner).contains('firePropertyChange')
    }

    @Test
    void testSitesWithoutAListenerSurfaceAreSkipped() {
        ClassNode ignored = new ClassNode('Ignored', ACC_PUBLIC, OBJECT_TYPE)

        ClassNode iface = new ClassNode('Marker', ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, OBJECT_TYPE)
        visit(iface)
        assert iface.methods.empty

        ClassNode staticOwner = new ClassNode('StaticBean', ACC_PUBLIC, OBJECT_TYPE)
        FieldNode staticField = new FieldNode('label', ACC_PUBLIC | ACC_STATIC, STRING_TYPE, staticOwner, null)
        staticOwner.addField(staticField)
        visit(staticField)
        assert listenerMethods(staticOwner).empty

        ClassNode finalOwner = new ClassNode('FinalBean', ACC_PUBLIC, OBJECT_TYPE)
        FieldNode finalField = new FieldNode('label', ACC_PUBLIC | ACC_FINAL, STRING_TYPE, finalOwner, constX('x'))
        finalOwner.addField(finalField)
        visit(finalField)
        assert listenerMethods(finalOwner).empty

        visit(new FieldNode('label', ACC_PUBLIC, STRING_TYPE, null, null))

        ClassNode plainOwner = new ClassNode('PlainBean', ACC_PUBLIC, OBJECT_TYPE)
        FieldNode plainField = new FieldNode('label', ACC_PUBLIC, STRING_TYPE, plainOwner, null)
        plainOwner.addField(plainField)
        assert plainOwner.getProperty('label') == null
        visit(plainField)
        assert listenerMethods(plainOwner).empty

        visit(new MethodNode('foo', ACC_PUBLIC, VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE))

        new BindableASTStubber().visit([new AnnotationNode(ClassHelper.make(Override)), ignored] as ASTNode[], null)
        assert ignored.methods.empty
    }

    // GROOVY-12726
    @Test
    void testOnlyListenerPlaceholdersAreRemoved() {
        ClassNode type = new ClassNode('Bean', ACC_PUBLIC, OBJECT_TYPE)
        addListenerStub(type, 'addPropertyChangeListener', params(param(LISTENER, 'listener')), VOID_TYPE)
        addListenerStub(type, 'removePropertyChangeListener', params(param(LISTENER, 'listener')), VOID_TYPE)
        addListenerStub(type, 'firePropertyChange', params(
                param(STRING_TYPE, 'name'), param(OBJECT_TYPE, 'oldValue'), param(OBJECT_TYPE, 'newValue')), VOID_TYPE)
        addListenerStub(type, 'getPropertyChangeListeners', Parameter.EMPTY_ARRAY, LISTENER.makeArray())
        StubberSupport.addStubMethod(type, 'toString', ACC_PUBLIC, STRING_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, returnS(constX('stub')))
        type.addMethod('helper', ACC_PUBLIC, VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)

        SourceUnit source = sourceUnit()
        // The query does not remove anything; placeholders still look like methods.
        assert !needsSupport().call(type, source)
        assert StubberSupport.isStub(type.getDeclaredMethod('addPropertyChangeListener', params(param(LISTENER, 'listener'))))
        assert StubberSupport.isStub(type.getDeclaredMethod('toString', Parameter.EMPTY_ARRAY))

        new BindableASTTransformation().visit([new AnnotationNode(ClassHelper.make(Bindable)), type] as ASTNode[], source)

        assert !StubberSupport.isStub(type.getDeclaredMethod('addPropertyChangeListener', params(param(LISTENER, 'listener'))))
        assert !StubberSupport.isStub(type.getDeclaredMethod('removePropertyChangeListener', params(param(LISTENER, 'listener'))))
        assert !StubberSupport.isStub(type.getDeclaredMethod('firePropertyChange', params(
                param(STRING_TYPE, 'name'), param(OBJECT_TYPE, 'oldValue'), param(OBJECT_TYPE, 'newValue'))))
        assert !StubberSupport.isStub(type.getDeclaredMethod('getPropertyChangeListeners', Parameter.EMPTY_ARRAY))
        assert StubberSupport.isStub(type.getDeclaredMethod('toString', Parameter.EMPTY_ARRAY))
        assert type.getDeclaredMethod('helper', Parameter.EMPTY_ARRAY) != null
        assert !source.errorCollector.hasErrors()

        ClassNode owner = new ClassNode('FieldBean', ACC_PUBLIC, OBJECT_TYPE)
        owner.addProperty('label', ACC_PUBLIC, STRING_TYPE, null, null, null)
        addListenerStub(owner, 'addPropertyChangeListener', params(param(LISTENER, 'listener')), VOID_TYPE)
        addListenerStub(owner, 'removePropertyChangeListener', params(param(LISTENER, 'listener')), VOID_TYPE)
        addListenerStub(owner, 'firePropertyChange', params(
                param(STRING_TYPE, 'name'), param(OBJECT_TYPE, 'oldValue'), param(OBJECT_TYPE, 'newValue')), VOID_TYPE)
        StubberSupport.addStubMethod(owner, 'toString', ACC_PUBLIC, STRING_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, returnS(constX('stub')))
        SourceUnit fieldSource = sourceUnit()
        new BindableASTTransformation().visit(
                [new AnnotationNode(ClassHelper.make(Bindable)), owner.getField('label')] as ASTNode[], fieldSource)
        assert !StubberSupport.isStub(owner.getDeclaredMethod('addPropertyChangeListener', params(param(LISTENER, 'listener'))))
        assert StubberSupport.isStub(owner.getDeclaredMethod('toString', Parameter.EMPTY_ARRAY))
        assert !fieldSource.errorCollector.hasErrors()
    }

    // GROOVY-12726
    @Test
    void testHandWrittenListenerMethodIsKept() {
        ClassNode type = new ClassNode('Bean', ACC_PUBLIC, OBJECT_TYPE)
        Parameter[] parameters = params(param(LISTENER, 'listener'))
        MethodNode handWritten = type.addMethod('addPropertyChangeListener', ACC_PUBLIC, VOID_TYPE,
                parameters, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        StubberSupport.addStubMethod(type, 'toString', ACC_PUBLIC, STRING_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, returnS(constX('stub')))

        SourceUnit source = sourceUnit()
        assert !needsSupport().call(type, source)

        assert type.getDeclaredMethod('addPropertyChangeListener', parameters).is(handWritten)
        assert StubberSupport.isStub(type.getDeclaredMethod('toString', Parameter.EMPTY_ARRAY))
        assert source.errorCollector.hasErrors()
        assert source.errorCollector.errors.any { it instanceof SimpleMessage && it.message.contains('@Bindable cannot be processed') }
    }

    private static void visit(AnnotatedNode parent) {
        def annotation = new AnnotationNode(ClassHelper.make(Bindable))
        new BindableASTStubber().visit([annotation, parent] as ASTNode[], null)
    }

    private static List<String> listenerMethods(ClassNode type) {
        type.methods*.name.findAll { it.contains('PropertyChange') }
    }

    private static void addListenerStub(ClassNode type, String name, Parameter[] parameters, ClassNode returnType) {
        StubberSupport.addStubMethod(type, name, ACC_PUBLIC, returnType, parameters, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
    }

    private static SourceUnit sourceUnit() {
        CompilerConfiguration config = new CompilerConfiguration()
        new SourceUnit('Bean.groovy', 'class Bean {}', config, new GroovyClassLoader(), new ErrorCollector(config))
    }

    private static Probe needsSupport() {
        new Probe()
    }

    private static class Probe extends BindableASTTransformation {
        boolean call(ClassNode type, SourceUnit source) {
            needsPropertyChangeSupport(type, source)
        }
    }
}
