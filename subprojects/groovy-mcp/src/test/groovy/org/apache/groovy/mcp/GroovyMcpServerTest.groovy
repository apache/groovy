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
import org.apache.groovy.mcp.internal.JsonValues
import org.apache.groovy.mcp.internal.ProtocolException
import org.apache.groovy.mcp.internal.ToolFailure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.math.BigDecimal
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission

final class GroovyMcpServerTest {

    private static final String SAMPLE = '''\
        class Sample {
            def ping() { ping() }
        }
        '''.stripIndent()

    private GroovyMcpServer server

    @AfterEach
    void tearDown() {
        server?.close()
    }

    @Test
    void lifecycleRejectsWorkUntilInitializedAndThenListsTools() {
        server = new GroovyMcpServer()
        assert server.exchange(null) == null
        assert server.exchange('') == null
        assert server.exchange('   ') == null
        assert server.exchange('\uFEFF') == null
        def bom = server.exchange('\uFEFF{"jsonrpc":"2.0","id":1,"method":"ping"}')
        assert json(bom).result != null

        def early = json(server.exchange('{"jsonrpc":"2.0","id":2,"method":"tools/list"}'))
        assert early.error.code == -32002

        def pingNote = server.exchange('{"jsonrpc":"2.0","method":"ping"}')
        assert pingNote == null

        def init = json(server.exchange('{"jsonrpc":"2.0","id":"s","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"0"}}}'))
        assert init.id == 's'
        assert init.result.protocolVersion == '2025-03-26'
        assert init.result.serverInfo.name == 'groovy-mcp'
        assert init.result.serverInfo.version == GroovyMcpServer.implementationVersion()
        assert init.result.serverInfo.version != 'null'
        assert init.result.capabilities.tools.listChanged == false
        assert init.result.instructions.contains('does not run scripts')

        def tooSoon = json(server.exchange('{"jsonrpc":"2.0","id":3,"method":"tools/list"}'))
        assert tooSoon.error.code == -32002
        assert tooSoon.error.message.contains('initialized')

        assert server.exchange('{"jsonrpc":"2.0","method":"notifications/initialized"}') == null
        def tools = json(server.exchange('{"jsonrpc":"2.0","id":4,"method":"tools/list","params":{}}'))
        def names = tools.result.tools.collect { it.name }
        assert names.contains('groovy_diagnostics')
        assert names.contains('groovy_rename')
        assert names.contains('groovy_close')
        assert tools.result.tools.every { it.annotations.readOnlyHint == true && it.annotations.destructiveHint == false }
        assert tools.result.tools.every { !JsonOutput.toJson(it).contains('\n') }

        def again = json(server.exchange('{"jsonrpc":"2.0","id":5,"method":"initialize","params":{"protocolVersion":"2025-03-26"}}'))
        assert again.error.code == -32600
    }

    @Test
    void protocolEdgesCoverParseIdsCursorsAndNotifications() {
        server = new GroovyMcpServer(null, new StringBuilder())
        assert json(server.exchange('not json')).error.code == -32700
        assert json(server.exchange('[1,2]')).error.message.contains('batches')
        assert json(server.exchange('1')).error.code == -32600
        assert json(server.exchange('true')).error.code == -32600
        assert json(server.exchange('"x"')).error.code == -32600
        assert json(server.exchange('{"id":1,"method":"ping"}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"1.0","id":1,"method":"ping"}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"2.0","id":1}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"2.0","id":1,"method":""}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"2.0","id":1,"method":1}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"2.0","id":1.5,"method":"ping"}')).error.code == -32600
        assert json(server.exchange('{"jsonrpc":"2.0","id":true,"method":"ping"}')).error.code == -32600
        def longId = 'a' * 1025
        def longIdResponse = server.exchange('{"jsonrpc":"2.0","id":"' + longId + '","method":"ping"}')
        assert json(longIdResponse).error.message.contains('too long')
        assert !longIdResponse.contains(longId)
        assert json(server.exchange('{"jsonrpc":"2.0","id":1,"method":"nope"}')).error.code == -32002
        assert json(server.exchange('{"jsonrpc":"2.0","id":null,"method":"ping"}')).error.code == -32600
        def echoed = json(server.exchange('{"jsonrpc":"2.0","id":42,"method":""}'))
        assert echoed.id == 42
        assert echoed.error.code == -32600
        assert json(server.exchange('{"jsonrpc":"1.0","id":true,"method":"ping"}')).id == null

        ready(server)
        assert json(server.exchange('{"jsonrpc":"2.0","id":9,"method":"nope"}')).error.code == -32601
        assert server.exchange('{"jsonrpc":"2.0","method":"notifications/cancelled","params":{"requestId":9}}') == null
        assert server.exchange('{"jsonrpc":"2.0","method":"notifications/progress","params":"nope"}') == null
        assert server.exchange('{"jsonrpc":"2.0","method":"notifications/initialized"}') == null
        assert json(server.exchange('{"jsonrpc":"2.0","id":10,"method":"ping"}')).result != null

        def nested = '[' * 40 + ']' * 40
        assert json(server.exchange('{"jsonrpc":"2.0","id":11,"method":"ping","params":' + nested + '}')).error.code == -32700

        assert json(server.exchange('{"jsonrpc":"2.0","id":12,"method":"tools/list","params":[]}')).error.code == -32602
        assert json(server.exchange('{"jsonrpc":"2.0","id":13,"method":"tools/list","params":{"cursor":"page-2"}}')).error.message.contains('cursor')
        assert json(server.exchange('{"jsonrpc":"2.0","id":14,"method":"resources/list","params":{"cursor":""}}')).result.resources.size() == 3
        assert json(server.exchange('{"jsonrpc":"2.0","id":15,"method":"resources/templates/list"}')).result.resourceTemplates.isEmpty()
        assert json(server.exchange('{"jsonrpc":"2.0","id":16,"method":"prompts/list","params":{"cursor":null}}')).result.prompts.size() == 2

        def badInit = new GroovyMcpServer()
        try {
            assert badInit.exchange('{"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26"}}') == null
            assert json(badInit.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":[]}')).error.code == -32602
            assert json(badInit.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}')).error.code == -32602
            assert json(badInit.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":""}}')).error.code == -32602
            def other = json(badInit.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"1999-01-01"}}'))
            assert other.result.protocolVersion == GroovyMcpServer.PROTOCOL_VERSION
            assert badInit.exchange('{"jsonrpc":"2.0","method":"initialize","params":[]}') == null
        } finally {
            badInit.close()
        }
        def negotiated = new GroovyMcpServer()
        try {
            def modern = json(negotiated.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{}}}'))
            assert modern.result.protocolVersion == '2025-06-18'
            assert modern.result.serverInfo.title == 'Groovy MCP'
            assert modern.result.serverInfo.description.contains('Does not run scripts')
            assert modern.result.serverInfo.websiteUrl == 'https://groovy.apache.org/'
            assert json(negotiated.exchange('{"jsonrpc":"2.0","id":2,"method":"notifications/initialized"}')).result != null
            assert json(negotiated.exchange('{"jsonrpc":"2.0","id":3,"method":"tools/list"}')).result.tools
            assert negotiated.exchange('{"jsonrpc":"2.0","method":"notifications/initialized"}') == null
        } finally {
            negotiated.close()
        }
        def latest = new GroovyMcpServer()
        try {
            def current = json(latest.exchange('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{}}}'))
            assert current.result.protocolVersion == '2025-11-25'
            assert current.result.protocolVersion == GroovyMcpServer.PROTOCOL_VERSION
        } finally {
            latest.close()
        }
    }

    @Test
    void toolsAnswerFromTheCompilerAndDoNotRunTheSource(@TempDir Path root) {
        def classpath = [root.resolve('libs').toString()]
        server = new GroovyMcpServer(new McpServerConfig(root, classpath))
        ready(server)

        def limits = read('groovy://server/limits')
        def limitsJson = new JsonSlurper().parseText(limits.contents[0].text) as Map
        assert limitsJson.execution == false
        assert limitsJson.grapeEnabled == false
        assert limitsJson.astTestEnabled == false
        assert limitsJson.classpathEntries == 1
        assert limitsJson.root == root.toAbsolutePath().normalize().toString()
        assert limitsJson.snippetUri == 'groovy-buffer:snippet'

        def keywords = new JsonSlurper().parseText(read('groovy://language/keywords').contents[0].text) as List
        assert keywords.contains('class')
        assert keywords.contains('def')
        def imports = new JsonSlurper().parseText(read('groovy://language/default-imports').contents[0].text) as List
        assert imports.contains('java.lang.')
        assert imports.contains('groovy.lang.')
        assert json(server.exchange('{"jsonrpc":"2.0","id":20,"method":"resources/read","params":{"uri":"groovy://nope"}}')).error.code == -32602
        assert json(server.exchange('{"jsonrpc":"2.0","id":21,"method":"resources/read","params":{}}')).error.code == -32602

        def broken = tool('groovy_diagnostics', [text: 'class A { def a = }'])
        assert broken.isError == false
        assert broken.structuredContent.diagnostics.any { it.message }

        def described = tool('groovy_describe', [text: SAMPLE, line: 0, character: 6])
        assert described.isError == false
        assert described.structuredContent.markdown.contains('Sample')
        def defined = tool('groovy_definition', [uri: 'groovy-buffer:snippet', line: 1, character: 8])
        assert defined.structuredContent.definitions.any { it.snippet.contains('ping') }
        def implemented = tool('groovy_implementations', [uri: 'groovy-buffer:snippet', line: 0, character: 6])
        assert implemented.structuredContent.implementations != null
        def refs = tool('groovy_references', [uri: 'groovy-buffer:snippet', line: 1, character: 8, maxResults: 10])
        assert refs.structuredContent.total >= 1
        def completed = tool('groovy_complete', [uri: 'groovy-buffer:snippet', line: 1, character: 16])
        assert completed.structuredContent.completions != null
        def signatures = tool('groovy_signatures', [uri: 'groovy-buffer:snippet', line: 1, character: 16])
        assert signatures.structuredContent.signatures != null
        def tokens = tool('groovy_tokens', [uri: 'groovy-buffer:snippet'])
        assert tokens.structuredContent.total > 0
        assert tokens.structuredContent.truncated == false
        def formatted = tool('groovy_format', [uri: 'groovy-buffer:snippet', tabSize: 2, insertSpaces: false])
        assert formatted.structuredContent.changes != null
        def organized = tool('groovy_organize_imports', [text: 'import java.util.List\nclass Sample {}\n', uri: 'groovy-buffer:other'])
        assert organized.isError == false
        def symbols = tool('groovy_symbols', [query: 'Sam', maxResults: 10])
        assert symbols.structuredContent.symbols.any { it.name == 'Sample' }
        def renamed = tool('groovy_rename', [uri: 'groovy-buffer:other', line: 1, character: 6, newName: 'Renamed'])
        assert renamed.structuredContent.newName == 'Renamed'

        def closed = tool('groovy_close', [uri: 'groovy-buffer:other'])
        assert closed.structuredContent.closed == true
        def missing = tool('groovy_close', [uri: 'groovy-buffer:other'])
        assert missing.structuredContent.closed == false

        def file = root.resolve('Sample.groovy')
        Files.writeString(file, SAMPLE)
        def original = Files.readString(file)
        def fromPath = tool('groovy_diagnostics', [path: 'Sample.groovy'])
        assert fromPath.isError == false
        def fromAbsolute = tool('groovy_describe', [path: file.toString(), line: 0, character: 6])
        assert fromAbsolute.structuredContent.markdown.contains('Sample')
        tool('groovy_rename', [path: 'Sample.groovy', line: 0, character: 6, newName: 'Renamed'])
        assert Files.readString(file) == original
        def fileUri = file.toUri().toString()
        def fromUri = tool('groovy_diagnostics', [uri: fileUri])
        assert fromUri.isError == false
        def closedFile = tool('groovy_close', [uri: fileUri])
        assert closedFile.structuredContent.closed == true
        assert Files.exists(file)

        assert tool('groovy_diagnostics', [text: 'class A {}', path: 'Sample.groovy']).isError == true
        assert tool('groovy_diagnostics', [path: 'Sample.groovy', uri: fileUri]).structuredContent.error.contains('not both')
        assert tool('groovy_diagnostics', [path: '   ']).structuredContent.error.contains('empty')
        assert tool('groovy_diagnostics', [path: file.toString() + '-missing-dir/A.groovy']).isError == true
        assert tool('groovy_diagnostics', [:]).structuredContent.error.contains('required')
        assert tool('groovy_diagnostics', [path: '../Sample.groovy']).structuredContent.error.contains('outside')
        assert tool('groovy_diagnostics', [path: root.toString()]).structuredContent.error.contains('not a file')
        assert tool('groovy_diagnostics', [path: 'missing.groovy']).structuredContent.error.contains('not a file')
        def missingUri = root.resolve('missing.groovy').toUri().toString()
        assert tool('groovy_diagnostics', [uri: missingUri]).structuredContent.error.contains('not readable')
        def outsideFile = Files.createTempFile('groovy-mcp-outside', '.groovy')
        try {
            Files.writeString(outsideFile, 'class Outside {}')
            assert tool('groovy_diagnostics', [uri: outsideFile.toUri().toString()]).structuredContent.error.contains('outside')
        } finally {
            Files.deleteIfExists(outsideFile)
        }
        def queried = root.resolve('Queried.groovy').toUri().toString()
        assert tool('groovy_diagnostics', [uri: queried + '?raw=1']).structuredContent.error.contains('not valid')
        assert tool('groovy_diagnostics', [path: "bad\u0000name.groovy"]).structuredContent.error.contains('not valid')
        assert tool('groovy_describe', [text: 'class A {}']).structuredContent.error.contains('required')
        assert tool('groovy_describe', [text: 'class A {}', line: -1, character: 0]).structuredContent.error.contains('between')
        assert tool('groovy_describe', [text: 'class A {}', line: 1.5, character: 0]).structuredContent.error.contains('integer')
        assert tool('groovy_describe', [text: 'class A {}', line: '0', character: 0]).structuredContent.error.contains('integer')
        assert tool('groovy_format', [text: 'class A {}', tabSize: 0]).isError == true
        assert tool('groovy_format', [text: 'class A {}', insertSpaces: 'yes']).structuredContent.error.contains('boolean')
        assert tool('groovy_rename', [text: 'class A {}', line: 0, character: 6, newName: '1bad']).isError == true
        assert tool('groovy_rename', [text: 'class A {}', line: 0, character: 6, newName: 'a-b']).isError == true
        assert tool('groovy_symbols', [maxResults: 0]).isError == true
        assert tool('groovy_diagnostics', [text: 'class A {}', extra: true]).structuredContent.error.contains('unknown argument')
        assert tool('groovy_describe', [text: 'class A {}', line: 0, character: 100001]).structuredContent.error.contains('between')
        assert tool('groovy_describe', [text: 'class A {}', line: 1000001, character: 0]).structuredContent.error.contains('between')
        assert tool('groovy_format', [text: 'class A {}', tabSize: 17]).isError == true
        assert tool('groovy_format', [text: 'class A {}']).isError == false
        assert tool('groovy_format', [text: 'class A {}', insertSpaces: null]).structuredContent.error.contains('boolean')
        assert tool('groovy_references', [text: 'class A {}', line: 0, character: 6, maxResults: null]).isError == true
        assert tool('groovy_references', [text: 'class A {}', line: 0, character: 6, maxResults: 201]).isError == true
        assert tool('groovy_rename', [text: 'class A {}', line: 0, character: 6]).structuredContent.error.contains('required')
        assert tool('groovy_symbols', [query: 1]).isError == true
        assert tool('groovy_diagnostics', [text: null]).structuredContent.error.contains('required')
        assert tool('groovy_diagnostics', [text: 1]).structuredContent.error.contains('string')
        assert tool('groovy_diagnostics', [uri: 'groovy-buffer:']).isError == true
        assert tool('groovy_diagnostics', [uri: 'groovy-buffer:' + ('n' * 201)]).structuredContent.error.contains('not valid')
        assert tool('groovy_diagnostics', [uri: '']).isError == true
        // Path.toUri() is a legal file URI on every OS. Prefixing FILE:// onto a
        // raw path is not: a Windows path contains backslashes.
        def upperScheme = file.toUri().toString().replaceFirst('(?i)^file:', 'FILE:')
        assert tool('groovy_close', [uri: upperScheme]).structuredContent.closed == false

        def scheme = tool('groovy_diagnostics', [text: 'class A {}', uri: 'http://example.com/A.groovy'])
        assert scheme.structuredContent.error.contains('groovy-buffer')
        def badUri = tool('groovy_diagnostics', [text: 'class A {}', uri: 'not a uri'])
        assert badUri.isError == true
        assert tool('groovy_diagnostics', [uri: 'groovy-buffer:missing']).structuredContent.error.contains('not open')
        assert tool('groovy_close', [uri: 'http://example.com/A']).isError == true

        def attack = '''\
            import groovy.transform.ASTTest
            import org.codehaus.groovy.control.CompilePhase
            @ASTTest(phase = CompilePhase.SEMANTIC_ANALYSIS, value = { assert false : "executed" })
            class Boom {}
            '''.stripIndent()
        def safe = tool('groovy_diagnostics', [text: attack, uri: 'groovy-buffer:attack'])
        assert safe.isError == false
        assert !safe.content[0].text.contains('executed')
    }

    @Test
    void smallLimitsRejectOversizedPayloadsAndAServerWithoutRootRejectsPaths(@TempDir Path root) {
        server = new GroovyMcpServer(new McpServerConfig(null, List.of(), 130, 16, 32))
        ready(server)
        def hugeLine = '{"jsonrpc":"2.0","id":1,"method":"ping","params":{"pad":"' + ('x' * 80) + '"}}'
        assert json(server.exchange(hugeLine)).error.message.contains('exceeds')
        assert server.tooLarge().contains('exceeds')
        assert server.malformed().contains('Parse error')
        def oversized = tool('groovy_diagnostics', [text: 'class BigEnough {}'])
        assert oversized.isError == true
        assert oversized.structuredContent.error == 'too large'
        def sourceCapped = new GroovyMcpServer(new McpServerConfig(null, List.of(),
                McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES, 16, McpServerConfig.DEFAULT_MAX_RESULT_CHARS))
        try {
            ready(sourceCapped)
            def limited = call(sourceCapped, 'groovy_diagnostics', [text: 'class BigEnough {}'])
            assert limited.structuredContent.error == 'text exceeds the source limit'
        } finally {
            sourceCapped.close()
        }
        def rootedSmall = new GroovyMcpServer(new McpServerConfig(root, List.of(),
                McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES, 16, McpServerConfig.DEFAULT_MAX_RESULT_CHARS))
        try {
            ready(rootedSmall)
            Files.writeString(root.resolve('Big.groovy'), 'class BigEnough {}')
            def big = call(rootedSmall, 'groovy_diagnostics', [path: 'Big.groovy'])
            assert big.structuredContent.error == 'file exceeds the source limit'
        } finally {
            rootedSmall.close()
        }

        def rooted = new GroovyMcpServer(new McpServerConfig(null, List.of(),
                McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES, McpServerConfig.DEFAULT_MAX_SOURCE_CHARS, 32))
        try {
            ready(rooted)
            def fat = call(rooted, 'groovy_tokens', [text: SAMPLE])
            assert fat.isError == true
            assert fat.structuredContent.error == 'too large'
            assert fat.content[0].text.length() <= 32
        } finally {
            rooted.close()
        }
        def wide = new GroovyMcpServer(new McpServerConfig(null, List.of(),
                McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES, McpServerConfig.DEFAULT_MAX_SOURCE_CHARS, 80))
        try {
            ready(wide)
            def limited = call(wide, 'groovy_tokens', [text: SAMPLE])
            assert limited.structuredContent.error.contains('size limit')
            assert json(wide.exchange('{"jsonrpc":"2.0","id":40,"method":"resources/read","params":{"uri":"groovy://language/keywords"}}')).error.message.contains('resource exceeds')
        } finally {
            wide.close()
        }

        def memory = new GroovyMcpServer()
        try {
            ready(memory)
            def rejected = call(memory, 'groovy_diagnostics', [path: root.resolve('A.groovy').toString()])
            assert rejected.structuredContent.error.contains('--root')
            def prompt = json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 30, method: 'prompts/get',
                    params: [name: 'explain-groovy', arguments: [code: 'class A {}', question: 'Why?']]])))
            assert prompt.result.messages[0].content.text.contains('class A {}')
            assert prompt.result.messages[0].content.text.contains('Do not run')
            def review = json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 31, method: 'prompts/get',
                    params: [name: 'review-groovy', arguments: [code: 'class A {}']]])))
            assert review.result.messages[0].content.text.contains('groovy_diagnostics')
            def plain = json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 41, method: 'prompts/get',
                    params: [name: 'explain-groovy', arguments: [code: 'class A {}']]])))
            assert plain.result.messages[0].content.text.contains('Explain what this Groovy source does.')
            def blankQuestion = json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 49, method: 'prompts/get',
                    params: [name: 'explain-groovy', arguments: [code: 'class A {}', question: '   ']]])))
            assert blankQuestion.result.messages[0].content.text.startsWith('Explain what this Groovy source does.')
            def noRootLimits = new JsonSlurper().parseText(json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 50,
                    method: 'resources/read', params: [uri: 'groovy://server/limits']]))).result.contents[0].text) as Map
            assert noRootLimits.root == ''
            assert json(memory.exchange('{"jsonrpc":"2.0","id":32,"method":"prompts/get","params":{"name":"missing"}}')).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":33,"method":"prompts/get","params":{"name":"explain-groovy"}}')).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":34,"method":"prompts/get","params":{"name":"explain-groovy","arguments":[]}}')).error.code == -32602
            assert json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 35, method: 'prompts/get',
                    params: [name: 'explain-groovy', arguments: [code: 'class A {}', extra: 1]]]))).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":36,"method":"tools/call","params":{"name":"groovy_diagnostics","arguments":[]}}')).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":37,"method":"tools/call","params":{}}')).error.code == -32602
            def unknownTool = json(memory.exchange('{"jsonrpc":"2.0","id":38,"method":"tools/call","params":{"name":"nope"}}'))
            assert unknownTool.result.isError == true
            assert unknownTool.result.structuredContent.error == 'unknown tool'
            assert !unknownTool.result.content[0].text.contains('nope')
            def newlineName = json(memory.exchange('{"jsonrpc":"2.0","id":39,"method":"tools/call","params":{"name":"groovy_diagnostics","arguments":{"unknown\\nname":1}}}'))
            assert newlineName.result.isError == true
            assert !newlineName.result.content[0].text.contains('\n')
            def brokenTool = json(memory.exchange('{"jsonrpc":"2.0","id":42,"method":"tools/call","params":{"name":"no-such\\ntool"}}'))
            assert brokenTool.result.isError == true
            assert brokenTool.result.structuredContent.error == 'unknown tool'
            assert !brokenTool.result.content[0].text.contains('no-such')
            assert json(memory.exchange('{"jsonrpc":"2.0","id":51,"method":"tools/call","params":{"name":1}}')).error.code == -32602
            def nullArgs = json(memory.exchange('{"jsonrpc":"2.0","id":52,"method":"tools/call","params":{"name":"groovy_diagnostics","arguments":null}}'))
            assert nullArgs.result.isError == true
            assert json(memory.exchange('{"jsonrpc":"2.0","id":53,"method":"prompts/get","params":{"name":1}}')).error.code == -32602
            def nullPromptArgs = json(memory.exchange('{"jsonrpc":"2.0","id":54,"method":"prompts/get","params":{"name":"explain-groovy","arguments":null}}'))
            assert nullPromptArgs.error.code == -32602
            assert json(memory.exchange('{"method":"ping"}')).error.code == -32600
            assert json(memory.exchange('{"jsonrpc":"2.0","id":43,"method":"tools/list","params":{"cursor":1}}')).error.message.contains('cursor')
            assert json(memory.exchange('{"jsonrpc":"2.0","id":44,"method":"prompts/get","params":[]}')).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":45,"method":"prompts/get","params":{"name":""}}')).error.code == -32602
            assert json(memory.exchange('{"jsonrpc":"2.0","id":46,"method":"resources/read","params":{"uri":""}}')).error.code == -32602
            def bigCode = json(memory.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 47, method: 'prompts/get',
                    params: [name: 'review-groovy', arguments: [code: 'c' * (McpServerConfig.DEFAULT_MAX_SOURCE_CHARS + 1)]]])))
            assert bigCode.error.message.contains('source limit')
        } finally {
            memory.close()
        }
        assert json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 48, method: 'prompts/get',
                params: [name: 'explain-groovy', arguments: [code: 'c' * 17]]]))).error.message.contains('source limit')
    }

    @Test
    void unreadableFileIsReportedAsAToolError(@TempDir Path root) {
        Assumptions.assumeTrue(FileSystems.default.supportedFileAttributeViews().contains('posix'),
                'posix permissions are not available')
        server = new GroovyMcpServer(new McpServerConfig(root, List.of()))
        ready(server)
        def file = root.resolve('Secret.groovy')
        Files.writeString(file, 'class Secret {}')
        def previous = Files.getPosixFilePermissions(file)
        Files.setPosixFilePermissions(file, EnumSet.noneOf(PosixFilePermission))
        try {
            def rejected = tool('groovy_diagnostics', [path: 'Secret.groovy'])
            assert rejected.isError == true
            def error = rejected.structuredContent.error as String
            assert error.contains('unreadable') || error.contains('not a file') || error.contains('not readable')
        } finally {
            Files.setPosixFilePermissions(file, previous)
        }
    }

    @Test
    void symlinkOutsideTheRootIsRejected(@TempDir Path root) {
        server = new GroovyMcpServer(new McpServerConfig(root, List.of()))
        ready(server)
        def outsideDir = Files.createTempDirectory('groovy-mcp-outside')
        try {
            def outside = outsideDir.resolve('Secret.groovy')
            Files.writeString(outside, 'class Secret {}')
            def link = root.resolve('Secret.groovy')
            try {
                Files.createSymbolicLink(link, outside)
            } catch (UnsupportedOperationException | IOException ex) {
                Assumptions.assumeTrue(false, 'symlinks are not available')
            }
            def rejected = tool('groovy_diagnostics', [path: 'Secret.groovy'])
            assert rejected.isError == true
            assert rejected.structuredContent.error.contains('outside')
        } finally {
            outsideDir.toFile().deleteDir()
        }
    }

    @Test
    void internalFailureIsLoggedWithoutBreakingTheStream() {
        def log = new StringBuilder()
        def throwing = new Appendable() {
            @Override
            Appendable append(CharSequence csq) throws IOException {
                throw new IOException('disk')
            }

            @Override
            Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException('disk')
            }

            @Override
            Appendable append(char c) throws IOException {
                throw new IOException('disk')
            }
        }
        server = new GroovyMcpServer(McpServerConfig.defaults(), throwing)
        def failed = json(server.respond(7, { throw new IllegalStateException('boom\nsecret') }))
        assert failed.error.code == -32603
        assert !failed.error.message.contains('secret')
        server.close()
        server = new GroovyMcpServer(McpServerConfig.defaults(), log)
        def blank = json(server.respond(8, { throw new RuntimeException('  ') }))
        assert blank.error.code == -32603
        def unnamed = json(server.respond(11, { throw new RuntimeException((String) null) }))
        assert unnamed.error.code == -32603
        assert log.toString().contains('RuntimeException')
        assert !log.toString().contains('\n\n')
        def protocol = json(server.respond(9, { throw new ProtocolException(-32601, 'missing') }))
        assert protocol.error.code == -32601
        server.close()
        server.close()
        def closed = false
        try {
            server.respond(10, { 'open' })
        } catch (IllegalStateException ex) {
            closed = ex.message == 'closed'
        }
        assert closed
        try {
            server.tooLarge()
        } catch (IllegalStateException ex) {
            assert ex.message == 'closed'
        }
        server = null
        assert new ProtocolException(1, null).message == ''
        assert new ToolFailure(null).message == ''
        assert GroovyMcpServer.failureText(null) == 'invalid request'
        assert GroovyMcpServer.failureText(new IllegalStateException('  ')) == 'invalid request'
        assert GroovyMcpServer.failureText(new IllegalArgumentException('unreadable')) == 'unreadable'

        assert JsonValues.asInt(1.0d) == 1
        assert JsonValues.asInt(1.5d) == null
        assert JsonValues.asInt(Float.NaN) == null
        assert JsonValues.asInt(Float.POSITIVE_INFINITY) == null
        assert JsonValues.asInt(1.0f) == 1
        assert JsonValues.asInt(1) == 1
        assert JsonValues.asInt(Long.MAX_VALUE) == null
        assert JsonValues.asInt(Long.MIN_VALUE) == null
        assert JsonValues.asInt(new BigDecimal('1.0')) == 1
        assert JsonValues.asInt(new BigDecimal('1.5')) == null
        assert JsonValues.asInt(new BigDecimal('99999999999999999999')) == null
        assert JsonValues.asInt('1') == null
        assert JsonValues.canonicalId(1) == 1
        assert JsonValues.canonicalId(1.0d) == 1
        assert JsonValues.canonicalId(5000000000L) == 5000000000L
        assert JsonValues.canonicalId(new BigDecimal('5000000000')) == 5000000000L
        def threw = false
        try {
            JsonValues.canonicalId(new BigDecimal('1.5'))
        } catch (ProtocolException ex) {
            threw = ex.code() == -32600
        }
        assert threw
        assert JsonValues.brief(null) == ''
        assert JsonValues.brief('') == ''
        assert JsonValues.brief('ok') == 'ok'
        assert JsonValues.brief('a\nb') == 'a?b'
        assert JsonValues.brief('a\u007fb') == 'a?b'
        assert JsonValues.brief('x' * 80).length() == 64
        assert JsonValues.brief('\uD83D\uDE00' * 80).codePointCount(0, JsonValues.brief('\uD83D\uDE00' * 80).length()) == 64
        assert McpServerConfig.defaults().maxMessageBytes == McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES
        def copied = ['a']
        def config = new McpServerConfig(Path.of('relative-root'), copied)
        copied.clear()
        assert config.classpath() == ['a']
        assert config.root().isAbsolute()
        assert new McpServerConfig(null, null).classpath().isEmpty()
        for (List<Integer> bad : [[0, 1, 1], [1, 0, 1], [1, 1, 0]]) {
            def rejected = false
            try {
                new McpServerConfig(null, null, bad[0], bad[1], bad[2])
            } catch (IllegalArgumentException ignored) {
                rejected = true
            }
            assert rejected
        }
        def hugeId = false
        try {
            JsonValues.canonicalId(new BigDecimal('1E+10000'))
        } catch (ProtocolException ex) {
            hugeId = ex.code() == -32600
        } catch (NumberFormatException ignored) {
            hugeId = true
        }
        assert hugeId
    }

    @Test
    void emptyQuerySymbolsAndBlankMessagesStayWellFormed(@TempDir Path root) {
        server = new GroovyMcpServer(new McpServerConfig(root, List.of()))
        ready(server)
        def listed = tool('groovy_symbols', [text: 'class Alpha {}', query: ''])
        assert listed.structuredContent.symbols.any { it.name == 'Alpha' }
        def queryOnly = tool('groovy_symbols', [query: 'Alpha', maxResults: 1])
        assert queryOnly.structuredContent.symbols.size() <= 1
        def many = tool('groovy_symbols', [text: 'class Alpha {}\nclass Beta {}\n', uri: 'groovy-buffer:many', maxResults: 1])
        assert many.structuredContent.symbols.size() == 1
        assert many.structuredContent.truncated == true
        def unfiltered = tool('groovy_symbols', [maxResults: 5])
        assert unfiltered.structuredContent.symbols.size() >= 1
        def sigSrc = 'class Box {\n    static void ping(String name) {}\n    static void ping(String name, int count) {}\n    void use() { Box.ping(1) }\n}\n'
        def sigLines = sigSrc.readLines()
        int sigLine = sigLines.findIndexOf { it.contains('Box.ping(') }
        int sigChar = sigLines[sigLine].indexOf('Box.ping(') + 'Box.ping('.length()
        def alone = new GroovyMcpServer()
        try {
            ready(alone)
            def helped = call(alone, 'groovy_signatures', [text: sigSrc, line: sigLine, character: sigChar])
            assert helped.structuredContent.signatures.size() >= 1
            assert helped.structuredContent.signatures.any { it.label }
        } finally {
            alone.close()
        }
        def member = 'class A { void m() { "hi". } }\n'
        def completed = tool('groovy_complete', [text: member, line: 0, character: member.indexOf('.') + 1])
        assert completed.structuredContent.completions.size() <= 200
        if (completed.structuredContent.total > 200) {
            assert completed.structuredContent.truncated == true
        }
        def capped = tool('groovy_references', [text: 'class Alpha {}', line: 0, character: 6, maxResults: 1])
        assert capped.structuredContent.locations.size() <= 1
        def blankName = tool('groovy_rename', [text: 'class Alpha {}', line: 0, character: 6, newName: ''])
        assert blankName.isError == true
        def longName = tool('groovy_rename', [text: 'class Alpha {}', line: 0, character: 6, newName: 'N' * 201])
        assert longName.isError == true
        def unicode = tool('groovy_rename', [text: 'class Alpha {}', line: 0, character: 6, newName: 'nämn'])
        assert unicode.structuredContent.newName == 'nämn' || unicode.isError == true
    }

    private Map tool(String name, Map arguments) {
        call(server, name, arguments)
    }

    private static Map call(GroovyMcpServer target, String name, Map arguments) {
        def response = target.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 2, method: 'tools/call',
                params: [name: name, arguments: arguments]]))
        assert response
        assert !response.contains('\n')
        def parsed = json(response)
        assert parsed.result
        parsed.result as Map
    }

    private Map read(String uri) {
        def response = json(server.exchange(JsonOutput.toJson([jsonrpc: '2.0', id: 3, method: 'resources/read',
                params: [uri: uri]])))
        assert response.result
        response.result as Map
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
