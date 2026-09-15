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

import org.apache.groovy.lsp.internal.LspFixture
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class JdtInspiredTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void generateAccessorsForPrivateField() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                private String name
            }
            '''.stripIndent())
        def actions = titles(uri, 1, 20)
        assert 'Generate getters and setters' in actions
        def edit = editFor(uri, 1, 20, 'Generate getters and setters')
        assert edit.contains('getName')
        assert edit.contains('setName')
    }

    @Test
    void generateToStringAndConstructor() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                String name
            }
            '''.stripIndent())
        def actions = titles(uri, 0, 6)
        assert 'Generate toString()' in actions
        assert 'Generate constructor' in actions
        assert 'Generate equals() and hashCode()' in actions
        def toString = editFor(uri, 0, 6, 'Generate toString()')
        assert toString.contains('toString')
        assert toString.contains('name:')
        def ctor = editFor(uri, 0, 6, 'Generate constructor')
        assert ctor.contains('Hello(String name)')
    }

    @Test
    void skipToStringWhenCanonicalPresent() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            @groovy.transform.Canonical
            class Hello {
                String name
            }
            '''.stripIndent())
        def actions = titles(uri, 1, 6)
        assert !('Generate toString()' in actions)
        assert !('Generate constructor' in actions)
    }

    @Test
    void addOverrideAnnotation() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Base { String name() { 'x' } }
            class Hello extends Base {
                String name() { 'y' }
            }
            '''.stripIndent())
        def edit = editFor(uri, 2, 11, 'Add @Override annotations')
        assert edit.contains('@Override')
    }

    @Test
    void codeLensShowsReferenceCount() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() { foo() }
            }
            '''.stripIndent())
        def lenses = fixture.server.textDocumentService.codeLens(
                new CodeLensParams(new TextDocumentIdentifier(uri))).get()
        assert lenses.any { it.command?.title ==~ /\d+ references?/ }
    }

    @Test
    void renameClassRenamesFileWhenNamesMatch() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(0, 6), 'World')).get()
        assert edit.documentChanges
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.contains('World.groovy') }
    }

    private List<String> titles(String uri, int line, int character) {
        fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(line, character), new Position(line, character + 1)),
                        new CodeActionContext([]))).get()
                .findAll { it.isRight() }
                .collect { it.getRight().title }
    }

    private String editFor(String uri, int line, int character, String title) {
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(line, character), new Position(line, character + 1)),
                        new CodeActionContext([]))).get()
        def action = actions.find { it.isRight() && it.getRight().title == title }
        assert action != null
        action.getRight().edit.changes.values().flatten()[0].newText
    }
}
