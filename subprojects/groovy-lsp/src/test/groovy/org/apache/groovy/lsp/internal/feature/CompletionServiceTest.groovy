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

import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Test

final class CompletionServiceTest {

    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    @Test
    void descriptorTypeNamesCoverJvmForms() {
        assert CompletionService.typeName(named('[B')) == 'byte[]'
        assert CompletionService.typeName(named('[C')) == 'char[]'
        assert CompletionService.typeName(named('[D')) == 'double[]'
        assert CompletionService.typeName(named('[F')) == 'float[]'
        assert CompletionService.typeName(named('[I')) == 'int[]'
        assert CompletionService.typeName(named('[J')) == 'long[]'
        assert CompletionService.typeName(named('[S')) == 'short[]'
        assert CompletionService.typeName(named('[Z')) == 'boolean[]'
        assert CompletionService.typeName(named('[[I')) == 'int[][]'
        assert CompletionService.typeName(named('[Ljava.lang.String;')) == 'String[]'
        assert CompletionService.typeName(named('[Ljava.util.Map$Entry;')) == 'Map.Entry[]'
        assert CompletionService.typeName(named('[Ljava/util/List;')) == 'List[]'
        assert CompletionService.typeName(named('[Q')).contains('[]')
        assert CompletionService.typeName(named('[')) == '['
        assert CompletionService.typeName(named('')) == 'Object'
        assert CompletionService.typeName(named('java.lang.String')) == 'String'
    }

    @Test
    void memberCompletionReachesReceiversArraysAndRepairs() {
        def service = new CompletionService()
        labels(service, '''\
            int[] items = new int[1]
            items.
            '''.stripIndent(), 'items.')
        labels(service, '''\
            int n = 1
            n.
            '''.stripIndent(), 'n.')
        labels(service, '''\
            def n = 1.0
            n.
            '''.stripIndent(), 'n.')
        labels(service, 'BigDecimal.\n', 'BigDecimal.')
        labels(service, 'BigInteger.\n', 'BigInteger.')
        labels(service, 'String.\n', 'String.')
        labels(service, '''\
            def s = new String('hi')
            s.
            '''.stripIndent(), 's.')
        labels(service, '''\
            def s = 'hi'.toUpperCase()
            s.
            '''.stripIndent(), 's.')
        def chained = labels(service, '''\
            class Box { String name }
            def b = new Box()
            b.name.
            '''.stripIndent(), 'b.name.')
        assert 'substring' in chained || 'length' in chained
        labels(service, 'null.\n', 'null.')
        labels(service, '.\n', '.')
        labels(service, '''\
            import java.util.*
            Collections.
            '''.stripIndent(), 'Collections.')
        labels(service, 'NoSuchType.\n', 'NoSuchType.')
        labels(service, "def a = ''\r\na.\n", 'a.')
        def wide = new TextDocument(fileUri('file:///tmp/cov-wide.groovy'), 'groovy', 1, 'a.\n')
        service.complete(wide, compileOf(wide), new Position(0, 80), UTF16)

        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'groovy.lang.Script'
        config.addCompilationCustomizers(new ImportCustomizer())
        def broken = new TextDocument(fileUri('file:///tmp/cov-repair.groovy'), 'groovy', 1, 'def a.\n')
        service.complete(broken, null, new Position(0, 6), UTF16, CompletionServiceTest.class.classLoader, config)

        labels(service, "def a = ''\na?.\n", 'a?.')
        labels(service, "def a = []\na*.\n", 'a*.')
        labels(service, '''\
            def a = ''
            a = a
            a.
            '''.stripIndent(), 'a.')
        labels(service, '''\
            class Box {
                public String name
                String title
                String getCity() { title }
            }
            def b = new Box()
            b.name.
            '''.stripIndent(), 'b.name.')

        def syntax = SyntaxView.tokens('def x = 1 /*c*/')
        assert syntax.any { it.name }
        def operators = SyntaxView.tokens('a + (b)')
        assert operators.any { it.text == '+' || it.text == '(' }
        assert SyntaxView.tokens('').empty
        assert SyntaxView.tokens(null).empty
        def tree = SyntaxView.cst('class C { }')
        assert tree.name
        def operatorTree = SyntaxView.cst('a + (b)')
        assert operatorTree.name
        assert SyntaxView.cst(null).name
        assert SyntaxView.cst('\u0000').name
    }

    private static ClassNode named(String name) {
        new ClassNode(name, 0, ClassHelper.OBJECT_TYPE)
    }

    private static List<String> labels(CompletionService service, String text, String token) {
        def document = new TextDocument(fileUri('file:///tmp/cov-' + token.hashCode() + '.groovy'), 'groovy', 1, text)
        def items = service.complete(document, compileOf(document), positionOf(text, token), UTF16).items
        items*.label
    }

    private static Position positionOf(String text, String token) {
        def lines = text.readLines()
        int line = 0
        int column = token.length()
        for (int i = 0; i < lines.size(); i++) {
            int at = lines[i].indexOf(token)
            if (at >= 0) {
                line = i
                column = at + token.length()
            }
        }
        new Position(line, column)
    }

    private static Object compileOf(TextDocument document) {
        def compiler = new GroovyCompiler()
        try {
            return compiler.compile([document], [], CompilerSettings.defaults(), CompletionServiceTest.class.classLoader)
        } finally {
            compiler.close()
        }
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }
}

