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
package org.codehaus.groovy.vmplugin.v8;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.vmplugin.v7.Java7;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java 8 based functions.
 *
 * @since 2.5.0
 */
public class Java8 extends Java7 {

    private final Class<?>[] PLUGIN_DGM;

    public Java8() {
        super();
        List<Class<?>> dgmClasses = new ArrayList<>();
        Collections.addAll(dgmClasses, (Class<?>[]) super.getPluginDefaultGroovyMethods());
        dgmClasses.add(PluginDefaultGroovyMethods.class);
        PLUGIN_DGM = dgmClasses.toArray(new Class<?>[0]);
    }

    @Override
    public Class<?>[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    @Override
    public int getVersion() {
        return 8;
    }

    @Override
    protected int getElementCode(ElementType value) {
        switch (value) {
            case TYPE_PARAMETER:
                return AnnotationNode.TYPE_PARAMETER_TARGET;
            case TYPE_USE:
                return AnnotationNode.TYPE_USE_TARGET;
        }
        return super.getElementCode(value);
    }

    @Override
    protected Parameter[] processParameters(CompileUnit compileUnit, Method m) {
        java.lang.reflect.Parameter[] parameters = m.getParameters();
        Type[] types = m.getGenericParameterTypes();
        Parameter[] params = Parameter.EMPTY_ARRAY;
        if (types.length > 0) {
            params = new Parameter[types.length];
            for (int i = 0; i < params.length; i++) {
                java.lang.reflect.Parameter p = parameters[i];
                String name = p.isNamePresent() ? p.getName() : "param" + i;
                params[i] = makeParameter(compileUnit, types[i], m.getParameterTypes()[i], m.getParameterAnnotations()[i], name);
            }
        }
        return params;
    }
}
