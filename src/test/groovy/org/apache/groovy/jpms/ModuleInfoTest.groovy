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
import groovy.transform.CompileStatic
import org.apache.groovy.ast.ModuleInfoNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.codehaus.groovy.tools.javac.JavaStubCompilationUnit
import org.codehaus.groovy.tools.javac.JavaStubGenerator
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
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
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
        def decl = unit.ast.moduleInfo
        assert decl != null
        assert !unit.ast.classes.any { it.name == 'module-info' }
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
            'H.groovy': 'package demo.normal.hidden; class H {}'
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

        // requires transitive java.base is legal at class file version 25+ (JEP 511).
        // Pin to JDK 17, the project minimum, so this assertion still holds on a
        // JDK 25 host whose default target bytecode is V25. The legal V25 case is
        // covered by testRequiresTransitiveJavaBaseOnTarget25.
        assert fail(dir('base-mod'), ['module-info.groovy': '''
            module demo.base { requires transitive java.base; }
        '''], CompilerConfiguration.JDK17).contains('requires java.base must not have modifiers')

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
    void testUnnamedPackageClassRejectedInNamedModule() {
        def err = fail(dir('unnamed-pkg'), [
            'module-info.groovy': 'module demo.loose { }',
            'Loose.groovy': 'class Loose {}'
        ])
        assert err.contains('unnamed package is not allowed in named modules')
    }

    @Test
    void testRequiresJavaSe() {
        def unit = compile(dir('java-se'), [
            'module-info.groovy': 'module demo.se { requires java.se; }'
        ])
        assert require(descriptor(unit), 'java.se') != null
    }

    @Test
    void testConcealedPackageInRequiredModuleIsNotASplit() {
        def lib = dir('conceal-lib')
        compile(lib, [
            'module-info.groovy': 'module conceal.lib { }',
            'H.groovy': 'package com.acme.internal; class Hidden {}'
        ])
        def libJar = new File(tmp, 'conceal-lib.jar')
        moduleJar(libJar, 'conceal.lib', null, null, lib)
        compile(dir('conceal-app'), libJar, [
            'module-info.groovy': 'module conceal.app { requires conceal.lib; }',
            'A.groovy': 'package com.acme.internal; class Also {}'
        ])
    }

    @Test
    void testDuplicateRequiresDoesNotHideJavaBaseTypes() {
        def err = fail(dir('dup-noise'), [
            'module-info.groovy': '''
                @Deprecated
                module demo.dup { requires java.sql; requires java.sql; uses java.lang.Runnable; }
            '''
        ])
        assert err.contains('duplicate requires on module java.sql')
        assert !err.contains('not accessible')
    }

    @Test
    void testNestedTypeNameResolutionAndModuleImport() {
        def unit = compile(dir('nested-names'), [
            'module-info.groovy': '''
                import demo.names.Outer
                import module java.base
                module demo.names {
                    uses System.LoggerFinder;
                    uses URLStreamHandlerFactory;
                    provides java.lang.Runnable with Outer.Inner;
                }
            ''',
            'Outer.groovy': '''
                package demo.names
                class Outer {
                    static class Inner implements Runnable { void run() {} }
                }
            '''
        ])
        def md = descriptor(unit)
        assert md.uses().contains('java.lang.System$LoggerFinder')
        assert md.uses().contains('java.net.URLStreamHandlerFactory')
        assert md.provides().find { it.service() == 'java.lang.Runnable' }.providers() as List ==
            ['demo.names.Outer$Inner']
    }

    @Test
    void testStarImportShadowsModuleImport() {
        def unit = compile(dir('shadow-mod'), [
            'module-info.groovy': '''
                import demo.shadow.*
                import module java.base
                module demo.shadow { uses List; }
            ''',
            'List.groovy': 'package demo.shadow; interface List {}'
        ])
        assert descriptor(unit).uses() == ['demo.shadow.List'] as Set
    }

    @Test
    void testStaticImportOfMemberType() {
        def unit = compile(dir('static-member'), [
            'module-info.groovy': '''
                import static demo.st.Outer.Inner
                module demo.st { provides java.lang.Runnable with Inner; }
            ''',
            'Outer.groovy': '''
                package demo.st
                class Outer {
                    static class Inner implements Runnable { void run() {} }
                }
            '''
        ])
        assert descriptor(unit).provides().find { it.service() == 'java.lang.Runnable' }.providers() as List ==
            ['demo.st.Outer$Inner']
    }

    @Test
    void testImportModuleWithNoExports() {
        def empty = dir('empty-mod-classes')
        compile(empty, ['module-info.groovy': 'module empty.mod { }'])
        def jar = new File(tmp, 'empty-mod.jar')
        moduleJar(jar, 'empty.mod', null, null, empty)
        compile(dir('import-empty'), jar, [
            'module-info.groovy': '''
                import module empty.mod
                module demo.importer { }
            '''
        ])
    }

    @Test
    void testProviderAfterCanonicalizationAndDefaultArguments() {
        compile(dir('providers-ok'), [
            'module-info.groovy': '''
                module demo.ok {
                    provides java.lang.Runnable with demo.ok.Delegated, demo.ok.DefaultCtor, demo.ok.Factory;
                }
            ''',
            'Delegated.groovy': '''
                package demo.ok
                class Delegated {
                    @Delegate Runnable r = new Runnable() { void run() {} }
                }
            ''',
            'DefaultCtor.groovy': '''
                package demo.ok
                class DefaultCtor implements Runnable {
                    DefaultCtor(String env = 'prod') {}
                    void run() {}
                }
            ''',
            'Factory.groovy': '''
                package demo.ok
                class Factory {
                    static Runnable provider(int retries = 3) { return new Runnable() { void run() {} } }
                }
            '''
        ])

        assert fail(dir('record-provider'), [
            'module-info.groovy': '''
                module demo.bad { provides java.lang.Runnable with demo.bad.Rec; }
            ''',
            'Rec.groovy': 'package demo.bad; record Rec(String url) implements Runnable { void run() {} }'
        ]).contains('public no-arg constructor')

        assert fail(dir('singleton-provider'), [
            'module-info.groovy': '''
                module demo.bad { provides java.lang.Runnable with demo.bad.Single; }
            ''',
            'Single.groovy': '''
                package demo.bad
                @Singleton
                class Single implements Runnable { void run() {} }
            '''
        ]).contains('public no-arg constructor')
    }

    @Test
    void testEnclosingTypeMustBePublicAndPrimitiveProviderRejected() {
        assert fail(dir('pkg-outer'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Runnable with demo.p.Outer.Inner; }
            ''',
            'O.groovy': '''
                package demo.p
                import groovy.transform.PackageScope
                @PackageScope
                class Outer {
                    static class Inner implements Runnable { void run() {} }
                }
            '''
        ]).contains('must be public')

        assert fail(dir('prim-provider'), [
            'module-info.groovy': '''
                module demo.p { provides java.lang.Object with demo.p.Factory; }
            ''',
            'F.groovy': '''
                package demo.p
                class Factory {
                    static int provider() { 1 }
                }
            '''
        ]).contains('must return a subtype')
    }

    @Test
    void testGlobalCompileStaticCustomizerOnModuleInfo() {
        def target = dir('cs-customizer')
        def config = new CompilerConfiguration(targetDirectory: target)
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('module-info.groovy', 'module demo.cs { }')
            unit.compile()
            assert descriptor(unit).name() == 'demo.cs'
        } finally {
            loader.close()
        }
    }

    @Test
    void testAnnotationCollectorOnModule() {
        def unit = compile(dir('ann-coll'), [
            'module-info.groovy': '''
                import demo.coll.ModAnns
                @ModAnns
                module demo.coll { }
            ''',
            'ModAnns.groovy': '''
                package demo.coll
                import groovy.transform.AnnotationCollector
                import java.lang.annotation.ElementType
                import java.lang.annotation.Retention
                import java.lang.annotation.RetentionPolicy
                import java.lang.annotation.Target
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface Mark {}
                @AnnotationCollector([Deprecated, Mark])
                @interface ModAnns {}
            '''
        ])
        def text = new String(unit.classes.find { it.name == 'module-info' }.bytes, 'ISO-8859-1')
        assert text.contains('Ljava/lang/Deprecated;') || descriptorHasDeprecated(unit)
        assert text.contains('Ldemo/coll/Mark;')
    }

    @Test
    void testNewlineAfterModuleAndRuntimeGroovydoc() {
        def target = dir('nl-doc')
        def config = new CompilerConfiguration(targetDirectory: target)
        config.optimizationOptions[CompilerConfiguration.GROOVYDOC] = true
        config.optimizationOptions[CompilerConfiguration.RUNTIME_GROOVYDOC] = true
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('module-info.groovy', '''
                /**@ Module documentation. */
                module
                demo.nl {
                }
            ''')
            unit.compile()
            assert unit.ast.moduleInfo.groovydoc?.content?.contains('Module documentation')
            def text = new String(unit.classes.find { it.name == 'module-info' }.bytes, 'ISO-8859-1')
            assert text.contains('Lgroovy/lang/Groovydoc;')
            assert descriptor(unit).name() == 'demo.nl'
        } finally {
            loader.close()
        }
    }

    @Test
    void testRequiresTransitiveJavaBaseOnTarget25() {
        def target = dir('trans-base-25')
        def config = new CompilerConfiguration(targetDirectory: target)
        config.targetBytecode = CompilerConfiguration.JDK25
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('module-info.groovy', 'module demo.tb { requires transitive java.base; }')
            unit.compile()
            // JDK 17 cannot ModuleDescriptor.read a V25 class file; inspect flags with ASM.
            def flags = [:]
            new ClassReader(unit.classes.find { it.name == 'module-info' }.bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                ModuleVisitor visitModule(String moduleName, int moduleAccess, String moduleVersion) {
                    new ModuleVisitor(Opcodes.ASM9) {
                        @Override
                        void visitRequire(String requiredName, int requireAccess, String requireVersion) {
                            flags[requiredName] = requireAccess
                        }
                    }
                }
            }, 0)
            assert (flags['java.base'] & Opcodes.ACC_TRANSITIVE) != 0
        } finally {
            loader.close()
        }
    }

    @Test
    void testJointCompilationSeesJavaOnlyPackagesAndProviders() {
        def root = dir('joint-java')
        def unit = compileJoint(root, [
            'module-info.groovy': '''
                module demo.jointx {
                    exports demo.javaonly;
                    uses demo.internal.JavaSpi;
                    provides demo.internal.JavaSpi with demo.impl.JavaProvider;
                }
            ''',
            'demo/javaonly/Api.java': '''
                package demo.javaonly;
                public class Api {}
            ''',
            'demo/internal/JavaSpi.java': '''
                package demo.internal;
                public interface JavaSpi {}
            ''',
            'demo/impl/JavaProvider.java': '''
                package demo.impl;
                public class JavaProvider implements demo.internal.JavaSpi {}
            '''
        ])
        def md = descriptor(unit)
        assert md.exports()*.source() == ['demo.javaonly']
        assert md.packages().containsAll(['demo.javaonly', 'demo.internal', 'demo.impl'])
        assert unit.compiledClasses.contains('demo.javaonly.Api')
        assert unit.compiledClasses.contains('demo.internal.JavaSpi')
        assert unit.compiledClasses.contains('demo.impl.JavaProvider')
        assert unit.compiledClasses.packages.containsAll(['demo.javaonly', 'demo.internal', 'demo.impl'])
        assert md.uses() == ['demo.internal.JavaSpi'] as Set
        assert md.provides().find { it.service() == 'demo.internal.JavaSpi' }.providers() as List ==
            ['demo.impl.JavaProvider']
    }

    @Test
    void testJointCompilationWithModuleInfoJava() {
        def root = dir('joint-minfo-java')
        def unit = compileJoint(root, [
            'module-info.java': '''
                module demo.minfo {
                    exports demo.api;
                }
            ''',
            'demo/api/Greeter.groovy': '''
                package demo.api
                class Greeter {
                    static String hi() { 'hi' }
                }
            ''',
            'demo/api/JavaGreeter.java': '''
                package demo.api;
                public class JavaGreeter {
                    public static String hello() {
                        return Greeter.hi();
                    }
                }
            '''
        ])
        def md = descriptor(unit)
        assert md.name() == 'demo.minfo'
        assert md.exports()*.source() == ['demo.api']
        assert new File(unit.configuration.targetDirectory, 'demo/api/JavaGreeter.class').isFile()
    }

    @Test
    void testModuleHeaderNewlineAfterKeyword() {
        def unit = compile(dir('open-nl'), [
            'module-info.groovy': '''
                open
                module
                demo.open.nl {
                }
            '''
        ])
        assert descriptor(unit).open
        assert descriptor(unit).name() == 'demo.open.nl'
    }

    @Test
    void testStaleClassInTargetDirectoryIsNotPartOfTheModule() {
        def root = dir('stale-target')
        def seed = { File outDir ->
            def stale = new File(outDir, 'com/stale')
            stale.mkdirs()
            def cw = new ClassWriter(0)
            cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, 'com/stale/Old', null, 'java/lang/Object', null)
            cw.visitEnd()
            new File(stale, 'Old.class').bytes = cw.toByteArray()
        }
        try {
            compileJoint(root, [
                'module-info.groovy': '''
                    module demo.stale {
                        exports com.stale;
                        exports demo.fresh;
                    }
                ''',
                'demo/fresh/Fresh.java': '''
                    package demo.fresh;
                    public class Fresh {}
                '''
            ], seed)
            throw new AssertionError('expected compilation to fail for stale-target')
        } catch (CompilationFailedException e) {
            assert e.message.contains('package is empty or does not exist: com.stale')
            assert !e.message.contains('demo.fresh')
        }

        def okRoot = dir('stale-target-ok')
        def unit = compileJoint(okRoot, [
            'module-info.groovy': '''
                module demo.staleok {
                    exports demo.fresh;
                }
            ''',
            'demo/fresh/Fresh.java': '''
                package demo.fresh;
                public class Fresh {}
            '''
        ], seed)
        def md = descriptor(unit)
        assert md.exports()*.source() == ['demo.fresh']
        assert !md.packages().contains('com.stale')
        assert md.packages().contains('demo.fresh')
    }

    @Test
    void testSubclassCanRecordCompiledClassNameWithoutTargetDirectorySnapshot() {
        def root = dir('hooked-output')
        def srcDir = new File(root, 'src')
        def files = []
        [
            'module-info.groovy': '''
                module demo.hooked {
                    exports com.hooked;
                }
            ''',
            'demo/fresh/Fresh.java': '''
                package demo.fresh;
                public class Fresh {}
            '''
        ].each { name, text ->
            def file = new File(srcDir, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }

        def defaultOut = new File(root, 'default-out')
        def defaultStubs = new File(root, 'default-stubs')
        defaultOut.mkdirs()
        defaultStubs.mkdirs()
        def defaultConfig = new CompilerConfiguration(targetDirectory: defaultOut)
        defaultConfig.jointCompilationOptions = [stubDir: defaultStubs]
        def defaultLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(defaultConfig, defaultLoader)
            unit.addSources(files as File[])
            unit.compile()
            throw new AssertionError('expected compilation to fail for hooked-output default')
        } catch (CompilationFailedException e) {
            assert e.message.contains('package is empty or does not exist: com.hooked')
        } finally {
            defaultLoader.close()
        }

        def hookOut = new File(root, 'hook-out')
        def hookStubs = new File(root, 'hook-stubs')
        hookOut.mkdirs()
        hookStubs.mkdirs()
        def hookConfig = new CompilerConfiguration(targetDirectory: hookOut)
        hookConfig.jointCompilationOptions = [stubDir: hookStubs]
        def hookLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(hookConfig, hookLoader) {
                @Override
                protected Object snapshotCompiledClassOutput() {
                    return null
                }

                @Override
                protected Set<String> collectCompiledClassNames(Object snapshot) {
                    return Set.of('com.hooked.Hooked')
                }
            }
            unit.addSources(files as File[])
            unit.compile()
            assert unit.compiledClasses.contains('com.hooked.Hooked')
            assert unit.compiledClasses.packages.contains('com.hooked')
            assert !unit.compiledClasses.contains('module-info')
            assert !unit.compiledClasses.hasUnnamedClass()
            def md = descriptor(unit)
            assert md.name() == 'demo.hooked'
            assert md.exports()*.source() == ['com.hooked']
            assert md.packages().contains('com.hooked')
            assert !md.packages().contains('demo.fresh')
        } finally {
            hookLoader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testInMemoryJavacOutputIsIndexed() {
        def src = new File(dir('mem-index'), 'src')
        def files = []
        [
            'module-info.groovy': '''
                module demo.mem {
                    exports demo.mem;
                }
            ''',
            'demo/mem/A.java': '''
                package demo.mem;
                public class A {
                    public static class Inner {}
                }
            '''
        ].each { name, text ->
            def file = new File(src, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }
        def config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(files as File[])
            unit.compile()
            assert unit.compiledClasses.contains('demo.mem.A')
            assert unit.compiledClasses.contains('demo.mem.A$Inner')
            assert unit.compiledClasses.packages.contains('demo.mem')
            assert !unit.compiledClasses.contains('module-info')
            assert !unit.compiledClasses.hasUnnamedClass()
            def md = descriptor(unit)
            assert md.name() == 'demo.mem'
            assert md.exports()*.source() == ['demo.mem']
            assert md.packages().contains('demo.mem')
            assert loader.getResource('demo/mem/A.class') != null
            assert loader.getResource('demo/mem/A$Inner.class') != null
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testInMemoryOutputDoesNotIndexStaleClassFiles() {
        def root = dir('mem-stale')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        def files = []
        [
            'module-info.groovy': '''
                module demo.memstale {
                    exports demo.mem;
                }
            ''',
            'demo/mem/A.java': '''
                package demo.mem;
                public class A {}
            '''
        ].each { name, text ->
            def file = new File(src, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }
        new File(out, 'com/stale').mkdirs()
        new File(out, 'com/stale/Old.class').bytes = [1, 2, 3] as byte[]
        new File(out, 'demo/mem').mkdirs()
        new File(out, 'demo/mem/A$Gone.class').bytes = [1, 2, 3] as byte[]

        def config = new CompilerConfiguration(targetDirectory: out)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(files as File[])
            unit.compile()
            assert unit.compiledClasses.contains('demo.mem.A')
            assert !unit.compiledClasses.contains('com.stale.Old')
            assert !unit.compiledClasses.contains('demo.mem.A$Gone')
            assert !unit.compiledClasses.packages.contains('com.stale')
            assert descriptor(unit).packages().contains('demo.mem')
            assert !descriptor(unit).packages().contains('com.stale')
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testInMemoryUnnamedJavaClassIsRejected() {
        def src = new File(dir('mem-unnamed'), 'src')
        def files = []
        [
            'module-info.groovy': 'module demo.memunnamed { }',
            'Loose.java': 'public class Loose {}'
        ].each { name, text ->
            def file = new File(src, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }
        def config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(files as File[])
            def thrown = GroovyAssert.shouldFail(CompilationFailedException) {
                unit.compile()
            }
            assert thrown.message.contains('unnamed package is not allowed in named modules')
            assert unit.compiledClasses.contains('Loose')
            assert unit.compiledClasses.hasUnnamedClass()
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testInMemoryOutputSkipsTargetDirectorySnapshot() {
        def root = dir('mem-skip-snapshot')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        out.mkdirs()
        def files = []
        [
            'module-info.groovy': '''
                module demo.memskip {
                    exports demo.mem;
                }
            ''',
            'demo/mem/A.java': '''
                package demo.mem;
                public class A {
                    public static class Inner {}
                }
            '''
        ].each { name, text ->
            def file = new File(src, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }
        // A disk walk would record this leftover. The blocked directory cannot
        // be listed, so the same walk fails before indexing can finish.
        new File(out, 'com/stale').mkdirs()
        new File(out, 'com/stale/Old.class').bytes = [1, 2, 3] as byte[]
        def blocked = new File(out, 'blocked')
        def locked = false
        if (FileSystems.default.supportedFileAttributeViews().contains('posix')) {
            blocked.mkdirs()
            new File(blocked, 'Old.class').bytes = [4, 5, 6] as byte[]
            Files.setPosixFilePermissions(blocked.toPath(), EnumSet.noneOf(PosixFilePermission))
            locked = true
        }
        def config = new CompilerConfiguration(targetDirectory: out)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        def seen = []
        try {
            def unit = new JavaAwareCompilationUnit(config, loader) {
                @Override
                protected Object snapshotCompiledClassOutput() {
                    def snapshot = super.snapshotCompiledClassOutput()
                    assert snapshot instanceof Map
                    assert snapshot.isEmpty()
                    seen << snapshot
                    return snapshot
                }
            }
            unit.addSources(files as File[])
            unit.compile()
            assert seen.size() == 1
            assert unit.compiledClasses.contains('demo.mem.A')
            assert unit.compiledClasses.contains('demo.mem.A$Inner')
            assert unit.compiledClasses.packages.contains('demo.mem')
            assert !unit.compiledClasses.contains('com.stale.Old')
            assert !unit.compiledClasses.contains('blocked.Old')
            assert !unit.compiledClasses.contains('module-info')
            assert !unit.compiledClasses.hasUnnamedClass()
            def md = descriptor(unit)
            assert md.name() == 'demo.memskip'
            assert md.exports()*.source() == ['demo.mem']
            assert md.packages().contains('demo.mem')
            assert !md.packages().contains('com.stale')
            assert loader.getResource('demo/mem/A.class') != null
            assert loader.getResource('demo/mem/A$Inner.class') != null
        } finally {
            if (locked) {
                Files.setPosixFilePermissions(blocked.toPath(), EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE))
            }
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testGenerateClassDoesNotInventModuleInfoStub() {
        def root = dir('stub-carrier')
        def config = new CompilerConfiguration(targetDirectory: new File(root, 'out'))
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('module-info.groovy', 'module demo.carrier { exports demo.carrier; }')
            unit.addSource('demo/carrier/A.groovy', 'package demo.carrier; class A {}')
            unit.compile(Phases.CONVERSION)
            assert unit.ast.moduleInfo != null
            assert unit.ast.moduleInfo.name == 'demo.carrier'
            assert !unit.ast.classes.any { it.name == 'module-info' }
            def type = unit.ast.classes.find { it.name == 'demo.carrier.A' }
            assert type != null

            def stubs = new File(root, 'stubs')
            stubs.mkdirs()
            new JavaStubGenerator(stubs, false, 'UTF-8').generateClass(type)
            assert new File(stubs, 'demo/carrier/A.java').isFile()
            assert !new File(stubs, 'module-info.java').exists()
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testJavaStubCompilationUnitDoesNotStubModuleInfo() {
        def root = dir('stub-unit')
        def src = new File(root, 'src')
        def moduleInfo = new File(src, 'module-info.groovy')
        moduleInfo.parentFile.mkdirs()
        moduleInfo.text = '''
            module demo.stubs {
                exports demo.api;
            }
        '''
        def greeter = new File(src, 'demo/api/Greeter.groovy')
        greeter.parentFile.mkdirs()
        greeter.text = '''
            package demo.api
            class Greeter {}
        '''
        def stubDir = new File(root, 'stubs')
        def out = new File(root, 'out')
        stubDir.mkdirs()
        out.mkdirs()
        def config = new CompilerConfiguration(targetDirectory: out)
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaStubCompilationUnit(config, loader, stubDir)
            assert unit.addSource(moduleInfo) != null
            assert unit.addSource(greeter) != null
            unit.compile()
            assert unit.stubCount == 1
            assert !new File(stubDir, 'module-info.java').exists()
            def stub = new File(stubDir, 'demo/api/Greeter.java')
            assert stub.isFile()
            assert stub.text.contains('class Greeter')
            assert !stub.text.contains('interface module-info')
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testCompiledClassIndexDoesNotKeepNamesFromAPreviousCompile() {
        def root = dir('index-reuse')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        def stubs = new File(root, 'stubs')
        src.mkdirs()
        out.mkdirs()
        stubs.mkdirs()
        def anchor = new File(src, 'Anchor.groovy')
        anchor.text = 'class Anchor {}'
        def a = new File(src, 'demo/a/A.java')
        a.parentFile.mkdirs()
        a.text = 'package demo.a; public class A {}'
        def b = new File(src, 'demo/b/B.java')
        b.parentFile.mkdirs()
        b.text = 'package demo.b; public class B { public static class Inner {} }'

        def config = new CompilerConfiguration(targetDirectory: out)
        config.jointCompilationOptions = [stubDir: stubs]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(anchor, a)
            unit.compile()
            assert unit.compiledClasses.contains('demo.a.A')
            assert !unit.compiledClasses.contains('demo.b.B')

            unit.addSources(b)
            unit.compile()
            assert unit.compiledClasses.contains('demo.b.B')
            assert unit.compiledClasses.contains('demo.b.B$Inner')
            assert !unit.compiledClasses.contains('demo.a.A')
            assert !unit.compiledClasses.packages.contains('demo.a')
            assert !unit.compiledClasses.hasUnnamedClass()
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testTwoModuleInfoSourcesAreRejected() {
        def config = new CompilerConfiguration(targetDirectory: dir('two-modules'))
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('src/demo/a/module-info.groovy', 'module demo.first { }')
            unit.addSource('src/demo/b/module-info.groovy', 'module demo.second { }')
            def thrown = GroovyAssert.shouldFail(CompilationFailedException) {
                unit.compile()
            }
            assert thrown.message.contains('only one module declaration')
            assert unit.ast.moduleInfo.name == 'demo.first'
            def rejected = unit.ast.modules.find { it.context.name.contains('/b/') }
            assert rejected.moduleDeclaration == null
            assert !rejected.classes.any { it.name == 'module-info' }
            assert unit.ast.moduleInfo.is(unit.ast.modules.find { it.moduleDeclaration != null }.moduleDeclaration)
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testSecondModuleDeclarationWithoutSourceIsRejected() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            def first = new ModuleInfoNode('demo.first', false, [])
            def second = new ModuleInfoNode('demo.second', false, [])
            unit.ast.moduleInfo = first
            unit.ast.moduleInfo = second
            assert unit.ast.moduleInfo.is(first)
            assert unit.ast.consumeModuleInfoConflict().contains('only one module declaration')
            // Setting the rejected declaration again must still be reported.
            unit.ast.moduleInfo = second
            assert unit.ast.moduleInfo.is(first)
            unit.addSource('A.groovy', 'class A {}')
            def thrown = GroovyAssert.shouldFail(CompilationFailedException) {
                unit.compile()
            }
            assert thrown.message.contains('only one module declaration')
            assert unit.ast.moduleInfo.is(first)
        } finally {
            loader.close()
        }
    }

    @Test
    void testDeclarationSurvivesStrippedClassMetadata() {
        def root = dir('strip-meta')
        def config = new CompilerConfiguration(targetDirectory: root)
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            def seen = []
            unit.addPhaseOperation({ SourceUnit source, GeneratorContext context, ClassNode classNode ->
                assert classNode.name != 'module-info'
                seen << classNode.name
                // Class metadata is not where the declaration lives. Clearing it
                // must not drop the compile-unit declaration. The module link is
                // restored because later phases read it from the class.
                def module = classNode.module
                classNode.metaDataMap = null
                classNode.module = module
                assert classNode.getNodeMetaData(ModuleInfoNode) == null
            }, Phases.SEMANTIC_ANALYSIS)
            unit.addSource('module-info.groovy', '''
                module demo.strip {
                    exports demo.strip;
                }
            ''')
            unit.addSource('demo/strip/A.groovy', '''
                package demo.strip
                class A {}
            ''')
            unit.compile()
            def declaration = unit.ast.moduleInfo
            assert declaration != null
            assert declaration.name == 'demo.strip'
            assert seen == ['demo.strip.A']
            assert !unit.ast.classes.any { it.name == 'module-info' }
            assert unit.compiledClasses.packages.empty
            assert !unit.compiledClasses.hasUnnamedClass()
            def md = descriptor(unit)
            assert md.name() == 'demo.strip'
            assert md.exports()*.source() == ['demo.strip']
        } finally {
            loader.close()
        }
    }

    @Test
    void testUnrelatedAutomaticModulesWithSplitPackageDoNotFailResolution() {
        // Both automatic JARs share conflict.pkg and provide ToolProvider.
        // The compiling module uses that service, so Configuration.resolveAndBind
        // would pull both JARs into the graph and fail on the split package.
        // Configuration.resolve must not.
        def oneDir = dir('auto-split-one')
        compile(oneDir, [
            'A.groovy': '''
                package conflict.pkg
                class A implements java.util.spi.ToolProvider {
                    String name() { 'split-one' }
                    int run(PrintWriter out, PrintWriter err, String... args) { 0 }
                }
            '''
        ])
        def twoDir = dir('auto-split-two')
        compile(twoDir, [
            'B.groovy': '''
                package conflict.pkg
                class B implements java.util.spi.ToolProvider {
                    String name() { 'split-two' }
                    int run(PrintWriter out, PrintWriter err, String... args) { 0 }
                }
            '''
        ])
        def svcOne = new File(oneDir, 'META-INF/services')
        svcOne.mkdirs()
        new File(svcOne, 'java.util.spi.ToolProvider').text = "conflict.pkg.A\n"
        def svcTwo = new File(twoDir, 'META-INF/services')
        svcTwo.mkdirs()
        new File(svcTwo, 'java.util.spi.ToolProvider').text = "conflict.pkg.B\n"
        def libDir = dir('auto-split-libs')
        packJar(new File(libDir, 'auto-one-1.0.jar'), 'auto.one', oneDir)
        packJar(new File(libDir, 'auto-two-1.0.jar'), 'auto.two', twoDir)
        compile(dir('auto-split-app'), libDir, [
            'module-info.groovy': '''
                module demo.splitauto {
                    uses java.util.spi.ToolProvider;
                }
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

    private CompilationUnit compileJoint(File root, Map<String, String> sources) {
        compileJoint(root, sources, null)
    }

    private CompilationUnit compileJoint(File root, Map<String, String> sources, Closure seedTargetDirectory) {
        def srcDir = new File(root, 'src')
        def outDir = new File(root, 'out')
        def stubDir = new File(root, 'stubs')
        srcDir.mkdirs()
        outDir.mkdirs()
        stubDir.mkdirs()
        if (seedTargetDirectory != null) {
            seedTargetDirectory.call(outDir)
        }
        def files = []
        sources.each { name, text ->
            def file = new File(srcDir, name)
            file.parentFile.mkdirs()
            file.text = text
            files << file
        }
        def config = new CompilerConfiguration(targetDirectory: outDir)
        config.jointCompilationOptions = [stubDir: stubDir]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(files as File[])
            unit.compile()
            unit
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testModuleInfoIsProcessedBeforeClasses() {
        def config = new CompilerConfiguration(targetDirectory: dir('order'))
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            def seen = []
            unit.addFirstPhaseOperation({ SourceUnit source, GeneratorContext context, ClassNode classNode ->
                assert classNode.name != 'module-info'
                def declaration = unit.ast.moduleInfo
                assert declaration != null
                assert declaration.readsReady
                assert !declaration.resolutionFailed
                assert declaration.hasResolvedTypes()
                assert declaration.uses[0].resolvedType?.name == 'java.lang.Runnable'
                seen << classNode.name
            }, Phases.SEMANTIC_ANALYSIS)
            unit.addSource('module-info.groovy', '''
                module demo.order {
                    exports demo.order;
                    uses java.lang.Runnable;
                }
            ''')
            unit.addSource('demo/order/A.groovy', '''
                package demo.order
                class A implements Runnable { void run() {} }
            ''')
            unit.compile()
            assert seen == ['demo.order.A']
            assert !unit.ast.classes.any { it.name == 'module-info' }
            def moduleClass = unit.classes.find { it.name == 'module-info' }
            assert moduleClass != null
            def md = ModuleDescriptor.read(new ByteArrayInputStream(moduleClass.bytes))
            assert md.name() == 'demo.order'
            assert md.exports()*.source() == ['demo.order']
            assert md.uses() == ['java.lang.Runnable'] as Set
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testClassTransformsDoNotSeeModuleDeclaration() {
        def unit = compile(dir('xform'), [
            'module-info.groovy': 'module demo.xform { exports demo.xform; }',
            'A.groovy': 'package demo.xform; class A {}'
        ])
        assert unit.ast.moduleInfo.name == 'demo.xform'
        assert unit.ast.classes*.name == ['demo.xform.A']
        assert unit.classes.any { it.name == 'module-info' }
    }

    // GROOVY-9273
    @Test
    void testNamedModuleRejectsTypeFromUnreadModule() {
        def messages = fail(dir('awt'), [
            'module-info.groovy': 'module demo.awt { exports demo.awt; }',
            'A.groovy': 'package demo.awt; class A { java.awt.Frame frame }'
        ])
        assert messages.contains('not accessible')
        assert messages.contains('java.awt.Frame')
    }

    // GROOVY-9273
    @Test
    void testNamedModuleAllowsTypeFromRequiredModule() {
        def unit = compile(dir('awt-ok'), [
            'module-info.groovy': '''
                module demo.awt {
                    requires java.desktop;
                    exports demo.awt;
                }
            ''',
            'A.groovy': 'package demo.awt; class A { java.awt.Frame frame }'
        ])
        assert unit.classes.any { it.name == 'demo.awt.A' }
        assert unit.classes.any { it.name == 'module-info' }
    }

    // GROOVY-9273
    @Test
    void testUnnamedCompilationDoesNotApplyModuleAccess() {
        def unit = compile(dir('awt-unnamed'), [
            'A.groovy': 'class A { java.awt.Frame frame }'
        ])
        assert unit.ast.moduleInfo == null
        assert unit.classes.any { it.name == 'A' }
        assert unit.classes.every { it.name != 'module-info' }
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
        compile(target, classpathEntry, sources, null)
    }

    private CompilationUnit compile(File target, File classpathEntry, Map<String, String> sources, String targetBytecode) {
        def config = new CompilerConfiguration(targetDirectory: target)
        if (targetBytecode != null) {
            config.targetBytecode = targetBytecode
        }
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

    private String fail(File target, Map<String, String> sources, String targetBytecode) {
        try {
            compile(target, null, sources, targetBytecode)
            throw new AssertionError('expected compilation to fail for ' + target.name)
        } catch (CompilationFailedException e) {
            return e.message
        }
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
