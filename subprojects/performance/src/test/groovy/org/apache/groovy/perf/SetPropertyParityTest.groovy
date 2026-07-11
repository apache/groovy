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
 * Standing parity guard for invokedynamic property writes (GROOVY-12138,
 * {@code groovy.indy.setproperty}, on by default). When enabled, a dynamic
 * property write compiles to an {@code invokedynamic setProperty} call site;
 * when disabled it compiles to a static {@code ScriptBytecodeAdapter.setProperty}
 * call. The fast path only handles provably sender/metaclass-independent
 * shapes and falls back to the exact classic adapter for everything else, so
 * behaviour must be identical either way.
 * <p>
 * The flag is a compile-time emission flag read once ({@code static final}) at
 * {@code IndyCallSiteWriter} class-init, so it cannot be toggled in-process.
 * This test therefore compiles+runs the companion corpus in two fresh JVMs —
 * setproperty explicitly enabled and disabled — and asserts the transcripts
 * are byte-identical. A second test confirms the flag genuinely gates emission
 * (indy sites when on, adapter calls when off) so the parity check cannot pass
 * vacuously.
 */
class SetPropertyParityTest {

    private static final String CORPUS_RESOURCE = 'set-property-parity-corpus.groovy'

    @Test
    void setPropertyMatchesAdapterDispatch() {
        String on = runCorpus(true)
        String off = runCorpus(false)
        if (on != off) {
            def a = off.readLines(), b = on.readLines()
            def diff = (0..<Math.max(a.size(), b.size())).findResults { i ->
                def x = i < a.size() ? a[i] : '<missing>'
                def y = i < b.size() ? b[i] : '<missing>'
                x != y ? "  line ${i + 1}:\n    off: $x\n    on : $y" : null
            }.take(10).join('\n')
            assert false, "indy setproperty transcript diverged from the adapter path:\n$diff"
        }
        assertTrue(on.readLines().size() >= 15, "corpus produced too few checks (${on.readLines().size()})")
    }

    @Test
    void setPropertyGatedByFlag() {
        // compile a probe with the flag on and off and count invokedynamic
        // setProperty sites, so the parity test cannot pass vacuously (e.g. if
        // the flag stopped reaching the compiler).
        int on = countIndySetProperty(true)
        int off = countIndySetProperty(false)
        assertTrue(on >= 1, "expected invokedynamic setProperty site(s) when enabled, got $on")
        assertEquals(0, off, "expected no invokedynamic setProperty sites when disabled, got $off")
    }

    // --- helpers ---

    private static String runCorpus(boolean setProperty) {
        def corpus = File.createTempFile('set-property-parity', '.groovy')
        corpus.deleteOnExit()
        corpus.bytes = SetPropertyParityTest.getResourceAsStream(CORPUS_RESOURCE).bytes
        def out = new StringBuilder()
        int rc = fork(setProperty, ['groovy.ui.GroovyMain', corpus.absolutePath], out)
        assertEquals(0, rc, "corpus JVM (setProperty=$setProperty) failed rc=$rc:\n$out")
        return out.toString()
    }

    private static int countIndySetProperty(boolean setProperty) {
        def dir = File.createTempDir()
        dir.deleteOnExit()
        def probe = new File(dir, 'SetProbe.groovy')
        probe.text = 'class B { String name; int count }\ndef b = new B()\nb.name = "x"\nb.count = 1\n'
        def compileOut = new StringBuilder()
        int rc = fork(setProperty, ['org.codehaus.groovy.tools.FileSystemCompiler', '-d', dir.absolutePath, probe.absolutePath], compileOut)
        assertEquals(0, rc, "probe compile (setProperty=$setProperty) failed:\n$compileOut")
        // disassemble and count invokedynamic setProperty sites in the script class
        def javap = new File(System.getProperty('java.home'), 'bin/javap').absolutePath
        def out = new StringBuilder(), err = new StringBuilder()
        def p = [javap, '-c', '-p', '-classpath', dir.absolutePath, 'SetProbe'].execute()
        p.waitForProcessOutput(out, err)
        return out.readLines().count { it.contains('invokedynamic') && it.contains('setProperty') }
    }

    private static int fork(boolean setProperty, List<String> mainAndArgs, StringBuilder mergedOutput) {
        def java = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def cp = System.getProperty('java.class.path')
        def cmd = [java, '-cp', cp, "-Dgroovy.indy.setproperty=${setProperty}".toString()] + mainAndArgs
        def pb = new ProcessBuilder(cmd)
        pb.redirectErrorStream(true)
        def process = pb.start()
        mergedOutput.append(process.inputStream.text)
        return process.waitFor()
    }
}
