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
package org.apache.groovy.mcp

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class GroovyMcpModernTest {

    private GroovyMcpServer server

    @AfterEach
    void tearDown() {
        server?.close()
    }

    @Test
    void modernClientsDiscoverAndCallWithoutTheHandshake() {
        server = new GroovyMcpServer()
        def discovered = json(server.exchange('{"jsonrpc":"2.0","id":1,"method":"server/discover"}'))
        assert discovered.result.supportedVersions.contains(GroovyMcpServer.MODERN_PROTOCOL)
        assert discovered.result.supportedVersions.contains('2025-11-25')
        assert discovered.result.supportedVersions.contains('2024-11-05')
        assert discovered.result.ttlMs == 3600000
        assert discovered.result.cacheScope == 'public'
        assert discovered.result.resultType == 'complete'
        assert discovered.result.instructions.contains('does not run scripts')
        assert discovered.result._meta['io.modelcontextprotocol/serverInfo'].name == 'groovy-mcp'
        assert server.exchange('{"jsonrpc":"2.0","method":"server/discover"}') == null
        assert json(server.exchange('{"jsonrpc":"2.0","id":70,"method":"server/discover","params":{}}')).result.supportedVersions.contains('2024-11-05')
        assert json(server.exchange('{"jsonrpc":"2.0","id":71,"method":"server/discover","params":{"_meta":[]}}')).result.cacheScope == 'public'
        assert json(server.exchange('{"jsonrpc":"2.0","id":2,"method":"tools/list"}')).error.code == -32002

        def meta = ['io.modelcontextprotocol/protocolVersion': GroovyMcpServer.MODERN_PROTOCOL]
        def modernList = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 3, method: 'tools/list', params: [_meta: meta]])))
        assert modernList.result.tools.any { it.name == 'groovy_diagnostics' }
        assert modernList.result.ttlMs == 3600000
        assert modernList.result.cacheScope == 'public'
        assert modernList.result.resultType == 'complete'
        assert modernList.result._meta['io.modelcontextprotocol/serverInfo'].version == GroovyMcpServer.implementationVersion()

        def resources = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 4, method: 'resources/list', params: [_meta: meta]])))
        assert resources.result.resources.every { it.annotations.audience == ['assistant'] && it.annotations.priority > 0 }
        def prompts = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 5, method: 'prompts/list', params: [_meta: meta]])))
        assert prompts.result.prompts.every { it.annotations.audience == ['assistant'] }
        def templates = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 6, method: 'resources/templates/list', params: [_meta: meta]])))
        assert templates.result.resourceTemplates.isEmpty()
        assert templates.result.cacheScope == 'public'
        def keywords = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 7, method: 'resources/read',
                params: [uri: 'groovy://language/keywords', _meta: meta]])))
        assert keywords.result.contents[0].text.startsWith('[')
        assert keywords.result.ttlMs == 3600000

        def called = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 8, method: 'tools/call',
                params: [name: 'groovy_diagnostics', arguments: [text: 'class A {}'], _meta: meta]])))
        assert called.result.resultType == 'complete'
        assert called.result.isError == false
        assert called.result._meta['io.modelcontextprotocol/serverInfo'].name == 'groovy-mcp'

        def legacyMeta = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 9, method: 'server/discover',
                params: [_meta: ['io.modelcontextprotocol/protocolVersion': '2025-11-25']]])))
        assert legacyMeta.result.supportedVersions.contains('2025-06-18')
        def legacyOnList = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 17, method: 'tools/list',
                params: [_meta: ['io.modelcontextprotocol/protocolVersion': '2025-11-25']]])))
        assert legacyOnList.error.code == -32022
        assert legacyOnList.error.data.requested == '2025-11-25'

        def bad = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 10, method: 'tools/list',
                params: [_meta: ['io.modelcontextprotocol/protocolVersion': '1999-01-01']]])))
        assert bad.error.code == -32022
        assert bad.error.data.supported.contains(GroovyMcpServer.MODERN_PROTOCOL)
        assert bad.error.data.requested == '1999-01-01'

        def huge = 'v' * 200
        def hugeVersion = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 11, method: 'tools/list',
                params: [_meta: ['io.modelcontextprotocol/protocolVersion': huge]]])))
        assert hugeVersion.error.code == -32022
        assert hugeVersion.error.data.requested.size() == 64
        assert hugeVersion.error.data.requested != huge

        def notText = json(server.exchange('{"jsonrpc":"2.0","id":12,"method":"tools/list","params":{"_meta":{"io.modelcontextprotocol/protocolVersion":1}}}'))
        assert notText.error.code == -32022
        assert notText.error.data.requested == null
        def emptyVersion = json(server.exchange('{"jsonrpc":"2.0","id":13,"method":"tools/list","params":{"_meta":{"io.modelcontextprotocol/protocolVersion":""}}}'))
        assert emptyVersion.error.code == -32022

        assert json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 14, method: 'ping', params: [_meta: meta]]))).error.code == -32601
        assert json(server.exchange('{"jsonrpc":"2.0","id":15,"method":"ping"}')).result != null

        def handshake = new GroovyMcpServer()
        try {
            def init = json(handshake.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2026-07-28"}}'))
            assert init.result.protocolVersion == GroovyMcpServer.PROTOCOL_VERSION
            assert init.result.resultType == 'complete'
        } finally {
            handshake.close()
        }
        ready(server)
        def listed = json(server.exchange('{"jsonrpc":"2.0","id":16,"method":"tools/list"}'))
        assert listed.result.cacheScope == 'public'
        assert listed.result.resultType == 'complete'
    }

    private static void ready(GroovyMcpServer target) {
        def init = json(target.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{}}}'))
        assert init.result.protocolVersion == '2024-11-05'
        assert target.exchange('{"jsonrpc":"2.0","method":"notifications/initialized"}') == null
    }

    private static Map json(String line) {
        assert line
        new JsonSlurper().parseText(line) as Map
    }
}
