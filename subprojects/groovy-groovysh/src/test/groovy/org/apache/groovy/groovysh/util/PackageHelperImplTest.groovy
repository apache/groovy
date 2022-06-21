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
package org.apache.groovy.groovysh.util

import groovy.test.GroovyTestCase

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * Unit tests for the {@link PackageHelperImpl} class.
 */
class PackageHelperImplTest extends GroovyTestCase {

    void testLoadAndGetPackagesEmpty() {
        PackageHelperImpl helper = new PackageHelperImpl(null)
        Set<String> rootPackages = helper.getContents('')
        assertNotNull(rootPackages)
        assert rootPackages.contains('java')
        assert rootPackages.contains('javax')
        assert rootPackages.contains('groovy')
    }

    void testLoadAndGetPackagesJava() {
        PackageHelperImpl helper = new PackageHelperImpl(null)
        Set<String> names = helper.getContents('java')
        assertNotNull(names)
        assert names.contains('io')
    }

    void testLoadAndGetPackagesJavaUtil() {
        PackageHelperImpl helper = new PackageHelperImpl(null)
        Set<String> names = helper.getContents('java.util')
        assertNotNull(names)
        assert names.contains('zip')
        assert names.contains('Set')
    }

    void testLoadAndGetPackagesInvalid() {
        PackageHelperImpl helper = new PackageHelperImpl(null)
        assert [] as Set<String> == helper.getContents('invalid:name')
    }

    void testLoadAndGetPackagesUnknown() {
        PackageHelperImpl helper = new PackageHelperImpl(null)
        assert [] as Set<String> == helper.getContents('java.util.regex.tools')
    }

    void testGetPackageNamesFromPathWithSign() {
        Path tempTestDirPath = Files.createTempDirectory(this.getClass().getName())
        Path folderWithSign = tempTestDirPath.resolve('dummy+folder++1%23%%')

        Path dummyFilePath = folderWithSign.resolve('dummypackage1').resolve('Dummy.class')
        Files.createDirectories(dummyFilePath.getParent())
        File dummyFile = dummyFilePath.toFile()
        try (FileOutputStream fos = new FileOutputStream(dummyFile)) {
            fos.write(0)
        }
        assert dummyFile.exists()

        Path jarWithSignPath = folderWithSign.resolve("dummy+lib++1%23%%.jar")
        try (FileOutputStream fos = new FileOutputStream(jarWithSignPath.toFile())
             JarOutputStream jos = new JarOutputStream(fos, new Manifest())) {
            JarEntry jarEntry = new JarEntry("dummypackage2/Dummy.class")
            jos.putNextEntry(jarEntry)
            jos.write(0)
        }
        assert jarWithSignPath.toFile().exists()

        PackageHelperImpl helper = new PackageHelperImpl(null)

        Set<String> packageInFolder = helper.getPackageNames(folderWithSign.toUri().toURL())
        assert packageInFolder.contains("dummypackage1")

        Set<String> packageInJar = helper.getPackageNames(jarWithSignPath.toUri().toURL())
        assert packageInJar.contains("dummypackage2")
    }

    void testGetPackageNamesFromPathWithSpace() {
        Path tempTestDirPath = Files.createTempDirectory(this.getClass().getName())
        Path folderWithSpacePath = Files.createTempDirectory(tempTestDirPath, 'dummy folder 1')

        Path dummyFilePath = folderWithSpacePath.resolve('dummypackage1').resolve('Dummy.class')
        Files.createDirectories(dummyFilePath.getParent())
        File dummyFile = dummyFilePath.toFile()
        try (FileOutputStream fos = new FileOutputStream(dummyFile)) {
            fos.write(0)
        }
        assert dummyFile.exists()

        Path jarWithSpacePath = folderWithSpacePath.resolve("dummy lib 2.jar")
        try (FileOutputStream fos = new FileOutputStream(jarWithSpacePath.toFile())
             JarOutputStream jos = new JarOutputStream(fos, new Manifest())) {
            JarEntry jarEntry = new JarEntry("dummypackage2/Dummy.class")
            jos.putNextEntry(jarEntry)
            jos.write(0)
        }
        assert jarWithSpacePath.toFile().exists()

        PackageHelperImpl helper = new PackageHelperImpl(null)

        Set<String> packageInFolder = helper.getPackageNames(folderWithSpacePath.toUri().toURL())
        assert packageInFolder.contains("dummypackage1")

        Set<String> packageInJar = helper.getPackageNames(jarWithSpacePath.toUri().toURL())
        assert packageInJar.contains("dummypackage2")
    }
}
