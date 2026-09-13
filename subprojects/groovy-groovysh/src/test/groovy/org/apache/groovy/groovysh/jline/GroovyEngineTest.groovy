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

import groovy.lang.Binding
import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyShell
import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.jline.console.CmdLine
import org.junit.jupiter.api.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Direct tests for {@link GroovyEngine}. The engine is the foundation of the
 * groovysh stack — it holds the binding, runs scripts, and tracks user-defined
 * imports / variables / methods / types. Exercising it directly (no JLine
 * registry, console, or terminal) gives the most portable test layer.
 */
class GroovyEngineTest {

    private final GroovyEngine engine = new GroovyEngine()

    // GROOVY-12334
    @Test
    void autoDeserializeNeverEvaluatesTheValue() {
        // a bracketed payload used to reach execute(); the shape of the document must not decide
        // whether it is treated as data or as code
        engine.execute('sideEffect = 0')
        engine.deserialize("[sideEffect = 1, 'x']", 'AUTO')
        assert engine.execute('sideEffect') == 0

        // and the same for the curly form, which was already parse-only
        engine.deserialize("{'a': sideEffect = 2}", 'AUTO')
        assert engine.execute('sideEffect') == 0
    }

    // GROOVY-12334
    @Test
    void autoDeserializeStillParsesJson() {
        assert engine.deserialize('[1, 2, 3]', 'AUTO').toString() == '[1, 2, 3]'
        assert engine.deserialize('{"a": 1}', 'AUTO')['a'] == 1
    }

    // GROOVY-12334
    @Test
    void autoDeserializeReturnsUnrecognisedTextUnchanged() {
        assert engine.deserialize('just some text', 'AUTO') == 'just some text'
    }

    // GROOVY-12334
    @Test
    void groovyFormatStillEvaluates() {
        // the explicit opt-in is unchanged: -f GROOVY is the documented way to evaluate content
        engine.execute('marker = 0')
        engine.deserialize('[marker = 7]', 'GROOVY')
        assert engine.execute('marker') == 7
    }

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

    @Test
    void defaultConstructorUsesDefaultCompilerConfiguration() {
        def engine = new GroovyEngine()
        assert engine.compilerConfiguration.is(CompilerConfiguration.DEFAULT)
        assert engine.classLoader.hasCompatibleConfiguration(CompilerConfiguration.DEFAULT)
        assert engine.classLoader.is(engine.getClassLoader())
    }

    @Test
    void nullCompilerConfigurationUsesDefault() {
        def engine = new GroovyEngine((CompilerConfiguration) null)
        assert engine.compilerConfiguration.is(CompilerConfiguration.DEFAULT)
        assert engine.execute('2 + 2') == 4
    }

    @Test
    void importCustomizerMakesTypesAvailableWithoutSnippetImports() {
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.atomic.AtomicInteger')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)
        def engine = new GroovyEngine(config)

        assert engine.execute('new AtomicInteger(7).get()') == 7
        assert engine.imports.isEmpty()
    }

    @Test
    void starAndStaticStarImportCustomizers() {
        def imports = new ImportCustomizer()
        imports.addStarImports('java.util.concurrent.atomic')
        imports.addStaticStars('java.lang.Math')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)
        def engine = new GroovyEngine(config)

        assert engine.execute('new AtomicInteger(3).get()') == 3
        assert engine.execute('abs(-4)') == 4
    }

    @Test
    void sharedBindingAndParentClassLoaderAreHonoured() {
        def parent = new URLClassLoader(new URL[0], ClassLoader.systemClassLoader)
        def binding = new Binding(foo: 11)
        def config = new CompilerConfiguration()
        def engine = new GroovyEngine(parent, binding, config)

        assert engine.classLoader.parent.is(parent)
        assert engine.execute('foo') == 11
        engine.execute('bar = 22')
        assert binding.getVariable('bar') == 22
        assert engine.compilerConfiguration.is(config)
    }

    @Test
    void compatibleEngineClassLoaderIsReused() {
        def config = new CompilerConfiguration()
        def loader = new GroovyEngine.EngineClassLoader(config)
        def engine = new GroovyEngine(loader, new Binding(), config)
        assert engine.classLoader.is(loader)
    }

    @Test
    void incompatibleEngineClassLoaderIsWrapped() {
        def config1 = new CompilerConfiguration()
        def config2 = new CompilerConfiguration()
        def loader = new GroovyEngine.EngineClassLoader(config1)
        def engine = new GroovyEngine(loader, new Binding(), config2)
        assert !engine.classLoader.is(loader)
        assert engine.classLoader.parent.is(loader)
        assert engine.classLoader.hasCompatibleConfiguration(config2)
    }

    @Test
    void createShellIsInvokedFromTheConstructor() {
        def engine = new RecordingEngine()
        assert engine.createShellCalls == 1
        assert engine.execute('1 + 1') == 2
    }

    @Test
    void createShellMustKeepTheEngineClassLoader() {
        def thrown = false
        try {
            new MismatchedLoaderEngine()
        } catch (IllegalStateException e) {
            thrown = e.message.contains('createShell')
        }
        assert thrown
    }

    @Test
    void threadInterruptCustomizerStopsALoopOnInterrupt() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))
        def engine = new GroovyEngine(config)
        def thrown = new AtomicReference<Throwable>()
        def started = new CountDownLatch(1)
        engine.put('started', started)
        def thread = Thread.start {
            try {
                engine.execute('started.countDown(); def n = 0; while (true) { n++ }')
            } catch (Throwable t) {
                thrown.set(t)
            }
        }
        assert started.await(5, TimeUnit.SECONDS)
        thread.interrupt()
        thread.join(5000)
        assert !thread.alive
        assert thrown.get() != null
        assert containsInterrupted(thrown.get())
    }

    @Test
    @SuppressWarnings('deprecation')
    void inspectorStillConstructsAgainstACustomConfiguration() {
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.atomic.AtomicInteger')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)
        def engine = new GroovyEngine(config)
        engine.execute('n = new AtomicInteger(1)')
        // scriptDescription constructs Inspector, which previously built a GroovyShell
        // with CompilerConfiguration.DEFAULT and would drop the customizer
        engine.scriptDescription(new CmdLine('n', 'n', '', ['n'], CmdLine.DescriptionType.COMMAND))
        assert engine.execute('new AtomicInteger(2).get()') == 2
    }

    @Test
    void engineClassLoaderConstructors() {
        def config = new CompilerConfiguration()
        def parent = ClassLoader.systemClassLoader
        assert new GroovyEngine.EngineClassLoader() != null
        assert new GroovyEngine.EngineClassLoader(config).hasCompatibleConfiguration(config)
        assert new GroovyEngine.EngineClassLoader(parent).parent.is(parent)
        assert new GroovyEngine.EngineClassLoader(parent, config).parent.is(parent)
    }

    private static boolean containsInterrupted(Throwable t) {
        while (t != null) {
            if (t instanceof InterruptedException) {
                return true
            }
            t = t.cause
        }
        false
    }

    private static class MismatchedLoaderEngine extends GroovyEngine {
        @Override
        protected GroovyShell createShell(ClassLoader classLoader, Binding binding, CompilerConfiguration configuration) {
            return new GroovyShell(new GroovyClassLoader(), binding, configuration)
        }
    }

    private static class RecordingEngine extends GroovyEngine {
        int createShellCalls

        RecordingEngine() {
            super()
        }

        @Override
        protected GroovyShell createShell(ClassLoader classLoader, Binding binding, CompilerConfiguration configuration) {
            createShellCalls++
            return super.createShell(classLoader, binding, configuration)
        }
    }
}
