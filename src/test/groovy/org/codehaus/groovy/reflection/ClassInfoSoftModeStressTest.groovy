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
package org.codehaus.groovy.reflection

import org.junit.jupiter.api.Test

import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12281: races soft-mode ClassInfo management against <em>real</em> GC
 * clearing in a child JVM (small heap, {@code SoftRefLRUPolicyMSPerMB=0}) —
 * see {@link ClassInfoSoftModeStressProbe} for the invariants. Runtime is
 * ~30s by design; the deterministic-clearing companion is
 * {@link ClassInfoSoftModeTest}.
 */
final class ClassInfoSoftModeStressTest {

    @Test
    void softModeSurvivesRealGcClearingUnderConcurrency() {
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Dgroovy.use.classvalue=soft',
                '-Xmx128m',
                '-XX:SoftRefLRUPolicyMSPerMB=0',
                '-cp', cp,
                'org.codehaus.groovy.reflection.ClassInfoSoftModeStressProbe')
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = new StringBuilder()
        proc.inputStream.eachLine { out.append(it).append('\n') }
        assertTrue(proc.waitFor(120, TimeUnit.SECONDS), "stress probe timed out: $out")
        assertEquals(0, proc.exitValue(), "stress probe failed: $out")
        assertTrue(out.contains('OK'), out.toString())
    }
}
