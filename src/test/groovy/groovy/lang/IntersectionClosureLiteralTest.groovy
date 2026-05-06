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

import org.junit.jupiter.api.Test

/**
 * Tests for native intersection support of closure literals
 * (GROOVY-11998 PR5).
 *
 * Under {@code @CompileStatic}, a closure literal cast to an intersection
 * has its generated class declare the marker interfaces so that the cast
 * is a no-op (no runtime proxy needed).
 */
final class IntersectionClosureLiteralTest {

    @Test
    void 'closure cast to (Runnable & MyMarker) implements MyMarker via addInterface'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            interface MyMarker {}

            @CompileStatic
            class T {
                static Object castForm() {
                    return (Runnable & MyMarker) { -> }
                }
            }

            def c = T.castForm()
            assert c instanceof Runnable
            assert c instanceof MyMarker
            // Generated closure class itself implements MyMarker - no proxy wrapping
            assert MyMarker.isAssignableFrom(c.class)
        ''')
    }

    @Test
    void 'closure cast to (Runnable & Serializable & MyMarker) implements all three'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            interface MyMarker {}

            @CompileStatic
            class T {
                static Object make() {
                    return (Runnable & java.io.Serializable & MyMarker) { -> }
                }
            }

            def c = T.make()
            assert c instanceof Runnable
            assert c instanceof java.io.Serializable
            assert c instanceof MyMarker
        ''')
    }

    @Test
    void 'as form on closure with custom marker also picks up addInterface'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            interface MyMarker {}

            @CompileStatic
            class T {
                static Object asForm() {
                    return ({ -> "hi" } as (Runnable & MyMarker))
                }
            }

            def c = T.asForm()
            assert c instanceof Runnable
            assert c instanceof MyMarker
        ''')
    }

    // GROOVY-11999: this previously NPE'd inside ProxyGeneratorAdapter when
    // interfaces list mixed bootstrap (Runnable) and user (MyMarker) loaders.
    @Test
    void 'dynamic closure as (Runnable & MyMarker) builds a multi-interface proxy'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            interface MyMarker {}
            def c = ({ -> "hi" } as (Runnable & MyMarker))
            assert c instanceof Runnable
            assert c instanceof MyMarker
            c.run()
        ''')
    }

    @Test
    void 'static cast form succeeds where dynamic would need a runtime proxy'() {
        // Without PR5, (R & MyMarker) cast form on a closure literal would throw
        // GroovyCastException at runtime because the closure isn't MyMarker.
        // PR5 makes the generated closure class implement MyMarker directly so
        // the cast is a true no-op.
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            interface MyMarker {}

            @CompileStatic
            class T {
                static Object castForm() {
                    return (Runnable & MyMarker) { -> }
                }
            }

            def c = T.castForm()
            // The closure subclass itself declares MyMarker — verify by class introspection
            assert MyMarker.isAssignableFrom(c.class)
            assert !c.class.name.startsWith('jdk.proxy')
            assert !c.class.name.contains('groovyProxy')
        ''')
    }

    @Test
    void 'closure cast to Cloneable & Serializable is identity (already implemented by Closure)'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            @CompileStatic
            class T {
                static Object make() {
                    def cl = { -> 1 }
                    return cl as (Cloneable & java.io.Serializable)
                }
            }
            def c = T.make()
            assert c instanceof Cloneable
            assert c instanceof java.io.Serializable
        ''')
    }

    @Test
    void 'marker interface with non-abstract default methods can still be added'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            interface MarkerWithDefault {
                default String tag() { 'tag' }
            }

            @CompileStatic
            class T {
                static Object make() {
                    return (Runnable & MarkerWithDefault) { -> }
                }
            }

            def c = T.make()
            assert c instanceof Runnable
            assert c instanceof MarkerWithDefault
            assert c.tag() == 'tag'
        ''')
    }

    @Test
    void 'closure literal intersection is serialisable when intersection includes Serializable'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.ByteArrayOutputStream
            import java.io.ByteArrayInputStream
            import java.io.ObjectOutputStream
            import java.io.ObjectInputStream

            interface MyMarker {}

            @CompileStatic
            class T {
                static Object make() {
                    return (Runnable & java.io.Serializable & MyMarker) { -> }
                }
            }

            def c = T.make()
            assert c instanceof java.io.Serializable
            assert c instanceof MyMarker

            def baos = new ByteArrayOutputStream()
            new ObjectOutputStream(baos).withCloseable { it.writeObject(c) }
            def bytes = baos.toByteArray()
            def restored = null
            new ObjectInputStream(new ByteArrayInputStream(bytes)).withCloseable {
                restored = it.readObject()
            }
            assert restored instanceof Runnable
            assert restored instanceof MyMarker
        ''')
    }
}
