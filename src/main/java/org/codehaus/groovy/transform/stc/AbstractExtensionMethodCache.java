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

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.codehaus.groovy.runtime.memoize.EvictableCache;
import org.codehaus.groovy.runtime.memoize.StampedCommonCache;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;

/**
 * Per-{@link ClassLoader} cache of extension methods (DGM and friends, or
 * another mapper such as macro methods). Values are immutable lists that
 * also carry a name index, so a named lookup does not scan every method
 * stored under a receiver (there are hundreds on {@code Object}).
 *
 * @since 3.0.0
 */
public abstract class AbstractExtensionMethodCache {
    /** Caches extension methods per class loader. */
    private final EvictableCache<ClassLoader, Map<String, List<MethodNode>>> cache = new StampedCommonCache<>(new WeakHashMap<>());
    /** Caches, per class loader, the names of extension methods that declare {@code @ClassTag(preempt=true)} (GROOVY-12115). */
    private final EvictableCache<ClassLoader, Set<String>> preemptiveNamesCache = new StampedCommonCache<>(new WeakHashMap<>());
    private final String disabledString = SystemUtil.getSystemPropertySafe(getDisablePropertyName());
    private final boolean disabling = disabledString != null;
    private final Set<String> disabledNames = disabling ? new HashSet<>(Arrays.asList(disabledString.split(","))) : null;

    /**
     * Returns the cached extension methods for the supplied class loader.
     * Each list is immutable and indexed by method name.
     */
    public final Map<String, List<MethodNode>> get(ClassLoader loader) {
        return cache.getAndPut(loader, this::getMethodsFromClassLoader);
    }

    /**
     * Returns methods stored under {@code key} whose
     * {@linkplain MethodNode#getName() name} is {@code name}.
     * Never {@code null}; empty when the key is absent or no method of
     * that name exists. The name index is built when the loader's
     * methods are scanned, so this is a hash get rather than a linear
     * scan of the receiver's DGM methods.
     */
    List<MethodNode> get(final ClassLoader loader, final String key, final String name) {
        List<MethodNode> methods = get(loader).get(key);
        if (methods == null || methods.isEmpty()) {
            return Collections.emptyList();
        }
        if (methods instanceof MethodsByName) {
            return ((MethodsByName) methods).named(name);
        }
        List<MethodNode> matches = new ArrayList<>(2);
        for (MethodNode method : methods) {
            if (method.getName().equals(name)) {
                matches.add(method);
            }
        }
        return matches.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(matches);
    }

    /**
     * Drops cached extension methods for the supplied class loader,
     * including derived data such as the preemptive-name set.
     */
    void invalidate(final ClassLoader loader) {
        cache.remove(loader);
        preemptiveNamesCache.remove(loader);
    }

    /**
     * Drops all cached extension methods and derived indexes.
     */
    void invalidateAll() {
        cache.clearAll();
        preemptiveNamesCache.clearAll();
    }

    /**
     * Returns the names of extension methods that declare {@code @ClassTag(preempt=true)}, for the
     * supplied class loader (GROOVY-12115). Preemption matching consults this to skip the full
     * candidate scan for the overwhelmingly common name that has no preemptive extension overload;
     * derived once (lazily) from the already-cached method map. {@link #invalidate(ClassLoader)}
     * and {@link #invalidateAll()} drop this set with the method map so it cannot go stale
     * independently. Out of the box this holds only {@code "withDefault"}.
     */
    Set<String> getPreemptiveNames(ClassLoader loader) {
        return preemptiveNamesCache.getAndPut(loader, l -> {
            Set<String> names = new HashSet<>();
            for (List<MethodNode> overloads : get(l).values()) {
                for (MethodNode method : overloads) {
                    if (ClassTagSupport.declaresPreemptIntent(method)) names.add(method.getName());
                }
            }
            return names.isEmpty() ? Collections.emptySet() : names;
        });
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

    /**
     * Freezes each per-key list as a {@link MethodsByName} so callers can
     * look up overloads by name without a linear scan. {@link #get(ClassLoader, String, String)}
     * relies on this wrapping.
     */
    private Map<String, List<MethodNode>> makeMethodsUnmodifiable(final Map<String, List<MethodNode>> methods) {
        methods.replaceAll((k, v) -> new MethodsByName(v));
        return Collections.unmodifiableMap(methods);
    }

    /**
     * Adds implementation-specific extension classes to the scan sets.
     */
    protected abstract void addAdditionalClassesToScan(Set<Class> instanceExtClasses, Set<Class> staticExtClasses);

    private void scan(Map<String, List<MethodNode>> accumulator, Iterable<Class> allClasses, boolean isStatic) {
        Predicate<MethodNode> methodFilter = getMethodFilter();
        Function<MethodNode, String> methodMapper = getMethodMapper();

        for (Class dgmLikeClass : allClasses) {
            ClassNode cn = makeWithoutCaching(dgmLikeClass, true);
            for (MethodNode methodNode : cn.getMethods()) {
                if (!(methodNode.isStatic() && methodNode.isPublic()) || methodNode.getParameters().length == 0) continue;
                if (methodFilter.test(methodNode)) continue;
                if (disabling && disabledNames.contains(methodNode.getName())) continue;

                accumulate(accumulator, isStatic, methodNode, methodMapper);
            }
        }
    }

    /**
     * Returns the system property used to disable selected extension methods.
     */
    protected abstract String getDisablePropertyName();

    /**
     * Returns a predicate that excludes methods from the cache.
     */
    protected abstract Predicate<MethodNode> getMethodFilter();

    /**
     * Maps an extension method to the cache key used during lookup.
     */
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

    /**
     * Immutable random-access list of extension methods with a name index.
     * Built once per cache key when a loader is scanned; the lists stored
     * in {@link #named(String)} are themselves unmodifiable.
     */
    private static final class MethodsByName extends AbstractList<MethodNode> implements RandomAccess {
        private static final MethodNode[] EMPTY = new MethodNode[0];

        private final MethodNode[] methods;
        private final Map<String, List<MethodNode>> byName;

        MethodsByName(final List<MethodNode> source) {
            this.methods = source.toArray(EMPTY);
            int count = this.methods.length;
            if (count == 0) {
                this.byName = Collections.emptyMap();
            } else if (count == 1) {
                MethodNode m = this.methods[0];
                this.byName = Collections.singletonMap(m.getName(), Collections.singletonList(m));
            } else if (allSameName(this.methods)) {
                this.byName = Collections.singletonMap(this.methods[0].getName(), this);
            } else {
                Map<String, List<MethodNode>> index = new HashMap<>(Math.max(4, (int) (count / 0.75f) + 1));
                for (MethodNode method : this.methods) {
                    index.computeIfAbsent(method.getName(), k -> new ArrayList<>(2)).add(method);
                }
                index.replaceAll((k, v) -> v.size() == 1
                        ? Collections.singletonList(v.get(0))
                        : Collections.unmodifiableList(v));
                this.byName = Collections.unmodifiableMap(index);
            }
        }

        private static boolean allSameName(final MethodNode[] methods) {
            String firstName = methods[0].getName();
            for (int i = 1, n = methods.length; i < n; i += 1) {
                if (!firstName.equals(methods[i].getName())) {
                    return false;
                }
            }
            return true;
        }

        List<MethodNode> named(final String name) {
            List<MethodNode> found = byName.get(name);
            return found != null ? found : Collections.emptyList();
        }

        @Override
        public MethodNode get(final int index) {
            Objects.checkIndex(index, methods.length);
            return methods[index];
        }

        @Override
        public int size() {
            return methods.length;
        }
    }
}
