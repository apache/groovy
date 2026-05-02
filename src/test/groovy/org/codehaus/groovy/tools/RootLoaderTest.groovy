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
package org.codehaus.groovy.tools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class RootLoaderTest {

    private static final String RESOURCE = 'META-INF/services/groovy.test.RootLoaderProbe'

    // GROOVY-11978: when the same jar is on both the launcher's startup classpath
    // and listed in groovy-starter.conf's "load lib/*.jar", getResources() must
    // not return the same provider URL twice.
    @Test
    void testGetResourcesDedupsSameJarOnParentAndChild(@TempDir Path tempDir) {
        def jar = createJar(tempDir, 'sample.jar', 'a')
        def url = jar.toURI().toURL()

        def parent = new URLClassLoader([url] as URL[], null)
        def loader = new RootLoader([url] as URL[], parent)

        def found = Collections.list(loader.getResources(RESOURCE))
        assert found.size() == 1
        assert found[0].toString().endsWith("sample.jar!/$RESOURCE" as String)
    }

    @Test
    void testGetResourcesKeepsDistinctJars(@TempDir Path tempDir) {
        def parentJar = createJar(tempDir, 'parent.jar', 'a')
        def childJar  = createJar(tempDir, 'child.jar', 'b')

        def parent = new URLClassLoader([parentJar.toURI().toURL()] as URL[], null)
        def loader = new RootLoader([childJar.toURI().toURL()] as URL[], parent)

        def found = Collections.list(loader.getResources(RESOURCE))
        assert found.size() == 2
    }

    @Test
    void testGetResourcesDedupsSymlinkedJar(@TempDir Path tempDir) {
        def real = createJar(tempDir, 'real.jar', 'a')
        def link = tempDir.resolve('alias.jar')
        try {
            Files.createSymbolicLink(link, real.toPath())
        } catch (UnsupportedOperationException | IOException ignore) {
            return // platform doesn't support symlinks
        }

        def parent = new URLClassLoader([real.toURI().toURL()] as URL[], null)
        def loader = new RootLoader([link.toUri().toURL()] as URL[], parent)

        def found = Collections.list(loader.getResources(RESOURCE))
        assert found.size() == 1
    }

    @Test
    void testGetResourcesPreservesDistinctDirectoryUrls(@TempDir Path tempDir) {
        def dir1 = createDirWithResource(tempDir, 'classes1', 'a')
        def dir2 = createDirWithResource(tempDir, 'classes2', 'b')

        def parent = new URLClassLoader([dir1.toUri().toURL()] as URL[], null)
        def loader = new RootLoader([dir2.toUri().toURL()] as URL[], parent)

        def found = Collections.list(loader.getResources(RESOURCE))
        assert found.size() == 2
    }

    @Test
    void testGetResourcesChildBeforeParent(@TempDir Path tempDir) {
        def parentJar = createJar(tempDir, 'parent.jar', 'a')
        def childJar  = createJar(tempDir, 'child.jar', 'b')

        def parent = new URLClassLoader([parentJar.toURI().toURL()] as URL[], null)
        def loader = new RootLoader([childJar.toURI().toURL()] as URL[], parent)

        def found = Collections.list(loader.getResources(RESOURCE))
        assert found.size() == 2
        assert found[0].toString().contains('child.jar')
        assert found[1].toString().contains('parent.jar')
    }

    private static File createJar(Path dir, String name, String content) {
        def file = dir.resolve(name).toFile()
        new JarOutputStream(new FileOutputStream(file), new Manifest()).withCloseable { jos ->
            jos.putNextEntry(new JarEntry(RESOURCE))
            jos.write(content.bytes)
            jos.closeEntry()
        }
        return file
    }

    private static Path createDirWithResource(Path parent, String name, String content) {
        def dir = Files.createDirectory(parent.resolve(name))
        def res = Files.createDirectories(dir.resolve('META-INF').resolve('services')).resolve('groovy.test.RootLoaderProbe')
        Files.writeString(res, content)
        return dir
    }
}
