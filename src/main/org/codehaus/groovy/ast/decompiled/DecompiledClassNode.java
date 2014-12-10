/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.*;

import java.util.List;

/**
 * @author Peter Gromov
 */
public class DecompiledClassNode extends ClassNode {
    private final ClassStub classData;
    private final AsmReferenceResolver resolver;
    private boolean supersInitialized = false;
    private boolean membersInitialized = false;

    public DecompiledClassNode(ClassStub data, AsmReferenceResolver resolver) {
        super(data.className, getFullModifiers(data, resolver), null, null, MixinNode.EMPTY_ARRAY);
        classData = data;
        this.resolver = resolver;
        isPrimaryNode = false;
    }

    private static int getFullModifiers(ClassStub data, AsmReferenceResolver resolver) {
        int result = data.accessModifiers;
        String className = data.className;
        int idx = className.lastIndexOf('$');
        if (idx > 0) {
            ClassNode outerClass = resolver.resolveClassNullable(className.substring(0, idx));
            if (outerClass instanceof DecompiledClassNode) {
                Integer outerModifiers = ((DecompiledClassNode) outerClass).classData.innerClassModifiers.get(className.substring(idx + 1));
                if (outerModifiers != null) {
                    result |= outerModifiers;
                }
            }
        }
        return result;
    }

    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInitSupers();
        return super.getGenericsTypes();
    }

    @Override
    public boolean isUsingGenerics() {
        lazyInitSupers();
        return super.isUsingGenerics();
    }

    @Override
    public List<FieldNode> getFields() {
        lazyInitMembers();
        return super.getFields();
    }

    @Override
    public ClassNode[] getInterfaces() {
        lazyInitSupers();
        return super.getInterfaces();
    }

    @Override
    public List<MethodNode> getMethods() {
        lazyInitMembers();
        return super.getMethods();
    }

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        lazyInitMembers();
        return super.getDeclaredConstructors();
    }

    @Override
    public FieldNode getDeclaredField(String name) {
        lazyInitMembers();
        return super.getDeclaredField(name);
    }

    @Override
    public List<MethodNode> getDeclaredMethods(String name) {
        lazyInitMembers();
        return super.getDeclaredMethods(name);
    }

    @Override
    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedSuperClass(useRedirect);
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedInterfaces(useRedirect);
    }

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
    public void setRedirect(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsPlaceHolder(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUsingGenerics(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public Class getTypeClass() {
        return resolver.resolveJvmClass(getName());
    }

    private void lazyInitSupers() {
        synchronized (lazyInitLock) {
            if (!supersInitialized) {
                ClassSignatureParser.configureClass(this, this.classData, this.resolver);
                addAnnotations(classData, this);
                supersInitialized = true;
            }
        }

    }

    private void lazyInitMembers() {
        synchronized (lazyInitLock) {
            if (!membersInitialized) {
                for (MethodStub method : classData.methods) {
                    MethodNode node = addAnnotations(method, MemberSignatureParser.createMethodNode(resolver, method));
                    if (node instanceof ConstructorNode) {
                        addConstructor((ConstructorNode) node);
                    } else {
                        addMethod(node);
                    }
                }

                for (FieldStub field : classData.fields) {
                    addField(addAnnotations(field, MemberSignatureParser.createFieldNode(field, resolver, this)));
                }

                membersInitialized = true;
            }
        }
    }

    private <T extends AnnotatedNode> T addAnnotations(MemberStub stub, T node) {
        for (AnnotationStub annotation : stub.annotations) {
            AnnotationNode annotationNode = Annotations.createAnnotationNode(annotation, resolver);
            if (annotationNode != null) {
                node.addAnnotation(annotationNode);
            }
        }
        return node;
    }

}