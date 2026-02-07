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

import groovy.test.GroovyTestCase

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class LoaderConfigurationTest extends GroovyTestCase {

    void testComment() {
        def txt = "# I am a comment"

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 0
    }

    void testNormalPath() {
        // generate a load instruction with a valid path
        def file = new File(".")
        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 1
        assert config.classPathUrls[0].sameFile(file.toURI().toURL())
    }

    void testNonExistingPath() {
        // generate a load instruction with a non-existing path
        def file = getNonExistingFile(new File("."))

        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 0
    }

    void testExistingProperty() {
        def txt = 'load ${java.home}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 1
        def url1 = config.classPathUrls[0]
        def url2 = new File(System.getProperty("java.home")).toURI().toURL()
        assert url1.sameFile(url2)
    }

    void testPropertyDefinition() {
        System.setProperty('myprop', 'baz')
        def txt = 'property foo1=bar\nproperty foo2=${myprop}\nproperty foo3=!{myprop}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)
        assert System.getProperty('foo1') == 'bar'
        assert System.getProperty('foo2') == 'baz'
        assert System.getProperty('foo3') == 'baz'
    }

    void testNonExistingProperty() {
        String name = getNonExistingPropertyName("foo")

        def txt = 'load !{' + name + '}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        shouldFail {
            configFromString(config, txt)
        }

        txt = 'load ${' + name + '}'

        config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 0
    }

    void testSlashCorrection() {
        def prop = getNonExistingPropertyName("nope")
        System.setProperty(prop,'/')

        def txt = "load \${$prop}/"

        def config = new LoaderConfiguration()
        config.requireMain = false
        configFromString(config, txt)

        assert config.classPathUrls.length == 1
        def url = config.classPathUrls[0]
        assert !url.path.endsWith("//")
        System.setProperty(prop, "")
    }

    private static configFromString(LoaderConfiguration config, String txt) {
        config.configure(new ByteArrayInputStream(txt.bytes))
    }

    private static getNonExistingPropertyName(String base) {
        while (System.getProperty(base) != null) {
            base += "x"
        }
        return base
    }

    private static File getNonExistingFile(File base) {
        def number = "0"
        while (base.exists()) {
            base = new File(base, number)
            number++
        }
        return base
    }

    // --- Merged from LoaderConfigurationJUnit5Test ---

    void testNewConfigurationHasNoClassPath() {
        def config = new LoaderConfiguration()
        assert config.getClassPathUrls().length == 0
    }

    void testNewConfigurationHasNoMainClass() {
        def config = new LoaderConfiguration()
        assert config.getMainClass() == null
    }

    void testNewConfigurationHasNoGrabUrls() {
        def config = new LoaderConfiguration()
        assert config.getGrabUrls().isEmpty()
    }

    void testSetMainClass() {
        def config = new LoaderConfiguration()
        config.setMainClass("com.example.Main")
        assert "com.example.Main" == config.getMainClass()
    }

    void testSetMainClassDisablesRequireMain() {
        def config = new LoaderConfiguration()
        config.setMainClass("com.example.Main")
        def configContent = "# just a comment\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        assert "com.example.Main" == config.getMainClass()
    }

    void testSetRequireMainFalse() {
        def config = new LoaderConfiguration()
        config.setRequireMain(false)
        def configContent = "# just a comment\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        assert config.getMainClass() == null
    }

    void testRequireMainTrueThrowsWithoutMain() {
        def config = new LoaderConfiguration()
        def configContent = "# just a comment\n"
        shouldFail(IOException) {
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        }
    }

    void testConfigureWithMainIs() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        assert "com.example.Main" == config.getMainClass()
    }

    void testConfigureDuplicateMainThrows() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\nmain is com.example.Other\n"
        shouldFail(IOException) {
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        }
    }

    void testConfigureIgnoresComments() {
        def config = new LoaderConfiguration()
        def configContent = "# this is a comment\nmain is com.example.Main\n# another comment\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        assert "com.example.Main" == config.getMainClass()
    }

    void testConfigureIgnoresEmptyLines() {
        def config = new LoaderConfiguration()
        def configContent = "\n\nmain is com.example.Main\n\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        assert "com.example.Main" == config.getMainClass()
    }

    void testConfigureWithLoadExistingFile() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def jarFile = new File(tempDir, "test.jar")
            jarFile.createNewFile()

            def config = new LoaderConfiguration()
            def configContent = "main is com.example.Main\nload " + jarFile.getAbsolutePath() + "\n"
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

            def urls = config.getClassPathUrls()
            assert 1 == urls.length
            assert urls[0].toString().contains("test.jar")
        } finally {
            tempDir.deleteDir()
        }
    }

    void testConfigureWithLoadNonExistingFile() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\nload /non/existent/path.jar\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

        assert 0 == config.getClassPathUrls().length
    }

    void testConfigureWithGrab() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\ngrab org.example:lib:1.0\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

        assert 1 == config.getGrabUrls().size()
        assert "org.example:lib:1.0" == config.getGrabUrls().get(0)
    }

    void testConfigureWithInvalidLineThrows() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\ninvalid line here\n"
        shouldFail(IOException) {
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        }
    }

    void testAddFileWithExistingFile() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def jarFile = new File(tempDir, "test.jar")
            jarFile.createNewFile()

            def config = new LoaderConfiguration()
            config.addFile(jarFile)

            def urls = config.getClassPathUrls()
            assert 1 == urls.length
        } finally {
            tempDir.deleteDir()
        }
    }

    void testAddFileWithNonExistingFile() {
        def config = new LoaderConfiguration()
        config.addFile(new File("/non/existent/file.jar"))
        assert 0 == config.getClassPathUrls().length
    }

    void testAddFileWithNullFile() {
        def config = new LoaderConfiguration()
        config.addFile((File) null)
        assert 0 == config.getClassPathUrls().length
    }

    void testAddFileWithFilename() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def jarFile = new File(tempDir, "test.jar")
            jarFile.createNewFile()

            def config = new LoaderConfiguration()
            config.addFile(jarFile.getAbsolutePath())

            def urls = config.getClassPathUrls()
            assert 1 == urls.length
        } finally {
            tempDir.deleteDir()
        }
    }

    void testAddFileWithNullFilename() {
        def config = new LoaderConfiguration()
        config.addFile((String) null)
        assert 0 == config.getClassPathUrls().length
    }

    void testAddClassPath() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def jarFile1 = new File(tempDir, "test1.jar")
            def jarFile2 = new File(tempDir, "test2.jar")
            jarFile1.createNewFile()
            jarFile2.createNewFile()

            def config = new LoaderConfiguration()
            def classpath = jarFile1.getAbsolutePath() + File.pathSeparator + jarFile2.getAbsolutePath()
            config.addClassPath(classpath)

            def urls = config.getClassPathUrls()
            assert 2 == urls.length
        } finally {
            tempDir.deleteDir()
        }
    }

    void testAddClassPathWithWildcard() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def dir = new File(tempDir, "lib")
            dir.mkdirs()
            new File(dir, "a.jar").createNewFile()
            new File(dir, "b.jar").createNewFile()
            new File(dir, "not-a-jar.txt").createNewFile()

            def config = new LoaderConfiguration()
            config.addClassPath(dir.getAbsolutePath() + "/*")

            def urls = config.getClassPathUrls()
            assert 2 == urls.length // only .jar files
        } finally {
            tempDir.deleteDir()
        }
    }

    void testConfigureWithPropertyExpansion() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def jarFile = new File(tempDir, "test.jar")
            jarFile.createNewFile()

            System.setProperty("test.loader.path", tempDir.getAbsolutePath())

            def config = new LoaderConfiguration()
            def configContent = "main is com.example.Main\nload \${test.loader.path}/test.jar\n"
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

            def urls = config.getClassPathUrls()
            assert 1 == urls.length
        } finally {
            System.clearProperty("test.loader.path")
            tempDir.deleteDir()
        }
    }

    void testConfigureWithMissingOptionalProperty() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\nload \${nonexistent.property}/test.jar\n"
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

        assert 0 == config.getClassPathUrls().length
    }

    void testConfigureWithMissingRequiredPropertyThrows() {
        def config = new LoaderConfiguration()
        def configContent = "main is com.example.Main\nload !{nonexistent.required.property}/test.jar\n"
        shouldFail(IllegalArgumentException) {
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))
        }
    }

    void testConfigureWithWildcardLoad() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def dir = new File(tempDir, "libs")
            dir.mkdirs()
            new File(dir, "a.jar").createNewFile()
            new File(dir, "b.jar").createNewFile()

            def config = new LoaderConfiguration()
            def configContent = "main is com.example.Main\nload " + dir.getAbsolutePath() + "/*.jar\n"
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

            def urls = config.getClassPathUrls()
            assert 2 == urls.length
        } finally {
            tempDir.deleteDir()
        }
    }

    void testConfigureWithRecursiveWildcard() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def dir = new File(tempDir, "libs")
            def subdir = new File(dir, "subdir")
            subdir.mkdirs()
            new File(dir, "a.jar").createNewFile()
            new File(subdir, "b.jar").createNewFile()

            def config = new LoaderConfiguration()
            def configContent = "main is com.example.Main\nload " + dir.getAbsolutePath() + "/**/*.jar\n"
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)))

            def urls = config.getClassPathUrls()
            assert 1 == urls.length // Only b.jar in subdir matches
        } finally {
            tempDir.deleteDir()
        }
    }

    void testAddMultipleFiles() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def config = new LoaderConfiguration()
            for (int i = 0; i < 5; i++) {
                def jarFile = new File(tempDir, "test" + i + ".jar")
                jarFile.createNewFile()
                config.addFile(jarFile)
            }

            assert 5 == config.getClassPathUrls().length
        } finally {
            tempDir.deleteDir()
        }
    }

    void testAddDirectory() {
        def tempDir = Files.createTempDirectory("loaderConfigTest").toFile()
        try {
            def config = new LoaderConfiguration()
            config.addFile(tempDir)

            assert 1 == config.getClassPathUrls().length
        } finally {
            tempDir.deleteDir()
        }
    }
}