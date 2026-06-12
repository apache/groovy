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
package org.apache.groovy.groovysh.jline

import org.junit.jupiter.api.Test

/**
 * Direct tests for {@link GroovyEngine}. The engine is the foundation of the
 * groovysh stack — it holds the binding, runs scripts, and tracks user-defined
 * imports / variables / methods / types. Exercising it directly (no JLine
 * registry, console, or terminal) gives the most portable test layer.
 */
class GroovyEngineTest {

    private final GroovyEngine engine = new GroovyEngine()

    @Test
    void executeReturnsLastValue() {
        assert engine.execute('1 + 1') == 2
        assert engine.execute("'hi' + ' there'") == 'hi there'
    }

    @Test
    void variablesPersistAcrossExecutes() {
        engine.execute('x = 5')
        assert engine.hasVariable('x')
        assert engine.execute('x * 2') == 10
    }

    @Test
    void putAndHasVariable() {
        engine.put('answer', 42)
        assert engine.hasVariable('answer')
        assert engine.execute('answer') == 42
    }

    @Test
    void methodDefinitionsTracked() {
        engine.execute('def twice(n) { n * 2 }')
        assert engine.methodNames.contains('twice')
        assert engine.execute('twice(21)') == 42
    }

    @Test
    void typesAccumulate() {
        engine.execute('class Foo {}')
        engine.execute('interface Bar {}')
        engine.execute('enum Baz { A, B }')
        assert engine.types.keySet().containsAll(['Foo', 'Bar', 'Baz'])
    }

    @Test
    void importsTracked() {
        engine.execute('import java.awt.Point')
        assert engine.imports.values().any { it.contains('java.awt.Point') }
    }

    @Test
    void resetClearsTrackedDefinitionsButLeavesBindingVarsForFreshExecutes() {
        // /reset wipes types/methods/imports/snippet-tracked variables;
        // it does NOT delete shared/binding variables (those are managed
        // by the underlying ScriptEngine and survive). This is contract
        // users rely on.
        engine.execute('class C {}')
        engine.execute('def m(x) { x * 3 }')
        engine.put('survivor', 'still here')

        engine.reset()

        assert engine.types.isEmpty()
        assert engine.methodNames.isEmpty()
        assert engine.imports.isEmpty()
        assert engine.hasVariable('survivor')
        assert engine.execute('survivor') == 'still here'
    }

    @Test
    void redefiningATypeReplacesTheTrackedSnippet() {
        // The user redefines a class (common in interactive use). The
        // engine should keep only one snippet under that name and run
        // the latest body — not stack two definitions and produce
        // ambiguous behaviour.
        engine.execute('class T { String greet() { "first" } }')
        engine.execute('class T { String greet() { "second" } }')
        assert engine.types.containsKey('T')
        assert engine.execute('new T().greet()') == 'second'
    }

    @Test
    void removeMethodDropsItFromTracking() {
        engine.execute('def disposable() { 1 }')
        assert engine.methodNames.contains('disposable')
        engine.removeMethod('disposable')
        assert !engine.methodNames.contains('disposable')
    }

    @Test
    void removeTypeDropsItFromTracking() {
        engine.execute('class Disposable {}')
        assert engine.types.containsKey('Disposable')
        engine.removeType('Disposable')
        assert !engine.types.containsKey('Disposable')
    }

    @Test
    void closureBindingVariableSurvivesAcrossExecutes() {
        // A closure stored in the binding can be invoked by name on a
        // later execute — useful for "save a callback, use it later"
        // patterns that show up in REPL workflows.
        engine.execute('greet = { name -> "hi, $name" }')
        assert engine.execute("greet('paul')") == 'hi, paul'
    }
}
