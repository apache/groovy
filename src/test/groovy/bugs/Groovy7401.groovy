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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * GROOVY-7401: a GroovyClassLoader classpath that goes through a
 * symbolic-link directory used not to reload an edited script, because
 * {@code recompile} evicted the source cache by the symlink URL while
 * {@code parseClass} looked up the canonical path.
 */
final class Groovy7401 {

    @TempDir
    Path tempDir

    @Test
    void testReloadWhenClasspathGoesThroughSymlink() {
        def realRoot = tempDir.resolve('bar').toFile()
        def linkRoot = tempDir.resolve('foo').toFile()
        def groovyDir = new File(realRoot, 'groovy')
        def pkgDir = new File(groovyDir, 'pkg')
        assert pkgDir.mkdirs()

        try {
            Files.createSymbolicLink(linkRoot.toPath(), realRoot.toPath())
        } catch (UnsupportedOperationException | IOException ignore) {
            assumeTrue(false, 'symbolic links are not supported on this platform')
        }

        def src = new File(pkgDir, 'CoolClass.groovy')
        src.text = '''
            package pkg
            class CoolClass {
                String value() { 'v1' }
            }
        '''

        def loader = new GroovyClassLoader()
        loader.setShouldRecompile(true)
        loader.addClasspath(new File(linkRoot, 'groovy').absolutePath)

        Class c1 = loader.loadClass('pkg.CoolClass', true, false)
        assert c1.newInstance().value() == 'v1'

        Thread.sleep(1100)
        src.text = '''
            package pkg
            class CoolClass {
                String value() { 'v2' }
            }
        '''

        Class c2 = loader.loadClass('pkg.CoolClass', true, false)
        assert !c1.is(c2)
        assert c2.newInstance().value() == 'v2'
    }
}
