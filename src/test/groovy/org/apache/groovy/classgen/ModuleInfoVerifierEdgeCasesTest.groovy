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

import org.apache.groovy.ast.ModuleInfoNode
import org.apache.groovy.ast.ModuleInfoNode.Exports
import org.apache.groovy.ast.ModuleInfoNode.Opens
import org.apache.groovy.ast.ModuleInfoNode.Provides
import org.apache.groovy.ast.ModuleInfoNode.Requires
import org.apache.groovy.ast.ModuleInfoNode.Uses
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.classgen.AsmClassGenerator
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.ProcessingUnit
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.lang.module.FindException
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference

/**
 * Branches that the pipeline does not reach on its own: guards, the module reader
 * required by {@code ModuleReference}, and emitter fallbacks.
 */
final class ModuleInfoVerifierEdgeCasesTest {

    @AfterEach
    void clearFinderOverrides() {
        ModuleInfoVerifier.classpathFinderOverride = null
        ModuleInfoVerifier.accessibilityFinderOverride = null
    }

    @Test
    void testSourceNameAndMissingDeclaration() {
        assert !ModuleInfoNode.isModuleInfoSource(null)
        assert !ModuleInfoNode.isModuleInfoSource('')
        assert !ModuleInfoNode.isModuleInfoSource('Foo.groovy')
        assert ModuleInfoNode.isModuleInfoSource('module-info.groovy')
        assert ModuleInfoNode.isModuleInfoSource('module-info.java')
        assert ModuleInfoNode.isModuleInfoSource('src/module-info.groovy')
        assert ModuleInfoNode.isModuleInfoSource('src\\module-info.java')
        assert ModuleInfoNode.of(null) == null
        assert ModuleInfoNode.of(new ClassNode('Foo', 0, ClassHelper.OBJECT_TYPE)) == null

        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def source = new SourceUnit('Foo.groovy', 'class Foo {}', config, loader, new ErrorCollector(config))
        def guarded = new ClassNode('module-info', 0, ClassHelper.OBJECT_TYPE)
        guarded.putNodeMetaData(ModuleInfoNode.class, new ModuleInfoNode('demo.guard', false, []))
        ModuleInfoVerifier.check(null, guarded)
        ModuleInfoVerifier.check(source, new ClassNode('Foo', 0, ClassHelper.OBJECT_TYPE))
    }

    @Test
    void testVerifierWithoutCompileUnit() {
        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def looseDir = File.createTempDir()
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
        def moduleInfo = source.AST.classes.find { it.name == 'module-info' }
        def declaration = ModuleInfoNode.of(moduleInfo)
        declaration.addAnnotation(new AnnotationNode(ClassHelper.make('missing.Anno')))
        // Static-star import node carries a null package name; simple-name lookup must skip it.
        source.AST.starImports << new ImportNode(ClassHelper.OBJECT_TYPE)
        source.AST.starImports << new ImportNode('')
        // Package name without a trailing dot, so the normalizer branch runs.
        // A second copy of the same package hits the "already a candidate" side.
        source.AST.starImports << new ImportNode('java.util')
        source.AST.starImports << new ImportNode('java.util')
        assert source.AST.unit == null
        ModuleInfoVerifier.check(source, moduleInfo)
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
        def moduleInfo = source.AST.classes.find { it.name == 'module-info' }
        ModuleInfoVerifier.check(source, moduleInfo)
        def declaration = ModuleInfoNode.of(moduleInfo)
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
        def classNode = new ClassNode('module-info', Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, ClassHelper.OBJECT_TYPE)
        classNode.setModule(module)
        module.addClass(classNode)
        compileUnit.addModule(module)
        def declaration = new ModuleInfoNode('demo.emit', false, [
            new Requires('java.sql', true, true),
            new Exports('demo.hid', ['java.logging']),
            new Opens('demo.hid', []),
            new Uses('java.lang.Runnable'),
            new Provides('java.lang.Runnable', ['demo.hid.H'])
        ])
        declaration.addAnnotation(new AnnotationNode(ClassHelper.DEPRECATED_TYPE))
        classNode.addAnnotation(declaration.annotations[0])
        classNode.putNodeMetaData(ModuleInfoNode.class, declaration)

        def writer = new ClassWriter(0)
        def generator = new AsmClassGenerator(null, new GeneratorContext(compileUnit), writer, 'module-info.groovy')
        def emit = AsmClassGenerator.getDeclaredMethod('emitModuleInfo', ClassNode, ModuleInfoNode, int)
        emit.accessible = true
        // Below the module class-file minimum: the emitter must raise it to V9.
        emit.invoke(generator, classNode, declaration, 52)
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

        def skipping = new ClassVisitor(Opcodes.ASM9) {}
        def skippingGenerator = new AsmClassGenerator(null, new GeneratorContext(compileUnit), skipping, 'module-info.groovy')
        emit.invoke(skippingGenerator, classNode, declaration, Opcodes.V17)

        // A default-package class and provider are skipped (unnamed packages cannot appear in ModulePackages).
        def looseType = new ClassNode('Loose', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        module.addClass(looseType)
        declaration.provides[0].resolvedProviders = [looseType, hidden]
        def unnamed = new ClassWriter(0)
        def unnamedGenerator = new AsmClassGenerator(null, new GeneratorContext(compileUnit), unnamed, 'module-info.groovy')
        emit.invoke(unnamedGenerator, classNode, declaration, Opcodes.V17)

        // Module java.base has no implicit requires edge, and a class with no compile unit skips package scanning.
        def baseDecl = new ModuleInfoNode('java.base', false, [])
        def baseNode = new ClassNode('module-info', Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, ClassHelper.OBJECT_TYPE)
        def baseWriter = new ClassWriter(0)
        def baseGenerator = new AsmClassGenerator(null, new GeneratorContext(compileUnit), baseWriter, 'module-info.groovy')
        emit.invoke(baseGenerator, baseNode, baseDecl, Opcodes.V17)
        def base = ModuleDescriptor.read(new ByteArrayInputStream(baseWriter.toByteArray()))
        assert base.name() == 'java.base'
        assert base.requires().empty
    }

    @Test
    void testNullModuleAndUnparsedSource() {
        def parsed = parsed('module demo.nullmod { uses Runnable; }')
        def classNode = parsed.AST.classes.find { it.name == 'module-info' }
        classNode.setModule(null)
        ModuleInfoVerifier.check(parsed, classNode)
        assert messages(parsed).contains('unable to resolve class Runnable')

        def config = new CompilerConfiguration()
        def loader = new GroovyClassLoader()
        def unparsed = new SourceUnit('module-info.groovy', 'module demo.x {}', config, loader, new ErrorCollector(config))
        def info = new ClassNode('module-info', Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, ClassHelper.OBJECT_TYPE)
        def module = new ModuleNode(unparsed)
        info.setModule(module)
        def declaration = new ModuleInfoNode('demo.x', false, [
            new ModuleInfoNode.Requires('java.base', false, false)
        ])
        def hosted = ClassHelper.makeWithoutCaching('not.yet.Resolved')
        hosted.putNodeMetaData(ModuleNode, new ModuleNode(new CompileUnit(loader, null, config)))
        declaration.addAnnotation(new AnnotationNode(hosted))
        info.putNodeMetaData(ModuleInfoNode.class, declaration)
        assert unparsed.AST == null
        ModuleInfoVerifier.check(unparsed, info)
        assert messages(unparsed).contains('annotation @not.yet.Resolved is not accessible')
    }

    @Test
    void testResolvedModuleNoLongerOpenable() {
        ModuleInfoVerifier.accessibilityFinderOverride = new ModuleFinder() {
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
        ModuleInfoVerifier.check(parsed, parsed.AST.classes.find { it.name == 'module-info' })
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

    private static void assertResolutionFailure(String failureMessage, String moduleName, String missing) {
        ModuleInfoVerifier.classpathFinderOverride = new ModuleFinder() {
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
        try {
            def parsed = parsed("module ${moduleName} { requires ${missing}; }")
            ModuleInfoVerifier.check(parsed, parsed.AST.classes.find { it.name == 'module-info' })
            assert messages(parsed).contains('module resolution failed for ' + moduleName)
        } finally {
            ModuleInfoVerifier.classpathFinderOverride = null
        }
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
