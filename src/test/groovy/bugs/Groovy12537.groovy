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

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A field reached through a protected or a doubly-nested private member type
 * is accessible wherever Java allows it, so the type checker must not reject
 * it as belonging to an inaccessible type, and must still reject it where Java
 * does, whether the member type is compiled with the accessor or precompiled.
 */
final class Groovy12537 {

    private GroovyClassLoader loader

    @BeforeEach
    void setUp() {
        loader = new GroovyClassLoader()
        loader.parseClass '''
            package p
            class Base {
                protected static class Prot { public static String F = 'f' }
            }
        '''
    }

    @Test
    void testProtectedMemberTypeFromSubclass() {
        def sub = loader.parseClass '''
            package q
            @groovy.transform.CompileStatic
            class Sub extends p.Base {
                static String run() { p.Base.Prot.F }
            }
        '''
        assert sub.run() == 'f'
    }

    @Test
    void testProtectedMemberTypeFromSubclassBody() {
        def sub = loader.parseClass '''
            package q
            import java.util.concurrent.Callable
            @groovy.transform.CompileStatic
            class Sub extends p.Base {
                static class In { static String run() { p.Base.Prot.F } }
                static String run() {
                    Callable<String> anon = new Callable<String>() { String call() { p.Base.Prot.F } }
                    def closure = { -> p.Base.Prot.F }
                    In.run() + anon.call() + closure()
                }
            }
        '''
        assert sub.run() == 'fff'
    }

    @Test
    void testProtectedMemberTypeFromSubclassBodyInSameUnit(@TempDir Path target) {
        compile(target,
            'Base.groovy': '''
                package p
                class Base {
                    protected static class Prot { public static String F = 'f' }
                }
            ''',
            'Sub.groovy': '''
                package q
                @groovy.transform.CompileStatic
                class Sub extends p.Base {
                    static class In { static String run() { p.Base.Prot.F } }
                    static String run() { def closure = { -> p.Base.Prot.F }; In.run() + closure() }
                }
            ''')
        def classes = new GroovyClassLoader()
        classes.addClasspath(target.toString())
        assert classes.loadClass('q.Sub').run() == 'ff'
    }

    @Test
    void testProtectedMemberTypeFromNonSubclassInSameUnit(@TempDir Path target) {
        def err = shouldFail(CompilationFailedException) {
            compile(target,
                'Base.groovy': '''
                    package p
                    class Base {
                        protected static class Prot { public static String F = 'f' }
                    }
                ''',
                'Other.groovy': '''
                    package q
                    @groovy.transform.CompileStatic
                    class Other { static String run() { p.Base.Prot.F } }
                ''')
        }
        assert err.message.contains('Cannot access field: F of class: p.Base$Prot from class: q.Other')
    }

    @Test
    void testPackagePrivateMemberTypeFromAnotherPackageInSameUnit(@TempDir Path target) {
        def err = shouldFail(CompilationFailedException) {
            compile(target,
                'Base.groovy': '''
                    package p
                    class Base {
                        @groovy.transform.PackageScope static class Pkg { public static String F = 'f' }
                    }
                ''',
                'Sub.groovy': '''
                    package q
                    @groovy.transform.CompileStatic
                    class Sub extends p.Base { static String run() { p.Base.Pkg.F } }
                ''')
        }
        assert err.message.contains('Cannot access field: F of class: p.Base$Pkg from class: q.Sub')
    }

    @Test
    void testPrecompiledPackagePrivateMemberTypeFromAnotherPackage() {
        loader.parseClass '''
            package p
            class Holder {
                @groovy.transform.PackageScope static class Pkg { public static String F = 'f' }
            }
        '''
        def err = shouldFail(CompilationFailedException) {
            loader.parseClass '''
                package q
                @groovy.transform.CompileStatic
                class Sub extends p.Holder { static String run() { p.Holder.Pkg.F } }
            '''
        }
        assert err.message.contains('Cannot access field: F of class: p.Holder$Pkg from class: q.Sub')
    }

    @Test
    void testPrecompiledPrivateMemberType() {
        loader.parseClass '''
            package p
            class Holder {
                private static class Hidden { public static String F = 'f' }
            }
        '''
        for (pkg in ['p', 'q']) { // another top-level class is outside the nest, in any package
            def err = shouldFail(CompilationFailedException) {
                loader.parseClass """
                    package $pkg
                    @groovy.transform.CompileStatic
                    class Other { static String run() { p.Holder.Hidden.F } }
                """
            }
            assert err.message.contains("Cannot access field: F of class: p.Holder\$Hidden from class: ${pkg}.Other")
        }
    }

    @Test
    void testPrecompiledPrivateMemberTypeReadFromClassFile(@TempDir Path target) {
        compile(target, 'Holder.groovy': '''
            package p
            class Holder {
                private static class Hidden { public static String F = 'f' }
            }
        ''')
        def classes = new GroovyClassLoader()
        classes.addClasspath(target.toString())
        def err = shouldFail(CompilationFailedException) {
            classes.parseClass '''
                package q
                @groovy.transform.CompileStatic
                class Other { static String run() { p.Holder.Hidden.F } }
            '''
        }
        assert err.message.contains('Cannot access field: F of class: p.Holder$Hidden from class: q.Other')
    }

    private static void compile(Map<String, String> sources, Path target) {
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: target.toFile()))
        sources.each { name, text -> unit.addSource(name, text) }
        unit.compile()
    }

    @Test
    void testPrivateMemberTypeNestedInMemberType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Outer {
                private static class A {
                    private static class B { public static String F = 'f' }
                }
                static class C { static String run() { A.B.F } }
                static String run() { A.B.F + C.run() }
            }
            assert Outer.run() == 'ff'
        '''
    }
}
