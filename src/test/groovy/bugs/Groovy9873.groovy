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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9873 {

    @Test
    void testCoerceClosure1() {
        assertScript '''
            @Grab('io.vavr:vavr:0.10.4;transitive=false')
            import io.vavr.control.Try
            class C { }
            C resolve() {new C()}
            Try.of(this::resolve)
        '''
    }

    @Test
    void testCoerceClosure2() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C<T> {
                    private T t
                    C(T item) {
                        t = item
                    }
                    static <U> C<U> of(U item) {
                        new C<U>(item)
                    }
                    def <V> C<V> map(F<? super T, ? super V> func) {
                        new C<V>(func.apply(t))
                    }
                }
            '''
            def d = new File(parentDir, 'D.groovy')
            d.write '''
                class D {
                    static <W> Set<W> wrap(W o) {
                        Collections.singleton(o)
                    }
                }
            '''
            def f = new File(parentDir, 'F.groovy')
            f.write '''
                interface F<X,Y> {
                    Y apply(X x)
                }
            '''
            def g = new File(parentDir, 'G.groovy')
            g.write '''
                def c = C.of(123)
                def d = c.map(D.&wrap)
                def e = d.map(x -> x.first().intValue())
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(c, d, f, g)
            cu.compile()

            loader.loadClass('G').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testCoerceClosure3() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def f = new File(parentDir, 'F.groovy')
            f.write '''
                class FInfo extends EventObject {
                    FInfo() { super(null) }
                }
                interface FListener extends EventListener {
                    void somethingHappened(FInfo i)
                }
            '''
            def g = new File(parentDir, 'G.groovy')
            g.write '''
                class H {
                    void addFListener(FListener f) {
                        f.somethingHappened(null)
                    }
                    void removeFListener(FListener f) {
                    }
                }

                new H().somethingHappened = { info -> }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(f, g)
            cu.compile()

            loader.loadClass('G').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
