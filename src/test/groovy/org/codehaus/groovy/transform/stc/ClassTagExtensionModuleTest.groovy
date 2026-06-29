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
package org.codehaus.groovy.transform.stc

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

import static groovy.test.GroovyAssert.shouldFail

/**
 * Integration tests for {@code @ClassTag} (GROOVY-12115) against a <em>precompiled</em> extension
 * module supplied as a jar - the situation of a library on the compile classpath, as opposed to
 * the same-unit sources covered by {@link groovy.transform.stc.ClassTagStaticTest}. Chiefly the
 * containment invariant: a module can preempt callers of <em>its own</em> lenient API, but a jar
 * on the compile classpath can never capture existing calls owned by another module (here: a
 * hostile {@code each} overload that must not re-route DGM's {@code each}), plus the fail-soft
 * degrade paths that only arise for precompiled declarations (a mistyped {@code @ClassTag("Z")}
 * override, a method-declared type variable shadowing the class's) - neither is visited as source,
 * so each must silently disable injection rather than error or mis-inject.
 */
final class ClassTagExtensionModuleTest {

    /**
     * The fixture "library": compiled once into a jar with an extension module descriptor, never
     * compiled as part of any test compilation. {@code each} attempts a cross-module capture of a
     * DGM method; the {@code tag} pair is the module's own lenient/checked API; {@code typoView}
     * carries a {@code @ClassTag} override naming a nonexistent type variable; {@code ShadowBox}
     * (a plain class, reachable via the same jar) tags a method-declared type variable that
     * shadows the class's - both mistakes that the source-level checks cannot reach here.
     */
    private static final String FIXTURE_SOURCE = '''
        package fixture

        import groovy.transform.stc.ClassTag

        class CtExtensions {
            static <T> void each(Iterable<T> self, @ClassTag(preempt=true) Class<T> type, Closure c) {
                throw new IllegalStateException('captured: a foreign module must not re-route DGM each')
            }
            static <T> String tag(List<T> self, Closure c) { 'lenient' }
            static <T> String tag(List<T> self, @ClassTag(preempt=true) Class<T> type, Closure c) {
                'checked:' + type.simpleName
            }
            static <T> List<T> typoView(List<T> self, @ClassTag('Z') Class<T> type) { self }
        }

        class ShadowBox<K,V> {
            def <K> K pick(@ClassTag Class<K> type, Closure c) { null }
        }
    '''

    private static final String DESCRIPTOR = '''\
        moduleName=ClassTag test module
        moduleVersion=1.0-test
        extensionClasses=fixture.CtExtensions
    '''.stripIndent()

    @TempDir
    static File tempDir

    private static File moduleJar

    @BeforeAll
    static void buildFixtureJar() {
        File classesDir = new File(tempDir, 'classes')
        // plain dynamic compilation: the fixture is never type checked, mirroring a library
        // built by any compiler - the source-level @ClassTag validation cannot have run on it
        def config = new CompilerConfiguration(targetDirectory: classesDir)
        def unit = new CompilationUnit(config)
        unit.addSource('fixture.groovy', FIXTURE_SOURCE)
        unit.compile()

        moduleJar = new File(tempDir, 'classtag-module-test.jar')
        new JarOutputStream(moduleJar.newOutputStream()).withCloseable { jar ->
            classesDir.eachFileRecurse { f ->
                if (f.file) {
                    String path = classesDir.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '/' as char)
                    jar.putNextEntry(new JarEntry(path))
                    jar << f.bytes
                    jar.closeEntry()
                }
            }
            jar.putNextEntry(new JarEntry(ExtensionModuleScanner.MODULE_META_INF_FILE))
            jar.write(DESCRIPTOR.getBytes('UTF-8'))
            jar.closeEntry()
        }
    }

    private static GroovyClassLoader loaderWithModule() {
        def loader = new GroovyClassLoader(ClassTagExtensionModuleTest.classLoader)
        loader.addURL(moduleJar.toURI().toURL())
        loader
    }

    private static Object evalCompileStatic(GroovyClassLoader loader, String script) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        new GroovyShell(loader, config).evaluate(script)
    }

    @Test
    void testForeignModuleCannotCaptureDgmMethod() {
        // the hostile each declares preempt intent and arity-matches, but its owner is not DGM:
        // containment filters it out and the call still binds (and runs) DGM's each
        def result = evalCompileStatic(loaderWithModule(), '''
            List<String> xs = ['a', 'b']
            def out = []
            xs.each { out << it.toUpperCase() }
            out
        ''')
        assert result == ['A', 'B']
    }

    @Test
    void testModulePreemptsItsOwnLenientApi() {
        // same-owner preemption: the module's checked tag overload upgrades calls that bound
        // its own lenient sibling, tokens reified from the receiver of the precompiled method
        def result = evalCompileStatic(loaderWithModule(), '''
            List<String> xs = []
            xs.tag{ }
        ''')
        assert result == 'checked:String'
    }

    @Test
    void testModulePreemptionSubjectToGlobalDisable() {
        // the consumer's global opt-out also vetoes a precompiled module's declared intent
        def config = new CompilerConfiguration()
        config.classTagPreemptionDisabled = true
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        def result = new GroovyShell(loaderWithModule(), config).evaluate('''
            List<String> xs = []
            xs.tag{ }
        ''')
        assert result == 'lenient'
    }

    @Test
    void testMistypedOverrideOnPrecompiledMethodDegradesSilently() {
        // @ClassTag('Z') names no type variable; on a precompiled method this cannot be reported
        // (never visited as source), so injection is abandoned and the call fails as written
        def err = shouldFail(MultipleCompilationErrorsException) {
            evalCompileStatic(loaderWithModule(), '''
                List<String> xs = []
                xs.typoView()
            ''')
        }
        assert err.message.contains('Cannot find matching method')
        assert err.message.contains('typoView()')
    }

    @Test
    void testShadowedTypeVariableOnPrecompiledMethodDegradesSilently() {
        // the method-declared K shadows the class-level K; the matching-side guard must degrade
        // to no injection (mis-injecting the receiver's K would reify the wrong class)
        def err = shouldFail(MultipleCompilationErrorsException) {
            evalCompileStatic(loaderWithModule(), '''
                import fixture.ShadowBox
                void use(ShadowBox<Number,String> b) {
                    b.pick{ }
                }
            ''')
        }
        assert err.message.contains('Cannot find matching method')
        assert err.message.contains('pick(groovy.lang.Closure')
    }

    @Test
    void testPreemptiveNamesDerivedPerClassLoader() {
        def loader = loaderWithModule()
        // trigger the extension scan for this loader, then check the gate's name set
        evalCompileStatic(loader, "['x'].tag{ }")
        Set<String> withModule = ExtensionMethodCache.INSTANCE.getPreemptiveNames(loader)
        assert withModule.contains('tag')
        assert withModule.contains('each')       // intent declared, even though containment blocks it
        assert !withModule.contains('typoView')  // no preempt intent
        // a loader without the module sees only DGM's declared intent
        Set<String> withoutModule = ExtensionMethodCache.INSTANCE.getPreemptiveNames(this.class.classLoader)
        assert withoutModule.contains('withDefault')
        assert !withoutModule.contains('tag')
    }
}
