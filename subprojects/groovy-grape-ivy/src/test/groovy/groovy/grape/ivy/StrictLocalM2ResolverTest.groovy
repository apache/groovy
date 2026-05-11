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
package groovy.grape.ivy

import org.apache.ivy.core.module.id.ModuleRevisionId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

final class StrictLocalM2ResolverTest {

    private static final String ENABLE_PROPERTY = 'groovy.grape.strict-localm2'

    @TempDir
    File m2

    StrictLocalM2Resolver resolver

    @BeforeEach
    void setUp() {
        resolver = new StrictLocalM2Resolver()
        resolver.setRoot(m2.toURI().toURL().toString())
        resolver.setM2compatible(true)
        System.setProperty(ENABLE_PROPERTY, 'true')
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ENABLE_PROPERTY)
    }

    @Test
    void rejects_pomOnly_packagingJar_default() {
        File dir = layout('com.example', 'foo', '1.0')
        writePom(dir, 'foo', '1.0', null)
        // no JAR
        assertTrue resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            new File(dir, 'foo-1.0.pom'))
    }

    @Test
    void rejects_pomOnly_packagingJar_explicit() {
        File dir = layout('com.example', 'foo', '1.0')
        writePom(dir, 'foo', '1.0', 'jar')
        assertTrue resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            new File(dir, 'foo-1.0.pom'))
    }

    @Test
    void accepts_pomOnly_packagingPom() {
        File dir = layout('com.example', 'parent', '1.0')
        writePom(dir, 'parent', '1.0', 'pom')
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'parent', '1.0'),
            new File(dir, 'parent-1.0.pom'))
    }

    @Test
    void accepts_pomAndJar_present() {
        File dir = layout('com.example', 'foo', '1.0')
        writePom(dir, 'foo', '1.0', 'jar')
        new File(dir, 'foo-1.0.jar') << 'fake-jar-bytes'
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            new File(dir, 'foo-1.0.pom'))
    }

    @Test
    void rejects_pomOnly_packagingWar_warAbsent() {
        File dir = layout('com.example', 'webapp', '1.0')
        writePom(dir, 'webapp', '1.0', 'war')
        // no .war file
        assertTrue resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'webapp', '1.0'),
            new File(dir, 'webapp-1.0.pom'))
    }

    @Test
    void accepts_pomOnly_packagingWar_warPresent() {
        File dir = layout('com.example', 'webapp', '1.0')
        writePom(dir, 'webapp', '1.0', 'war')
        new File(dir, 'webapp-1.0.war') << 'fake-war-bytes'
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'webapp', '1.0'),
            new File(dir, 'webapp-1.0.pom'))
    }

    @Test
    void accepts_packagingBundle_jarPresent() {
        // OSGi bundle packaging produces a .jar file in Maven layout.
        File dir = layout('com.example', 'osgi-thing', '1.0')
        writePom(dir, 'osgi-thing', '1.0', 'bundle')
        new File(dir, 'osgi-thing-1.0.jar') << 'fake-jar-bytes'
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'osgi-thing', '1.0'),
            new File(dir, 'osgi-thing-1.0.pom'))
    }

    @Test
    void accepts_snapshotRevision_unconditionally() {
        // Snapshot filenames are timestamp-based; literal name check would
        // false-fail. Skip strictness for snapshots.
        File dir = layout('com.example', 'snap', '1.0-SNAPSHOT')
        writePom(dir, 'snap', '1.0-SNAPSHOT', 'jar')
        // no JAR alongside the POM (yet still accept)
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'snap', '1.0-SNAPSHOT'),
            new File(dir, 'snap-1.0-SNAPSHOT.pom'))
    }

    @Test
    void accepts_when_strictness_disabled_via_system_property() {
        System.setProperty(ENABLE_PROPERTY, 'false')
        File dir = layout('com.example', 'foo', '1.0')
        writePom(dir, 'foo', '1.0', 'jar')
        // No JAR; would normally reject.
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            new File(dir, 'foo-1.0.pom'))
    }

    @Test
    void accepts_when_not_m2compatible() {
        resolver.setM2compatible(false)
        File dir = layout('com.example', 'foo', '1.0')
        writePom(dir, 'foo', '1.0', 'jar')
        assertFalse resolver.shouldRejectAsHalfPopulated(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            new File(dir, 'foo-1.0.pom'))
    }

    @Test
    void readPackaging_findsLiteralValue() {
        File pom = File.createTempFile('pom-', '.xml')
        pom.deleteOnExit()
        pom.text = '<project><packaging>war</packaging></project>'
        assertEquals 'war', StrictLocalM2Resolver.readPackaging(pom)
    }

    @Test
    void readPackaging_returnsNullWhenAbsent() {
        File pom = File.createTempFile('pom-', '.xml')
        pom.deleteOnExit()
        pom.text = '<project><groupId>x</groupId></project>'
        assertNull StrictLocalM2Resolver.readPackaging(pom)
    }

    @Test
    void readPackaging_returnsNullForMalformedFile() {
        File pom = File.createTempFile('pom-', '.xml')
        pom.deleteOnExit()
        pom.text = 'not a valid xml file'
        assertNull StrictLocalM2Resolver.readPackaging(pom)
    }

    @Test
    void computeM2Dir_returnsExpectedPath() {
        File dir = resolver.computeM2Dir(
            ModuleRevisionId.newInstance('org.apache.commons', 'commons-lang3', '3.9'))
        assertEquals new File(m2, 'org/apache/commons/commons-lang3/3.9'), dir
    }

    @Test
    void computeM2Dir_handlesDoubleSlashRoot() {
        // Mimic the rendered ${user.home.url}/.m2/repository/ which can yield
        // a double-slash mid-path (e.g. file:/Users/x//.m2/repository/).
        resolver.setRoot('file:' + m2.absolutePath.replaceFirst('/', '//'))
        File dir = resolver.computeM2Dir(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'))
        assertEquals new File(m2, 'com/example/foo/1.0'), dir
    }

    private File layout(String org, String mod, String rev) {
        File dir = new File(m2, "${org.replace('.', '/')}/${mod}/${rev}")
        dir.mkdirs()
        dir
    }

    private static void writePom(File dir, String mod, String rev, String packaging) {
        StringBuilder pom = new StringBuilder()
        pom << '<project xmlns="http://maven.apache.org/POM/4.0.0">\n'
        pom << "  <groupId>com.example</groupId>\n"
        pom << "  <artifactId>${mod}</artifactId>\n"
        pom << "  <version>${rev}</version>\n"
        if (packaging != null) {
            pom << "  <packaging>${packaging}</packaging>\n"
        }
        pom << '</project>\n'
        new File(dir, "${mod}-${rev}.pom").text = pom.toString()
    }
}
