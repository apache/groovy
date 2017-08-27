/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;

public class ClassNodeUtils {
    public static void addInterfaceMethods(ClassNode cnode, Map<String, MethodNode> methodsMap) {
        // add in unimplemented abstract methods from the interfaces
        for (ClassNode iface : cnode.getInterfaces()) {
            Map<String, MethodNode> ifaceMethodsMap = iface.getDeclaredMethodsMap();
            for (String methSig : ifaceMethodsMap.keySet()) {
                if (!methodsMap.containsKey(methSig)) {
                    MethodNode methNode = ifaceMethodsMap.get(methSig);
                    methodsMap.put(methSig, methNode);
                }
            }
        }
    }

    public static Map<String, MethodNode> getDeclaredMethodMapsFromInterfaces(ClassNode classNode) {
        Map<String, MethodNode> result = new HashMap<String, MethodNode>();
        ClassNode[] interfaces = classNode.getInterfaces();
        for (ClassNode iface : interfaces) {
            result.putAll(iface.getDeclaredMethodsMap());
        }
        return result;
    }

    public static void addDeclaredMethodMapsFromSuperInterfaces(ClassNode cn, Map<String, MethodNode> allInterfaceMethods) {
        List cnInterfaces = Arrays.asList(cn.getInterfaces());
        ClassNode sn = cn.getSuperClass();
        while (sn != null && !sn.equals(ClassHelper.OBJECT_TYPE)) {
            ClassNode[] interfaces = sn.getInterfaces();
            for (ClassNode iface : interfaces) {
                if (!cnInterfaces.contains(iface)) {
                    allInterfaceMethods.putAll(iface.getDeclaredMethodsMap());
                }
            }
            sn = sn.getSuperClass();
        }
    }

    /**
     * Returns true if the given method has a possibly matching static method with the given name and arguments.
     *
     * @param cNode     the ClassNode of interest
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @param trySpread whether to try to account for SpreadExpressions within the arguments
     * @return true if a matching method was found
     */
    public static boolean hasPossibleStaticMethod(ClassNode cNode, String name, Expression arguments, boolean trySpread) {
        int count = 0;
        boolean foundSpread = false;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            for (Expression arg : tuple.getExpressions()) {
                if (arg instanceof SpreadExpression) {
                    foundSpread = true;
                } else {
                    count++;
                }
            }
        } else if (arguments instanceof MapExpression) {
            count = 1;
        }

        for (MethodNode method : cNode.getMethods(name)) {
            if (method.isStatic()) {
                Parameter[] parameters = method.getParameters();
                // do fuzzy match for spread case: count will be number of non-spread args
                if (trySpread && foundSpread && parameters.length >= count) return true;

                if (parameters.length == count) return true;

                // handle varargs case
                if (parameters.length > 0 && parameters[parameters.length - 1].getType().isArray()) {
                    if (count >= parameters.length - 1) return true;
                    // fuzzy match any spread to a varargs
                    if (trySpread && foundSpread) return true;
                }

                // handle parameters with default values
                int nonDefaultParameters = 0;
                for (Parameter parameter : parameters) {
                    if (!parameter.hasInitialExpression()) {
                        nonDefaultParameters++;
                    }
                }

                if (count < parameters.length && nonDefaultParameters <= count) {
                    return true;
                }
                // TODO handle spread with nonDefaultParams?
            }
        }
        return false;
    }

    /**
     * Return true if we have a static accessor
     */
    public static boolean hasPossibleStaticProperty(ClassNode candidate, String methodName) {
        // assume explicit static method call checked first so we can assume a simple check here
        if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
            return false;
        }
        String propName = getPropNameForAccessor(methodName);
        PropertyNode pNode = getStaticProperty(candidate, propName);
        return pNode != null && (methodName.startsWith("get") || boolean_TYPE.equals(pNode.getType()));
    }

    /**
     * Returns the property name, e.g. age, given an accessor name, e.g. getAge.
     * Returns the original if a valid prefix cannot be removed.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return the property name, e.g. age, or original if not a valid property accessor name
     */
    public static String getPropNameForAccessor(String accessorName) {
        if (!isValidAccessorName(accessorName)) return accessorName;
        int prefixLength = accessorName.startsWith("is") ? 2 : 3;
        return String.valueOf(accessorName.charAt(prefixLength)).toLowerCase() + accessorName.substring(prefixLength + 1);
    }

    /**
     * Detect whether the given accessor name starts with "get", "set" or "is" followed by at least one character.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return true if a valid prefix is found
     */
    public static boolean isValidAccessorName(String accessorName) {
        if (accessorName.startsWith("get") || accessorName.startsWith("is") || accessorName.startsWith("set")) {
            int prefixLength = accessorName.startsWith("is") ? 2 : 3;
            return accessorName.length() > prefixLength;
        };
        return false;
    }

    public static boolean hasStaticProperty(ClassNode cNode, String propName) {
        return getStaticProperty(cNode, propName) != null;
    }

    /**
     * Detect whether a static property with the given name is within the class
     * or a super class.
     *
     * @param cNode the ClassNode of interest
     * @param propName the property name
     * @return the static property if found or else null
     */
    public static PropertyNode getStaticProperty(ClassNode cNode, String propName) {
        ClassNode classNode = cNode;
        while (classNode != null) {
            for (PropertyNode pn : classNode.getProperties()) {
                if (pn.getName().equals(propName) && pn.isStatic()) return pn;
            }
            classNode = classNode.getSuperClass();
        }
        return null;
    }

    /**
     * Detect whether a given ClassNode is a inner class (non-static).
     *
     * @param currentClass the ClassNode of interest
     * @return true if the given node is a (non-static) inner class, else false
     */
    public static boolean isInnerClass(ClassNode currentClass) {
        return currentClass.redirect().getOuterClass() != null
                && !Modifier.isStatic(currentClass.getModifiers());
    }
}
