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
package org.apache.groovy.control

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.lang.module.FindException
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReader
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit tests for {@link ModuleOptions}: JEP 261 path splitting, JMOD discovery,
 * {@code --add-reads}, {@code --add-exports}, {@code --limit-modules}, and
 * {@code --module-version} validation.
 */
final class ModuleOptionsTest {

    @TempDir
    Path tempDir

    @Test
    void testSplitPathAndCommaList() {
        assertEquals([], ModuleOptions.splitPath(null))
        assertEquals([], ModuleOptions.splitPath(''))
        def sep = File.pathSeparator
        assertEquals(['a', 'b', 'c'], ModuleOptions.splitPath("${sep}a${sep}${sep}b${sep}c${sep}"))

        assertEquals([], ModuleOptions.splitCommaList(null))
        assertEquals([], ModuleOptions.splitCommaList('   '))
        assertEquals([], ModuleOptions.splitCommaList('a,,b'))
        assertEquals([], ModuleOptions.splitCommaList('a,'))
        assertEquals(['java.sql', 'java.logging'], ModuleOptions.splitCommaList(' java.sql , java.logging '))
    }

    @Test
    void testValidateModuleVersion() {
        assertEquals('1.2.3-beta+4', ModuleOptions.validateModuleVersion('1.2.3-beta+4').toString())
        assertThrows(IllegalArgumentException) {
            ModuleOptions.validateModuleVersion('not-a-version')
        }
    }

    @Test
    void testParseAddReads() {
        def errors = []
        assertEquals(ModuleOptions.AddReads.EMPTY, ModuleOptions.parseAddReads(null, 'demo.app', errors.&add))
        assertEquals(ModuleOptions.AddReads.EMPTY, ModuleOptions.parseAddReads([], 'demo.app', errors.&add))

        def parsed = ModuleOptions.parseAddReads([
            'demo.app=java.sql, ALL-UNNAMED, java.logging',
            'other.mod=java.xml',
            null,
            'missing-equals',
            '=java.sql',
            'demo.app=',
            ' =java.sql',
            'demo.app=java.sql,'
        ], 'demo.app', errors.&add)

        assertEquals(['java.sql', 'java.logging'] as Set, parsed.modules)
        assertTrue(parsed.readsUnnamed)
        assertEquals(6, errors.size())
        assertTrue(errors.every { it.startsWith('invalid --add-reads option:') })

        def noneForSelf = ModuleOptions.parseAddReads(['other.mod=java.sql'], 'demo.app', errors.&add)
        assertEquals(ModuleOptions.AddReads.EMPTY, noneForSelf)
    }

    @Test
    void testParseAddExports() {
        def errors = []
        assertEquals([:], ModuleOptions.parseAddExports(null, 'demo.app', errors.&add))
        assertEquals([:], ModuleOptions.parseAddExports([], 'demo.app', errors.&add))

        def parsed = ModuleOptions.parseAddExports([
            'java.base/sun.security.util=demo.app,other.mod',
            'java.base/jdk.internal.misc=demo.app',
            'java.sql/java.sql=other.mod',
            null,
            'no-equals',
            '/pkg=demo.app',
            'mod/=demo.app',
            'mod/pkg=',
            ' /pkg=demo.app',
            'mod/ =demo.app',
            'mod/pkg=demo.app,'
        ], 'demo.app', errors.&add)

        assertEquals(['java.base': ['sun.security.util', 'jdk.internal.misc'] as Set], parsed)
        assertEquals(8, errors.size())
        assertTrue(errors.every { it.startsWith('invalid --add-exports option:') })

        assertEquals([:], ModuleOptions.parseAddExports(['java.sql/java.sql=other.mod'], 'demo.app', errors.&add))
    }

    @Test
    void testModulePathFinderAndJmodHandling() {
        assertTrue(ModuleOptions.modulePathFinder(null).findAll().isEmpty())
        assertTrue(ModuleOptions.modulePathFinder([]).findAll().isEmpty())
        assertTrue(ModuleOptions.modulePathFinder([null, '', tempDir.resolve('missing').toString()]).findAll().isEmpty())

        // Valid JMOD with classes, META-INF entry, non-class resource, and concealed package (no ModulePackages attr)
        Path jmodDir = Files.createDirectory(tempDir.resolve('jmod-dir'))
        Path validJmod = jmodDir.resolve('demo.jmod')
        writeZip(validJmod, [
            'classes/'                                : null,
            'classes/module-info.class'               : minimalModuleInfo('demo.jmod', 'demo.jmod.api'),
            'classes/demo/jmod/api/Api.class'         : minimalClass('demo/jmod/api/Api'),
            'classes/demo/jmod/internal/Hidden.class' : minimalClass('demo/jmod/internal/Hidden'),
            'classes/META-INF/versions/21/V.class'    : minimalClass('V'),
            'classes/Root.class'                      : minimalClass('Root'),
            'classes/demo/jmod/api/readme.txt'        : 'hi'.bytes,
            'conf/app.properties'                     : 'k=v'.bytes
        ])
        // Non-module file sitting alongside JMOD in directory is ignored
        Files.writeString(jmodDir.resolve('README.txt'), 'ignore me')

        ModuleFinder singleJmodFinder = ModuleOptions.modulePathFinder([validJmod.toString()])
        def ref = singleJmodFinder.find('demo.jmod').orElseThrow()
        assertEquals('demo.jmod', ref.descriptor().name())
        assertEquals(['demo.jmod.api', 'demo.jmod.internal'] as Set, ref.descriptor().packages())
        assertTrue(singleJmodFinder.find('absent').isEmpty())
        assertEquals(1, singleJmodFinder.findAll().size())
        ref.open().withCloseable { reader ->
            assertTrue(reader.find('anything').isEmpty())
            assertEquals(0L, reader.list().count())
        }

        // Directory with JMOD + exploded module
        Path exploded = Files.createDirectories(jmodDir.resolve('demo.exp/demo/exp'))
        Files.write(jmodDir.resolve('demo.exp/module-info.class'), minimalModuleInfo('demo.exp', 'demo.exp'))
        Files.write(exploded.resolve('E.class'), minimalClass('demo/exp/E'))
        ModuleFinder mixedFinder = ModuleOptions.modulePathFinder([jmodDir.toString()])
        assertTrue(mixedFinder.find('demo.jmod').isPresent())
        assertTrue(mixedFinder.find('demo.exp').isPresent())

        // Duplicate module name inside the same directory throws FindException
        Path dupDir = Files.createDirectory(tempDir.resolve('dup-dir'))
        writeZip(dupDir.resolve('a.jmod'), ['classes/module-info.class': minimalModuleInfo('dup.mod', null)])
        writeZip(dupDir.resolve('b.jmod'), ['classes/module-info.class': minimalModuleInfo('dup.mod', null)])
        assertThrows(FindException) {
            ModuleOptions.modulePathFinder([dupDir.toString()]).findAll()
        }

        // JMOD missing classes/module-info.class throws FindException
        Path missingDesc = tempDir.resolve('missing-desc.jmod')
        writeZip(missingDesc, ['classes/demo/X.class': minimalClass('demo/X')])
        assertThrows(FindException) {
            ModuleOptions.modulePathFinder([missingDesc.toString()]).findAll()
        }

        // Corrupt JMOD throws FindException
        Path corruptJmod = tempDir.resolve('corrupt.jmod')
        Files.write(corruptJmod, 'not-a-zip'.bytes)
        assertThrows(FindException) {
            ModuleOptions.modulePathFinder([corruptJmod.toString()]).findAll()
        }

        // Multiple separate module-path entries compose in first-wins order
        Path secondEntry = tempDir.resolve('second.jmod')
        writeZip(secondEntry, ['classes/module-info.class': minimalModuleInfo('demo.jmod', 'demo.jmod.v2')])
        ModuleFinder composed = ModuleOptions.modulePathFinder([validJmod.toString(), secondEntry.toString()])
        assertEquals(['demo.jmod.api'], composed.find('demo.jmod').orElseThrow().descriptor().exports()*.source())

        // moduleClassLoaderUrls: null/empty fast path, valid JMOD + exploded URLs, broken entry tolerated
        assertEquals([], ModuleOptions.moduleClassLoaderUrls(null))
        assertEquals([], ModuleOptions.moduleClassLoaderUrls(new CompilerConfiguration()))
        def cfg = new CompilerConfiguration()
        cfg.setUpgradeModulePathList([validJmod.toString()])
        cfg.setModulePathList([jmodDir.toString()])
        def urls = ModuleOptions.moduleClassLoaderUrls(cfg)
        assertTrue(urls.any { it.toString().startsWith('jar:') && it.toString().endsWith('.jmod!/classes/') })
        assertTrue(urls.any { it.protocol == 'file' })

        def brokenCfg = new CompilerConfiguration()
        brokenCfg.setModulePathList([corruptJmod.toString()])
        assertEquals([], ModuleOptions.moduleClassLoaderUrls(brokenCfg))

        // Invalid URI scheme returns null cleanly from toClassLoaderUrl
        assertNull(ModuleOptions.toClassLoaderUrl(URI.create('nosuchscheme:foo')))

        // Unreadable directory falls back to ModuleFinder.of(dir)
        Path unreadableDir = Files.createDirectory(tempDir.resolve('unreadable-dir'))
        try {
            if (unreadableDir.toFile().setReadable(false)) {
                // Only assert fallback path when OS actually enforced non-readable permission (non-root)
                if (!Files.isReadable(unreadableDir)) {
                    assertThrows(FindException) {
                        ModuleOptions.modulePathFinder([unreadableDir.toString()]).findAll()
                    }
                }
            }
        } finally {
            unreadableDir.toFile().setReadable(true)
        }
    }

    @Test
    void testObserveModulesDedupeAndLimitModules() {
        // Null source still returns system modules with empty boundedPackages
        def obs = ModuleOptions.observeModules(null, null, null, null)
        assertTrue(obs.finder().find('java.base').isPresent())
        assertTrue(obs.boundedPackages().isEmpty())

        // --limit-modules with findAll(), duplicate transitive seed, unknown seed, empty addModules, and ALL-MODULE-PATH
        def limCfg = new CompilerConfiguration()
        limCfg.setLimitModules(['java.sql', 'no.such.mod'])
        limCfg.setAddModules(['java.xml', '', ModuleOptions.ALL_MODULE_PATH])
        def limSrc = new SourceUnit('Test.groovy', 'class Test {}', limCfg, new GroovyClassLoader(), new ErrorCollector(limCfg))
        def limObs = ModuleOptions.observeModules(limSrc, null, null, ModuleFinder.of())
        def limitedNames = limObs.finder().findAll()*.descriptor()*.name() as Set
        assertTrue(limitedNames.contains('java.base'))
        assertTrue(limitedNames.contains('java.sql'))
        assertTrue(limitedNames.contains('java.xml'))
        assertTrue(limitedNames.contains('java.logging'))
        assertFalse(limitedNames.contains('java.compiler'))

        // Broken module-path entry tolerated during collectBoundedPackages, addAllModuleNames, and findQuietly
        Path badJmod = tempDir.resolve('bad-observe.jmod')
        Files.write(badJmod, 'not-a-zip'.bytes)
        def badCfg = new CompilerConfiguration()
        badCfg.setModulePathList([badJmod.toString()])
        badCfg.setLimitModules(['java.base', 'missing.from.bad'])
        badCfg.setAddModules([ModuleOptions.ALL_MODULE_PATH])
        def badSrc = new SourceUnit('Bad.groovy', 'class Bad {}', badCfg, new GroovyClassLoader(), new ErrorCollector(badCfg))
        def throwingCp = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { throw new FindException('boom') }
            @Override Set<ModuleReference> findAll() { throw new FindException('boom') }
        }
        def badObs = ModuleOptions.observeModules(badSrc, null, 'demo.bad', throwingCp)
        assertTrue(badObs.boundedPackages().isEmpty())
        assertTrue(badObs.finder().find('java.base').isPresent())

        // Dedupe preserves first occurrence in findAll()
        def m1 = ModuleDescriptor.newModule('dup.sys').exports('p.one').build()
        def m2 = ModuleDescriptor.newModule('dup.sys').exports('p.two').build()
        def ref1 = descriptorRef(m1)
        def ref2 = descriptorRef(m2)
        def raw = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { 'dup.sys' == name ? Optional.of(ref1) : Optional.empty() }
            @Override Set<ModuleReference> findAll() { new LinkedHashSet<>([ref1, ref2]) }
        }
        def deduped = ModuleOptions.dedupe(raw)
        assertEquals(1, deduped.findAll().size())
        assertEquals(['p.one'], deduped.find('dup.sys').orElseThrow().descriptor().exports()*.source())
    }

    @Test
    void testDefaultSystemRootsAllSystemAllDefaultAndUnnamedReadability() {
        Set<String> roots = ModuleOptions.defaultSystemRootModules()
        assertTrue(roots.contains('java.base'))
        assertTrue(roots.contains('java.sql'))
        assertFalse(roots.contains('java.se'))
        assertTrue(roots.every { !it.startsWith('jdk.incubator.') })

        // --limit-modules java.base with --add-modules ALL-DEFAULT and ALL-SYSTEM
        def defCfg = new CompilerConfiguration()
        defCfg.setLimitModules(['java.base'])
        defCfg.setAddModules([ModuleOptions.ALL_DEFAULT])
        def defSrc = new SourceUnit('Def.groovy', 'class Def {}', defCfg, new GroovyClassLoader(), new ErrorCollector(defCfg))
        def defObs = ModuleOptions.observeModules(defSrc, null, null, ModuleFinder.of())
        assertTrue(defObs.finder().find('java.sql').isPresent())
        assertFalse(defObs.finder().find('java.se').isPresent())

        def sysCfg = new CompilerConfiguration()
        sysCfg.setLimitModules(['java.base'])
        sysCfg.setAddModules([ModuleOptions.ALL_SYSTEM])
        def sysSrc = new SourceUnit('Sys.groovy', 'class Sys {}', sysCfg, new GroovyClassLoader(), new ErrorCollector(sysCfg))
        def sysObs = ModuleOptions.observeModules(sysSrc, null, null, ModuleFinder.of())
        assertTrue(sysObs.finder().find('java.se').isPresent())

        // isReadableByUnnamedModule edge cases
        assertFalse(ModuleOptions.isReadableByUnnamedModule(null, ModuleFinder.ofSystem(), null))
        assertFalse(ModuleOptions.isReadableByUnnamedModule('', ModuleFinder.ofSystem(), null))
        assertTrue(ModuleOptions.isReadableByUnnamedModule('demo.custom.app', ModuleFinder.ofSystem(), null))
        assertTrue(ModuleOptions.isReadableByUnnamedModule('java.base', ModuleFinder.ofSystem(), null))
        assertTrue(ModuleOptions.isReadableByUnnamedModule('java.sql', ModuleFinder.ofSystem(), null))
        assertFalse(ModuleOptions.isReadableByUnnamedModule('java.se', ModuleFinder.ofSystem(), null))
        assertFalse(ModuleOptions.isReadableByUnnamedModule('java.se', ModuleFinder.ofSystem(), new CompilerConfiguration()))

        def onlyDefaultCfg = new CompilerConfiguration()
        onlyDefaultCfg.setAddModules([ModuleOptions.ALL_DEFAULT, '', null])
        assertFalse(ModuleOptions.isReadableByUnnamedModule('java.se', ModuleFinder.ofSystem(), onlyDefaultCfg))

        def allSysCfg = new CompilerConfiguration()
        allSysCfg.setAddModules([ModuleOptions.ALL_SYSTEM])
        assertTrue(ModuleOptions.isReadableByUnnamedModule('java.se', ModuleFinder.ofSystem(), allSysCfg))

        def explicitSeCfg = new CompilerConfiguration()
        explicitSeCfg.setAddModules(['java.se'])
        assertTrue(ModuleOptions.isReadableByUnnamedModule('java.se', null, explicitSeCfg))

        // ALL-MODULE-PATH seeds non-system modules whose requires pull in non-default system modules
        def appRequiresSe = ModuleDescriptor.newModule('demo.needs.se').requires('java.se').build()
        def appRef = descriptorRef(appRequiresSe)
        def combinedFinder = ModuleFinder.compose(new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { 'demo.needs.se' == name ? Optional.of(appRef) : Optional.empty() }
            @Override Set<ModuleReference> findAll() { Set.of(appRef) }
        }, ModuleFinder.ofSystem())
        def allMpCfg = new CompilerConfiguration()
        allMpCfg.setAddModules([ModuleOptions.ALL_MODULE_PATH])
        assertTrue(ModuleOptions.isReadableByUnnamedModule('java.se', combinedFinder, allMpCfg))

        // Broken finder during ALL-MODULE-PATH scan fails safely without throwing
        def brokenFinder = new ModuleFinder() {
            @Override Optional<ModuleReference> find(String name) { throw new FindException('boom') }
            @Override Set<ModuleReference> findAll() { throw new FindException('boom') }
        }
        assertFalse(ModuleOptions.isReadableByUnnamedModule('java.se', brokenFinder, allMpCfg))
    }

    private static ModuleReference descriptorRef(ModuleDescriptor md) {
        new ModuleReference(md, null) {
            @Override ModuleReader open() { throw new UnsupportedOperationException() }
        }
    }

    private static void writeZip(Path zipPath, Map<String, byte[]> entries) {
        new ZipOutputStream(Files.newOutputStream(zipPath)).withCloseable { zos ->
            entries.each { name, bytes ->
                zos.putNextEntry(new ZipEntry(name))
                if (bytes != null) {
                    zos.write(bytes)
                }
                zos.closeEntry()
            }
        }
    }

    private static byte[] minimalModuleInfo(String name, String exportedPkg) {
        def cw = new ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_MODULE, 'module-info', null, null, null)
        def mv = cw.visitModule(name, 0, null)
        mv.visitRequire('java.base', Opcodes.ACC_MANDATED, null)
        if (exportedPkg != null) {
            mv.visitExport(exportedPkg.replace('.', '/'), 0, (String[]) null)
        }
        mv.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static byte[] minimalClass(String internalName) {
        def cw = new ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        cw.visitEnd()
        cw.toByteArray()
    }
}
