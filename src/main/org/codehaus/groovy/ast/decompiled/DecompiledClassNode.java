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
import org.codehaus.groovy.ast.expr.*;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
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
                    //todo method generics
                    Type[] argumentTypes = Type.getArgumentTypes(method.desc);
                    Parameter[] parameters = new Parameter[argumentTypes.length];
                    for (int i = 0; i < argumentTypes.length; i++) {
                        parameters[i] = new Parameter(resolveType(argumentTypes[i]), "param" + i);
                    }

                    for (Map.Entry<Integer, List<AnnotationStub>> entry : method.parameterAnnotations.entrySet()) {
                        for (AnnotationStub stub : entry.getValue()) {
                            parameters[entry.getKey()].addAnnotation(createAnnotationNode(stub));
                        }
                    }

                    ClassNode[] exceptions = new ClassNode[method.exceptions.length];
                    for (int i = 0; i < method.exceptions.length; i++) {
                        exceptions[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(method.exceptions[i]));
                    }

                    if ("<init>".equals(method.methodName)) {
                        addConstructor(addAnnotations(method, new ConstructorNode(method.accessModifiers, parameters, exceptions, null)));
                    } else {
                        ClassNode returnType = resolveType(Type.getReturnType(method.desc));
                        addMethod(addAnnotations(method, new MethodNode(method.methodName, method.accessModifiers, returnType, parameters, exceptions, null)));
                    }
                }

                for (FieldStub field : classData.fields) {
                    //todo field generics
                    addField(addAnnotations(field, new FieldNode(field.fieldName, field.accessModifiers, resolveType(Type.getType(field.desc)), this, null)));
                }

                lazyInitDone = true;
            }
        }
    }

    private <T extends AnnotatedNode> T addAnnotations(MemberStub stub, T node) {
        for (AnnotationStub annotation : stub.annotations) {
            node.addAnnotation(createAnnotationNode(annotation));
        }
        return node;
    }

    private AnnotationNode createAnnotationNode(AnnotationStub annotation) {
        AnnotationNode node = new AnnotationNode(resolveType(Type.getType(annotation.className)));
        for (Map.Entry<String, Object> entry : annotation.members.entrySet()) {
            node.addMember(entry.getKey(), annotationValueToExpression(entry.getValue()));
        }
        return node;
    }

    private Expression annotationValueToExpression(Object value) {
        if (value instanceof TypeWrapper) {
            return new ClassExpression(resolveType(Type.getType(((TypeWrapper) value).desc)));
        }

        if (value instanceof EnumConstantWrapper) {
            EnumConstantWrapper wrapper = (EnumConstantWrapper) value;
            return new PropertyExpression(new ClassExpression(resolveType(Type.getType(wrapper.enumDesc))), wrapper.constant);
        }

        if (value instanceof AnnotationStub) {
            return new AnnotationConstantExpression(createAnnotationNode((AnnotationStub) value));
        }

        if (value != null && value.getClass().isArray()) {
            ListExpression elementExprs = new ListExpression();
            int len = Array.getLength(value);
            for (int i = 0; i != len; ++i) {
                elementExprs.addExpression(annotationValueToExpression(Array.get(value, i)));
            }
            return elementExprs;
        }

        if (value instanceof List) {
            ListExpression elementExprs = new ListExpression();
            for (Object o : (List) value) {
                elementExprs.addExpression(annotationValueToExpression(o));
            }
            return elementExprs;
        }

        return new ConstantExpression(value);
    }

    private ClassNode resolveType(Type type) {
        if (type.getSort() == Type.ARRAY) {
            ClassNode result = resolveNonArrayType(type.getElementType());
            for (int i = 0; i < type.getDimensions(); i++) {
                result = result.makeArray();
            }
            return result;
        }

        return resolveNonArrayType(type);
    }

    private ClassNode resolveNonArrayType(Type type) {
        String className = type.getClassName();
        if (type.getSort() != Type.OBJECT) {
            return ClassHelper.make(className);
        }

        return resolver.resolveClass(className);
    }

}
