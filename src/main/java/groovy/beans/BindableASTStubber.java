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
package groovy.beans;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.beans.PropertyChangeListener;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link Bindable} (class-level use). Emits
 * placeholder {@code add/removePropertyChangeListener},
 * {@code firePropertyChange}, and {@code getPropertyChangeListeners}
 * methods so Java consumers can attach listeners and fire events against
 * the joint-compilation stub.
 *
 * <p>The full {@link BindableASTTransformation} at CANONICALIZATION
 * removes any stubber-tagged methods before installing the
 * {@code propertyChangeSupport} field and the real method bodies.
 *
 * <p>Field-level {@code @Bindable} on individual properties is not
 * stubbed: the visible API (the property's accessors) is unchanged, and
 * the property-write rewriting is body-internal to the generated setter
 * — invisible to the stub.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class BindableASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Bindable.class);
    private static final ClassNode PCL_TYPE = make(PropertyChangeListener.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        Parameter listener = param(PCL_TYPE, "listener");
        Parameter name = param(ClassHelper.STRING_TYPE, "name");

        // void addPropertyChangeListener(PropertyChangeListener)
        if (classNode.getDeclaredMethod("addPropertyChangeListener", params(listener)) == null) {
            addStubMethod(classNode, "addPropertyChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(PCL_TYPE, "listener")), ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void addPropertyChangeListener(String, PropertyChangeListener)
        if (classNode.getDeclaredMethod("addPropertyChangeListener", params(name, listener)) == null) {
            addStubMethod(classNode, "addPropertyChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(ClassHelper.STRING_TYPE, "name"), param(PCL_TYPE, "listener")),
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void removePropertyChangeListener(PropertyChangeListener)
        if (classNode.getDeclaredMethod("removePropertyChangeListener", params(listener)) == null) {
            addStubMethod(classNode, "removePropertyChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(PCL_TYPE, "listener")), ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void removePropertyChangeListener(String, PropertyChangeListener)
        if (classNode.getDeclaredMethod("removePropertyChangeListener", params(name, listener)) == null) {
            addStubMethod(classNode, "removePropertyChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(ClassHelper.STRING_TYPE, "name"), param(PCL_TYPE, "listener")),
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void firePropertyChange(String, Object, Object)
        Parameter[] fireParams = params(
                param(ClassHelper.STRING_TYPE, "name"),
                param(ClassHelper.OBJECT_TYPE, "oldValue"),
                param(ClassHelper.OBJECT_TYPE, "newValue"));
        if (classNode.getDeclaredMethod("firePropertyChange", fireParams) == null) {
            addStubMethod(classNode, "firePropertyChange", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    fireParams, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // PropertyChangeListener[] getPropertyChangeListeners()
        if (classNode.getDeclaredMethod("getPropertyChangeListeners", Parameter.EMPTY_ARRAY) == null) {
            addStubMethod(classNode, "getPropertyChangeListeners", ACC_PUBLIC, PCL_TYPE.makeArray(),
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }

        // PropertyChangeListener[] getPropertyChangeListeners(String)
        if (classNode.getDeclaredMethod("getPropertyChangeListeners", params(name)) == null) {
            addStubMethod(classNode, "getPropertyChangeListeners", ACC_PUBLIC, PCL_TYPE.makeArray(),
                    params(param(ClassHelper.STRING_TYPE, "name")),
                    ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }
    }
}
