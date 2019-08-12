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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Map;

@Deprecated
public class ClassNodeUtils {
    @Deprecated
    public static void addInterfaceMethods(ClassNode cNode, Map<String, MethodNode> methodsMap) {
        org.apache.groovy.ast.tools.ClassNodeUtils.addDeclaredMethodsFromInterfaces(cNode, methodsMap);
    }

    @Deprecated
    public static Map<String, MethodNode> getDeclaredMethodMapsFromInterfaces(ClassNode cNode) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.getDeclaredMethodsFromInterfaces(cNode);
    }

    @Deprecated
    public static void addDeclaredMethodMapsFromSuperInterfaces(ClassNode cNode, Map<String, MethodNode> methodsMap) {
        org.apache.groovy.ast.tools.ClassNodeUtils.addDeclaredMethodsFromAllInterfaces(cNode, methodsMap);
    }

    @Deprecated
    public static boolean hasPossibleStaticMethod(ClassNode cNode, String name, Expression arguments, boolean trySpread) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticMethod(cNode, name, arguments, trySpread);
    }

    @Deprecated
    public static boolean hasPossibleStaticProperty(ClassNode cNode, String methodName) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticProperty(cNode, methodName);
    }

    @Deprecated
    public static String getPropNameForAccessor(String accessorName) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.getPropNameForAccessor(accessorName);
    }

    @Deprecated
    public static boolean isValidAccessorName(String accessorName) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.isValidAccessorName(accessorName);
    }

    @Deprecated
    public static boolean hasStaticProperty(ClassNode cNode, String propName) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.hasStaticProperty(cNode, propName);
    }

    @Deprecated
    public static PropertyNode getStaticProperty(ClassNode cNode, String propName) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.getStaticProperty(cNode, propName);
    }

    @Deprecated
    public static boolean isInnerClass(ClassNode cNode) {
        return org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass(cNode);
    }
}
