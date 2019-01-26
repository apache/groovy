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
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.codehaus.groovy.runtime.memoize.EvictableCache;
import org.codehaus.groovy.runtime.memoize.StampedCommonCache;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TODO share some code with {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.ExtensionMethodCache}
 *
 * @since 2.5.0
 */
class MacroMethodsCache {
    private static final ClassNode MACRO_ANNOTATION_CLASS_NODE = ClassHelper.make(Macro.class);
    private static final EvictableCache<ClassLoader, Map<String, List<MethodNode>>> CACHE = new StampedCommonCache<>(new WeakHashMap<>());

    public static Map<String, List<MethodNode>> get(final ClassLoader classLoader) {
        return CACHE.getAndPut(classLoader, new EvictableCache.ValueProvider<ClassLoader, Map<String, List<MethodNode>>>() {
            @Override
            public Map<String, List<MethodNode>> provide(ClassLoader key) {
                return Collections.unmodifiableMap(getMacroMethodsFromClassLoader(key));
            }
        });
    }

    protected static Map<String, List<MethodNode>> getMacroMethodsFromClassLoader(ClassLoader classLoader) {
        final Map<String, List<MethodNode>> result = new HashMap<>();
        ExtensionModuleScanner.ExtensionModuleListener listener = new ExtensionModuleScanner.ExtensionModuleListener() {
            @Override
            public void onModule(ExtensionModule module) {
                if (!(module instanceof MetaInfExtensionModule)) {
                    return;
                }

                MetaInfExtensionModule extensionModule = (MetaInfExtensionModule) module;

                scanExtClasses(result, extensionModule.getInstanceMethodsExtensionClasses(), false);
                scanExtClasses(result, extensionModule.getStaticMethodsExtensionClasses(), true);
            }
        };

        ExtensionModuleScanner macroModuleScanner = new ExtensionModuleScanner(listener, classLoader);

        macroModuleScanner.scanClasspathModules();

        for (Map.Entry<String, List<MethodNode>> entry : result.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }

        return Collections.unmodifiableMap(result);
    }

    private static void scanExtClasses(Map<String, List<MethodNode>> accumulator, List<Class> classes, boolean isStatic) {
        for (Class dgmLikeClass : classes) {
            ClassNode cn = ClassHelper.makeWithoutCaching(dgmLikeClass, true);
            for (MethodNode metaMethod : cn.getMethods()) {
                Parameter[] types = metaMethod.getParameters();
                if (!(metaMethod.isStatic() && metaMethod.isPublic())) {
                    continue;
                }

                if (types.length == 0) {
                    continue;
                }

                if (metaMethod.getAnnotations(MACRO_ANNOTATION_CLASS_NODE).isEmpty()) {
                    continue;
                }

                Parameter[] parameters = new Parameter[types.length - 1];
                System.arraycopy(types, 1, parameters, 0, parameters.length);
                ExtensionMethodNode node = new ExtensionMethodNode(
                        metaMethod,
                        metaMethod.getName(),
                        metaMethod.getModifiers(),
                        metaMethod.getReturnType(),
                        parameters,
                        ClassNode.EMPTY_ARRAY, null,
                        isStatic);
                node.setGenericsTypes(metaMethod.getGenericsTypes());
                ClassNode declaringClass = types[0].getType();
                node.setDeclaringClass(declaringClass);

                List<MethodNode> macroMethods = accumulator.get(metaMethod.getName());

                if (macroMethods == null) {
                    macroMethods = new ArrayList<>();
                    accumulator.put(metaMethod.getName(), macroMethods);
                }

                macroMethods.add(node);
            }
        }
    }
}
