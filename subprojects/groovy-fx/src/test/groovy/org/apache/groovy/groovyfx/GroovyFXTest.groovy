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
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE

import java.util.concurrent.CountDownLatch

/**
 * Launcher tests need the toolkit; the build runs them under Monocle's headless
 * platform, whose 21.x builds require Java 21.
 */
@EnabledForJreRange(min = JRE.JAVA_21, disabledReason = 'openjfx-monocle 21.x needs Java 21')
final class GroovyFXTest {

    @BeforeAll
    static void keepToolkitAlive() {
        GroovyFX.ensureToolkit()
        Platform.implicitExit = false
    }

    @Test
    void startBuildsOnTheApplicationThreadWithThePrimaryStage() {
        def onFxThread = false
        def received = null
        Stage stage = GroovyFX.start { Stage primary ->
            onFxThread = Platform.fxApplicationThread
            received = primary
            stage(title: 'App') {
                scene(width: 120, height: 80) {
                    vbox { label('Hello', id: 'greeting') }
                }
            }
        }
        assert onFxThread
        assert stage.is(received)
        assert stage.title == 'App'
        assert stage.scene.root instanceof VBox
        assert stage.scene.root.children[0] instanceof Label
    }

    @Test
    void onlyTheFirstStageNodeReusesThePrimaryStage() {
        def stages = []
        Stage primary = GroovyFX.start {
            stages << stage(title: 'first')
            stages << stage(title: 'second')
        }
        assert stages[0].is(primary)
        assert !stages[1].is(primary)
        assert stages[1].title == 'second'
    }

    @Test
    void closureWithoutParameterStillSeesTheBuilder() {
        Stage primary = GroovyFX.start {
            stage { scene { label('x') } }
        }
        assert primary.scene != null
    }

    @Test
    void ensureToolkitIsIdempotent() {
        GroovyFX.ensureToolkit()
        GroovyFX.ensureToolkit()
        def ran = false
        def done = new CountDownLatch(1)
        Platform.runLater { ran = true; done.countDown() }
        done.await()
        assert ran
    }
}
