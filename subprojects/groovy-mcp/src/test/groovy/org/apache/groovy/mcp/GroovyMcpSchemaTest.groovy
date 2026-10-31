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

final class GroovyMcpSchemaTest {

    private GroovyMcpServer server

    @AfterEach
    void tearDown() {
        server?.close()
    }

    @Test
    void toolSchemasAcceptTextWithUriAndRequireTheRenameName() {
        server = new GroovyMcpServer()
        ready(server)
        def tools = json(server.exchange('{"jsonrpc":"2.0","id":60,"method":"tools/list"}')).result.tools
        def byName = tools.collectEntries { [(it.name): it.inputSchema] }

        def diagnostics = byName.groovy_diagnostics
        assert accepts(diagnostics, [text: 'class A {}'])
        assert accepts(diagnostics, [text: 'class A {}', uri: 'groovy-buffer:other'])
        assert accepts(diagnostics, [path: 'A.groovy'])
        assert accepts(diagnostics, [uri: 'groovy-buffer:other'])
        assert !accepts(diagnostics, [text: 'class A {}', path: 'A.groovy'])
        assert !accepts(diagnostics, [path: 'A.groovy', uri: 'file:///A.groovy'])
        assert !accepts(diagnostics, [text: 'class A {}', path: 'A.groovy', uri: 'groovy-buffer:other'])
        assert !accepts(diagnostics, [:])
        assert !accepts(diagnostics, [text: 'class A {}', extra: 1])

        def rename = byName.groovy_rename
        assert rename['required'].contains('newName')
        assert rename['required'].contains('line')
        assert rename['required'].contains('character')
        assert accepts(rename, [text: 'class A {}', line: 0, character: 6, newName: 'B'])
        assert accepts(rename, [text: 'class A {}', uri: 'groovy-buffer:other', line: 0, character: 6, newName: 'B'])
        assert accepts(rename, [path: 'A.groovy', line: 0, character: 6, newName: 'B'])
        assert !accepts(rename, [text: 'class A {}', line: 0, character: 6])
        assert !accepts(rename, [line: 0, character: 6, newName: 'B'])
        assert !accepts(rename, [text: 'class A {}', path: 'A.groovy', line: 0, character: 6, newName: 'B'])

        def symbols = byName.groovy_symbols
        assert accepts(symbols, [:])
        assert accepts(symbols, [query: 'A', maxResults: 1])
        assert accepts(symbols, [text: 'class A {}', uri: 'groovy-buffer:many'])
        assert accepts(symbols, [path: 'A.groovy'])
        assert accepts(symbols, [uri: 'groovy-buffer:many'])
        assert !accepts(symbols, [text: 'class A {}', path: 'A.groovy'])
        assert !accepts(symbols, [path: 'A.groovy', uri: 'file:///A.groovy'])
        assert !accepts(symbols, [nope: 1])

        def format = byName.groovy_format
        assert accepts(format, [text: 'class A {}', tabSize: 4, insertSpaces: true])
        assert !accepts(format, [text: 'class A {}', path: 'A.groovy', tabSize: 2])

        def closeSchema = byName.groovy_close
        assert closeSchema['oneOf'] == null
        assert closeSchema['required'] == ['uri']
        assert accepts(closeSchema, [uri: 'groovy-buffer:other'])
        assert !accepts(closeSchema, [:])

        def schemaUri = 'https://json-schema.org/draft/2020-12/schema'
        assert tools.every { it.title && it.inputSchema['$schema'] == schemaUri && it.outputSchema['$schema'] == schemaUri }
        assert tools.every { nameOk(it.name) }
        def outputs = tools.collectEntries { [(it.name): it.outputSchema] }
        assert accepts(outputs.groovy_diagnostics, [diagnostics: [], total: 0, truncated: false])
        assert accepts(outputs.groovy_diagnostics, [error: 'unknown tool'])
        assert !accepts(outputs.groovy_diagnostics, [error: 'x', total: 0])
        assert !accepts(outputs.groovy_diagnostics, [diagnostics: [], total: 0])
        assert accepts(outputs.groovy_references, [locations: [], total: 1, truncated: false])
        assert accepts(outputs.groovy_symbols, [symbols: [], truncated: false])
        assert accepts(outputs.groovy_describe, [markdown: null])
        assert accepts(outputs.groovy_signatures, [activeSignature: 0, activeParameter: 0, signatures: []])
        assert accepts(outputs.groovy_rename, [newName: 'B', changeCount: 0, changes: [], truncated: false])
        assert accepts(outputs.groovy_close, [closed: true])
        assert accepts(outputs.groovy_format, [changes: [], total: 0, truncated: false])
        def prompts = json(server.exchange('{"jsonrpc":"2.0","id":61,"method":"prompts/list"}')).result.prompts
        assert prompts.every { it.title }
        def resources = json(server.exchange('{"jsonrpc":"2.0","id":62,"method":"resources/list"}')).result.resources
        assert resources.every { it.title }

        def live = call('groovy_diagnostics', [text: 'class A {}'])
        assert accepts(outputs.groovy_diagnostics, live.structuredContent as Map)
        def unknown = call('missing', [:])
        assert unknown.isError == true
        assert accepts(outputs.groovy_diagnostics, unknown.structuredContent as Map)
        def closed = call('groovy_close', [uri: 'groovy-buffer:snippet'])
        assert accepts(outputs.groovy_close, closed.structuredContent as Map)
    }

    private Map call(String name, Map arguments) {
        def response = server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 2, method: 'tools/call',
                params: [name: name, arguments: arguments]]))
        assert response
        def parsed = json(response)
        assert parsed.result
        parsed.result as Map
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

    private static boolean nameOk(String name) {
        name ==~ /[A-Za-z0-9._-]{1,128}/ && !name.startsWith('-') && !name.startsWith('.') && !name.endsWith('-') && !name.endsWith('.')
    }

    private static boolean accepts(Map schema, Map args) {
        if (schema['additionalProperties'] == false && !args.keySet().every { schema['properties'].containsKey(it) }) {
            return false
        }
        def required = schema['required'] ?: []
        if (!required.every { args.containsKey(it) }) {
            return false
        }
        def branches = schema['oneOf']
        if (branches == null) {
            return true
        }
        int matched = 0
        for (branch in branches) {
            if (branchMatches(branch, args)) {
                matched++
            }
        }
        return matched == 1
    }

    private static boolean branchMatches(Map branch, Map args) {
        def required = branch['required'] ?: []
        if (!required.every { args.containsKey(it) }) {
            return false
        }
        return branch['not'] == null || !clause(branch['not'], args)
    }

    private static boolean clause(Map node, Map args) {
        if (node['anyOf'] != null) {
            return node['anyOf'].any { clause(it, args) }
        }
        def required = node['required'] ?: []
        return required.every { args.containsKey(it) }
    }
}
