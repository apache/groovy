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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit tests for {@link GrapeIvy#resolveExplicitConfig(String)} — the dispatcher
 * for {@code -Dgrape.config=<value>}.
 */
final class GrapeConfigResolutionTest {

    @Test
    void resolves_relaxed_shorthand_to_packaged_xml() {
        URL url = GrapeIvy.resolveExplicitConfig('relaxed')
        assertNotNull url
        assertTrue url.toString().endsWith('relaxedGrapeConfig.xml')
    }

    @Test
    void resolves_default_shorthand_to_packaged_xml() {
        URL url = GrapeIvy.resolveExplicitConfig('default')
        assertNotNull url
        assertTrue url.toString().endsWith('defaultGrapeConfig.xml')
    }

    @Test
    void resolves_classpath_prefix_via_classloader() {
        URL url = GrapeIvy.resolveExplicitConfig('classpath:groovy/grape/ivy/relaxedGrapeConfig.xml')
        assertNotNull url
        assertTrue url.toString().endsWith('relaxedGrapeConfig.xml')
    }

    @Test
    void returns_null_for_missing_classpath_resource() {
        assertNull GrapeIvy.resolveExplicitConfig('classpath:does/not/exist.xml')
    }

    @Test
    void resolves_existing_filesystem_path(@TempDir File tmp) {
        File f = new File(tmp, 'custom-grape.xml')
        f << '<ivysettings/>'
        URL url = GrapeIvy.resolveExplicitConfig(f.absolutePath)
        assertNotNull url
        assertEquals f.toURI().toURL(), url
    }

    @Test
    void returns_null_for_missing_filesystem_path() {
        assertNull GrapeIvy.resolveExplicitConfig('/nonexistent/no/such/file-grape.xml')
    }

    @Test
    void urlAsLocalFile_returns_file_for_existing_file_url(@TempDir File tmp) {
        // GROOVY-8372 / testConf2 regression: IvySettings.load(File) and load(URL)
        // set ivy.settings.dir differently (filesystem path vs file:-prefixed URL
        // string). User-supplied ivysettings.xml files that interpolate
        // ${ivy.settings.dir} into resolver patterns expect the path form, so we
        // route file:-URLs back through the File overload via urlAsLocalFile.
        File f = new File(tmp, 'custom-grape.xml')
        f << '<ivysettings/>'
        File mapped = GrapeIvy.urlAsLocalFile(f.toURI().toURL())
        assertEquals f, mapped
    }

    @Test
    void urlAsLocalFile_returns_null_for_non_file_url() {
        // e.g. a jar:file:... or http:... URL — only file: URLs can be routed
        // through the File overload. (jar: URLs are what classpath resources
        // resolve to in production; in tests they often resolve to real files
        // under build/resources/main/, which is a file: URL backed by a real
        // file, so that case is covered by the "existing file url" test above.)
        URL jarUrl = new URL('jar:file:/tmp/whatever.jar!/groovy/grape/ivy/foo.xml')
        assertNull GrapeIvy.urlAsLocalFile(jarUrl)
    }

    @Test
    void urlAsLocalFile_returns_null_for_nonexistent_file_url() {
        URL bogus = new File('/nonexistent/no/such/file-grape.xml').toURI().toURL()
        assertNull GrapeIvy.urlAsLocalFile(bogus)
    }

    @Test
    void urlAsLocalFile_returns_null_for_null_input() {
        assertNull GrapeIvy.urlAsLocalFile(null)
    }
}
