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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.InstructionSequence
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import java.security.CodeSource

import static groovy.test.GroovyAssert.shouldFail

/**
 * A package-private field of a superclass with the same package name but of
 * another class loader, as a class of an earlier parse of the same GroovyShell
 * is: the JVM refuses direct access, so dynamic and {@code @TypeChecked} code
 * use it through the meta-class, as they do for a plain reference to the field.
 */
final class Groovy12718 extends AbstractBytecodeTestCase {

    private final GroovyShell shell = new GroovyShell()

    private static final String FIELDS = '''package p
        class F {
            @groovy.transform.PackageScope String pf = 'pf'
            @groovy.transform.PackageScope static String spf = 'spf'
            protected String qf = 'qf'
        }
        true
    '''

    private Object evaluate(final String annotation, final String body) {
        shell.evaluate """package p
            $annotation
            class C extends F {
                def test() { $body }
            }
            new C().test()
        """
    }

    /**
     * Compiles the sources one after another with a GroovyClassLoader, which
     * defines the classes of each compilation with a class loader of their own,
     * and returns the instructions of method test of class p.C.
     */
    private InstructionSequence compileInMemory(final String... sources) {
        CompilationUnit unit = null
        def loader = new GroovyClassLoader(this.class.classLoader) {
            @Override
            protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
                unit = super.createCompilationUnit(config, source)
            }
        }
        sources.each { loader.parseClass(it) }
        extractMethodSequence(unit.classes.find { it.name == 'p.C' }.bytes, 'test')
    }

    // GROOVY-12718
    @ParameterizedTest(name = '{0}')
    @ValueSource(strings = ['', '@groovy.transform.TypeChecked'])
    void testReadOfPackagePrivateFieldOfSuperclass(final String annotation) {
        shell.evaluate FIELDS
        assert evaluate(annotation, 'super.pf') == 'pf'
        assert evaluate(annotation, 'super.@pf') == 'pf'
        assert evaluate(annotation, 'this.@pf') == 'pf'
        assert evaluate(annotation, 'super.spf') == 'spf'
        assert evaluate(annotation, 'this.@spf') == 'spf'
    }

    // GROOVY-12718
    @ParameterizedTest(name = '{0}')
    @ValueSource(strings = ['', '@groovy.transform.TypeChecked'])
    void testWriteOfPackagePrivateFieldOfSuperclass(final String annotation) {
        shell.evaluate FIELDS
        assert evaluate(annotation, "super.pf = 'w'; pf") == 'w'
        assert evaluate(annotation, "super.@pf = 'w'; pf") == 'w'
        assert evaluate(annotation, "this.@pf = 'w'; pf") == 'w'
    }

    // the same field accessed from a closure in such a class
    @ParameterizedTest(name = '{0}')
    @ValueSource(strings = ['', '@groovy.transform.TypeChecked'])
    void testPackagePrivateFieldOfSuperclassFromClosure(final String annotation) {
        shell.evaluate FIELDS
        assert evaluate(annotation, '[1].collect { this.@pf }') == ['pf']
        assert evaluate(annotation, '[1].collect { super.pf }') == ['pf']
    }

    // a subclass can use a protected field of another run-time package directly
    @Test
    void testProtectedFieldOfSuperclassIsStillDirect() {
        def bytecode = compileInMemory(FIELDS, '''package p
            class C extends F {
                def test() { [super.qf, this.@qf, super.@qf] }
            }
        ''')
        assert bytecode.hasSequence(['GETFIELD p/F.qf : Ljava/lang/String;'])
    }

    // a field of the same compilation is in the same run-time package
    @Test
    void testPackagePrivateFieldOfSameCompilationIsStillDirect() {
        def bytecode = compileInMemory(FIELDS + '''
            class C extends F {
                def test() { [super.pf, this.@pf, super.@pf] }
            }
        ''')
        assert bytecode.hasSequence(['GETFIELD p/F.pf : Ljava/lang/String;'])
    }

    // static compilation does not select a member at run time
    @Test
    void testStaticCompilationIsUnchanged() {
        shell.evaluate FIELDS
        ['super.pf', 'super.@pf', 'this.@pf', "this.@pf = 'w'"].each { body ->
            def err = shouldFail(IllegalAccessError) {
                evaluate('@groovy.transform.CompileStatic', body)
            }
            assert err.message.contains('tried to access field p.F.pf')
        }
    }
}
