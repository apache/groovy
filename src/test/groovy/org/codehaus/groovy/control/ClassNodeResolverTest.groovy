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
package org.codehaus.groovy.control


import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassWriter

import java.nio.file.Files
import java.nio.file.Path

import static groovy.test.GroovyAssert.shouldFail
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import static org.objectweb.asm.Opcodes.V17

/**
 * Tests for {@link ClassNodeResolver}: cache behaviour, ASM vs class-loader lookup,
 * package-info resolution, and recovery from {@link NoClassDefFoundError} when a
 * class is present but cannot be linked. A groovy source may replace an existing
 * class only when that class came from another loader; same-loader linkage
 * failure is not treated as {@link ClassNotFoundException}.
 */
class ClassNodeResolverTest {

    @TempDir
    Path tempDir

    //--------------------------------------------------------------------------
    // LookupResult

    @Test
    void lookupResultStoresClassNode() {
        def cn = ClassHelper.OBJECT_TYPE
        def result = new ClassNodeResolver.LookupResult(null, cn)
        assert result.classNode
        assert !result.sourceUnit
        assert result.getClassNode().is(cn)
        assert result.getSourceUnit() == null
    }

    @Test
    void lookupResultStoresSourceUnit() {
        def su = SourceUnit.create('Dummy', 'class Dummy {}')
        def result = new ClassNodeResolver.LookupResult(su, null)
        assert result.sourceUnit
        assert !result.classNode
        assert result.getSourceUnit().is(su)
        assert result.getClassNode() == null
    }

    @Test
    void lookupResultRejectsBothNull() {
        def e = shouldFail(IllegalArgumentException) {
            new ClassNodeResolver.LookupResult(null, null)
        }
        assert e.message.contains('SourceUnit') || e.message.contains('ClassNode')
    }

    @Test
    void lookupResultRejectsBothSet() {
        def e = shouldFail(IllegalArgumentException) {
            new ClassNodeResolver.LookupResult(SourceUnit.create('Dummy', 'class Dummy {}'), ClassHelper.OBJECT_TYPE)
        }
        assert e.message.contains('same time')
    }

    //--------------------------------------------------------------------------
    // cache / resolveName / findClassNode

    @Test
    void resolveNameReturnsNullAndCachesMiss() {
        def resolver = new ClassNodeResolver()
        def unit = unitWith([:])
        assert resolver.resolveName('cnr.missing.NoSuchClass', unit) == null
        assert resolver.getFromClassCache('cnr.missing.NoSuchClass').is(ClassNodeResolver.NO_CLASS)
        // a second lookup must not escape the negative cache
        assert resolver.resolveName('cnr.missing.NoSuchClass', unit) == null
    }

    @Test
    void resolveNameHonoursCachedNoClassWithoutLookup() {
        def resolver = new ClassNodeResolver()
        resolver.cacheClass('cnr.cached.Absent', ClassNodeResolver.NO_CLASS)
        // compilation unit is unused on a cache hit; passing null guards the short-circuit
        assert resolver.resolveName('cnr.cached.Absent', null) == null
    }

    @Test
    void resolveNameReturnsCachedClassNode() {
        def resolver = new ClassNodeResolver()
        def cn = ClassHelper.make('cnr.cached.Present')
        resolver.cacheClass('cnr.cached.Present', cn)
        def result = resolver.resolveName('cnr.cached.Present', null)
        assert result.classNode
        assert result.getClassNode().is(cn)
    }

    @Test
    void findClassNodeDoesNotCacheMisses() {
        def resolver = new ClassNodeResolver()
        def unit = unitWith([:])
        assert resolver.findClassNode('cnr.missing.Uncached', unit) == null
        assert resolver.getFromClassCache('cnr.missing.Uncached') == null
    }

    @Test
    void findClassNodeReturnsNullForNullCompilationUnit() {
        assert new ClassNodeResolver().findClassNode('java.lang.String', null) == null
    }

    @Test
    void resolveNameFindsJavaLangString() {
        def resolver = new ClassNodeResolver()
        def result = resolver.resolveName('java.lang.String', unitWith([:]))
        assert result.classNode
        assert result.getClassNode().isResolved()
        assert result.getClassNode().name == 'java.lang.String'
    }

    @Test
    void resolveNameWithClassLoaderResolvingOnlyFindsString() {
        def resolver = new ClassNodeResolver()
        def result = resolver.resolveName('java.lang.String', unitWith(asmResolving: false))
        assert result.classNode
        assert result.getClassNode().isResolved()
        assert result.getClassNode().name == 'java.lang.String'
    }

    @Test
    void resolveNameWithBothStrategiesDisabledDoesNotLoadClasses() {
        def resolver = new ClassNodeResolver()
        def result = resolver.resolveName('java.lang.String', unitWith(asmResolving: false, classLoaderResolving: false))
        // both lookup strategies are off, and java.* names are not treated as scripts
        assert result == null
        assert resolver.getFromClassCache('java.lang.String').is(ClassNodeResolver.NO_CLASS)
    }

    //--------------------------------------------------------------------------
    // resolvePackage (GROOVY-12207)

    @Test
    void resolvePackageReturnsNullWhenClassLoaderIsNull() {
        def unit = new CompilationUnit()
        // setClassLoader(null) would install a fresh loader; poke the field instead
        unit.@classLoader = null
        assert new ClassNodeResolver().resolvePackage('cnr.pkg.noloader', unit) == null
    }

    @Test
    void resolvePackageRejectsNullEmptyOrNullUnit() {
        def resolver = new ClassNodeResolver()
        def unit = unitWith([:])
        assert resolver.resolvePackage(null, unit) == null
        assert resolver.resolvePackage('', unit) == null
        assert resolver.resolvePackage('cnr.pkg', null) == null
    }

    @Test
    void resolvePackageReturnsNullWhenPackageInfoIsMissing() {
        def resolver = new ClassNodeResolver()
        assert resolver.resolvePackage('cnr.pkg.absent', unitWith([:])) == null
        // negative cache: a second call is still a miss
        assert resolver.resolvePackage('cnr.pkg.absent', unitWith([:])) == null
    }

    @Test
    void resolvePackageReadsAnnotationsFromPackageInfo() {
        String pkg = 'cnr.pkg.annotated'
        writeBytes(tempDir, pkg.replace('.', '/') + '/package-info', packageInfoBytes(pkg.replace('.', '/') + '/package-info', true))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            def resolver = new ClassNodeResolver()
            def pkgNode = resolver.resolvePackage(pkg, unit)
            assert pkgNode != null
            assert pkgNode.name == pkg + '.'
            assert pkgNode.annotations.any { it.classNode.name == Deprecated.name }
            assert resolver.resolvePackage(pkg, unit).is(pkgNode)
        }
    }

    @Test
    void resolvePackageReturnsNullWhenPackageInfoHasNoAnnotations() {
        String pkg = 'cnr.pkg.bare'
        writeBytes(tempDir, pkg.replace('.', '/') + '/package-info', packageInfoBytes(pkg.replace('.', '/') + '/package-info', false))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            assert new ClassNodeResolver().resolvePackage(pkg, unit) == null
        }
    }

    @Test
    void resolvePackageCachesMissesAfterTheFirstLookup() {
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        assert resolver.resolvePackage('cnr.pkg.counted', unit) == null
        int afterFirst = loader.resourceLookups
        assert afterFirst > 0
        assert resolver.resolvePackage('cnr.pkg.counted', unit) == null
        assert loader.resourceLookups == afterFirst
    }

    @Test
    void resolvePackageIgnoresPackageInfoWithAMismatchedName() {
        String actualPkg = 'cnr.pkg.other'
        Path file = writeBytes(tempDir, actualPkg.replace('.', '/') + '/package-info',
                packageInfoBytes(actualPkg.replace('.', '/') + '/package-info', true))
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/pkg/mismatch/package-info.class'] = file.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolvePackage('cnr.pkg.mismatch', unit) == null
    }

    @Test
    void resolvePackageSwallowsUnreadablePackageInfo() {
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/pkg/unreadable/package-info.class'] = new URL('file:///definitely/does/not/exist/package-info.class')
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolvePackage('cnr.pkg.unreadable', unit) == null
    }

    @Test
    void resolvePackageSwallowsMalformedPackageInfo() {
        Path garbage = tempDir.resolve('cnr/pkg/garbage/package-info.class')
        Files.createDirectories(garbage.parent)
        Files.write(garbage, [0, 1, 2, 3, 4] as byte[])
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/pkg/garbage/package-info.class'] = garbage.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolvePackage('cnr.pkg.garbage', unit) == null
    }

    //--------------------------------------------------------------------------
    // NoClassDefFoundError recovery

    @Test
    void ncdfeForUnlinkableClassIsRecoveredByDecompilation() {
        String name = 'cnr.ncdfe.HasDep'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/'), 'cnr/ncdfe/MissingDep'))
        parentLoader(tempDir).withCloseable { parent ->
            shouldFail(NoClassDefFoundError) {
                parent.loadClass(name)
            }
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result != null
            assert result.classNode
            assert result.getClassNode().name == name
            assert result.getClassNode() instanceof DecompiledClassNode
        }
    }

    @Test
    void ncdfeRecoveryIsCachedOnTheResolver() {
        String name = 'cnr.ncdfe.CachedHasDep'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/'), 'cnr/ncdfe/MissingDep'))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            def resolver = new ClassNodeResolver()
            def first = resolver.resolveName(name, unit)
            assert first.classNode
            def second = resolver.resolveName(name, unit)
            assert second.getClassNode().is(first.getClassNode())
        }
    }

    @Test
    void unlinkableClassIsAlsoFoundWhenAsmResolvingIsEnabled() {
        String name = 'cnr.ncdfe.AsmHasDep'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/'), 'cnr/ncdfe/MissingDep'))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:]) // asmResolving defaults to on
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
            assert result.getClassNode() instanceof DecompiledClassNode
        }
    }

    @Test
    void unrecoverableNcdfeIsRethrownWithTheLookedUpName() {
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.Broken'] = new NoClassDefFoundError('cnr/ncdfe/MissingDep')
        def unit = new CompilationUnit(config, null, loader)
        def error = shouldFail(NoClassDefFoundError) {
            new ClassNodeResolver().resolveName('cnr.ncdfe.Broken', unit)
        }
        assert error.message.contains('cnr.ncdfe.Broken')
        assert error.message.contains('cnr/ncdfe/MissingDep')
        assert error.cause instanceof NoClassDefFoundError
        assert error.cause.message == 'cnr/ncdfe/MissingDep'
        // must not have been cached as a miss — a retry still throws
        shouldFail(NoClassDefFoundError) {
            new ClassNodeResolver().resolveName('cnr.ncdfe.Broken', unit)
        }
    }

    @Test
    void unrecoverableNcdfeWithEmptyMessageStillNamesTheLookedUpClass() {
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.EmptyMsg'] = new NoClassDefFoundError('')
        def unit = new CompilationUnit(config, null, loader)
        def error = shouldFail(NoClassDefFoundError) {
            new ClassNodeResolver().resolveName('cnr.ncdfe.EmptyMsg', unit)
        }
        assert error.message.contains('cnr.ncdfe.EmptyMsg')
        assert error.cause instanceof NoClassDefFoundError
    }

    @Test
    void unrecoverableNcdfeWithNullMessageStillNamesTheLookedUpClass() {
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.NullMsg'] = new NoClassDefFoundError()
        def unit = new CompilationUnit(config, null, loader)
        def error = shouldFail(NoClassDefFoundError) {
            new ClassNodeResolver().resolveName('cnr.ncdfe.NullMsg', unit)
        }
        assert error.message.contains('cnr.ncdfe.NullMsg')
        assert error.cause instanceof NoClassDefFoundError
    }

    @Test
    void wrongNameNcdfeIsTreatedAsAMiss() {
        String name = 'cnr.ncdfe.WrongName'
        // path matches the request; bytecode declares a different binary name
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes('cnr/ncdfe/wrongname'))
        parentLoader(tempDir).withCloseable { parent ->
            shouldFail(NoClassDefFoundError) {
                parent.loadClass(name)
            }
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            def resolver = new ClassNodeResolver()
            assert resolver.resolveName(name, unit) == null
            assert resolver.getFromClassCache(name).is(ClassNodeResolver.NO_CLASS)
        }
    }

    @Test
    void wrongNameNcdfeFallsBackToAGroovySource() {
        String name = 'cnr.ncdfe.WrongNameSrc'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes('cnr/ncdfe/wrongnamesrc'))
        Path source = tempDir.resolve('cnr/ncdfe/WrongNameSrc.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class WrongNameSrc {}')
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result != null
            assert result.sourceUnit
            assert result.getSourceUnit().name.contains('WrongNameSrc.groovy')
        }
    }

    @Test
    void nameMismatchWithAsmResolvingOnIsAMissWithoutRethrowingNcdfe() {
        String name = 'cnr.ncdfe.AsmMismatch'
        Path file = writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes('cnr/ncdfe/asmmismatch'))
        def config = configWith([:]) // asmResolving defaults to on
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses[name] = new NoClassDefFoundError('cnr/ncdfe/MissingDep')
        loader.resources[name.replace('.', '/') + '.class'] = file.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        assert resolver.resolveName(name, unit) == null
        assert resolver.getFromClassCache(name).is(ClassNodeResolver.NO_CLASS)
    }

    @Test
    void nameMismatchIsDetectedWithoutInspectingNcdfeText() {
        String name = 'cnr.ncdfe.NomIncorrect'
        Path file = writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes('cnr/ncdfe/nomincorrect'))
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        // text must not be why this is a miss (HotSpot English "wrong name" is not used)
        loader.loadResponses[name] = new NoClassDefFoundError(name + ' (nom incorrect: cnr/ncdfe/nomincorrect)')
        loader.resources[name.replace('.', '/') + '.class'] = file.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        assert resolver.resolveName(name, unit) == null
        assert resolver.getFromClassCache(name).is(ClassNodeResolver.NO_CLASS)
    }

    @Test
    void lyingWrongNameMessageDoesNotSkipDecompilationOfMatchingBytes() {
        String name = 'cnr.ncdfe.LyingMsg'
        Path file = writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/'), 'cnr/ncdfe/MissingDep'))
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses[name] = new NoClassDefFoundError(name.replace('.', '/') + ' (wrong name: cnr/ncdfe/missingdep)')
        loader.resources[name.replace('.', '/') + '.class'] = file.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        def result = new ClassNodeResolver().resolveName(name, unit)
        assert result != null
        assert result.classNode
        assert result.getClassNode().name == name
        assert result.getClassNode() instanceof DecompiledClassNode
    }

    @Test
    void ncdfeWithoutBytecodeIsRethrownEvenIfAGroovySourceExists() {
        Path source = tempDir.resolve('cnr/ncdfe/FromSource.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class FromSource {}')
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.FromSource'] = new NoClassDefFoundError('cnr/ncdfe/MissingDep')
        loader.resourceLoader = { String filename ->
            filename == 'cnr.ncdfe.FromSource' ? source.toUri().toURL() : null
        } as GroovyResourceLoader
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        def error = shouldFail(NoClassDefFoundError) {
            resolver.resolveName('cnr.ncdfe.FromSource', unit)
        }
        assert error.message.contains('cnr.ncdfe.FromSource')
        assert error.cause instanceof NoClassDefFoundError
        // must not have been cached as a miss — a retry still throws
        assert resolver.getFromClassCache('cnr.ncdfe.FromSource') == null
        shouldFail(NoClassDefFoundError) {
            resolver.resolveName('cnr.ncdfe.FromSource', unit)
        }
    }

    @Test
    void sameLoaderUnlinkableClassIsNotReplacedByAGroovySource() {
        String name = 'cnr.ncdfe.SameLoaderHasDep'
        // timestamp 0 so a same-loader tryAsScript(oldClass) would take the source
        writeBytes(tempDir, name.replace('.', '/'), unlinkableStampedClassBytes(name.replace('.', '/'), '0'))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class SameLoaderHasDep {}')
        def config = configWith(asmResolving: false)
        def parent = new URLClassLoader(new URL[0], (ClassLoader) null)
        parent.withCloseable {
            def loader = new GroovyClassLoader(parent, config)
            loader.addClasspath(tempDir.toAbsolutePath().toString())
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
            assert result.getClassNode() instanceof DecompiledClassNode
        }
    }

    @Test
    void parentUnlinkableClassWithOlderSourceIsKeptWhenAsmResolvingIsDisabled() {
        String name = 'cnr.ncdfe.ParentOlder'
        writeBytes(tempDir, name.replace('.', '/'), unlinkableStampedClassBytes(name.replace('.', '/'), Long.MAX_VALUE.toString()))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class ParentOlder {}')
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
            assert result.getClassNode() instanceof DecompiledClassNode
        }
    }

    @Test
    void parentUnlinkableClassWithNewerSourceIsReplacedWhenAsmResolvingIsDisabled() {
        String name = 'cnr.ncdfe.ParentNewer'
        writeBytes(tempDir, name.replace('.', '/'), unlinkableStampedClassBytes(name.replace('.', '/'), '0'))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class ParentNewer {}')
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.sourceUnit
            assert result.getSourceUnit().name.contains('ParentNewer.groovy')
        }
    }

    @Test
    void unrecoverableNcdfeWithAsmResolvingOnIsRethrown() {
        def config = configWith([:]) // asmResolving defaults to on
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.AsmBroken'] = new NoClassDefFoundError('cnr/ncdfe/MissingDep')
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        def error = shouldFail(NoClassDefFoundError) {
            resolver.resolveName('cnr.ncdfe.AsmBroken', unit)
        }
        assert error.message.contains('cnr.ncdfe.AsmBroken')
        assert error.cause instanceof NoClassDefFoundError
        assert resolver.getFromClassCache('cnr.ncdfe.AsmBroken') == null
        shouldFail(NoClassDefFoundError) {
            resolver.resolveName('cnr.ncdfe.AsmBroken', unit)
        }
    }

    @Test
    void definedClassIsFoundEvenWhenTheClassResourceHasADifferentBytecodeName() {
        String name = 'cnr.ncdfe.DefinedDespiteMismatch'
        Path foreign = writeBytes(tempDir, 'cnr/ncdfe/foreign', simpleClassBytes('cnr/ncdfe/foreign'))
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        Class defined = loader.defineClass(name, simpleClassBytes(name.replace('.', '/')))
        loader.resources[name.replace('.', '/') + '.class'] = foreign.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        def result = new ClassNodeResolver().resolveName(name, unit)
        assert result.classNode
        assert result.getClassNode().isResolved()
        assert result.getClassNode().typeClass.is(defined)
        assert !(result.getClassNode() instanceof DecompiledClassNode)
    }

    @Test
    void compilationDuringClassLookupIsABug() {
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.Compiling'] = new CompilationFailedException(Phases.SEMANTIC_ANALYSIS, null)
        def unit = new CompilationUnit(config, null, loader)
        def error = shouldFail(GroovyBugError) {
            new ClassNodeResolver().resolveName('cnr.ncdfe.Compiling', unit)
        }
        assert error.message.contains('cnr.ncdfe.Compiling')
        assert error.cause instanceof CompilationFailedException
    }

    @Test
    void loadClassReturningNullIsAMiss() {
        def config = configWith(asmResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.loadResponses['cnr.ncdfe.NullClass'] = null
        def unit = new CompilationUnit(config, null, loader)
        def resolver = new ClassNodeResolver()
        assert resolver.resolveName('cnr.ncdfe.NullClass', unit) == null
        assert resolver.getFromClassCache('cnr.ncdfe.NullClass').is(ClassNodeResolver.NO_CLASS)
    }

    @Test
    void unlinkableClassCanBeReferencedFromAScriptWhenAsmResolvingIsDisabled() {
        String name = 'cnr.ncdfe.ScriptHasDep'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/'), 'cnr/ncdfe/MissingDep'))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            def unit = new CompilationUnit(config, null, loader)
            unit.addSource('script.groovy', "def x = (${name}) null")
            unit.compile(Phases.SEMANTIC_ANALYSIS)
            assert unit.errorCollector.errorCount == 0
        }
    }

    //--------------------------------------------------------------------------
    // scripts, decompilation edge cases, class-loader origin

    @Test
    void javaNamesAreNotResolvedAsScripts() {
        Path source = tempDir.resolve('String.groovy')
        Files.writeString(source, 'class String {}')
        def config = configWith(asmResolving: false, classLoaderResolving: false)
        def loader = new GroovyClassLoader(ClassNodeResolverTest.classLoader, config)
        loader.resourceLoader = { String filename ->
            filename == 'java.lang.String' ? source.toUri().toURL() : null
        } as GroovyResourceLoader
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('java.lang.String', unit) == null
    }

    @Test
    void innerClassNamesAreNotResolvedAsScripts() {
        Path source = tempDir.resolve('Outer$Inner.groovy')
        Files.writeString(source, 'class Inner {}')
        def config = configWith(asmResolving: false, classLoaderResolving: false)
        def loader = new GroovyClassLoader(ClassNodeResolverTest.classLoader, config)
        loader.resourceLoader = { String filename ->
            filename == 'cnr.script.Outer$Inner' ? source.toUri().toURL() : null
        } as GroovyResourceLoader
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('cnr.script.Outer$Inner', unit) == null
    }

    @Test
    void malformedScriptUrlIsIgnored() {
        def config = configWith(asmResolving: false, classLoaderResolving: false)
        def loader = new GroovyClassLoader(ClassNodeResolverTest.classLoader, config)
        loader.resourceLoader = { String filename ->
            throw new MalformedURLException(filename)
        } as GroovyResourceLoader
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('cnr.script.Malformed', unit) == null
    }

    @Test
    void missingClassFallsBackToAGroovySource() {
        Path source = tempDir.resolve('cnr/script/FromClasspath.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, 'class FromClasspath {}')
        def config = configWith(asmResolving: false)
        def loader = new GroovyClassLoader(ClassNodeResolverTest.classLoader, config)
        loader.resourceLoader = { String filename ->
            filename == 'cnr.script.FromClasspath' ? source.toUri().toURL() : null
        } as GroovyResourceLoader
        def unit = new CompilationUnit(config, null, loader)
        def result = new ClassNodeResolver().resolveName('cnr.script.FromClasspath', unit)
        assert result.sourceUnit
        assert result.getSourceUnit().name.contains('FromClasspath.groovy')
    }

    @Test
    void findDecompiledIgnoresAResourceWhoseBytecodeNameDoesNotMatch() {
        String actual = 'cnr.decompiled.Actual'
        Path file = writeBytes(tempDir, actual.replace('.', '/'), simpleClassBytes(actual.replace('.', '/')))
        def config = configWith(asmResolving: true, classLoaderResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Requested.class'] = file.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('cnr.decompiled.Requested', unit) == null
    }

    @Test
    void findDecompiledSwallowsUnreadableClassFilesWhenClassLoaderResolvingIsOn() {
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Unreadable.class'] = new URL('file:///definitely/does/not/exist/Unreadable.class')
        loader.loadResponses['cnr.decompiled.Unreadable'] = new ClassNotFoundException('cnr.decompiled.Unreadable')
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('cnr.decompiled.Unreadable', unit) == null
    }

    @Test
    void findDecompiledRethrowsIllegalArgumentExceptionFromATruncatedClassFile() {
        Path tiny = tempDir.resolve('cnr/decompiled/Tiny.class')
        Files.createDirectories(tiny.parent)
        Files.write(tiny, [0xCA, 0xFE] as byte[])
        def config = configWith(classLoaderResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Tiny.class'] = tiny.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        shouldFail(IllegalArgumentException) {
            new ClassNodeResolver().resolveName('cnr.decompiled.Tiny', unit)
        }
    }

    @Test
    void findDecompiledRethrowsParseErrorsWhenClassLoaderResolvingIsOff() {
        Path garbage = tempDir.resolve('cnr/decompiled/Garbage.class')
        Files.createDirectories(garbage.parent)
        Files.write(garbage, [0, 1, 2, 3, 4] as byte[])
        def config = configWith(classLoaderResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Garbage.class'] = garbage.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        def error = shouldFail(IllegalArgumentException) {
            new ClassNodeResolver().resolveName('cnr.decompiled.Garbage', unit)
        }
        assert error.message.startsWith('Failed to parse class cnr.decompiled.Garbage')
        assert error.cause instanceof IndexOutOfBoundsException
    }

    @Test
    void findDecompiledSwallowsParseErrorsWhenClassLoaderResolvingIsOn() {
        Path garbage = tempDir.resolve('cnr/decompiled/Garbage2.class')
        Files.createDirectories(garbage.parent)
        Files.write(garbage, [0, 1, 2, 3, 4] as byte[])
        def config = configWith([:])
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Garbage2.class'] = garbage.toUri().toURL()
        loader.loadResponses['cnr.decompiled.Garbage2'] = new ClassNotFoundException('cnr.decompiled.Garbage2')
        def unit = new CompilationUnit(config, null, loader)
        assert new ClassNodeResolver().resolveName('cnr.decompiled.Garbage2', unit) == null
    }

    @Test
    void dummyNoClassNodeRejectsRedirect() {
        shouldFail(GroovyBugError) {
            ClassNodeResolver.NO_CLASS.setRedirect(ClassHelper.OBJECT_TYPE)
        }
    }

    @Test
    void classDefinedByTheCompilationLoaderIsUsedWithoutScriptLookup() {
        def config = configWith(asmResolving: false)
        def loader = new GroovyClassLoader(ClassNodeResolverTest.classLoader, config)
        String name = 'cnr.same.Defined'
        loader.defineClass(name, simpleClassBytes(name.replace('.', '/')))
        def unit = new CompilationUnit(config, null, loader)
        def result = new ClassNodeResolver().resolveName(name, unit)
        assert result.classNode
        assert result.getClassNode().name == name
        assert result.getClassNode().isResolved()
    }

    @Test
    void newerSourceReplacesALoadedClassFromAnotherLoader() {
        String name = 'cnr.recompile.LoadedStamped'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, "class LoadedStamped {}")
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith(asmResolving: false)
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.sourceUnit
            assert result.getSourceUnit().name.contains('LoadedStamped.groovy')
        }
    }

    @Test
    void newerSourceReplacesAClassFromAnotherLoader() {
        String name = 'cnr.recompile.Stamped'
        // timestamp "0" so any on-disk source is newer than the decompiled class
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, "class Stamped {}")
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.sourceUnit
            assert result.getSourceUnit().name.contains('Stamped.groovy')
        }
    }

    @Test
    void olderSourceDoesNotReplaceAClassFromAnotherLoader() {
        String name = 'cnr.recompile.Fresh'
        // Long.MAX_VALUE as a hex-ish decimal that Long.decode accepts
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), Long.MAX_VALUE.toString()))
        Path source = tempDir.resolve(name.replace('.', '/') + '.groovy')
        Files.createDirectories(source.parent)
        Files.writeString(source, "class Fresh {}")
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? source.toUri().toURL() : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
        }
    }

    @Test
    void nonFileSourceUrlUsesUrlConnectionForFreshness() {
        String name = 'cnr.recompile.Jarred'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        Path jar = tempDir.resolve('sources.jar')
        new java.util.jar.JarOutputStream(Files.newOutputStream(jar)).withCloseable { jos ->
            jos.putNextEntry(new java.util.jar.JarEntry(name.replace('.', '/') + '.groovy'))
            jos.write('class Jarred {}'.bytes)
            jos.closeEntry()
        }
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            URL sourceUrl = new URL('jar:' + jar.toUri().toURL().toExternalForm() + '!/' + name.replace('.', '/') + '.groovy')
            loader.resourceLoader = { String filename ->
                filename == name ? sourceUrl : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.sourceUnit
            assert result.getSourceUnit().name.contains('Jarred.groovy')
        }
        // jar: connections must not pin the file: Windows cannot delete an open JAR,
        // which is what fails JUnit @TempDir cleanup for this test.
        Files.delete(jar)
    }

    @Test
    void nonFileSourceUrlDisablesUrlConnectionCaches() {
        String name = 'cnr.recompile.Tracked'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        def handler = new CacheTrackingURLStreamHandler(System.currentTimeMillis())
        URL sourceUrl = new URL('cnrtrack', 'localhost', -1, '/' + name.replace('.', '/') + '.groovy', handler)
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? sourceUrl : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.sourceUnit
        }
        assert handler.useCaches == Boolean.FALSE
        assert handler.lastModifiedCalled
        assert handler.inputStreamClosed
    }

    @Test
    void olderNonFileSourceKeepsTheExistingClass() {
        String name = 'cnr.recompile.TrackedOld'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        def handler = new CacheTrackingURLStreamHandler(0L)
        URL sourceUrl = new URL('cnrtrack', 'localhost', -1, '/' + name.replace('.', '/') + '.groovy', handler)
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? sourceUrl : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
        }
        assert handler.lastModifiedCalled
        assert handler.useCaches == Boolean.FALSE
    }

    @Test
    void missingJarEntryKeepsTheExistingClass() {
        String name = 'cnr.recompile.MissingEntry'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        Path jar = tempDir.resolve('other.jar')
        new java.util.jar.JarOutputStream(Files.newOutputStream(jar)).withCloseable { jos ->
            jos.putNextEntry(new java.util.jar.JarEntry('unrelated.txt'))
            jos.write('x'.bytes)
            jos.closeEntry()
        }
        URL sourceUrl = new URL('jar:' + jar.toUri().toURL().toExternalForm() + '!/' + name.replace('.', '/') + '.groovy')
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? sourceUrl : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
        }
        Files.delete(jar)
    }

    @Test
    void unreadableNonFileSourceKeepsTheExistingClass() {
        String name = 'cnr.recompile.UnreadableSrc'
        writeBytes(tempDir, name.replace('.', '/'), stampedClassBytes(name.replace('.', '/'), '0'))
        parentLoader(tempDir).withCloseable { parent ->
            def config = configWith([:])
            def loader = new GroovyClassLoader(parent, config)
            loader.resourceLoader = { String filename ->
                filename == name ? new URL('jar:file:///definitely/does/not/exist.jar!/UnreadableSrc.groovy') : null
            } as GroovyResourceLoader
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
        }
    }

    @Test
    void decompiledClassFromThisLoaderIsReturnedWithoutScriptLookup() {
        String name = 'cnr.local.OnlyHere'
        writeBytes(tempDir, name.replace('.', '/'), simpleClassBytes(name.replace('.', '/')))
        def config = configWith([:])
        def parent = new URLClassLoader(new URL[0], (ClassLoader) null)
        parent.withCloseable {
            def loader = new GroovyClassLoader(parent, config)
            loader.addClasspath(tempDir.toAbsolutePath().toString())
            def unit = new CompilationUnit(config, null, loader)
            def result = new ClassNodeResolver().resolveName(name, unit)
            assert result.classNode
            assert result.getClassNode().name == name
            assert result.getClassNode() instanceof DecompiledClassNode
        }
    }

    @Test
    void findDecompiledRethrowsIllegalArgumentExceptionFromAsm() {
        // long enough to pass ASM's size check, too broken to parse: ClassReader throws IAE
        byte[] bytes = new byte[24]
        bytes[0] = (byte) 0xCA
        bytes[1] = (byte) 0xFE
        bytes[2] = (byte) 0xBA
        bytes[3] = (byte) 0xBE
        Path tiny = tempDir.resolve('cnr/decompiled/Iae.class')
        Files.createDirectories(tiny.parent)
        Files.write(tiny, bytes)
        def config = configWith(classLoaderResolving: false)
        def loader = new ControllableLoader(ClassNodeResolverTest.classLoader, config)
        loader.resources['cnr/decompiled/Iae.class'] = tiny.toUri().toURL()
        def unit = new CompilationUnit(config, null, loader)
        shouldFail(IllegalArgumentException) {
            new ClassNodeResolver().resolveName('cnr.decompiled.Iae', unit)
        }
    }

    @Test
    void parentlessLoaderUsesAClassItDefinedWithoutScriptLookup() {
        def config = configWith(asmResolving: false)
        def loader = new GroovyClassLoader((ClassLoader) null, config)
        String name = 'cnr.parentless.Defined'
        loader.defineClass(name, simpleClassBytes(name.replace('.', '/')))
        def unit = new CompilationUnit(config, null, loader)
        def result = new ClassNodeResolver().resolveName(name, unit)
        assert result.classNode
        assert result.getClassNode().name == name
    }

    //--------------------------------------------------------------------------
    // helpers

    private static CompilerConfiguration configWith(Map<String, Boolean> options) {
        def config = new CompilerConfiguration()
        config.optimizationOptions.putAll(options)
        config
    }

    private static CompilationUnit unitWith(Map<String, Boolean> options) {
        def config = configWith(options)
        new CompilationUnit(config, null, new GroovyClassLoader(ClassNodeResolverTest.classLoader, config))
    }

    private static URLClassLoader parentLoader(Path dir) {
        new URLClassLoader(dir.toUri().toURL() as URL[], (ClassLoader) null)
    }

    private static Path writeBytes(Path dir, String internalName, byte[] bytes) {
        Path file = dir.resolve(internalName + '.class')
        Files.createDirectories(file.parent)
        Files.write(file, bytes)
        file
    }

    private static byte[] simpleClassBytes(String internalName, String superInternalName = 'java/lang/Object') {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internalName, null, superInternalName, null)
        cw.visitEnd()
        cw.toByteArray()
    }

    private static byte[] stampedClassBytes(String internalName, String timestampSuffix) {
        stampedClassBytes(internalName, timestampSuffix, 'java/lang/Object')
    }

    private static byte[] unlinkableStampedClassBytes(String internalName, String timestampSuffix) {
        stampedClassBytes(internalName, timestampSuffix, 'cnr/ncdfe/MissingDep')
    }

    private static byte[] stampedClassBytes(String internalName, String timestampSuffix, String superInternalName) {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internalName, null, superInternalName, null)
        cw.visitField(ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_STATIC | org.objectweb.asm.Opcodes.ACC_FINAL,
                org.codehaus.groovy.classgen.Verifier.__TIMESTAMP__ + timestampSuffix,
                'J', null, Long.valueOf(0)).visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static byte[] packageInfoBytes(String internalName, boolean deprecated) {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC, internalName, null, 'java/lang/Object', null)
        if (deprecated) {
            def av = cw.visitAnnotation('Ljava/lang/Deprecated;', true)
            av.visitEnd()
        }
        cw.visitEnd()
        cw.toByteArray()
    }

    /**
     * GroovyClassLoader that can force {@code loadClass} outcomes and remap {@code getResource}.
     */
    static class ControllableLoader extends GroovyClassLoader {
        final Map<String, Object> loadResponses = [:]
        final Map<String, URL> resources = [:]
        int resourceLookups

        ControllableLoader(ClassLoader parent, CompilerConfiguration config) {
            super(parent, config)
        }

        @Override
        Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
                throws ClassNotFoundException, CompilationFailedException {
            if (loadResponses.containsKey(name)) {
                Object value = loadResponses.get(name)
                if (value instanceof Error) {
                    throw (Error) value
                }
                if (value instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) value
                }
                if (value instanceof RuntimeException) {
                    throw (RuntimeException) value
                }
                return (Class) value
            }
            return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
        }

        @Override
        URL getResource(String name) {
            resourceLookups += 1
            if (resources.containsKey(name)) {
                return resources.get(name)
            }
            return super.getResource(name)
        }
    }

    /**
     * Records how {@link ClassNodeResolver} opens a non-{@code file:} source URL
     * for the freshness check: caching must be off, and the stream must be closed.
     */
    static class CacheTrackingURLStreamHandler extends URLStreamHandler {
        final long lastModified
        Boolean useCaches
        boolean lastModifiedCalled
        boolean inputStreamClosed

        CacheTrackingURLStreamHandler(long lastModified) {
            this.lastModified = lastModified
        }

        @Override
        protected URLConnection openConnection(URL u) {
            new CacheTrackingURLConnection(u, this)
        }
    }

    private static class CacheTrackingURLConnection extends URLConnection {
        private final CacheTrackingURLStreamHandler handler

        CacheTrackingURLConnection(URL url, CacheTrackingURLStreamHandler handler) {
            super(url)
            this.handler = handler
        }

        @Override
        void connect() {
            connected = true
        }

        @Override
        void setUseCaches(boolean usecaches) {
            handler.useCaches = usecaches
            super.setUseCaches(usecaches)
        }

        @Override
        long getLastModified() {
            handler.lastModifiedCalled = true
            return handler.lastModified
        }

        @Override
        InputStream getInputStream() {
            return new FilterInputStream(InputStream.nullInputStream()) {
                @Override
                void close() throws IOException {
                    handler.inputStreamClosed = true
                    super.close()
                }
            }
        }
    }
}
