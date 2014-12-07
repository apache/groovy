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
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

/**
 * @author Peter Gromov
 */
class DecompiledClassNode extends ClassNode {
    private final ClassStub classData;
    private final AsmReferenceResolver resolver;
    private boolean lazyInitDone = false;

    public DecompiledClassNode(ClassStub data, AsmReferenceResolver resolver) {
        super(data.className, data.accessModifiers, null, null, MixinNode.EMPTY_ARRAY);
        classData = data;
        this.resolver = resolver;
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
    public boolean isGenericsPlaceHolder() {
        lazyInit();
        return super.isGenericsPlaceHolder();
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
    public ClassNode getUnresolvedSuperClass() {
        lazyInit();
        return super.getUnresolvedSuperClass();
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces() {
        lazyInit();
        return super.getUnresolvedInterfaces();
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

    private void lazyInit() {
        synchronized (lazyInitLock) {
            if (!lazyInitDone) {
                ClassSignatureParser.configureClass(this, this.classData, this.resolver);

                addAnnotations(classData, this);

                for (MethodStub method : classData.methods) {
                    MethodNode node = addAnnotations(method, parseMethodSignature(method));
                    if (node instanceof ConstructorNode) {
                        addConstructor((ConstructorNode) node);
                    } else {
                        addMethod(node);
                    }
                }

                for (FieldStub field : classData.fields) {
                    //todo field generics
                    addField(addAnnotations(field, new FieldNode(field.fieldName, field.accessModifiers, resolver.resolveType(Type.getType(field.desc)), this, null)));
                }

                lazyInitDone = true;
            }
        }
    }

    private MethodNode parseMethodSignature(MethodStub method) {
        //todo method generics
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        Parameter[] parameters = new Parameter[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            parameters[i] = new Parameter(resolver.resolveType(argumentTypes[i]), "param" + i);
        }

        for (Map.Entry<Integer, List<AnnotationStub>> entry : method.parameterAnnotations.entrySet()) {
            for (AnnotationStub stub : entry.getValue()) {
                parameters[entry.getKey()].addAnnotation(Annotations.createAnnotationNode(stub, resolver));
            }
        }

        ClassNode[] exceptions = new ClassNode[method.exceptions.length];
        for (int i = 0; i < method.exceptions.length; i++) {
            exceptions[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(method.exceptions[i]));
        }

        return "<init>".equals(method.methodName) ?
                new ConstructorNode(method.accessModifiers, parameters, exceptions, null) :
                new MethodNode(method.methodName, method.accessModifiers, resolver.resolveType(Type.getReturnType(method.desc)), parameters, exceptions, null);
    }

    private <T extends AnnotatedNode> T addAnnotations(MemberStub stub, T node) {
        for (AnnotationStub annotation : stub.annotations) {
            node.addAnnotation(Annotations.createAnnotationNode(annotation, resolver));
        }
        return node;
    }

}