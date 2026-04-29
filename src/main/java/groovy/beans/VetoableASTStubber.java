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

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link Vetoable} (class-level use).
 * Symmetric to {@link BindableASTStubber}: emits placeholder
 * {@code add/removeVetoableChangeListener},
 * {@code fireVetoableChange} (with {@code throws PropertyVetoException}),
 * and {@code getVetoableChangeListeners} methods.
 *
 * <p>The full {@link VetoableASTTransformation} at CANONICALIZATION
 * removes any stubber-tagged methods before installing the
 * {@code vetoableChangeSupport} field and the real method bodies.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class VetoableASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Vetoable.class);
    private static final ClassNode VCL_TYPE = make(VetoableChangeListener.class);
    private static final ClassNode PVE_TYPE = make(PropertyVetoException.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        Parameter listener = param(VCL_TYPE, "listener");
        Parameter name = param(ClassHelper.STRING_TYPE, "name");

        // void addVetoableChangeListener(VetoableChangeListener)
        if (classNode.getDeclaredMethod("addVetoableChangeListener", params(listener)) == null) {
            addStubMethod(classNode, "addVetoableChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(VCL_TYPE, "listener")), ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void addVetoableChangeListener(String, VetoableChangeListener)
        if (classNode.getDeclaredMethod("addVetoableChangeListener", params(name, listener)) == null) {
            addStubMethod(classNode, "addVetoableChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(ClassHelper.STRING_TYPE, "name"), param(VCL_TYPE, "listener")),
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void removeVetoableChangeListener(VetoableChangeListener)
        if (classNode.getDeclaredMethod("removeVetoableChangeListener", params(listener)) == null) {
            addStubMethod(classNode, "removeVetoableChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(VCL_TYPE, "listener")), ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void removeVetoableChangeListener(String, VetoableChangeListener)
        if (classNode.getDeclaredMethod("removeVetoableChangeListener", params(name, listener)) == null) {
            addStubMethod(classNode, "removeVetoableChangeListener", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    params(param(ClassHelper.STRING_TYPE, "name"), param(VCL_TYPE, "listener")),
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }

        // void fireVetoableChange(String, Object, Object) throws PropertyVetoException
        Parameter[] fireParams = params(
                param(ClassHelper.STRING_TYPE, "name"),
                param(ClassHelper.OBJECT_TYPE, "oldValue"),
                param(ClassHelper.OBJECT_TYPE, "newValue"));
        if (classNode.getDeclaredMethod("fireVetoableChange", fireParams) == null) {
            addStubMethod(classNode, "fireVetoableChange", ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    fireParams, new ClassNode[]{PVE_TYPE}, EmptyStatement.INSTANCE);
        }

        // VetoableChangeListener[] getVetoableChangeListeners()
        if (classNode.getDeclaredMethod("getVetoableChangeListeners", Parameter.EMPTY_ARRAY) == null) {
            addStubMethod(classNode, "getVetoableChangeListeners", ACC_PUBLIC, VCL_TYPE.makeArray(),
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }

        // VetoableChangeListener[] getVetoableChangeListeners(String)
        if (classNode.getDeclaredMethod("getVetoableChangeListeners", params(name)) == null) {
            addStubMethod(classNode, "getVetoableChangeListeners", ACC_PUBLIC, VCL_TYPE.makeArray(),
                    params(param(ClassHelper.STRING_TYPE, "name")),
                    ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }
    }
}
