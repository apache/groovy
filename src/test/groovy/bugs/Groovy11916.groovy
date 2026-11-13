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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Star imports, module imports, and default imports resolve only accessible
 * types. A source import shadows those defaults, including {@code java.lang}.
 * Two source star imports bind the first accessible type in source order.
 * An explicit single-type import, a fully qualified name, and
 * {@code Class.forName} still name a type that is not public.
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
        // Not the accessibility policy. isStarImportAccessible decides whether
        // a star, module, or default import may bind a type, and the test
        // methods below are that check: same package, another package, a
        // protected member, a private member, a public member of a non-public
        // outer. This block only pins the bytecode flags of the classes just
        // compiled, which those tests then load. Resolve reads the flags from
        // the class file. If @PackageScope had not cleared public, or a member
        // type did not have the flag named here, a later test would pass or
        // fail for a fixture that is not the type it names. The asserts are
        // not a second copy of the policy and are not repeated elsewhere.
        // Hidden and Collision are not public. Visible is public. Outer is not
        // public and its member Inner is public. Protector.Secret is protected.
        // Priv.Secret is private.
        assert !Modifier.isPublic(types.loadClass('p.Hidden').modifiers)
        assert !Modifier.isPublic(types.loadClass('p.Collision').modifiers)
        assert Modifier.isPublic(types.loadClass('p.Visible').modifiers)
        assert !Modifier.isPublic(types.loadClass('p.Outer').modifiers)
        assert Modifier.isPublic(types.loadClass('p.Outer$Inner').modifiers)
        assert Modifier.isProtected(types.loadClass('p.Protector$Secret').modifiers)
        assert Modifier.isPrivate(types.loadClass('p.Priv$Secret').modifiers)
    }

    private static GroovyClassLoader compile(ClassLoader parent, Map<String, String> sources, CompilerConfiguration config = null) {
        def loader = new GroovyClassLoader(parent, config)
        def unit = new CompilationUnit(config, null, loader)
        sources.each { name, text -> unit.addSource(name + '.groovy', text) }
        unit.compile(Phases.CLASS_GENERATION)
        unit.classes.sort { it.name }.each { groovyClass ->
            loader.defineClass(groovyClass.name, groovyClass.bytes)
        }
        loader
    }

    private static void putType(Map<String, String> sources, String pkg, String simpleName, String state) {
        sources[pkg + '/Marker'] = "package ${pkg}\nclass Marker {}"
        if (state == 'p') {
            sources[pkg + '/' + simpleName] = "package ${pkg}\nclass ${simpleName} {}"
        } else if (state == 'k') {
            sources[pkg + '/' + simpleName] = "package ${pkg}\n@groovy.transform.PackageScope\nclass ${simpleName} {}"
        }
    }

    private static String useSource(String pkg, String imports, String simpleName) {
        """\
package ${pkg}
${imports}
class Use {
    ${simpleName} field
    static Class which() { ${simpleName} }
}
"""
    }

    /** First public type in source order. {@code p} public, {@code k} package-private, {@code m} missing. */
    private static String firstStar(String a, String b, String c) {
        if (a == 'p') return 'pa.X'
        if (b == 'p') return 'pb.X'
        if (c == 'p') return 'pc.X'
        return 'unresolved'
    }

    private static String classifyUse(Map<String, String> sources, String className, CompilerConfiguration config = null) {
        // Dependency types are compiled first so package-private modifiers are
        // already on the bytecode when the importing compilation unit is resolved.
        def deps = sources.findAll { name, text -> !name.endsWith('/Use') }
        def use = sources.findAll { name, text -> name.endsWith('/Use') }
        try {
            def parent = deps ? compile(Groovy11916.classLoader, deps) : Groovy11916.classLoader
            def loader = compile(parent, use, config)
            return loader.loadClass(className).getDeclaredMethod('which').invoke(null).name
        } catch (Exception e) {
            String text = ''
            for (Throwable t = e; t != null; t = t.cause) {
                if (t.message != null) text += t.message + '\n'
            }
            if (text.contains('ambiguous')) return 'ambiguous'
            if (text.contains('unable to resolve')) return 'unresolved'
            if (text.contains('not public')) return 'not-public'
            def snippet = text.readLines().collect { it.trim() }.findAll { it }.take(4).join(' | ')
            return 'other:' + (snippet ?: e.getClass().simpleName)
        }
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
        // Compiles because a single-type import is not an on-demand import.
        // visitClass resolves each module import with resolve(type, false,
        // false, true) and fails only when that name does not exist.
        // isStarImportAccessible is not called on it, nor on a fully qualified
        // name. The program-structure chapter describes a simple import as
        // naming the class; it does not require a compile-time access error
        // for an explicit import. Star, module, and default imports are
        // filtered because they would bind a simple name the source did not
        // write.
        // The script also runs. TimSort is not public, so the class expression
        // is not an ldc (BytecodeHelper.isClassLiteralPossible is public
        // only). It is loaded with Class.forName, and ".name" is
        // Class.getName(), which does not access-check the class. That does
        // not decide whether ResolveVisitor accepts the import.
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
    void testOnDemandImportBindsStaticMemberTypeOnly() {
        def nest = compile(Groovy11916.classLoader, [
            'p/Nest': '''
                package p
                class Nest {
                    class Room {}
                    static class Hall {}
                }
            '''
        ])
        assert !Modifier.isStatic(nest.loadClass('p.Nest$Room').modifiers)
        assert Modifier.isStatic(nest.loadClass('p.Nest$Hall').modifiers)

        def err = shouldFail {
            compile(nest, [
                'q/RoomUser': '''
                    package q
                    import p.Nest.*
                    class RoomUser {
                        Room r
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Room')

        err = shouldFail {
            compile(nest, [
                'q/RoomStatic': '''
                    package q
                    import static p.Nest.*
                    class RoomStatic {
                        Room r
                    }
                '''
            ])
        }
        assert err.message.contains('unable to resolve class Room')

        compile(nest, [
            'q/HallUser': '''
                package q
                import p.Nest.*
                class HallUser {
                    Hall h
                }
            '''
        ])
        compile(nest, [
            'q/HallStatic': '''
                package q
                import static p.Nest.*
                class HallStatic {
                    Hall h
                }
            '''
        ])
    }

    @Test
    void testPackageScopeInSameCompilationUnitIsNotPublicYet() {
        // @PackageScope clears the synthetic public flag after resolve.
        // A star import in the same compilation unit must still skip the type.
        def err = shouldFail {
            compile(Groovy11916.classLoader, [
                'p/Hidden': '''
                    package p
                    @groovy.transform.PackageScope
                    class Hidden {}
                ''',
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

        compile(Groovy11916.classLoader, [
            'p/Hidden': '''
                package p
                @groovy.transform.PackageScope
                class Hidden {}
            ''',
            'p/Peer': '''
                package p
                import p.*
                class Peer {
                    Hidden h
                }
            '''
        ])

        err = shouldFail {
            compile(Groovy11916.classLoader, [
                'p/Outer': '''
                    package p
                    @groovy.transform.PackageScope
                    class Outer {
                        public static class Inner {}
                    }
                ''',
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

        // Targeting fields does not make the class package-private.
        compile(Groovy11916.classLoader, [
            'p/Fields': '''
                package p
                @groovy.transform.PackageScope(groovy.transform.PackageScopeTarget.FIELDS)
                class Fields {
                    String name
                }
            ''',
            'q/FieldsUser': '''
                package q
                import p.*
                class FieldsUser {
                    Fields f
                }
            '''
        ])
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

    /**
     * Two or more source star imports bind the first accessible type.
     * They do not tie, and an inaccessible type does not hide a later one.
     * <p>
     * pa.X, pb.X and pc.X are each missing ({@code m}), public ({@code p}),
     * or package-private ({@code k}). The use class is in another package
     * and imports {@code pa.*}, {@code pb.*}, {@code pc.*} in that order.
     * The expected type is the first public X. With no public X the name
     * stays unresolved. A repeated {@code import pa.*} still binds pa.X,
     * and the reversed list binds pc.X.
     */
    // GROOVY-11916
    @Test
    void testStarImportsBindFirstAccessibleType() {
        def failures = []
        for (String a : ['m', 'p', 'k']) {
            for (String b : ['m', 'p', 'k']) {
                for (String c : ['m', 'p', 'k']) {
                    def sources = [:]
                    putType(sources, 'pa', 'X', a)
                    putType(sources, 'pb', 'X', b)
                    putType(sources, 'pc', 'X', c)
                    sources['user/Use'] = useSource('user', 'import pa.*\nimport pb.*\nimport pc.*', 'X')
                    def actual = classifyUse(sources, 'user.Use')
                    def expected = firstStar(a, b, c)
                    if (actual != expected) {
                        failures << "pa=$a pb=$b pc=$c expected $expected but was $actual"
                    }
                }
            }
        }
        def sources = [:]
        putType(sources, 'pa', 'X', 'p')
        putType(sources, 'pb', 'X', 'p')
        putType(sources, 'pc', 'X', 'p')
        sources['user/Use'] = useSource('user', 'import pa.*\nimport pa.*', 'X')
        def duplicate = classifyUse(sources, 'user.Use')
        if (duplicate != 'pa.X') {
            failures << "duplicate star import expected pa.X but was $duplicate"
        }
        sources = [:]
        putType(sources, 'pa', 'X', 'p')
        putType(sources, 'pb', 'X', 'p')
        putType(sources, 'pc', 'X', 'p')
        sources['user/Use'] = useSource('user', 'import pc.*\nimport pb.*\nimport pa.*', 'X')
        def reversed = classifyUse(sources, 'user.Use')
        if (reversed != 'pc.X') {
            failures << "reversed import order expected pc.X but was $reversed"
        }
        assert failures.isEmpty() : failures.join('\n')
    }

    /**
     * A type declared in the same package is chosen before any on-demand
     * import, including a star of that same package.
     * <p>
     * The use class is {@code pa.Use}, with the same {@code m}/{@code p}/{@code k}
     * matrix as {@code testStarImportsBindFirstAccessibleType}. When pa.X
     * exists it wins whether it is public or package-private, even if pb.X
     * or pc.X is public. When pa.X is missing, the first accessible star
     * import is used.
     */
    // GROOVY-11916
    @Test
    void testSamePackageTypeShadowsOnDemandImports() {
        def failures = []
        for (String a : ['m', 'p', 'k']) {
            for (String b : ['m', 'p', 'k']) {
                for (String c : ['m', 'p', 'k']) {
                    def sources = [:]
                    putType(sources, 'pa', 'X', a)
                    putType(sources, 'pb', 'X', b)
                    putType(sources, 'pc', 'X', c)
                    sources['pa/Use'] = useSource('pa', 'import pa.*\nimport pb.*\nimport pc.*', 'X')
                    def actual = classifyUse(sources, 'pa.Use')
                    def expected = a == 'm' ? firstStar('m', b, c) : 'pa.X'
                    if (actual != expected) {
                        failures << "pa=$a pb=$b pc=$c expected $expected but was $actual"
                    }
                }
            }
        }
        assert failures.isEmpty() : failures.join('\n')
    }

    /**
     * A source star import shadows the language defaults, including
     * {@code java.lang}. The use class names {@code String}. pb.String and
     * pc.String are missing, public, or package-private. The first public
     * one is bound; a package-private one is skipped, and with none the
     * name is {@code java.lang.String}.
     * <p>
     * An explicit {@code import java.lang.*} is a source star too. Written
     * before {@code pb.*} it keeps {@code java.lang.String}; written after
     * a public pb.String it loses.
     */
    // GROOVY-11916
    @Test
    void testDeclaredStarShadowsLanguageDefaults() {
        def failures = []
        for (String b : ['m', 'p', 'k']) {
            for (String c : ['m', 'p', 'k']) {
                def sources = [:]
                putType(sources, 'pb', 'String', b)
                putType(sources, 'pc', 'String', c)
                sources['user/Use'] = useSource('user', 'import pb.*\nimport pc.*', 'String')
                def actual = classifyUse(sources, 'user.Use')
                def expected = b == 'p' ? 'pb.String' : c == 'p' ? 'pc.String' : 'java.lang.String'
                if (actual != expected) {
                    failures << "pb=$b pc=$c expected $expected but was $actual"
                }
            }
        }
        def sources = [:]
        putType(sources, 'pb', 'String', 'p')
        sources['user/Use'] = useSource('user', 'import java.lang.*\nimport pb.*', 'String')
        def explicitLang = classifyUse(sources, 'user.Use')
        if (explicitLang != 'java.lang.String') {
            failures << "source java.lang.* before pb.* expected java.lang.String but was $explicitLang"
        }
        sources = [:]
        putType(sources, 'pb', 'String', 'p')
        sources['user/Use'] = useSource('user', 'import pb.*\nimport java.lang.*', 'String')
        def userFirst = classifyUse(sources, 'user.Use')
        if (userFirst != 'pb.String') {
            failures << "pb.* before source java.lang.* expected pb.String but was $userFirst"
        }
        sources = ['user/Use': useSource('user', 'import java.lang.*', 'String')]
        def onlyLang = classifyUse(sources, 'user.Use')
        if (onlyLang != 'java.lang.String') {
            failures << "explicit java.lang.* expected java.lang.String but was $onlyLang"
        }
        assert failures.isEmpty() : failures.join('\n')
    }

    // GROOVY-11916
    @Test
    void testStaticOnDemandUsesSourceOrder() {
        def holder = '''
            package pb
            class Holder {
                static class X {}
                static class String {}
                static class Date {}
                @groovy.transform.PackageScope
                static class Hidden {}
            }
        '''
        def other = '''
            package pc
            class Other {
                static class X {}
            }
            class X {}
        '''
        def two = classifyUse([
            'pb/Holder': holder,
            'pc/Other' : other,
            'user/Use' : useSource('user', 'import static pb.Holder.*\nimport static pc.Other.*', 'X')
        ], 'user.Use')
        assert two == 'pb.Holder$X' : two

        def reversed = classifyUse([
            'pb/Holder': holder,
            'pc/Other' : other,
            'user/Use' : useSource('user', 'import static pc.Other.*\nimport static pb.Holder.*', 'X')
        ], 'user.Use')
        assert reversed == 'pc.Other$X' : reversed

        def withTop = classifyUse([
            'pb/Holder': holder,
            'pc/Other' : other,
            'user/Use' : useSource('user', 'import static pb.Holder.*\nimport pc.*', 'X')
        ], 'user.Use')
        assert withTop == 'pb.Holder$X' : withTop

        def typeFirst = classifyUse([
            'pb/Holder': holder,
            'pc/Other' : other,
            'user/Use' : useSource('user', 'import pc.*\nimport static pb.Holder.*', 'X')
        ], 'user.Use')
        assert typeFirst == 'pc.X' : typeFirst

        def withLang = classifyUse([
            'pb/Holder': holder,
            'user/Use' : useSource('user', 'import static pb.Holder.*', 'String')
        ], 'user.Use')
        assert withLang == 'pb.Holder$String' : withLang

        def hidden = classifyUse([
            'pb/Holder': holder,
            'user/Use' : useSource('user', 'import static pb.Holder.*', 'Hidden')
        ], 'user.Use')
        assert hidden == 'unresolved' : hidden

        def date = classifyUse([
            'pb/Holder': holder,
            'user/Use' : useSource('user', 'import static pb.Holder.*', 'Date')
        ], 'user.Use')
        assert date == 'pb.Holder$Date' : date

        def once = classifyUse([
            'pb/Holder': holder,
            'user/Use' : useSource('user', 'import static pb.Holder.*\nimport pb.Holder.*', 'X')
        ], 'user.Use')
        assert once == 'pb.Holder$X' : once
    }

    // GROOVY-11916
    @Test
    void testExtraDefaultImportsStayBelowDeclaredStars() {
        def sql = classifyUse([
            'user/Use': useSource('user', 'import java.sql.*', 'Date')
        ], 'user.Use')
        assert sql == 'java.sql.Date' : sql

        def awt = classifyUse([
            'user/Use': useSource('user', 'import java.awt.*', 'List')
        ], 'user.Use')
        assert awt == 'java.awt.List' : awt

        def both = classifyUse([
            'user/Use': useSource('user', 'import java.util.*\nimport java.sql.*', 'Date')
        ], 'user.Use')
        assert both == 'java.util.Date' : both

        def bothRev = classifyUse([
            'user/Use': useSource('user', 'import java.sql.*\nimport java.util.*', 'Date')
        ], 'user.Use')
        assert bothRev == 'java.sql.Date' : bothRev

        def tim = classifyUse([
            'pb/TimSort': 'package pb\nclass TimSort {}',
            'user/Use'  : useSource('user', 'import java.util.*\nimport pb.*', 'TimSort')
        ], 'user.Use')
        assert tim == 'pb.TimSort' : tim

        def timRev = classifyUse([
            'pb/TimSort': 'package pb\nclass TimSort {}',
            'user/Use'  : useSource('user', 'import pb.*\nimport java.util.*', 'TimSort')
        ], 'user.Use')
        assert timRev == 'pb.TimSort' : timRev

        def none = classifyUse([
            'pb/TimSort': 'package pb\n@groovy.transform.PackageScope\nclass TimSort {}',
            'user/Use'  : useSource('user', 'import java.util.*\nimport pb.*', 'TimSort')
        ], 'user.Use')
        assert none == 'unresolved' : none
    }

    // GROOVY-11916
    @Test
    void testSingleTypeImportShadowsOnDemand() {
        def sources = [:]
        putType(sources, 'pb', 'X', 'p')
        putType(sources, 'pc', 'X', 'p')
        sources['user/Use'] = useSource('user', 'import pb.X\nimport pc.*', 'X')
        assert classifyUse(sources, 'user.Use') == 'pb.X'

        sources = [:]
        putType(sources, 'pb', 'String', 'p')
        sources['user/Use'] = useSource('user', 'import java.lang.String\nimport pb.*', 'String')
        assert classifyUse(sources, 'user.Use') == 'java.lang.String'
    }

    // GROOVY-11916
    @Test
    void testModuleImportFollowsSourceOrder() {
        def string = classifyUse([
            'user/Use': '''
                package user
                import module java.base
                class Use {
                    String field
                    static Class which() { String }
                }
            '''
        ], 'user.Use')
        assert string == 'java.lang.String' : string

        def list = classifyUse([
            'user/Use': '''
                package user
                import module java.base
                class Use {
                    List field
                    static Class which() { List }
                }
            '''
        ], 'user.Use')
        assert list == 'java.util.List' : list

        def awt = classifyUse([
            'user/Use': '''
                package user
                import module java.desktop
                class Use {
                    List field
                    static Class which() { List }
                }
            '''
        ], 'user.Use')
        assert awt == 'java.awt.List' : awt

        // The module import is earlier, so java.awt.List wins over the later star.
        def moduleFirst = classifyUse([
            'user/Use': '''
                package user
                import module java.desktop
                import java.util.*
                class Use {
                    List field
                    static Class which() { List }
                }
            '''
        ], 'user.Use')
        assert moduleFirst == 'java.awt.List' : moduleFirst

        def starFirst = classifyUse([
            'user/Use': '''
                package user
                import java.util.*
                import module java.desktop
                class Use {
                    List field
                    static Class which() { List }
                }
            '''
        ], 'user.Use')
        assert starFirst == 'java.util.List' : starFirst
    }

    // GROOVY-11916
    @Test
    void testImportCustomizerStarStaysBelowDeclaredImports() {
        def strings = [
            'pb/String': 'package pb\nclass String {}',
            'pc/String': 'package pc\nclass String {}'
        ]
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ImportCustomizer().addStarImports('pb'))

        def fromCustomizer = classifyUse(strings + [
            'user/Use': useSource('user', '', 'String')
        ], 'user.Use', config)
        assert fromCustomizer == 'pb.String' : fromCustomizer

        def userStar = classifyUse(strings + [
            'user/Use': useSource('user', 'import pc.*', 'String')
        ], 'user.Use', config)
        assert userStar == 'pc.String' : userStar

        def userSingle = classifyUse(strings + [
            'user/Use': useSource('user', 'import pc.String', 'String')
        ], 'user.Use', config)
        assert userSingle == 'pc.String' : userSingle

        def hidden = classifyUse([
            'pb/Hidden': 'package pb\n@groovy.transform.PackageScope\nclass Hidden {}',
            'user/Use' : useSource('user', '', 'Hidden')
        ], 'user.Use', config)
        assert hidden == 'unresolved' : hidden

        def visible = classifyUse([
            'pb/Visible': 'package pb\nclass Visible {}',
            'user/Use'  : useSource('user', '', 'Visible')
        ], 'user.Use', config)
        assert visible == 'pb.Visible' : visible

        def sqlFirst = new CompilerConfiguration()
        sqlFirst.addCompilationCustomizers(new ImportCustomizer().addStarImports('java.sql', 'java.util'))
        def sqlDate = classifyUse([
            'user/Use': useSource('user', '', 'Date')
        ], 'user.Use', sqlFirst)
        assert sqlDate == 'java.sql.Date' : sqlDate

        def qStar = new CompilerConfiguration()
        qStar.addCompilationCustomizers(new ImportCustomizer().addStarImports('q'))
        def userHidesInaccessible = classifyUse([
            'p/Collision': 'package p\n@groovy.transform.PackageScope\nclass Collision {}',
            'q/Collision': 'package q\nclass Collision {}',
            'user/Use'   : useSource('user', 'import p.*', 'Collision')
        ], 'user.Use', qStar)
        assert userHidesInaccessible == 'q.Collision' : userHidesInaccessible
    }
}
