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
package org.apache.groovy.jpms

import groovy.test.GroovyAssert
import org.apache.groovy.ast.ModuleInfoNode
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes

import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.nio.file.Files
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

import static groovy.test.GroovyAssert.assertScript
import static java.lang.module.ModuleDescriptor.Requires.Modifier.MANDATED
import static java.lang.module.ModuleDescriptor.Requires.Modifier.STATIC
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE

/**
 * JPMS module declarations (JLS §7.7): parse, semantic checks, {@code module-info.class},
 * and interop with the JDK module system. {@code --add-exports}, {@code --add-opens},
 * {@code --add-reads}, and the manifest attributes {@code Add-Exports} / {@code Add-Opens}
 * are JVM launch forms of the same directives; the tests assert the class-file form.
 */
final class ModuleInfoTest {

    @TempDir
    File tmp

    // GROOVY-9273, GROOVY-9668
    @Test
    void testEveryDirectiveAndAstShape() {
        def unit = compile(dir('ast'), [
            'module-info.groovy': '''
                import java.util.ServiceLoader
                import java.sql.Driver
                /* block */
                @Deprecated
                open module demo.ast {
                    requires transitive static java.sql; // comment
                    requires static java.compiler;
                    requires java.logging;
                    exports demo.ast.api to java.logging, java.sql;
                    exports demo.ast.pub;
                    // an open module has no opens directives; every package is reflective
                    uses Driver;
                    uses java.util.ServiceLoader;
                    provides java.lang.Runnable with demo.ast.api.Task, demo.ast.api.Other;
                }
            ''',
            'Api.groovy': '''
                package demo.ast.api
                class Task implements Runnable { void run() {} }
                class Other implements Runnable {
                    Other() {}
                    void run() {}
                }
            ''',
            'Pub.groovy': '''
                package demo.ast.pub
                class Pub {}
            '''
        ])
        def cn = unit.ast.classes.find { it.name == 'module-info' }
        def decl = ModuleInfoNode.of(cn)
        assert decl.name == 'demo.ast'
        assert decl.open
        assert decl.directives.size() == decl.requires.size() + decl.exports.size() + decl.uses.size() + decl.provides.size()
        assert decl.annotations*.classNode*.name.contains('java.lang.Deprecated')
        assert decl.requires.collect { [it.moduleName, it.transitive, it.staticPhase] } == [
            ['java.sql', true, true],
            ['java.compiler', false, true],
            ['java.logging', false, false]
        ]
        assert decl.exports[0].qualified
        assert decl.exports[0].packageName == 'demo.ast.api'
        assert decl.exports[0].targets == ['java.logging', 'java.sql']
        assert !decl.exports[1].qualified
        assert decl.exports[1].packageName == 'demo.ast.pub'
        assert decl.opens.empty
        assert decl.uses*.serviceName == ['Driver', 'java.util.ServiceLoader']
        assert decl.uses[0].resolvedType.name == 'java.sql.Driver'
        assert decl.provides[0].providerNames == ['demo.ast.api.Task', 'demo.ast.api.Other']
        assert decl.provides[0].resolvedService.name == 'java.lang.Runnable'
        assert decl.provides[0].resolvedProviders*.name == ['demo.ast.api.Task', 'demo.ast.api.Other']

        def md = descriptor(unit)
        assert md.open
        assert md.name() == 'demo.ast'
        def sql = require(md, 'java.sql')
        assert TRANSITIVE in sql.modifiers()
        assert STATIC in sql.modifiers()
        assert STATIC in require(md, 'java.compiler').modifiers()
        assert require(md, 'java.logging').modifiers().empty
        // explicit requires java.base is absent, so the mandated edge is added first
        assert MANDATED in require(md, 'java.base').modifiers()
        assert require(md, 'java.base').rawCompiledVersion().isPresent()
        assert requireOrder(unit)[0] == 'java.base'
        assert md.exports().size() == 2
        assert md.exports().find { it.source() == 'demo.ast.api' }.targets() == ['java.logging', 'java.sql'] as Set
        assert md.exports().find { it.source() == 'demo.ast.pub' }.targets().empty
        assert md.opens().empty
        assert md.uses() == ['java.sql.Driver', 'java.util.ServiceLoader'] as Set
        assert md.provides().find { it.service() == 'java.lang.Runnable' }.providers() as List ==
            ['demo.ast.api.Task', 'demo.ast.api.Other']
        assert new String(unit.classes.find { it.name == 'module-info' }.bytes).contains('Deprecated') ||
            descriptorHasDeprecated(unit)
    }

    @Test
    void testNormalModuleExportsOpensConcealedAndExplicitJavaBase() {
        def unit = compile(dir('normal'), [
            'module-info.groovy': '''
                module demo.normal {
                    requires java.base;
                    requires static transitive java.sql;
                    exports demo.normal.api;
                    opens demo.normal.opened to java.logging;
                    opens demo.not.declared;
                }
            ''',
            'A.groovy': 'package demo.normal.api; class A {}',
            'O.groovy': 'package demo.normal.opened; class O {}',
            'H.groovy': 'package demo.normal.hidden; class H {}',
            'Loose.groovy': 'class Loose {}'
        ])
        def md = descriptor(unit)
        assert !md.open
        def base = require(md, 'java.base')
        assert !(MANDATED in base.modifiers())
        def sql = require(md, 'java.sql')
        assert TRANSITIVE in sql.modifiers() && STATIC in sql.modifiers()
        assert md.exports()*.source() == ['demo.normal.api']
        assert md.opens()*.source() as Set == ['demo.normal.opened', 'demo.not.declared'] as Set
        assert md.packages().contains('demo.normal.hidden')
        assert !md.exports()*.source().contains('demo.normal.hidden')
        assert !md.opens()*.source().contains('demo.normal.hidden')
        def warnings = unit.errorCollector.warnings ?: []
        assert warnings.any { it.message.contains('package is empty or does not exist: demo.not.declared') }
        assert !warnings.any { it.message.contains('demo.normal.opened') || it.message.contains('demo.normal.hidden') }
    }

    @Test
    void testModuleInfoJavaFileNameAndImports() {
        def unit = compile(dir('java-name'), [
            'module-info.java': '''
                import java.sql.Driver;
                module demo.jname {
                    requires java.sql;
                    uses Driver;
                }
            '''
        ])
        assert descriptor(unit).uses() == ['java.sql.Driver'] as Set
    }

    @Test
    void testImportModuleInsideModuleInfo() {
        // GROOVY-11896 still expands import module; the simple name is then legal in uses.
        def unit = compile(dir('impmod'), [
            'module-info.groovy': '''
                import module java.sql
                module demo.imp {
                    requires java.sql;
                    uses Driver;
                }
            '''
        ])
        assert descriptor(unit).uses() == ['java.sql.Driver'] as Set
    }

    @Test
    void testStaticRequiresStillReadableAtCompileTime() {
        compile(dir('static-read'), [
            'module-info.groovy': '''
                module demo.staticread {
                    requires static java.sql;
                    uses java.sql.Driver;
                }
            '''
        ])
    }

    @Test
    void testTransitiveReadabilityAcrossModules() {
        def mods = dir('mods-trans')
        compile(new File(mods, 'demo.mid'), [
            'module-info.groovy': '''
                module demo.mid {
                    requires transitive java.sql;
                }
            '''
        ])
        // demo.app does not require java.sql itself; it reads Driver through demo.mid.
        compile(new File(mods, 'demo.app'), mods, [
            'module-info.groovy': '''
                module demo.app {
                    requires demo.mid;
                    uses java.sql.Driver;
                }
            '''
        ])
        def finder = ModuleFinder.of(mods.toPath())
        def cf = Configuration.resolve(finder, [ModuleLayer.boot().configuration()], ModuleFinder.of(), ['demo.app'] as Set)
        def reads = cf.findModule('demo.app').get().reads()*.name() as Set
        assert 'demo.mid' in reads
        assert 'java.sql' in reads
    }

    @Test
    void testNonTransitiveDependencyDoesNotExportReadability() {
        def mods = dir('mods-direct')
        compile(new File(mods, 'demo.direct'), [
            'module-info.groovy': '''
                module demo.direct {
                    requires java.sql;
                }
            '''
        ])
        def err = fail(new File(mods, 'demo.client'), mods, [
            'module-info.groovy': '''
                module demo.client {
                    requires demo.direct;
                    uses java.sql.Driver;
                }
            '''
        ])
        assert err.contains('not accessible')
    }

    @Test
    void testAutomaticModuleAndUnnamedModule() {
        def autoClasses = dir('auto-classes')
        compile(autoClasses, [
            'T.groovy': 'package demo.auto; class AutoType {}'
        ])
        def autoJar = new File(tmp, 'demo-auto.jar')
        packJar(autoJar, 'demo.auto', autoClasses)

        compile(dir('needs-auto'), autoJar, [
            'module-info.groovy': '''
                module demo.needsauto {
                    requires demo.auto;
                    requires java.sql;
                    uses demo.auto.AutoType;
                    uses java.sql.Driver;
                }
            '''
        ])

        // Driver's package is not in the automatic module, and java.sql is not read.
        def missed = fail(dir('auto-miss'), autoJar, [
            'module-info.groovy': '''
                module demo.automiss {
                    requires demo.auto;
                    uses java.sql.Driver;
                }
            '''
        ])
        assert missed.contains('not accessible')

        // A directory of class files is not a named or automatic module (unnamed module).
        // A named module cannot requires it.
        def loose = dir('loose')
        compile(loose, ['U.groovy': 'package loose.pkg; class U {}'])
        def err = fail(dir('needs-loose'), loose, [
            'module-info.groovy': '''
                module demo.needsloose {
                    requires loose.pkg;
                }
            '''
        ])
        assert err.contains('not found') || err.contains('loose.pkg')
    }

    @Test
    void testQualifiedExportIsNotAccessibleAndFriendIs() {
        def secretClasses = dir('secret-classes')
        compile(secretClasses, [
            'S.groovy': 'package secret.pkg; class SecretType {}'
        ])
        // Hand-built descriptors: one qualified away from us, one qualified to us.
        def away = new File(tmp, 'secret-away.jar')
        moduleJar(away, 'secret.away', 'secret.pkg', 'other.mod', secretClasses)
        def toward = new File(tmp, 'secret-toward.jar')
        moduleJar(toward, 'secret.toward', 'secret.pkg', 'demo.friend', secretClasses)

        def denied = fail(dir('denied'), away, [
            'module-info.groovy': '''
                module demo.friend {
                    requires secret.away;
                    uses secret.pkg.SecretType;
                }
            '''
        ])
        assert denied.contains('not accessible')

        compile(dir('allowed'), toward, [
            'module-info.groovy': '''
                module demo.friend {
                    requires secret.toward;
                    uses secret.pkg.SecretType;
                }
            '''
        ])
    }

    @Test
    void testAccessMatrixAndCliDescribe() {
        def mods = dir('mods-access')
        compile(new File(mods, 'demo.lib'), [
            'module-info.groovy': '''
                module demo.lib {
                    exports demo.lib.api;
                    exports demo.lib.friends to demo.friend;
                    opens demo.lib.opened to demo.friend;
                }
            ''',
            'Api.groovy': 'package demo.lib.api; class Api {}',
            'Fr.groovy': 'package demo.lib.friends; class Fr {}',
            'Op.groovy': 'package demo.lib.opened; class Op {}',
            'Hi.groovy': 'package demo.lib.hidden; class Hi {}'
        ])
        compile(new File(mods, 'demo.friend'), mods, [
            'module-info.groovy': 'module demo.friend { requires demo.lib; }'
        ])
        compile(new File(mods, 'demo.stranger'), mods, [
            'module-info.groovy': 'module demo.stranger { requires demo.lib; }'
        ])
        compile(new File(mods, 'demo.open'), [
            'module-info.groovy': '''
                open module demo.open {
                    exports demo.open.api;
                }
            ''',
            'Api.groovy': 'package demo.open.api; class Api {}',
            'Hi.groovy': 'package demo.open.hidden; class Hi {}'
        ])
        compile(new File(mods, 'demo.reader'), mods, [
            'module-info.groovy': 'module demo.reader { requires demo.open; }'
        ])

        def finder = ModuleFinder.of(mods.toPath())
        def cf = ModuleLayer.boot().configuration().resolve(finder, ModuleFinder.of(),
            ['demo.lib', 'demo.friend', 'demo.stranger', 'demo.open', 'demo.reader'] as Set)
        def layer = ModuleLayer.boot().defineModulesWithOneLoader(cf, ClassLoader.systemClassLoader)
        def lib = layer.findModule('demo.lib').get()
        def friend = layer.findModule('demo.friend').get()
        def stranger = layer.findModule('demo.stranger').get()
        def open = layer.findModule('demo.open').get()
        def reader = layer.findModule('demo.reader').get()

        // Export: compile-time (isExported) to every reader, public members only.
        assert lib.isExported('demo.lib.api', friend)
        assert lib.isExported('demo.lib.api', stranger)
        // Qualified export: only the friend.
        assert lib.isExported('demo.lib.friends', friend)
        assert !lib.isExported('demo.lib.friends', stranger)
        // Open package: not in the exports table (no compile-time access). The JVM's
        // isExported(pkg, friend) is also true for a package opened to that friend,
        // so the descriptor is the compile-time record and isOpen is the reflective one.
        def libDescriptor = lib.descriptor
        assert libDescriptor.exports()*.source().contains('demo.lib.api')
        assert !libDescriptor.exports()*.source().contains('demo.lib.opened')
        assert !libDescriptor.exports()*.source().contains('demo.lib.hidden')
        assert libDescriptor.opens()*.source().contains('demo.lib.opened')
        assert !libDescriptor.opens()*.source().contains('demo.lib.hidden')
        assert lib.isOpen('demo.lib.opened', friend)
        assert !lib.isOpen('demo.lib.opened', stranger)
        // Default (neither exported nor opened): no compile or reflective access.
        assert !lib.isExported('demo.lib.hidden', friend)
        assert !lib.isOpen('demo.lib.hidden', friend)
        // Open module: the descriptor exports only the named packages (compile-time access).
        // Every package is reflective (isOpen). The JVM also reports isExported for those
        // packages because an open module opens them to all readers.
        def openDescriptor = open.descriptor
        assert openDescriptor.open
        assert openDescriptor.exports()*.source().contains('demo.open.api')
        assert !openDescriptor.exports()*.source().contains('demo.open.hidden')
        assert open.isOpen('demo.open.hidden', reader)
        assert open.isOpen('demo.open.api', reader)

        def described = exec(javaBin(), '--module-path', mods.absolutePath, '--describe-module', 'demo.lib')
        assert described.contains('exports demo.lib.api')
        assert described.contains('exports demo.lib.friends to demo.friend')
        assert described.contains('opens demo.lib.opened to demo.friend')
        assert described.contains('requires java.base')

        def jdeps = exec(jdepsBin(), '--module-path', mods.absolutePath, '-m', 'demo.lib')
        assert jdeps.contains('demo.lib')
        assert jdeps.contains('java.base')
    }

    @Test
    void testServiceLoaderProvides() {
        def mods = dir('mods-svc')
        compile(new File(mods, 'demo.svc'), [
            'module-info.groovy': '''
                module demo.svc {
                    exports demo.svc;
                    provides demo.svc.Greeting with demo.svc.Hello;
                    uses demo.svc.Greeting;
                }
            ''',
            'Greeting.groovy': '''
                package demo.svc
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                interface Greeting { String hello() }
            ''',
            'Hello.groovy': '''
                package demo.svc
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                class Hello implements Greeting {
                    String hello() { 'hi' }
                }
            '''
        ])
        def finder = ModuleFinder.of(mods.toPath())
        def cf = ModuleLayer.boot().configuration().resolve(finder, ModuleFinder.of(), ['demo.svc'] as Set)
        def layer = ModuleLayer.boot().defineModulesWithOneLoader(cf, ClassLoader.systemClassLoader)
        def svc = layer.findLoader('demo.svc').loadClass('demo.svc.Greeting')
        def loaded = ServiceLoader.load(layer, svc).iterator()
        assert loaded.hasNext()
        assert loaded.next().hello() == 'hi'
    }

    @Test
    void testProviderForms() {
        compile(dir('providers'), [
            'module-info.groovy': '''
                import java.lang.Runnable
                module demo.prov {
                    exports demo.prov;
                    provides Runnable with demo.prov.Top, demo.prov.Outer.Inner, demo.prov.Factory, demo.prov.E, demo.prov.LooksLikeProvider, demo.prov.OnlyPrivate, demo.prov.Outer2.Mid.Deep;
                }
            ''',
            'Types.groovy': '''
                package demo.prov
                class Top implements Runnable { void run() {} }
                class Outer {
                    static class Inner implements Runnable { void run() {} }
                }
                class Factory {
                    static Runnable provider() { new Top() }
                }
                class LooksLikeProvider implements Runnable {
                    void run() {}
                    Runnable provider() { null }
                    static Runnable provider(String ignored) { null }
                }
                class OnlyPrivate implements Runnable {
                    void run() {}
                    private static Runnable provider() { null }
                }
                class Outer2 {
                    static class Mid {
                        static class Deep implements Runnable { void run() {} }
                    }
                }
                enum E implements Runnable {
                    INST
                    void run() {}
                    static Runnable provider() { INST }
                }
            '''
        ])
    }

    @Test
    void testNegativeRequires() {
        assert fail(dir('dup-req'), ['module-info.groovy': '''
            module demo.dup { requires java.sql; requires java.sql; }
        ''']).contains('duplicate requires on module java.sql')

        assert fail(dir('self'), ['module-info.groovy': '''
            module demo.self { requires demo.self; }
        ''']).contains('requires itself')

        assert fail(dir('base-mod'), ['module-info.groovy': '''
            module demo.base { requires transitive java.base; }
        ''']).contains('requires java.base must not have modifiers')

        assert fail(dir('base-static'), ['module-info.groovy': '''
            module demo.base2 { requires static java.base; }
        ''']).contains('requires java.base must not have modifiers')

        assert fail(dir('primordial'), ['module-info.groovy': '''
            module java.base { requires java.sql; }
        ''']).contains('not allowed in module java.base')

        // No requires clauses: java.base is not given an implicit requires edge.
        def primordial = compile(dir('primordial-empty'), ['module-info.groovy': '''
            module java.base { }
        '''])
        assert descriptor(primordial).name() == 'java.base'
        assert descriptor(primordial).requires().empty

        assert fail(dir('missing'), ['module-info.groovy': '''
            module demo.missing { requires no.such.module; }
        ''']).toLowerCase().contains('not found')

        assert fail(dir('twice-static'), ['module-info.groovy': '''
            module demo.m { requires static static java.sql; }
        ''']).contains("modifier 'static'")

        assert fail(dir('twice-trans'), ['module-info.groovy': '''
            module demo.m { requires transitive transitive java.sql; }
        ''']).contains("modifier 'transitive'")

        // A lone "transitive" is the module name, not a modifier (javac).
        def named = fail(dir('mod-named-transitive'), ['module-info.groovy': '''
            module demo.m { requires transitive; }
        '''])
        assert named.toLowerCase().contains('not found')
        assert !named.contains("modifier 'transitive'")

        def namedAgain = fail(dir('trans-of-transitive'), ['module-info.groovy': '''
            module demo.m { requires transitive transitive; }
        '''])
        assert namedAgain.toLowerCase().contains('not found')
        assert !namedAgain.contains("modifier 'transitive'")

        def dotted = fail(dir('dotted-transitive'), ['module-info.groovy': '''
            module demo.m { requires transitive.extra; }
        '''])
        assert dotted.toLowerCase().contains('not found')
        assert !dotted.contains("modifier 'transitive'")

        def broken = fail(dir('trans-nl'), ['module-info.groovy': '''
            module demo.m {
                requires
                    transitive;
            }
        '''])
        assert broken.toLowerCase().contains('not found')
        assert !broken.contains("modifier 'transitive'")

        // requires static is optional during resolution but still must be observable.
        assert fail(dir('static-missing'), ['module-info.groovy': '''
            module demo.m { requires static no.such.mod; }
        ''']).toLowerCase().contains('not found')

        assert fail(dir('static-named-transitive'), ['module-info.groovy': '''
            module demo.m { requires static transitive; }
        ''']).toLowerCase().contains('not found')
    }

    @Test
    void testCycle() {
        def jar = new File(tmp, 'cycle-b.jar')
        moduleJarRequires(jar, 'cycle.b', 'cycle.a')
        def err = fail(dir('cycle-a'), jar, [
            'module-info.groovy': '''
                module cycle.a { requires cycle.b; }
            '''
        ])
        assert err.toLowerCase().contains('cycle')
    }

    @Test
    void testNegativePackagesAndServices() {
        assert fail(dir('dup-ex'), [
            'module-info.groovy': '''
                module demo.p {
                    exports demo.p.a;
                    exports demo.p.a to java.logging;
                }
            ''',
            'A.groovy': 'package demo.p.a; class A {}'
        ]).contains('duplicate exports of package demo.p.a')

        assert fail(dir('missing-pkg'), [
            'module-info.groovy': '''
                module demo.p { exports demo.missing; }
            '''
        ]).contains('package is empty or does not exist: demo.missing')

        assert fail(dir('dup-to'), [
            'module-info.groovy': '''
                module demo.p { exports demo.p.a to java.sql, java.sql; }
            ''',
            'A.groovy': 'package demo.p.a; class A {}'
        ]).contains('duplicate module java.sql in to clause')

        assert fail(dir('dup-open'), [
            'module-info.groovy': '''
                module demo.p { opens demo.p.a; opens demo.p.a; }
            ''',
            'A.groovy': 'package demo.p.a; class A {}'
        ]).contains('duplicate opens of package demo.p.a')

        assert fail(dir('open-opens'), [
            'module-info.groovy': '''
                open module demo.p { opens demo.p.a; }
            ''',
            'A.groovy': 'package demo.p.a; class A {}'
        ]).contains('not allowed in an open module')

        assert fail(dir('enum-uses'), [
            'module-info.groovy': '''
                module demo.p { uses java.time.Month; }
            '''
        ]).contains('enum class')

        // An enum service is legal when provider() returns it; a plain class is not a subtype.
        assert fail(dir('enum-provides'), [
            'module-info.groovy': '''
                module demo.p { provides java.time.Month with demo.p.A; }
            ''',
            'A.groovy': 'package demo.p; class A {}'
        ]).contains('must be a subtype')

        compile(dir('enum-factory'), [
            'module-info.groovy': '''
                module demo.p { provides java.time.Month with demo.p.Factory; }
            ''',
            'F.groovy': '''
                package demo.p
                class Factory {
                    static java.time.Month provider() { java.time.Month.JANUARY }
                }
            '''
        ])

        assert fail(dir('dup-uses'), [
            'module-info.groovy': '''
                import java.lang.Runnable
                module demo.p { uses Runnable; uses java.lang.Runnable; }
            '''
        ]).contains('duplicate uses')

        assert fail(dir('unresolved'), [
            'module-info.groovy': '''
                module demo.p { uses missing.NoSuch; }
            '''
        ]).contains('unable to resolve class missing.NoSuch')

        assert fail(dir('simple-miss'), [
            'module-info.groovy': '''
                module demo.p { uses Driver; }
            '''
        ]).contains('unable to resolve class Driver')

        assert fail(dir('ambig'), [
            'module-info.groovy': '''
                import java.util.*
                import java.awt.*
                module demo.p { uses List; }
            '''
        ]).contains('ambiguous')

        assert fail(dir('no-read'), [
            'module-info.groovy': '''
                module demo.p { uses java.sql.Driver; }
            '''
        ]).contains('not accessible')

        assert fail(dir('no-read-prov'), [
            'module-info.groovy': '''
                module demo.p { provides java.sql.Driver with demo.p.A; }
            ''',
            'A.groovy': 'package demo.p; class A {}'
        ]).contains('not accessible')

        assert fail(dir('missing-svc'), [
            'module-info.groovy': '''
                module demo.p { provides missing.NoSuch with demo.p.A; }
            ''',
            'A.groovy': 'package demo.p; class A {}'
        ]).contains('unable to resolve class missing.NoSuch')

        compile(dir('uses-string'), [
            'module-info.groovy': '''
                module demo.str { uses String; }
            '''
        ])
        compile(dir('java-lang-star-ok'), [
            'module-info.groovy': '''
                import java.lang.*
                module demo.langstar { uses Object; }
            '''
        ])

        assert fail(dir('dup-prov'), [
            'module-info.groovy': '''
                module demo.p {
                    provides java.lang.Runnable with demo.p.A;
                    provides java.lang.Runnable with demo.p.A;
                }
            ''',
            'A.groovy': 'package demo.p; class A implements Runnable { void run() {} }'
        ]).contains('duplicate provides')

        assert fail(dir('dup-with'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.A, demo.p.A; }
            ''',
            'A.groovy': 'package demo.p; class A implements Runnable { void run() {} }'
        ]).contains('duplicate provider')

        assert fail(dir('outside'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with java.lang.Thread; }
            '''
        ]).contains('must be declared in the current module')

        // A single-type import wins over a star import of the same simple name.
        def chosen = compile(dir('import-wins'), [
            'module-info.groovy': '''
                import demo.p.one.List
                import demo.p.two.*
                module demo.p { uses List; }
            ''',
            'One.groovy': 'package demo.p.one; interface List {}',
            'Two.groovy': 'package demo.p.two; interface List {}'
        ])
        assert descriptor(chosen).uses() == ['demo.p.one.List'] as Set

        def splits = dir('splits')
        compile(new File(splits, 'split.a'), [
            'module-info.groovy': '''
                module split.a { exports split.shared; }
            ''',
            'A.groovy': 'package split.shared; class A {}'
        ])
        def split = fail(new File(splits, 'split.b'), splits, [
            'module-info.groovy': '''
                module split.b { requires split.a; }
            ''',
            'B.groovy': 'package split.shared; class B {}'
        ])
        assert split.contains('package split.shared exists in another module: split.a')
    }

    @Test
    void testProviderDiagnostics() {
        assert fail(dir('pkg-priv'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Hidden; }
            ''',
            'H.groovy': '''
                package demo.p
                import groovy.transform.PackageScope
                @PackageScope
                class Hidden implements Runnable { void run() {} }
            '''
        ]).contains('must be public')

        assert fail(dir('inner'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Outer.Inner; }
            ''',
            'O.groovy': '''
                package demo.p
                class Outer {
                    class Inner implements Runnable { void run() {} }
                }
            '''
        ]).contains('top-level or static')

        assert fail(dir('deep'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Outer.Mid.Deep; }
            ''',
            'D.groovy': '''
                package demo.p
                class Outer {
                    class Mid {
                        static class Deep implements Runnable { void run() {} }
                    }
                }
            '''
        ]).contains('top-level or static')

        assert fail(dir('anon'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Outer$1; }
            ''',
            'O.groovy': '''
                package demo.p
                class Outer {
                    Runnable r = new Runnable() { void run() {} }
                }
            '''
        ]).contains('top-level or static')

        assert fail(dir('iface'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.P; }
            ''',
            'P.groovy': 'package demo.p; interface P extends Runnable {}'
        ]).contains('public no-arg constructor')

        assert fail(dir('abs'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.A; }
            ''',
            'A.groovy': 'package demo.p; abstract class A implements Runnable { void run() {} }'
        ]).contains('public no-arg constructor')

        assert fail(dir('en'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.E; }
            ''',
            'E.groovy': 'package demo.p; enum E implements Runnable { INST; void run() {} }'
        ]).contains('public no-arg constructor')

        assert fail(dir('args'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Needs; }
            ''',
            'N.groovy': '''
                package demo.p
                class Needs implements Runnable {
                    Needs(String s) {}
                    void run() {}
                }
            '''
        ]).contains('public no-arg constructor')

        assert fail(dir('priv-ctor'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Priv; }
            ''',
            'P.groovy': '''
                package demo.p
                class Priv implements Runnable {
                    private Priv() {}
                    void run() {}
                }
            '''
        ]).contains('public no-arg constructor')

        assert fail(dir('not-sub'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Nope; }
            ''',
            'N.groovy': 'package demo.p; class Nope {}'
        ]).contains('must be a subtype')

        assert fail(dir('bad-ret'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Factory; }
            ''',
            'F.groovy': '''
                package demo.p
                class Factory {
                    static String provider() { '' }
                }
            '''
        ]).contains('must return a subtype')

        assert fail(dir('ret-access'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Factory; }
            ''',
            'F.groovy': '''
                package demo.p
                import groovy.transform.PackageScope
                class Factory {
                    static Hidden provider() { null }
                }
                @PackageScope
                class Hidden implements Runnable { void run() {} }
            '''
        ]).contains('not accessible')

        assert fail(dir('bad-prov'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Missing; }
            '''
        ]).contains('unable to resolve class')
    }

    @Test
    void testModuleAnnotations() {
        compile(dir('anno-ok'), [
            'module-info.groovy': '''
                import demo.anno.Mark
                @Mark
                module demo.anno { exports demo.anno; }
            ''',
            'Mark.groovy': '''
                package demo.anno
                import java.lang.annotation.ElementType
                import java.lang.annotation.Target
                @Target(ElementType.MODULE)
                @interface Mark {}
            '''
        ])
        assert fail(dir('anno-override'), [
            'module-info.groovy': '''
                @Override
                module demo.bad {}
            '''
        ]).contains('MODULE')

        assert fail(dir('anno-type'), [
            'module-info.groovy': '''
                import demo.anno.TypeOnly
                @TypeOnly
                module demo.anno { exports demo.anno; }
            ''',
            'T.groovy': '''
                package demo.anno
                import java.lang.annotation.ElementType
                import java.lang.annotation.Target
                @Target([ElementType.TYPE])
                @interface TypeOnly {}
            '''
        ]).contains('MODULE')

        def annClasses = dir('ann-classes')
        compile(annClasses, [
            'Mark.groovy': '''
                package demo.foreign
                import java.lang.annotation.ElementType
                import java.lang.annotation.Target
                @Target([ElementType.MODULE, ElementType.TYPE])
                @interface ForeignMark {}
            '''
        ])
        def jar = new File(tmp, 'foreign-ann.jar')
        moduleJar(jar, 'demo.foreign', 'demo.foreign', null, annClasses)
        assert fail(dir('ann-noreq'), jar, [
            'module-info.groovy': '''
                @demo.foreign.ForeignMark
                module demo.consumer {}
            '''
        ]).contains('not accessible')

        compile(dir('ann-req'), jar, [
            'module-info.groovy': '''
                @demo.foreign.ForeignMark
                module demo.consumer { requires demo.foreign; }
            '''
        ])
    }

    @Test
    void testOrdinaryCodeIsUnchanged() {
        // GROOVY-11896
        assertScript '''
            import module java.base
            assert List != null
            def module = 'hello'
            def requires = 1
            def exports = 1
            def opens = 1
            def to = 1
            def uses = 1
            def provides = 1
            def with = 1
            def open = 1
            def transitive = 1
            assert [module, requires, exports, opens, to, uses, provides, with, open, transitive].sum { it instanceof String ? 0 : it } == 9
            assert module.toUpperCase() == 'HELLO'
        '''
        def unit = compile(dir('script'), [
            'Foo.groovy': '''
                module foo { }
            '''
        ])
        assert unit.classes.every { it.name != 'module-info' }
    }

    @Test
    void testSyntaxErrors() {
        assert fail(dir('pkg'), [
            'module-info.groovy': '''
                package demo
                module demo.m {}
            '''
        ]) != null
        assert fail(dir('class'), [
            'module-info.groovy': '''
                class NotAModule {}
            '''
        ]) != null
    }

    @Test
    void testDefaultPackageTypeIsNotAccessibleFromModuleInfo() {
        def loose = dir('def-pkg')
        compile(loose, ['Loose.groovy': 'class Loose {}'])
        def looseJar = new File(tmp, 'loose.jar')
        packJar(looseJar, null, loose)
        def err = fail(dir('uses-loose'), looseJar, [
            'module-info.groovy': '''
                module demo.p { uses Loose; }
            '''
        ])
        assert err.contains('not accessible') || err.contains('unable to resolve')
    }

    private File dir(String name) {
        def file = new File(tmp, name)
        file.mkdirs()
        file
    }

    private CompilationUnit compile(File target, Map<String, String> sources) {
        compile(target, null, sources)
    }

    private CompilationUnit compile(File target, File classpathEntry, Map<String, String> sources) {
        def config = new CompilerConfiguration(targetDirectory: target)
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            if (classpathEntry != null) {
                config.classpath = classpathEntry.absolutePath
                loader.addClasspath(classpathEntry.absolutePath)
            }
            def unit = new CompilationUnit(config, null, loader)
            sources.each { name, text -> unit.addSource(name, text) }
            unit.compile()
            unit
        } finally {
            // GroovyClassLoader is a URLClassLoader: it caches a JarFile for a
            // classpath JAR until close(). On Windows that lock is still held
            // when @TempDir deletes the directory, because GC has not run.
            loader.close()
        }
    }

    private String fail(File target, Map<String, String> sources) {
        fail(target, null, sources)
    }

    private String fail(File target, File classpathEntry, Map<String, String> sources) {
        try {
            compile(target, classpathEntry, sources)
            throw new AssertionError('expected compilation to fail for ' + target.name)
        } catch (CompilationFailedException e) {
            return e.message
        }
    }

    private static ModuleDescriptor descriptor(CompilationUnit unit) {
        def bytes = unit.classes.find { it.name == 'module-info' }.bytes
        ModuleDescriptor.read(new ByteArrayInputStream(bytes))
    }

    private static ModuleDescriptor.Requires require(ModuleDescriptor md, String name) {
        md.requires().find { it.name() == name }
    }

    private static List<String> requireOrder(CompilationUnit unit) {
        def order = []
        new ClassReader(unit.classes.find { it.name == 'module-info' }.bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            ModuleVisitor visitModule(String moduleName, int moduleAccess, String moduleVersion) {
                new ModuleVisitor(Opcodes.ASM9) {
                    @Override
                    void visitRequire(String requiredName, int requireAccess, String requireVersion) {
                        order << requiredName
                    }
                }
            }
        }, 0)
        order
    }

    private static boolean descriptorHasDeprecated(CompilationUnit unit) {
        def text = new String(unit.classes.find { it.name == 'module-info' }.bytes, 'ISO-8859-1')
        text.contains('Ljava/lang/Deprecated;')
    }

    private static void packJar(File jar, String automaticName, File classes) {
        def manifest = new Manifest()
        manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, '1.0')
        if (automaticName != null) {
            manifest.mainAttributes.put(new Attributes.Name('Automatic-Module-Name'), automaticName)
        }
        writeJar(jar, manifest, classes)
    }

    /**
     * Modular jar exporting {@code pkg} (unqualified when {@code friend} is null).
     * {@code classes} holds the class files, including a Groovy {@code module-info.class} that is replaced.
     */
    private static void moduleJar(File jar, String moduleName, String pkg, String friend, File classes) {
        def manifest = new Manifest()
        manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, '1.0')
        def bytes = moduleInfo(moduleName, pkg, friend, null)
        writeJar(jar, manifest, classes, ['module-info.class': bytes])
    }

    private static void moduleJarRequires(File jar, String moduleName, String requires) {
        def manifest = new Manifest()
        manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, '1.0')
        def bytes = moduleInfo(moduleName, null, null, requires)
        writeJar(jar, manifest, null, ['module-info.class': bytes])
    }

    private static byte[] moduleInfo(String moduleName, String exportPackage, String friend, String requires) {
        def cw = new ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_MODULE, 'module-info', null, null, null)
        def mv = cw.visitModule(moduleName, 0, null)
        mv.visitRequire('java.base', Opcodes.ACC_MANDATED, null)
        if (requires != null) {
            mv.visitRequire(requires, 0, null)
        }
        if (exportPackage != null) {
            if (friend == null) {
                mv.visitExport(exportPackage.replace('.', '/'), 0, (String[]) null)
            } else {
                mv.visitExport(exportPackage.replace('.', '/'), 0, friend)
            }
        }
        mv.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static void writeJar(File jar, Manifest manifest, File classes, Map<String, byte[]> extras = [:]) {
        jar.parentFile?.mkdirs()
        new JarOutputStream(new FileOutputStream(jar), manifest).withCloseable { jos ->
            if (classes != null) {
                Files.walk(classes.toPath()).forEach { path ->
                    if (!Files.isRegularFile(path)) {
                        return
                    }
                    def rel = classes.toPath().relativize(path).toString().replace(File.separatorChar, '/' as char)
                    if (rel == 'module-info.class' && extras.containsKey('module-info.class')) {
                        return
                    }
                    jos.putNextEntry(new JarEntry(rel))
                    jos.write(Files.readAllBytes(path))
                    jos.closeEntry()
                }
            }
            extras.each { name, bytes ->
                jos.putNextEntry(new JarEntry(name))
                jos.write(bytes)
                jos.closeEntry()
            }
        }
    }

    private static String exec(String... command) {
        def process = new ProcessBuilder(command).redirectErrorStream(true).start()
        def text = process.inputStream.getText('UTF-8')
        int code = process.waitFor()
        assert code == 0 : "command ${command} failed (${code}):\n${text}"
        text
    }

    private static String javaBin() {
        new File(System.getProperty('java.home'), 'bin/java').absolutePath
    }

    private static String jdepsBin() {
        new File(System.getProperty('java.home'), 'bin/jdeps').absolutePath
    }
}
