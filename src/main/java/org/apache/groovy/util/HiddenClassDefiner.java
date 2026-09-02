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

import groovy.transform.Internal;
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
 * <h2>Lookup ownership</h2>
 * <p>{@link MethodHandles#lookup()} is caller-sensitive: it returns a
 * full-privilege lookup only for the class that literally contains the call.
 * Production call sites capture a lookup on the intended nest host:
 * <pre>{@code
 * private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
 * Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytecode, false);
 * }</pre>
 * {@link Lookup#lookupClass()} fixes the hidden class's defining loader,
 * package, protection domain, and nest host. Lookups from different runtime
 * classes are not interchangeable as nest hosts even though they share the same
 * module rights (see below).
 *
 * <p>The lookup captured inside this class is used only as the <em>caller</em>
 * argument to {@link MethodHandles#privateLookupIn(Class, Lookup)} for the
 * foreign-host overload — never as a production nest host.
 *
 * <h2>Module rights vs nest host</h2>
 * <p>Every production call site lives in the Groovy runtime, so {@code lookup()}
 * always grants the same module-level access. Capturing it in different runtime
 * classes does not open a third-party module that never opened itself to the
 * runtime. What differs is the nest host (package / loader / nest membership),
 * which still matters for unloadability and linkage.
 *
 * <h2>What nestmates cover (modules A / B / C)</h2>
 * <p>With modules A (Java library), B (Groovy program), C (Groovy runtime):
 * <ol>
 *   <li><strong>Caller-owned lookup</strong>
 *       ({@link #tryDefineNestmate(Lookup, byte[], boolean)}): nestmate of a
 *       runtime class. Works when every type named by the bytecode is resolvable
 *       from the runtime loader. No private access into foreign modules.</li>
 *   <li><strong>Foreign host</strong>
 *       ({@link #tryDefineNestmate(Class, byte[], boolean)}): best-effort
 *       {@code privateLookupIn}. Succeeds when the host package is open to the
 *       runtime (typical for unnamed-module application classes); not for
 *       strongly encapsulated packages such as {@code java.lang}
 *       ({@link String} is the counter-example). Callers must handle
 *       {@code null} and fall back to {@link ClassLoader#defineClass}.</li>
 * </ol>
 * Nestmates cover C and often open/unnamed B. They do not tunnel private access
 * into A. Visible {@code defineClass} is the intentional safety net.
 *
 * <h2>Soft-fail contract</h2>
 * <p>{@code try*} methods return {@code null} on expected failures
 * ({@link IllegalAccessException}, {@link SecurityException}, {@link LinkageError},
 * invalid class-file / ASM exceptions, GraalVM {@code UnsupportedFeatureError}
 * matched by class name). Other {@link Error}s are rethrown.
 *
 * <h2>Enablement</h2>
 * <p>{@link #isEnabled()} is evaluated per call so
 * {@code -Dgroovy.hidden.classes.disable=true} works under native-image
 * build-time init, and is always {@code false} when
 * {@code org.graalvm.nativeimage.imagecode=runtime}.
 *
 * @since 6.0.0
 * @see Lookup#defineHiddenClass(byte[], boolean, Lookup.ClassOption...)
 */
public final class HiddenClassDefiner {

    /** System property that disables hidden-class definitions at run time. */
    public static final String PROPERTY_DISABLE = "groovy.hidden.classes.disable";

    /**
     * GraalVM property set in native images. Value {@code "runtime"} means the
     * current process is executing an already-built native image. Not a public
     * configuration surface — use {@link #isEnabled()}.
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
     * Lookup for <em>this</em> class only — caller argument to
     * {@link MethodHandles#privateLookupIn(Class, Lookup)}, never a production
     * nest host.
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Nestmate with the default (weak) lifecycle. Default hidden classes are
     * weakly held; {@link Lookup.ClassOption#STRONG} is not requested.
     */
    private static final Lookup.ClassOption[] NESTMATE_WEAK =
            new Lookup.ClassOption[]{Lookup.ClassOption.NESTMATE};

    private HiddenClassDefiner() {
    }

    /**
     * Whether hidden-class definition may be attempted in this process.
     *
     * @return {@code false} when the kill switch is set or when running inside
     *         a GraalVM native image at run time
     */
    public static boolean isEnabled() {
        if (isNativeImageRuntime()) {
            return false;
        }
        return !SystemUtil.getBooleanSafe(PROPERTY_DISABLE, false);
    }

    /**
     * Defines {@code bytes} as a hidden nestmate of {@code lookup.lookupClass()}
     * with a weak lifecycle. The class-file package is rewritten to match the
     * lookup class before definition.
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
     * Best-effort definition of a hidden nestmate of a <em>foreign</em> host.
     * Uses {@code privateLookupIn} from this class; gated by
     * {@link #canAttemptPrivateLookup(Class)}. Not a substitute for a host-owned
     * lookup.
     *
     * @param host       nest host and class-loader / package donor
     * @param bytes      class-file bytes
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden class, or {@code null} if private lookup or definition fails
     */
    public static Class<?> tryDefineNestmate(
            final Class<?> host,
            final byte[] bytes,
            final boolean initialize) {
        if (!isEnabled() || !canAttemptPrivateLookup(host) || bytes == null) {
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

    /**
     * Like {@link #tryDefineNestmate(Lookup, byte[], boolean)}, but returns the
     * hidden {@link Lookup} so the caller can {@code findConstructor} / read
     * classData without a public-constructor round-trip.
     *
     * <p>{@link ExceptionInInitializerError} and {@link NoClassDefFoundError}
     * are sticky-failed as {@code null} (in addition to the usual soft-fail
     * set). Needed from {@code InvokerFactory} in another package; not user API.
     *
     * @param lookup     full-privilege lookup for the nest host
     * @param bytes      class-file bytes
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden lookup, or {@code null} if definition is not possible
     * @since 6.0.0
     */
    @Internal
    public static Lookup tryDefineNestmateLookup(
            final Lookup lookup,
            final byte[] bytes,
            final boolean initialize) {
        return defineHidden(lookup, bytes, initialize, /*classData*/ null, false);
    }

    /**
     * Foreign host: {@code privateLookupIn(host, LOOKUP)} then the
     * Lookup-returning define. Nest host is {@code host}, not this utility.
     *
     * @param host       nest host and class-loader / package donor
     * @param bytes      class-file bytes
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden lookup, or {@code null} if private lookup or definition fails
     * @since 6.0.0
     */
    @Internal
    public static Lookup tryDefineNestmateLookup(
            final Class<?> host,
            final byte[] bytes,
            final boolean initialize) {
        if (!isEnabled() || !canAttemptPrivateLookup(host) || bytes == null) {
            return null;
        }
        try {
            final Lookup hostLookup = MethodHandles.privateLookupIn(host, LOOKUP);
            return tryDefineNestmateLookup(hostLookup, bytes, initialize);
        } catch (IllegalAccessException | SecurityException e) {
            return null;
        } catch (Error e) {
            if (isUnsupportedFeatureError(e)) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Defines {@code bytes} as a hidden nestmate of {@code lookup.lookupClass()}
     * with {@code classData} attached ({@link Lookup#defineHiddenClassWithClassData}).
     * Returns the hidden {@link Lookup}. Initialization errors sticky-fail as
     * {@code null}. Needed from {@code InvokerFactory} in another package; not
     * user API.
     *
     * @param lookup     full-privilege lookup for the nest host
     * @param bytes      class-file bytes
     * @param classData  value retrieved by {@link MethodHandles#classData} /
     *                   {@code ConstantDynamic} in the hidden class
     * @param initialize {@code true} to run {@code <clinit>} immediately
     * @return the hidden lookup, or {@code null} if definition is not possible
     * @since 6.0.0
     */
    @Internal
    public static Lookup tryDefineNestmateWithClassData(
            final Lookup lookup,
            final byte[] bytes,
            final Object classData,
            final boolean initialize) {
        if (classData == null) {
            return null;
        }
        return defineHidden(lookup, bytes, initialize, classData, true);
    }

    /**
     * Internal policy: whether {@code privateLookupIn} from this utility into
     * {@code host} is worth attempting.
     *
     * <p>Returns {@code false} for unusable host shapes and for named-module
     * packages that are not open to the Groovy runtime (e.g. {@code String} in
     * {@code java.base}). A {@code true} result does not guarantee success.
     *
     * <p>Not a stable user API — for runtime define policy and tests only.
     *
     * @param host candidate foreign nest host
     * @return {@code true} when a private-lookup attempt is not known to be futile
     * @since 6.0.0
     */
    @Internal
    public static boolean canAttemptPrivateLookup(final Class<?> host) {
        if (!isUsableHost(host)) {
            return false;
        }
        final Module hostModule = host.getModule();
        final Module callerModule = LOOKUP.lookupClass().getModule();
        return hostModule.isOpen(host.getPackageName(), callerModule);
    }

    // -------------------------------------------------------------------------
    // Internals (package-visible where tests need them)
    // -------------------------------------------------------------------------

    /** {@code true} when executing inside a GraalVM native image (not during image build). */
    static boolean isNativeImageRuntime() {
        return NATIVE_IMAGE_CODE_RUNTIME.equals(System.getProperty(PROPERTY_NATIVE_IMAGE_CODE));
    }

    /** {@code true} when {@code e} is GraalVM's unsupported-feature error (name match). */
    static boolean isUnsupportedFeatureError(final Error e) {
        return e != null && UNSUPPORTED_FEATURE_ERROR.equals(e.getClass().getName());
    }

    /**
     * Soft-fails GraalVM unsupported-feature errors as {@code null}; rethrows
     * every other {@link Error}.
     */
    static Class<?> softFailOrRethrow(final Error e) {
        if (isUnsupportedFeatureError(e)) {
            return null;
        }
        throw e;
    }

    /**
     * Shared hidden-class define. {@code withClassData == false} uses
     * {@link Lookup#defineHiddenClass}; otherwise
     * {@link Lookup#defineHiddenClassWithClassData}. {@link LinkageError}
     * (including {@link ExceptionInInitializerError} and
     * {@link NoClassDefFoundError} on current JDKs) sticky-fails as {@code null}.
     */
    private static Lookup defineHidden(
            final Lookup lookup,
            final byte[] bytes,
            final boolean initialize,
            final Object classData,
            final boolean withClassData) {
        if (!isEnabled() || lookup == null || bytes == null) {
            return null;
        }
        try {
            final byte[] aligned = alignPackage(bytes, lookup.lookupClass());
            if (withClassData) {
                return lookup.defineHiddenClassWithClassData(
                        aligned, classData, initialize, NESTMATE_WEAK);
            }
            return lookup.defineHiddenClass(aligned, initialize, NESTMATE_WEAK);
        } catch (IllegalAccessException | SecurityException | LinkageError
                 | IllegalArgumentException | IndexOutOfBoundsException e) {
            // LinkageError includes NoClassDefFoundError and, on current JDKs,
            // ExceptionInInitializerError — both sticky-fail as null.
            // IllegalArgumentException / IndexOutOfBoundsException: bad class-file.
            return null;
        } catch (Error e) {
            // Graal's unsupported-feature signal is an Error, not Exception.
            if (e instanceof ExceptionInInitializerError || isUnsupportedFeatureError(e)) {
                return null;
            }
            throw e;
        }
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
