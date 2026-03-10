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
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy11292 {

    @Test
    void testClassWithNonSealedParent1() {
        assertScript '''import java.lang.ref.SoftReference // non-sealed type

            class TestReference<T> extends SoftReference<T> {
                TestReference(T referent) {
                    super(referent)
                }
            }

            assert new TestReference(null)
        '''
    }

    @Test
    void testClassWithNonSealedParent2() {
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
                class E extends B {}
            '''
            def f = new File(parentDir, 'F.groovy')
            f.write '''
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

    // GROOVY-11768
    @Test
    void testClassWithNonSealedParent3() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public abstract sealed class A permits B {}
            '''
            def b = new File(parentDir, 'B.java')
            b.write '''
                public abstract non-sealed class B extends A {}
            '''
            def c = new File(parentDir, 'C.java')
            c.write '''
                public class C extends B {}
            '''
            def d = new File(parentDir, 'D.groovy')
            d.write '''
                class D extends C {}
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d)
            cu.compile()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
