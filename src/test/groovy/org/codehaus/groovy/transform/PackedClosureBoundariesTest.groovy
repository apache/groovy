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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * The packability boundary as one readable matrix — executable documentation of GEP-27's
 * "packability decision procedure". Each case is a method body containing one closure literal;
 * the assertion is whether that literal packs (no generated closure class) or declines (keeps
 * its class), under the flag with {@code @CompileStatic} or dynamic compilation as noted.
 * Individual gates have focused behavioural tests elsewhere; this class exists so the whole
 * boundary can be read top-to-bottom in one place.
 */
final class PackedClosureBoundariesTest {

    private static final String PROP = 'groovy.target.closure.pack'

    /** [description, dynamic-packs?, cs-packs?, method body containing exactly one closure literal] */
    private static final List CASES = [
        // ---- the syntactic no-free-name subset: packs everywhere -------------------------------
        ['no free names',                true,  true,  'def m(List<Integer> xs) { xs.collect { x -> x + 1 } }'],
        ['implicit it',                  true,  true,  'def m(List<Integer> xs) { xs.collect { it * 2 } }'],
        ['explicit zero params',         true,  true,  'def m() { def c = { -> 42 }; c() }'],
        ['captured local (read-only)',   true,  true,  'def m(List<Integer> xs) { def k = 10; xs.collect { x -> x + k } }'],
        ['captured local (written)',     true,  true,  'def m(List<Integer> xs) { int t = 0; xs.each { t += it }; t }'],
        ['parameter receiver',           true,  true,  'def m(List<Integer> xs) { xs.collect { it.toString() } }'],
        // ---- free names: the types-or-trust boundary (dynamic declines, CS proof packs) --------
        ['implicit-this method call',    false, true,  'def helper(x) { x }\ndef m(List<Integer> xs) { xs.collect { helper(it) } }'],
        ['bare field-bound name',        false, true,  'int field = 1\ndef m(List<Integer> xs) { xs.collect { it + field } }'],
        ['explicit this-property',       false, true,  'int field = 1\ndef m(List<Integer> xs) { xs.collect { it + this.field } }'],
        // ---- real-Closure semantics: declines everywhere ----------------------------------------
        ['uses delegate',                false, false, 'def m() { def c = { delegate.toString() }; c }'],
        ['uses owner',                   false, false, 'def m() { def c = { owner.toString() }; c }'],
        ['default parameter values',     false, false, 'def m() { def c = { int x = 1 -> x }; c(2) }'],
        // ---- escapes: declines everywhere --------------------------------------------------------
        ['returned',                     false, false, 'Closure m() { return { it } }'],
        ['stored to property',           false, false, 'def m(Map attrs) { attrs.handler = { it } }'],
        ['in a collection literal',      false, false, 'def m() { [{ it }] }'],
        // ---- serialization-bound: declines everywhere -------------------------------------------
        ['cast to Serializable',         false, false, 'def m() { ({ it } as Serializable) != null }'],
        ['local into writeObject',       false, false, '''def m() {
                def c = { it }
                new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(c)
            }'''],
        // ---- contexts the adapter cannot inhabit: declines everywhere ---------------------------
        ['intersection cast',            false, false, 'def m() { (Runnable & java.io.Serializable) { -> } }'],
    ]

    @Test
    void boundaries() {
        CASES.each { desc, dynPacks, csPacks, body ->
            assertEquals(dynPacks, packs("class C {\n${body}\n}"),
                    "dynamic: '$desc' should ${dynPacks ? '' : 'NOT '}pack")
            assertEquals(csPacks, packs("@groovy.transform.CompileStatic\nclass C {\n${body}\n}"),
                    "@CompileStatic: '$desc' should ${csPacks ? '' : 'NOT '}pack")
        }
    }

    @Test
    void fieldInitializerDeclines() {
        // separate shape: the literal is a class-level field initialiser, not inside a method
        assert !packs('class C { def action = { 42 } }')
        assert !packs('@groovy.transform.CompileStatic\nclass C { def action = { 42 } }')
    }

    @Test
    void traitBodyDeclines() {
        assert !packs('trait T { def m(List<Integer> xs) { xs.collect { it * 2 } } }\nclass C implements T {}')
    }

    @Test
    void constructorSpecialCallDeclines() {
        assert !packs('class C { C(Closure c) {}; C() { this({ 42 }) } }')
    }

    @Test
    void reportPropertySurfacesDeclineReasonsOnTheFlagPath() {
        // groovy.target.closure.pack.report=true: every decline on the un-annotated flag path is
        // reported as a warning with its reason -- the operational boundary explainer
        System.setProperty('groovy.target.closure.pack.report', 'true')
        try {
            def cu = compile('class C { Closure m() { return { it } } }')  // escapes: returned
            def warnings = cu.errorCollector.warnings
            assert warnings.any { it.message.contains('closure was not packed') && it.message.contains('escapes') },
                    "expected an escape decline warning, got: ${warnings*.message}"
        } finally {
            System.clearProperty('groovy.target.closure.pack.report')
        }
    }

    private static boolean packs(String src) {
        compile(src).classes.every { !it.name.contains('_closure') }
    }

    private static CompilationUnit compile(String src) {
        String previous = System.getProperty(PROP)
        System.setProperty(PROP, 'true')
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('C.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu
        } finally {
            if (previous != null) System.setProperty(PROP, previous) else System.clearProperty(PROP)
        }
    }
}
