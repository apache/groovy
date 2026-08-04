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
 * Production call sites therefore capture a lookup on the intended nest host
 * itself, for example:
 * <pre>{@code
 * // ReflectorLoader / ProxyGeneratorAdapter (or any nest host):
 * private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
 *
 * Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytecode, false);
 * }</pre>
 * The {@link Lookup#lookupClass() lookup class} determines the hidden class's
 * defining loader, run-time package, protection domain, and nest host. A lookup
 * from {@code ReflectorLoader} is therefore <em>not</em> interchangeable with
 * one from {@code ProxyGeneratorAdapter} or from this utility: each hosts
 * nestmates in a different package / loader / nest. What is shared is only the
 * define policy (package alignment, {@code NESTMATE}+weak options, soft-fail).
 *
 * <p>A lookup captured inside {@code HiddenClassDefiner} only has full privilege
 * for {@code HiddenClassDefiner} itself. It is used solely as the <em>caller</em>
 * argument to {@link MethodHandles#privateLookupIn(Class, Lookup)} in the
 * foreign-host overload — never as the nest host of production generators.
 *
 * <p>The overload {@link #tryDefineNestmate(Class, byte[], boolean)} is a
 * <em>best-effort</em> helper for foreign hosts (user classes Groovy does not
 * control). It succeeds only when the host's package is accessible to Groovy's
 * module (typical for unnamed-module application classes; not for sealed /
 * unopened packages such as those in {@code java.base}). Callers must always
 * handle a {@code null} result and fall back to {@link ClassLoader#defineClass}.
 *
 * <h2>Soft-fail contract</h2>
 * <p>{@code try*} methods return {@code null} on expected failure modes so call
 * sites can fall back with one null check:
 * <ul>
 *   <li>{@link IllegalAccessException}, {@link SecurityException}</li>
 *   <li>{@link LinkageError}</li>
 *   <li>{@link IllegalArgumentException}, {@link IndexOutOfBoundsException}
 *       (invalid class-file bytes / ASM)</li>
 *   <li>GraalVM native-image "feature unsupported" errors, detected by class
 *       name ({@code com.oracle.svm.core.jdk.UnsupportedFeatureError}) without
 *       a build dependency on GraalVM</li>
 * </ul>
 * Other {@link Error}s and unexpected failures are rethrown.
 *
 * <h2>Enablement (kill switch and native image)</h2>
 * <p>{@link #isEnabled()} is evaluated on each call (class definition is not a
 * hot path). That keeps {@code -Dgroovy.hidden.classes.disable=true} effective
 * under GraalVM native image, where a {@code static final} snapshot taken at
 * <em>build-time</em> class-init would freeze the wrong answer. When running
 * inside a native image ({@code org.graalvm.nativeimage.imagecode=runtime}),
 * enablement is {@code false}: runtime class definition is unsupported there,
 * so the soft path never attempts it.
 *
 * @since 6.0.0
 * @see Lookup#defineHiddenClass(byte[], boolean, Lookup.ClassOption...)
 */
public final class HiddenClassDefiner {

    /** System property that disables hidden-class definitions at run time. */
    public static final String PROPERTY_DISABLE = "groovy.hidden.classes.disable";

    /**
     * GraalVM property set in native images. Value {@code "runtime"} means the
     * current process is executing an already-built native image (as opposed to
     * {@code "buildtime"} during image generation). Package-private: not a
     * public configuration surface — use {@link #isEnabled()}.
     */
    static final String PROPERTY_NATIVE_IMAGE_CODE = "org.graalvm.nativeimage.imagecode";

    /** Value of {@link #PROPERTY_NATIVE_IMAGE_CODE} while executing a native image. */
    static final String NATIVE_IMAGE_CODE_RUNTIME = "runtime";

    /**
     * Fully-qualified name of GraalVM's "feature not supported at runtime" error.
     * Matched by name to avoid a compile-time dependency on {@code org.graalvm.*}.
     */
    static final String UNSUPPORTED_FEATURE_ERROR =
            "com.oracle.svm.core.jdk.UnsupportedFeatureError";

    /**
     * Lookup for <em>this</em> class only — used exclusively as the caller
     * argument to {@link MethodHandles#privateLookupIn(Class, Lookup)} in the
     * foreign-host overload. It is never used as a nest host for production
     * generators (those pass their own {@link MethodHandles#lookup()}).
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    /** Nestmate + weak (eager unloading) — the only option set production uses. */
    private static final Lookup.ClassOption[] NESTMATE_WEAK =
            new Lookup.ClassOption[]{Lookup.ClassOption.NESTMATE};

    private HiddenClassDefiner() {
    }

    /**
     * Whether hidden-class definition may be attempted in this process.
     *
     * <p>Returns {@code false} when:
     * <ul>
     *   <li>{@code -Dgroovy.hidden.classes.disable=true}, or</li>
     *   <li>the process is a GraalVM native image at run time
     *       ({@code org.graalvm.nativeimage.imagecode=runtime}).</li>
     * </ul>
     * Evaluated on each call so the kill switch remains effective when this
     * class is initialized at native-image <em>build</em> time.
     *
     * @return {@code true} when a {@code tryDefineNestmate} attempt is allowed
     */
    public static boolean isEnabled() {
        if (isNativeImageRuntime()) {
            return false;
        }
        return !SystemUtil.getBooleanSafe(PROPERTY_DISABLE, false);
    }

    /**
     * Defines {@code bytes} as a hidden nestmate of {@code lookup.lookupClass()}
     * with a weak lifecycle.
     *
     * <p>The lookup must have been obtained via {@link MethodHandles#lookup()}
     * <em>inside</em> the intended nest-host class (or otherwise carry full
     * privilege for that class). Which class called {@code lookup()} matters:
     * it fixes the hidden class's loader, package, and nest. The class-file
     * package is rewritten to match the lookup class before definition.
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
        if (!isEnabled() || lookup == null || bytes == null) {
            return null;
        }
        try {
            final byte[] aligned = alignPackage(bytes, lookup.lookupClass());
            return lookup.defineHiddenClass(aligned, initialize, NESTMATE_WEAK).lookupClass();
        } catch (IllegalAccessException | SecurityException | LinkageError e) {
            return null;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            // Invalid class-file bytes (including ASM ClassReader).
            return null;
        } catch (Error e) {
            return softFailOrRethrow(e);
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
        if (!isEnabled() || !isUsableHost(host) || bytes == null) {
            return null;
        }
        try {
            final Lookup hostLookup = MethodHandles.privateLookupIn(host, LOOKUP);
            return tryDefineNestmate(hostLookup, bytes, initialize);
        } catch (IllegalAccessException | SecurityException e) {
            return null;
        } catch (Error e) {
            return softFailOrRethrow(e);
        }
    }

    // -------------------------------------------------------------------------
    // Internals (package-visible where tests need them)
    // -------------------------------------------------------------------------

    /**
     * {@code true} when executing inside a GraalVM native image (not during
     * image build). Package-private for tests.
     */
    static boolean isNativeImageRuntime() {
        return NATIVE_IMAGE_CODE_RUNTIME.equals(System.getProperty(PROPERTY_NATIVE_IMAGE_CODE));
    }

    /**
     * {@code true} when {@code e} is GraalVM's unsupported-feature error.
     * Package-private for tests.
     */
    static boolean isUnsupportedFeatureError(final Error e) {
        return e != null && UNSUPPORTED_FEATURE_ERROR.equals(e.getClass().getName());
    }

    /**
     * Soft-fails GraalVM unsupported-feature errors as {@code null}; rethrows
     * every other {@link Error}. Package-private so unit tests cover the same
     * branch used by both {@code tryDefineNestmate} overloads.
     *
     * @param e error caught around define / privateLookupIn
     * @return {@code null} if soft-failed
     */
    static Class<?> softFailOrRethrow(final Error e) {
        if (isUnsupportedFeatureError(e)) {
            return null;
        }
        throw e;
    }

    /**
     * Rewrites {@code this_class} (and internal references to it) into
     * {@code host}'s run-time package. No-op when already aligned.
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
                new SimpleRemapper(
                        CompilerConfiguration.ASM_API_VERSION,
                        Collections.singletonMap(oldInternal, newInternal))), 0);
        return writer.toByteArray();
    }

    private static boolean isUsableHost(final Class<?> host) {
        return host != null
                && !host.isPrimitive()
                && !host.isArray()
                && !host.isHidden();
    }
}
