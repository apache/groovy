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
package org.codehaus.groovy.reflection;

import groovy.lang.MissingMethodException;
import org.apache.groovy.internal.runtime.invoke.DirectInvoker;
import org.apache.groovy.internal.runtime.invoke.DirectInvokerSubjects;
import org.apache.groovy.internal.runtime.invoke.InvokerFactory;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hook tests for {@link CachedMethod#invoke} installing a {@code DirectInvoker}.
 *
 * <p>Java (not Groovy) so {@code threshold=0} does not install trampolines on
 * Groovy test closures. Each test uses {@code new CachedMethod(method)} so the
 * interned MOP {@code CachedMethod} is not mutated.
 */
final class CachedMethodDirectInvokerTest {

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testThresholdZeroUsesGeneratedPath() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            assertEquals("pong", cm.invoke(new DirectInvokerSubjects(), null));
            assertEquals("pong", cm.invoke(new DirectInvokerSubjects(), MetaClassHelper.EMPTY_ARRAY));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testNullArgumentsAreEmptyArray() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            assertEquals("pong", cm.invoke(new DirectInvokerSubjects(), null));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testPrimitiveBoxingMatchesReflective() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod generated = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("add", int.class, int.class));
            Object viaGenerated = generated.invoke(new DirectInvokerSubjects(), new Object[]{2, 5});
            withDisable(() -> {
                CachedMethod reflective = new CachedMethod(
                        DirectInvokerSubjects.class.getMethod("add", int.class, int.class));
                Object viaReflect = reflective.invoke(new DirectInvokerSubjects(), new Object[]{2, 5});
                assertEquals(viaReflect, viaGenerated);
                assertEquals(Integer.valueOf(7), viaGenerated);
            });
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testRuntimeExceptionIsRethrown() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomRuntime"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> cm.invoke(subject, null));
            assertEquals("runtime-boom", ex.getMessage());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testMissingMethodExceptionIsWrapped() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomMme"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, null));
            assertInstanceOf(MissingMethodException.class, ex.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testErrorFromTargetIsWrapped() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomError"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, null));
            assertInstanceOf(AssertionError.class, ex.getCause());
            assertEquals("error-boom", ex.getCause().getMessage());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testCheckedExceptionIsWrapped() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomChecked"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, null));
            assertInstanceOf(IOException.class, ex.getCause());
            assertEquals("checked-boom", ex.getCause().getMessage());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testReflectivePathStillWrapsIllegalArgumentException() throws Exception {
        withDisable(() -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGeneratedPathRethrowsClassCastOnBadArgs() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            assertThrows(ClassCastException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGeneratedPathWrongArityTooFewMatchesReflective() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[0]));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGeneratedPathWrongArityTooManyMatchesReflective() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[]{"a", "b"}));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
            // Fallback must not sticky-fail the trampoline.
            assertEquals("x", cm.invoke(subject, new Object[]{"x"}));
            assertThrows(ClassCastException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testCallerSensitiveStaysReflective() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(Class.class.getMethod("forName", String.class));
            assertTrue(cm.isCallerSensitive());
            Class<?> loaded = (Class<?>) cm.invoke(null, new Object[]{String.class.getName()});
            assertEquals(String.class, loaded);
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testKillSwitchUsesReflectivePath() throws Exception {
        withDisable(() -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            assertEquals("pong", cm.invoke(new DirectInvokerSubjects(), null));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testDefaultThresholdDoesNotGenerateOnFirstCall() throws Exception {
        String previous = System.getProperty(InvokerFactory.PROPERTY_THRESHOLD);
        try {
            System.clearProperty(InvokerFactory.PROPERTY_THRESHOLD);
            // echo(String) with a non-String: reflective wraps IAE, generated
            // rethrows ClassCastException. The exception type is the probe that
            // the first call (hits=1, default threshold 1000) stayed reflective.
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        } finally {
            restore(InvokerFactory.PROPERTY_THRESHOLD, previous);
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testThresholdCrossingInstallsGeneratedPath() throws Exception {
        withThreshold(2L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            Object[] bad = new Object[]{1};
            // hits 1 and 2:  n > 2 is false, stay reflective (wrapped IAE).
            InvokerInvocationException first = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, bad));
            assertInstanceOf(IllegalArgumentException.class, first.getCause());
            InvokerInvocationException second = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, bad));
            assertInstanceOf(IllegalArgumentException.class, second.getCause());
            // hit 3: generate, then CHECKCAST fails with ClassCastException.
            assertThrows(ClassCastException.class, () -> cm.invoke(subject, bad));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testStaticMethodAndVoidThroughHook() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod stat = new CachedMethod(DirectInvokerSubjects.class.getMethod("staticPing"));
            assertEquals("static-pong", stat.invoke(null, null));
            CachedMethod v = new CachedMethod(DirectInvokerSubjects.class.getMethod("noop"));
            assertNull(v.invoke(new DirectInvokerSubjects(), null));
            CachedMethod sv = new CachedMethod(DirectInvokerSubjects.class.getMethod("staticNoop"));
            assertNull(sv.invoke(null, null));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testDefaultThresholdConstantIsOneThousand() throws Exception {
        assertEquals(1000L, InvokerFactory.DEFAULT_THRESHOLD);
        String previous = System.getProperty(InvokerFactory.PROPERTY_THRESHOLD);
        try {
            System.clearProperty(InvokerFactory.PROPERTY_THRESHOLD);
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            for (int i = 0; i < 999; i++) {
                assertEquals("ok", cm.invoke(subject, new Object[]{"ok"}));
            }
            // hit 1000: still reflective (hits > 1000 is required)
            InvokerInvocationException stillReflective = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
            assertInstanceOf(IllegalArgumentException.class, stillReflective.getCause());
            // hit 1001: generated trampoline CHECKCAST
            assertThrows(ClassCastException.class, () -> cm.invoke(subject, new Object[]{1}));
        } finally {
            restore(InvokerFactory.PROPERTY_THRESHOLD, previous);
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testCachedThresholdDoesNotReReadProperty() throws Exception {
        withThreshold(100L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            assertEquals("a", cm.invoke(subject, new Object[]{"a"}));
            System.setProperty(InvokerFactory.PROPERTY_THRESHOLD, "0");
            // hits=2, cached threshold is still 100, so stay reflective
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, new Object[]{1}));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testKillSwitchIsStickyAcrossSubsequentInvokes() throws Exception {
        withDisable(() -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            Object[] bad = new Object[]{1};
            InvokerInvocationException first = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, bad));
            assertInstanceOf(IllegalArgumentException.class, first.getCause());
            assertTrue((Boolean) field(cm, "invokerAttempted"));
            assertNull(field(cm, "invoker"));
            InvokerInvocationException second = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, bad));
            assertInstanceOf(IllegalArgumentException.class, second.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testAbstractMethodStickySkipsGeneration() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.AbstractHost.class.getMethod("abs"));
            DirectInvokerSubjects.AbstractHost impl = new DirectInvokerSubjects.AbstractHost() {
                @Override
                public String abs() {
                    return "impl";
                }
            };
            assertEquals("impl", cm.invoke(impl, null));
            assertTrue((Boolean) field(cm, "invokerAttempted"));
            assertNull(field(cm, "invoker"));
            assertEquals("impl", cm.invoke(impl, null));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testCallerSensitiveStickySkipsGeneration() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(Class.class.getMethod("forName", String.class));
            assertTrue(cm.isCallerSensitive());
            Class<?> loaded = (Class<?>) cm.invoke(null, new Object[]{String.class.getName()});
            assertEquals(String.class, loaded);
            assertTrue((Boolean) field(cm, "invokerAttempted"));
            assertNull(field(cm, "invoker"));
            Class<?> again = (Class<?>) cm.invoke(null, new Object[]{Integer.class.getName()});
            assertEquals(Integer.class, again);
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testNullArgumentsOnParameterizedMethodFallBackToReflective() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(
                    DirectInvokerSubjects.class.getMethod("echo", String.class));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            // First call installs the trampoline; null args are EMPTY_ARRAY (arity 0 != 1).
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, null));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
            assertEquals("ok", cm.invoke(subject, new Object[]{"ok"}));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGeneratedInvokerIsReusedAndHidden() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            assertEquals("pong", cm.invoke(subject, null));
            Object first = field(cm, "invoker");
            assertInstanceOf(DirectInvoker.class, first);
            assertTrue(first.getClass().isHidden());
            assertEquals("pong", cm.invoke(subject, MetaClassHelper.EMPTY_ARRAY));
            assertSame(first, field(cm, "invoker"));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testPrimitiveIdentityMatchesValueOfCacheOnGeneratedPath() throws Exception {
        withThreshold(0L, () -> {
            DirectInvokerSubjects s = new DirectInvokerSubjects();
            CachedMethod b = new CachedMethod(DirectInvokerSubjects.class.getMethod("boxedByte", byte.class));
            assertSame(Byte.valueOf((byte) 42), b.invoke(s, new Object[]{(byte) 42}));
            CachedMethod bool = new CachedMethod(DirectInvokerSubjects.class.getMethod("flag", boolean.class));
            assertSame(Boolean.TRUE, bool.invoke(s, new Object[]{Boolean.TRUE}));
            CachedMethod i = new CachedMethod(DirectInvokerSubjects.class.getMethod("add", int.class, int.class));
            assertSame(Integer.valueOf(7), i.invoke(s, new Object[]{3, 4}));
            CachedMethod l = new CachedMethod(DirectInvokerSubjects.class.getMethod("boxedLong", long.class));
            assertSame(Long.valueOf(99L), l.invoke(s, new Object[]{99L}));
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testReflectivePathExceptionPolicy() throws Exception {
        withDisable(() -> {
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            CachedMethod runtime = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomRuntime"));
            IllegalStateException re = assertThrows(IllegalStateException.class,
                    () -> runtime.invoke(subject, null));
            assertEquals("runtime-boom", re.getMessage());

            CachedMethod err = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomError"));
            InvokerInvocationException ee = assertThrows(InvokerInvocationException.class,
                    () -> err.invoke(subject, null));
            assertInstanceOf(AssertionError.class, ee.getCause());

            CachedMethod checked = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomChecked"));
            InvokerInvocationException ce = assertThrows(InvokerInvocationException.class,
                    () -> checked.invoke(subject, null));
            assertInstanceOf(IOException.class, ce.getCause());

            CachedMethod mme = new CachedMethod(DirectInvokerSubjects.class.getMethod("boomMme"));
            InvokerInvocationException me = assertThrows(InvokerInvocationException.class,
                    () -> mme.invoke(subject, null));
            assertInstanceOf(MissingMethodException.class, me.getCause());
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testReflectivePathWrapsIllegalAccessWhenNotMadeAccessible() throws Exception {
        withDisable(() -> {
            Method secret = DirectInvokerSubjects.class.getDeclaredMethod("secret");
            assertFalse(secret.canAccess(new DirectInvokerSubjects()));
            CachedMethod cm = new CachedMethod(secret);
            setField(cm, "makeAccessibleDone", true);
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            InvokerInvocationException ex = assertThrows(InvokerInvocationException.class,
                    () -> cm.invoke(subject, null));
            assertInstanceOf(IllegalAccessException.class, ex.getCause());
        });
    }

    @Test
    void testInjectedInvokerWrongArityDoesNotCallTrampoline() throws Exception {
        CachedMethod cm = new CachedMethod(
                DirectInvokerSubjects.class.getMethod("echo", String.class));
        AtomicBoolean called = new AtomicBoolean();
        DirectInvoker injected = (receiver, arguments) -> {
            called.set(true);
            return "should-not-run";
        };
        setField(cm, "invoker", injected);
        DirectInvokerSubjects subject = new DirectInvokerSubjects();
        InvokerInvocationException tooFew = assertThrows(InvokerInvocationException.class,
                () -> cm.invoke(subject, new Object[0]));
        assertInstanceOf(IllegalArgumentException.class, tooFew.getCause());
        assertFalse(called.get(), "arity mismatch must not invoke the trampoline");

        InvokerInvocationException tooMany = assertThrows(InvokerInvocationException.class,
                () -> cm.invoke(subject, new Object[]{"a", "b"}));
        assertInstanceOf(IllegalArgumentException.class, tooMany.getCause());
        assertFalse(called.get());
        assertEquals("should-not-run", cm.invoke(subject, new Object[]{"ok"}));
        assertTrue(called.get());
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGenerationLockSeesInvokerInstalledByRacingThread() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            DirectInvoker injected = (receiver, arguments) -> "from-lock";
            Object[] box = new Object[1];
            Throwable[] error = new Throwable[1];
            Thread worker = new Thread(() -> {
                try {
                    box[0] = cm.invoke(subject, null);
                } catch (Throwable t) {
                    error[0] = t;
                }
            });
            synchronized (cm) {
                worker.start();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (worker.getState() != Thread.State.BLOCKED) {
                    if (System.nanoTime() > deadline) {
                        throw new AssertionError("worker never blocked on generation lock, state=" + worker.getState());
                    }
                    Thread.yield();
                }
                setField(cm, "invoker", injected);
            }
            worker.join(TimeUnit.SECONDS.toMillis(5));
            assertNull(error[0]);
            assertEquals("from-lock", box[0]);
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testGenerationLockSeesStickyFailureFromRacingThread() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            Object[] box = new Object[1];
            Throwable[] error = new Throwable[1];
            Thread worker = new Thread(() -> {
                try {
                    box[0] = cm.invoke(subject, null);
                } catch (Throwable t) {
                    error[0] = t;
                }
            });
            synchronized (cm) {
                worker.start();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (worker.getState() != Thread.State.BLOCKED) {
                    if (System.nanoTime() > deadline) {
                        throw new AssertionError("worker never blocked on generation lock, state=" + worker.getState());
                    }
                    Thread.yield();
                }
                setField(cm, "invokerAttempted", true);
            }
            worker.join(TimeUnit.SECONDS.toMillis(5));
            assertNull(error[0]);
            assertEquals("pong", box[0]);
            assertNull(field(cm, "invoker"));
        });
    }

    @Test
    void testInjectedInvokerFastPathWithoutThreshold() throws Exception {
        CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
        DirectInvoker injected = (receiver, arguments) -> "injected-pong";
        setField(cm, "invoker", injected);
        assertEquals("injected-pong", cm.invoke(new DirectInvokerSubjects(), null));
        assertSame(injected, field(cm, "invoker"));
    }

    @Test
    void testInjectedInvokerExceptionPolicy() throws Exception {
        CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
        DirectInvokerSubjects subject = new DirectInvokerSubjects();

        setField(cm, "invoker", (DirectInvoker) (r, a) -> {
            throw new IllegalStateException("injected-runtime");
        });
        IllegalStateException runtime = assertThrows(IllegalStateException.class,
                () -> cm.invoke(subject, null));
        assertEquals("injected-runtime", runtime.getMessage());

        setField(cm, "invoker", (DirectInvoker) (r, a) -> {
            throw new MissingMethodException("missing", DirectInvokerSubjects.class, null);
        });
        InvokerInvocationException mme = assertThrows(InvokerInvocationException.class,
                () -> cm.invoke(subject, null));
        assertInstanceOf(MissingMethodException.class, mme.getCause());

        setField(cm, "invoker", (DirectInvoker) (r, a) -> {
            throw new IOException("injected-checked");
        });
        InvokerInvocationException checked = assertThrows(InvokerInvocationException.class,
                () -> cm.invoke(subject, null));
        assertInstanceOf(IOException.class, checked.getCause());

        setField(cm, "invoker", (DirectInvoker) (r, a) -> {
            throw new AssertionError("injected-error");
        });
        InvokerInvocationException err = assertThrows(InvokerInvocationException.class,
                () -> cm.invoke(subject, null));
        assertInstanceOf(AssertionError.class, err.getCause());
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testConcurrentGenerationReusesOneInvoker() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(DirectInvokerSubjects.class.getMethod("ping"));
            DirectInvokerSubjects subject = new DirectInvokerSubjects();
            int n = 32;
            ExecutorService pool = Executors.newFixedThreadPool(8);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Object>> futures = new ArrayList<>();
            try {
                for (int i = 0; i < n; i++) {
                    futures.add(pool.submit(() -> {
                        start.await();
                        return cm.invoke(subject, null);
                    }));
                }
                start.countDown();
                for (Future<Object> future : futures) {
                    assertEquals("pong", future.get(10, TimeUnit.SECONDS));
                }
            } finally {
                pool.shutdownNow();
            }
            Object invoker = field(cm, "invoker");
            assertInstanceOf(DirectInvoker.class, invoker);
        });
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testStickyFailFromTryCreateNullIsReused() throws Exception {
        withThreshold(0L, () -> {
            CachedMethod cm = new CachedMethod(Class.class.getMethod("forName", String.class));
            // caller-sensitive: shouldAttemptGeneration sticky-fails without tryCreate
            cm.invoke(null, new Object[]{String.class.getName()});
            assertTrue((Boolean) field(cm, "invokerAttempted"));
            long hitsAfterFirst = (Long) field(cm, "invokeHits");
            cm.invoke(null, new Object[]{String.class.getName()});
            assertEquals(hitsAfterFirst, field(cm, "invokeHits"),
                    "sticky skip must not keep incrementing the hit counter");
        });
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void withThreshold(long threshold, ThrowingRunnable body) throws Exception {
        String previous = System.getProperty(InvokerFactory.PROPERTY_THRESHOLD);
        String disable = System.getProperty(InvokerFactory.PROPERTY_DISABLE);
        try {
            System.setProperty(InvokerFactory.PROPERTY_THRESHOLD, Long.toString(threshold));
            System.clearProperty(InvokerFactory.PROPERTY_DISABLE);
            body.run();
        } finally {
            restore(InvokerFactory.PROPERTY_THRESHOLD, previous);
            restore(InvokerFactory.PROPERTY_DISABLE, disable);
        }
    }

    private static void withDisable(ThrowingRunnable body) throws Exception {
        String previous = System.getProperty(InvokerFactory.PROPERTY_DISABLE);
        try {
            System.setProperty(InvokerFactory.PROPERTY_DISABLE, "true");
            body.run();
        } finally {
            restore(InvokerFactory.PROPERTY_DISABLE, previous);
        }
    }

    private static void restore(String name, String previous) {
        if (previous == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, previous);
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = CachedMethod.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = CachedMethod.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
