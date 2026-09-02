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
import org.apache.groovy.internal.runtime.invoke.DirectInvokerSubjects;
import org.apache.groovy.internal.runtime.invoke.InvokerFactory;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
}
