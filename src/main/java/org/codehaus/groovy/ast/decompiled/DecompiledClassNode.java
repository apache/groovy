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
package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.classgen.Verifier;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link ClassNode} kind representing the classes coming from *.class files decompiled using ASM.
 *
 * @see AsmDecompiler
 */
public class DecompiledClassNode extends ClassNode {

    private final ClassStub classData;
    private final AsmReferenceResolver resolver;

    public DecompiledClassNode(final ClassStub classData, final AsmReferenceResolver resolver) {
        super(classData.className, getModifiers(classData), null, null, MixinNode.EMPTY_ARRAY);
        this.classData = classData;
        this.resolver = resolver;
        isPrimaryNode = false;
    }

    /**
     * Handle the case of inner classes returning the correct modifiers from
     * the INNERCLASS reference since the top-level modifiers for inner classes
     * won't include static or private/protected.
     */
    private static int getModifiers(ClassStub classData) {
        return (classData.innerClassModifiers != -1 ? classData.innerClassModifiers : classData.accessModifiers);
    }

    //

    public long getCompilationTimeStamp() {
        if (classData.fields != null) {
            for (FieldStub field : classData.fields) {
                if (Modifier.isStatic(field.accessModifiers)) {
                    Long timestamp = Verifier.getTimestampFromFieldName(field.fieldName);
                    if (timestamp != null) {
                        return timestamp;
                    }
                }
            }
        }
        return Long.MAX_VALUE;
    }

    @Override
    public Class getTypeClass() {
        return resolver.resolveJvmClass(getName());
    }

    public boolean isParameterized() {
        return (classData.signature != null && classData.signature.charAt(0) == '<');
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public String setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRedirect(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUsingGenerics(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsPlaceHolder(boolean b) {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------

    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInitSupers();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInitSupers();
        return super.getAnnotations(type);
    }

    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInitSupers();
        return super.getGenericsTypes();
    }

    @Override
    public ClassNode[] getInterfaces() {
        lazyInitSupers();
        return super.getInterfaces();
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedInterfaces(useRedirect);
    }

    @Override
    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedSuperClass(useRedirect);
    }

    @Override
    public boolean isUsingGenerics() {
        lazyInitSupers();
        return super.isUsingGenerics();
    }

    private volatile boolean supersInitialized;

    private void lazyInitSupers() {
        if (supersInitialized) return;

        synchronized (lazyInitLock) {
            if (!supersInitialized) {
                ClassSignatureParser.configureClass(this, classData, resolver);
                addAnnotations(classData, this);
                supersInitialized = true;
            }
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        lazyInitMembers();
        return super.getDeclaredConstructors();
    }

    @Override
    public FieldNode getDeclaredField(final String name) {
        lazyInitMembers();
        return super.getDeclaredField(name);
    }

    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        lazyInitMembers();
        return super.getDeclaredMethods(name);
    }

    @Override
    public List<FieldNode> getFields() {
        lazyInitMembers();
        return super.getFields();
    }

    @Override
    public List<MethodNode> getMethods() {
        lazyInitMembers();
        return super.getMethods();
    }

    private volatile boolean membersInitialized;

    private void lazyInitMembers() {
        if (membersInitialized) return;

        synchronized (lazyInitLock) {
            if (!membersInitialized) {
                if (classData.methods != null) {
                    for (MethodStub method : classData.methods) {
                        if ("<init>".equals(method.methodName)) {
                            addConstructor(createConstructor(method));
                        } else {
                            addMethod(createMethodNode(method));
                        }
                    }
                }

                if (classData.fields != null) {
                    for (FieldStub field : classData.fields) {
                        addField(createFieldNode(field));
                    }
                }

                membersInitialized = true;
            }
        }
    }

    private FieldNode createFieldNode(final FieldStub field) {
        Supplier<FieldNode> fieldNodeSupplier = () -> addAnnotations(field, MemberSignatureParser.createFieldNode(field, resolver, this));

        if ((field.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyFieldNode(fieldNodeSupplier, field.fieldName);
        }

        return fieldNodeSupplier.get();
    }

    private MethodNode createMethodNode(final MethodStub method) {
        Supplier<MethodNode> methodNodeSupplier = () -> addAnnotations(method, MemberSignatureParser.createMethodNode(resolver, method));

        if ((method.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyMethodNode(methodNodeSupplier, method.methodName);
        }

        return methodNodeSupplier.get();
    }

    private ConstructorNode createConstructor(final MethodStub method) {
        Supplier<ConstructorNode> constructorNodeSupplier = () -> (ConstructorNode) addAnnotations(method, MemberSignatureParser.createMethodNode(resolver, method));

        if ((method.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyConstructorNode(constructorNodeSupplier);
        }

        return constructorNodeSupplier.get();
    }

    private <T extends AnnotatedNode> T addAnnotations(MemberStub stub, T node) {
        List<AnnotationStub> annotations = stub.annotations;
        if (annotations != null) {
            for (AnnotationStub annotation : annotations) {
                AnnotationNode annotationNode = Annotations.createAnnotationNode(annotation, resolver);
                if (annotationNode != null) {
                    node.addAnnotation(annotationNode);
                }
            }
        }
        return node;
    }
}
