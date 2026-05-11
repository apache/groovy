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

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

final class StrictCachedGrapesResolverTest {

    private static final String ENABLE_PROPERTY = 'groovy.grape.strict-cached-grapes'

    @TempDir
    File grapesRoot

    StrictCachedGrapesResolver resolver

    @BeforeEach
    void setUp() {
        resolver = new StrictCachedGrapesResolver()
        System.clearProperty(ENABLE_PROPERTY)  // exercise the default-on path
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ENABLE_PROPERTY)
    }

    @Test
    void rejects_ivy_without_original_companion() {
        File moduleDir = layout('com.example', 'foo', '1.0')
        File ivy = writeIvyStub(moduleDir, 'foo', '1.0')
        // no .original companion
        assertTrue resolver.shouldRejectAsStub(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            ivy)
    }

    @Test
    void accepts_ivy_with_original_companion() {
        File moduleDir = layout('com.example', 'foo', '1.0')
        File ivy = writeIvyStub(moduleDir, 'foo', '1.0')
        new File(moduleDir, "foo-1.0.pom") // unrelated POM, not the .original
        new File(moduleDir, ivy.name + '.original') << '<project/>' // the .original
        assertFalse resolver.shouldRejectAsStub(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            ivy)
    }

    @Test
    void accepts_snapshot_revision_regardless() {
        File moduleDir = layout('com.example', 'foo', '1.0-SNAPSHOT')
        File ivy = writeIvyStub(moduleDir, 'foo', '1.0-SNAPSHOT')
        // no .original — but it's a snapshot, so strictness is skipped
        assertFalse resolver.shouldRejectAsStub(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0-SNAPSHOT'),
            ivy)
    }

    @Test
    void accepts_when_disabled_via_system_property() {
        System.setProperty(ENABLE_PROPERTY, 'false')
        File moduleDir = layout('com.example', 'foo', '1.0')
        File ivy = writeIvyStub(moduleDir, 'foo', '1.0')
        // no .original — would normally be rejected, but strictness is off
        assertFalse resolver.shouldRejectAsStub(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            ivy)
    }

    @Test
    void accepts_when_mrid_null() {
        File moduleDir = layout('com.example', 'foo', '1.0')
        File ivy = writeIvyStub(moduleDir, 'foo', '1.0')
        assertFalse resolver.shouldRejectAsStub(null, ivy)
    }

    @Test
    void accepts_when_ivyFile_null() {
        assertFalse resolver.shouldRejectAsStub(
            ModuleRevisionId.newInstance('com.example', 'foo', '1.0'),
            null)
    }

    private File layout(String org, String mod, String rev) {
        File dir = new File(grapesRoot, "${org}/${mod}")
        dir.mkdirs()
        dir
    }

    private static File writeIvyStub(File dir, String mod, String rev) {
        File ivy = new File(dir, "ivy-${rev}.xml")
        ivy << """<?xml version="1.0" encoding="UTF-8"?>
<ivy-module version="2.0">
  <info organisation="com.example" module="${mod}" revision="${rev}" status="release"/>
  <publications>
    <artifact name="${mod}" type="jar" ext="jar"/>
  </publications>
</ivy-module>
"""
        ivy
    }
}
