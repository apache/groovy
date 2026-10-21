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
package groovy.transform.stc

import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.transform.stc.SignatureCodecVersion1
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import java.nio.file.Path

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A call to a {@code def} method that only its own body can answer has the type
 * that body returns: a final method or a method of a final class under any
 * dispatch, and a private or static method when the call is dispatched
 * statically. The type is inferred within the class being checked and is read
 * from the class file of a precompiled method.
 */
final class MethodReturnTypeInferenceTest {

    private static final String OBJECT_TO_UPPER_CASE = 'Cannot find matching method java.lang.Object#toUpperCase()'

    @TempDir
    Path classes

    private static void assertFailsWith(final String script, final String message = OBJECT_TO_UPPER_CASE) {
        def error = shouldFail(MultipleCompilationErrorsException, script)
        assert error.message.contains(message)
    }

    //--------------------------------------------------------------------------
    // within the class being checked

    @Test
    void testStaticallyCompiledCallsUsePrivateStaticAndFinalBodies() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                private def name() { 'x' }
                static def twice(int i) { i * 2 }
                final def names() { ['a', 'b'] }
                String use() {
                    name().toUpperCase() + twice(2).intValue() + names().get(1).toUpperCase()
                }
            }
            assert new A().use() == 'X4B'
        '''
    }

    @Test
    void testMethodDeclaredAfterItsCaller() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                int use() { helper().size() }
                private def helper() { [1, 2, 3] }
            }
            assert new A().use() == 3
        '''
    }

    @Test
    void testCallFromClosure() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                private def name() { 'x' }
                String use() {
                    def c = { -> name().toUpperCase() }
                    c()
                }
            }
            assert new A().use() == 'X'
        '''
    }

    @Test
    void testTypeCheckedFinalMethodAndFinalClass() {
        assertScript '''
            @groovy.transform.TypeChecked
            class A {
                final def name() { 'a' }
                String use() { name().toUpperCase() }
            }
            @groovy.transform.TypeChecked
            final class B {
                def name() { 'b' }
                String use() { name().toUpperCase() }
            }
            assert new A().use() + new B().use() == 'AB'
        '''
    }

    @Test
    void testTypeCheckedRecord() {
        assertScript '''
            @groovy.transform.TypeChecked
            record Point(int x, int y) {
                def label() { "($x, $y)".toString() }
                String use() { label().toUpperCase() }
            }
            assert new Point(1, 2).use() == '(1, 2)'
        '''
    }

    // a dynamically dispatched call can reach a same-named method of a subclass
    @ParameterizedTest
    @ValueSource(strings = ['private def', 'static def'])
    void testTypeCheckedPrivateOrStaticMethodViaInstance(final String declaration) {
        assertFailsWith """
            @groovy.transform.TypeChecked
            class A {
                $declaration name() { 'x' }
                String use() { this.name().toUpperCase() }
            }
        """
        assertFailsWith """
            @groovy.transform.TypeChecked
            class A {
                $declaration name() { 'x' }
                String use() { def c = { -> name().toUpperCase() }; c() }
            }
        """
    }

    @Test
    void testTypeCheckedStaticMethodViaClass() {
        assertScript '''
            @groovy.transform.TypeChecked
            class A {
                static def name() { 'a' }
                static String use() {
                    name().toUpperCase() + A.name().toUpperCase() + { -> name().toUpperCase() }()
                }
                String viaClass() { A.name().toUpperCase() }
            }
            assert A.use() + new A().viaClass() == 'AAAA'
        '''
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testOverridableMethodUsesDeclaredType(final String mode) {
        assertFailsWith """
            @groovy.transform.$mode
            class A {
                def name() { 'x' }
                String use() { name().toUpperCase() }
            }
        """
    }

    // an override returns a different type
    @Test
    void testOverriddenMethodUnderStaticCompilation() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                def create() { 'a' }
                String use() { def x = create(); x.toString() }
            }
            @groovy.transform.CompileStatic
            class B extends A {
                def create() { 42 }
            }
            assert new B().use() == '42'
        '''
    }

    @Test
    void testValuesAndNullsCombine() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                private def name(boolean b) { if (b) return 'x'; null }
                private def count() { 1 }
                String use() { name(true).toUpperCase() + (name(false) == null) + count().intValue() }
            }
            assert new A().use() == 'Xtrue1'
        '''
    }

    @Test
    void testUnrelatedValuesUseDeclaredType() {
        assertFailsWith '''
            @groovy.transform.CompileStatic
            class A {
                private def value(boolean b) { if (b) return 'x'; 1 }
                def use() { value(true).toUpperCase() }
            }
        '''
    }

    @Test
    void testRecursiveCallUsesDeclaredType() {
        assertFailsWith '''
            @groovy.transform.CompileStatic
            class A {
                private def countdown(int n) { n == 0 ? 'done' : countdown(n - 1) }
                String use() { countdown(3).toUpperCase() }
            }
        '''
    }

    @Test
    void testTypeParameterUsesDeclaredType() {
        assertFailsWith '''
            @groovy.transform.CompileStatic
            class Box<T> {
                T value
                final def get() { value }
            }
            @groovy.transform.CompileStatic
            String use() { new Box<String>(value: 'x').get().toUpperCase() }
        '''
    }

    // whether another class has been checked depends on the order classes are checked in
    @Test
    void testMethodOfAnotherClassUsesDeclaredType() {
        assertFailsWith '''
            @groovy.transform.CompileStatic
            final class A {
                def name() { 'x' }
            }
            @groovy.transform.CompileStatic
            class B {
                String use(A a) { a.name().toUpperCase() }
            }
        '''
        assertFailsWith '''
            @groovy.transform.CompileStatic
            class B {
                String use(A a) { a.name().toUpperCase() }
            }
            @groovy.transform.CompileStatic
            final class A {
                def name() { 'x' }
            }
        '''
    }

    // an extension expects to see each method visited at its declaration
    @Test
    void testTypeCheckingExtensionUsesDeclaredType() {
        assertFailsWith '''
            @groovy.transform.CompileStatic(extensions = 'groovy/transform/stc/SetupTestExtension.groovy')
            class A {
                private def name() { 'x' }
                String use() { name().toUpperCase() }
            }
        '''
    }

    @Test
    void testErrorInMethodCheckedEarlyIsReportedOnce() {
        def error = shouldFail(MultipleCompilationErrorsException, '''
            @groovy.transform.CompileStatic
            class A {
                String use() { helper().toUpperCase() }
                private def helper() { 'x'.missing(); 'x' }
            }
        ''')
        assert error.errorCollector.errorCount == 1
        assert error.message.contains('Cannot find matching method java.lang.String#missing()')
    }

    //--------------------------------------------------------------------------
    // precompiled

    private static final String LIBRARY = '''
        package lib
        @groovy.transform.CompileStatic
        class Lib {
            static def today() { new Date() }
            final def names() { ['a', 'b'] }
            def open() { 'open' }
            private def hidden() { 'hidden' }
            static def nothing() { null }
            def touch() { hidden() }
        }
        @groovy.transform.TypeChecked
        final class Box {
            def label() { 'box' }
        }
    '''

    private void compileLibrary(final String source) {
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: classes.toFile()))
        unit.addSource('Lib.groovy', source)
        unit.compile()
    }

    /**
     * Returns a shell that sees the library as class files ({@code "decompiled"})
     * or as classes loaded without class files ({@code "loaded"}).
     */
    private GroovyShell consumer(final String resolution, final String source = LIBRARY) {
        def parent = new GroovyClassLoader(this.class.classLoader)
        if (resolution == 'decompiled') {
            compileLibrary(source)
            parent.addClasspath(classes.toString())
        } else {
            parent.parseClass(source, 'Lib.groovy')
        }
        new GroovyShell(new GroovyClassLoader(parent))
    }

    @Test
    void testWriterAnnotatesMethodsWithSoleImplementation() {
        compileLibrary(LIBRARY)
        def loader = new URLClassLoader([classes.toUri().toURL()] as URL[], this.class.classLoader)
        def lib = loader.loadClass('lib.Lib')
        def info = { Class type, String name -> type.getDeclaredMethods().find { it.name == name }.getAnnotation(TypeChecked.TypeCheckingInfo) }

        assert info(lib, 'today')?.version() == 1
        assert info(lib, 'names')?.version() == 1
        assert info(loader.loadClass('lib.Box'), 'label')?.version() == 1
        for (name in ['open', 'hidden', 'nothing', 'touch', 'getMetaClass']) {
            assert info(lib, name) == null
        }
        assert lib.declaredConstructors.every { it.getAnnotation(TypeChecked.TypeCheckingInfo) == null }
    }

    @ParameterizedTest
    @ValueSource(strings = ['decompiled', 'loaded'])
    void testStaticallyCompiledConsumer(final String resolution) {
        assert consumer(resolution).evaluate('''
            import lib.*
            @groovy.transform.CompileStatic
            def use() {
                [Lib.today().getTime() > 0, new Lib().names().get(0).toUpperCase(), new Box().label().toUpperCase()]
            }
            use()
        ''') == [true, 'A', 'BOX']
    }

    @ParameterizedTest
    @ValueSource(strings = ['decompiled', 'loaded'])
    void testTypeCheckedConsumer(final String resolution) {
        def shell = consumer(resolution)
        assert shell.evaluate('''
            import lib.*
            @groovy.transform.TypeChecked
            def use() {
                [Lib.today().getTime() > 0, new Lib().names().get(0).toUpperCase(), new Box().label().toUpperCase()]
            }
            use()
        ''') == [true, 'A', 'BOX']

        def error = shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate('''
                import lib.*
                @groovy.transform.CompileStatic
                def use() { new Lib().open().toUpperCase() }
            ''')
        }
        assert error.message.contains(OBJECT_TO_UPPER_CASE)
    }

    @Test
    void testReaderIgnoresUnusableRecords() {
        def codec = new SignatureCodecVersion1(this.class.classLoader)
        String string = codec.encode(ClassHelper.STRING_TYPE)
        String missing = codec.encode(ClassHelper.make('no.such.Type'))
        def shell = consumer('decompiled', """
            package lib
            import groovy.transform.TypeChecked.TypeCheckingInfo
            class Lib {
                @TypeCheckingInfo(version=2, inferredType='$string') static def future() { 'f' }
                @TypeCheckingInfo(inferredType='$string') static def unversioned() { 'u' }
                @TypeCheckingInfo(version=1, inferredType='!not base64!') static def corrupt() { 'c' }
                @TypeCheckingInfo(version=1, inferredType='$missing') static def missing() { 'm' }
                @TypeCheckingInfo(version=1, inferredType='$string') def overridable() { 'o' }
                @TypeCheckingInfo(version=1, inferredType='$string') static def good() { 'g' }
            }
        """)
        for (call in ['Lib.future()', 'Lib.unversioned()', 'Lib.corrupt()', 'Lib.missing()', 'new Lib().overridable()']) {
            def error = shouldFail(MultipleCompilationErrorsException) {
                shell.evaluate("import lib.*; @groovy.transform.CompileStatic def use() { ${call}.toUpperCase() }")
            }
            assert error.message.contains(OBJECT_TO_UPPER_CASE) : call
        }
        assert shell.evaluate('import lib.*; @groovy.transform.CompileStatic def use() { Lib.good().toUpperCase() }; use()') == 'G'
    }
}
