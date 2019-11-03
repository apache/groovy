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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.codehaus.groovy.runtime.memoize.EvictableCache;
import org.codehaus.groovy.runtime.memoize.StampedCommonCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;

/**
 * @since 3.0.0
 */
public abstract class AbstractExtensionMethodCache {
    private final EvictableCache<ClassLoader, Map<String, List<MethodNode>>> cache = new StampedCommonCache<>(new WeakHashMap<>());

    public Map<String, List<MethodNode>> get(ClassLoader loader) {
        return cache.getAndPut(loader, this::getMethodsFromClassLoader);
    }

    private Map<String, List<MethodNode>> getMethodsFromClassLoader(ClassLoader classLoader) {
        final List<ExtensionModule> modules = new LinkedList<>();
        ExtensionModuleScanner scanner = new ExtensionModuleScanner(
                module -> {
                    if (!(module instanceof MetaInfExtensionModule)) return;

                    boolean skip = false;
                    for (ExtensionModule extensionModule : modules) {
                        if (extensionModule.getName().equals(module.getName())) {
                            skip = true;
                            break;
                        }
                    }
                    if (!skip) modules.add(module);
                },
                classLoader
        );
        scanner.scanClasspathModules();

        return makeMethodsUnmodifiable(getMethods(modules));
    }

    /**
     * Returns a map which contains, as the key, the name of a class. The value
     * consists of a list of MethodNode, one for each groovy default method found
     * which is applicable for this class.
     *
     * @param modules extension modules
     * @return
     */
    private Map<String, List<MethodNode>> getMethods(List<ExtensionModule> modules) {
        Set<Class> instanceExtClasses = new LinkedHashSet<>();
        Set<Class> staticExtClasses = new LinkedHashSet<>();
        for (ExtensionModule module : modules) {
            MetaInfExtensionModule extensionModule = (MetaInfExtensionModule) module;
            instanceExtClasses.addAll(extensionModule.getInstanceMethodsExtensionClasses());
            staticExtClasses.addAll(extensionModule.getStaticMethodsExtensionClasses());
        }
        Map<String, List<MethodNode>> methods = new HashMap<>();

        addAdditionalClassesToScan(instanceExtClasses, staticExtClasses);

        scan(methods, staticExtClasses, true);
        scan(methods, instanceExtClasses, false);

        return methods;
    }

    private Map<String, List<MethodNode>> makeMethodsUnmodifiable(Map<String, List<MethodNode>> methods) {
        methods.replaceAll((k, v) -> Collections.unmodifiableList(v));

        return Collections.unmodifiableMap(methods);
    }

    protected abstract void addAdditionalClassesToScan(Set<Class> instanceExtClasses, Set<Class> staticExtClasses);

    private void scan(Map<String, List<MethodNode>> accumulator, Iterable<Class> allClasses, boolean isStatic) {
        Predicate<MethodNode> methodFilter = getMethodFilter();
        Function<MethodNode, String> methodMapper = getMethodMapper();

        for (Class dgmLikeClass : allClasses) {
            ClassNode cn = makeWithoutCaching(dgmLikeClass, true);
            for (MethodNode methodNode : cn.getMethods()) {
                if (!(methodNode.isStatic() && methodNode.isPublic()) || methodNode.getParameters().length == 0) continue;
                if (methodFilter.test(methodNode)) continue;

                accumulate(accumulator, isStatic, methodNode, methodMapper);
            }
        }
    }

    protected abstract Predicate<MethodNode> getMethodFilter();
    protected abstract Function<MethodNode, String> getMethodMapper();

    private void accumulate(Map<String, List<MethodNode>> accumulator, boolean isStatic, MethodNode metaMethod,
                                   Function<MethodNode, String> mapperFunction) {

        Parameter[] types = metaMethod.getParameters();
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

        String key = mapperFunction.apply(metaMethod);

        List<MethodNode> nodes = accumulator.computeIfAbsent(key, k -> new ArrayList<>());
        nodes.add(node);
    }
}
