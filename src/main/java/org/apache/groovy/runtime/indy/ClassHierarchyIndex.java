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
package org.apache.groovy.runtime.indy;

import groovy.transform.Internal;
import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.util.ManagedConcurrentLinkedQueue;
import org.codehaus.groovy.util.ReferenceBundle;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ancestor → descendant {@link ClassInfo} index for scoped SwitchPoint fan-out
 * (GROOVY-12191).
 * <p>
 * Built once per {@link ClassInfo} at construction: each type is registered under
 * every MOP-relevant strict supertype (superclass chain, transitive interfaces,
 * and the full JLS array-covariance lattice). Hierarchy invalidation for type
 * {@code T} then walks only the descendants of {@code T} —
 * {@code O(|subtypes of T|)} — instead of scanning every loaded
 * {@code ClassInfo} with {@code isAssignableFrom}.
 * <p>
 * The index is consulted when MetaClass-aware policy chooses hierarchy fan-out
 * (EMC install/update, global EMC, interface / array MetaClass changes). It is
 * not modelling a shared {@code MetaClassImpl} table — each class still has its
 * own MetaClass. Subtype sites re-link so the live <em>missing-method</em>
 * hierarchy walk ({@code MetaClassImpl.findMethodInClassHierarchy}) can observe
 * expando methods installed on ancestors; present methods on the child are not
 * replaced by that walk. Pure {@code MetaClassImpl} class replaces do not walk
 * this index (exact-class invalidation only). See {@link IndyInvalidation}.
 * <p>
 * Keys and values are weakly held so the index does not pin classes or
 * {@code ClassInfo} instances after they become unreachable.
 * <p>
 * <strong>Not a public API.</strong> Wired only from {@link ClassInfo} and
 * {@link IndyInvalidation}.
 *
 * @since 6.0.0
 */
@Internal
public final class ClassHierarchyIndex {

    private static final ReferenceBundle WEAK = ReferenceBundle.getWeakBundle();

    /**
     * Ancestor class (weak, identity) → weakly-reachable descendant ClassInfos.
     * A type is never registered under itself.
     */
    private static final ManagedIdentityConcurrentMap<Class<?>, ManagedConcurrentLinkedQueue<ClassInfo>> BY_ANCESTOR =
            new ManagedIdentityConcurrentMap<>(256);

    private ClassHierarchyIndex() {
    }

    /**
     * Indexes {@code info} under every strict supertype of its class.
     * Called exactly once from the {@link ClassInfo} constructor while the
     * {@link Class} is strongly reachable.
     *
     * @param info the newly constructed ClassInfo
     */
    @Internal
    public static void register(final ClassInfo info) {
        Class<?> type = info.getTheClass();
        if (type == null) {
            return;
        }
        Set<Class<?>> ancestors = new LinkedHashSet<>();
        collectStrictSupertypes(type, ancestors);
        for (Class<?> ancestor : ancestors) {
            addUnder(ancestor, info);
        }
    }

    private static void addUnder(final Class<?> ancestor, final ClassInfo info) {
        ManagedConcurrentLinkedQueue<ClassInfo> queue =
                BY_ANCESTOR.applyIfAbsent(ancestor, k -> new ManagedConcurrentLinkedQueue<>(WEAK));
        queue.add(info);
    }

    /**
     * Appends every ClassInfo registered as a descendant of {@code ancestor}
     * (not including {@code ancestor} itself) to {@code out}. Empty when no
     * subtypes have been indexed (e.g. finals, primitives, or unused parents).
     *
     * @param ancestor the root of the fan-out
     * @param out      destination collection
     */
    @Internal
    public static void collectDescendants(final Class<?> ancestor, final Collection<ClassInfo> out) {
        ManagedConcurrentLinkedQueue<ClassInfo> queue = BY_ANCESTOR.get(ancestor);
        if (queue == null) {
            return;
        }
        for (ClassInfo info : queue) {
            // Iterator of ManagedConcurrentLinkedQueue skips cleared weak refs.
            out.add(info);
        }
    }

    /**
     * Collects every strict supertype {@code S} of {@code type} such that
     * {@code S.isAssignableFrom(type) && S != type}, computed structurally from
     * the type's hierarchy (no global ClassInfo scan).
     * <p>
     * Array types follow the JLS covariance lattice: for component {@code C},
     * every supertype {@code S} of {@code C} (including {@code C}) yields
     * {@code S[]} as a supertype of {@code C[]}, plus {@code Object},
     * {@code Cloneable}, and {@code Serializable}.
     *
     * @param type the type whose strict supertypes are collected
     * @param out  destination set
     */
    static void collectStrictSupertypes(final Class<?> type, final Set<Class<?>> out) {
        if (type == null || type == Object.class || type.isPrimitive()) {
            return;
        }
        if (type.isArray()) {
            out.add(Object.class);
            out.add(Cloneable.class);
            out.add(Serializable.class);
            Class<?> component = type.getComponentType();
            // {component} ∪ strict-supers(component) — each lifted to a 1-D array.
            Set<Class<?>> componentLattice = new LinkedHashSet<>();
            componentLattice.add(component);
            collectStrictSupertypes(component, componentLattice);
            for (Class<?> componentType : componentLattice) {
                Class<?> arrayOf = Array.newInstance(componentType, 0).getClass();
                if (arrayOf != type) {
                    out.add(arrayOf);
                }
            }
            return;
        }
        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            out.add(superclass);
            collectStrictSupertypes(superclass, out);
        } else if (type.isInterface()) {
            // Class.getSuperclass() is null for interfaces; assignability still
            // treats Object as a supertype (Object.isAssignableFrom(iface) == true).
            out.add(Object.class);
        }
        for (Class<?> iface : type.getInterfaces()) {
            out.add(iface);
            collectStrictSupertypes(iface, out);
        }
    }
}
