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
package org.apache.groovy.util;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;

/**
 * Central facility for defining <em>hidden classes</em>
 * (<a href="https://openjdk.org/jeps/371">JEP 371</a>).
 *
 * <h2>Lookup ownership (read this first)</h2>
 * <p>{@link MethodHandles#lookup()} is <em>caller-sensitive</em>: it returns a
 * full-privilege lookup only for the class that literally contains the call.
 * A lookup captured in this utility therefore only has full privilege for
 * {@code HiddenClassDefiner} itself — never for arbitrary foreign classes
 * (for example {@code java.lang.String} in {@code java.base}).
 *
 * <p>Consequently the <strong>preferred</strong> entry point is
 * {@link #tryDefineNestmate(Lookup, byte[], boolean)}, where the caller
 * supplies a {@link Lookup} obtained inside the intended nest-host class:
 * <pre>{@code
 * // Inside the class that should host the hidden nestmate:
 * private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
 *
 * Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytecode, false);
 * if (hidden == null) {
 *     // fall back to ClassLoader.defineClass(...)
 * }
 * }</pre>
 *
 * <p>The overload {@link #tryDefineNestmate(Class, byte[], boolean)} is a
 * <em>best-effort</em> helper for foreign hosts (user classes Groovy does not
 * control). It uses {@link MethodHandles#privateLookupIn(Class, Lookup)} from
 * this class and therefore succeeds only when the host's package is accessible
 * to Groovy's module — typically true for unnamed-module application classes,
 * and typically false for sealed / unopened packages such as those in
 * {@code java.base}. Callers must always handle a {@code null} result.
 *
 * <h2>What this utility centralises</h2>
 * <ul>
 *   <li>{@code NESTMATE} + weak-lifecycle policy for dynamic Groovy classes;</li>
 *   <li>rewriting {@code this_class} into the lookup class's package (required
 *       by {@link Lookup#defineHiddenClass});</li>
 *   <li>a soft API that returns {@code null} on the <em>expected</em> failure
 *       modes ({@link IllegalAccessException}, {@link IllegalArgumentException},
 *       {@link SecurityException}, {@link LinkageError}) so call sites fall back
 *       to {@link ClassLoader#defineClass} with one null check. Unexpected
 *       failures (e.g. programming errors in callers) are not swallowed as a
 *       blanket {@link RuntimeException}.</li>
 * </ul>
 *
 * <h2>Kill switch</h2>
 * <p>{@code -Dgroovy.hidden.classes.disable=true} forces every {@code try*}
 * method to return {@code null}.
 *
 * @since 6.0.0
 * @see Lookup#defineHiddenClass(byte[], boolean, Lookup.ClassOption...)
 */
public final class HiddenClassDefiner {

    /** System property that disables hidden-class definitions. */
    public static final String PROPERTY_DISABLE = "groovy.hidden.classes.disable";

    /**
     * {@code true} when hidden-class definitions are globally disabled.
     * Evaluated once at class-init so hot paths pay no property-lookup cost.
     */
    public static final boolean HIDDEN_CLASSES_DISABLED =
            SystemUtil.getBooleanSafe(PROPERTY_DISABLE, false);

    /**
     * Lookup for <em>this</em> class only — used exclusively as the caller
     * argument to {@link MethodHandles#privateLookupIn(Class, Lookup)} in the
     * foreign-host overload. It is never used as a nest host for user code.
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    /** Nestmate + weak (eager unloading) — the only option set production uses. */
    private static final Lookup.ClassOption[] NESTMATE_WEAK =
            new Lookup.ClassOption[]{Lookup.ClassOption.NESTMATE};

    private HiddenClassDefiner() {
    }

    /**
     * @return {@code true} when hidden-class definition is enabled
     *         (the default unless {@value #PROPERTY_DISABLE} is set)
     */
    public static boolean isEnabled() {
        return !HIDDEN_CLASSES_DISABLED;
    }

    /**
     * Defines {@code bytes} as a hidden nestmate of {@code lookup.lookupClass()}
     * with a weak lifecycle.
     *
     * <p>The lookup must have been obtained via {@link MethodHandles#lookup()}
     * inside the intended nest-host class (or otherwise carry full privilege
     * for that class). The class-file package is rewritten to match the lookup
     * class before definition.
     *
     * @param lookup     full-privilege lookup for the nest host
     * @param bytes      class-file bytes
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden class, or {@code null} if definition is not possible
     */
    public static Class<?> tryDefineNestmate(
            final Lookup lookup,
            final byte[] bytes,
            final boolean initialize) {
        if (HIDDEN_CLASSES_DISABLED || lookup == null || bytes == null) {
            return null;
        }
        try {
            final byte[] aligned = alignPackage(bytes, lookup.lookupClass());
            return lookup.defineHiddenClass(aligned, initialize, NESTMATE_WEAK).lookupClass();
        } catch (IllegalAccessException | SecurityException | LinkageError e) {
            return null;
        } catch (IllegalArgumentException e) {
            // Invalid / unaligned class-file bytes (including ASM ClassReader).
            return null;
        } catch (IndexOutOfBoundsException e) {
            // Corrupt bytes can surface as bounds errors from ClassReader.
            return null;
        }
    }

    /**
     * Best-effort definition of a hidden nestmate of a <em>foreign</em> host
     * class (one whose source Groovy does not control).
     *
     * <p>Obtains a host lookup via
     * {@code MethodHandles.privateLookupIn(host, }lookup captured in this class)
     * and then delegates to {@link #tryDefineNestmate(Lookup, byte[], boolean)}.
     * This succeeds only when the host's package is accessible to Groovy's
     * module; it is <strong>not</strong> a substitute for a lookup created
     * inside the host.
     *
     * @param host       nest host and class-loader / package donor; must be a
     *                   normal (non-hidden) reference type
     * @param bytes      class-file bytes
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden class, or {@code null} if private lookup or definition fails
     */
    public static Class<?> tryDefineNestmate(
            final Class<?> host,
            final byte[] bytes,
            final boolean initialize) {
        if (HIDDEN_CLASSES_DISABLED || !isUsableHost(host) || bytes == null) {
            return null;
        }
        try {
            final Lookup hostLookup = MethodHandles.privateLookupIn(host, LOOKUP);
            return tryDefineNestmate(hostLookup, bytes, initialize);
        } catch (IllegalAccessException | SecurityException e) {
            return null;
        }
    }

    /**
     * Rewrites {@code this_class} (and internal references to it) into
     * {@code host}'s run-time package. No-op when already aligned.
     *
     * <p>ASM may throw {@link IllegalArgumentException} for corrupt bytes; callers
     * of {@link #tryDefineNestmate(Lookup, byte[], boolean)} treat that as soft-fail.
     */
    private static byte[] alignPackage(final byte[] bytes, final Class<?> host) {
        final String hostPkg = host.getPackageName();
        final ClassReader reader = new ClassReader(bytes);
        final String oldInternal = reader.getClassName();
        final int slash = oldInternal.lastIndexOf('/');
        final String simple = slash < 0 ? oldInternal : oldInternal.substring(slash + 1);
        final String newInternal = hostPkg.isEmpty()
                ? simple
                : hostPkg.replace('.', '/') + '/' + simple;
        if (oldInternal.equals(newInternal)) {
            return bytes;
        }
        final ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassRemapper(writer,
            new SimpleRemapper(CompilerConfiguration.ASM_API_VERSION, Collections.singletonMap(oldInternal, newInternal))), 0);
        return writer.toByteArray();
    }

    private static boolean isUsableHost(final Class<?> host) {
        return host != null
                && !host.isPrimitive()
                && !host.isArray()
                && !host.isHidden();
    }
}
