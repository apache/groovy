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
package groovy.jmx.builder

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler

import java.lang.management.ManagementFactory
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Test-only JUnit 5 extension that absorbs the JDK cgroup-v2 first-use NPE
 * (a null {@code anyController} in {@code CgroupV2Subsystem.getInstance})
 * sporadically seen on some containerised CI hosts when JMX initialises the
 * platform OS MXBean.
 *
 * <ul>
 *   <li>{@link #beforeAll} primes the affected JDK code paths once per JVM,
 *       so a one-shot race is absorbed before any test runs.</li>
 *   <li>{@link #handleTestExecutionException} converts a deterministic
 *       occurrence into a skipped assumption (via {@link Assumptions#abort})
 *       instead of a test failure.</li>
 * </ul>
 *
 * Applied via {@code @ExtendWith(CgroupV2NpeMitigationExtension)} on JMX
 * test classes that touch the platform MBean server.
 */
class CgroupV2NpeMitigationExtension implements BeforeAllCallback, TestExecutionExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(CgroupV2NpeMitigationExtension.name)
    private static volatile boolean primed = false

    @Override
    void beforeAll(ExtensionContext context) {
        if (primed) return
        primed = true
        try {
            ManagementFactory.getOperatingSystemMXBean().toString()
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, 'Primed OS MXBean init absorbed exception (likely JDK cgroup-v2)', t)
        }
        try {
            new JmxBuilder().timer(name: 'prime:type=timer-prime', period: 60000)
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, 'Primed JmxBuilder.timer absorbed exception (likely JDK cgroup-v2)', t)
        }
    }

    @Override
    void handleTestExecutionException(ExtensionContext context, Throwable t) throws Throwable {
        for (Throwable c = t; c != null; c = c.cause) {
            String msg = c.message
            if (c instanceof NullPointerException && msg != null &&
                (msg.contains('CgroupInfo.getMountPoint') ||
                        (msg.contains('anyController') && msg.contains('null')))) {
                Assumptions.abort("Skipped: known JDK cgroup-v2 NPE in this CI environment: ${msg}")
            }
        }
        throw t
    }
}
