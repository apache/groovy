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
package groovy.lang

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * Tests for runtime intersection-type cast and {@code as} coercion on
 * non-functional sources (GROOVY-11998 PR4).
 *
 * The functional-source cases (lambdas / method references) are covered
 * by {@code IntersectionCastE2ETest}; this suite focuses on the dynamic
 * runtime path that goes through {@code IntersectionCastSupport}.
 */
final class IntersectionCoercionTest {

    @Test
    void 'dynamic strict cast accepts a value that already implements all components'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            class Both implements Runnable, Cloneable {
                void run() {}
            }
            def b = new Both()
            return (Runnable & Cloneable) b
        ''')
        assert result instanceof Runnable
        assert result instanceof Cloneable
    }

    @Test
    void 'dynamic strict cast throws on missing component'() {
        def shell = new GroovyShell()
        def thrown = assertThrows(GroovyCastException) {
            shell.evaluate('''
                def s = "hello"
                return (Runnable & java.io.Serializable) s
            ''')
        }
        // String is Serializable but not Runnable
        assert thrown.message.contains('Runnable') || thrown.message.contains('Cannot cast')
    }

    @Test
    void 'dynamic as on a closure builds a proxy implementing all interfaces'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            interface Greeter { String greet(String name) }
            def proxy = ({ String n -> "Hello, " + n } as (Greeter & java.io.Serializable))
            return [proxy, proxy.greet("world")]
        ''')
        def (proxy, greeting) = result
        assert proxy instanceof java.io.Serializable
        // The proxy's class implements Greeter via ProxyGenerator
        assert proxy.class.interfaces.any { it.name == 'Greeter' }
        assert greeting == 'Hello, world'
    }

    @Test
    void 'dynamic as on a closure that already implements all components is identity'() {
        // Closure already implements Runnable AND Serializable, so `as (Runnable & Serializable)`
        // should not wrap; identity coercion.
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            def cl = { -> 1 }
            def coerced = cl as (Runnable & java.io.Serializable)
            return coerced.is(cl)
        ''')
        assert result == true
    }

    @Test
    void 'dynamic as on a map builds a proxy implementing all interfaces'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            interface Action { void perform() }
            def calls = 0
            def proxy = ([perform: { -> calls++ }] as (Action & java.io.Serializable))
            proxy.perform()
            proxy.perform()
            return [proxy, calls]
        ''')
        def (proxy, calls) = result
        assert proxy instanceof java.io.Serializable
        assert proxy.class.interfaces.any { it.name == 'Action' }
        assert calls == 2
    }

    @Test
    void 'static cast on a value that already implements all components passes'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            import groovy.transform.CompileStatic

            class Both implements Runnable, Cloneable {
                void run() {}
            }

            @CompileStatic
            class T {
                static Object make() {
                    Both b = new Both()
                    return (Runnable & Cloneable) b
                }
            }
            T.make()
        ''')
        assert result instanceof Runnable
        assert result instanceof Cloneable
    }

    @Test
    void 'static as coercion on a closure builds a multi-interface proxy'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            import groovy.transform.CompileStatic
            interface Greeter { String greet(String name) }
            @CompileStatic
            class T {
                static Object make() {
                    def cl = { String n -> "Hello, " + n }
                    return (cl as (Greeter & java.io.Serializable))
                }
            }
            def p = T.make()
            return [p, p.greet("world")]
        ''')
        def (proxy, greeting) = result
        assert proxy instanceof java.io.Serializable
        assert proxy.class.interfaces.any { it.name == 'Greeter' }
        assert greeting == 'Hello, world'
    }

    @Test
    void 'three-component intersection coerces correctly'() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            interface One { String one() }
            interface Two { String two() }
            def proxy = ([one: { 'a' }, two: { 'b' }] as (One & Two & java.io.Serializable))
            return [proxy.one(), proxy.two(), proxy instanceof java.io.Serializable]
        ''')
        assert result == ['a', 'b', true]
    }

    @Test
    void 'null sources pass through cast and as'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            assert ((Runnable & java.io.Serializable) null) == null
            assert (null as (Runnable & java.io.Serializable)) == null
        ''')
    }
}
