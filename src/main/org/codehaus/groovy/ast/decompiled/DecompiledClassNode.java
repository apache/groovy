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
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationUnit;
import org.objectweb.asm.Type;

import java.util.List;

/**
 * @author Peter Gromov
 */
class DecompiledClassNode extends ClassNode {
    private final ClassStub classData;
    private final ClassNodeResolver resolver;
    private final CompilationUnit unit;
    private boolean lazyInitDone = false;

    public DecompiledClassNode(ClassStub data, ClassNodeResolver resolver, CompilationUnit unit) {
        super(data.className, data.accessModifiers, null, null, MixinNode.EMPTY_ARRAY);
        classData = data;
        this.resolver = resolver;
        this.unit = unit;
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
    public List<AnnotationNode> getAnnotations() { //todo
        lazyInit();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) { //todo
        lazyInit();
        return super.getAnnotations(type);
    }

    private void lazyInit() {
        synchronized (lazyInitLock) {
            if (!lazyInitDone) {

                if (classData.superName != null) {
                    setSuperClass(resolveClassNode(AsmDecompiler.fromInternalName(classData.superName)));
                }

                ClassNode[] interfaces = new ClassNode[classData.interfaceNames.length];
                for (int i = 0; i < classData.interfaceNames.length; i++) {
                    interfaces[i] = resolveClassNode(AsmDecompiler.fromInternalName(classData.interfaceNames[i]));
                }
                setInterfaces(interfaces);

                for (MethodStub method : classData.methods) {
                    //todo method generics, annotations
                    Type[] argumentTypes = Type.getArgumentTypes(method.desc);
                    Parameter[] parameters = new Parameter[argumentTypes.length];
                    for (int i = 0; i < argumentTypes.length; i++) {
                        parameters[i] = new Parameter(resolveType(argumentTypes[i]), "param" + i);
                    }

                    ClassNode[] exceptions = new ClassNode[method.exceptions.length];
                    for (int i = 0; i < method.exceptions.length; i++) {
                        exceptions[i] = resolveClassNode(AsmDecompiler.fromInternalName(method.exceptions[i]));
                    }

                    if ("<init>".equals(method.methodName)) {
                        addConstructor(new ConstructorNode(method.accessModifiers, parameters, exceptions, null));
                    } else {
                        ClassNode returnType = resolveType(Type.getReturnType(method.desc));
                        addMethod(new MethodNode(method.methodName, method.accessModifiers, returnType, parameters, exceptions, null));
                    }
                }

                for (FieldStub field : classData.fields) {
                    //todo field generics, annotations
                    addField(new FieldNode(field.fieldName, field.accessModifiers, resolveType(Type.getType(field.desc)), this, null));
                }

                lazyInitDone = true;
            }
        }
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

        return resolveClassNode(className);
    }

    private ClassNode resolveClassNode(String className) {
        ClassNodeResolver.LookupResult lookupResult = resolver.resolveName(className, unit);
        if (lookupResult == null || lookupResult.getClassNode() == null) {
            throw new NoClassDefFoundError(className);
        }

        return lookupResult.getClassNode();
    }

}
