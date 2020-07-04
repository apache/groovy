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
import org.codehaus.groovy.syntax.Types
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link SecureASTCustomizer}.
 */
final class SecureASTCustomizerTest {

    private final CompilerConfiguration configuration = new CompilerConfiguration()
    private final SecureASTCustomizer customizer = new SecureASTCustomizer()

    @Before
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
    void testAllowedIndirectStarImports() {
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

    @Test // GROOVY-8135
    void testStarImportsAllowedListWithIndirectImportCheckEnabled() {
        customizer.indirectImportCheckEnabled = true
        customizer.allowedStarImports = ['java.lang']
        def shell = new GroovyShell(configuration)
        shell.evaluate('Object object = new Object()')
        shell.evaluate('Object object = new Object(); object.hashCode()')
        shell.evaluate('Object[] array = new Object[0]; array.size()')
        shell.evaluate('Object[][] array = new Object[0][0]; array.size()')
    }
}
