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
package groovy.junit6.plugin;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit 5 {@link InvocationInterceptor} backing the {@link ForkedJvm}
 * annotation. When applied, the test method is skipped in the current JVM and
 * re-run in a freshly forked JVM via {@link ForkedJvmTestRunner}, with any
 * declared system properties and JVM args.
 * <p>
 * Recursion is avoided by setting the system property
 * {@link ForkedJvmTestRunner#FORKED_FLAG} on the child; when the extension
 * sees that flag set in the current JVM it just proceeds with the normal
 * invocation (i.e. the child JVM actually runs the test body).
 *
 * @since 6.0.0
 */
public class ForkedJvmExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        if (Boolean.parseBoolean(System.getProperty(ForkedJvmTestRunner.FORKED_FLAG))) {
            // Already in the child JVM — run the body for real.
            invocation.proceed();
            return;
        }

        ForkedJvm config = findAnnotation(extensionContext);
        if (config == null) {
            invocation.proceed();
            return;
        }

        // Skip in this (parent) JVM; we'll run the same method in a child.
        invocation.skip();

        Class<?> testClass = invocationContext.getTargetClass();
        Method testMethod = invocationContext.getExecutable();
        boolean parentInverting = Boolean.TRUE.equals(extensionContext
                .getStore(ExpectedToFailExtension.NAMESPACE)
                .get(ExpectedToFailExtension.STORE_KEY_PARENT_INVERTING));
        runInForkedJvm(testClass, testMethod, config, parentInverting);
    }

    private static ForkedJvm findAnnotation(ExtensionContext context) {
        return context.getElement()
                .map(el -> el.getAnnotation(ForkedJvm.class))
                .orElseGet(() -> context.getTestClass()
                        .map(c -> c.getAnnotation(ForkedJvm.class))
                        .orElse(null));
    }

    private static void runInForkedJvm(Class<?> testClass, Method testMethod,
                                       ForkedJvm config, boolean parentInverting) throws Throwable {
        Path resultFile = Files.createTempFile("groovy-forked-jvm-result", ".bin");
        try {
            List<String> command = buildCommand(testClass, testMethod, config, resultFile, parentInverting);
            ProcessBuilder pb = new ProcessBuilder(command).inheritIO();
            int exit = pb.start().waitFor();
            propagateOutcome(exit, resultFile, testClass, testMethod);
        } finally {
            try { Files.deleteIfExists(resultFile); } catch (IOException ignore) {}
        }
    }

    private static List<String> buildCommand(Class<?> testClass, Method testMethod,
                                             ForkedJvm config, Path resultFile,
                                             boolean parentInverting) {
        List<String> cmd = new ArrayList<>();
        String javaHome = System.getProperty("java.home");
        String javaExe = System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java";
        cmd.add(javaHome + File.separator + "bin" + File.separator + javaExe);
        cmd.add("-D" + ForkedJvmTestRunner.FORKED_FLAG + "=true");
        cmd.add("-D" + ForkedJvmTestRunner.RESULT_FILE_PROP + "=" + resultFile);
        if (parentInverting) {
            cmd.add("-D" + ExpectedToFailExtension.DEFERRED_TO_PARENT_PROP + "=true");
        }
        for (String sp : config.systemProperties()) {
            int eq = sp.indexOf('=');
            if (eq < 0) {
                throw new IllegalArgumentException(
                        "@ForkedJvm system property must be 'key=value', got: " + sp);
            }
            cmd.add("-D" + sp);
        }
        for (String arg : config.jvmArgs()) {
            cmd.add(arg);
        }
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(ForkedJvmTestRunner.class.getName());
        cmd.add(testClass.getName());
        cmd.add(testMethod.getName());
        return cmd;
    }

    private static void propagateOutcome(int exit, Path resultFile,
                                         Class<?> testClass, Method testMethod) throws Throwable {
        String location = testClass.getName() + "#" + testMethod.getName();
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(resultFile);
        } catch (IOException ioe) {
            throw new AssertionError("Forked JVM for " + location
                    + " exited with code " + exit
                    + " and the result file at " + resultFile + " was unreadable", ioe);
        }
        if (exit == 0 && bytes.length == 0) return;

        if (bytes.length == 0) {
            throw new AssertionError("Forked JVM for " + location
                    + " exited with code " + exit + " (no failure detail captured)");
        }
        if (bytes[0] == ForkedJvmTestRunner.ABORTED_MARKER) {
            String reason = new String(bytes, 1, bytes.length - 1, StandardCharsets.UTF_8);
            throw new TestAbortedException("Forked JVM for " + location + " aborted: " + reason);
        }
        if (bytes[0] == ForkedJvmTestRunner.TEXT_FALLBACK_MARKER) {
            throw new AssertionError("Forked JVM for " + location + " failed:\n"
                    + new String(bytes, 1, bytes.length - 1, StandardCharsets.UTF_8));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Throwable t = (Throwable) ois.readObject();
            // Rethrow exactly so JUnit reports the original failure type/message.
            throw t;
        } catch (ClassNotFoundException | IOException deserFailed) {
            throw new AssertionError("Forked JVM for " + location
                    + " failed and result couldn't be deserialised", deserFailed);
        }
    }
}
