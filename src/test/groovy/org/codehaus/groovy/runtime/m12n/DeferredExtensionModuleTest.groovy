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
package org.codehaus.groovy.runtime.m12n

import groovy.grape.GrapeUtil
import groovy.junit6.plugin.ForkedJvm
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static groovy.test.GroovyAssert.shouldFail

/**
 * An extension module whose extension classes reference an optional dependency
 * can name a class of that dependency in {@code requiredClasses}. While the
 * class is missing the module is deferred rather than loaded (or failed), and
 * it is registered once the class becomes available, as after {@code @Grab}.
 * <p>
 * Forked because the deferred module ends up in the shared metaclass registry.
 */
@ForkedJvm
final class DeferredExtensionModuleTest {

    @TempDir
    File tempDir

    @Test
    void testModuleIsDeferredUntilRequiredClassIsAvailable() {
        File moduleDir = new File(tempDir, 'module')
        compileTo(moduleDir, 'Ext.groovy', '''
            package deferred
            class Ext {
                static String deferredShout(String self) { self.toUpperCase() + '!' }
            }
        ''')
        File descriptor = new File(moduleDir, ExtensionModuleScanner.MODULE_META_INF_FILE)
        descriptor.parentFile.mkdirs()
        descriptor.text = '''\
            moduleName=deferred-test
            moduleVersion=1.0
            extensionClasses=deferred.Ext
            requiredClasses=deferred.Marker
        '''.stripIndent()
        File markerDir = new File(tempDir, 'marker')
        compileTo(markerDir, 'Marker.groovy', 'package deferred; class Marker {}')

        def loader = new GroovyClassLoader(getClass().classLoader)
        loader.addURL(moduleDir.toURI().toURL())
        def registry = (MetaClassRegistryImpl) GroovySystem.metaClassRegistry
        def props = new Properties()
        descriptor.withInputStream { props.load(it) }

        // the registry scan (startup, or a grab of the module's jar) finds Marker missing and defers the module
        registry.registerExtensionModuleFromProperties(props, loader, [:])
        assert !registry.moduleRegistry.hasModule('deferred-test')
        assert ExtensionModuleScanner.getDeferredModuleNames(loader) == ['deferred-test'] as Set

        def shell = new GroovyShell(loader)
        shouldFail(MissingMethodException) {
            shell.evaluate('"x".deferredShout()')
        }
        def err = shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate('@groovy.transform.CompileStatic def m() { "x".deferredShout() }; m()')
        }
        assert err.message.contains('Cannot find matching method java.lang.String#deferredShout()')

        // Marker becomes available, as after @Grab, and Grape revisits the deferred modules
        loader.addURL(markerDir.toURI().toURL())
        GrapeUtil.processDeferredExtensionModules()

        assert registry.moduleRegistry.hasModule('deferred-test')
        assert ExtensionModuleScanner.getDeferredModuleNames(loader).isEmpty()
        assert shell.evaluate('"x".deferredShout()') == 'X!'
        assert shell.evaluate('@groovy.transform.CompileStatic def m() { "x".deferredShout() }; m()') == 'X!'
    }

    @Test
    void testModuleStaysDeferredWhenRequiredClassIsVisibleOnlyToChildLoader() {
        // the module jar sits in a parent loader (lib/ of a Groovy install) and a plain @Grab adds the
        // dependency to the script's child loader: the extension classes still cannot link, so the
        // module must stay deferred rather than register broken
        File moduleDir = new File(tempDir, 'module')
        compileTo(moduleDir, 'Ext.groovy', '''
            package parented
            class Ext {
                static String parentedShout(String self) { self.toUpperCase() + '#' }
            }
        ''')
        File descriptor = new File(moduleDir, ExtensionModuleScanner.MODULE_META_INF_FILE)
        descriptor.parentFile.mkdirs()
        descriptor.text = '''\
            moduleName=parented-test
            moduleVersion=1.0
            extensionClasses=parented.Ext
            requiredClasses=parented.Marker
        '''.stripIndent()
        File markerDir = new File(tempDir, 'marker')
        compileTo(markerDir, 'Marker.groovy', 'package parented; class Marker {}')

        def parent = new GroovyClassLoader(getClass().classLoader)
        parent.addURL(moduleDir.toURI().toURL())
        def child = new GroovyClassLoader(parent)
        def registry = (MetaClassRegistryImpl) GroovySystem.metaClassRegistry
        def props = new Properties()
        descriptor.withInputStream { props.load(it) }

        registry.registerExtensionModuleFromProperties(props, child, [:])
        assert ExtensionModuleScanner.getDeferredModuleNames(child) == ['parented-test'] as Set

        child.addURL(markerDir.toURI().toURL())
        GrapeUtil.processDeferredExtensionModules()

        assert !registry.moduleRegistry.hasModule('parented-test')
        assert ExtensionModuleScanner.getDeferredModuleNames(child) == ['parented-test'] as Set

        // once the parent can see it too, the module loads
        parent.addURL(markerDir.toURI().toURL())
        GrapeUtil.processDeferredExtensionModules()

        assert registry.moduleRegistry.hasModule('parented-test')
        assert ExtensionModuleScanner.getDeferredModuleNames(child).isEmpty()
        assert new GroovyShell(child).evaluate('@groovy.transform.CompileStatic def m() { "x".parentedShout() }; m()') == 'X#'
    }

    @Test
    void testModuleWithRequiredClassPresentLoadsNormally() {
        File moduleDir = new File(tempDir, 'module')
        compileTo(moduleDir, 'Ext.groovy', '''
            package present
            class Ext {
                static String presentShout(String self) { self.toUpperCase() + '?' }
            }
        ''')
        File descriptor = new File(moduleDir, ExtensionModuleScanner.MODULE_META_INF_FILE)
        descriptor.parentFile.mkdirs()
        descriptor.text = '''\
            moduleName=present-test
            moduleVersion=1.0
            extensionClasses=present.Ext
            requiredClasses=java.util.List, java.util.Map
        '''.stripIndent()

        def loader = new GroovyClassLoader(getClass().classLoader)
        loader.addURL(moduleDir.toURI().toURL())
        def registry = (MetaClassRegistryImpl) GroovySystem.metaClassRegistry
        def props = new Properties()
        descriptor.withInputStream { props.load(it) }

        registry.registerExtensionModuleFromProperties(props, loader, [:])
        assert registry.moduleRegistry.hasModule('present-test')
        assert ExtensionModuleScanner.getDeferredModuleNames(loader).isEmpty()
        assert new GroovyShell(loader).evaluate('@groovy.transform.CompileStatic def m() { "x".presentShout() }; m()') == 'X?'
    }

    @Test
    void testExtensionClassWithMissingSignatureTypeDoesNotBreakTypeChecking() {
        // the extension class compiles against a type that is then not on the class path and the
        // module does not declare requiredClasses: the class cannot be scanned, but other code must
        // still compile (the type checker and macro caches scan every module visible to the loader)
        File depDir = new File(tempDir, 'dep')
        compileTo(depDir, 'Binding.groovy', 'package optional; class Binding {}')
        File moduleDir = new File(tempDir, 'module')
        compileTo(moduleDir, 'Ext.groovy', '''
            package fragile
            class Ext {
                static optional.Binding bind(String self) { null }
            }
        ''', depDir)
        File descriptor = new File(moduleDir, ExtensionModuleScanner.MODULE_META_INF_FILE)
        descriptor.parentFile.mkdirs()
        descriptor.text = '''\
            moduleName=fragile-test
            moduleVersion=1.0
            extensionClasses=fragile.Ext
        '''.stripIndent()

        def loader = new GroovyClassLoader(getClass().classLoader)
        loader.addURL(moduleDir.toURI().toURL())

        assert new GroovyShell(loader).evaluate('@groovy.transform.CompileStatic def m() { "x".toUpperCase() }; m()') == 'X'
    }

    private static void compileTo(File targetDir, String fileName, String source, File... classpath) {
        def loader = new GroovyClassLoader(DeferredExtensionModuleTest.classLoader)
        classpath.each { loader.addURL(it.toURI().toURL()) }
        def config = new CompilerConfiguration()
        config.targetDirectory = targetDir
        def unit = new CompilationUnit(config, null, loader)
        unit.addSource(fileName, source)
        unit.compile()
    }
}
