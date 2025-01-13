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
package groovy.grape.maven

import groovy.grape.Grape
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.util.jar.JarOutputStream

final class GrapeMavenTest {
    private static File writeEmptyJar(File jarFile) {
        jarFile.parentFile.mkdirs()
        jarFile.withOutputStream { os ->
            new JarOutputStream(os).close()
        }
        jarFile
    }

    private static void deleteCachedGroup(String groupId) {
        File cachedGroupDir = new File(GrapeMaven.grapeCacheDir, groupId.replace('.' as char, File.separatorChar))
        if (cachedGroupDir.exists()) {
            cachedGroupDir.deleteDir()
        }
    }

    private static File writePom(File pomFile, String groupId, String artifactId, String version, List<Map<String, Object>> deps = []) {
        pomFile.parentFile.mkdirs()
        String depsXml = deps.collect { Map<String, Object> dep ->
            String optionalXml = dep.optional ? '\n      <optional>true</optional>' : ''
            """\
    <dependency>
      <groupId>${dep.groupId}</groupId>
      <artifactId>${dep.artifactId}</artifactId>
      <version>${dep.version}</version>
      <scope>compile</scope>${optionalXml}
    </dependency>""".stripIndent()
        }.join('\n')

        pomFile.text = (
"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${groupId}</groupId>
  <artifactId>${artifactId}</artifactId>
  <version>${version}</version>
  <packaging>jar</packaging>
  <dependencies>
${depsXml}
  </dependencies>
</project>
"""
        )
        pomFile
    }

    private static void publishArtifact(File repoDir, String groupId, String artifactId, String version, List<Map<String, Object>> deps = []) {
        String relPath = groupId.replace('.', '/') + "/${artifactId}/${version}"
        File artifactDir = new File(repoDir, relPath)
        writePom(new File(artifactDir, "${artifactId}-${version}.pom"), groupId, artifactId, version, deps)
        writeEmptyJar(new File(artifactDir, "${artifactId}-${version}.jar"))
    }

    private static Set<String> jarNames(GroovyClassLoader loader) {
        loader.URLs.collect { url -> url.path.split('/')[-1] } as Set
    }

    @BeforeAll
    static void setUpClass() {
        System.setProperty('groovy.grape.impl', 'groovy.grape.maven.GrapeMaven')
        Grape.reset()
    }

    @AfterAll
    static void cleanup() {
        System.clearProperty('groovy.grape.impl')
        Grape.reset()
    }

    @Test
    void testResolveReturnsUris() {
        URI[] uris = Grape.resolve([autoDownload: true, classLoader: new GroovyClassLoader()],
            [groupId: 'commons-logging', artifactId: 'commons-logging', version: '1.2'])

        assert uris.length > 0
        assert uris.any { it.toString().contains('commons-logging-1.2') }
    }

    @Test
    void testListDependenciesAfterGrab() {
        def loader = new GroovyClassLoader()

        Grape.grab(classLoader: loader,
            [groupId: 'commons-logging', artifactId: 'commons-logging', version: '1.2'])

        Map[] loadedDependencies = Grape.listDependencies(loader)
        assert loadedDependencies.find {
            it.group == 'commons-logging' && it.module == 'commons-logging' && it.version == '1.2'
        }
    }

    @Test
    void testNonTransitiveGrabResolvesOnlyDirectArtifact() {
        URI[] uris = Grape.resolve([autoDownload: true, classLoader: new GroovyClassLoader()],
            [groupId: 'org.apache.httpcomponents', artifactId: 'httpclient', version: '4.5.14', transitive: false])

        assert uris.any { it.toString().contains('httpclient-4.5.14') }
        assert !uris.any { it.toString().contains('httpcore-') }
    }

    @Test
    void testClassifierResolvesJsonLibJdk15() {
        def loader = new GroovyClassLoader()

        Grape.grab(classLoader: loader,
            [groupId: 'net.sf.json-lib', artifactId: 'json-lib', version: '2.2.3', classifier: 'jdk15'])

        Set jars = jarNames(loader)
        assert jars.any { it == 'json-lib-2.2.3-jdk15.jar' }
        assert jars.any { it.startsWith('ezmorph-') }
    }

    @Test
    void testOptionalDependencyIsNotPulledTransitively() {
        File tempRoot = Files.createTempDirectory('grape-maven-optional-test').toFile()
        File repoDir = new File(tempRoot, 'repo')

        String g = 'dev.grape.optional'
        deleteCachedGroup(g)
        publishArtifact(repoDir, g, 'dep-required', '1.0.0')
        publishArtifact(repoDir, g, 'dep-optional', '1.0.0')
        publishArtifact(repoDir, g, 'root-artifact', '1.0.0', [
            [groupId: g, artifactId: 'dep-required', version: '1.0.0', optional: false],
            [groupId: g, artifactId: 'dep-optional', version: '1.0.0', optional: true],
        ])

        Grape.reset()
        System.setProperty('groovy.grape.impl', 'groovy.grape.maven.GrapeMaven')
        Grape.addResolver(name: 'local-optional-test', root: repoDir.toURI().toString(), m2Compatible: true)
        URI[] uris = Grape.resolve([autoDownload: true, classLoader: new GroovyClassLoader()],
            [groupId: g, artifactId: 'root-artifact', version: '1.0.0'])

        assert uris.any { it.toString().contains('root-artifact-1.0.0.jar') }
        assert uris.any { it.toString().contains('dep-required-1.0.0.jar') }
        assert !uris.any { it.toString().contains('dep-optional-1.0.0.jar') }
    }
}
