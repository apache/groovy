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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * An anonymous inner class declared in a method-level {@code @CompileStatic}
 * method (enclosing class not annotated) was marked as statically compiled only
 * after the type checker had visited its body, so the body compiled dynamically
 * while the class was generated with the static-compilation MOP writer, which
 * emits no {@code super$} bridges: any {@code super.m()} call then failed with
 * {@code MissingMethodException}.
 */
final class Groovy12363 {

    @Test
    void testSuperCallFromAnonymousClassInCompileStaticMethod() {
        assertScript '''
            import java.util.logging.*

            class Holders {
                @groovy.transform.CompileStatic
                static Handler cs() {
                    new StreamHandler(System.out, new SimpleFormatter()) {
                        void publish(LogRecord r) { super.publish(r); flush() }
                    }
                }
                static Handler dyn() {
                    new StreamHandler(System.out, new SimpleFormatter()) {
                        void publish(LogRecord r) { super.publish(r); flush() }
                    }
                }
            }

            def record = new LogRecord(Level.INFO, 'groovy12363')
            Holders.cs().publish(record)   // threw MissingMethodException
            Holders.dyn().publish(record)

            // the body now compiles statically (direct super call, no bridge needed);
            // the dynamic variant keeps its MOP bridge
            assert !Holders.cs().getClass().declaredMethods.any { it.name.startsWith('super$') }
            assert  Holders.dyn().getClass().declaredMethods.any { it.name.startsWith('super$') }
        '''
    }

    @Test
    void testSuperCallWithArgumentsAndResultFromGroovySuperclass() {
        assertScript '''
            abstract class Base {
                String greet(String who) { "hi $who" }
            }
            class Factory {
                @groovy.transform.CompileStatic
                static Base make() {
                    new Base() {
                        String greet(String who) { 'cs:' + super.greet(who) }
                    }
                }
            }
            assert Factory.make().greet('x') == 'cs:hi x'
        '''
    }

    @Test
    void testOperatorsAndCapturedVariablesInAnonymousClassBody() {
        // the statically compiled body also needs StaticCompilationTransformer's
        // rewriting (operator calls), which the method-level path did not apply
        // to anonymous classes; covers captured parameters, locals, fields and nesting
        assertScript '''
            class H {
                StringBuilder field = new StringBuilder()
                @groovy.transform.CompileStatic
                Runnable make(StringBuilder param) {
                    StringBuilder local = new StringBuilder()
                    new Runnable() {
                        void run() {
                            param << 'p'
                            local << 'l'
                            field << 'f'
                            def nested = new Runnable() { void run() { param << 'n' } }
                            nested.run()
                            param << local.toString() + field.toString()
                        }
                    }
                }
            }
            def h = new H()
            def param = new StringBuilder()
            h.make(param).run()
            assert param.toString() == 'pnlf'
        '''
    }

    @Test
    void testCompileStaticMethodInsideCompileDynamicClass() {
        // the most specific annotation wins: the anonymous class body follows the
        // @CompileStatic method even though the enclosing class is @CompileDynamic
        assertScript '''
            abstract class Base {
                String greet(String who) { "hi $who" }
            }
            @groovy.transform.CompileDynamic
            class Outer {
                @groovy.transform.CompileStatic
                Base make(StringBuilder sink) {
                    new Base() {
                        String greet(String who) { sink << who; 'cs:' + super.greet(who) }
                    }
                }
            }
            def sink = new StringBuilder()
            def b = new Outer().make(sink)
            assert b.greet('x') == 'cs:hi x'
            assert sink.toString() == 'x'
            assert !b.getClass().declaredMethods.any { it.name.startsWith('super$') } : 'body should be statically compiled'
        '''
    }

    @Test
    void testCompileDynamicMethodInsideCompileStaticClass() {
        // the reverse: a dynamic method in a static class keeps a dynamic anonymous class
        assertScript '''
            abstract class Base {
                String greet(String who) { "hi $who" }
            }
            @groovy.transform.CompileStatic
            class Outer {
                @groovy.transform.CompileDynamic
                Base make(StringBuilder sink) {
                    new Base() {
                        String greet(String who) { sink << who; 'dyn:' + super.greet(who) }
                    }
                }
            }
            def sink = new StringBuilder()
            def b = new Outer().make(sink)
            assert b.greet('x') == 'dyn:hi x'
            assert sink.toString() == 'x'
        '''
    }

    @Test
    void testInvokedFromJavaCode() {
        // the shape of the original report: the JDK logger calls publish, which calls super
        assertScript '''
            import java.util.logging.*

            class Holders {
                @groovy.transform.CompileStatic
                static Logger logger(StringBuilder sink) {
                    def l = Logger.getLogger('groovy12363'); l.useParentHandlers = false
                    l.addHandler(new Handler() {
                        void publish(LogRecord r) { sink << r.message }
                        void flush() {}
                        void close() {}
                    })
                    l.addHandler(new StreamHandler(new ByteArrayOutputStream(), new SimpleFormatter()) {
                        void publish(LogRecord r) { super.publish(r); sink << '!' }
                    })
                    l
                }
            }
            def sink = new StringBuilder()
            Holders.logger(sink).info('hello')
            assert sink.toString() == 'hello!'
        '''
    }
}
