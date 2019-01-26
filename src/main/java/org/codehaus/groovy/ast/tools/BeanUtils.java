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

import groovy.transform.Internal;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.hasAnnotation;
import static org.apache.groovy.util.BeanUtils.decapitalize;

public class BeanUtils {
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";
    private static final ClassNode INTERNAL_TYPE = ClassHelper.make(Internal.class);

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
        return getAllProperties(type, includeSuperProperties, includeStatic, includePseudoGetters, false, false);
    }

    /**
     * Get all properties including JavaBean pseudo properties matching JavaBean getter or setter conventions.
     *
     * @param type the ClassNode
     * @param includeSuperProperties whether to include super properties
     * @param includeStatic whether to include static properties
     * @param includePseudoGetters whether to include JavaBean pseudo (getXXX/isYYY) properties with no corresponding field
     * @param includePseudoSetters whether to include JavaBean pseudo (setXXX) properties with no corresponding field
     * @param superFirst are properties gathered first from parent classes
     * @return the list of found property nodes
     */
    public static List<PropertyNode> getAllProperties(ClassNode type, boolean includeSuperProperties, boolean includeStatic, boolean includePseudoGetters, boolean includePseudoSetters, boolean superFirst) {
        return getAllProperties(type, type, new HashSet<>(), includeSuperProperties, includeStatic, includePseudoGetters, includePseudoSetters, superFirst);
    }

    private static List<PropertyNode> getAllProperties(ClassNode origType, ClassNode type, Set<String> names, boolean includeSuperProperties, boolean includeStatic, boolean includePseudoGetters, boolean includePseudoSetters, boolean superFirst) {
        // TODO add generics support so this can be used for @EAHC
        if (type == null) {
            return new ArrayList<>();
        }
        List<PropertyNode> result = new ArrayList<>();
        if (superFirst && includeSuperProperties) {
            result.addAll(getAllProperties(origType, type.getSuperClass(), names, includeSuperProperties, includeStatic, includePseudoGetters, includePseudoSetters, superFirst));
        }
        addExplicitProperties(type, result, names, includeStatic);
        addPseudoProperties(origType, type, result, names, includeStatic, includePseudoGetters, includePseudoSetters);
        if (!superFirst && includeSuperProperties) {
            result.addAll(getAllProperties(origType, type.getSuperClass(), names, includeSuperProperties, includeStatic, includePseudoGetters, includePseudoSetters, superFirst));
        }
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

    public static void addPseudoProperties(ClassNode origType, ClassNode cNode, List<PropertyNode> result, Set<String> names, boolean includeStatic, boolean includePseudoGetters, boolean includePseudoSetters) {
        if (!includePseudoGetters && !includePseudoSetters) return;
        List<MethodNode> methods = cNode.getAllDeclaredMethods();
        for (MethodNode mNode : methods) {
            if (!includeStatic && mNode.isStatic()) continue;
            if (hasAnnotation(mNode, INTERNAL_TYPE)) continue;
            String name = mNode.getName();
            if ((name.length() <= 3 && !name.startsWith(IS_PREFIX)) || name.equals("getClass") || name.equals("getMetaClass") || name.equals("getDeclaringClass")) {
                // Optimization: skip invalid propertyNames
                continue;
            }
            if (mNode.getDeclaringClass() != origType && mNode.isPrivate()) {
                // skip private super methods
                continue;
            }
            int paramCount = mNode.getParameters().length;
            ClassNode paramType = mNode.getReturnType();
            String propName = null;
            Statement getter = null;
            Statement setter = null;
            if (paramCount == 0) {
                if (includePseudoGetters && name.startsWith(GET_PREFIX)) {
                    // Simple getter
                    propName = decapitalize(name.substring(3));
                    getter = mNode.getCode();
                } else if (includePseudoGetters && name.startsWith(IS_PREFIX) && paramType.equals(ClassHelper.boolean_TYPE)) {
                    // boolean getter
                    propName = decapitalize(name.substring(2));
                    getter = mNode.getCode();
                }
            } else if (paramCount == 1) {
                if (includePseudoSetters && name.startsWith(SET_PREFIX)) {
                    // Simple setter
                    propName = decapitalize(name.substring(3));
                    setter = mNode.getCode();
                    paramType = mNode.getParameters()[0].getType();

                }
            }
            if (propName != null) {
                addIfMissing(cNode, result, names, mNode, paramType, propName, getter, setter);
            }
        }
    }

    private static void addIfMissing(ClassNode cNode, List<PropertyNode> result, Set<String> names, MethodNode mNode, ClassNode returnType, String propName, Statement getter, Statement setter) {
        if (cNode.getProperty(propName) != null) return;
        if (names.contains(propName)) {
            for (PropertyNode pn : result) {
                if (pn.getName().equals(propName) && getter != null && pn.getGetterBlock() == null) {
                    pn.setGetterBlock(getter);
                }
                if (pn.getName().equals(propName) && setter != null && pn.getSetterBlock() == null) {
                    pn.setSetterBlock(setter);
                }
            }
        } else {
            result.add(new PropertyNode(propName, mNode.getModifiers(), returnType, cNode, null, getter, setter));
            names.add(propName);
        }
    }

}
