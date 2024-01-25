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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

final class Groovy11292 {
    @Test
    void testClassWithNonSealedParent_1() {
        assumeTrue(isAtLeastJdk('17.0'))

        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public sealed class A permits B {}
            '''
            def b = new File(parentDir, 'B.java')
            b.write '''
                public non-sealed class B extends A {}
            '''
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C extends B {}
            '''
            def d = new File(parentDir, 'D.groovy')
            d.write '''
                class D extends C {}
            '''
            def e = new File(parentDir, 'E.groovy')
            e.write '''
                @groovy.transform.CompileStatic
                class E extends B {}
            '''
            def f = new File(parentDir, 'F.groovy')
            f.write '''
                @groovy.transform.CompileStatic
                class F extends E {}
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d, e, f)
            cu.compile()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    @Test
    void testClassWithNonSealedParent_2() {
        assertScript '''
            import org.codehaus.groovy.util.Finalizable
            class TestReference<T>
                extends java.lang.ref.SoftReference<T>
                implements org.codehaus.groovy.util.Reference<T, Finalizable> {

                final Finalizable handler

                TestReference(T referent) {
                    super(referent)
                }

                @Override
                Finalizable getHandler() {
                    return handler
                }
            }
            assert new TestReference(null)
        '''
    }
}
