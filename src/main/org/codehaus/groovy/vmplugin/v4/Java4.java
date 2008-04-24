/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.vmplugin.v4;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.vmplugin.VMPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * java 4 based functions
 * @author Jochen Theodorou
 *
 */
public class Java4 implements VMPlugin {
    private static Class[] EMPTY_CLASS_ARRAY = new Class[0];

    public void setAdditionalClassInformation(ClassNode c) {
        return;
    }

    public Class[] getPluginDefaultGroovyMethods() {
        return EMPTY_CLASS_ARRAY;
    }

    public void configureAnnotation(AnnotationNode an) {
        // do nothing
    }
    public void configureClassNode(CompileUnit compileUnit, ClassNode classNode) {
        Class clazz = classNode.getTypeClass();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            classNode.addField(fields[i].getName(), fields[i].getModifiers(), classNode, null);
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            MethodNode mn = new MethodNode(m.getName(), m.getModifiers(), ClassHelper.make(m.getReturnType()), createParameters(m.getParameterTypes()), ClassHelper.make(m.getExceptionTypes()), null);
            classNode.addMethod(mn);
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor ctor = constructors[i];
            classNode.addConstructor(ctor.getModifiers(), createParameters(ctor.getParameterTypes()), ClassHelper.make(ctor.getExceptionTypes()), null);
        }

        Class sc = clazz.getSuperclass();
        if (sc != null) classNode.setUnresolvedSuperClass(getPrimaryClassNode(compileUnit,sc));

        buildInterfaceTypes(compileUnit,classNode,clazz);

    }

    private ClassNode getPrimaryClassNode(CompileUnit compileUnit,Class clazz) {
        // there might be a new super class from the compile unit,
        // we want to use this instead of simply referencing the old
        // class
        ClassNode result = null;
        if (compileUnit!=null) {
            result = compileUnit.getClass(clazz.getName());
        }
        if (result==null) result = ClassHelper.make(clazz);
        return result;
    }

    private void buildInterfaceTypes(CompileUnit compileUnit, ClassNode classNode, Class c) {
        Class[] interfaces = c.getInterfaces();
        ClassNode[] ret = new ClassNode[interfaces.length];
        for (int i=0;i<interfaces.length;i++){
            ret[i] = getPrimaryClassNode(compileUnit, interfaces[i]);
        }
        classNode.setInterfaces(ret);
    }

    private Parameter[] createParameters(Class[] types) {
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        int size = types.length;
        if (size > 0) {
            parameters = new Parameter[size];
            for (int i = 0; i < size; i++) {
                parameters[i] = createParameter(types[i], i);
            }
        }
        return parameters;
    }

    private Parameter createParameter(Class parameterType, int idx) {
        return new Parameter(ClassHelper.make(parameterType), "param" + idx);
    }

}
