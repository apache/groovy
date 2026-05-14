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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Joint-compilation and decompiled-types matrix for sealed types per
 * GEP-13 §"Joint compilation and decompiled types":
 *
 * <ul>
 * <li>a Groovy class extending a Java sealed class is checked against the
 *     Java type's {@code permits} set;</li>
 * <li>a Java class extending a Groovy sealed class likewise honours the
 *     Groovy {@code permits} set;</li>
 * <li>for a class loaded from bytecode (without source available),
 *     non-sealed status is computed as: <em>the parent is sealed</em> AND
 *     <em>this type is neither final nor sealed</em>.</li>
 * </ul>
 *
 * The reverse-direction tests rely on native sealed bytecode, which
 * requires JDK 17+.
 */
final class SealedJointCompilationTest {

    private static void compileSources(Map<String, String> sources) {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            def files = sources.collect { name, content ->
                def f = new File(parentDir, name)
                f.write(content)
                f
            }
            def loader = new GroovyClassLoader(SealedJointCompilationTest.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(files as File[])
            cu.compile()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-11292
    @Test
    void testExtendingDecompiledNonSealedJdkClass() {
        assertScript '''import java.lang.ref.SoftReference // non-sealed type

            class TestReference<T> extends SoftReference<T> {
                TestReference(T referent) {
                    super(referent)
                }
            }

            assert new TestReference(null)
        '''
    }

    // GROOVY-11292
    @Test
    void testJavaSealedJavaNonSealedGroovyDescendants() {
        compileSources([
            'A.java'  : 'public sealed class A permits B {}',
            'B.java'  : 'public non-sealed class B extends A {}',
            'C.groovy': 'class C extends B {}',
            'D.groovy': 'class D extends C {}',
            'E.groovy': 'class E extends B {}',
            'F.groovy': 'class F extends E {}'
        ])
    }

    // GROOVY-11768
    @Test
    void testJavaSealedAbstractNonSealedJavaIntermediateGroovyDescendant() {
        compileSources([
            'A.java'  : 'public abstract sealed class A permits B {}',
            'B.java'  : 'public abstract non-sealed class B extends A {}',
            'C.java'  : 'public class C extends B {}',
            'D.groovy': 'class D extends C {}'
        ])
    }

    @Test
    void testJavaSealedDirectGroovyPermitted() {
        compileSources([
            'A.java'  : 'public sealed class A permits B {}',
            'B.groovy': 'final class B extends A {}'
        ])
    }

    @Test
    void testJavaSealedGroovyNotPermitted() {
        shouldFail {
            compileSources([
                'A.java'  : 'public sealed class A permits B {}',
                'B.java'  : 'public final class B extends A {}',
                'C.groovy': 'class C extends A {}'
            ])
        }
    }

    @Test
    void testGroovySealedJavaPermitted() {
        assumeTrue(isAtLeastJdk('17.0'))
        compileSources([
            'A.groovy': 'sealed class A permits B {}',
            'B.java'  : 'public final class B extends A {}'
        ])
    }

    // Groovy sealed -> Groovy implicit-non-sealed intermediate -> Java descendant.
    // The stub for the Groovy intermediate must declare non-sealed for javac to
    // accept it as a permitted subtype of the sealed parent.
    @Test
    void testGroovySealedImplicitNonSealedGroovyIntermediateJavaDescendant() {
        assumeTrue(isAtLeastJdk('17.0'))
        compileSources([
            'A.groovy': 'sealed class A permits B {}',
            'B.groovy': 'class B extends A {}',
            'C.java'  : 'public class C extends B {}'
        ])
    }

    // Inferred permits (sealed in source, permitted subtypes inferred from the
    // same compilation unit). Stub-gen must surface the inferred list so javac
    // sees the sealed contract.
    @Test
    void testGroovySealedInferredPermitsJavaConsumer() {
        assumeTrue(isAtLeastJdk('17.0'))
        // Positive: Java permitted subtype that IS the inferred one.
        compileSources([
            'AB.groovy': '@groovy.transform.Sealed class A {}\nfinal class B extends A {}',
            // No-op file referring to A's permitted subtype B just to exercise the surface.
            'Use.java' : 'public class Use { public B b() { return null; } }'
        ])
        // Negative: Java tries to extend A but isn't in the inferred permits.
        shouldFail {
            compileSources([
                'AB.groovy': '@groovy.transform.Sealed class A {}\nfinal class B extends A {}',
                'C.java'   : 'public final class C extends A {}'
            ])
        }
    }

    @Test
    void testGroovySealedJavaNotPermitted() {
        assumeTrue(isAtLeastJdk('17.0'))
        shouldFail {
            compileSources([
                'A.groovy': 'sealed class A permits B {}',
                'B.java'  : 'public final class B extends A {}',
                'C.java'  : 'public final class C extends A {}'
            ])
        }
    }
}
