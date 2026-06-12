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
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;

/**
 * Utility methods for discovering and working with bean properties on {@link ClassNode} instances.
 *
 * <p>This utility supports JavaBean property conventions including:
 * <ul>
 *   <li>Explicit properties defined via the {@code @Property} annotation</li>
 *   <li>Pseudo-properties from getter methods following JavaBean naming conventions (getXxx, isXxx)</li>
 *   <li>Pseudo-properties from setter methods following JavaBean naming conventions (setXxx)</li>
 *   <li>Inherited properties from superclasses and interfaces</li>
 *   <li>Static property filtering</li>
 * </ul>
 *
 * <p>Commonly used in AST transformation and type checking to analyze class structure and
 * generate property access patterns.
 *
 * @see PropertyNode for property representation
 * @see ClassNode for class information
 */
public class BeanUtils {
    private BeanUtils() {}

    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";
    private static final ClassNode INTERNAL_TYPE = ClassHelper.make(Internal.class);

    /**
     * Discovers all properties in a class, optionally including inherited and pseudo-properties.
     * Pseudo-properties are derived from getter methods following JavaBean naming conventions.
     *
     * @param type the {@link ClassNode} to analyze
     * @param includeSuperProperties whether to include properties from superclasses
     * @param includeStatic whether to include static properties
     * @param includePseudoGetters whether to include pseudo-properties created from getXxx/isXxx methods
     * @return a list of all discovered {@link PropertyNode}s, may be empty
     * @see #getAllProperties(ClassNode, boolean, boolean, boolean, boolean, boolean)
     */
    public static List<PropertyNode> getAllProperties(ClassNode type, boolean includeSuperProperties, boolean includeStatic, boolean includePseudoGetters) {
        return getAllProperties(type, includeSuperProperties, includeStatic, includePseudoGetters, false, false);
    }

    /**
     * Discovers all properties in a class, optionally including both getter and setter pseudo-properties.
     * Pseudo-properties include JavaBean getters (getXxx/isXxx) and setters (setXxx) without corresponding fields.
     *
     * @param type the {@link ClassNode} to analyze
     * @param includeSuperProperties whether to include properties from superclasses
     * @param includeStatic whether to include static properties
     * @param includePseudoGetters whether to include pseudo-properties from getter methods
     * @param includePseudoSetters whether to include pseudo-properties from setter methods
     * @param superFirst whether to list superclass properties before current class properties
     * @return a list of all discovered {@link PropertyNode}s, with order controlled by superFirst, may be empty
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
        addPseudoProperties(origType, type, result, names, includeStatic, includePseudoGetters, includePseudoSetters, includeSuperProperties);
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

    /**
     * Adds pseudo-properties for getters and setters to a property list.
     * Pseudo-properties are created from methods following JavaBean naming conventions
     * but without corresponding field declarations.
     *
     * @param origType the original type being analyzed (used for access checks)
     * @param cNode the class node to scan for getter/setter methods
     * @param result the list to accumulate discovered pseudo-properties
     * @param names a set tracking property names already discovered (to prevent duplicates)
     * @param includeStatic whether to include static methods
     * @param includePseudoGetters whether to add properties from getter methods
     * @param includePseudoSetters whether to add properties from setter methods
     */
    public static void addPseudoProperties(ClassNode origType, ClassNode cNode, List<PropertyNode> result, Set<String> names, boolean includeStatic, boolean includePseudoGetters, boolean includePseudoSetters) {
        addPseudoProperties(origType, cNode, result, names, includeStatic, includePseudoGetters, includePseudoSetters, true);
    }

    /**
     * Adds pseudo-properties for getters and setters to a property list, optionally traversing superclasses.
     * Pseudo-properties are created from methods following JavaBean naming conventions
     * but without corresponding field declarations. Methods marked with {@code @Internal} are skipped.
     *
     * @param origType the original type being analyzed (used for access checks)
     * @param cNode the class node to scan for getter/setter methods
     * @param result the list to accumulate discovered pseudo-properties
     * @param names a set tracking property names already discovered (to prevent duplicates)
     * @param includeStatic whether to include static methods
     * @param includePseudoGetters whether to add properties from getXxx/isXxx getter methods
     * @param includePseudoSetters whether to add properties from setXxx setter methods
     * @param traverseSuperClasses whether to include inherited methods from superclasses
     */
    public static void addPseudoProperties(ClassNode origType, ClassNode cNode, List<PropertyNode> result, Set<String> names, boolean includeStatic, boolean includePseudoGetters, boolean includePseudoSetters, boolean traverseSuperClasses) {
        if (!includePseudoGetters && !includePseudoSetters) return;
        List<MethodNode> methods = traverseSuperClasses ?
                cNode.getAllDeclaredMethods() :
                cNode.getMethods();
        for (MethodNode mNode : methods) {
            if (!includeStatic && mNode.isStatic()) continue;
            if (hasAnnotation(mNode, INTERNAL_TYPE)) continue;
            String name = mNode.getName();
            if ((name.length() <= 3 && !name.startsWith(IS_PREFIX)) || "getClass".equals(name) || "getMetaClass".equals(name) || "getDeclaringClass".equals(name)) {
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
                } else if (includePseudoGetters && name.startsWith(IS_PREFIX) && isPrimitiveBoolean(paramType)) {
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
