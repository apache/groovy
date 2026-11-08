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
package org.apache.groovy.mcp.internal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

final class McpInternalsTest {

    @Test
    void pathPolicyRejectsAMissingRootAndANullPath(@TempDir Path root) {
        assert !PathPolicy.inside(null, root)
        assert !PathPolicy.inside(root, null)
        assert PathPolicy.inside(root, root.resolve('A.groovy'))

        def noRoot = new PathPolicy(null)
        def missingRoot = false
        try {
            noRoot.requireInside(root)
        } catch (ToolFailure ex) {
            missingRoot = ex.message.contains('--root')
        }
        assert missingRoot

        def policy = new PathPolicy(root)
        def missingPath = false
        try {
            policy.requireInside(null)
        } catch (ToolFailure ex) {
            missingPath = ex.message.contains('outside')
        }
        assert missingPath

        def outside = false
        try {
            policy.resolveFile('   ', 16)
        } catch (ToolFailure ex) {
            outside = ex.message.contains('empty')
        }
        assert outside
        def missing = false
        try {
            policy.resolveFile(null, 16)
        } catch (ToolFailure ex) {
            missing = ex.message.contains('empty')
        }
        assert missing
        assert !EngineBridge.identifier(null)
        assert !EngineBridge.identifier('')
        assert EngineBridge.identifier('name')
        assert EngineBridge.identifier('nämn')
    }

    @Test
    void argumentMapRejectsANonStringNameAndAllowsANullAllowList() {
        def named = false
        try {
            ArgumentMap.of([(1): 'a'], Set.of('a'))
        } catch (ProtocolException ex) {
            named = ex.code() == -32602
        }
        assert named

        def open = ArgumentMap.of([text: 'class A {}'], null)
        assert open.requiredString('text') == 'class A {}'
        assert open.optionalString('missing') == null
        assert !open.has('missing')

        def notObject = false
        try {
            ArgumentMap.of(['a'], Set.of())
        } catch (ProtocolException ex) {
            notObject = ex.message.contains('object')
        }
        assert notObject

        def empty = ArgumentMap.of(null, Set.of())
        assert empty.optionalInt('maxResults', 50, 1, 200) == 50
        assert empty.optionalBoolean('insertSpaces', true)

        def encoded = JsonCodec.error(null, -32600, null)
        assert encoded.contains('"message":""')
        assert JsonCodec.parse('{"a":1}').a == 1
        def nested = false
        try {
            JsonCodec.parse('[' * 40 + ']' * 40)
        } catch (ProtocolException ex) {
            nested = ex.code() == -32700
        }
        assert nested
    }

    @Test
    void toolNamesFollowTheProtocolShape() {
        assert ToolCatalog.outputSchema('groovy_diagnostics')['$schema'] == 'https://json-schema.org/draft/2020-12/schema'
        ToolCatalog.checkToolName('groovy_diagnostics')
        ToolCatalog.checkToolName('a')
        ToolCatalog.checkToolName('a.b_c-d')
        ToolCatalog.checkToolName('a' * 128)
        for (name in [null, '', 'a' * 129, 'has space', '-led', 'dot.', '.led', 'trail-', 'a/b', 'nämn']) {
            def rejected = false
            try {
                ToolCatalog.checkToolName(name)
            } catch (IllegalStateException ex) {
                rejected = ex.message == 'invalid tool name'
            }
            assert rejected
        }
        def missing = false
        try {
            ToolCatalog.outputSchema('nope')
        } catch (IllegalStateException ex) {
            missing = ex.message == 'invalid tool name'
        }
        assert missing
    }

    @Test
    void utilityConstructorsAreCallable() {
        for (Class type : [JsonCodec, JsonValues, StdioFrames]) {
            def constructor = type.getDeclaredConstructor()
            constructor.accessible = true
            assert constructor.newInstance() != null
        }
        def launcher = Class.forName('org.apache.groovy.mcp.GroovyMcpLauncher').getDeclaredConstructor()
        launcher.accessible = true
        assert launcher.newInstance() != null
    }
}
