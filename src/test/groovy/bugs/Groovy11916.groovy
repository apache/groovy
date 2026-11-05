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

import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Star imports and the implicit default imports resolve only accessible types.
 * An explicit single-type import, a fully qualified name, and {@code Class.forName}
 * still name a type that is not public.
 */
final class Groovy11916 {

    private static GroovyClassLoader types

    @BeforeAll
    static void compilePackagePrivateTypes() {
        types = compile(Groovy11916.classLoader, [
            'p/Types': '''
                package p

                @groovy.transform.PackageScope
                class Hidden {}

                class Visible {}

                @groovy.transform.PackageScope
                class Outer {
                    public static class Inner {}
                }

                class Protector {
                    protected static class Secret {}
                }

                class Priv {
                    private static class Secret {}
                    public static class Shown {}
                }

                @groovy.transform.PackageScope
                class Collision {}
            '''
        ])
        assert !Modifier.isPublic(types.loadClass('p.Hidden').modifiers)
        assert !Modifier.isPublic(types.loadClass('p.Collision').modifiers)
        assert Modifier.isPublic(types.loadClass('p.Visible').modifiers)
        assert !Modifier.isPublic(types.loadClass('p.Outer').modifiers)
        assert Modifier.isPublic(types.loadClass('p.Outer$Inner').modifiers)
        assert Modifier.isProtected(types.loadClass('p.Protector$Secret').modifiers)
        assert Modifier.isPrivate(types.loadClass('p.Priv$Secret').modifiers)
    }

    private static GroovyClassLoader compile(ClassLoader parent, Map<String, String> sources) {
        def loader = new GroovyClassLoader(parent)
        def unit = new CompilationUnit(null, null, loader)
        sources.each { name, text -> unit.addSource(name + '.groovy', text) }
        unit.compile(Phases.CLASS_GENERATION)
        unit.classes.sort { it.name }.each { groovyClass ->
            loader.defineClass(groovyClass.name, groovyClass.bytes)
        }
        loader
    }

    @Test
    void testStarImportDoesNotResolvePackagePrivateJdkType() {
        assert !Modifier.isPublic(Class.forName('java.util.TimSort').modifiers)

        def err = shouldFail '''
            import java.util.*
            def seen = null instanceof TimSort
        '''
        assert err.message.contains('unable to resolve class TimSort')

        // A bare name in a script is a dynamic property when it is not a type.
        // It must not bind the inaccessible class (that binding is what throws
        // IllegalAccessError for a later instanceof / checkcast).
        err = shouldFail '''
            import java.util.*
            assert TimSort.name == 'java.util.TimSort'
        '''
        assert err instanceof MissingPropertyException
    }

    @Test
    void testDefaultImportDoesNotResolvePackagePrivateJdkType() {
        def err = shouldFail '''
            def seen = null instanceof TimSort
        '''
        assert err.message.contains('unable to resolve class TimSort')
    }

    @Test
    void testModuleImportDoesNotResolvePackagePrivateJdkType() {
        def err = shouldFail '''
            import module java.base
            def seen = null instanceof TimSort
        '''
        assert err.message.contains('unable to resolve class TimSort')
    }

    @Test
    void testStarImportStillResolvesPublicTypes() {
        assertScript '''
            import java.util.*
            assert ArrayList.name == 'java.util.ArrayList'
            assert List.name == 'java.util.List'
            assert new ArrayList() instanceof List
        '''
        assertScript '''
            import java.util.Map.*
            Entry e = [a:1].entrySet().first()
            assert e.key == 'a'
        '''
        assertScript '''
            import static java.util.Map.*
            Entry e = [a:1].entrySet().first()
            assert e instanceof Entry
        '''
        assertScript '''
            import module java.base
            assert ArrayList.name == 'java.util.ArrayList'
            assert LocalDate.name == 'java.time.LocalDate'
        '''
    }

    @Test
    void testExplicitReferenceToPackagePrivateTypeStillResolves() {
        assertScript '''
            import java.util.TimSort
            assert TimSort.name == 'java.util.TimSort'
        '''
        assertScript '''
            assert java.util.TimSort.name == 'java.util.TimSort'
        '''
        assertScript '''
            assert Class.forName('java.util.TimSort').name == 'java.util.TimSort'
        '''
        compile(types, [
            'q/Direct': '''
                package q
                import p.Hidden
                class Direct {
                    Hidden h
                }
            '''
        ])
    }

    @Test
    void testSamePackageStarImportSeesPackagePrivateType() {
        compile(types, [
            'p/Peer': '''
                package p
                import p.*
                class Peer {
                    Hidden h
                    Visible v
                }
            '''
        ])
    }

    @Test
    void testOtherPackageStarImportDoesNotSeePackagePrivateType() {
        def err = shouldFail {
            compile(types, [
                'q/User': '''
                    package q
                    import p.*
                    class User {
                        Hidden h
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Hidden')

        compile(types, [
            'q/PublicUser': '''
                package q
                import p.*
                class PublicUser {
                    Visible v
                }
            '''
        ])
    }

    @Test
    void testStarImportOfPublicMemberOfInaccessibleType() {
        def err = shouldFail {
            compile(types, [
                'q/InnerUser': '''
                    package q
                    import p.Outer.*
                    class InnerUser {
                        Inner i
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Inner')

        compile(types, [
            'p/InnerPeer': '''
                package p
                import p.Outer.*
                class InnerPeer {
                    Inner i
                }
            '''
        ])
    }

    @Test
    void testProtectedMemberTypeFromSubclass() {
        compile(types, [
            'q/Child': '''
                package q
                import p.Protector
                import p.Protector.*
                class Child extends Protector {
                    Secret s
                }
            '''
        ])
        def err = shouldFail {
            compile(types, [
                'q/Stranger': '''
                    package q
                    import p.Protector.*
                    class Stranger {
                        Secret s
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Secret')
    }

    @Test
    void testPrivateMemberTypeStarImport() {
        compile(types, [
            'q/ShownUser': '''
                package q
                import p.Priv.*
                class ShownUser {
                    Shown s
                }
            '''
        ])
        compile(types, [
            'q/ShownStatic': '''
                package q
                import static p.Priv.*
                class ShownStatic {
                    Shown s
                }
            '''
        ])
        def err = shouldFail {
            compile(types, [
                'q/SecretUser': '''
                    package q
                    import p.Priv.*
                    class SecretUser {
                        Secret s
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Secret')
    }

    @Test
    void testInaccessibleStarImportDoesNotHideLaterPublicType() {
        def loader = compile(types, [
            'q/Collision': '''
                package q
                class Collision {}
            ''',
            'r/User': '''
                package r
                import p.*
                import q.*
                class User {
                    static Class which() { Collision }
                }
            '''
        ])
        assert loader.loadClass('r.User').getDeclaredMethod('which').invoke(null).name == 'q.Collision'
    }
}
