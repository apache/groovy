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
package groovy.junit5.plugin

import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.LoggingListener
import org.junit.platform.launcher.listeners.SummaryGeneratingListener

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass

class GroovyJUnitRunnerHelper {
    static Throwable execute(Class testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass)).build()
        Launcher launcher = LauncherFactory.create()

        TestExecutionListener listener = new SummaryGeneratingListener()
        launcher.registerTestExecutionListeners(listener)
        launcher.registerTestExecutionListeners(LoggingListener.forJavaUtilLogging())
        launcher.execute(request)
        println listener.summary.with{ "JUnit5 launcher: passed=$testsSucceededCount, aborted=$testsAbortedCount, failed=$testsFailedCount, skipped=$testsSkippedCount, time=${timeFinished-timeStarted}ms" }
        if (listener.summary.failures) {
            listener.summary.printFailuresTo(new PrintWriter(System.out, true))
            return listener.summary.failures[0].exception
        }
        return null
    }
}
