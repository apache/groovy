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
package org.apache.groovy.lsp.internal

import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

final class LspFixtureTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void publishedParamsCanBeReadWhileTheCompileThreadPublishes() {
        def testThread = Thread.currentThread()
        def armed = new AtomicBoolean()
        def published = new CountDownLatch(1)
        fixture = new LspFixture() {
            @Override
            void publishDiagnostics(PublishDiagnosticsParams params) {
                def afterReadStarted = Thread.currentThread() != testThread && armed.get()
                super.publishDiagnostics(params)
                if (afterReadStarted) {
                    published.countDown()
                }
            }
        }
        fixture.open('Hello.groovy', 'class Hello {\n}\n')
        // a test reading the recorded notifications, as `publishedParams*.uri` does,
        // while the debounced compile thread publishes the next round
        def reading = fixture.publishedParams.iterator()
        armed.set(true)
        fixture.server.context.scheduleRecompile(0)
        assert published.await(10, TimeUnit.SECONDS)
        assert reading.toList()*.uri.any { it.endsWith('Hello.groovy') }
    }
}
