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
package org.codehaus.groovy.transform;

import groovy.transform.ExternalizeMethods;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link ExternalizeMethods}. Adds the
 * {@link Externalizable} interface and emits placeholder
 * {@code writeExternal(ObjectOutput)} and {@code readExternal(ObjectInput)}
 * methods so Java consumers see the class as a valid {@code Externalizable}
 * implementation against the joint-compilation stub.
 *
 * <p>Bodies are placeholders; the full
 * {@link ExternalizeMethodsASTTransformation} at CANONICALIZATION
 * recognises the stubber metadata and replaces them with the real
 * field-by-field write/read implementations.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class ExternalizeMethodsASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(ExternalizeMethods.class);
    private static final ClassNode EXTERNALIZABLE_TYPE = make(Externalizable.class);
    private static final ClassNode OBJECTOUTPUT_TYPE = make(ObjectOutput.class);
    private static final ClassNode OBJECTINPUT_TYPE = make(ObjectInput.class);
    private static final ClassNode IO_EXCEPTION_TYPE = make(IOException.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        if (!classNode.implementsInterface(EXTERNALIZABLE_TYPE)) {
            classNode.addInterface(EXTERNALIZABLE_TYPE);
        }

        Parameter[] writeParams = params(param(OBJECTOUTPUT_TYPE, "out"));
        if (classNode.getDeclaredMethod("writeExternal", writeParams) == null) {
            addStubMethod(classNode, "writeExternal", ACC_PUBLIC,
                    ClassHelper.VOID_TYPE, writeParams,
                    new ClassNode[]{IO_EXCEPTION_TYPE}, EmptyStatement.INSTANCE);
        }

        Parameter[] readParams = params(param(OBJECTINPUT_TYPE, "oin"));
        if (classNode.getDeclaredMethod("readExternal", readParams) == null) {
            addStubMethod(classNode, "readExternal", ACC_PUBLIC,
                    ClassHelper.VOID_TYPE, readParams,
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }
    }
}
