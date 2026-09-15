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
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TypeHierarchyPrepareParams
import org.eclipse.lsp4j.TypeHierarchySubtypesParams
import org.eclipse.lsp4j.TypeHierarchySupertypesParams
import org.eclipse.lsp4j.WorkspaceEdit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class NavigationIdentityTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void hoverAndDefinitionOnCallNameUseTheMethod() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        def hover = fixture.server.textDocumentService.hover(new HoverParams(id, new Position(1, 27))).get()
        assert hover != null
        def value = hover.contents instanceof MarkupContent
                ? hover.contents.value
                : hover.contents.getRight().value
        assert value.contains('foo')
        assert !value.contains('```groovy\nfoo\n```') || value.contains('(')
        def defs = fixture.server.textDocumentService.definition(new DefinitionParams(id, new Position(1, 27))).get()
        assert defs.isRight()
        assert defs.getRight().any { it.targetSelectionRange.start.line == 1 && it.targetSelectionRange.start.character == 8 }
    }

    @Test
    void prepareRenameOnParameterSelectsTheParameter() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            '''.stripIndent())
        def prepare = fixture.server.textDocumentService.prepareRename(
                new PrepareRenameParams(new TextDocumentIdentifier(uri), new Position(1, 20))).get()
        assert prepare != null
        def result = prepare.getSecond()
        assert result.placeholder == 'bar'
        assert result.range.start.character == 19
    }

    @Test
    void renameDoesNotTouchADifferentMethodOfTheSameName() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            class Other {
                def foo() {}
            }
            '''.stripIndent())
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(1, 9), 'renamed')).get()
        def text = edit.changes.values().flatten()*.newText
        assert text.every { it == 'renamed' }
        def ranges = edit.changes.values().flatten()*.range
        assert ranges.every { it.start.line != 4 }
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri), new Position(1, 9), new ReferenceContext(true))).get()
        assert refs.every { it.range.start.line != 4 }
        assert refs.size() >= 2
    }

    @Test
    void signatureHelpUsesEnclosingCallAndActiveArgument() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar, int n) {
                    foo(bar, 1)
                }
            }
            '''.stripIndent())
        // caret after the comma of foo(bar, 1)
        def help = fixture.server.textDocumentService.signatureHelp(
                new SignatureHelpParams(new TextDocumentIdentifier(uri), new Position(2, 16))).get()
        assert help.signatures
        assert help.signatures[0].label.contains('foo')
        assert help.activeParameter == 1
    }

    @Test
    void organizeImportsKeepsStarStaticAndAlias() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.LinkedList as LL
            import java.util.regex.Pattern
            import static java.util.Collections.emptyList
            import java.io.*
            class Hello {}
            '''.stripIndent())
        def edit = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.organizeImports', [uri])).get()
        assert edit instanceof WorkspaceEdit
        def replacement = edit.changes.values().flatten()[0].newText
        assert replacement.contains('import java.io.*')
        assert replacement.contains('import java.util.LinkedList as LL')
        assert replacement.contains('import static java.util.Collections.emptyList')
        assert fixture.appliedEdits
    }

    @Test
    void callAndTypeHierarchyWalkTheSnapshot() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            interface Named {}
            class Hello implements Named {
                def foo() { bar() }
                def bar() {}
            }
            '''.stripIndent())
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        def prepared = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(2, 8))).get()
        assert prepared.size() == 1
        assert prepared[0].name == 'foo'
        def outgoing = tds.callHierarchyOutgoingCalls(new CallHierarchyOutgoingCallsParams(prepared[0])).get()
        assert outgoing.any { it.to.name == 'bar' }
        def bar = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(3, 8))).get()
        def incoming = tds.callHierarchyIncomingCalls(new CallHierarchyIncomingCallsParams(bar[0])).get()
        assert incoming.any { it.from.name == 'foo' }

        def named = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(0, 10))).get()
        assert named[0].name == 'Named'
        def subtypes = tds.typeHierarchySubtypes(new TypeHierarchySubtypesParams(named[0])).get()
        assert subtypes.any { it.name == 'Hello' }
        def hello = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(1, 6))).get()
        def supers = tds.typeHierarchySupertypes(new TypeHierarchySupertypesParams(hello[0])).get()
        assert supers.any { it.name == 'Named' }
    }

    @Test
    void renameDoesNotTouchAFieldOfTheSameNameOnAnotherClass() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                String name
                def go() { name }
            }
            class Other {
                String name
            }
            '''.stripIndent())
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri), new Position(1, 11), new ReferenceContext(true))).get()
        assert refs.every { it.range.start.line != 5 }
        assert refs.size() >= 2
    }

    @Test
    void documentLinkTargetIsAFileUri() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.LinkedList
            class Hello {}
            '''.stripIndent())
        def links = fixture.server.textDocumentService.documentLink(
                new DocumentLinkParams(new TextDocumentIdentifier(uri))).get()
        assert links.every { it.target == null || it.target.contains('://') }
    }

    @Test
    void openDoesNotCompileTheWorkingTree() {
        fixture = new LspFixture()
        def uri = fixture.open('Only.groovy', 'class Only {}\n')
        assert fixture.server.context.snapshot.documents().size() == 1
        assert fixture.server.context.snapshot.get(uri) != null
    }
}
