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
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.InstructionSequence
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation: protected and package-private members of
 * a class with the same package name, compiled by another compilation unit.
 * <p>
 * The JVM allows such access only within the same run-time package, which is
 * the package name and the class loader. GroovyClassLoader defines the classes
 * of each compilation unit with its own class loader, so a class of an earlier
 * parse of the same GroovyShell, or a precompiled class used by a script, is
 * in another run-time package.
 */
final class StaticCompileRuntimePackageTest extends AbstractBytecodeTestCase {

    private final GroovyShell shell = new GroovyShell()

    private static final String LIBRARY = '''package p
        class A {
            final String value
            protected A(String value) { this.value = 'prot:' + value }
            @groovy.transform.PackageScope A(Integer i) { this.value = 'pkg:' + i }
            @groovy.transform.PackageScope A() { this.value = 'none' }
            String name
        }
        true
    '''

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateConstructorOfClassOfAnotherClassLoader() {
        shell.evaluate LIBRARY
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class C { // same package name as A, but another class loader
                static A prot() { new A('x') }
                static A pkg() { new A(1) }
                static A none() { new A() }
                static List<A> closure() { [1, 2].collect { Integer i -> new A(i) } }
                static A lambda() { java.util.function.Supplier<A> s = () -> new A('y'); s.get() }
            }
            [C.prot(), C.pkg(), C.none(), *C.closure(), C.lambda()]*.value
        ''') == ['prot:x', 'pkg:1', 'none', 'pkg:1', 'pkg:2', 'prot:y']
    }

    // GROOVY-12568
    @Test
    void testPackagePrivateConstructorOfPrecompiledClassUsedByScript() {
        def unit = new CompilationUnit()
        unit.addSource('Library.groovy', LIBRARY)
        unit.compile(Phases.CLASS_GENERATION)
        def libraryLoader = new GroovyClassLoader()
        unit.classes.each { libraryLoader.defineClass(it.name, it.bytes) }

        assert new GroovyShell(libraryLoader).evaluate('''package p
            @groovy.transform.CompileStatic
            A make() { new A(1) }
            make().value
        ''') == 'pkg:1'
    }

    // GROOVY-12568
    @Test
    void testMapStyleConstructionWithPackagePrivateConstructorOfClassOfAnotherClassLoader() {
        shell.evaluate LIBRARY
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            A make() { new A(name: 'n') }
            def a = make()
            a.value + ':' + a.name
        ''') == 'none:n'
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateConstructorsOfSameCompilationUnitAreCalledDirectly() {
        def bytecode = compile(method: 'make', classNamePattern: 'p.C', '''package p
            class A {
                protected A(String s) { }
                @groovy.transform.PackageScope A(Integer i) { }
            }
            @groovy.transform.CompileStatic
            class C {
                static List<A> make() {
                    [new A('x'), new A(1)]
                }
            }
        ''')
        assert bytecode.hasSequence(['NEW p/A', 'DUP', 'LDC "x"', 'INVOKESPECIAL p/A.<init> (Ljava/lang/String;)V'])
        assert bytecode.hasSequence(['NEW p/A', 'DUP', 'ICONST_1', 'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;', 'INVOKESPECIAL p/A.<init> (Ljava/lang/Integer;)V'])
        assert !bytecode.hasSequence(['ScriptBytecodeAdapter.invokeNew'])
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateConstructorsOfClassOfAnotherCompilationUnitAreSelectedAtRuntime() {
        def bytecode = compileAgainstLibrary(LIBRARY, 'p.C', 'make', '''package p
            @groovy.transform.CompileStatic
            class C {
                static List<A> make() {
                    [new A('x'), new A(1)]
                }
            }
        ''')
        assert bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeNewN (Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;', 'CHECKCAST p/A'])
        assert !bytecode.hasSequence(['INVOKESPECIAL p/A.<init>'])
    }

    // GROOVY-12568
    @Test
    void testPublicConstructorOfClassOfAnotherCompilationUnitIsCalledDirectly() {
        def bytecode = compileAgainstLibrary('''package p
            class B {
                B(String s) { }
            }
        ''', 'p.C', 'make', '''package p
            @groovy.transform.CompileStatic
            class C {
                static B make() {
                    new B('x')
                }
            }
        ''')
        assert bytecode.hasSequence(['NEW p/B', 'DUP', 'LDC "x"', 'INVOKESPECIAL p/B.<init> (Ljava/lang/String;)V'])
        assert !bytecode.hasSequence(['ScriptBytecodeAdapter.invokeNew'])
    }

    /**
     * Compiles {@code script} against the classes of {@code library}, which are
     * compiled before, by another compilation unit and class loader.
     */
    private InstructionSequence compileAgainstLibrary(final String library, final String className, final String method, final String script) {
        def loader = new GroovyClassLoader()
        loader.parseClass(library)
        def unit = new CompilationUnit(null, null, loader)
        unit.addSource('script', script)
        unit.compile(Phases.CLASS_GENERATION)
        def generated = unit.classes.find { it.name == className }
        assert generated != null : "class $className not found"
        def sequence = extractSequence(generated.bytes, [method: method])
        List<String> instructions = sequence.instructions
        int begin = instructions.indexOf('--BEGIN--'), end = instructions.indexOf('--END--')
        assert begin >= 0 && end > begin : "method $method not found"
        new InstructionSequence(instructions: instructions[(begin + 1)..<end])
    }
}
