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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.beans.Introspector.decapitalize;

public class BeanUtils {
    static final String GET_PREFIX = "get";
    static final String IS_PREFIX = "is";

    /**
     * Get all properties including JavaBean pseudo properties matching getter conventions.
     *
     * @param type the ClassNode
     * @param includeSuperProperties whether to include super properties
     * @param includeStatic whether to include static properties
     * @param includePseudoGetters whether to include JavaBean pseudo (getXXX/isYYY) properties with no corresponding field
     * @return the list of found property nodes
     */
    public static List<PropertyNode> getAllProperties(ClassNode type, boolean includeSuperProperties, boolean includeStatic, boolean includePseudoGetters) {
        // TODO add generics support so this can be used for @EAHC
        // TODO add an includePseudoSetters so this can be used for @TupleConstructor
        ClassNode node = type;
        List<PropertyNode> result = new ArrayList<PropertyNode>();
        Set<String> names = new HashSet<String>();
        while (node != null) {
            addExplicitProperties(node, result, names, includeStatic);
            if (!includeSuperProperties) break;
            node = node.getSuperClass();
        }
        addPseudoProperties(type, result, names, includeStatic, includePseudoGetters, includeSuperProperties);
        return result;
    }

    private static void addExplicitProperties(ClassNode cNode, List<PropertyNode> result, Set<String> names, boolean includeStatic) {
        for (PropertyNode pNode : cNode.getProperties()) {
            if (includeStatic || !pNode.isStatic()) {
                if (!names.contains(pNode.getName())) {
                    result.add(pNode);
                    names.add(pNode.getName());
                }
            }
        }
    }

    private static void addPseudoProperties(ClassNode cNode, List<PropertyNode> result, Set<String> names, boolean includeStatic, boolean includePseudoGetters, boolean includeSuperProperties) {
        if (!includePseudoGetters) return;
        List<MethodNode> methods = cNode.getAllDeclaredMethods();
        ClassNode node = cNode.getSuperClass();
        if (includeSuperProperties) {
            while (node != null) {
                for (MethodNode next : node.getAllDeclaredMethods()) {
                    if (!next.isPrivate()) {
                        methods.add(next);
                    }
                }
                node = node.getSuperClass();
            }
        }
        for (MethodNode mNode : methods) {
            if (!includeStatic && mNode.isStatic()) continue;
            String name = mNode.getName();
            if ((name.length() <= 3 && !name.startsWith(IS_PREFIX)) || name.equals("getClass") || name.equals("getMetaClass") || name.equals("getDeclaringClass")) {
                // Optimization: skip invalid propertyNames
                continue;
            }
            if (mNode.getDeclaringClass() != cNode && mNode.isPrivate()) {
                // skip private super methods
                continue;
            }
            int paramCount = mNode.getParameters().length;
            ClassNode returnType = mNode.getReturnType();
            if (paramCount == 0) {
                if (name.startsWith(GET_PREFIX)) {
                    // Simple getter
                    String propName = decapitalize(name.substring(3));
                    if (!names.contains(propName)) {
                        result.add(new PropertyNode(propName, mNode.getModifiers(), returnType, cNode, null, mNode.getCode(), null));
                        names.add(propName);
                    }
                } else {
                    if (name.startsWith(IS_PREFIX) && returnType.equals(ClassHelper.boolean_TYPE)) {
                        // boolean getter
                        String propName = decapitalize(name.substring(2));
                        if (!names.contains(propName)) {
                            names.add(propName);
                            result.add(new PropertyNode(propName, mNode.getModifiers(), returnType, cNode, null, mNode.getCode(), null));
                        }
                    }
                }
            }
        }
    }
}
