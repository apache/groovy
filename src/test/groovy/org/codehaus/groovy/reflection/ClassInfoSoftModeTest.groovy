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

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12281: soft-value global ClassValue mode
 * ({@code -Dgroovy.use.classvalue=soft}). The mode is chosen once at startup,
 * so the semantics are asserted in a child JVM running
 * {@link ClassInfoSoftModeProbe}.
 */
final class ClassInfoSoftModeTest {

    @Test
    void softModeSemantics_inChildProcess() {
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Xmx256m',   // bounded heap: the reverse scenario fills it to apply real memory pressure
                '-Dgroovy.use.classvalue=soft',
                '-cp', cp,
                'org.codehaus.groovy.reflection.ClassInfoSoftModeProbe')
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = proc.inputStream.text
        assertEquals(0, proc.waitFor(), "soft-mode probe failed: $out")
        assertTrue(out.contains('OK'), out)
    }
}
