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

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Child-JVM entry point for {@link ForkedJvm}-annotated tests.
 * <p>
 * Invoked by {@link ForkedJvmExtension} with the qualified test class name
 * and method name as command-line arguments. Runs exactly that one test method
 * via the JUnit Platform {@link Launcher}, then reports outcome to the parent
 * via the file referenced by the system property
 * {@code groovy.junit6.forked.result}: empty file on success, serialised
 * {@link Throwable} (with text fallback) on failure.
 *
 * @since 6.0.0
 */
public final class ForkedJvmTestRunner {

    /** Set on the child JVM so {@link ForkedJvmExtension} doesn't re-fork. */
    public static final String FORKED_FLAG = "groovy.junit6.forked";

    /** Path of the result file the child writes into. */
    public static final String RESULT_FILE_PROP = "groovy.junit6.forked.result";

    /**
     * Marker byte at the start of the result file when the failure had to be
     * written as text instead of a serialised {@link Throwable}.
     * Distinguishable because {@link ObjectOutputStream}'s STREAM_MAGIC is
     * {@code 0xACED}, never starts with {@code 0x00}.
     */
    public static final byte TEXT_FALLBACK_MARKER = 0;

    /** Marker byte at the start of the result file for an aborted test. */
    public static final byte ABORTED_MARKER = 1;

    /** Exit code used when the child reports an aborted test. */
    public static final int ABORTED_EXIT_CODE = 4;

    private ForkedJvmTestRunner() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: ForkedJvmTestRunner <testClass> <methodName>");
            System.exit(2);
        }
        String className = args[0];
        String methodName = args[1];
        String resultFilePath = System.getProperty(RESULT_FILE_PROP);
        if (resultFilePath == null) {
            System.err.println("ForkedJvmTestRunner: required system property "
                    + RESULT_FILE_PROP + " is not set");
            System.exit(2);
        }
        Path resultPath = Paths.get(resultFilePath);

        Class<?> testClass = Class.forName(className);
        // Resolve a single overload by parameter-less name; ForkedJvm tests
        // shouldn't have parameterised method signatures we can't match.
        LauncherDiscoveryRequest req = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(testClass, methodName))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(req);

        TestExecutionSummary summary = listener.getSummary();
        if (summary.getTestsFoundCount() == 0) {
            writeTextFallback(resultPath,
                    "ForkedJvmTestRunner: no test discovered for "
                            + className + "#" + methodName);
            System.exit(3);
        }
        if (summary.getTotalFailureCount() == 0 && summary.getTestsAbortedCount() > 0) {
            // Test was aborted (e.g. via Assumptions); propagate as abort, not as success.
            String reason = "test aborted";
            writeAborted(resultPath, reason);
            System.exit(ABORTED_EXIT_CODE);
        }
        if (summary.getTotalFailureCount() == 0) {
            Files.write(resultPath, new byte[0]);
            System.exit(0);
        }
        Throwable cause = summary.getFailures().get(0).getException();
        writeFailure(resultPath, cause);
        System.exit(1);
    }

    private static void writeAborted(Path path, String reason) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(ABORTED_MARKER);
            out.write(reason.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static void writeFailure(Path path, Throwable t) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(t);
            oos.flush();
            Files.write(path, bytes.toByteArray());
        } catch (Exception serializationFailed) {
            // Some test exceptions aren't Serializable; fall back to text.
            writeTextFallback(path, stackTraceText(t));
        }
    }

    private static void writeTextFallback(Path path, String text) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(TEXT_FALLBACK_MARKER);
            out.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static String stackTraceText(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
