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
package org.codehaus.groovy.runtime;

import groovy.lang.GroovyObject;
import org.apache.groovy.util.HiddenClassDefiner;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Defines proxy classes for {@link ProxyGeneratorAdapter}, preferring a JEP 371
 * hidden nestmate when safe and falling back to a visible {@link ClassLoader}
 * definition otherwise.
 *
 * <p>Extracted from {@link ProxyGeneratorAdapter} so that class keeps focused on
 * bytecode generation while nest-host policy and define/fallback live here.
 *
 * <h2>When a proxy stays visible</h2>
 * <ul>
 *   <li>hidden classes are disabled;</li>
 *   <li>any named dependency (super / delegate / implemented type) is itself
 *       hidden — hidden types cannot appear as nominal field/super types;</li>
 *   <li>interface-style aggregates ({@code Object} super, no typed delegate,
 *       at least one user interface) — these are routinely re-wrapped (e.g. by
 *       MockFor) and must remain loadable by binary name.</li>
 * </ul>
 *
 * <h2>Nest host selection (at most one foreign try, then own Lookup)</h2>
 * <ol>
 *   <li>{@code delegateClass} if usable and its loader can see every dependency;</li>
 *   <li>else concrete {@code superClass} (not {@code Object}) under the same rule;</li>
 *   <li>else the caller-owned {@link Lookup} from {@link ProxyGeneratorAdapter}
 *       when that loader can see every dependency.</li>
 * </ol>
 * Interfaces are not used as nest hosts: they add trials without helping the
 * common cases and complicate class-loader reasoning.
 *
 * @since 6.0.0
 */
final class ProxyClassDefiner {

    private ProxyClassDefiner() {
    }

    /**
     * Result of a define attempt.
     */
    static final class Result {
        final Class<?> type;
        final boolean hidden;
        final Constructor<?> constructor;

        Result(final Class<?> type, final boolean hidden, final Constructor<?> constructor) {
            this.type = type;
            this.hidden = hidden;
            this.constructor = constructor;
        }
    }

    /**
     * Defines {@code bytecode} as a hidden nestmate when policy allows, else via
     * {@code visibleDefine}.
     *
     * @param bytecode      generated class-file bytes
     * @param binaryName    binary name for the visible fallback path
     * @param superClass    proxy superclass (already normalised; may be Object)
     * @param delegateClass typed {@code $delegate} field type, or {@code null}
     * @param implClasses   super + interfaces + related types named by the bytecode
     * @param ownLookup     {@link MethodHandles#lookup()} from {@link ProxyGeneratorAdapter}
     * @param visibleDefine {@code (binaryName, bytes) -> Class} fallback (e.g. InnerLoader)
     * @param ctorArgs      constructor parameter types to resolve after define
     * @return defined class, whether it is hidden, and the matching public ctor (or null)
     */
    static Result define(
            final byte[] bytecode,
            final String binaryName,
            final Class<?> superClass,
            final Class<?> delegateClass,
            final Collection<? extends Class<?>> implClasses,
            final Lookup ownLookup,
            final BiFunction<String, byte[], Class<?>> visibleDefine,
            final Class<?>[] ctorArgs) {

        Class<?> type = null;

        if (mayDefineHidden(superClass, delegateClass, implClasses)) {
            final Class<?> foreign = preferredForeignHost(superClass, delegateClass, implClasses);
            if (foreign != null) {
                type = accept(HiddenClassDefiner.tryDefineNestmate(foreign, bytecode, true), ctorArgs);
            }
            if (type == null && canResolveAll(ownLookup.lookupClass(), superClass, delegateClass, implClasses)) {
                type = accept(HiddenClassDefiner.tryDefineNestmate(ownLookup, bytecode, true), ctorArgs);
            }
            if (type != null) {
                return new Result(type, true, resolvePublicConstructor(type, ctorArgs));
            }
        }

        // Visible fallback — never mark as hidden.
        type = visibleDefine.apply(binaryName, bytecode);
        return new Result(type, false, resolvePublicConstructor(type, ctorArgs));
    }

    // -------------------------------------------------------------------------
    // Policy
    // -------------------------------------------------------------------------

    /**
     * {@code true} when attempting a hidden definition is worthwhile.
     */
    static boolean mayDefineHidden(
            final Class<?> superClass,
            final Class<?> delegateClass,
            final Collection<? extends Class<?>> implClasses) {
        if (!HiddenClassDefiner.isEnabled()) {
            return false;
        }
        if (isUnusableNamedType(superClass) || isUnusableNamedType(delegateClass)) {
            return false;
        }
        if (implClasses != null) {
            for (Class<?> impl : implClasses) {
                if (isUnusableNamedType(impl)) {
                    return false;
                }
            }
        }
        // Interface aggregates must stay nameable for re-wrapping (MockFor, etc.).
        return delegateClass != null || superClass != Object.class || !hasUserInterface(implClasses);
    }

    /**
     * Single preferred foreign nest host, or {@code null} to skip to own Lookup.
     */
    static Class<?> preferredForeignHost(
            final Class<?> superClass,
            final Class<?> delegateClass,
            final Collection<? extends Class<?>> implClasses) {
        if (isCandidateHost(delegateClass)
                && canResolveAll(delegateClass, superClass, delegateClass, implClasses)) {
            return delegateClass;
        }
        if (superClass != Object.class
                && isCandidateHost(superClass)
                && canResolveAll(superClass, superClass, delegateClass, implClasses)) {
            return superClass;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Loader visibility
    // -------------------------------------------------------------------------

    /**
     * Every type the proxy bytecode names must be resolvable from {@code host}'s
     * defining loader (child loaders see parents; parents do not see children).
     */
    static boolean canResolveAll(
            final Class<?> host,
            final Class<?> superClass,
            final Class<?> delegateClass,
            final Collection<? extends Class<?>> implClasses) {
        if (!loaderCanResolve(host, superClass) || !loaderCanResolve(host, delegateClass)) {
            return false;
        }
        if (implClasses != null) {
            for (Class<?> impl : implClasses) {
                if (!loaderCanResolve(host, impl)) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean loaderCanResolve(final Class<?> host, final Class<?> type) {
        if (type == null || type.isPrimitive()) {
            return true;
        }
        final ClassLoader defining = type.getClassLoader(); // null ⇒ bootstrap
        if (defining == null) {
            return true;
        }
        for (ClassLoader cl = host.getClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl == defining) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Class<?> accept(final Class<?> candidate, final Class<?>[] ctorArgs) {
        if (candidate == null) {
            return null;
        }
        // Ensure deferred linkage succeeds before we commit to the hidden path.
        if (resolvePublicConstructor(candidate, ctorArgs) == null) {
            return null;
        }
        return candidate;
    }

    /**
     * Public constructor only — preserves the historical
     * {@link Class#getConstructor(Class[])} contract used by proxy instantiation.
     */
    static Constructor<?> resolvePublicConstructor(final Class<?> type, final Class<?>[] args) {
        try {
            return type.getConstructor(args);
        } catch (NoSuchMethodException | LinkageError e) {
            return null;
        }
    }

    private static boolean isCandidateHost(final Class<?> type) {
        return type != null
                && type != Object.class
                && type != ProxyGeneratorAdapter.class
                && !isUnusableNamedType(type)
                && !type.isSealed();
    }

    private static boolean isUnusableNamedType(final Class<?> type) {
        return type != null && (type.isPrimitive() || type.isArray() || type.isHidden());
    }

    private static boolean hasUserInterface(final Collection<? extends Class<?>> implClasses) {
        if (implClasses == null) {
            return false;
        }
        for (Class<?> impl : implClasses) {
            if (impl != null && impl != Object.class && impl != GroovyObject.class) {
                return true;
            }
        }
        return false;
    }
}
