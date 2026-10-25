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
package org.codehaus.groovy.classgen.asm

import groovy.transform.AnnotationCollector
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

import static groovy.test.GroovyAssert.shouldFail

/**
 * Checks how the constructor of a class that implements a trait runs the instance
 * initializer of the trait.
 * <p>
 * The constructor calls the static {@code $init$} method of the trait helper with a
 * direct {@code invokestatic}, in dynamically and statically compiled classes alike,
 * instead of dispatching the call through the meta class of the helper.
 */
final class TraitInitBytecodeTest extends AbstractBytecodeTestCase {

    @ParameterizedTest
    @ValueSource(strings=['@groovy.transform.CompileDynamic','@groovy.transform.TypeChecked','@groovy.transform.CompileStatic'])
    @AnnotationCollector
    @interface CompileModesTest { }

    private List<String> constructorsOf(final String className, final String source) {
        compile(method: '<init>', classNamePattern: className, source)
        sequence.instructions
    }

    private static void assertDirectInitCalls(final List<String> code, final String... traitNames) {
        def initCalls = code.findAll { it.contains('.$init$ (') }
        assert initCalls == traitNames.collect { 'INVOKESTATIC ' + it + '$Trait$Helper.$init$ (L' + it + ';)V' }
        // the name is a constant only for invokeMethodN or as the bootstrap argument of an invokedynamic call
        assert !code.any { it.contains('"$init$"') }
    }

    @CompileModesTest
    void testTraitInitializerIsCalledDirectly(final String mode) {
        def code = constructorsOf 'Greeter', """
            trait Greeting {
                String greeting = 'hello'
            }
            $mode
            class Greeter implements Greeting {
            }
        """
        assertDirectInitCalls(code, 'Greeting')
    }

    @CompileModesTest
    void testInitializerOfTraitWithoutFieldsIsCalledDirectly(final String mode) {
        def code = constructorsOf 'Greeter', """
            trait Greeting {
                String greet() { 'hello' }
            }
            $mode
            class Greeter implements Greeting {
            }
        """
        assertDirectInitCalls(code, 'Greeting')
    }

    @CompileModesTest
    void testInitializersOfSeveralTraitsAreCalledDirectlyInOrder(final String mode) {
        def code = constructorsOf 'Impl', """
            trait A {
                String a = 'a'
            }
            trait B extends A {
                String b = 'b'
            }
            trait C {
                String c = 'c'
            }
            $mode
            class Impl implements B, C {
            }
        """
        assertDirectInitCalls(code, 'C', 'B', 'A')
    }

    @CompileModesTest
    void testInitializerOfGenericTraitIsCalledDirectly(final String mode) {
        def code = constructorsOf 'Strings', """
            trait Holder<T> {
                T value
                List<T> items = []
            }
            $mode
            class Strings implements Holder<String> {
            }
        """
        assertDirectInitCalls(code, 'Holder')
    }

    @CompileModesTest
    void testInitializerOfSelfTypeTraitIsCalledDirectly(final String mode) {
        def code = constructorsOf 'Greeter', """
            class Base {
                String name = 'base'
            }
            @groovy.transform.SelfType(Base)
            trait Greeting {
                String greeting = 'hello'
                String greet() { "\$greeting \$name" }
            }
            $mode
            class Greeter extends Base implements Greeting {
            }
        """
        assertDirectInitCalls(code, 'Greeting')
    }

    @CompileModesTest
    void testInitializerOfPrecompiledTraitIsCalledDirectly(final String mode) {
        File traitDir = File.createTempDir()
        try {
            def traitUnit = new CompilationUnit(new CompilerConfiguration(targetDirectory: traitDir))
            traitUnit.addSource('Greeting.groovy', '''
                trait Greeting {
                    String greeting = 'hello'
                    private int count = 2
                    String greet() { greeting * count }
                }
            ''')
            traitUnit.compile()

            def loader = new GroovyClassLoader(this.class.classLoader)
            loader.addClasspath(traitDir.absolutePath)
            def unit = new CompilationUnit(new CompilerConfiguration(), null, loader)
            unit.addSource('Greeter.groovy', """
                $mode
                class Greeter implements Greeting {
                }
            """)
            unit.compile(Phases.CLASS_GENERATION)
            assert unit.classes*.name == ['Greeter'] // the trait and its helper come from class files

            def greeterClass = unit.classes.find { it.name == 'Greeter' }
            assertDirectInitCalls(extractSequence(greeterClass.bytes, [method: '<init>']).instructions, 'Greeting')

            def greeter = loader.defineClass('Greeter', greeterClass.bytes).getDeclaredConstructor().newInstance()
            assert greeter.greeting == 'hello'
            assert greeter.greet() == 'hellohello'
        } finally {
            traitDir.deleteDir()
        }
    }

    // GROOVY-12491
    @CompileModesTest
    void testPrecompiledTraitHelperWithoutInitializerIsRejected(final String mode) {
        File traitDir = File.createTempDir()
        try {
            def traitUnit = new CompilationUnit(new CompilerConfiguration(targetDirectory: traitDir))
            traitUnit.addSource('Greeting.groovy', '''
                trait Greeting {
                    String greeting = 'hello'
                }
            ''')
            traitUnit.compile()

            // remove $init$ from the helper, so that the lookup of the call target finds nothing
            File helperFile = new File(traitDir, 'Greeting$Trait$Helper.class')
            def writer = new ClassWriter(0)
            new ClassReader(helperFile.bytes).accept(new ClassVisitor(CompilerConfiguration.ASM_API_VERSION, writer) {
                @Override
                MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    name == '$init$' ? null : super.visitMethod(access, name, desc, signature, exceptions)
                }
            }, 0)
            helperFile.bytes = writer.toByteArray()

            def loader = new GroovyClassLoader(this.class.classLoader)
            loader.addClasspath(traitDir.absolutePath)
            def unit = new CompilationUnit(new CompilerConfiguration(), null, loader)
            unit.addSource('Greeter.groovy', """
                $mode
                class Greeter implements Greeting {
                }
            """)
            def err = shouldFail(GroovyBugError) {
                unit.compile(Phases.CLASS_GENERATION)
            }
            assert err.message.contains('Trait helper Greeting$Trait$Helper does not declare $init$(Greeting)')
        } finally {
            traitDir.deleteDir()
        }
    }

    @CompileModesTest
    void testTraitFieldsAreInitialized(final String mode) {
        assertScript """
            trait Greeting {
                String greeting = 'hello'
                private int count = 2
                final List<String> names = ['a', 'b']
                String owner = this.getClass().simpleName
                String greet() { "\$greeting x\$count \$names" }
            }
            trait Holder<T> {
                T value
                List<T> items = []
            }
            $mode
            class Greeter implements Greeting, Holder<String> {
                static Greeting anonymous() {
                    new Greeting() {}
                }
            }

            def greeter = new Greeter()
            assert greeter.greeting == 'hello'
            assert greeter.names == ['a', 'b']
            assert greeter.owner == 'Greeter'
            assert greeter.greet() == 'hello x2 [a, b]'
            assert greeter.value == null
            assert greeter.items == []
            assert !greeter.items.is(new Greeter().items)

            def anonymous = Greeter.anonymous()
            assert anonymous.greeting == 'hello'
            assert anonymous.greet() == 'hello x2 [a, b]'
        """
    }

    @CompileModesTest
    void testInitializationOrderIsUnchanged(final String mode) {
        assertScript """
            class Log {
                static List<String> events = []
                static String log(String event) { events << event; event }
            }
            trait A {
                String a = Log.log('A.a')
            }
            trait B extends A {
                String b = Log.log('B.b')
            }
            trait C {
                String c = Log.log('C.c')
                final String cf = Log.log('C.cf')
            }
            $mode
            class Impl implements B, C {
                String own = Log.log('own');
                { Log.log('block') }
                Impl() { Log.log('ctor') }
            }

            def impl = new Impl()
            assert Log.events == ['own', 'block', 'C.cf', 'C.c', 'B.b', 'A.a', 'ctor']
            assert [impl.a, impl.b, impl.c, impl.cf, impl.own] == ['A.a', 'B.b', 'C.c', 'C.cf', 'own']
        """
    }

    // a method added to the meta class of the helper only replaces $init$ for a call that is dispatched
    // through that meta class; this also reaches the classes generated for withTraits and as at runtime
    @Test
    void testTraitInitializerIsNotDispatchedThroughMetaClass() {
        assertScript '''
            trait Greeting {
                String greeting = 'hello'
            }
            class Greeter implements Greeting {
            }
            @groovy.transform.CompileStatic
            class StaticGreeter implements Greeting {
            }

            def dispatched = []
            Class helper = Greeting.classLoader.loadClass('Greeting$Trait$Helper')
            helper.metaClass.static.'$init$' = { Greeting self -> dispatched << self }

            def greetings = [new Greeter(), new StaticGreeter(), new Object().withTraits(Greeting), new Object() as Greeting]
            assert dispatched.isEmpty()
            assert greetings*.greeting == ['hello'] * 4
        '''
    }

    @Test
    void testRuntimeTraitApplicationInitializesFields() {
        assertScript '''
            class Log {
                static List<String> events = []
                static String log(String event) { events << event; event }
            }
            trait A {
                String a = Log.log('A.a')
            }
            trait B extends A {
                String b = Log.log('B.b')
            }
            trait C {
                String c = Log.log('C.c')
                final String cf = Log.log('C.cf')
            }

            def withTraits = new Object().withTraits(B, C)
            assert Log.events == ['C.cf', 'C.c', 'A.a', 'B.b']
            assert [withTraits.a, withTraits.b, withTraits.c, withTraits.cf] == ['A.a', 'B.b', 'C.c', 'C.cf']

            Log.events.clear()
            def coerced = new Object() as C
            assert Log.events == ['C.cf', 'C.c']
            assert [coerced.c, coerced.cf] == ['C.c', 'C.cf']
        '''
    }
}
