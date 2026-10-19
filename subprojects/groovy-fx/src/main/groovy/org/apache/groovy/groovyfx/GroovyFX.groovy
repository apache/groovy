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
package org.apache.groovy.groovyfx

import javafx.application.Platform
import javafx.stage.Stage
import org.apache.groovy.lang.annotation.Incubating

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Entry point for JavaFX applications written with {@link SceneGraphBuilder}.
 * <p>
 * Unlike {@code javafx.application.Application}, nothing here needs to be
 * subclassed and the launching class does not have to be a JavaFX class,
 * which keeps scripts free of the "JavaFX runtime components are missing"
 * launcher check. The toolkit is started with {@code Platform.startup} the
 * first time it is needed.
 * <pre>
 * GroovyFX.start {
 *     stage(title: 'Hello', show: true) {
 *         scene(width: 300, height: 100) {
 *             vbox(spacing: 10, padding: 10) {
 *                 button('Click me', onAction: { println 'clicked' })
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
@Incubating
class GroovyFX {

    /**
     * Builder variable holding the stage created for {@link #start}; the first
     * {@code stage} node without an explicit value reuses it.
     */
    static final String PRIMARY_STAGE = 'primaryStage'

    /**
     * Seconds to wait for the toolkit to start, or for the application thread to run a closure,
     * before giving up; the {@code groovyfx.toolkit.timeout} system property overrides the default of 30.
     */
    static final long TOOLKIT_TIMEOUT_SECONDS = Long.getLong('groovyfx.toolkit.timeout', 30L)

    private static volatile boolean toolkitStarted

    private GroovyFX() {
    }

    /**
     * Starts the JavaFX toolkit if needed, creates a primary stage and runs the
     * application closure against a new {@link SceneGraphBuilder} on the JavaFX
     * application thread, returning once the closure has completed.
     * <p>
     * The closure receives the primary stage as its argument and has the builder
     * as its delegate, so builder nodes may be called directly. The stage is
     * also available as the builder variable {@code primaryStage}, and is used
     * by the first {@code stage} node that is given no explicit stage.
     *
     * @param app the application closure
     * @return the primary stage
     */
    static Stage start(@DelegatesTo(SceneGraphBuilder) Closure app) {
        ensureToolkit()
        SceneGraphBuilder builder = new SceneGraphBuilder()
        Stage primaryStage = null
        builder.fx {
            primaryStage = new Stage()
            builder.setVariable(PRIMARY_STAGE, primaryStage)
            app.delegate = builder
            app.resolveStrategy = Closure.DELEGATE_FIRST
            app.call(primaryStage)
        }
        primaryStage
    }

    /**
     * Starts the JavaFX toolkit unless it is already running, and waits until it is ready.
     * <p>
     * Safe to call repeatedly and from any thread.
     *
     * @throws IllegalStateException if JavaFX is not available or the toolkit does not start in time
     */
    static void ensureToolkit() {
        if (toolkitStarted) return
        synchronized (GroovyFX) {
            if (toolkitStarted) return
            FXRuntime.requireJavaFX()
            CountDownLatch started = new CountDownLatch(1)
            try {
                Platform.startup { started.countDown() }
            } catch (IllegalStateException alreadyRunning) {
                // started elsewhere (or a previous attempt failed): prove it is alive before trusting it
                try {
                    Platform.runLater { started.countDown() }
                } catch (IllegalStateException exited) {
                    throw new IllegalStateException('The JavaFX toolkit has been shut down and cannot be restarted in this JVM: ' +
                        'Platform.exit() was called, or the last window closed while implicit exit was enabled. ' +
                        'Call Platform.setImplicitExit(false) when windows come and go, such as in tests.', exited)
                }
            }
            if (!started.await(TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The JavaFX toolkit did not start within ${TOOLKIT_TIMEOUT_SECONDS} seconds. " +
                    'Without a display, run with -Dglass.platform=Monocle -Dmonocle.platform=Headless ' +
                    '(openjfx-monocle on the classpath) or, on JavaFX 26 and later, -Dglass.platform=headless.')
            }
            toolkitStarted = true
        }
    }
}
