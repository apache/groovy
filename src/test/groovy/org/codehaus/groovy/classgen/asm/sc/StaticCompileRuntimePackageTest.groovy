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
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

import javax.tools.ToolProvider

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for static compilation: protected and package-private members of
 * a class with the same package name, compiled by another compilation and so
 * maybe defined by another class loader.
 * <p>
 * The JVM allows such access only within the same run-time package, which is
 * the package name and the class loader. GroovyClassLoader defines the classes
 * of each compilation in memory with a class loader of their own, so a class of
 * an earlier parse of the same GroovyShell, or a precompiled class used by a
 * script, is in another run-time package. Class files are expected to be loaded
 * by one class loader, as javac expects.
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

    private static final String MEMBERS = '''package p
        class M {
            @groovy.transform.PackageScope String pm() { 'pm' }
            protected String qm() { 'qm' }
            @groovy.transform.PackageScope static String spm() { 'spm' }
            protected static String sqm() { 'sqm' }
            @groovy.transform.PackageScope int count(int i) { i + 1 }
            protected String getQp() { 'qp' }
            @groovy.transform.PackageScope String getPp() { 'pp' }
            private String hidden = 'h'
            protected void setQs(String s) { hidden = 'qs:' + s }
            String getHidden() { hidden }
        }
        true
    '''

    private static final String FIELDS = '''package p
        class F {
            @groovy.transform.PackageScope String pf = 'pf'
            protected String qf = 'qf'
            @groovy.transform.PackageScope static String spf = 'spf'
            protected static String sqf = 'sqf'
            @groovy.transform.PackageScope int n = 1
        }
        true
    '''

    private static final String HIDDEN = '''package p
        class Base { String base() { 'base' } }
        @groovy.transform.PackageScope class Hidden extends Base implements Runnable {
            static String SF = 'sf'
            static String sm(String s) { 'sm:' + s }
            String name = 'n'
            public String pubField = 'pf'
            boolean ran
            String own() { 'own' }
            void run() { ran = true }
        }
        true
    '''

    /**
     * Uses the package-private class {@code Hidden} in the ways that dynamic
     * code supports from another class loader: none of them names the class.
     */
    private static String usesOfHidden(final String className, final String annotation) {
        """package p
            $annotation
            class $className {
                static List test() {
                    def h = new Hidden()
                    h.run()
                    Runnable r = new Hidden()
                    r.run()
                    h.name = 'm'
                    Hidden.SF = 'w'
                    java.util.function.Supplier<String> ref = h::own
                    def ptr = h.&own
                    def list = [h, new Hidden(name: 'x')]
                    [h.getClass().name, Hidden.sm('a'), Hidden.SF, h.ran, h.own(), h.base(), h.name, h.pubField,
                     arg(r), ref.get(), ptr(), list*.name, list.name, (h ?: new Hidden()).own()]
                }
                static String arg(Runnable r) { r.getClass().simpleName }
            }
            ${className}.test()
        """
    }

    private static final List USES_OF_HIDDEN = ['p.Hidden', 'sm:a', 'w', true, 'own', 'base', 'm', 'pf', 'Hidden', 'own', 'own', ['m', 'x'], ['m', 'x'], 'own']

    private static final String SHUT = '''package p
        class Open {
            public static String SF = 'sf'
            public String pf = 'pf'
            public int n = 1
            public Shut peer
            protected String prot() { 'open' }
            String take(Shut s) { 'open' }
            String take(Object o) { 'object' }
            static Shut make() { new Shut2() }
            static Secret secret() { new Shut2() }
        }
        @groovy.transform.PackageScope class Shut extends Open {
            String prot() { 'shut' }
            String take(Shut s) { 'shut' }
            String own() { 'own' }
        }
        @groovy.transform.PackageScope class Shut2 extends Shut implements Secret {
            String hint() { 'hint' }
        }
        @groovy.transform.PackageScope class Plain {
            String toString() { 'plain' }
        }
        @groovy.transform.PackageScope interface Secret {
            String hint()
        }
        class Pub extends Shut {
        }
        true
    '''

    /**
     * Uses the package-private classes of {@code SHUT} through the public
     * classes {@code Open} and {@code Pub}: a hidden superclass, a hidden class
     * that extends {@code Object}, a hidden interface, members of a public
     * class whose signature names a hidden class, and a public class whose
     * methods are declared by a hidden class.
     */
    private static String usesOfShut(final String className, final String annotation) {
        """package p
            import java.util.function.BiFunction
            import java.util.function.Function
            import java.util.function.Supplier
            $annotation
            class $className {
                static List test() {
                    def s = new Shut2()
                    def plain = new Plain()
                    def secret = Open.secret()
                    Shut.@SF = 'w'
                    s.@pf = 'q'
                    s.@peer = s
                    def list = [s]
                    Function<Shut, String> str = Open::toString
                    Supplier<Shut> made = Open::make
                    Supplier<Object> made2 = Open::make
                    BiFunction<Open, Object, String> take = Open::take
                    Function<Pub, String> own = Pub::own
                    [s.getClass().name, plain.toString(), secret.hint(), Shut.SF, s.pf, s?.n, s.peer.is(s), s.prot(), s.take(s),
                     s.take('x'), s.own(), new Pub().own(), Shut.make().own(), list*.@pf, str.apply(s).startsWith('p.Shut2@'),
                     made.get().getClass().name, made2.get().getClass().name, take.apply(s, 'x'), own.apply(new Pub())]
                }
            }
            ${className}.test()
        """
    }

    private static final List USES_OF_SHUT = ['p.Shut2', 'plain', 'hint', 'w', 'q', 1, true, 'shut', 'shut',
        'object', 'own', 'own', 'own', ['q'], true, 'p.Shut2', 'p.Shut2', 'object', 'own']

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
            @groovy.transform.CompileStatic
            A make(Map map) { new A(map) } // a map that is not a literal
            def a = make(), b = make(name: 'm')
            [a.value + ':' + a.name, b.value + ':' + b.name]
        ''') == ['none:n', 'none:m']
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
    void testProtectedAndPackagePrivateConstructorsOfClassOfAnotherClassLoaderAreSelectedAtRuntime() {
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
    void testPublicConstructorOfClassOfAnotherClassLoaderIsCalledDirectly() {
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

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateMethodsOfClassOfAnotherClassLoader() {
        shell.evaluate MEMBERS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class C { // same package name as M, but another class loader
                static List test(M m) {
                    int n = m.count(1)
                    [m.pm(), m.qm(), M.spm(), M.sqm(), n, m?.pm(), [m]*.qm(), m.qp, m.pp, { -> m.pm() }(), m.with { it.qm() }]
                }
                static String setter(M m) {
                    m.qs = 'v'
                    m.hidden
                }
            }
            C.test(new M()) << C.setter(new M())
        ''') == ['pm', 'qm', 'spm', 'sqm', 2, 'pm', ['qm'], 'qp', 'pp', 'pm', 'qm', 'qs:v']
    }

    // GROOVY-12568
    @Test
    void testInheritedProtectedAndPackagePrivateMethodsOfClassOfAnotherClassLoader() {
        shell.evaluate MEMBERS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class B extends M {
                List test() {
                    [pm(), qm(), spm(), sqm(), this.pm(), super.qm(), qp, pp, { -> pm() + qm() }()]
                }
                static List statics() {
                    [spm(), sqm()]
                }
            }
            new B().test() + B.statics()
        ''') == ['pm', 'qm', 'spm', 'sqm', 'pm', 'qm', 'qp', 'pp', 'pmqm', 'spm', 'sqm']
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateMethodsOfSameCompilationUnitAreCalledDirectly() {
        def bytecode = compile(method: 'test', classNamePattern: 'p.C', MEMBERS + '''
            @groovy.transform.CompileStatic
            class C {
                static List test(M m) {
                    [m.pm(), m.qm(), M.spm()]
                }
            }
        ''')
        assert bytecode.hasSequence(['INVOKEVIRTUAL p/M.pm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL p/M.qm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['INVOKESTATIC p/M.spm ()Ljava/lang/String;'])
        assert !bytecode.hasSequence(['ScriptBytecodeAdapter.invokeMethod'])
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateMethodsOfClassOfAnotherClassLoaderAreSelectedAtRuntime() {
        def bytecode = compileAgainstLibrary(MEMBERS, 'p.C', 'test', '''package p
            @groovy.transform.CompileStatic
            class C {
                static List test(M m) {
                    [m.pm(), m.qm(), M.spm(), m.hidden]
                }
            }
        ''')
        assert bytecode.hasSequence(['LDC "pm"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeMethod0'])
        assert bytecode.hasSequence(['LDC "qm"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeMethod0'])
        assert bytecode.hasSequence(['LDC "spm"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeMethod0'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL p/M.getHidden ()Ljava/lang/String;']) // public
        assert !bytecode.hasSequence(['INVOKEVIRTUAL p/M.pm'])
        assert !bytecode.hasSequence(['INVOKEVIRTUAL p/M.qm'])
        assert !bytecode.hasSequence(['INVOKESTATIC p/M.spm'])
    }

    // GROOVY-12568
    @Test
    void testProtectedMethodsOfSuperclassOfAnotherClassLoaderAreCalledDirectly() {
        def bytecode = compileAgainstLibrary(MEMBERS, 'p.B', 'test', '''package p
            @groovy.transform.CompileStatic
            class B extends M {
                List test(B b) {
                    [qm(), this.qm(), super.qm(), b.qm(), sqm(), pm()]
                }
            }
        ''')
        assert bytecode.hasSequence(['ALOAD 0', 'INVOKEVIRTUAL p/B.qm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['ALOAD 0', 'INVOKESPECIAL p/M.qm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['ALOAD 1', 'INVOKEVIRTUAL p/B.qm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['INVOKESTATIC p/M.sqm ()Ljava/lang/String;'])
        assert bytecode.hasSequence(['LDC "pm"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeMethodOnCurrent0'])
    }

    // GROOVY-12568
    @Test
    void testMethodReferencesToProtectedAndPackagePrivateMethodsOfClassOfAnotherClassLoader() {
        shell.evaluate MEMBERS
        assert shell.evaluate('''package p
            import java.util.function.*

            @groovy.transform.CompileStatic
            class C { // same package name as M, but another class loader
                static List test(M m) {
                    Supplier<String> bound = m::pm
                    Function<M, String> unbound = M::qm
                    Supplier<String> stat = M::spm
                    IntUnaryOperator prim = m::count
                    BiFunction<M, Integer, Integer> unboundPrim = M::count
                    [bound.get(), unbound.apply(m), stat.get(), prim.applyAsInt(1), unboundPrim.apply(m, 2)]
                }
            }
            @groovy.transform.CompileStatic
            class B extends M {
                List test() {
                    Supplier<String> pkg = this::pm, prot = this::qm, stat = M::sqm
                    [pkg.get(), prot.get(), stat.get()]
                }
            }
            C.test(new M()) + new B().test()
        ''') == ['pm', 'qm', 'spm', 2, 3, 'pm', 'qm', 'sqm']
    }

    // GROOVY-12568
    @Test
    void testMethodReferencesOfInterfaceToPackagePrivateMethodsOfClassOfAnotherClassLoader() {
        shell.evaluate MEMBERS
        assert shell.evaluate('''package p
            import java.util.function.*

            @groovy.transform.CompileStatic
            interface I { // same package name as M, but another class loader
                static Supplier<String> stat() { M::spm }
                default Function<M, String> unbound() { M::pm }
            }
            assert I.declaredMethods.findAll { it.name.startsWith('access$') }.size() == 2
            [I.stat().get(), new I() {}.unbound().apply(new M())]
        ''') == ['spm', 'pm']
    }

    // GROOVY-12568
    @Test
    void testMethodReferenceToPackagePrivateMethodOfSameCompilationUnitIsDirect() {
        def bytecode = compile(method: 'test', classNamePattern: 'p.C', MEMBERS + '''
            @groovy.transform.CompileStatic
            class C {
                static java.util.function.Supplier<String> test(M m) {
                    m::pm
                }
            }
        ''')
        assert bytecode.hasSequence(['// handle kind 0x5 : INVOKEVIRTUAL', 'p/M.pm()Ljava/lang/String;'])
        assert !bytecode.hasSequence(['p/C.access$M$pm$'])
    }

    // GROOVY-12568
    @Test
    void testMethodReferenceToPackagePrivateMethodOfClassOfAnotherClassLoaderIsSelectedAtRuntime() {
        def script = '''package p
            @groovy.transform.CompileStatic
            class C {
                static java.util.function.Supplier<String> test(M m) {
                    m::pm
                }
            }
        '''
        def bytecode = compileAgainstLibrary(MEMBERS, 'p.C', 'test', script)
        assert bytecode.hasSequence(['// handle kind 0x6 : INVOKESTATIC', 'p/C.access$M$pm$'])
        assert !bytecode.hasSequence(['p/M.pm()Ljava/lang/String;'])

        String method = bytecode.instructions.find { it.startsWith('p/C.access$M$pm$') }.with { it.substring(4, it.indexOf('(')) }
        def access = compileAgainstLibrary(MEMBERS, 'p.C', method, script)
        assert access.hasSequence(['LDC "pm"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeMethod0'])
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateFieldsOfClassOfAnotherClassLoader() {
        shell.evaluate FIELDS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class C { // same package name as F, but another class loader
                static List read(F f) {
                    [f.pf, f.qf, F.spf, F.sqf, f.@pf, f.@qf, f?.pf, [f]*.pf, { -> f.pf }(), f.with { it.qf }]
                }
                static List write(F f) {
                    f.pf = 'w'
                    f.qf = 'x'
                    F.spf = 'y'
                    f.@n = 5
                    f.n += 2
                    f.n++
                    int m = ++f.n
                    [f.pf, f.qf, F.spf, f.n, m]
                }
            }
            C.read(new F()) + C.write(new F())
        ''') == ['pf', 'qf', 'spf', 'sqf', 'pf', 'qf', 'pf', ['pf'], 'pf', 'qf', 'w', 'x', 'y', 9, 9]
    }

    // GROOVY-12568
    @Test
    void testInheritedProtectedAndPackagePrivateFieldsOfClassOfAnotherClassLoader() {
        shell.evaluate FIELDS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class B extends F {
                List read(F other) {
                    [pf, qf, this.pf, this.qf, super.pf, this.@pf, super.@pf, spf, sqf, { -> pf + qf }(), other.qf, other.pf]
                }
                List write(F other) {
                    pf = 'a'
                    this.@qf = 'b'
                    other.qf = 'c'
                    other.pf = 'd'
                    [pf, qf, other.qf, other.pf]
                }
            }
            class D extends F { // dynamic
                List read() {
                    [super.pf, super.@pf, this.@pf]
                }
                List write() {
                    super.pf = 'e'
                    def x = pf
                    this.@pf = 'f'
                    [x, pf]
                }
            }
            new B().read(new F()) + new B().write(new F()) + new D().read() + new D().write()
        ''') == ['pf', 'qf', 'pf', 'qf', 'pf', 'pf', 'pf', 'spf', 'sqf', 'pfqf', 'qf', 'pf', 'a', 'b', 'c', 'd', 'pf', 'pf', 'pf', 'e', 'f']
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateFieldsOfSameCompilationUnitAreAccessedDirectly() {
        def bytecode = compile(method: 'test', classNamePattern: 'p.C', FIELDS + '''
            @groovy.transform.CompileStatic
            class C {
                static List test(F f) {
                    f.@pf = 'w'
                    [f.pf, f.qf, F.spf]
                }
            }
        ''')
        assert bytecode.hasSequence(['PUTFIELD p/F.pf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETFIELD p/F.pf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETFIELD p/F.qf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETSTATIC p/F.spf : Ljava/lang/String;'])
        assert !bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/InvokerHelper.getProperty'])
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateFieldsOfClassOfAnotherClassLoaderAreAccessedAtRuntime() {
        def bytecode = compileAgainstLibrary(FIELDS, 'p.C', 'test', '''package p
            @groovy.transform.CompileStatic
            class C {
                static List test(F f) {
                    f.@pf = 'w'
                    [f.pf, f.qf, F.spf]
                }
            }
        ''')
        assert bytecode.hasSequence(['LDC "pf"', 'INVOKESTATIC org/codehaus/groovy/runtime/InvokerHelper.getProperty'])
        assert bytecode.hasSequence(['LDC "qf"', 'INVOKESTATIC org/codehaus/groovy/runtime/InvokerHelper.getProperty'])
        assert bytecode.hasSequence(['LDC "spf"', 'INVOKESTATIC org/codehaus/groovy/runtime/InvokerHelper.getProperty'])
        assert bytecode.hasSequence(['LDC "pf"', 'CHECKCAST java/lang/String', 'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setGroovyObjectField'])
        assert !bytecode.hasSequence(['GETFIELD p/F'])
        assert !bytecode.hasSequence(['PUTFIELD p/F'])
        assert !bytecode.hasSequence(['GETSTATIC p/F'])
    }

    // GROOVY-12568
    @Test
    void testProtectedFieldOfSuperclassOfAnotherClassLoaderIsAccessedDirectly() {
        def bytecode = compileAgainstLibrary(FIELDS, 'p.B', 'test', '''package p
            @groovy.transform.CompileStatic
            class B extends F {
                List test() {
                    this.@qf = 'x'
                    [qf, sqf, pf]
                }
            }
        ''')
        assert bytecode.hasSequence(['PUTFIELD p/F.qf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETFIELD p/F.qf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETSTATIC p/B.sqf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['LDC "pf"', 'INVOKESTATIC org/codehaus/groovy/runtime/InvokerHelper.getProperty'])
    }

    // GROOVY-12568
    @Test
    void testMembersOfClassOfAnotherClassLoaderWithoutInvokeDynamic() {
        def config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.INVOKEDYNAMIC] = false
        def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        shell.evaluate LIBRARY
        shell.evaluate MEMBERS
        shell.evaluate FIELDS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class C {
                static List test(M m, F f) {
                    java.util.function.Supplier<String> ref = m::pm
                    f.pf = 'w'
                    [new A(1).value, m.pm(), M.spm(), m.qp, ref.get(), f.pf, F.spf, dynamic()]
                }
                static Object dynamic() {
                    'dynamic'.toUpperCase().with { it }
                }
            }
            C.test(new M(), new F())
        ''') == ['pkg:1', 'pm', 'spm', 'qp', 'pm', 'w', 'spf', 'DYNAMIC']

        shell.evaluate HIDDEN
        assert shell.evaluate(usesOfHidden('S', '@groovy.transform.CompileStatic')) == USES_OF_HIDDEN
    }

    // GROOVY-12568
    @Test
    void testProtectedAndPackagePrivateMembersOfClassCompiledBeforeToClassFilesAreAccessedDirectly() {
        def dir = File.createTempDir()
        try {
            compileToDirectory(dir, 'Library.groovy': LIBRARY + MEMBERS.replace('package p', '') + FIELDS.replace('package p', ''))
            compileToDirectory(dir, 'C.groovy': '''package p
                @groovy.transform.CompileStatic
                class C { // compiled by another compilation unit, to class files like groovyc
                    static List test(M m, F f) {
                        f.@pf = 'w'
                        [new A(1).value, m.pm(), m.qm(), M.spm(), f.pf, F.spf]
                    }
                    static java.util.function.Function<Integer, A> ctorRef() {
                        A::new
                    }
                    static java.util.function.Supplier<String> methodRef(M m) {
                        m::pm
                    }
                }
            ''')

            def bytecode = extractMethodSequence(new File(dir, 'p/C.class').bytes, 'test')
            assert bytecode.hasSequence(['INVOKESPECIAL p/A.<init> (Ljava/lang/Integer;)V'])
            assert bytecode.hasSequence(['INVOKEVIRTUAL p/M.pm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['INVOKEVIRTUAL p/M.qm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['INVOKESTATIC p/M.spm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['PUTFIELD p/F.pf : Ljava/lang/String;'])
            assert bytecode.hasSequence(['GETFIELD p/F.pf : Ljava/lang/String;'])
            assert bytecode.hasSequence(['GETSTATIC p/F.spf : Ljava/lang/String;'])
            assert !bytecode.hasSequence(['ScriptBytecodeAdapter.invoke'])
            assert !bytecode.hasSequence(['InvokerHelper.getProperty'])
            assert extractMethodSequence(new File(dir, 'p/C.class').bytes, 'ctorRef$ctorRef$0').hasSequence(['NEW p/A', 'DUP', 'ALOAD 0', 'INVOKESPECIAL p/A.<init> (Ljava/lang/Integer;)V'])
            assert extractMethodSequence(new File(dir, 'p/C.class').bytes, 'methodRef').hasSequence(['// handle kind 0x5 : INVOKEVIRTUAL', 'p/M.pm()Ljava/lang/String;'])

            // one class loader for all the class files, as javac expects
            def loader = new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader)
            def m = loader.loadClass('p.M').getDeclaredConstructor().newInstance()
            def f = loader.loadClass('p.F').getDeclaredConstructor().newInstance()
            def c = loader.loadClass('p.C')
            assert c.test(m, f) == ['pkg:1', 'pm', 'qm', 'spm', 'w', 'spf']
            assert c.ctorRef().apply(2).value == 'pkg:2'
            assert c.methodRef(m).get() == 'pm'
        } finally {
            dir.deleteDir()
        }
    }

    // GROOVY-12568
    @Test
    void testFullAndIncrementalCompilationToClassFilesGiveTheSameBytecode() {
        def caller = '''package p
            @groovy.transform.CompileStatic
            class C {
                static List test(M m, F f) {
                    java.util.function.Supplier<String> ref = m::pm
                    [new A(1).value, m.pm(), M.spm(), f.pf, F.spf, ref.get()]
                }
            }
        '''
        def library = LIBRARY + MEMBERS.replace('package p', '') + FIELDS.replace('package p', '')
        def full = File.createTempDir(), incremental = File.createTempDir()
        try {
            compileToDirectory(full, 'Library.groovy': library, 'C.groovy': caller)
            compileToDirectory(incremental, 'Library.groovy': library)
            compileToDirectory(incremental, 'C.groovy': caller) // C is recompiled alone

            assert new File(incremental, 'p/C.class').bytes == new File(full, 'p/C.class').bytes
        } finally {
            full.deleteDir()
            incremental.deleteDir()
        }
    }

    // GROOVY-12568
    @Test
    void testPackagePrivateMembersOfJavaClassOfJointCompilationAreAccessedDirectly() {
        def dir = File.createTempDir(), src = File.createTempDir()
        try {
            new File(src, 'p').mkdirs()
            def j = new File(src, 'p/J.java')
            j.text = '''package p;
                public class J {
                    String pf = "pf";
                    J() { }
                    String pm() { return "pm"; }
                    protected String qm() { return "qm"; }
                    static String spm() { return "spm"; }
                }
            '''
            def g = new File(src, 'p/G.groovy')
            g.text = '''package p
                @groovy.transform.CompileStatic
                class G {
                    static List test() {
                        J j = new J()
                        [j.pm(), j.qm(), J.spm(), j.pf]
                    }
                }
            '''
            def config = new CompilerConfiguration(targetDirectory: dir, jointCompilationOptions: [memStub: true])
            def unit = new JavaAwareCompilationUnit(config, new GroovyClassLoader(this.class.classLoader))
            unit.addSources(j, g)
            unit.compile()

            def bytecode = extractMethodSequence(new File(dir, 'p/G.class').bytes, 'test')
            assert bytecode.hasSequence(['NEW p/J', 'DUP', 'INVOKESPECIAL p/J.<init> ()V'])
            assert bytecode.hasSequence(['INVOKEVIRTUAL p/J.pm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['INVOKEVIRTUAL p/J.qm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['INVOKESTATIC p/J.spm ()Ljava/lang/String;'])
            assert bytecode.hasSequence(['GETFIELD p/J.pf : Ljava/lang/String;'])
            assert !bytecode.hasSequence(['ScriptBytecodeAdapter.invoke'])

            def loader = new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader)
            assert loader.loadClass('p.G').test() == ['pm', 'qm', 'spm', 'pf']
        } finally {
            dir.deleteDir()
            src.deleteDir()
        }
    }

    // GROOVY-12569
    @Test
    void testMapStyleConstructionOfPackagePrivateClassOfAnotherClassLoader() {
        shell.evaluate HIDDEN
        ['D': '', 'S': '@groovy.transform.CompileStatic'].each { name, annotation ->
            assert shell.evaluate("""package p
                $annotation
                class $name {
                    static List test(Map map) {
                        [new Hidden(name: 'x').name, new Hidden(map).name]
                    }
                }
                ${name}.test(name: 'y')
            """) == ['x', 'y']
        }
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassOfAnotherClassLoaderIsUsedAsInDynamicCode() {
        shell.evaluate HIDDEN
        assert shell.evaluate(usesOfHidden('D', '')) == USES_OF_HIDDEN
        assert shell.evaluate(usesOfHidden('S', '@groovy.transform.CompileStatic')) == USES_OF_HIDDEN
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassOfLibraryIsUsedByScriptAsInDynamicCode() {
        def unit = new CompilationUnit()
        unit.addSource('Library.groovy', HIDDEN)
        unit.compile(Phases.CLASS_GENERATION)
        def libraryLoader = new GroovyClassLoader()
        unit.classes.each { libraryLoader.defineClass(it.name, it.bytes) }

        def shell = new GroovyShell(libraryLoader)
        assert shell.evaluate(usesOfHidden('D', '')) == USES_OF_HIDDEN
        assert shell.evaluate(usesOfHidden('S', '@groovy.transform.CompileStatic')) == USES_OF_HIDDEN
    }

    // GROOVY-12569
    @Test
    void testUsesThatNamePackagePrivateClassOfAnotherClassLoaderFailAsInDynamicCode() {
        shell.evaluate HIDDEN
        ['', '@groovy.transform.CompileStatic'].each { annotation ->
            ['Hidden h = new Hidden()', 'Object o = new Hidden(); ((Hidden) o).own()', 'Object o = new Hidden(); o instanceof Hidden'].each { code ->
                def err = shouldFail(IllegalAccessError) {
                    shell.evaluate("package p\n$annotation def f() { $code }\nf()")
                }
                assert err.message.contains('failed to access class p.Hidden') // JVMS 5.4.4
            }
        }
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassOfAnotherClassLoaderIsNotNamedInBytecode() {
        def bytecode = compileAgainstLibrary(HIDDEN, 'p.C', 'test', usesOfHidden('C', '@groovy.transform.CompileStatic'))
        for (instruction in ['NEW', 'CHECKCAST', 'INSTANCEOF', 'INVOKEVIRTUAL', 'INVOKESTATIC', 'INVOKESPECIAL', 'INVOKEINTERFACE', 'GETFIELD', 'PUTFIELD', 'GETSTATIC', 'PUTSTATIC']) {
            assert !bytecode.hasSequence([instruction + ' p/Hidden'])
        }
        assert bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.invokeNew0'])
        assert bytecode.hasSequence(['INVOKEINTERFACE java/lang/Runnable.run ()V'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL p/Base.base ()Ljava/lang/String;'])
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassesOfAnotherClassLoaderAreUsedThroughPublicClassesAsInDynamicCode() {
        shell.evaluate SHUT
        assert shell.evaluate(usesOfShut('D', '')) == USES_OF_SHUT
        assert shell.evaluate(usesOfShut('S', '@groovy.transform.CompileStatic')) == USES_OF_SHUT
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassesOfAnotherClassLoaderAreNotNamedInBytecodeOfUsesThroughPublicClasses() {
        def bytecode = compileAgainstLibrary(SHUT, 'p.C', 'test', usesOfShut('C', '@groovy.transform.CompileStatic'))
        for (instruction in ['NEW', 'CHECKCAST', 'INSTANCEOF', 'INVOKEVIRTUAL', 'INVOKESTATIC', 'INVOKESPECIAL', 'INVOKEINTERFACE', 'GETFIELD', 'PUTFIELD', 'GETSTATIC', 'PUTSTATIC']) {
            for (type in ['Shut', 'Plain', 'Secret']) {
                assert !bytecode.hasSequence([instruction + ' p/' + type])
            }
        }
        assert bytecode.hasSequence(['PUTSTATIC p/Open.SF : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETSTATIC p/Open.SF : Ljava/lang/String;'])
        assert bytecode.hasSequence(['PUTFIELD p/Open.pf : Ljava/lang/String;'])
        assert bytecode.hasSequence(['GETFIELD p/Open.n : I', 'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL p/Open.take (Ljava/lang/Object;)Ljava/lang/String;'])
        assert bytecode.hasSequence(['INVOKESTATIC p/Open.make ()Lp/Shut;'])
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateJavaClassOfAnotherClassLoaderWithInstanceMethodNamedAsStaticMethodOfInterface() {
        def dir = File.createTempDir()
        try {
            def source = new File(dir, 'p/Q.java')
            source.parentFile.mkdirs()
            source.text = '''package p;
                class Q implements java.util.function.Function<Object, String> {
                    public String apply(Object o) { return "apply"; }
                    public Q identity() { return this; } // Function.identity() is static, so Q does not inherit it
                }
            '''
            assert ToolProvider.systemJavaCompiler.run(null, null, null, source.path) == 0

            def shell = new GroovyShell(new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader))
            ['', '@groovy.transform.CompileStatic'].each { annotation ->
                assert shell.evaluate("""package p
                    $annotation
                    def test() {
                        def q = new Q()
                        [q.identity().is(q), q.apply(null)]
                    }
                    test()
                """) == [true, 'apply']
            }
        } finally {
            dir.deleteDir()
        }
    }

    // GROOVY-12569
    @Test
    void testPackagePrivateClassOfClassFilesOrSameCompilationUnitIsNamedInBytecode() {
        def script = usesOfHidden('C', '@groovy.transform.CompileStatic')
        def sameUnit = compile(method: 'test', classNamePattern: 'p.C', HIDDEN + script.replace('package p', ''))
        assert sameUnit.hasSequence(['NEW p/Hidden', 'DUP', 'INVOKESPECIAL p/Hidden.<init> ()V'])
        assert sameUnit.hasSequence(['INVOKESTATIC p/Hidden.sm (Ljava/lang/String;)Ljava/lang/String;'])
        assert sameUnit.hasSequence(['INVOKEVIRTUAL p/Hidden.own ()Ljava/lang/String;'])

        def dir = File.createTempDir()
        try {
            compileToDirectory(dir, 'Library.groovy': HIDDEN)
            compileToDirectory(dir, 'C.groovy': script.substring(0, script.lastIndexOf('C.test()')))
            def classFiles = extractMethodSequence(new File(dir, 'p/C.class').bytes, 'test')
            assert classFiles.hasSequence(['NEW p/Hidden', 'DUP', 'INVOKESPECIAL p/Hidden.<init> ()V'])
            assert classFiles.hasSequence(['INVOKESTATIC p/Hidden.sm (Ljava/lang/String;)Ljava/lang/String;'])
            assert classFiles.hasSequence(['INVOKEVIRTUAL p/Hidden.own ()Ljava/lang/String;'])

            def loader = new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader)
            assert loader.loadClass('p.C').test() == USES_OF_HIDDEN
        } finally {
            dir.deleteDir()
        }
    }

    /**
     * Compiles the sources to class files in {@code dir}, against the class
     * files already there, as groovyc does.
     */
    private static void compileToDirectory(final Map<String, String> sources, final File dir) {
        def config = new CompilerConfiguration(targetDirectory: dir, classpathList: [dir.path])
        def unit = new CompilationUnit(config, null, new GroovyClassLoader(StaticCompileRuntimePackageTest.classLoader, config))
        sources.each { name, text -> unit.addSource(name, text) }
        unit.compile(Phases.OUTPUT)
    }
}
