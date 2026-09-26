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
package org.apache.groovy.lsp.internal.feature

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.LspFixture
import org.eclipse.lsp4j.SymbolKind
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.Parameter
import java.lang.reflect.Modifier
import org.junit.jupiter.api.AfterEach
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.junit.jupiter.api.Test
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.FieldNode

final class DocumentSymbolTest {
    private static final PositionEncoding ENC = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }


    @Test
    void symbolsSkipUnresolvedRangesAndReportKinds() {
        def symbols = new SymbolService()
        assert symbols.documentSymbols(null, ENC).isEmpty()
        assert symbols.documentSymbols(new CompiledDocument(URI.create('file:///none.groovy'), 1, '', null, null, null), ENC).isEmpty()
        assert symbols.workspaceSymbols(null, 'Named', ENC).isEmpty()
        assert symbols.documentSymbolInformation(null, ENC).isEmpty()
        assert SymbolService.kindOf(new ClassNode('Color', Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)) == SymbolKind.Enum
        assert SymbolService.kindOf(new ConstructorNode(Modifier.PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())) == SymbolKind.Constructor
        assert SymbolService.kindOf(new FieldNode('f', 0, ClassHelper.int_TYPE, ClassHelper.OBJECT_TYPE, null)) == SymbolKind.Field
        assert SymbolService.kindOf(new Parameter(ClassHelper.int_TYPE, 'p')) == SymbolKind.Variable

        def src = '''\
            import java.util.List
            class Named {
                String title
                def foo() { title }
            }
            '''.stripIndent()
        def uri = open('Named.groovy', src)
        def doc = compiled(uri)
        def type = typeNamed(doc, 'Named')
        def field = ensureField(doc, type, 'title')
        def top = symbols.documentSymbols(doc, ENC)
        assert top*.name == ['Named']
        assert top[0].children.any { it.name == 'title' && it.kind == SymbolKind.Field }
        def info = symbols.documentSymbolInformation(doc, ENC)
        assert info*.name.containsAll(['Named', 'title', 'foo'])
        assert info.size() < AstQuery.declarations(doc.module).size()
        def extra = plus(snapshot(), new CompiledDocument(URI.create('file:///null-module.groovy'), 1, '', null, null, null))
        assert symbols.workspaceSymbols(extra, 'Named', ENC)*.name.contains('Named')

        field.lineNumber = 0
        type.properties.findAll { it.name == 'title' }.each { it.lineNumber = 0 }
        assert symbols.workspaceSymbols(snapshot(), 'title', ENC).isEmpty()
        assert symbols.workspaceSymbols(snapshot(), 'foo', ENC)*.name.contains('foo')
        def children = symbols.documentSymbols(doc, ENC)[0].children*.name
        assert !children.contains('title')
        assert children.contains('foo')
    }

    private String open(String path, String text) {
        if (fixture == null) {
            fixture = new LspFixture()
        }
        fixture.open(path, text)
    }

    private CompilationSnapshot snapshot() {
        fixture.server.context.snapshot
    }

    private CompiledDocument compiled(String uri) {
        def doc = snapshot().get(uri)
        assert doc?.module != null
        doc
    }

    private static ClassNode typeNamed(CompiledDocument doc, String simple) {
        def found = doc.module.classes.find { it.nameWithoutPackage == simple && !it.name.contains('$') }
        assert found != null : doc.module.classes*.name
        found
    }

    private static CompilationSnapshot plus(CompilationSnapshot base, CompiledDocument... extra) {
        def map = new LinkedHashMap<URI, CompiledDocument>()
        if (base != null) {
            for (CompiledDocument doc : base.documents()) {
                if (doc.uri != null) {
                    map.put(doc.uri, doc)
                }
            }
        }
        for (CompiledDocument doc : extra) {
            def key = doc.uri != null ? doc.uri : URI.create('file:///synthetic-' + System.identityHashCode(doc) + '.groovy')
            map.put(key, doc)
        }
        new CompilationSnapshot(map)
    }

    private static void place(ASTNode node, int line, int column, int lastColumn) {
        node.lineNumber = line
        node.columnNumber = column
        node.lastLineNumber = line
        node.lastColumnNumber = lastColumn
    }

    private static FieldNode ensureField(CompiledDocument doc, ClassNode type, String name) {
        def existing = type.fields.find { it.name == name && !it.synthetic }
        if (existing != null) {
            return existing
        }
        def lines = doc.text.readLines()
        int line = lines.findIndexOf { it.contains(name) }
        assert line >= 0 : name
        int column = lines[line].indexOf(name)
        def field = new FieldNode(name, Modifier.PRIVATE, ClassHelper.STRING_TYPE, type, null)
        place(field, line + 1, column + 1, column + 1 + name.length())
        type.addField(field)
        field
    }
}
