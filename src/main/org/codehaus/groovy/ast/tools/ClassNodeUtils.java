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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
