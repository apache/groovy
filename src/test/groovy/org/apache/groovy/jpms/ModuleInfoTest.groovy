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
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.codehaus.groovy.tools.javac.JavaCompiler
import org.codehaus.groovy.tools.javac.JavaCompilerFactory
import org.codehaus.groovy.tools.javac.JavaStubCompilationUnit
import org.codehaus.groovy.tools.javac.JavaStubGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.CheckClassAdapter

import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.reflect.InaccessibleObjectException
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
 * JPMS module declarations (JLS §7.7, JEP 261, JEP 396, JEP 403, JEP 511):
 * parse, semantic checks, {@code module-info.class}, compile-time JPMS options,
 * and interop with the JDK module system.
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
        // JLS §7.5.5 (JEP 511): import module in a named module requires readability.
        compile(dir('import-empty-read'), jar, [
            'module-info.groovy': '''
                import module empty.mod
                module demo.importer {
                    requires empty.mod;
                }
            '''
        ])
        assert fail(dir('import-empty-unread'), jar, [
            'module-info.groovy': '''
                import module empty.mod
                module demo.importer { }
            '''
        ]).contains('module empty.mod is not readable in module demo.importer')
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
    void testCustomJavaCompilerCanRecordCompiledClassNames() {
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
            def unit = new JavaAwareCompilationUnit(hookConfig, hookLoader)
            unit.compilerFactory = { CompilerConfiguration cfg ->
                return { List<String> sources, CompilationUnit cu ->
                    cu.compiledClasses.add('com.hooked.Hooked')
                    cu.compiledClasses.add('module-info')
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
    void testJavacDoesNotWalkTargetDirectoryForIndexing() {
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
        // A disk walk would record this leftover and fail on the unreadable subdirectory.
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
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(files as File[])
            unit.compile()
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

            // Disk mode with JavacJavaCompiler also never walks targetDirectory.
            def diskStubs = new File(root, 'disk-stubs')
            diskStubs.mkdirs()
            def diskConfig = new CompilerConfiguration(targetDirectory: out)
            diskConfig.jointCompilationOptions = [stubDir: diskStubs]
            def diskLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
            try {
                def diskUnit = new JavaAwareCompilationUnit(diskConfig, diskLoader)
                diskUnit.addSources(files as File[])
                diskUnit.compile()
                assert diskUnit.compiledClasses.contains('demo.mem.A')
                assert diskUnit.compiledClasses.contains('demo.mem.A$Inner')
                assert !diskUnit.compiledClasses.contains('com.stale.Old')
                assert !diskUnit.compiledClasses.contains('blocked.Old')
            } finally {
                diskLoader.close()
            }
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
    void testNamedModuleRejectsClassExpressionsAndStaticImports() {
        // A field type is not the only reference. A class expression, a simple
        // name, and a static import bind the type without resolveOrFail.
        def expression = fail(dir('awt-expr'), [
            'module-info.groovy': 'module demo.awt { exports demo.awt; }',
            'A.groovy': 'package demo.awt; class A { def type = java.awt.Frame }'
        ])
        assert expression.contains('not accessible')
        assert expression.contains('java.awt.Frame')

        def simple = fail(dir('awt-simple'), [
            'module-info.groovy': 'module demo.awt { exports demo.awt; }',
            'A.groovy': '''
                package demo.awt
                import java.awt.Frame
                class A { def type = Frame }
            '''
        ])
        assert simple.contains('not accessible')
        assert simple.contains('java.awt.Frame')

        def member = fail(dir('awt-static'), [
            'module-info.groovy': 'module demo.awt { exports demo.awt; }',
            'A.groovy': '''
                package demo.awt
                import static java.awt.Color.RED
                class A { def color = RED }
            '''
        ])
        assert member.contains('not accessible')
        assert member.contains('java.awt.Color')

        def star = fail(dir('awt-static-star'), [
            'module-info.groovy': 'module demo.awt { exports demo.awt; }',
            'A.groovy': '''
                package demo.awt
                import static java.awt.Color.*
                class A { def color = RED }
            '''
        ])
        assert star.contains('not accessible')
        assert star.contains('java.awt.Color')
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

        def staticImport = compile(dir('awt-static-ok'), [
            'module-info.groovy': '''
                module demo.awt {
                    requires java.desktop;
                    exports demo.awt;
                }
            ''',
            'A.groovy': '''
                package demo.awt
                import static java.awt.Color.RED
                class A { def color = RED }
            '''
        ])
        assert staticImport.classes.any { it.name == 'demo.awt.A' }
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

        def staticImport = compile(dir('awt-unnamed-static'), [
            'A.groovy': '''
                import static java.awt.Color.RED
                class A { def color = RED }
            '''
        ])
        assert staticImport.ast.moduleInfo == null
        assert staticImport.classes.any { it.name == 'A' }
    }

    // GROOVY-9273
    @Test
    void testPosixBackslashFileNameIsNotModuleInfo() {
        if (File.separatorChar != '/') {
            return
        }
        def root = dir('posix-backslash')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        src.mkdirs()
        out.mkdirs()
        def scriptFile = new File(src, 'x\\module-info.groovy')
        scriptFile.text = 'def answer = 42'
        def config = new CompilerConfiguration(targetDirectory: out)
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource(scriptFile)
            unit.compile()
            assert unit.ast.moduleInfo == null
            assert unit.classes.every { it.name != 'module-info' }
        } finally {
            loader.close()
        }

        // Joint compilation also routes a file literally named x\module-info.java to javac, not Groovy module-info.
        def jointRoot = dir('posix-backslash-joint')
        def jointSrc = new File(jointRoot, 'src')
        def jointOut = new File(jointRoot, 'out')
        def jointStubs = new File(jointRoot, 'stubs')
        jointSrc.mkdirs()
        jointOut.mkdirs()
        jointStubs.mkdirs()
        def javaLike = new File(jointSrc, 'x\\module-info.java')
        javaLike.text = 'package demo.bs; class Helper {}'
        def anchor = new File(jointSrc, 'Anchor.groovy')
        anchor.text = 'package demo.bs; class Anchor {}'
        def jConfig = new CompilerConfiguration(targetDirectory: jointOut)
        jConfig.jointCompilationOptions = [stubDir: jointStubs]
        def jLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def jUnit = new JavaAwareCompilationUnit(jConfig, jLoader)
            jUnit.addSources([anchor, javaLike] as File[])
            jUnit.compile()
            assert jUnit.ast.moduleInfo == null
            assert jUnit.compiledClasses.contains('demo.bs.Helper')
        } finally {
            jLoader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testStaleInnerClassInReusedTargetDirectoryIsNotIndexed() {
        def root = dir('stale-inner')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        def stubs = new File(root, 'stubs')
        src.mkdirs()
        out.mkdirs()
        stubs.mkdirs()

        def boxFile = new File(src, 'demo/box/Box.java')
        boxFile.parentFile.mkdirs()
        boxFile.text = '''
            package demo.box;
            public class Box {
                public static class RemovedInner implements Runnable {
                    public void run() {}
                }
                public static class KeptInner implements Runnable {
                    public void run() {}
                }
            }
            class AuxiliaryTopLevel {}
        '''
        def groovyPeer = new File(src, 'demo/box/GroovyPeer.groovy')
        groovyPeer.text = '''
            package demo.box
            class GroovyPeer {
                static String tag() { 'peer' }
            }
        '''
        def modFile = new File(src, 'module-info.groovy')
        modFile.text = '''
            module demo.box {
                exports demo.box;
                provides java.lang.Runnable with demo.box.Box.KeptInner;
            }
        '''

        // First compile writes Box.class, Box$RemovedInner.class, Box$KeptInner.class, and AuxiliaryTopLevel.class.
        def config1 = new CompilerConfiguration(targetDirectory: out)
        config1.jointCompilationOptions = [stubDir: stubs]
        def loader1 = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit1 = new JavaAwareCompilationUnit(config1, loader1)
            unit1.addSources([modFile, groovyPeer, boxFile] as File[])
            unit1.compile()
            assert unit1.compiledClasses.contains('demo.box.Box')
            assert unit1.compiledClasses.contains('demo.box.Box$RemovedInner')
            assert unit1.compiledClasses.contains('demo.box.Box$KeptInner')
            assert unit1.compiledClasses.contains('demo.box.AuxiliaryTopLevel')
            assert !unit1.compiledClasses.contains('demo.box.GroovyPeer')
            assert new File(out, 'demo/box/Box$RemovedInner.class').isFile()
        } finally {
            loader1.close()
        }

        // Remove RemovedInner from Box.java while leaving stale Box$RemovedInner.class on disk in out.
        boxFile.text = '''
            package demo.box;
            public class Box {
                public static class KeptInner implements Runnable {
                    public void run() {
                        GroovyPeer.tag();
                    }
                }
            }
        '''
        def stubs2 = new File(root, 'stubs2')
        stubs2.mkdirs()
        def config2 = new CompilerConfiguration(targetDirectory: out)
        config2.jointCompilationOptions = [stubDir: stubs2]
        def loader2 = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit2 = new JavaAwareCompilationUnit(config2, loader2)
            unit2.addSources([modFile, groovyPeer, boxFile] as File[])
            unit2.compile()
            assert unit2.compiledClasses.contains('demo.box.Box')
            assert unit2.compiledClasses.contains('demo.box.Box$KeptInner')
            // Stale inner class and removed auxiliary class must not be indexed even though their .class files remain in out.
            assert !unit2.compiledClasses.contains('demo.box.Box$RemovedInner')
            assert !unit2.compiledClasses.contains('demo.box.AuxiliaryTopLevel')
            // Groovy stub compiled implicitly by javac must not leak into CompiledClassIndex.
            assert !unit2.compiledClasses.contains('demo.box.GroovyPeer')
        } finally {
            loader2.close()
        }

        // Referencing the stale Box$RemovedInner as a service provider must fail.
        modFile.text = '''
            module demo.box {
                exports demo.box;
                provides java.lang.Runnable with demo.box.Box.RemovedInner;
            }
        '''
        def stubs3 = new File(root, 'stubs3')
        stubs3.mkdirs()
        def config3 = new CompilerConfiguration(targetDirectory: out)
        config3.jointCompilationOptions = [stubDir: stubs3]
        def loader3 = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit3 = new JavaAwareCompilationUnit(config3, loader3)
            unit3.addSources([modFile, groovyPeer, boxFile] as File[])
            def err = GroovyAssert.shouldFail(CompilationFailedException) {
                unit3.compile()
            }
            assert err.message.contains('must be declared in the current module')
        } finally {
            loader3.close()
        }

        // Recompiling unchanged Box.java immediately (same size and coarse mtime) still indexes Box via RecordingJavaFileManager.
        modFile.text = '''
            module demo.box {
                exports demo.box;
                provides java.lang.Runnable with demo.box.Box.KeptInner;
            }
        '''
        def config4 = new CompilerConfiguration(targetDirectory: out)
        config4.jointCompilationOptions = [memStub: true]
        def loader4 = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit4 = new JavaAwareCompilationUnit(config4, loader4)
            unit4.addSources([modFile, groovyPeer, boxFile] as File[])
            unit4.compile()
            assert unit4.compiledClasses.contains('demo.box.Box')
            assert unit4.compiledClasses.contains('demo.box.Box$KeptInner')
            assert !unit4.compiledClasses.contains('demo.box.Box$RemovedInner')
            assert !unit4.compiledClasses.contains('demo.box.GroovyPeer')
        } finally {
            loader4.close()
        }
    }

    // GROOVY-9273
    @Test
    void testStagedJointCompilationPreservesCompiledClassIndex() {
        def root = dir('staged-joint')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        def stubs = new File(root, 'stubs')
        src.mkdirs()
        out.mkdirs()
        stubs.mkdirs()
        def modFile = new File(src, 'module-info.groovy')
        modFile.text = 'module demo.staged { exports demo.javaonly; }'
        def javaFile = new File(src, 'demo/javaonly/Api.java')
        javaFile.parentFile.mkdirs()
        javaFile.text = 'package demo.javaonly; public class Api {}'
        def config = new CompilerConfiguration(targetDirectory: out)
        config.jointCompilationOptions = [stubDir: stubs]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources([modFile, javaFile] as File[])
            unit.compile(Phases.SEMANTIC_ANALYSIS)
            assert unit.compiledClasses.contains('demo.javaonly.Api')
            unit.compile(Phases.ALL)
            assert unit.compiledClasses.contains('demo.javaonly.Api')
            def md = descriptor(unit)
            assert md.exports()*.source() == ['demo.javaonly']
            assert md.packages().contains('demo.javaonly')
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testCustomJavaCompilerFallbackDiffWithoutStaleEntries() {
        def root = dir('custom-compiler-disk')
        def src = new File(root, 'src')
        def out = new File(root, 'out')
        def stubs = new File(root, 'stubs')
        src.mkdirs()
        out.mkdirs()
        stubs.mkdirs()

        // Seed stale files including an unchanged stale inner class, a stale Box.class to be overwritten, and a non-class resource file.
        def staleDir = new File(out, 'demo/custom')
        staleDir.mkdirs()
        new File(staleDir, 'Box$StaleInner.class').bytes = [1, 2, 3] as byte[]
        new File(staleDir, 'Box.class').bytes = [0] as byte[]
        new File(staleDir, 'README.txt').text = 'ignore'

        def modFile = new File(src, 'module-info.groovy')
        modFile.text = 'module demo.custom { exports demo.custom; }'
        def javaFile = new File(src, 'demo/custom/Box.java')
        javaFile.parentFile.mkdirs()
        javaFile.text = 'package demo.custom; public class Box {}'

        def config = new CompilerConfiguration(targetDirectory: out)
        config.jointCompilationOptions = [stubDir: stubs]
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.compilerFactory = { CompilerConfiguration cfg ->
                return { List<String> sources, CompilationUnit cu ->
                    def cw = new ClassWriter(0)
                    cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, 'demo/custom/Box', null, 'java/lang/Object', null)
                    cw.visitEnd()
                    new File(staleDir, 'Box.class').bytes = cw.toByteArray()
                } as JavaCompiler
            } as JavaCompilerFactory
            unit.addSources([modFile, javaFile] as File[])
            unit.compile()
            assert unit.compiledClasses.contains('demo.custom.Box')
            assert !unit.compiledClasses.contains('demo.custom.Box$StaleInner')
            assert descriptor(unit).exports()*.source() == ['demo.custom']
        } finally {
            loader.close()
        }

        // Custom JavaCompiler with a non-existent targetDirectory safely returns empty snapshot.
        def missingTargetRoot = dir('custom-compiler-missing-dir')
        def missingOut = new File(missingTargetRoot, 'not-created-yet')
        def mStubs = new File(missingTargetRoot, 'stubs')
        mStubs.mkdirs()
        def mConfig = new CompilerConfiguration(targetDirectory: missingOut)
        mConfig.jointCompilationOptions = [stubDir: mStubs]
        def mLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def mUnit = new JavaAwareCompilationUnit(mConfig, mLoader)
            mUnit.compilerFactory = { CompilerConfiguration cfg ->
                return { List<String> sources, CompilationUnit cu -> } as JavaCompiler
            } as JavaCompilerFactory
            def emptyMod = new File(missingTargetRoot, 'module-info.groovy')
            emptyMod.text = 'module demo.missingdir {}'
            mUnit.addSources([emptyMod, javaFile] as File[])
            mUnit.compile(Phases.SEMANTIC_ANALYSIS)
            assert mUnit.compiledClasses.empty
        } finally {
            mLoader.close()
        }

        // Custom JavaCompiler with an unreadable target subdirectory reports an indexing error (both pre- and post-compile).
        if (FileSystems.default.supportedFileAttributeViews().contains('posix')) {
            def blockedRoot = dir('custom-compiler-blocked')
            def blockedOut = new File(blockedRoot, 'out')
            def blockedSub = new File(blockedOut, 'locked')
            blockedSub.mkdirs()
            new File(blockedSub, 'X.class').bytes = [1] as byte[]
            Files.setPosixFilePermissions(blockedSub.toPath(), EnumSet.noneOf(PosixFilePermission))
            def bStubs = new File(blockedRoot, 'stubs')
            bStubs.mkdirs()
            def bConfig = new CompilerConfiguration(targetDirectory: blockedOut)
            bConfig.jointCompilationOptions = [stubDir: bStubs]
            def bLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
            try {
                def bUnit = new JavaAwareCompilationUnit(bConfig, bLoader)
                bUnit.compilerFactory = { CompilerConfiguration cfg ->
                    return { List<String> sources, CompilationUnit cu -> } as JavaCompiler
                } as JavaCompilerFactory
                bUnit.addSources([modFile, javaFile] as File[])
                def err = GroovyAssert.shouldFail(CompilationFailedException) {
                    bUnit.compile()
                }
                assert err.message.contains('Failed to index class files')
            } finally {
                Files.setPosixFilePermissions(blockedSub.toPath(), EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE))
                bLoader.close()
            }

            // Post-compile snapshot failure when directory becomes unreadable during Java compilation.
            def postRoot = dir('custom-compiler-post-blocked')
            def postOut = new File(postRoot, 'out')
            def postSub = new File(postOut, 'locked-after')
            postSub.mkdirs()
            def pStubs = new File(postRoot, 'stubs')
            pStubs.mkdirs()
            def pConfig = new CompilerConfiguration(targetDirectory: postOut)
            pConfig.jointCompilationOptions = [stubDir: pStubs]
            def pLoader = new GroovyClassLoader(GroovyAssert.class.classLoader)
            try {
                def pUnit = new JavaAwareCompilationUnit(pConfig, pLoader)
                pUnit.compilerFactory = { CompilerConfiguration cfg ->
                    return { List<String> sources, CompilationUnit cu ->
                        Files.setPosixFilePermissions(postSub.toPath(), EnumSet.noneOf(PosixFilePermission))
                    } as JavaCompiler
                } as JavaCompilerFactory
                pUnit.addSources([modFile, javaFile] as File[])
                def err = GroovyAssert.shouldFail(CompilationFailedException) {
                    pUnit.compile()
                }
                assert err.message.contains('Failed to index class files')
            } finally {
                Files.setPosixFilePermissions(postSub.toPath(), EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE))
                pLoader.close()
            }
        }
    }

    // GROOVY-9273
    @Test
    void testProtectedNestedClassAccessibleToSubclassInNamedModule() {
        def libClasses = dir('prot-lib-classes')
        compile(libClasses, [
            'Base.groovy': '''
                package demo.lib
                import groovy.transform.PackageScope
                class Base {
                    static class PubNested {}
                    protected static class ProtNested {}
                }
                @PackageScope
                class PkgTop {}
            '''
        ])
        def libJar = new File(tmp, 'prot-lib.jar')
        moduleJar(libJar, 'demo.lib', 'demo.lib', null, libClasses)

        // Subclass and nested class inside subclass may reference protected nested type from required module.
        compile(dir('prot-sub-ok'), libJar, [
            'module-info.groovy': 'module demo.app { requires demo.lib; }',
            'Sub.groovy': '''
                package demo.app
                import demo.lib.Base
                class Sub extends Base {
                    Base.PubNested a
                    Base.ProtNested b
                    ProtNested c
                    static class Child {
                        Base.ProtNested d
                    }
                    static def inheritedNestedClassRef() { Sub.ProtNested }
                }
            '''
        ])

        // Unrelated class outside demo.lib cannot access protected nested type or package-private top-level type.
        def protDenied = fail(dir('prot-unrelated'), libJar, [
            'module-info.groovy': 'module demo.app { requires demo.lib; }',
            'Unrelated.groovy': '''
                package demo.app
                class Unrelated {
                    demo.lib.Base.ProtNested p
                }
            '''
        ])
        assert protDenied.contains('not accessible')

        def pkgDenied = fail(dir('pkg-unrelated'), libJar, [
            'module-info.groovy': 'module demo.app { requires demo.lib; }',
            'Unrelated.groovy': '''
                package demo.app
                class Unrelated {
                    demo.lib.PkgTop p
                }
            '''
        ])
        assert pkgDenied.contains('not accessible')
    }

    // GROOVY-9273
    @Test
    void testNamedModuleAccessibilityAcrossLanguageConstructs() {
        Map<String, String> cases = [
            'extends-type': 'package demo.acc; class A extends java.awt.Frame {}',
            'implements-type': 'package demo.acc; abstract class A implements java.sql.Driver {}',
            'method-return': 'package demo.acc; class A { java.awt.Frame m() { null } }',
            'method-param': 'package demo.acc; class A { void m(java.awt.Frame f) {} }',
            'throws-type': 'package demo.acc; class A { void m() throws javax.xml.parsers.ParserConfigurationException {} }',
            'local-var': 'package demo.acc; class A { void m() { java.awt.Frame f = null } }',
            'ctor-call': 'package demo.acc; class A { def m() { new java.awt.Frame() } }',
            'array-new': 'package demo.acc; class A { def m() { new java.awt.Frame[1] } }',
            'cast-java': 'package demo.acc; class A { def m(x) { (java.awt.Frame) x } }',
            'cast-as': 'package demo.acc; class A { def m(x) { x as java.awt.Frame } }',
            'instanceof-op': 'package demo.acc; class A { boolean m(x) { x instanceof java.awt.Frame } }',
            'catch-clause': 'package demo.acc; class A { void m() { try {} catch (javax.xml.parsers.ParserConfigurationException e) {} } }',
            'generic-arg': 'package demo.acc; class A { List<java.awt.Frame> list }',
            'generic-bound': 'package demo.acc; class A<T extends java.awt.Frame> {}',
            'annotation-use': 'package demo.acc; @javax.annotation.processing.Generated("x") class A {}',
            'method-ref': 'package demo.acc; class A { def c = java.awt.Frame::new }',
            'unused-import': 'package demo.acc; import java.awt.Frame; class A {}',
            'unused-star-import': 'package demo.acc; import java.awt.*; class A {}',
            'nested-dot': 'package demo.acc; class A { java.awt.Dialog.ModalityType m }',
            'nested-dollar': 'package demo.acc; class A { java.awt.Dialog$ModalityType m }',
            'concealed-jdk': 'package demo.acc; class A { jdk.internal.misc.Unsafe u }'
        ]
        cases.each { label, source ->
            def err = fail(dir('acc-' + label), [
                'module-info.groovy': 'module demo.acc { exports demo.acc; }',
                'A.groovy': source
            ])
            assert err.contains('not accessible') : "Expected accessibility error for ${label}, got: ${err}"
        }

        // Own Groovy package and joint-compiled Java-only package star imports and references succeed in a named module.
        compileJoint(dir('acc-own-joint'), [
            'module-info.groovy': 'module demo.ownj { exports demo.ownj.g; exports demo.ownj.j; }',
            'demo/ownj/j/JavaPeer.java': 'package demo.ownj.j; public class JavaPeer {}',
            'demo/ownj/g/GroovyPeer.groovy': 'package demo.ownj.g; class GroovyPeer {}',
            'demo/ownj/use/Consumer.groovy': '''
                package demo.ownj.use
                import demo.ownj.g.*
                import demo.ownj.j.*
                class Consumer {
                    GroovyPeer g
                    JavaPeer j
                }
            '''
        ])
    }

    // GROOVY-9273
    @Test
    void testNamedModuleTransitiveQualifiedAndStaticReadabilityInSourceClasses() {
        def mods = dir('mods-src-read')
        def libDir = new File(mods, 'demo.lib')
        def midDir = new File(mods, 'demo.mid')
        def nonTransDir = new File(mods, 'demo.nontrans')
        compile(libDir, [
            'module-info.groovy': '''
                module demo.lib {
                    exports demo.lib.api;
                    exports demo.lib.friend to demo.friend;
                }
            ''',
            'Api.groovy': 'package demo.lib.api; class Api {}',
            'Friend.groovy': 'package demo.lib.friend; class Friend {}',
            'Secret.groovy': 'package demo.lib.secret; class Secret {}'
        ])
        compile(midDir, [libDir], [
            'module-info.groovy': 'module demo.mid { requires transitive demo.lib; }'
        ])
        compile(nonTransDir, [libDir], [
            'module-info.groovy': 'module demo.nontrans { requires demo.lib; }'
        ])
        def modPath = [libDir, midDir, nonTransDir]

        // Transitive readability grants access to demo.lib.api in Groovy source classes.
        compile(new File(mods, 'demo.app'), modPath, [
            'module-info.groovy': 'module demo.app { requires demo.mid; }',
            'App.groovy': 'package demo.app; class App { demo.lib.api.Api api }'
        ])

        // Concealed package of required module is rejected in source classes.
        assert fail(dir('src-concealed'), modPath, [
            'module-info.groovy': 'module demo.app { requires demo.lib; }',
            'App.groovy': 'package demo.app; class App { demo.lib.secret.Secret s }'
        ]).contains('not accessible')

        // Qualified export allows friend and denies non-friend in source classes.
        compile(dir('src-friend-ok'), modPath, [
            'module-info.groovy': 'module demo.friend { requires demo.lib; }',
            'UseFriend.groovy': 'package demo.friend; class UseFriend { demo.lib.friend.Friend f }'
        ])
        assert fail(dir('src-stranger-denied'), modPath, [
            'module-info.groovy': 'module demo.stranger { requires demo.lib; }',
            'UseFriend.groovy': 'package demo.stranger; class UseFriend { demo.lib.friend.Friend f }'
        ]).contains('not accessible')

        // Non-transitive dependency does not leak readability to source classes.
        assert fail(dir('src-nontrans-denied'), modPath, [
            'module-info.groovy': 'module demo.blocked { requires demo.nontrans; }',
            'Blocked.groovy': 'package demo.blocked; class Blocked { demo.lib.api.Api api }'
        ]).contains('not accessible')

        // requires static is readable at compile time in source classes.
        compile(dir('src-static-ok'), [
            'module-info.groovy': 'module demo.stat { requires static java.sql; }',
            'UseSql.groovy': 'package demo.stat; class UseSql { java.sql.Driver d }'
        ])
    }

    // GROOVY-9273
    @Test
    void testRequiresStaticCycleAndCrossModuleSplitPackages() {
        // Direct cycle via requires static
        def cycleJar = new File(tmp, 'static-cycle-b.jar')
        moduleJarRequires(cycleJar, 'scycle.b', 'scycle.a')
        def directCycle = fail(dir('static-cycle-direct'), cycleJar, [
            'module-info.groovy': 'module scycle.a { requires static scycle.b; }'
        ])
        assert directCycle.toLowerCase().contains('cyclic')

        // Indirect cycle via requires static: a -> static b -> c -> a
        def cycleDir = dir('static-cycle-indirect-libs')
        moduleJarRequires(new File(cycleDir, 'sc2-b.jar'), 'sc2.b', 'sc2.c')
        moduleJarRequires(new File(cycleDir, 'sc2-c.jar'), 'sc2.c', 'sc2.a')
        def indirectCycle = fail(dir('static-cycle-indirect'), cycleDir, [
            'module-info.groovy': 'module sc2.a { requires static sc2.b; }'
        ])
        assert indirectCycle.toLowerCase().contains('cyclic')

        // Split package between requires mod.a and requires static mod.b
        def splitLibs = dir('split-static-libs')
        def clsA = dir('split-cls-a')
        compile(clsA, ['A.groovy': 'package shared.pkg; class A {}'])
        moduleJar(new File(splitLibs, 'mod-a.jar'), 'mod.a', 'shared.pkg', null, clsA)
        def clsB = dir('split-cls-b')
        compile(clsB, ['B.groovy': 'package shared.pkg; class B {}'])
        moduleJar(new File(splitLibs, 'mod-b.jar'), 'mod.b', 'shared.pkg', null, clsB)
        def splitErr = fail(dir('split-static-app'), splitLibs, [
            'module-info.groovy': '''
                module split.app {
                    requires mod.a;
                    requires static mod.b;
                }
            '''
        ])
        assert splitErr.contains('reads package shared.pkg from both')
    }

    // GROOVY-9273
    @Test
    void testModuleInfoImportsAndAnnotationValuesCheckedForAccessibility() {
        // Unused unreadable imports in module-info.groovy
        assert fail(dir('minfo-imp-single'), [
            'module-info.groovy': 'import java.awt.Frame; module demo.mi1 {}'
        ]).contains('not accessible')
        assert fail(dir('minfo-imp-star'), [
            'module-info.groovy': 'import java.awt.*; module demo.mi2 {}'
        ]).contains('not accessible')
        assert fail(dir('minfo-imp-static'), [
            'module-info.groovy': 'import static java.awt.Color.RED; module demo.mi3 {}'
        ]).contains('not accessible')
        assert fail(dir('minfo-imp-static-star'), [
            'module-info.groovy': 'import static java.awt.Color.*; module demo.mi4 {}'
        ]).contains('not accessible')

        def annoDefs = [
            'Annos.groovy': '''
                package demo.an
                import java.lang.annotation.ElementType
                import java.lang.annotation.Repeatable
                import java.lang.annotation.Retention
                import java.lang.annotation.RetentionPolicy
                import java.lang.annotation.Target
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface TypeRef { Class<?> value() }
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface TypeRefs { Class<?>[] value() }
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface IntVal { int value() }
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface OuterRef { TypeRef value() }
                @Target(ElementType.TYPE_USE)
                @interface TypeUseOnly {}
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @interface Tags { Tag[] value() }
                @Target(ElementType.MODULE)
                @Retention(RetentionPolicy.RUNTIME)
                @Repeatable(Tags)
                @interface Tag { String value() }
            '''
        ]

        // Annotation value referencing unreadable class / array / enum-or-constant / nested annotation
        assert fail(dir('minfo-ann-cls'), annoDefs + [
            'module-info.groovy': '@demo.an.TypeRef(java.awt.Frame) module demo.an { exports demo.an; }'
        ]).contains('not accessible')
        assert fail(dir('minfo-ann-arr'), annoDefs + [
            'module-info.groovy': '@demo.an.TypeRefs([String, java.awt.Frame[]]) module demo.an { exports demo.an; }'
        ]).contains('not accessible')
        assert fail(dir('minfo-ann-prop'), annoDefs + [
            'module-info.groovy': '@demo.an.IntVal(java.awt.event.KeyEvent.VK_ENTER) module demo.an { exports demo.an; }'
        ]).contains('not accessible')
        assert fail(dir('minfo-ann-nested'), annoDefs + [
            'module-info.groovy': '@demo.an.OuterRef(@demo.an.TypeRef(java.awt.Frame)) module demo.an { exports demo.an; }'
        ]).contains('not accessible')

        // With requires java.desktop, those annotation values, primitive/simple class literals, and imported constants succeed.
        compile(dir('minfo-ann-ok'), annoDefs + [
            'module-info.groovy': '''
                import java.awt.event.KeyEvent
                @demo.an.TypeRef(int)
                @demo.an.IntVal(KeyEvent.VK_ENTER)
                @demo.an.TypeRefs([String, java.awt.Frame[]])
                @demo.an.OuterRef(@demo.an.TypeRef(java.awt.Frame))
                module demo.an {
                    requires java.desktop;
                    exports demo.an;
                }
            '''
        ])

        // TYPE_USE-only annotation is rejected on module declaration (JLS §9.6.4.1).
        assert fail(dir('minfo-ann-typeuse'), annoDefs + [
            'module-info.groovy': '@demo.an.TypeUseOnly module demo.an { exports demo.an; }'
        ]).contains('MODULE')

        // Invalid annotation on an import inside module-info.groovy is checked by ExtendedVerifier.
        assert fail(dir('minfo-imp-anno'), [
            'module-info.groovy': '''
                @Override
                import java.lang.Runnable
                module demo.impan {}
            '''
        ]).contains('IMPORT')

        // Repeatable annotations on module-info.groovy are wrapped into the container annotation.
        def repUnit = compile(dir('minfo-ann-repeat'), annoDefs + [
            'module-info.groovy': '''
                @demo.an.Tag("first")
                @demo.an.Tag("second")
                module demo.an { exports demo.an; }
            '''
        ])
        def raw = new String(repUnit.classes.find { it.name == 'module-info' }.bytes, 'ISO-8859-1')
        assert raw.contains('Ldemo/an/Tags;')

        // Unreadable @AnnotationCollector on module-info is rejected even after expansion.
        def collClasses = dir('foreign-coll-classes')
        compile(collClasses, [
            'Coll.groovy': '''
                package demo.fcoll
                import groovy.transform.AnnotationCollector
                @AnnotationCollector([Deprecated])
                @interface ForeignColl {}
            '''
        ])
        def collJar = new File(tmp, 'foreign-coll.jar')
        moduleJar(collJar, 'demo.fcoll', 'demo.fcoll', null, collClasses)
        assert fail(dir('minfo-coll-unread'), collJar, [
            'module-info.groovy': '@demo.fcoll.ForeignColl module demo.usecoll {}'
        ]).contains('not accessible')
    }

    // GROOVY-9273
    @Test
    void testRequiresTransitiveJavaBaseOnTarget25Preview() {
        def target = dir('trans-base-25-preview')
        def config = new CompilerConfiguration(targetDirectory: target)
        config.targetBytecode = CompilerConfiguration.JDK25
        config.previewFeatures = true
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('module-info.groovy', 'module demo.tbp { requires transitive java.base; }')
            unit.compile()
            def bytes = unit.classes.find { it.name == 'module-info' }.bytes
            int version = 0
            def flags = [:]
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                void visit(int v, int access, String name, String sig, String superName, String[] ifaces) {
                    version = v
                }

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
            assert version == (Opcodes.V25 | Opcodes.V_PREVIEW)
            assert (flags['java.base'] & Opcodes.ACC_TRANSITIVE) != 0
        } finally {
            loader.close()
        }
    }

    // GROOVY-9273
    @Test
    void testModuleInfoResolveVisitorAdvancedForms() {
        // Class star import, static star import, aliased import, aliased static nested import, and fallback from type head to package.
        def unit = compile(dir('resolve-adv'), [
            'module-info.groovy': '''
                import demo.adv.Outer.*
                import static demo.adv.StaticHolder.*
                import java.sql.Driver as SqlDriver
                import static demo.adv.Outer.Second as AliasSecond
                import demo.adv.other.pkg
                module demo.adv {
                    requires java.sql;
                    uses SqlDriver;
                    provides java.lang.Runnable with First, Third, AliasSecond, pkg.RealImpl;
                }
            ''',
            'Outer.groovy': '''
                package demo.adv
                class Outer {
                    static class First implements Runnable { void run() {} }
                    static class Second implements Runnable { void run() {} }
                }
                class StaticHolder {
                    static class Third implements Runnable { void run() {} }
                }
            ''',
            'PkgType.groovy': 'package demo.adv.other; class pkg {}',
            'RealImpl.groovy': 'package pkg; class RealImpl implements Runnable { void run() {} }'
        ])
        def md = descriptor(unit)
        assert md.uses() == ['java.sql.Driver'] as Set
        assert md.provides().find { it.service() == 'java.lang.Runnable' }.providers() as List ==
            ['demo.adv.Outer$First', 'demo.adv.StaticHolder$Third', 'demo.adv.Outer$Second', 'pkg.RealImpl']

        // Ambiguous qualified head Outer.Inner reports ambiguity on Outer.
        def ambigHead = fail(dir('resolve-ambig-head'), [
            'module-info.groovy': '''
                import demo.a.*
                import demo.b.*
                module demo.amb {
                    provides java.lang.Runnable with Outer.Inner;
                }
            ''',
            'A.groovy': 'package demo.a; class Outer { static class Inner implements Runnable { void run() {} } }',
            'B.groovy': 'package demo.b; class Outer { static class Inner implements Runnable { void run() {} } }'
        ])
        assert ambigHead.contains('ambiguous')

        // Ambiguous module star imports report ambiguity.
        def ambigModDir = dir('resolve-ambig-mod-libs')
        def m1Cls = dir('amb-m1-cls')
        compile(m1Cls, ['S.groovy': 'package amb.one; interface SharedSpi {}'])
        def ambOneJar = new File(ambigModDir, 'amb-one.jar')
        moduleJar(ambOneJar, 'amb.one', 'amb.one', null, m1Cls)
        def m2Cls = dir('amb-m2-cls')
        compile(m2Cls, ['S.groovy': 'package amb.two; interface SharedSpi {}'])
        def ambTwoJar = new File(ambigModDir, 'amb-two.jar')
        moduleJar(ambTwoJar, 'amb.two', 'amb.two', null, m2Cls)
        def ambigMod = fail(dir('resolve-ambig-mod'), [ambOneJar, ambTwoJar], [
            'module-info.groovy': '''
                import module amb.one
                import module amb.two
                module demo.ambmod {
                    requires amb.one;
                    requires amb.two;
                    uses SharedSpi;
                }
            '''
        ])
        assert ambigMod.contains('ambiguous')
    }

    // GROOVY-9273
    @Test
    void testProviderFormsExtendedAndPackageInfoEmpty() {
        // Void provider() rejected.
        assert fail(dir('void-provider'), [
            'module-info.groovy': 'module demo.vp { provides java.lang.Object with demo.vp.Factory; }',
            'Factory.groovy': 'package demo.vp; class Factory { static void provider() {} }'
        ]).contains('must return a subtype')

        // Interface, abstract class, generic provider(), 0-component record, generic service, and trait service/provider.
        compile(dir('extended-providers'), [
            'module-info.groovy': '''
                module demo.ep {
                    exports demo.ep;
                    uses demo.ep.GenSvc;
                    uses demo.ep.TraitSvc;
                    provides java.lang.Runnable with demo.ep.IfaceFactory, demo.ep.AbsFactory, demo.ep.GenFactory, demo.ep.EmptyRec;
                    provides demo.ep.GenSvc with demo.ep.GenImpl;
                    provides demo.ep.TraitSvc with demo.ep.TraitImpl;
                }
            ''',
            'All.groovy': '''
                package demo.ep
                interface IfaceFactory {
                    static Runnable provider() { new EmptyRec() }
                }
                abstract class AbsFactory {
                    static Runnable provider() { new EmptyRec() }
                }
                class GenFactory {
                    static <T> Runnable provider() { new EmptyRec() }
                }
                record EmptyRec() implements Runnable {
                    void run() {}
                }
                interface GenSvc<T> {}
                class GenImpl implements GenSvc<String> {}
                trait TraitSvc {}
                class TraitImpl implements TraitSvc {}
            '''
        ])

        // Joint-compiled non-static Java inner class is rejected as a service provider.
        def nonStaticJava = GroovyAssert.shouldFail(CompilationFailedException) {
            compileJoint(dir('joint-nonstatic-inner'), [
                'module-info.groovy': 'module demo.nsi { provides java.lang.Runnable with demo.nsi.Outer.Inner; }',
                'demo/nsi/Outer.java': '''
                    package demo.nsi;
                    public class Outer {
                        public class Inner implements Runnable {
                            public void run() {}
                        }
                    }
                '''
            ])
        }
        assert nonStaticJava.message.contains('top-level or static')

        // A package containing only package-info.groovy does not satisfy exports (JLS §7.7.2 / javac).
        assert fail(dir('pkg-info-only-groovy'), [
            'module-info.groovy': 'module demo.pio { exports demo.doc; }',
            'demo/doc/package-info.groovy': '@Deprecated package demo.doc'
        ]).contains('package is empty or does not exist: demo.doc')

        // Same for joint-compiled annotated package-info.java alone.
        def pioJava = GroovyAssert.shouldFail(CompilationFailedException) {
            compileJoint(dir('pkg-info-only-java'), [
                'module-info.groovy': 'module demo.pioj { exports demo.doc; }',
                'demo/doc/package-info.java': '@Deprecated package demo.doc;'
            ])
        }
        assert pioJava.message.contains('package is empty or does not exist: demo.doc')
    }

    // GROOVY-9273
    @Test
    void testGrammarStrictSemicolonsContextualKeywordsAndErrorRecovery() {
        // Stray semicolons inside module body or after closing brace are syntax errors (JLS §7.3 / §7.7).
        assert fail(dir('semi-double'), [
            'module-info.groovy': 'module demo.s { requires java.sql;; }'
        ]) != null
        assert fail(dir('semi-empty'), [
            'module-info.groovy': 'module demo.s { ; }'
        ]) != null
        assert fail(dir('semi-trailing'), [
            'module-info.groovy': 'module demo.s { };'
        ]) != null

        // Contextual keywords remain legal as module, package, and target names.
        def ctxUnit = compile(dir('ctx-keywords'), [
            'module-info.groovy': '''
                open module requires.exports.opens.uses.provides.to.with.transitive.open.module {
                    exports to.with to requires, opens;
                }
            ''',
            'A.groovy': 'package to.with; class A {}'
        ])
        assert descriptor(ctxUnit).name() == 'requires.exports.opens.uses.provides.to.with.transitive.open.module'
        assert descriptor(ctxUnit).open
        assert descriptor(ctxUnit).exports()[0].targets() == ['requires', 'opens'] as Set

        // open module open and module module are legal.
        assert descriptor(compile(dir('mod-open'), ['module-info.groovy': 'open module open {}'])).name() == 'open'
        assert descriptor(compile(dir('mod-module'), ['module-info.groovy': 'module module {}'])).name() == 'module'

        // Single-line Java module-info syntax (imports + multiple directives on one line) remains 100% compatible.
        def oneLineUnit = compile(dir('java-single-line'), [
            'module-info.groovy': 'import java.sql.Driver; module demo.oneline { requires java.sql; exports demo.ol; uses Driver; }',
            'A.groovy': 'package demo.ol; class A {}'
        ])
        assert descriptor(oneLineUnit).uses() == ['java.sql.Driver'] as Set
        assert descriptor(oneLineUnit).exports()*.source() == ['demo.ol']

        // Star import skips package-private types in other packages (JLS §7.5.2 / javac 21), avoiding false ambiguity.
        def ppClsA = dir('star-pp-a')
        compile(ppClsA, [
            'Helper.groovy': 'package demo.ppa; import groovy.transform.PackageScope; @PackageScope class Helper {}'
        ])
        def ppJarA = new File(tmp, 'star-pp-a.jar')
        moduleJar(ppJarA, 'demo.ppa', 'demo.ppa', null, ppClsA)
        def ppClsB = dir('star-pp-b')
        compile(ppClsB, [
            'Helper.groovy': 'package demo.ppb; interface Helper {}'
        ])
        def ppJarB = new File(tmp, 'star-pp-b.jar')
        moduleJar(ppJarB, 'demo.ppb', 'demo.ppb', null, ppClsB)
        def disambigUnit = compile(dir('star-pp-disambig'), [ppJarA, ppJarB], [
            'module-info.groovy': '''
                import demo.ppa.*;
                import demo.ppb.*;
                module demo.ppuse {
                    requires demo.ppa;
                    requires demo.ppb;
                    uses Helper;
                }
            '''
        ])
        assert descriptor(disambigUnit).uses() == ['demo.ppb.Helper'] as Set

        // Duplicate requires modifiers are rejected by the parser/AST builder (JLS §7.7.1).
        assert fail(dir('dup-static-mod'), [
            'module-info.groovy': 'module demo.ds { requires static static java.sql; }'
        ]).contains("modifier 'static' appears more than once")
        assert fail(dir('dup-trans-mod'), [
            'module-info.groovy': 'module demo.dt { requires transitive transitive java.sql; }'
        ]).contains("modifier 'transitive' appears more than once")

        // Multiple import module statements and import module following a star import.
        def modImpUnit = compile(dir('multi-imp-mod'), [
            'module-info.groovy': '''
                import java.lang.*
                import module java.sql
                import module java.logging
                module demo.mim {
                    requires java.sql;
                    requires java.logging;
                    uses Driver;
                }
            '''
        ])
        assert descriptor(modImpUnit).uses() == ['java.sql.Driver'] as Set

        // Invalid numeric literal in module annotation reports a parsing error.
        assert fail(dir('bad-num-mod-anno'), [
            'module-info.groovy': '@Deprecated(since = 999999999999999999999999999999999999999i) module demo.num {}'
        ]) != null

        // ERROR_RECOVERY mode does not crash on incomplete module-info.groovy.
        ['', 'import java.lang.Runnable', 'module {}', 'module demo.broken { bad }'].each { broken ->
            def config = new CompilerConfiguration()
            config.optimizationOptions[CompilerConfiguration.ERROR_RECOVERY] = true
            def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
            try {
                def unit = new CompilationUnit(config, null, loader)
                unit.addSource('module-info.groovy', broken)
                GroovyAssert.shouldFail(CompilationFailedException) {
                    unit.compile(Phases.CONVERSION)
                }
            } finally {
                loader.close()
            }
        }
    }

    // GROOVY-9273
    @Test
    void testMultiModuleRuntimeInteropAndAsmBytecodeVerification() {
        def mods = dir('mods-runtime')
        def spiDir = new File(mods, 'demo.spi')
        def implDir = new File(mods, 'demo.impl')
        def appDir = new File(mods, 'demo.app')
        def spiUnit = compile(spiDir, [
            'module-info.groovy': '''
                module demo.spi {
                    exports demo.spi;
                }
            ''',
            'Greeter.groovy': '''
                package demo.spi
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                interface Greeter { String greet() }
            '''
        ])
        def implUnit = compile(implDir, [spiDir], [
            'module-info.groovy': '''
                module demo.impl {
                    requires demo.spi;
                    opens demo.impl.reflect to demo.app;
                    provides demo.spi.Greeter with demo.impl.internal.HelloGreeter;
                }
            ''',
            'HelloGreeter.groovy': '''
                package demo.impl.internal
                import demo.spi.Greeter
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                class HelloGreeter implements Greeter {
                    private String secret = 'hidden'
                    String greet() { 'hello from module' }
                }
            ''',
            'ReflectTarget.groovy': '''
                package demo.impl.reflect
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                class ReflectTarget {
                    private String value = 'opened'
                }
            '''
        ])
        def appUnit = compile(appDir, [spiDir, implDir], [
            'module-info.groovy': '''
                module demo.app {
                    exports demo.app;
                    requires demo.spi;
                    uses demo.spi.Greeter;
                }
            ''',
            'AppMain.groovy': '''
                package demo.app
                import demo.spi.Greeter
                import groovy.transform.CompileStatic
                import groovy.transform.stc.POJO
                @CompileStatic @POJO
                class AppMain {
                    static String run(ModuleLayer layer) {
                        ServiceLoader.load(layer, Greeter).findFirst().get().greet()
                    }
                }
            '''
        ])

        // Verify every generated module-info.class passes ASM CheckClassAdapter validation.
        [spiUnit, implUnit, appUnit].each { CompilationUnit u ->
            def bytes = u.classes.find { it.name == 'module-info' }.bytes
            new ClassReader(bytes).accept(new CheckClassAdapter(new ClassWriter(0)), 0)
        }

        // Boot a real ModuleLayer and verify ServiceLoader + opens/concealed reflection semantics.
        def finder = ModuleFinder.of(mods.toPath())
        def cf = ModuleLayer.boot().configuration().resolveAndBind(finder, ModuleFinder.of(), Set.of('demo.app'))
        def layer = ModuleLayer.boot().defineModulesWithOneLoader(cf, ClassLoader.systemClassLoader)
        def appClass = layer.findLoader('demo.app').loadClass('demo.app.AppMain')
        assert appClass.getMethod('run', ModuleLayer).invoke(null, layer) == 'hello from module'

        def implMod = layer.findModule('demo.impl').get()
        def appMod = layer.findModule('demo.app').get()
        assert implMod.isOpen('demo.impl.reflect', appMod)
        assert !implMod.isOpen('demo.impl.internal', appMod)
        def concealedClass = layer.findLoader('demo.impl').loadClass('demo.impl.internal.HelloGreeter')
        GroovyAssert.shouldFail(InaccessibleObjectException) {
            concealedClass.getDeclaredField('secret').setAccessible(true)
        }
    }

    // GROOVY-9273: JEP 261 / JLS §7.7.1 requires static transitive & requires static automatic readability
    @Test
    void testRequiresStaticTransitiveAndStaticAutomaticReadability() {
        def mods = dir('mods-st')
        compile(new File(mods, 'demo.c'), [
            'module-info.groovy': 'module demo.c { exports demo.c; }',
            'C.groovy'          : 'package demo.c; class C {}'
        ])
        compileConfigured(new File(mods, 'demo.a'), { it.modulePath = mods.absolutePath }, [
            'module-info.groovy': '''
                module demo.a {
                    requires static transitive demo.c;
                    exports demo.a;
                }
            ''',
            'A.groovy'          : 'package demo.a; class A { demo.c.C ref }'
        ])
        // demo.b requires demo.a (non-static), and demo.a requires static transitive demo.c:
        // demo.b must read demo.c at compile time when demo.c is observable (javac parity).
        compileConfigured(new File(mods, 'demo.b'), { it.modulePath = mods.absolutePath }, [
            'module-info.groovy': '''
                module demo.b {
                    requires demo.a;
                    uses demo.c.C;
                }
            ''',
            'B.groovy'          : 'package demo.b; class B { demo.c.C ref }'
        ])

        // Two automatic modules on --module-path: requires static auto1 grants readability to auto2,
        // while requiring no automatic module encapsulates auto1 on --module-path.
        def autoMods = dir('mods-auto-static')
        def a1Classes = dir('a1-classes')
        compile(a1Classes, ['A1.groovy': 'package a1; class A1 {}'])
        packJar(new File(autoMods, 'auto1.jar'), 'auto1', a1Classes)
        def a2Classes = dir('a2-classes')
        compile(a2Classes, ['A2.groovy': 'package a2; class A2 {}'])
        packJar(new File(autoMods, 'auto2.jar'), 'auto2', a2Classes)

        compileConfigured(dir('reads-static-auto'), { it.modulePath = autoMods.absolutePath }, [
            'module-info.groovy': 'module demo.rsa { requires static auto1; }',
            'Use.groovy'        : 'package demo.rsa; class Use { a2.A2 other }'
        ])

        def blocked = failConfigured(dir('unread-auto-mp'), { it.modulePath = autoMods.absolutePath }, [
            'module-info.groovy': 'module demo.uam {}',
            'Use.groovy'        : 'package demo.uam; class Use { a1.A1 blocked }'
        ])
        assert blocked.contains('not accessible')
    }

    // GROOVY-9273: JEP 261 --module-path (exploded dirs + JMODs), --upgrade-module-path, and --module-version
    @Test
    void testJep261ModulePathUpgradeJmodAndVersion() {
        // Exploded module directory on --module-path (parent directory, not classpath entry)
        def mods = dir('mods-mp')
        compile(new File(mods, 'demo.dep'), [
            'module-info.groovy': 'module demo.dep { exports demo.dep; }',
            'Dep.groovy'        : 'package demo.dep; class Dep { static String tag() { "v1" } }'
        ])
        def appOut = dir('mods-mp-app')
        def appUnit = compileConfigured(appOut, {
            it.modulePath = mods.absolutePath
            it.moduleVersion = '1.2.3-beta'
        }, [
            'module-info.groovy': '''
                module demo.mpapp {
                    requires demo.dep;
                    exports demo.mpapp;
                }
            ''',
            'App.groovy'        : 'package demo.mpapp; class App { demo.dep.Dep dep }'
        ])
        def md = descriptor(appUnit)
        assert md.rawVersion() == Optional.of('1.2.3-beta')
        assert exec(javaBin(), '--module-path', appOut.absolutePath, '--describe-module', 'demo.mpapp').contains('demo.mpapp@1.2.3-beta')

        // Invalid --module-version fails cleanly
        assert failConfigured(dir('bad-ver'), { it.moduleVersion = 'not-a-version' }, [
            'module-info.groovy': 'module demo.badver {}'
        ]).contains('bad value for --module-version')

        // --upgrade-module-path overrides a module of the same name on --module-path
        def upgradeDir = dir('mods-upgrade')
        compile(new File(upgradeDir, 'demo.dep'), [
            'module-info.groovy': 'module demo.dep { exports demo.dep.v2; }',
            'DepV2.groovy'      : 'package demo.dep.v2; class DepV2 {}'
        ])
        compileConfigured(dir('uses-upgraded'), {
            it.upgradeModulePath = upgradeDir.absolutePath
            it.modulePath = mods.absolutePath
        }, [
            'module-info.groovy': 'module demo.upclient { requires demo.dep; }',
            'Client.groovy'     : 'package demo.upclient; class Client { demo.dep.v2.DepV2 upgraded }'
        ])

        // JMOD file on --module-path (JEP 261 compile-time JMOD support)
        def jmodDir = dir('mods-jmod')
        def jmodFile = new File(jmodDir, 'demo.dep.jmod')
        exec(jmodBin(), 'create', '--class-path', new File(mods, 'demo.dep').absolutePath, jmodFile.absolutePath)
        compileConfigured(dir('uses-jmod'), { it.modulePath = jmodDir.absolutePath }, [
            'module-info.groovy': 'module demo.jmodclient { requires demo.dep; }',
            'Client.groovy'     : 'package demo.jmodclient; class Client { demo.dep.Dep fromJmod }'
        ])

        // Joint compilation sees exploded modules on --module-path and ignores JMOD jar: URLs safely
        def extraJmodSrc = dir('extra-jmod-cls')
        compile(extraJmodSrc, [
            'module-info.groovy': 'module demo.jmodextra { exports demo.jmodextra; }',
            'Extra.groovy'      : 'package demo.jmodextra; class Extra {}'
        ])
        def extraJmodFile = new File(dir('extra-jmod-dir'), 'demo.jmodextra.jmod')
        exec(jmodBin(), 'create', '--class-path', extraJmodSrc.absolutePath, extraJmodFile.absolutePath)
        def jointSrc = dir('joint-mp-src')
        def jointOut = dir('joint-mp-out')
        def stubs = dir('joint-mp-stubs')
        new File(jointSrc, 'module-info.groovy').text = 'module demo.jmp { requires demo.dep; exports demo.jmp; }'
        new File(jointSrc, 'JHelper.java').text = 'package demo.jmp; public class JHelper { public demo.dep.Dep dep; }'
        new File(jointSrc, 'GMain.groovy').text = 'package demo.jmp; class GMain { JHelper h }'
        def jointConfig = new CompilerConfiguration(targetDirectory: jointOut)
        jointConfig.modulePathList = [mods.absolutePath, extraJmodFile.absolutePath]
        jointConfig.jointCompilationOptions = [stubDir: stubs]
        def jointLoader = new GroovyClassLoader(GroovyAssert.class.classLoader, jointConfig)
        try {
            def jointUnit = new JavaAwareCompilationUnit(jointConfig, jointLoader)
            jointUnit.addSources([
                new File(jointSrc, 'module-info.groovy'),
                new File(jointSrc, 'JHelper.java'),
                new File(jointSrc, 'GMain.groovy')
            ] as File[])
            jointUnit.compile()
            assert descriptor(jointUnit).exports()*.source() == ['demo.jmp']
        } finally {
            jointLoader.close()
        }
    }

    // GROOVY-9273: JEP 261 + JEP 396 + JEP 403 --limit-modules, --add-modules, --add-reads, --add-exports
    @Test
    void testJep261LimitModulesAddModulesAddReadsAndAddExports() {
        // --limit-modules java.base hides java.sql
        assert failConfigured(dir('limit-hide'), { it.limitModules = ['java.base'] }, [
            'module-info.groovy': 'module demo.lim { requires java.sql; }'
        ]).contains('java.sql')

        // --limit-modules java.base + --add-modules java.sql restores java.sql
        compileConfigured(dir('limit-add'), {
            it.limitModules = ['java.base']
            it.addModules = ['java.sql']
        }, [
            'module-info.groovy': 'module demo.limok { requires java.sql; uses java.sql.Driver; }'
        ])

        // --limit-modules java.base + --add-modules ALL-MODULE-PATH exposes --module-path modules
        def mods = dir('mods-all-mp')
        compile(new File(mods, 'demo.extra'), [
            'module-info.groovy': 'module demo.extra { exports demo.extra; }',
            'E.groovy'          : 'package demo.extra; class E {}'
        ])
        compileConfigured(dir('limit-all-mp'), {
            it.modulePath = mods.absolutePath
            it.limitModules = ['java.base']
            it.addModules = ['ALL-MODULE-PATH']
        }, [
            'module-info.groovy': 'module demo.allmp { requires demo.extra; }',
            'U.groovy'          : 'package demo.allmp; class U { demo.extra.E e }'
        ])

        // --add-reads grants compile-time readability (including ALL-UNNAMED for module-info uses)
        def unnamedCp = dir('unnamed-cp-svc')
        compile(unnamedCp, ['LegacySvc.groovy': 'package legacy.pkg; interface LegacySvc {}'])
        assert failConfigured(dir('unnamed-uses-denied'), {
            it.classpathList = [unnamedCp.absolutePath]
        }, [
            'module-info.groovy': 'module demo.ar { uses legacy.pkg.LegacySvc; }'
        ]).contains('not accessible')
        def addReadsUnit = compileConfigured(dir('add-reads-ok'), {
            it.classpathList = [unnamedCp.absolutePath]
            it.addReads = ['demo.ar=java.sql,ALL-UNNAMED', 'other.mod=java.logging']
        }, [
            'module-info.groovy': 'module demo.ar { uses legacy.pkg.LegacySvc; }',
            'U.groovy'          : 'package demo.ar; class U { java.sql.Driver d }'
        ])
        assert addReadsUnit.ast.moduleInfo.readsUnnamed

        // --add-exports breaks JEP 396/403 strong encapsulation on java.base (implicitly read)
        assert fail(dir('internal-blocked'), [
            'module-info.groovy': 'module demo.ae {}',
            'U.groovy'          : 'package demo.ae; class U { Object x = sun.security.util.SecurityConstants.ALL_PERMISSION }'
        ]).contains('not accessible')
        compileConfigured(dir('add-exports-ok'), {
            it.addExports = ['java.base/sun.security.util=demo.ae']
        }, [
            'module-info.groovy': 'module demo.ae {}',
            'U.groovy'          : 'package demo.ae; class U { Object x = sun.security.util.SecurityConstants.ALL_PERMISSION }'
        ])

        // --add-exports does NOT imply readability: java.sql is not read, so access is still denied
        assert failConfigured(dir('add-exports-no-read'), {
            it.addExports = ['java.sql/java.sql=demo.aenr']
        }, [
            'module-info.groovy': 'module demo.aenr {}',
            'U.groovy'          : 'package demo.aenr; class U { java.sql.Driver d }'
        ]).contains('not accessible')

        // Malformed --add-reads / --add-exports fail; unknown modules emit warnings
        assert failConfigured(dir('bad-ar'), { it.addReads = ['broken'] }, [
            'module-info.groovy': 'module demo.badar {}'
        ]).contains('invalid --add-reads option')
        assert failConfigured(dir('bad-ae'), { it.addExports = ['broken'] }, [
            'module-info.groovy': 'module demo.badae {}'
        ]).contains('invalid --add-exports option')

        def warnUnit = compileConfigured(dir('warn-unknown-opts'), {
            it.addReads = ['demo.wo=no.such.read']
            it.addExports = ['no.such.src/p=demo.wo']
        }, [
            'module-info.groovy': 'module demo.wo {}'
        ])
        def warnMessages = (warnUnit.errorCollector.warnings ?: [])*.message
        assert warnMessages.any { it.contains('module name in --add-reads option not found: no.such.read') }
        assert warnMessages.any { it.contains('module name in --add-exports option not found: no.such.src') }
    }

    // GROOVY-9273: JEP 261 compile-time module warnings (requires-transitive-automatic, requires-automatic, qualified to unknown)
    @Test
    void testJep261ModuleWarnings() {
        def autoClasses = dir('warn-auto-classes')
        compile(autoClasses, ['W.groovy': 'package w; class W {}'])
        def autoJar = new File(tmp, 'warn-auto.jar')
        packJar(autoJar, 'warn.auto', autoClasses)

        def defaultWarnUnit = compile(dir('warn-default'), autoJar, [
            'module-info.groovy': '''
                module demo.wd {
                    requires transitive warn.auto;
                    exports demo.wd to ghost.mod, demo.wd;
                    opens demo.wd to ghost.open;
                }
            ''',
            'D.groovy'          : 'package demo.wd; class D {}'
        ])
        def msgs = (defaultWarnUnit.errorCollector.warnings ?: [])*.message
        assert msgs.any { it.contains('requires transitive directive for an automatic module: warn.auto') }
        assert msgs.any { it.contains('module not found: ghost.mod') }
        assert msgs.any { it.contains('module not found: ghost.open') }
        assert !msgs.any { it.contains('module not found: demo.wd') }

        // Non-transitive requires automatic: silent at default level (1), emitted at level 2 (POSSIBLE_ERRORS)
        def quietUnit = compile(dir('warn-auto-quiet'), autoJar, [
            'module-info.groovy': 'module demo.wq { requires warn.auto; }'
        ])
        assert !(quietUnit.errorCollector.warnings ?: []).any { it.message.contains('automatic module') }

        def verboseUnit = compileConfigured(dir('warn-auto-verbose'), {
            it.classpathList = [autoJar.absolutePath]
            it.warningLevel = WarningMessage.POSSIBLE_ERRORS
        }, [
            'module-info.groovy': 'module demo.wv { requires warn.auto; }'
        ])
        assert (verboseUnit.errorCollector.warnings ?: []).any {
            it.message.contains('requires directive for an automatic module: warn.auto')
        }
    }

    // GROOVY-9273: JEP 511 / JLS §7.5.5 import module inside named modules (readability, qualified exports, self-import, --add-exports)
    @Test
    void testImportModuleInNamedModuleQualifiedSelfAndUnread() {
        // Unread import module in a source class is rejected
        assert fail(dir('imp-unread-cls'), [
            'module-info.groovy': 'module demo.iuc {}',
            'App.groovy'        : '''
                package demo.iuc
                import module java.sql
                class App {}
            '''
        ]).contains('module java.sql is not readable in module demo.iuc')

        // Unknown import module in a named module (both module-info and class) is rejected
        assert fail(dir('imp-unknown-mi'), [
            'module-info.groovy': '''
                import module no.such.mod
                module demo.iumi {}
            '''
        ]).contains('Unknown module: no.such.mod')
        assert fail(dir('imp-unknown-cls'), [
            'module-info.groovy': 'module demo.iuc2 {}',
            'App.groovy'        : '''
                package demo.iuc2
                import module no.such.mod
                class App {}
            '''
        ]).contains('Unknown module: no.such.mod')

        // Qualified export via import module (JEP 511 worked example: exports secret.pkg to demo.friend)
        def secretClasses = dir('imp-secret-classes')
        compile(secretClasses, ['SecretSvc.groovy': 'package secret.pkg; interface SecretSvc {}'])
        def secretJar = new File(tmp, 'imp-secret.jar')
        moduleJar(secretJar, 'secret.lib', 'secret.pkg', 'demo.friend', secretClasses)
        def qUnit = compileConfigured(dir('imp-qualified-ok'), { it.modulePath = secretJar.absolutePath }, [
            'module-info.groovy': '''
                import module secret.lib
                module demo.friend {
                    requires secret.lib;
                    uses SecretSvc;
                }
            ''',
            'App.groovy'        : '''
                package demo.friend
                import module secret.lib
                class App { SecretSvc svc }
            '''
        ])
        assert descriptor(qUnit).uses() == ['secret.pkg.SecretSvc'] as Set

        // Self-module import (JEP 511): imports packages exported by self, NOT concealed packages
        def selfUnit = compile(dir('imp-self-ok'), [
            'module-info.groovy': '''
                import module demo.self
                module demo.self {
                    exports demo.self.api;
                    uses SelfSvc;
                }
            ''',
            'SelfSvc.groovy'    : 'package demo.self.api; interface SelfSvc {}',
            'Hidden.groovy'     : 'package demo.self.internal; class Hidden {}',
            'Use.groovy'        : '''
                package demo.self.use
                import module demo.self
                class Use { SelfSvc svc }
            '''
        ])
        assert descriptor(selfUnit).uses() == ['demo.self.api.SelfSvc'] as Set

        assert fail(dir('imp-self-concealed'), [
            'module-info.groovy': 'module demo.selfc { exports demo.selfc.api; }',
            'Api.groovy'        : 'package demo.selfc.api; class Api {}',
            'Hidden.groovy'     : 'package demo.selfc.internal; class Hidden {}',
            'Use.groovy'        : '''
                package demo.selfc.use
                import module demo.selfc
                class Use { Hidden h }
            '''
        ]).contains('unable to resolve class Hidden')

        // import module java.base picks up --add-exports packages on java.base
        compileConfigured(dir('imp-add-exports'), {
            it.addExports = ['java.base/sun.security.util=demo.iae']
        }, [
            'module-info.groovy': 'module demo.iae {}',
            'Use.groovy'        : '''
                package demo.iae
                import module java.base
                class Use { Object perm = SecurityConstants.ALL_PERMISSION }
            '''
        ])
    }

    // GROOVY-9273: JLS §7.7 multi-line/commented dotted names & JLS §8.5.1/8.9/8.10 implicitly static nested providers
    @Test
    void testMultilineDottedNamesAndImplicitlyStaticProviders() {
        def unit = compile(dir('multiline-and-nested'), [
            'module-info.groovy': '''
                module com
                    .example
                    .multi {
                    requires java /* comment */ .sql;
                    exports com
                        .example
                        .multi to demo
                        .friend;
                    uses java
                        .sql
                        .Driver;
                    provides com
                        .example
                        .multi
                        .Svc with com.example.multi.Svc.NestedIface,
                                  com.example.multi.Svc.NestedEnum,
                                  com.example.multi.Svc.NestedRec;
                }
            ''',
            'Svc.groovy'        : '''
                package com.example.multi
                interface Svc {
                    interface NestedIface {
                        static Svc provider() { new NestedRec() }
                    }
                    enum NestedEnum {
                        A
                        static Svc provider() { new NestedRec() }
                    }
                    record NestedRec() implements Svc {}
                }
            '''
        ])
        def md = descriptor(unit)
        assert md.name() == 'com.example.multi'
        assert md.uses() == ['java.sql.Driver'] as Set
        def provided = md.provides().find { it.service() == 'com.example.multi.Svc' }
        assert provided.providers() == [
            'com.example.multi.Svc$NestedIface',
            'com.example.multi.Svc$NestedEnum',
            'com.example.multi.Svc$NestedRec'
        ]
    }

    // GROOVY-9273: parser duplicate modifiers, annotation number overflow, untargeted annotations, and ResolveVisitor edge cases
    @Test
    void testJpmsSyntaxAndResolveEdgeCases() {
        // Duplicate static / transitive modifiers on requires
        assert fail(dir('dup-static'), [
            'module-info.groovy': 'module demo.ds { requires static static java.sql; }'
        ]).contains("modifier 'static' appears more than once")
        assert fail(dir('dup-trans'), [
            'module-info.groovy': 'module demo.dt { requires transitive transitive java.sql; }'
        ]).contains("modifier 'transitive' appears more than once")

        // Numeric literal overflow inside a module annotation triggers deferred numberFormatError
        assert fail(dir('num-overflow'), [
            'module-info.groovy': '@demo.num.Num(2147483648i)\nmodule demo.num {}',
            'Num.groovy'        : 'package demo.num; @interface Num { int value() }'
        ]).contains('2147483648i')

        // Annotation without @Target (precompiled class) is permitted on module declarations (JLS §9.6.4.1)
        def noTargetDir = dir('no-target-lib')
        compile(noTargetDir, [
            'module-info.groovy': 'module demo.ntlib { exports demo.ntlib; }',
            'NoTarget.groovy'   : 'package demo.ntlib; @interface NoTarget {}'
        ])
        compileConfigured(dir('no-target-use'), { it.modulePath = noTargetDir.absolutePath }, [
            'module-info.groovy': '@demo.ntlib.NoTarget\nmodule demo.ntuse { requires demo.ntlib; }'
        ])

        // Star import already covering all packages exported by import module reuses existing star import
        def onePkgDir = dir('one-pkg-mod')
        compile(onePkgDir, [
            'module-info.groovy': 'module demo.onepkg { exports demo.onepkg; }',
            'Item.groovy'       : 'package demo.onepkg; class Item {}'
        ])
        compileConfigured(dir('star-covers-mod'), { it.modulePath = onePkgDir.absolutePath }, [
            'Use.groovy': '''
                package demo.staruse
                import demo.onepkg.*
                import module demo.onepkg
                class Use { Item item }
            '''
        ])

        // import module in a class does not cascade when module-info graph resolution failed
        assert fail(dir('broken-graph-imp'), [
            'module-info.groovy': 'module demo.bg { requires no.such.dep; }',
            'Use.groovy'        : '''
                package demo.bg
                import module java.sql
                class Use {}
            '''
        ]).contains('no.such.dep')

        // Non-modular script import module when --module-path has a broken/duplicate directory reports Unknown module
        def dupDir = dir('dup-mp-dir')
        def aClasses = dir('dup-a-cls')
        compile(aClasses, ['A.groovy': 'package dup.a; class A {}'])
        moduleJar(new File(dupDir, 'a1.jar'), 'dup.x', 'dup.a', null, aClasses)
        moduleJar(new File(dupDir, 'a2.jar'), 'dup.x', 'dup.a', null, aClasses)
        assert failConfigured(dir('dup-mp-nonmod'), { it.modulePath = dupDir.absolutePath }, [
            'Script.groovy': '''
                import module dup.x
                class Script {}
            '''
        ]).contains('Unknown module: dup.x')

        // Inner class resolved via resolveToInner inside a named module checks module accessibility
        compile(dir('resolve-inner'), [
            'module-info.groovy': 'module demo.inner { exports demo.inner; }',
            'Base.groovy'       : '''
                package demo.inner
                class Base { static class Nested {} }
                class Sub extends Base { Nested n }
            '''
        ])
    }

    // GROOVY-9273: JEP 511 transitive --add-exports, implicit java.lang shadowing, java.se in named module, and ImportCustomizer in named module
    @Test
    void testImportModuleJep511TransitiveAddExportsJavaLangAndCustomizer() {
        // 1. --add-exports applies across requires transitive closure for import module (JLS §7.5.5)
        def lowDir = dir('jep511-low')
        compile(lowDir, [
            'module-info.groovy': 'module m.low { exports m.low.api; }',
            'LowApi.groovy'     : 'package m.low.api; class LowApi {}',
            'Secret.groovy'     : 'package m.low.internal; class Secret {}'
        ])
        def topDir = dir('jep511-top')
        compileConfigured(topDir, { it.modulePath = lowDir.absolutePath }, [
            'module-info.groovy': 'module m.top { requires transitive m.low; exports m.top.api; }',
            'TopApi.groovy'     : 'package m.top.api; class TopApi {}'
        ])
        def mp = [lowDir.absolutePath, topDir.absolutePath].join(File.pathSeparator)
        compileConfigured(dir('jep511-app'), {
            it.modulePath = mp
            it.addExports = ['m.low/m.low.internal=demo.trans']
        }, [
            'module-info.groovy': 'module demo.trans { requires m.top; }',
            'App.groovy'        : '''
                package demo.trans
                import module m.top
                class App {
                    TopApi a
                    LowApi b
                    Secret c
                }
            '''
        ])

        // 2. Implicit java.lang.* shadows import module (JLS §6.4.1), while explicit star still shadows java.lang.*
        def strModDir = dir('jep511-strmod')
        compile(strModDir, [
            'module-info.groovy': 'module m.str { exports m.str; }',
            'String.groovy'     : 'package m.str; class String { static int tag() { 7 } }'
        ])
        def strUseDir = dir('jep511-struse')
        compileConfigured(strUseDir, { it.modulePath = strModDir.absolutePath }, [
            'module-info.groovy': 'module demo.struse { requires m.str; exports demo.struse; }',
            'Check.groovy'      : '''
                package demo.struse
                import module m.str
                import module m.str
                class Check {
                    static Class<?> which() { String }
                }
            ''',
            'StarOverride.groovy': '''
                package demo.struse
                import m.str.*
                import module m.str
                class StarOverride {
                    static Class<?> which() { String }
                }
            '''
        ])
        new URLClassLoader([strUseDir.toURI().toURL(), strModDir.toURI().toURL()] as URL[], this.class.classLoader).withCloseable { cl ->
            assert cl.loadClass('demo.struse.Check').getMethod('which').invoke(null).name == 'java.lang.String'
            assert cl.loadClass('demo.struse.StarOverride').getMethod('which').invoke(null).name == 'm.str.String'
        }

        // 3. Named module declaring requires java.se can import module java.se without --add-modules
        compile(dir('jep511-javase'), [
            'module-info.groovy': 'module demo.se { requires java.se; }',
            'UseSe.groovy'      : '''
                package demo.se
                import module java.se
                class UseSe {
                    Connection conn
                    Logger log
                }
            '''
        ])

        // 4. ImportCustomizer.addModuleImports respects named-module readability, qualified exports, and tiering
        assert failConfigured(dir('jep511-cust-unread'), {
            it.addCompilationCustomizers(new ImportCustomizer().addModuleImports('java.sql'))
        }, [
            'module-info.groovy': 'module demo.custun {}',
            'App.groovy'        : 'package demo.custun; class App {}'
        ]).contains('module java.sql is not readable in module demo.custun')

        def qClasses = dir('jep511-q-cls')
        compile(qClasses, ['QType.groovy': 'package q.secret; class QType {}'])
        def qJar = new File(tmp, 'jep511-q.jar')
        moduleJar(qJar, 'q.lib', 'q.secret', 'demo.custok', qClasses)
        def srcModDir = dir('jep511-srcmod')
        compile(srcModDir, [
            'module-info.groovy': 'module src.win { exports src.win; }',
            'QType.groovy'      : 'package src.win; class QType {}'
        ])
        def custOut = dir('jep511-cust-ok')
        compileConfigured(custOut, {
            it.modulePath = [qJar.absolutePath, srcModDir.absolutePath].join(File.pathSeparator)
            it.addCompilationCustomizers(new ImportCustomizer().addModuleImports('q.lib'))
        }, [
            'module-info.groovy': 'module demo.custok { requires q.lib; requires src.win; exports demo.custok; }',
            'FromCust.groovy'   : 'package demo.custok; class FromCust { static Class<?> which() { QType } }',
            'FromSrc.groovy'    : '''
                package demo.custok
                import module src.win
                class FromSrc { static Class<?> which() { QType } }
            '''
        ])
        new URLClassLoader([custOut.toURI().toURL(), qJar.toURI().toURL(), srcModDir.toURI().toURL()] as URL[], this.class.classLoader).withCloseable { cl ->
            assert cl.loadClass('demo.custok.FromCust').getMethod('which').invoke(null).name == 'q.secret.QType'
            assert cl.loadClass('demo.custok.FromSrc').getMethod('which').invoke(null).name == 'src.win.QType'
        }
    }

    private CompilationUnit compileConfigured(File target, Closure<?> configure, Map<String, String> sources) {
        def config = new CompilerConfiguration(targetDirectory: target)
        configure.call(config)
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader, config)
        try {
            def unit = new CompilationUnit(config, null, loader)
            sources.each { name, text -> unit.addSource(name, text) }
            unit.compile()
            unit
        } finally {
            loader.close()
        }
    }

    private String failConfigured(File target, Closure<?> configure, Map<String, String> sources) {
        try {
            compileConfigured(target, configure, sources)
            throw new AssertionError('expected compilation to fail for ' + target.name)
        } catch (CompilationFailedException e) {
            return e.message
        }
    }

    private File dir(String name) {
        def file = new File(tmp, name)
        file.mkdirs()
        file
    }

    private CompilationUnit compile(File target, Map<String, String> sources) {
        compile(target, (List<File>) [], sources, null)
    }

    private CompilationUnit compile(File target, File classpathEntry, Map<String, String> sources) {
        compile(target, classpathEntry != null ? [classpathEntry] : [], sources, null)
    }

    private CompilationUnit compile(File target, List<File> classpathEntries, Map<String, String> sources) {
        compile(target, classpathEntries, sources, null)
    }

    private CompilationUnit compile(File target, File classpathEntry, Map<String, String> sources, String targetBytecode) {
        compile(target, classpathEntry != null ? [classpathEntry] : [], sources, targetBytecode)
    }

    private CompilationUnit compile(File target, List<File> classpathEntries, Map<String, String> sources, String targetBytecode) {
        def config = new CompilerConfiguration(targetDirectory: target)
        if (targetBytecode != null) {
            config.targetBytecode = targetBytecode
        }
        def loader = new GroovyClassLoader(GroovyAssert.class.classLoader)
        try {
            if (classpathEntries) {
                config.classpathList = classpathEntries*.absolutePath
                classpathEntries.each { loader.addClasspath(it.absolutePath) }
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
        fail(target, (List<File>) [], sources)
    }

    private String fail(File target, Map<String, String> sources, String targetBytecode) {
        try {
            compile(target, (List<File>) [], sources, targetBytecode)
            throw new AssertionError('expected compilation to fail for ' + target.name)
        } catch (CompilationFailedException e) {
            return e.message
        }
    }

    private String fail(File target, File classpathEntry, Map<String, String> sources) {
        fail(target, classpathEntry != null ? [classpathEntry] : [], sources)
    }

    private String fail(File target, List<File> classpathEntries, Map<String, String> sources) {
        try {
            compile(target, classpathEntries, sources)
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

    private static String jmodBin() {
        new File(System.getProperty('java.home'), 'bin/jmod').absolutePath
    }
}
