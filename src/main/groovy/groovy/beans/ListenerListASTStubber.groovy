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
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes

import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod

/**
 * Joint-compilation stubber for {@link ListenerList}. Emits placeholder
 * {@code addXxxListener(L)}, {@code removeXxxListener(L)},
 * {@code getXxxListeners()}, plus a {@code fireXxx(...)} per public method
 * of the listener interface, so Java consumers can register listeners and
 * fire events against the joint-compilation stub.
 *
 * <p>The listener interface and its method set are read directly from the
 * field's generic type parameter at CONVERSION; for typical
 * classpath-resolvable listeners (e.g. {@code java.beans.PropertyChangeListener})
 * this works the same way the full transform does. Same-unit Groovy
 * interfaces with their own transform-added members fall under the
 * Tier 3 cross-class limitation.
 *
 * <p>The full {@link ListenerListASTTransformation} at CANONICALIZATION
 * removes stubber-tagged methods before its conflict check, so its
 * "method already declared" errors continue to catch user conflicts but
 * not stubber placeholders.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class ListenerListASTStubber extends AbstractASTTransformation {

    private static final ClassNode COLLECTION_TYPE = ClassHelper.make(Collection)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode annotation = (AnnotationNode) nodes[0]
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        if (!(parent instanceof FieldNode)) return
        FieldNode field = (FieldNode) parent
        ClassNode declaringClass = field.declaringClass
        ClassNode fieldType = field.type

        boolean isCollection = fieldType.isDerivedFrom(COLLECTION_TYPE) || fieldType.implementsInterface(COLLECTION_TYPE)
        if (!isCollection) return
        def types = fieldType.genericsTypes
        if (!types || types[0].wildcard) return
        ClassNode listener = types[0].type

        String name = annotation.getMember('name')?.value ?: listener.nameWithoutPackage
        String capName = name[0].toUpperCase() + name.substring(1)
        boolean synchronize = annotation.getMember('synchronize')?.value ?: false
        int modifiers = synchronize ? Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNCHRONIZED : Opcodes.ACC_PUBLIC

        // Listener parameter type — use a redirect copy as the full transform does.
        ClassNode lParam = ClassHelper.makeWithoutCaching(listener.name)
        lParam.redirect = listener

        Parameter[] addParam = [param(lParam, 'listener')] as Parameter[]
        if (declaringClass.getDeclaredMethod("add$capName", addParam) == null) {
            addStubMethod(declaringClass, "add$capName", modifiers,
                    ClassHelper.VOID_TYPE, addParam, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        }

        Parameter[] removeParam = [param(lParam, 'listener')] as Parameter[]
        if (declaringClass.getDeclaredMethod("remove$capName", removeParam) == null) {
            addStubMethod(declaringClass, "remove$capName", modifiers,
                    ClassHelper.VOID_TYPE, removeParam, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        }

        if (declaringClass.getDeclaredMethod("get${capName}s", Parameter.EMPTY_ARRAY) == null) {
            addStubMethod(declaringClass, "get${capName}s", modifiers,
                    listener.makeArray(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                    returnS(constX(null)))
        }

        // fireXxx per public listener method.
        listener.methods.findAll { MethodNode m ->
            m.isPublic() && !m.isSynthetic() && !m.isStatic()
        }.each { MethodNode m ->
            String fireName = "fire${m.name[0].toUpperCase() + m.name.substring(1)}"
            // Mirror the full transform's parameter copying (wrapper types,
            // plain refs) so stub signatures match what the full transform
            // will emit.
            Parameter[] fireParams = m.parameters.collect { Parameter p ->
                ClassNode wrapper = ClassHelper.getWrapper(p.type)
                ClassNode cn = wrapper.plainNodeReference
                cn.redirect = wrapper
                param(cn, p.name)
            } as Parameter[]
            if (declaringClass.getDeclaredMethod(fireName, fireParams) == null) {
                addStubMethod(declaringClass, fireName, modifiers,
                        ClassHelper.VOID_TYPE, fireParams, ClassNode.EMPTY_ARRAY,
                        EmptyStatement.INSTANCE)
            }
        }
    }
}
