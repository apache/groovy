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
package org.apache.groovy.classgen

import groovy.test.GroovyAssert
import org.apache.groovy.ast.ModuleInfoNode
import org.apache.groovy.ast.ModuleInfoNode.Exports
import org.apache.groovy.ast.ModuleInfoNode.Opens
import org.apache.groovy.ast.ModuleInfoNode.Provides
import org.apache.groovy.ast.ModuleInfoNode.Requires
import org.apache.groovy.ast.ModuleInfoNode.Uses
import org.apache.groovy.control.ModuleInfoResolveVisitor
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.decompiled.AsmDecompiler
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.classgen.ExtendedVerifier
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.ClassNodeResolver
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompiledClassIndex
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.ModuleImportHelper
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.ProcessingUnit
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes

import javax.tools.ToolProvider
import java.lang.module.FindException
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReader
import java.lang.module.ModuleReference
import java.net.URLClassLoader

/**
 * Branches that the pipeline does not reach on its own: guards, the module reader
 * required by {@code ModuleReference}, and emitter fallbacks.
 */
final class ModuleInfoVerifierEdgeCasesTest {

    @Test
    void testSourceNameAndMissingDeclaration() {
        assert !ModuleInfoNode.isModuleInfoSource((String) null)
        assert !ModuleInfoNode.isModuleInfoSource('')
        assert !ModuleInfoNode.isModuleInfoSource('Foo.groovy')
        assert ModuleInfoNode.isModuleInfoSource('module-info.groovy')
        assert ModuleInfoNode.isModuleInfoSource('module-info.java')
        assert ModuleInfoNode.isModuleInfoSource('src/module-info.groovy')
        assert ModuleInfoNode.isModuleInfoSource('src\\module-info.java') == (File.separatorChar == '\\')
        assert !ModuleInfoNode.isModuleInfoSource((SourceUnit) null)
        assert !ModuleInfoNode.isModuleInfoSource((File) null)
        assert ModuleInfoNode.isModuleInfoSource(new File('src', 'module-info.groovy'))
        assert ModuleInfoNode.isModuleInfoSource(new File('src', 'module-info.java'))
        assert !ModuleInfoNode.isModuleInfoSource(new File('src', 'Foo.groovy'))
        if (File.separatorChar == '/') {
            assert !ModuleInfoNode.isModuleInfoSource(new File('/tmp', 'x\\module-info.groovy'))
        }

        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def source = new SourceUnit('Foo.groovy', 'class Foo {}', config, loader, new ErrorCollector(config))
        ModuleInfoVerifier.check(null, (ModuleInfoNode) null)
        ModuleInfoVerifier.check(source, (ModuleInfoNode) null)

        def compileUnit = new CompileUnit(loader, null, config)
        def module = new ModuleNode(compileUnit)
        def declaration = new ModuleInfoNode('demo.guard', false, [])
        module.moduleDeclaration = declaration
        compileUnit.addModule(module)
        assert compileUnit.moduleInfo.is(declaration)
        assert declaration.sourceModule.is(module)
    }

    @Test
    void testVerifierWithoutCompileUnit(@TempDir File looseDir) {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def loose = new File(looseDir, 'Loose.class')
        def cw = new ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, 'Loose', null, 'java/lang/Object', null)
        cw.visitEnd()
        loose.bytes = cw.toByteArray()
        loader.addClasspath(looseDir.absolutePath)
        def source = new SourceUnit('module-info.groovy',
            'import Loose; module demo.direct { uses Loose; uses java.lang.Runnable; uses List; uses NotAType; }',
            config, loader, new ErrorCollector(config))
        source.parse()
        source.completePhase()
        source.convert()
        def declaration = source.AST.moduleDeclaration
        assert declaration != null
        assert !source.AST.classes.any { it.name == 'module-info' }
        declaration.addAnnotation(new AnnotationNode(ClassHelper.make('missing.Anno')))
        // Static-star import node carries a null package name; simple-name lookup must skip it.
        source.AST.starImports << new ImportNode(ClassHelper.OBJECT_TYPE)
        source.AST.starImports << new ImportNode('')
        // Package name without a trailing dot, so the normalizer branch runs.
        // A second copy of the same package hits the "already a candidate" side.
        source.AST.starImports << new ImportNode('java.util')
        source.AST.starImports << new ImportNode('java.util')
        assert source.AST.unit == null
        ModuleInfoVerifier.check(source, declaration)
        def messages = (0..<source.errorCollector.errorCount).collect { source.errorCollector.getSyntaxError(it)?.message }.join('\n')
        assert messages.contains('not accessible')
        assert declaration.uses.find { it.serviceName == 'java.lang.Runnable' }.resolvedType.name == 'java.lang.Runnable'
    }

    @Test
    void testSystemClassLoaderWhenSourceHasNone() {
        def config = new CompilerConfiguration()
        def source = new SourceUnit('module-info.groovy',
            'module demo.sys { uses java.lang.Runnable; }',
            config, new GroovyClassLoader(), new ErrorCollector(config))
        source.parse()
        source.completePhase()
        source.convert()
        def loader = ProcessingUnit.getDeclaredField('classLoader')
        loader.accessible = true
        loader.set(source, null)
        assert source.classLoader == null
        assert source.AST.unit == null
        def declaration = source.AST.moduleDeclaration
        ModuleInfoVerifier.check(source, declaration)
        assert declaration.uses[0].resolvedType.name == 'java.lang.Runnable'
        assert source.errorCollector.errorCount == 0
    }

    @Test
    void testCompilingModuleReferenceReader() {
        def descriptor = ModuleDescriptor.newModule('demo.reader').build()
        def reference = new ModuleInfoVerifier.CompilingModuleReference(descriptor)
        def reader = reference.open()
        assert reader.find('module-info.class').isEmpty()
        assert reader.list().count() == 0
        reader.close()
    }

    @Test
    void testEmitterFallbacks() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def compileUnit = new CompileUnit(loader, null, config)
        def module = new ModuleNode(compileUnit)
        def hidden = new ClassNode('demo.hid.H', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def concealed = new ClassNode('demo.other.X', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        module.addClass(hidden)
        module.addClass(concealed)
        compileUnit.addModule(module)
        def declaration = new ModuleInfoNode('demo.emit', false, [
            new Requires('java.sql', true, true),
            new Exports('demo.hid', ['java.logging']),
            new Opens('demo.hid', []),
            new Uses('java.lang.Runnable'),
            new Provides('java.lang.Runnable', ['demo.hid.H'])
        ])
        declaration.addAnnotation(new AnnotationNode(ClassHelper.DEPRECATED_TYPE))
        module.moduleDeclaration = declaration
        compileUnit.moduleInfo = declaration
        def context = new GeneratorContext(compileUnit)

        def writer = new ClassWriter(0)
        // Below the module class-file minimum: the emitter must raise it to V9.
        ModuleInfoClassGenerator.write(writer, null, context, declaration, 52)
        def md = ModuleDescriptor.read(new ByteArrayInputStream(writer.toByteArray()))
        assert !md.open
        assert md.name() == 'demo.emit'
        assert md.requires().find { it.name() == 'java.sql' }.modifiers().containsAll(
            [ModuleDescriptor.Requires.Modifier.TRANSITIVE, ModuleDescriptor.Requires.Modifier.STATIC])
        assert md.exports().find { it.source() == 'demo.hid' }.targets() == ['java.logging'] as Set
        assert md.opens()*.source() == ['demo.hid']
        // Unresolved service names are still written, with dots folded to slashes by the fallback.
        assert md.uses().contains('java.lang.Runnable') || md.uses().contains('java/lang/Runnable')
        assert md.packages().contains('demo.other')
        assert new String(writer.toByteArray(), 'ISO-8859-1').contains('Deprecated')

        def skipping = new ClassVisitor(Opcodes.ASM9) {}
        ModuleInfoClassGenerator.write(skipping, null, context, declaration, Opcodes.V17)

        // A default-package class and provider are skipped (unnamed packages cannot appear in ModulePackages).
        def looseType = new ClassNode('Loose', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        module.addClass(looseType)
        declaration.provides[0].resolvedProviders = [looseType, hidden]
        def unnamed = new ClassWriter(0)
        ModuleInfoClassGenerator.write(unnamed, null, context, declaration, Opcodes.V17)

        // Module java.base has no implicit requires edge.
        def baseDecl = new ModuleInfoNode('java.base', false, [])
        def baseWriter = new ClassWriter(0)
        ModuleInfoClassGenerator.write(baseWriter, null, new GeneratorContext(new CompileUnit(loader, null, config)), baseDecl, Opcodes.V17)
        def base = ModuleDescriptor.read(new ByteArrayInputStream(baseWriter.toByteArray()))
        assert base.name() == 'java.base'
        assert base.requires().empty
    }

    @Test
    void testNullModuleAndUnparsedSource() {
        def parsed = parsed('module demo.nullmod { uses Runnable; }')
        def declaration = parsed.AST.moduleDeclaration
        declaration.sourceModule = null
        def holding = new CompilationUnit(parsed.configuration, null, new GroovyClassLoader())
        holding.AST.moduleInfo = declaration
        ModuleInfoVerifier.check(parsed, declaration, holding)
        assert messages(parsed).contains('unable to resolve class Runnable')

        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def unparsed = new SourceUnit('module-info.groovy', 'module demo.x {}', config, loader, new ErrorCollector(config))
        def module = new ModuleNode(unparsed)
        def info = new ModuleInfoNode('demo.x', false, [
            new ModuleInfoNode.Requires('java.base', false, false)
        ])
        module.moduleDeclaration = info
        assert info.sourceModule.is(module)
        def hosted = ClassHelper.makeWithoutCaching('not.yet.Resolved')
        hosted.putNodeMetaData(ModuleNode, new ModuleNode(new CompileUnit(loader, null, config)))
        info.addAnnotation(new AnnotationNode(hosted))
        assert unparsed.AST == null
        ModuleInfoVerifier.check(unparsed, info)
        assert messages(unparsed).contains('annotation @not.yet.Resolved is not accessible')
    }

    @Test
    void testResolvedModuleNoLongerOpenable() {
        def emptyFinder = new ModuleFinder() {
            @Override
            Optional<ModuleReference> find(String name) {
                Optional.empty()
            }

            @Override
            Set<ModuleReference> findAll() {
                Set.of()
            }
        }
        def parsed = parsed('module demo.gap { uses java.lang.Runnable; }')
        ModuleInfoVerifier.check(parsed, parsed.AST.moduleDeclaration, null, emptyFinder)
        assert messages(parsed).contains('not accessible')
    }

    @Test
    void testResolutionFailureWithoutMessage() {
        assertResolutionFailure(' ', 'demo.blank', 'missing.blank')
        assertResolutionFailure(null, 'demo.nilmsg', 'missing.nil')
    }

    @Test
    void testReadModuleVersion() {
        assert ModuleInfoVerifier.readModuleVersion(ModuleFinder.ofSystem(), 'java.base')
        assert ModuleInfoVerifier.readModuleVersion(ModuleFinder.ofSystem(), 'no.such.module') == null
        def descriptor = ModuleDescriptor.newModule('demo.nover').build()
        def reference = new ModuleInfoVerifier.CompilingModuleReference(descriptor)
        def finder = new ModuleFinder() {
            @Override
            Optional<ModuleReference> find(String name) {
                'demo.nover' == name ? Optional.of(reference) : Optional.empty()
            }

            @Override
            Set<ModuleReference> findAll() {
                Set.of(reference)
            }
        }
        assert ModuleInfoVerifier.readModuleVersion(finder, 'demo.nover') == null
        def failing = new ModuleFinder() {
            @Override
            Optional<ModuleReference> find(String name) {
                throw new FindException('boom')
            }

            @Override
            Set<ModuleReference> findAll() {
                Set.of()
            }
        }
        assert ModuleInfoVerifier.readModuleVersion(failing, 'java.base') == null
    }

    // GROOVY-9273
    @Test
    void testRepeatedDeclarationIsRejected() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def compileUnit = new CompileUnit(loader, null, config)
        def module = new ModuleNode(compileUnit)
        def declaration = new ModuleInfoNode('demo.one', false, [])
        module.moduleDeclaration = declaration
        assert compileUnit.moduleInfo.is(declaration)
        assert declaration.sourceModule.is(module)
        assert !module.classes.any { it.name == 'module-info' }

        def second = new ModuleInfoNode('demo.second', false, [])
        module.moduleDeclaration = second
        assert compileUnit.moduleInfo.is(declaration)
        assert module.moduleDeclaration.is(declaration)
        assert second.sourceModule == null
        assert compileUnit.consumeModuleInfoConflict().contains('only one module declaration')
        module.moduleDeclaration = second
        assert compileUnit.moduleInfo.is(declaration)
        assert module.moduleDeclaration.is(declaration)
        assert compileUnit.consumeModuleInfoConflict() != null
    }

    @Test
    void testCompiledClassIndexEdgeCases() {
        def index = new CompiledClassIndex()
        assert index.empty
        assert index.packages.empty
        assert index.typePackages.empty
        assert !index.hasUnnamedClass()

        index.add(null)
        index.add('')
        index.add(ModuleInfoNode.CLASS_FILE_NAME)
        assert index.empty
        assert !index.hasUnnamedClass()

        index.add('UnnamedType')
        assert !index.empty
        assert index.hasUnnamedClass()
        assert index.packages.empty
        assert index.typePackages.empty

        index.add('demo.onlyinfo.package-info')
        assert index.packages == ['demo.onlyinfo'] as Set
        assert index.typePackages.empty

        index.add('demo.real.Widget')
        assert index.packages == ['demo.onlyinfo', 'demo.real'] as Set
        assert index.typePackages == ['demo.real'] as Set

        index.clear()
        assert index.empty
        assert index.packages.empty
        assert index.typePackages.empty
        assert !index.hasUnnamedClass()
    }

    @Test
    void testModuleInfoAnnotationsAndExtendedVerifierGuards() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def cu = new CompilationUnit(config, null, loader)
        def source = new SourceUnit('module-info.groovy', 'module demo.ann {}', config, loader, new ErrorCollector(config))
        def declaration = new ModuleInfoNode('demo.ann', false, [])

        // ModuleInfoAnnotations guard branches
        ModuleInfoAnnotations.expand(null, declaration, cu)
        ModuleInfoAnnotations.expand(source, null, cu)
        ModuleInfoAnnotations.expand(source, declaration, cu)

        // Plain non-collector annotation with null sourceModule
        def dep = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)
        declaration.addAnnotation(dep)
        ModuleInfoAnnotations.expand(source, declaration, cu)
        assert declaration.annotations.size() == 1
        assert declaration.expandedCollectors.empty

        // ExtendedVerifier.visitModuleAnnotations guard branches
        def verifier = new ExtendedVerifier(source)
        verifier.visitModuleAnnotations(null)

        def bare = new ModuleInfoNode('demo.bare', false, [])
        verifier.visitModuleAnnotations(bare)

        def module = new ModuleNode(source)
        module.moduleDeclaration = bare
        verifier.visitModuleAnnotations(bare)
        // Second visit on the same module hits visitedModules.add(module) == false with empty annotations
        verifier.visitModuleAnnotations(bare)
        // With module annotations but already-visited module, still visits module annotations
        bare.addAnnotation(new AnnotationNode(ClassHelper.DEPRECATED_TYPE))
        verifier.visitModuleAnnotations(bare)
        assert source.errorCollector.errorCount == 0
    }

    @Test
    void testModuleInfoClassGeneratorEdgeCases() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()

        // generate() with null context and with null config falls back to V9
        def bareDecl = new ModuleInfoNode('demo.gen', false, [])
        byte[] noCtxBytes = ModuleInfoClassGenerator.generate(null, null, bareDecl)
        assert readMajorMinorVersion(noCtxBytes) == Opcodes.V9

        def noConfigUnit = new CompileUnit(loader, null, null)
        byte[] noCfgBytes = ModuleInfoClassGenerator.generate(null, new GeneratorContext(noConfigUnit), bareDecl)
        assert readMajorMinorVersion(noCfgBytes) == Opcodes.V9

        // write() drops V_PREVIEW when clamping bytecode version below V9 or below V12 (JVMS §4.1)
        def previewWriter = new ClassWriter(0)
        ModuleInfoClassGenerator.write(previewWriter, null, null, bareDecl, Opcodes.V1_8 | Opcodes.V_PREVIEW)
        assert readMajorMinorVersion(previewWriter.toByteArray()) == Opcodes.V9

        def v11PreviewWriter = new ClassWriter(0)
        ModuleInfoClassGenerator.write(v11PreviewWriter, null, null, bareDecl, Opcodes.V11 | Opcodes.V_PREVIEW)
        assert readMajorMinorVersion(v11PreviewWriter.toByteArray()) == Opcodes.V11

        // write() preserves V_PREVIEW when >= V12 (e.g. V21 | V_PREVIEW)
        def v21PreviewWriter = new ClassWriter(0)
        ModuleInfoClassGenerator.write(v21PreviewWriter, null, null, bareDecl, Opcodes.V21 | Opcodes.V_PREVIEW)
        assert readMajorMinorVersion(v21PreviewWriter.toByteArray()) == (Opcodes.V21 | Opcodes.V_PREVIEW)

        // open module ignores opens directives; null context falls back to sourceModule.unit;
        // null entry in resolvedProviders is skipped; SOURCE-retention annotation is omitted;
        // explicit requires java.base suppresses implicit ACC_MANDATED edge.
        def compileUnit = new CompileUnit(loader, null, config)
        def module = new ModuleNode(compileUnit)
        def impl = new ClassNode('demo.open.Impl', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        module.addClass(impl)
        compileUnit.addModule(module)

        def provides = new Provides('java.lang.Runnable', ['demo.open.Impl'])
        provides.resolvedProviders = [null, impl]
        def openDecl = new ModuleInfoNode('demo.open', true, [
            new Requires('java.base', false, false),
            new Opens('demo.open', []),
            provides
        ])
        def sourceRetained = new AnnotationNode(ClassHelper.make(SuppressWarnings))
        def runtimeRetained = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)
        openDecl.addAnnotation(sourceRetained)
        openDecl.addAnnotation(runtimeRetained)
        module.moduleDeclaration = openDecl
        openDecl.adoptResolution(['java.base'] as Set, ModuleFinder.ofSystem(), false)

        def cw = new ClassWriter(0)
        ModuleInfoClassGenerator.write(cw, null, null, openDecl, Opcodes.V17)
        def md = ModuleDescriptor.read(new ByteArrayInputStream(cw.toByteArray()))
        assert md.open
        assert md.opens().empty
        assert md.packages().contains('demo.open')
        assert md.requires().size() == 1
        assert md.requires().first().modifiers().empty

        // ClassVisitor.visitAnnotation returning null skips attribute emission cleanly
        def nullAnnoVisitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            ModuleVisitor visitModule(String name, int access, String version) {
                new ModuleVisitor(Opcodes.ASM9) {}
            }

            @Override
            AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                null
            }
        }
        ModuleInfoClassGenerator.write(nullAnnoVisitor, null, null, openDecl, Opcodes.V17)
    }

    @Test
    void testModuleInfoVerifierAndResolveVisitorGuards() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def source = parsed('module demo.guards { uses java.lang.Runnable; }')
        def declaration = source.AST.moduleDeclaration

        // prepareReads and ModuleInfoResolveVisitor.resolve null & idempotence guards
        ModuleInfoVerifier.prepareReads(null, declaration)
        ModuleInfoVerifier.prepareReads(source, null)
        ModuleInfoResolveVisitor.resolve(null, declaration, null)
        ModuleInfoResolveVisitor.resolve(source, null, null)
        ModuleInfoResolveVisitor.resolve(source, declaration, null)
        assert declaration.hasResolvedTypes()
        // Already-resolved declaration is a no-op
        ModuleInfoResolveVisitor.resolve(source, declaration, null)

        // Static requires check & cycle check handle FindException (both non-blank and blank messages)
        def staticFail = { String msg ->
            def s = parsed('module demo.staticmiss { requires static missing.dep; }')
            int calls = 0
            def finder = new ModuleFinder() {
                @Override
                Optional<ModuleReference> find(String name) {
                    if ('missing.dep' == name) {
                        if (++calls == 1) {
                            return Optional.empty()
                        }
                        throw new FindException(msg)
                    }
                    Optional.empty()
                }

                @Override
                Set<ModuleReference> findAll() {
                    Set.of()
                }
            }
            ModuleInfoVerifier.check(s, s.AST.moduleDeclaration, finder, null)
            messages(s)
        }
        assert staticFail('cannot read').contains('cannot read')
        assert staticFail('').contains('module not found: missing.dep')
        assert staticFail(null).contains('module not found: missing.dep')

        // Annotation value with primitive, array, unresolved, and null-typed ClassExpression
        def annSource = parsed('module demo.annval {}')
        def annDecl = annSource.AST.moduleDeclaration
        annDecl.markTypesResolved()
        def anno = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)
        anno.setMember('prim', new ClassExpression(ClassHelper.int_TYPE))
        anno.setMember('arr', new ClassExpression(ClassHelper.STRING_TYPE.makeArray()))
        anno.setMember('unresolved', new ClassExpression(ClassHelper.make('demo.never.Resolved')))
        anno.setMember('nilType', new ClassExpression(null))
        annDecl.addAnnotation(anno)
        // Null-typed import node is safely skipped by checkImportedType
        annSource.AST.imports << new ImportNode('java.lang.')
        ModuleInfoVerifier.check(annSource, annDecl)
        assert annSource.errorCollector.errorCount == 0

        // CompileUnit.setModuleInfo null, idempotent re-set, and conflict reporting on existing source
        def cu = new CompileUnit(loader, null, config)
        cu.moduleInfo = null
        assert cu.moduleInfo == null

        def src1 = new SourceUnit('module-info.groovy', 'module demo.dup1 {}', config, loader, new ErrorCollector(config))
        def mod1 = new ModuleNode(src1)
        mod1.unit = cu
        def d1 = new ModuleInfoNode('demo.dup1', false, [])
        mod1.moduleDeclaration = d1
        // Setting identical declaration on ModuleNode and CompileUnit is idempotent
        mod1.moduleDeclaration = d1
        cu.moduleInfo = d1
        assert cu.consumeModuleInfoConflict() == null

        def d2 = new ModuleInfoNode('demo.dup2', false, [])
        cu.moduleInfo = d2
        assert cu.consumeModuleInfoConflict() == null
        assert messages(src1).contains('only one module declaration')
    }

    @Test
    void testVerifierAndPipelineRemainingBranches(@TempDir File tmp) {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()

        // 1. Split package from CompiledClassIndex only (not in compileUnit.classes) hits classInPackage fallback
        def splitUnit = new CompilationUnit(config, null, loader)
        splitUnit.compiledClasses.add('java.lang.FakeSplit')
        def splitSource = parsed('module demo.jsplit {}')
        splitSource.AST.unit = splitUnit.AST
        splitUnit.AST.addModule(splitSource.AST)
        def extraMod = new ModuleNode(splitUnit.AST)
        extraMod.addClass(new ClassNode('demo.other.Unrelated', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE))
        splitUnit.AST.addModule(extraMod)
        ModuleInfoVerifier.check(splitSource, splitSource.AST.moduleDeclaration, splitUnit)
        assert messages(splitSource).contains('package java.lang exists in another module: java.base')

        // 2. Unresolved service & unresolved provider in provides directive; AST provider with empty constructors;
        //    loaded local, anonymous, and nested-inside-non-static classes
        def provUnit = new CompilationUnit(config, null, loader)
        def provSource = parsed('''
            module demo.provs {
                provides missing.Svc with missing.Impl;
                provides java.lang.Runnable with demo.provs.EmptyCtor, missing.Impl2;
            }
        ''')
        def provMod = provSource.AST
        provMod.unit = provUnit.AST
        def emptyCtor = new ClassNode('demo.provs.EmptyCtor', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                [ClassHelper.make(Runnable)] as ClassNode[], null)
        provMod.addClass(emptyCtor)
        provUnit.AST.addModule(provMod)
        ModuleInfoVerifier.check(provSource, provMod.moduleDeclaration, provUnit)
        assert messages(provSource).contains('unable to resolve class missing.Svc')
        assert messages(provSource).contains('unable to resolve class missing.Impl2')
        assert !messages(provSource).contains('EmptyCtor')

        // Loaded anonymous, local, and non-static-enclosed provider classes
        def javaSrc = new File(tmp, 'demo/local/Host.java')
        javaSrc.parentFile.mkdirs()
        javaSrc.text = '''
            package demo.local;
            public class Host {
                public static Class<?> localClass() {
                    class LocalImpl implements Runnable { public void run() {} }
                    return LocalImpl.class;
                }
                public class NonStaticMid {
                    public static class StaticLeaf implements Runnable { public void run() {} }
                }
                public static class StaticOuter {
                    public static class StaticInner implements Runnable { public void run() {} }
                }
            }
        '''
        def outDir = new File(tmp, 'classes')
        outDir.mkdirs()
        assert ToolProvider.systemJavaCompiler.run(null, null, null, '-d', outDir.absolutePath, javaSrc.absolutePath) == 0
        def anon = new Runnable() { @Override void run() {} }
        def urlLoader = new URLClassLoader([outDir.toURI().toURL()] as URL[], this.class.classLoader)
        try {
            def hostClass = urlLoader.loadClass('demo.local.Host')
            def localClass = (Class<?>) hostClass.getMethod('localClass').invoke(null)
            def leafClass = urlLoader.loadClass('demo.local.Host$NonStaticMid$StaticLeaf')
            def loadedProvSource = parsed('module demo.loadedprov {}')
            def badProvides = new Provides('java.lang.Runnable', ['a', 'b', 'c'])
            badProvides.resolvedService = ClassHelper.make(Runnable)
            badProvides.resolvedProviders = [
                ClassHelper.make(anon.class),
                ClassHelper.make(localClass),
                ClassHelper.make(leafClass)
            ]
            def withBadProvides = new ModuleInfoNode('demo.loadedprov', false, [badProvides])
            withBadProvides.markTypesResolved()
            loadedProvSource.AST.moduleDeclaration = withBadProvides
            ModuleInfoVerifier.check(loadedProvSource, withBadProvides)
            assert messages(loadedProvSource).contains('must be a top-level or static nested type')

            // DecompiledClassNode member check without loading classes into the JVM
            def decompLoader = new GroovyClassLoader(urlLoader)
            try {
                def decompUnit = new CompilationUnit(config, null, decompLoader)
                decompUnit.compiledClasses.add('demo.local.Host$StaticOuter$StaticInner')
                decompUnit.compiledClasses.add('demo.local.Host$NonStaticMid$StaticLeaf')
                def resolver = new AsmReferenceResolver(new ClassNodeResolver(), decompUnit)
                def staticDecomp = new DecompiledClassNode(
                        AsmDecompiler.parseClass(new File(outDir, 'demo/local/Host$StaticOuter$StaticInner.class').toURI().toURL()), resolver)
                def nonStaticDecomp = new DecompiledClassNode(
                        AsmDecompiler.parseClass(new File(outDir, 'demo/local/Host$NonStaticMid$StaticLeaf.class').toURI().toURL()), resolver)
                def decompSource = parsed('module demo.decompprov {}')
                def decompProvides = new Provides('java.lang.Runnable', ['ok', 'bad'])
                decompProvides.resolvedService = ClassHelper.make(Runnable)
                decompProvides.resolvedProviders = [staticDecomp, nonStaticDecomp]
                def decompDecl = new ModuleInfoNode('demo.decompprov', false, [decompProvides])
                decompDecl.markTypesResolved()
                decompSource.AST.moduleDeclaration = decompDecl
                ModuleInfoVerifier.check(decompSource, decompDecl, decompUnit)
                def decompMsgs = messages(decompSource)
                assert !decompMsgs.contains('StaticInner')
                assert decompMsgs.contains('StaticLeaf must be a top-level or static nested type')
            } finally {
                decompLoader.close()
            }
        } finally {
            urlLoader.close()
        }

        // 3. CompileUnit fallback scanning modules for a SourceUnit, plus moduleOwning & ModuleNode rollback
        def cu2 = new CompileUnit(loader, null, config)
        def fallbackSrc = new SourceUnit('Other.groovy', 'class Other {}', config, loader, new ErrorCollector(config))
        def fallbackMod = new ModuleNode(fallbackSrc)
        fallbackMod.unit = cu2
        cu2.addModule(fallbackMod)
        def noSrcMod1 = new ModuleNode(cu2)
        def noSrcMod2 = new ModuleNode(cu2)
        cu2.addModule(noSrcMod1)
        cu2.addModule(noSrcMod2)
        def firstDecl = new ModuleInfoNode('demo.first', false, [])
        noSrcMod1.moduleDeclaration = firstDecl
        // Detach sourceModule so moduleOwning scans modules list, and reject second declaration via fallbackMod's SourceUnit
        firstDecl.sourceModule = null
        def secondDecl = new ModuleInfoNode('demo.second', false, [])
        noSrcMod2.moduleDeclaration = secondDecl
        assert cu2.moduleInfo.is(firstDecl)
        assert noSrcMod2.moduleDeclaration == null
        assert messages(fallbackSrc).contains('only one module declaration')
        // Re-setting on noSrcMod1 restores previous declaration when conflict occurs
        def thirdDecl = new ModuleInfoNode('demo.third', false, [])
        noSrcMod1.moduleDeclaration = thirdDecl
        assert noSrcMod1.moduleDeclaration.is(firstDecl)

        // 4. CompilationUnit.IModuleInfoOperation: null source skipped, RuntimeException & GroovyBugError wrapped
        def opCu = new CompilationUnit(config, null, loader)
        def detachedDecl = new ModuleInfoNode('demo.detached', false, [])
        opCu.AST.moduleInfo = detachedDecl
        def opRan = false
        opCu.addModuleInfoPhaseOperation({ SourceUnit s, GeneratorContext g, ModuleInfoNode d ->
            opRan = true
        } as CompilationUnit.IModuleInfoOperation, Phases.SEMANTIC_ANALYSIS)
        opCu.compile(Phases.SEMANTIC_ANALYSIS)
        assert !opRan

        def reCu = new CompilationUnit(config, null, loader)
        reCu.addSource('module-info.groovy', 'module demo.operr {}')
        reCu.addModuleInfoPhaseOperation({ SourceUnit s, GeneratorContext g, ModuleInfoNode d ->
            throw new IllegalStateException('synthetic failure')
        } as CompilationUnit.IModuleInfoOperation, Phases.SEMANTIC_ANALYSIS)
        def reBug = GroovyAssert.shouldFail(GroovyBugError) {
            reCu.compile(Phases.SEMANTIC_ANALYSIS)
        }
        assert reBug.message.contains('module-info.groovy')

        def bugCu = new CompilationUnit(config, null, loader)
        bugCu.addSource('module-info.groovy', 'module demo.opbug {}')
        bugCu.addModuleInfoPhaseOperation({ SourceUnit s, GeneratorContext g, ModuleInfoNode d ->
            throw new GroovyBugError('direct bug')
        } as CompilationUnit.IModuleInfoOperation, Phases.SEMANTIC_ANALYSIS)
        def directBug = GroovyAssert.shouldFail(GroovyBugError) {
            bugCu.compile(Phases.SEMANTIC_ANALYSIS)
        }
        assert directBug.message.contains('module-info.groovy')

        // 5. ResolveVisitor direct null/empty guard coverage
        def rv = new ResolveVisitor((CompilationUnit) null) {
            void probe() {
                checkModuleAccessibility(null, null)
                checkModulePackageAccessibility(null, null)
                checkModulePackageAccessibility('', null)
            }
        }
        rv.probe()
        // Static import & static star import of missing classes report unable to resolve class
        def missingStaticCu = new CompilationUnit(config, null, loader)
        missingStaticCu.addSource('BadStatic.groovy', '''
            import static no.such.Holder.VAL
            import static no.such.StarHolder.*
            class BadStatic {}
        ''')
        def badStaticErr = GroovyAssert.shouldFail(CompilationFailedException) {
            missingStaticCu.compile(Phases.SEMANTIC_ANALYSIS)
        }
        assert badStaticErr.message.contains('unable to resolve class no.such.Holder')
        assert badStaticErr.message.contains('unable to resolve class no.such.StarHolder')
    }

    @Test
    void testResolveModuleImportAndAutomaticFinderEdgeCases() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()

        // Null accessibilityFinder on a ready ModuleInfoNode skips module-import expansion safely
        def cuNullFinder = new CompilationUnit(config, null, loader)
        def infoNull = new ModuleInfoNode('demo.nullf', false, [])
        infoNull.adoptResolution(Set.of('java.base'), null, false)
        cuNullFinder.AST.moduleInfo = infoNull
        def srcNull = new SourceUnit('A.groovy', 'package demo.nullf; import module java.base; class A {}', config, loader, new ErrorCollector(config))
        srcNull.parse(); srcNull.completePhase(); srcNull.convert()
        def rvNull = new ResolveVisitor(cuNullFinder)
        rvNull.startResolving(srcNull.AST.classes[0], srcNull)
        assert srcNull.errorCollector.errorCount == 0

        // Self import when finder does not contain compiling module catches IllegalArgumentException cleanly
        def cuMissingSelf = new CompilationUnit(config, null, loader)
        def infoMissing = new ModuleInfoNode('demo.missing', false, [])
        infoMissing.adoptResolution(Set.of('java.base'), ModuleFinder.ofSystem(), false)
        cuMissingSelf.AST.moduleInfo = infoMissing
        def srcSelf = new SourceUnit('B.groovy', 'package demo.missing; import module demo.missing; class B {}', config, loader, new ErrorCollector(config))
        srcSelf.parse(); srcSelf.completePhase(); srcSelf.convert()
        def rvSelf = new ResolveVisitor(cuMissingSelf)
        rvSelf.startResolving(srcSelf.AST.classes[0], srcSelf)
        assert srcSelf.errorCollector.errorCount == 0

        // addAllObservableAutomaticModules handles FindException from accessibility finder gracefully
        def autoDesc = ModuleDescriptor.newAutomaticModule('auto.one').packages(['auto.one'] as Set).build()
        def autoRef = new ModuleReference(autoDesc, null) {
            @Override ModuleReader open() { throw new UnsupportedOperationException() }
        }
        def okFinder = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { 'auto.one' == name ? Optional.of(autoRef) : Optional.empty() }
            @Override Set<ModuleReference> findAll() { Set.of(autoRef) }
        }
        def failingFindAll = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { 'auto.one' == name ? Optional.of(autoRef) : Optional.empty() }
            @Override Set<ModuleReference> findAll() { throw new FindException('simulated findAll failure') }
        }
        def srcAuto = parsed('module demo.autofail { requires static auto.one; }')
        ModuleInfoVerifier.check(srcAuto, srcAuto.AST.moduleDeclaration, okFinder, failingFindAll)
        assert srcAuto.errorCollector.errorCount == 0
    }

    @Test
    void testModuleImportHelperDeterministicOrderAndResolveEdgeCases() {
        // Automatic module packages are returned in deterministic sorted order
        def autoDesc = ModuleDescriptor.newAutomaticModule('auto.sorted')
                .packages(['z.pkg', 'a.pkg', 'm.pkg'] as Set).build()
        def autoRef = new ModuleReference(autoDesc, null) {
            @Override ModuleReader open() { throw new UnsupportedOperationException() }
        }
        def autoFinder = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { 'auto.sorted' == name ? Optional.of(autoRef) : Optional.empty() }
            @Override Set<ModuleReference> findAll() { Set.of(autoRef) }
        }
        assert ModuleImportHelper.resolveModulePackages('auto.sorted', autoFinder) == ['a.pkg', 'm.pkg', 'z.pkg']

        // Explicit module exports, extraExports, and FindException on transitive requires
        def expDesc = ModuleDescriptor.newModule('exp.sorted')
                .exports('z.exp')
                .exports('a.exp')
                .exports('q.exp', Set.of('reader.mod'))
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), 'broken.dep')
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), 'missing.dep')
                .build()
        def expRef = new ModuleReference(expDesc, null) {
            @Override ModuleReader open() { throw new UnsupportedOperationException() }
        }
        def expFinder = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) {
                if ('exp.sorted' == name) return Optional.of(expRef)
                if ('broken.dep' == name) throw new FindException('simulated transitive lookup failure')
                Optional.empty()
            }
            @Override Set<ModuleReference> findAll() { Set.of(expRef) }
        }
        def extras = ['exp.sorted': ['b.extra', 'a.exp', 'y.extra'] as Set]
        def resolved = ModuleImportHelper.resolveModulePackages('exp.sorted', expFinder, 'reader.mod', extras)
        assert resolved == ['a.exp', 'q.exp', 'z.exp', 'b.extra', 'y.extra']
        assert ModuleImportHelper.resolveModulePackages('exp.sorted', expFinder, 'reader.mod') == ['a.exp', 'q.exp', 'z.exp']
        assert ModuleImportHelper.resolveModulePackages('exp.sorted', expFinder, null, null) == ['a.exp', 'z.exp']
        assert ModuleImportHelper.resolveModulePackages('exp.sorted', expFinder, null, Map.of()) == ['a.exp', 'z.exp']

        def direct4 = []
        ModuleImportHelper.collectModuleExports(expRef, expFinder, direct4, new LinkedHashSet<>())
        assert direct4 == ['a.exp', 'z.exp']
        def direct5 = []
        ModuleImportHelper.collectModuleExports(expRef, expFinder, 'reader.mod', direct5, new LinkedHashSet<>())
        assert direct5 == ['a.exp', 'q.exp', 'z.exp']

        // Direct ResolveVisitor module-tier edge cases: null packageName node, same canonical class twice,
        // same-line star imports (column tie-break), package-private java.lang type, and null source
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def cu = new CompilationUnit(config, null, loader)
        def src = new SourceUnit('ModTier.groovy', 'import java.sql.*; import java.util.*; class ModTier { Date d; Logger log }', config, loader, new ErrorCollector(config))
        src.parse(); src.completePhase(); src.convert()
        def nullPkgImport = new ImportNode(ClassHelper.OBJECT_TYPE, 'Obj')
        nullPkgImport.lineNumber = 1
        src.AST.moduleStarImports.add(nullPkgImport)
        def dup1 = new ImportNode('java.util.logging.')
        dup1.lineNumber = 1
        def dup2 = new ImportNode('java.util.logging.')
        dup2.lineNumber = 1
        src.AST.moduleStarImports.add(dup1)
        src.AST.moduleStarImports.add(dup2)
        new ResolveVisitor(cu).startResolving(src.AST.classes[0], src)
        assert src.errorCollector.errorCount == 0
        assert src.AST.classes[0].getField('d').type.name == 'java.sql.Date'
        assert src.AST.classes[0].getField('log').type.name == 'java.util.logging.Logger'

        // Package-private java.lang type is not bound by implicit java.lang.* when import module is present
        def srcPrivLang = new SourceUnit('PrivLang.groovy', 'import module java.base; class PrivLang { VersionProps v }', config, loader, new ErrorCollector(config))
        srcPrivLang.parse(); srcPrivLang.completePhase(); srcPrivLang.convert()
        new ResolveVisitor(cu).startResolving(srcPrivLang.AST.classes[0], srcPrivLang)
        assert srcPrivLang.errorCollector.errorCount > 0

        // Null SourceUnit in unnamed-module resolution falls back to system finder and skips null-package star
        def srcNullUnit = new SourceUnit('NullSrc.groovy', 'import module java.sql; class NullSrc { Connection c }', config, loader, new ErrorCollector(config))
        srcNullUnit.parse(); srcNullUnit.completePhase(); srcNullUnit.convert()
        srcNullUnit.AST.starImports.add(new ImportNode(ClassHelper.OBJECT_TYPE, 'Obj'))
        new ResolveVisitor(cu).startResolving(srcNullUnit.AST.classes[0], null)
        assert srcNullUnit.AST.classes[0].getField('c').type.name == 'java.sql.Connection'
    }

    private static int readMajorMinorVersion(byte[] bytecode) {
        int version = -1
        new ClassReader(bytecode).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            void visit(int v, int access, String name, String signature, String superName, String[] interfaces) {
                version = v
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)
        version
    }

    private static void assertResolutionFailure(String failureMessage, String moduleName, String missing) {
        def failing = new ModuleFinder() {
            @Override
            Optional<ModuleReference> find(String name) {
                if (missing == name) {
                    throw new FindException(failureMessage)
                }
                Optional.empty()
            }

            @Override
            Set<ModuleReference> findAll() {
                Set.of()
            }
        }
        def parsed = parsed("module ${moduleName} { requires ${missing}; }")
        ModuleInfoVerifier.check(parsed, parsed.AST.moduleDeclaration, failing, null)
        assert messages(parsed).contains('module resolution failed for ' + moduleName)
    }

    private static SourceUnit parsed(String text) {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def source = new SourceUnit('module-info.groovy', text, config, loader, new ErrorCollector(config))
        source.parse()
        source.completePhase()
        source.convert()
        source
    }

    private static String messages(SourceUnit source) {
        (0..<source.errorCollector.errorCount).collect { source.errorCollector.getSyntaxError(it)?.message }.join('\n')
    }
}
