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
package org.apache.groovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

import java.util.concurrent.atomic.AtomicLong

/**
 * Aggregates test results across all subprojects and prints
 * a summary at the end of the build.
 *
 * <p>Suite-level results are fed in via {@link #recordSuite} from
 * {@code afterSuite} callbacks. The service also implements
 * {@link OperationCompletionListener} so it stays alive until
 * all tasks complete; {@link #close()} prints the aggregate.</p>
 */
@CompileStatic
abstract class TestResultAggregatorService implements BuildService<Params>, OperationCompletionListener, AutoCloseable {

    interface Params extends BuildServiceParameters {}

    private final AtomicLong totalPassed = new AtomicLong()
    private final AtomicLong totalFailed = new AtomicLong()
    private final AtomicLong totalSkipped = new AtomicLong()

    /** Called from afterSuite when a root suite completes. */
    void recordSuite(long passed, long failed, long skipped) {
        totalPassed.addAndGet(passed)
        totalFailed.addAndGet(failed)
        totalSkipped.addAndGet(skipped)
    }

    @Override
    void onFinish(FinishEvent event) {
        // Required by OperationCompletionListener; keeps the service
        // alive until the last task finishes so close() runs at build end.
    }

    @Override
    void close() {
        long passed = totalPassed.get()
        long failed = totalFailed.get()
        long skipped = totalSkipped.get()
        long total = passed + failed + skipped
        if (total == 0) return

        String green = '\u001B[32m'
        String red = '\u001B[31m'
        String yellow = '\u001B[33m'
        String reset = '\u001B[0m'

        String failText = failed ? "${red}${failed} failed${reset}, " : ''
        System.out.println("Aggregate test results: ${failText}${green}${passed} passed${reset}, " +
                "${yellow}${skipped} skipped${reset} " +
                "(${total} tests)")
        System.out.flush()
    }

    static Provider<TestResultAggregatorService> register(BuildEventsListenerRegistry registry,
                                                          org.gradle.api.invocation.Gradle gradle) {
        Provider<TestResultAggregatorService> provider = gradle.sharedServices.registerIfAbsent(
                'testResultAggregator', TestResultAggregatorService) {}
        registry.onTaskCompletion(provider)
        return provider
    }
}
