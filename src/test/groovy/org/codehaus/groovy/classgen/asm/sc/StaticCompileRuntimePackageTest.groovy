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
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for {@link CompilerConfiguration#RUNTIME_PACKAGE_CHECK}: protected
 * and package-private members of a class with the same package name, compiled
 * by another compilation and so maybe defined by another class loader.
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

    private static final String SC = '@groovy.transform.CompileStatic'

    /** Compiles {@code body} as a method of a class C, with the options shell, clause and annotation, and runs it. */
    private Object evaluate(final Map options = [:], final String body) {
        (options.shell ?: shell).evaluate """package p
            ${options.containsKey('annotation') ? options.annotation : SC}
            class C ${options.clause ?: ''} {
                def test() { $body }
            }
            new C().test()
        """
    }

    private void assertReported(final Map options = [:], final String member, final String body) {
        def err = shouldFail(MultipleCompilationErrorsException) {
            evaluate(options, body)
        }
        assert err.message.contains(member) : body
        assert err.message.contains('is defined by another class loader, so the JVM refuses this access') : body
        assert err.message.contains('Use @CompileDynamic to reach it through the meta-class') : body
    }

    private static void assertRefusedAtRunTime(final Closure<?> run) {
        def err = shouldFail(IncompatibleClassChangeError, run)
        assert err instanceof IllegalAccessError
    }

    // GROOVY-12568
    @Test
    void testMessageOfAccessToAnotherRuntimePackage() {
        shell.evaluate MEMBERS
        def err = shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate '''package p
                @groovy.transform.CompileStatic
                class C {
                    def test() {
                        new M().pm()
                    }
                }
            '''
        }
        assert err.message.contains('Cannot call package-private method pm of p.M from class p.C: ' +
            'M is defined by another class loader, so the JVM refuses this access. ' +
            'Use @CompileDynamic to reach it through the meta-class @ line 5, column 25.')
    }

    // GROOVY-12568
    @Test
    void testConstructorsOfAnotherRuntimePackage() {
        shell.evaluate LIBRARY
        [
            "new A('x')"                                                                         : 'protected constructor of p.A',
            'new A(1)'                                                                           : 'package-private constructor of p.A',
            'new A()'                                                                            : 'package-private constructor of p.A',
            "new A(name: 'n')"                                                                   : 'package-private constructor of p.A',
            '[1, 2].collect { Integer i -> new A(i) }'                                           : 'package-private constructor of p.A',
            "java.util.function.Supplier<A> s = () -> new A('y'); s.get()"                       : 'protected constructor of p.A',
        ].each { body, member -> assertReported(member, body) }
    }

    // GROOVY-12568
    @Test
    void testSuperConstructorsOfAnotherRuntimePackage() {
        shell.evaluate LIBRARY
        [
            'class D extends A { D() { super(1) } }',
            'class D extends A { D() { super() } }',
            'class D extends A { D() { } }', // implicit super()
            'class D extends A { }', // generated constructor
        ].each { type ->
            def err = shouldFail(MultipleCompilationErrorsException) {
                shell.evaluate "package p\n$SC\n$type"
            }
            assert err.message.contains('Cannot call package-private constructor of p.A from class p.D') : type
        }
        assertReported('package-private constructor of p.A', 'new A(1) { }')
        assertReported('package-private constructor of p.A', 'new A() { }')
        assertReported('package-private constructor of p.A', 'int i = 1; new A(i) { int shared() { i } }')

        // a subclass can call a protected constructor with super(...)
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class D extends A { D() { super('x') } }
            new D().value
        ''') == 'prot:x'
        assert evaluate("new A('y') { }.value") == 'prot:y'
        assert evaluate("String s = 'z'; new A(s) { String shared() { s } }.value") == 'prot:z'
    }

    // GROOVY-12568
    @Test
    void testMethodsOfAnotherRuntimePackage() {
        shell.evaluate MEMBERS
        [
            'new M().pm()'                                                         : 'package-private method pm of p.M',
            'new M().qm()'                                                         : 'protected method qm of p.M',
            'M.spm()'                                                              : 'package-private method spm of p.M',
            'M.sqm()'                                                              : 'protected method sqm of p.M',
            'new M().count(1)'                                                     : 'package-private method count of p.M',
            'new M().pp'                                                           : 'package-private method getPp of p.M',
            'new M().qp'                                                           : 'protected method getQp of p.M',
            "new M().qs = 'x'"                                                     : 'protected method setQs of p.M',
            '[1].collect { new M().pm() }'                                         : 'package-private method pm of p.M',
            'java.util.function.Supplier<String> s = () -> new M().pm(); s.get()'  : 'package-private method pm of p.M',
            'new M().with { pm() }'                                                : 'package-private method pm of p.M',
        ].each { body, member -> assertReported(member, body) }
    }

    // GROOVY-12568
    @Test
    void testInheritedMethodsOfAnotherRuntimePackage() {
        shell.evaluate MEMBERS
        [
            'pm()'                 : 'package-private method pm of p.M',
            'this.pm()'            : 'package-private method pm of p.M',
            'super.pm()'           : 'package-private method pm of p.M',
            'spm()'                : 'package-private method spm of p.M',
            'pp'                   : 'package-private method getPp of p.M',
            'super.pp'             : 'package-private method getPp of p.M',
            'new M().qm()'         : 'protected method qm of p.M', // not through a receiver of the subclass
            '[1].collect { pm() }' : 'package-private method pm of p.M',
            '[1].collect { qm() }' : 'protected method qm of p.M', // the closure class is not a subclass
        ].each { body, member -> assertReported(clause: 'extends M', member, body) }

        // the subclass rule
        [
            'qm()'       : 'qm',
            'this.qm()'  : 'qm',
            'super.qm()' : 'qm',
            'sqm()'      : 'sqm',
            'qp'         : 'qp',
            'super.qp'   : 'qp',
            'new C().qm()': 'qm',
        ].each { body, value -> assert evaluate(clause: 'extends M', body) == value : body }
    }

    // GROOVY-12568
    @Test
    void testFieldsOfAnotherRuntimePackage() {
        shell.evaluate FIELDS
        [
            'new F().pf'                     : 'package-private field pf of p.F',
            'new F().qf'                     : 'protected field qf of p.F',
            'new F().@pf'                    : 'package-private field pf of p.F',
            "new F().@pf = 'w'"              : 'package-private field pf of p.F',
            "new F().@qf = 'w'"              : 'protected field qf of p.F',
            'new F().n++'                    : 'package-private field n of p.F',
            'new F().n += 1'                 : 'package-private field n of p.F',
            'F.spf'                          : 'package-private field spf of p.F',
            'F.sqf'                          : 'protected field sqf of p.F',
            '[1].collect { new F().pf }'     : 'package-private field pf of p.F',
        ].each { body, member -> assertReported(member, body) }
    }

    // GROOVY-12568
    @Test
    void testInheritedFieldsOfAnotherRuntimePackage() {
        shell.evaluate FIELDS
        [
            'pf'             : 'package-private field pf of p.F',
            'this.pf'        : 'package-private field pf of p.F',
            'super.pf'       : 'package-private field pf of p.F',
            'this.@pf'       : 'package-private field pf of p.F',
            'super.@pf'      : 'package-private field pf of p.F',
            "this.@pf = 'w'" : 'package-private field pf of p.F',
            "super.pf = 'w'" : 'package-private field pf of p.F',
            'spf'            : 'package-private field spf of p.F',
            'n++'            : 'package-private field n of p.F',
            'new F().qf'     : 'protected field qf of p.F', // not through a receiver of the subclass
        ].each { body, member -> assertReported(clause: 'extends F', member, body) }

        // the subclass rule
        [
            'qf'           : 'qf',
            'this.qf'      : 'qf',
            'super.qf'     : 'qf',
            'this.@qf'     : 'qf',
            'super.@qf'    : 'qf',
            'sqf'          : 'sqf',
            'new C().qf'   : 'qf',
            'new C().@qf'  : 'qf',
        ].each { body, value -> assert evaluate(clause: 'extends F', body) == value : body }
    }

    // GROOVY-12568
    @Test
    void testMethodReferencesOfAnotherRuntimePackage() {
        shell.evaluate MEMBERS
        [
            'java.util.function.Supplier<String> s = new M()::pm; s.get()'        : 'package-private method pm of p.M',
            'java.util.function.Supplier<String> s = new M()::qm; s.get()'        : 'protected method qm of p.M',
            'java.util.function.Function<M, String> f = M::pm; f.apply(new M())'  : 'package-private method pm of p.M',
            'java.util.function.Supplier<String> s = M::spm; s.get()'             : 'package-private method spm of p.M',
        ].each { body, member -> assertReported(member, body) }

        assertReported(clause: 'extends M', 'package-private method pm of p.M', 'java.util.function.Supplier<String> s = this::pm; s.get()')
        assert evaluate(clause: 'extends M', 'java.util.function.Supplier<String> s = this::qm; s.get()') == 'qm'
    }

    // GROOVY-12568: a public member of such a class, and a constructor reference, are not refused
    @Test
    void testWhatTheCheckDoesNotReport() {
        shell.evaluate LIBRARY
        shell.evaluate '''package p
            class B {
                public String pf = 'pf'
                String pm() { 'pm' }
            }
            true
        '''
        assert evaluate('[new B().pf, new B().pm()]') == ['pf', 'pm']
        assert evaluate('java.util.function.Function<Integer, A> f = A::new; f.apply(2).value') == 'pkg:2'
        assert evaluate("java.util.function.Function<String, A> f = A::new; f.apply('y').value") == 'prot:y'

        // another package name: the language rules decide, not the run-time package
        shell.evaluate MEMBERS
        assert shell.evaluate('''package q
            @groovy.transform.CompileStatic
            class C extends p.M {
                def test() { qm() }
            }
            new C().test()
        ''') == 'qm'
    }

    // GROOVY-12568
    @Test
    void testDynamicAndTypeCheckedCodeUsesTheMetaClass() {
        shell.evaluate LIBRARY
        shell.evaluate MEMBERS
        shell.evaluate FIELDS
        ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic(groovy.transform.TypeCheckingMode.SKIP)'].each { annotation ->
            assert evaluate(annotation: annotation, 'new A(1).value') == 'pkg:1'
            assert evaluate(annotation: annotation, 'new M().pm()') == 'pm'
            assert evaluate(annotation: annotation, 'M.spm()') == 'spm'
            assert evaluate(annotation: annotation, 'new F().pf') == 'pf'
            assert evaluate(annotation: annotation, 'new F().@pf') == 'pf'
        }
    }

    // GROOVY-12568
    @Test
    void testCompileDynamicReachesTheMember() {
        shell.evaluate MEMBERS
        assert shell.evaluate('''package p
            @groovy.transform.CompileStatic
            class C {
                @groovy.transform.CompileDynamic
                def test() { new M().pm() }
            }
            new C().test()
        ''') == 'pm'
    }

    // GROOVY-12568
    @Test
    void testPrecompiledClassUsedByScript() {
        def unit = new CompilationUnit()
        unit.addSource('Library.groovy', LIBRARY)
        unit.compile(Phases.CLASS_GENERATION)
        def libraryLoader = new GroovyClassLoader()
        unit.classes.each { libraryLoader.defineClass(it.name, it.bytes) }

        def shell = new GroovyShell(libraryLoader, new CompilerConfiguration())
        def err = shouldFail(MultipleCompilationErrorsException) {
            evaluate(shell: shell, 'new A(1)')
        }
        assert err.message.contains('Cannot call package-private constructor of p.A from class p.C')
    }

    // GROOVY-12568
    @Test
    void testMembersOfTheSameScriptAreNotReported() {
        assert shell.evaluate('''package p
            class M {
                @groovy.transform.PackageScope String pm() { 'pm' }
                @groovy.transform.PackageScope String pf = 'pf'
            }
            @groovy.transform.CompileStatic
            class C {
                def test() { [new M().pm(), new M().pf] }
            }
            new C().test()
        ''') == ['pm', 'pf']
    }

    // GROOVY-12568: the option decides over the default of a shell, and no bytecode changes without the check
    @Test
    void testOptionOffGivesTheFailureAtRunTime() {
        def config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.RUNTIME_PACKAGE_CHECK] = false
        def shell = new GroovyShell(config)
        shell.evaluate LIBRARY
        shell.evaluate MEMBERS
        shell.evaluate FIELDS

        assertRefusedAtRunTime { evaluate(shell: shell, 'new A(1)') }
        assertRefusedAtRunTime { evaluate(shell: shell, 'new M().pm()') }
        assertRefusedAtRunTime { evaluate(shell: shell, 'new F().pf') }
        assertRefusedAtRunTime { evaluate(shell: shell, "new F().@pf = 'w'") }
    }

    // GROOVY-12568
    @Test
    void testShellDoesNotChangeTheConfiguration() {
        def config = new CompilerConfiguration()
        def shell = new GroovyShell(config)
        assert !config.optimizationOptions.containsKey(CompilerConfiguration.RUNTIME_PACKAGE_CHECK)
        assert !CompilerConfiguration.DEFAULT.optimizationOptions.containsKey(CompilerConfiguration.RUNTIME_PACKAGE_CHECK)

        shell.evaluate MEMBERS
        assertReportedBy(shell, 'new M().pm()')
    }

    // GROOVY-12568: a configuration change after the shell is created still applies
    @Test
    void testOptionSetAfterTheShellIsCreated() {
        def config = new CompilerConfiguration()
        def shell = new GroovyShell(config)
        shell.evaluate MEMBERS
        config.optimizationOptions[CompilerConfiguration.RUNTIME_PACKAGE_CHECK] = false
        assertRefusedAtRunTime { evaluate(shell: shell, 'new M().pm()') }
    }

    // GROOVY-12568: off for a class loader that is not created by a shell, unless the option is set
    @Test
    void testGroovyClassLoader() {
        def compile = { CompilerConfiguration config, boolean viaShell ->
            def loader = new GroovyClassLoader(this.class.classLoader, config)
            def members = loader.parseClass(MEMBERS.replace('true', ''))
            def caller = '''package p
                @groovy.transform.CompileStatic
                class C {
                    def test() { new M().pm() }
                }
            '''
            viaShell ? new GroovyShell(loader, config).parse(caller + 'new C().test()').run() : loader.parseClass(caller).getDeclaredConstructor().newInstance().test()
        }

        def config = new CompilerConfiguration()
        assertRefusedAtRunTime { compile(config, false) }
        assertRefusedAtRunTime { compile(config, true) } // the shell did not create this class loader

        config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.RUNTIME_PACKAGE_CHECK] = true
        def err = shouldFail(MultipleCompilationErrorsException) { compile(config, false) }
        assert err.message.contains('Cannot call package-private method pm of p.M from class p.C')
    }

    // GROOVY-12568
    @Test
    void testSystemProperty() {
        System.setProperty('groovy.runtime.package.check', 'false')
        try {
            def config = new CompilerConfiguration()
            assert config.optimizationOptions[CompilerConfiguration.RUNTIME_PACKAGE_CHECK] == Boolean.FALSE
        } finally {
            System.clearProperty('groovy.runtime.package.check')
        }
        assert !new CompilerConfiguration().optimizationOptions.containsKey(CompilerConfiguration.RUNTIME_PACKAGE_CHECK)
    }

    private void assertReportedBy(final GroovyShell shell, final String body) {
        def err = shouldFail(MultipleCompilationErrorsException) {
            evaluate(shell: shell, body)
        }
        assert err.message.contains('is defined by another class loader')
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

    /**
     * Compiles the sources to class files in {@code dir}, against the class
     * files already there, as groovyc does. The check has no effect on them.
     */
    private static void compileToDirectory(final Map<String, String> sources, final File dir) {
        def config = new CompilerConfiguration(targetDirectory: dir, classpathList: [dir.path])
        config.optimizationOptions[CompilerConfiguration.RUNTIME_PACKAGE_CHECK] = true
        def unit = new CompilationUnit(config, null, new GroovyClassLoader(StaticCompileRuntimePackageTest.classLoader, config))
        sources.each { name, text -> unit.addSource(name, text) }
        unit.compile(Phases.OUTPUT)
    }
}
