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
package org.codehaus.groovy.control.customizers

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.syntax.Types
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for {@link SecureASTCustomizer}.
 */
final class SecureASTCustomizerTest {

    private final CompilerConfiguration configuration = new CompilerConfiguration()
    private final SecureASTCustomizer customizer = new SecureASTCustomizer()

    @BeforeEach
    void setUp() {
        configuration.addCompilationCustomizers(customizer)
    }

    private static boolean hasSecurityException(Closure closure) {
        boolean result = false
        try {
            closure()
        } catch (SecurityException e) {
            result = true
        } catch (MultipleCompilationErrorsException e) {
            result = e.errorCollector.errors.any { it.cause?.class == SecurityException }
        }
        return result
    }

    @Test
    void testPackageDefinition() {
        String script = '''
            package dummy
            class A {
            }
            new A()
        '''
        def shell = new GroovyShell(configuration)
        shell.evaluate(script)
        // no error means success
        customizer.packageAllowed = false
        assert hasSecurityException {
            shell.evaluate(script)
        }
    }

    @Test
    void testMethodDefinition() {
        String script = '''
            def method() {
                true
            }
            method()
        '''
        def shell = new GroovyShell(configuration)
        shell.evaluate(script)
        // no error means success
        customizer.methodDefinitionAllowed = false
        assert hasSecurityException {
            shell.evaluate(script)
        }
    }

    @Test
    void testMethodDefinitionInClass() {
        String script = '''
            class A {
                def method() {
                    true
                }
            }
            new A()
        '''
        def shell = new GroovyShell(configuration)
        shell.evaluate(script)
        // no error means success
        customizer.methodDefinitionAllowed = false
        assert hasSecurityException {
            shell.evaluate(script)
        }
    }

    @Test
    void testClassExtendingClassWithMethods() {
        String script = '''
            class A extends LinkedList {
            }
            new A()
        '''
        def shell = new GroovyShell(configuration)
        shell.evaluate(script)
        // no error means success
        customizer.methodDefinitionAllowed = false
        shell.evaluate(script)
    }

    @Test
    void testAllowedExpressions() {
        customizer.allowedExpressions = [BinaryExpression, ConstantExpression]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1+1')
        assert hasSecurityException {
            shell.evaluate('''
                class A {}
                new A()
            ''')
        }
    }

    @Test
    void testDisallowedExpressions() {
        customizer.disallowedExpressions = [MethodCallExpression]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1+1')
        assert hasSecurityException {
            shell.evaluate('''
                1+1
                if (1+1==2) {
                    "test".length()
                }
            ''')
        }
    }

    @Test
    void testAllowedTokens() {
        customizer.allowedTokens = [Types.PLUS, Types.MINUS]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1+1;1-1')
        assert hasSecurityException {
            shell.evaluate('''
                if (i==2) println 'ok'
            ''')
        }
    }

    @Test
    void testDisallowedTokens() {
        customizer.disallowedTokens = [Types.PLUS_PLUS]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1+1;1-1')
        assert hasSecurityException {
            shell.evaluate('''
                i++
            ''')
        }
    }

    @Test
    void testAllowedImports() {
        customizer.allowedImports = ['java.util.ArrayList']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.LinkedList
                new LinkedList()
            ''')
        }
    }

    @Test
    void testAllowedStarImports1() {
        customizer.allowedStarImports = ['java.util.*']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.concurrent.atomic.AtomicInteger
                new AtomicInteger(0)
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.*
                import java.util.concurrent.atomic.*
                new ArrayList()
                new AtomicInteger(0)
            ''')
        }
    }

    @Test
    void testAllowedStarImports2() {
        customizer.allowedStarImports = ['java.**']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.lang.Object
            Object obj
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import javax.swing.Action
                Action act
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.*
                import javax.swing.*
                Object obj
                Action act
            ''')
        }
    }

    @Test
    void testAllowedStarImportsWithAllowedImports() {
        customizer.allowedImports = ['java.util.concurrent.atomic.AtomicInteger']
        customizer.allowedStarImports = ['java.util.*']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        shell.evaluate('''
            import java.util.concurrent.atomic.AtomicInteger
            new AtomicInteger(0)
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.concurrent.atomic.AtomicBoolean
                new AtomicBoolean(false)
            ''')
        }
    }

    @Test
    void testDisallowedImports() {
        customizer.disallowedImports = ['java.util.LinkedList']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.LinkedList
                new LinkedList()
            ''')
        }
    }

    @Test
    void testDisallowedStarImports1() {
        customizer.disallowedStarImports = ['java.lang.*']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.LinkedList
            import javax.swing.Action
            LinkedList list
            Action act
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.lang.Object
                Object obj
            ''')
        }
    }

    @Test
    void testDisallowedStarImports2() {
        customizer.disallowedStarImports = ['java.**']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import javax.swing.Action
            Action act
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.lang.Object
                Object obj
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.Deque
                Deque deck
            ''')
        }
    }

    @Test
    void testDisallowedStarImportsWithDisallowedImports() {
        customizer.disallowedImports = ['java.util.concurrent.atomic.AtomicBoolean']
        customizer.disallowedStarImports = ['java.util.*']
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.ArrayList
                new ArrayList()
            ''')
        }
        shell.evaluate('''
            import java.util.concurrent.atomic.AtomicInteger
            new AtomicInteger(0)
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import java.util.concurrent.atomic.AtomicBoolean
                new AtomicBoolean(false)
            ''')
        }
    }

    // GROOVY-12279: a method pointer's own type is fixed to groovy.lang.Closure, so the
    // indirect import check was asking about Closure rather than about the class the pointer
    // is taken on. In deny mode that let the pointer through; in allow mode it rejected every
    // pointer, since Closure is never in an allow list.
    @Test
    void testIndirectImportCheckUsesMethodPointerTargetWhenDenied() {
        customizer.disallowedImports = ['java.util.LinkedList']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate('return java.util.LinkedList.&size')
        }
        assert hasSecurityException {
            shell.evaluate('return java.util.LinkedList::size')
        }
        // The constructor form was already checked, and stays checked.
        assert hasSecurityException {
            shell.evaluate('return new java.util.LinkedList()')
        }
    }

    @Test
    void testIndirectImportCheckUsesMethodPointerTargetWhenAllowed() {
        customizer.allowedImports = ['java.util.ArrayList']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        // Permitted because the target is allowed. Previously refused, because the type being
        // asked about was Closure, which no allow list names.
        shell.evaluate('return java.util.ArrayList.&size')
        shell.evaluate('return java.util.ArrayList::size')
        // A target which is not allowed is still refused, in both pointer and reference form.
        assert hasSecurityException {
            shell.evaluate('return java.util.LinkedList.&size')
        }
        assert hasSecurityException {
            shell.evaluate('return java.util.LinkedList::size')
        }
    }

    @Test
    void testAllowedIndirectImports() {
        customizer.allowedImports = ['java.util.ArrayList']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                new java.util.LinkedList()
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                return java.util.LinkedList.&size
            ''')
        }
    }

    @Test
    void testAllowedIndirectStarImports1() {
        customizer.allowedStarImports = ['java.util.*']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import java.util.ArrayList
            new ArrayList()
        ''')
        shell.evaluate('''
            new java.util.ArrayList()
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                new java.util.concurrent.atomic.AtomicBoolean(false)
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                return java.util.concurrent.atomic.AtomicBoolean.&get
            ''')
        }
    }

    @Test // GROOVY-8135
    void testAllowedIndirectStarImports2() {
        customizer.allowedStarImports = ['java.lang']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate('Object object = new Object()')
        shell.evaluate('Object object = new Object(); object.hashCode()')
        shell.evaluate('Object[] array = new Object[0]; array.size()')
        shell.evaluate('Object[][] array = new Object[0][0]; array.size()')
    }

    @Test // GROOVY-10184
    void testAllowedIndirectStarImports3() {
        customizer.allowedStarImports = ['java.lang.*']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate('def obj = new Object(); def method = "hashCode"; obj."${method}"()')
    }

    @Test
    void testAllowedStaticImports() {
        customizer.allowedStaticImports = ['java.lang.Math.PI']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import static java.lang.Math.PI
            PI
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import static java.lang.Math.PI
                import static java.lang.Math.cos
                cos(PI)
            ''')
        }
    }

    @Test
    void testAllowedStaticStarImports1() {
        customizer.allowedStaticStarImports = ['java.lang.Math.*']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import static java.lang.Math.PI
            import static java.lang.Math.cos
            cos(PI)
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import static java.util.Collections.*
                sort([5,4,2])
            ''')
        }
    }

    @Test
    void testAllowedStaticStarImports2() {
        customizer.allowedStaticStarImports = ['java.lang.**']
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            import static java.lang.Math.PI
            import static java.lang.Math.cos
            cos(PI)
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import static java.util.Collections.*
                sort([5,4,2])
            ''')
        }
    }

    @Test
    void testDisallowedStaticStarImports1() {
        customizer.disallowedStaticStarImports = ['java.lang.**']
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate('''
                import static java.lang.Math.PI
                import static java.lang.Math.cos
                cos(PI)
            ''')
        }
        shell.evaluate('''
            import static java.util.Collections.*
            sort([5,4,2])
        ''')
    }

    @Test
    void testIndirectStaticImport() {
        customizer.allowedStaticImports = ['java.lang.Math.PI']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate('java.lang.Math.cos(1)')
        }
    }

    @Test
    void testIndirectStaticStarImport() {
        customizer.allowedStaticStarImports = ['java.lang.Math.*']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate('java.lang.Math.cos(1)')
        assert hasSecurityException {
            shell.evaluate('java.util.Collections.unmodifiableList([1])')
        }
    }

    @Test
    void testAllowedConstantTypes() {
        customizer.allowedConstantTypesClasses = [Integer.TYPE]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1')
        assert hasSecurityException {
            shell.evaluate('"string"')
        }
        assert hasSecurityException {
            shell.evaluate('2d')
        }
    }

    @Test
    void testDisallowedConstantTypes() {
        customizer.disallowedConstantTypesClasses = [String]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1')
        shell.evaluate('2d')
        assert hasSecurityException {
            shell.evaluate('"string"')
        }
    }

    @Test
    void testAllowedReceivers() {
        customizer.allowedReceiversClasses = [Integer.TYPE]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1.plus(1)')
        assert hasSecurityException {
            shell.evaluate('"string".toUpperCase()')
        }
        assert hasSecurityException {
            shell.evaluate('2.0.multiply(4)')
        }
    }

    @Test
    void testAllowedReceiversMethod() {
        customizer.allowedReceiversClasses = [Integer.TYPE]
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            static main(args) {
                1.plus(1)
            }
        ''')
        shell.run('''
            def main(args) {
                1.plus(1)
            }
        ''', 'dummyName')
        shell.evaluate('''
            def run() {
                1.plus(1)
            }
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                static main(args) {
                    "string".toUpperCase()
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                def main(args) {
                    "string".toUpperCase()
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                def run() {
                    "string".toUpperCase()
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                static main(args) {
                    2.0.multiply(4)
                }
            ''')
        }
    }

    @Test
    void testAllowedReceiversClass() {
        customizer.allowedReceiversClasses = [Integer.TYPE]
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            class Dummy {
                static main(args) {
                    assert 2 == 1.plus(1)
                }
            }
        ''')
        shell.run('''
            class Dummy {
                def main(args) {
                    assert 2 == 1.plus(1)
                }
            }
        ''', 'dummyName')
        assert hasSecurityException {
            shell.evaluate('''
                class Dummy {
                    static main(args) {
                        "string".toUpperCase()
                    }
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                class Dummy {
                    def main(args) {
                        "string".toUpperCase()
                    }
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                class Dummy {
                    def run() {
                        "string".toUpperCase()
                    }
                }
            ''')
        }
        assert hasSecurityException {
            shell.evaluate('''
                class Dummy {
                    static main(args) {
                        2.0.multiply(4)
                    }
                }
            ''')
        }
    }

    @Test
    void testDisallowedReceivers() {
        customizer.disallowedReceiversClasses = [String]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1.plus(1)')
        shell.evaluate('2.0.multiply(4)')
        assert hasSecurityException {
            shell.evaluate('"string".toUpperCase()')
        }
    }

    @Test
    void testDisallowedReceiversInvokerHelperEdgeCase() {
        assert 'a,b' == InvokerHelper.invokeStaticMethod(String, 'join', [',', ['a', 'b']] as Object[])
        customizer.disallowedReceiversClasses = [InvokerHelper]
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            def run() {
                assert 'a,b' == String.join(',', ['a', 'b'])
            }
        ''')
        shell.run('''
            def main() {
                assert 'a,b' == String.join(',', ['a', 'b'])
            }
        ''', 'dummyName')
        shell.evaluate('''
            static main(args) {
                assert 'a,b' == String.join(',', ['a', 'b'])
            }
        ''')
        assert hasSecurityException {
            shell.evaluate('''
                import org.codehaus.groovy.runtime.InvokerHelper
                InvokerHelper.invokeStaticMethod(String, 'join', [',', ['a', 'b']] as Object[])
            ''')
        }
    }

    @Test
    void testAllowedReceiversWithStaticMethod() {
        customizer.allowedReceiversClasses = [Integer.TYPE]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1.plus(1)')
        assert hasSecurityException {
            shell.evaluate('java.lang.Math.cos(2)')
        }
    }

    @Test
    void testDisallowedReceiversWithStaticMethod() {
        customizer.disallowedReceiversClasses = [Math]
        def shell = new GroovyShell(configuration)
        shell.evaluate('1.plus(1)')
        shell.evaluate('Collections.sort([])')
        assert hasSecurityException {
            shell.evaluate('java.lang.Math.cos(2)')
        }
    }

    @Test // GROOVY-4978
    void testVisitMethodBody() {
        customizer.disallowedImports = [
            "java.lang.System",
            "groovy.lang.GroovyShell",
            "groovy.lang.GroovyClassLoader"]
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate('System.println(1)')
        }
        assert hasSecurityException {
            shell.evaluate('def x() { System.println(1) }')
        }
    }

    @Test // GROOVY-7424
    void testClassWithInterfaceVisitable() {
        def shell = new GroovyShell(configuration)
        shell.evaluate '''
            interface Foo { def baz() }
            class Bar implements Foo { def baz() { 42 } }
            assert new Bar().baz() == 42
        '''
    }

    @Test // GROOVY-6153
    void testDeterministicAllowedListBehaviour() {
        def allowedClasses = ["java.lang.Object", "test"]
        customizer.with {
            setIndirectImportCheckEnabled(true);
            setAllowedImports(allowedClasses);
            setAllowedReceivers(allowedClasses);
            setPackageAllowed(true);
            setClosuresAllowed(true);
            setMethodDefinitionAllowed(true);
        }
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                java.lang.System.out.println("run ")
            '''
        }
    }

    @Test // GROOVY-6153
    void testDeterministicAllowedListBehaviour2() {
        def allowedClasses = ["java.lang.Object", "test"]
        customizer.with {
            setIndirectImportCheckEnabled(true);
            setAllowedConstantTypes(allowedClasses);
            setAllowedReceivers(allowedClasses);
            setPackageAllowed(true);
            setClosuresAllowed(true);
            setMethodDefinitionAllowed(true);
        }
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                java.lang.Long x = 666L
            '''
        }
    }

    //--------------------------------------------------------------------------
    // code outside method bodies: constructors and initializers

    private void disallowSystemReceiver() {
        customizer.disallowedReceivers = ['java.lang.System']
    }

    @Test
    void testDisallowedReceiverInScriptBody() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate "System.getProperty('java.version')"
        }
    }

    @Test
    void testDisallowedReceiverInConstructor() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                class A { A() { System.getProperty('java.version') } }
                new A()
            '''
        }
    }

    @Test
    void testDisallowedReceiverInStaticInitializer() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                class A { static { System.getProperty('java.version') } }
                new A()
            '''
        }
    }

    @Test
    void testDisallowedReceiverInObjectInitializer() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                class A { { System.getProperty('java.version') } }
                new A()
            '''
        }
    }

    @Test
    void testDisallowedReceiverInFieldInitializer() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                class A { def f = System.getProperty('java.version') }
                new A()
            '''
        }
    }

    @Test
    void testDisallowedReceiverInStaticFieldInitializer() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                class A { static def f = System.getProperty('java.version') }
                new A()
            '''
        }
    }

    @Test
    void testGeneratedScriptConstructorsAreNotChecked() {
        // every script class has generated constructors which call super(Binding); they are not
        // written by the author of the script, so they must not be subject to the restrictions
        customizer.with {
            disallowedReceivers = ['java.lang.System']
            allowedExpressions = [BinaryExpression, ConstantExpression]
        }
        def shell = new GroovyShell(configuration)
        shell.evaluate '1 + 1'
        // no error means success
    }

    @Test
    void testDisallowedReceiverMovedIntoGeneratedConstructor() {
        // @TupleConstructor(pre=...) relocates the closure body into the constructor it generates;
        // the statements keep their original source position, so they are still the author's code
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                @groovy.transform.TupleConstructor(pre={ System.getProperty('java.version') })
                class A { String a }
                new A('x')
            '''
        }
    }

    @Test
    void testDisallowedReceiverMovedIntoGeneratedMapConstructor() {
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                @groovy.transform.MapConstructor(pre={ System.getProperty('java.version') })
                class A { String a }
                null
            '''
        }
    }

    @Test
    void testDisallowedReceiverMovedIntoSyntheticMethod() {
        // @ConditionalInterrupt lifts its closure into a synthetic method and calls it at every
        // method start and every loop; the closure is still code the script author wrote
        disallowSystemReceiver()
        def shell = new GroovyShell(configuration)
        assert hasSecurityException {
            shell.evaluate '''
                import groovy.transform.ConditionalInterrupt
                @ConditionalInterrupt({ System.getProperty('java.version') != null })
                class A { def m() { 1 } }
                null
            '''
        }
    }

    @Test
    void testGeneratedSyntheticMethodsAreNotChecked() {
        // an enum gets synthetic values(), valueOf(), next(), previous() and $INIT methods whose
        // bodies call java.lang.Enum. None of that is written by the script author, and none of it
        // carries a source position, so the restrictions must not reach it. Without the filter in
        // visitSyntheticMethods this script is rejected with
        // "Method calls not allowed on [java.lang.Enum]".
        customizer.disallowedReceivers = ['java.lang.Enum']
        def shell = new GroovyShell(configuration)
        shell.evaluate '''
            enum E { X, Y }
            null
        '''
        // no error means success
    }

    @Test
    void testTransformGeneratedConstructorIsNotChecked() {
        // @TupleConstructor generates a constructor, which likewise is not authored source
        customizer.with {
            disallowedReceivers = ['java.lang.System']
            indirectImportCheckEnabled = true
        }
        def shell = new GroovyShell(configuration)
        shell.evaluate '''
            @groovy.transform.TupleConstructor
            class A { String a }
            new A('x')
        '''
        // no error means success
    }

    // GROOVY-12283: a cast whose operand is a list/map/closure literal, and a named-argument
    // subscript, construct an instance of the named type rather than converting a value, so the
    // indirect import check applies to them exactly as it does to a constructor call.

    @Test
    void testIndirectImportCheckBlocksCastAndAsListCoercion() {
        customizer.allowedImports = ['java.lang.String']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        // the constructor form is blocked already; the coercion forms build the same File
        assert hasSecurityException { shell.evaluate("new java.io.File('/etc/passwd')") }
        assert hasSecurityException { shell.evaluate("(java.io.File) ['/etc/passwd']") }
        assert hasSecurityException { shell.evaluate("['/etc/passwd'] as java.io.File") }
    }

    @Test
    void testIndirectImportCheckBlocksClosureCoercion() {
        customizer.allowedImports = ['java.lang.String']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        assert hasSecurityException { shell.evaluate("(Runnable) { }") }
        assert hasSecurityException { shell.evaluate("{ -> } as Runnable") }
    }

    @Test
    void testIndirectImportCheckBlocksNamedArgConstruction() {
        customizer.disallowedImports = ['org.codehaus.groovy.control.customizers.SecGadget']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        String g = 'org.codehaus.groovy.control.customizers.SecGadget'
        // entry-only and explicit-cast forms coerce to a cast of a map literal (the cast branch)
        assert hasSecurityException { shell.evaluate("${g}[a: 1]") }
        assert hasSecurityException { shell.evaluate("(${g}) [a: 1]") }
        // an entry mixed with a spread stays a subscript BinaryExpression (the subscript branch)
        assert hasSecurityException { shell.evaluate("def m = [b: 2]; ${g}[a: 1, *: m]") }
    }

    @Test
    void testIndirectImportCheckAllowsCoercionToPermittedType() {
        customizer.allowedImports = ['java.io.File', 'java.lang.Runnable']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        // the target types are permitted, so the coercions are permitted
        shell.evaluate("(java.io.File) ['/tmp/x']")
        shell.evaluate("['/tmp/x'] as java.io.File")
        shell.evaluate("(Runnable) { }")
    }

    @Test
    void testIndirectImportCheckLeavesInertCastsUnexamined() {
        // a plain (converting) cast does not construct, so it is not checked even when its type
        // is not on the allow list — this pins the slice boundary
        customizer.allowedImports = ['java.util.ArrayList']
        customizer.indirectImportCheckEnabled = true
        def shell = new GroovyShell(configuration)
        shell.evaluate("(CharSequence) 'hello'")     // operand is a value, not a literal coercion
        shell.evaluate("def n = 1; (Number) n")
        // and a genuine positional subscript is untouched
        shell.evaluate("def list = [10, 20]; list[1]")
        // a primitive-array coercion has no class name to check, so it is not blocked
        shell.evaluate("(int[]) [1, 2, 3]")
        shell.evaluate("[1, 2, 3] as int[]")
        // a pure spread subscript coerces to `m as Foo` — a variable operand, the same
        // non-literal coercion residual as `var as Foo`, so it is not examined (and constructs)
        shell.evaluate("def m = [x: 1]; org.codehaus.groovy.control.customizers.SecGadget[*: m]")
    }
}

/**
 * Helper for {@link SecureASTCustomizerTest}: a class with a map constructor, referenced by
 * fully qualified name so the indirect import check applies (GROOVY-12283).
 */
class SecGadget {
    String tag
    SecGadget(Map m) { tag = "map:$m" }
}
