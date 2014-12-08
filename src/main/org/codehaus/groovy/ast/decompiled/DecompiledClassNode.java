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
    private boolean lazyInitDone = false;

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
            ClassNode outerClass = resolver.resolveClass(className.substring(0, idx));
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
        lazyInit();
        return super.getGenericsTypes();
    }

    @Override
    public boolean isUsingGenerics() {
        lazyInit();
        return super.isUsingGenerics();
    }

    @Override
    public List<FieldNode> getFields() {
        lazyInit();
        return super.getFields();
    }

    @Override
    public ClassNode[] getInterfaces() {
        lazyInit();
        return super.getInterfaces();
    }

    @Override
    public List<MethodNode> getMethods() {
        lazyInit();
        return super.getMethods();
    }

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        lazyInit();
        return super.getDeclaredConstructors();
    }

    @Override
    public FieldNode getDeclaredField(String name) {
        lazyInit();
        return super.getDeclaredField(name);
    }

    @Override
    public List<MethodNode> getDeclaredMethods(String name) {
        lazyInit();
        return super.getDeclaredMethods(name);
    }

    @Override
    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        lazyInit();
        return super.getUnresolvedSuperClass(useRedirect);
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces(boolean useRedirect) {
        lazyInit();
        return super.getUnresolvedInterfaces(useRedirect);
    }

    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInit();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInit();
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

    private void lazyInit() {
        synchronized (lazyInitLock) {
            if (!lazyInitDone) {
                ClassSignatureParser.configureClass(this, this.classData, this.resolver);

                addAnnotations(classData, this);

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

                lazyInitDone = true;
            }
        }
    }

    private <T extends AnnotatedNode> T addAnnotations(MemberStub stub, T node) {
        for (AnnotationStub annotation : stub.annotations) {
            node.addAnnotation(Annotations.createAnnotationNode(annotation, resolver));
        }
        return node;
    }

}