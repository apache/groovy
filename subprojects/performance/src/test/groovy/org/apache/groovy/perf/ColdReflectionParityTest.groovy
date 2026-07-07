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
package org.apache.groovy.perf

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Standing parity guard for the reflective cold tier (GROOVY-12137,
 * {@code groovy.indy.cold.reflection}, on by default). The tier's fast path
 * dispatches via {@code java.lang.reflect.Method.invoke} while the normal
 * path uses MethodHandles, so its unique risk is behavioural divergence
 * between those two invocation mechanisms (e.g. primitive-return box identity,
 * caller-sensitivity, exception unwrapping, argument coercion).
 * <p>
 * The companion corpus {@code cold-reflection-parity-corpus.groovy} exercises
 * a broad battery of dynamic-dispatch shapes and prints a deterministic
 * transcript. This test runs it in two fresh JVMs — the tier explicitly
 * disabled and explicitly enabled (the flag is read once at
 * {@code IndyInterface} class-init, so it cannot be toggled in-process, and
 * both states are pinned so the test is independent of the default) — and
 * asserts the transcripts are byte-identical. Any difference is a genuine
 * reflection-vs-MethodHandle divergence.
 */
class ColdReflectionParityTest {

    private static final String CORPUS_RESOURCE = 'cold-reflection-parity-corpus.groovy'

    @Test
    void coldReflectionMatchesBaselineDispatch() {
        String off = runCorpus(false)
        String on = runCorpus(true)
        if (off != on) {
            // surface the first divergence for a readable failure
            def a = off.readLines(), b = on.readLines()
            def diff = (0..<Math.max(a.size(), b.size())).findResults { i ->
                def x = i < a.size() ? a[i] : '<missing>'
                def y = i < b.size() ? b[i] : '<missing>'
                x != y ? "  line ${i + 1}:\n    off: $x\n    on : $y" : null
            }.take(10).join('\n')
            assert false, "cold.reflection transcript diverged from baseline:\n$diff"
        }
        assertTrue(off.readLines().size() >= 60, "corpus produced too few checks (${off.readLines().size()})")
    }

    @Test
    void coldReflectionTierGatedByFlag() {
        // The tier logs each reflective dispatch. Assert both directions:
        // enabled must fire broadly (so the parity test cannot pass vacuously),
        // and disabled must not fire at all (so the "off" case genuinely
        // exercises the non-reflective path, not merely a coincident result).
        int firedOn = countColdDispatches(runCorpus(true, true))
        int firedOff = countColdDispatches(runCorpus(false, true))
        assertTrue(firedOn >= 50, "reflective cold tier fired only $firedOn times when enabled; expected broad exercise")
        assertEquals(0, firedOff, "reflective cold tier fired $firedOff times when disabled; expected none")
    }

    private static int countColdDispatches(String log) {
        log.readLines().count { it.contains('using reflective cold tier') }
    }

    private static String runCorpus(boolean coldReflection, boolean logging = false) {
        def corpus = File.createTempFile('cold-reflection-parity', '.groovy')
        corpus.deleteOnExit()
        corpus.bytes = ColdReflectionParityTest.getResourceAsStream(CORPUS_RESOURCE).bytes

        def java = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def cp = System.getProperty('java.class.path')
        def cmd = [java, '-cp', cp]
        // pin the state explicitly (the flag is opt-out / on by default), so
        // the off case actively disables and the test does not rely on the default
        cmd << "-Dgroovy.indy.cold.reflection=${coldReflection}".toString()
        if (logging) cmd << '-Dgroovy.indy.logging=true'
        cmd += ['groovy.ui.GroovyMain', corpus.absolutePath]

        def pb = new ProcessBuilder(cmd)
        // when logging, the tier writes to the JUL logger (stderr); fold it in
        if (logging) pb.redirectErrorStream(true)
        def process = pb.start()
        def stdout = process.inputStream.text
        def stderr = logging ? '' : process.errorStream.text
        int rc = process.waitFor()
        assertEquals(0, rc, "corpus JVM (coldReflection=$coldReflection) failed rc=$rc:\n$stderr")
        return stdout
    }
}
