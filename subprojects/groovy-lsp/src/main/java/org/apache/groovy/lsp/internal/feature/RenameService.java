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
package org.apache.groovy.lsp.internal.feature;

import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SnippetTextEdit;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepare-rename and rename using reference locations. Calls whose
 * {@code methodTarget} is empty are refused: a name-and-arity guess is
 * good enough for hover, not for rewriting source.
 */
public final class RenameService {

    private static final String GROOVY_EXTENSION = ".groovy";

    private final NavigationService navigation = new NavigationService();

    /**
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return the range of the identifier, or {@code null} when it cannot be renamed
     */
    public Either<Range, PrepareRenameResult> prepareRename(final TextDocument document,
                                                            final CompilationSnapshot snapshot,
                                                            final Position position,
                                                            final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return null;
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        if (!NavigationService.isRenameSafe(node, snapshot)) {
            return null;
        }
        String name = NavigationService.nameOf(node);
        if (name == null) {
            return null;
        }
        Range range = Positions.toIdentifierRange(node, document.getText(), encoding);
        if (range == null) {
            return null;
        }
        return Either.forRight(new PrepareRenameResult(range, name));
    }

    /**
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param newName replacement identifier
     * @param encoding negotiated encoding
     * @return a workspace edit
     */
    public WorkspaceEdit rename(final TextDocument document, final CompilationSnapshot snapshot,
                                final Position position, final String newName, final PositionEncoding encoding) {
        if (newName == null || newName.isBlank() || !isIdentifier(newName)) {
            return new WorkspaceEdit(Map.of());
        }
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return new WorkspaceEdit(Map.of());
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        if (!NavigationService.isRenameSafe(node, snapshot)) {
            return new WorkspaceEdit(Map.of());
        }
        List<Location> locations = navigation.references(document, snapshot, position, encoding, true);
        Map<String, List<TextEdit>> changes = new HashMap<>();
        for (Location location : locations) {
            changes.computeIfAbsent(location.getUri(), key -> new ArrayList<>())
                    .add(new TextEdit(location.getRange(), newName));
        }
        WorkspaceEdit edit = new WorkspaceEdit(changes);
        addFileRename(edit, document, node, newName, changes);
        return edit;
    }

    private static void addFileRename(final WorkspaceEdit edit, final TextDocument document, final ASTNode node,
                                      final String newName, final Map<String, List<TextEdit>> changes) {
        if (!(node instanceof ClassNode classNode)) {
            return;
        }
        String fileName = Uris.fileName(document.getUri());
        if (!fileName.endsWith(GROOVY_EXTENSION)) {
            return;
        }
        String stem = fileName.substring(0, fileName.length() - GROOVY_EXTENSION.length());
        if (!stem.equals(classNode.getNameWithoutPackage())) {
            return;
        }
        String oldUri = document.getUri().toString();
        int slash = Math.max(oldUri.lastIndexOf('/'), oldUri.lastIndexOf('\\'));
        if (slash < 0) {
            return;
        }
        String newUri = oldUri.substring(0, slash + 1) + newName + GROOVY_EXTENSION;
        List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
        for (Map.Entry<String, List<TextEdit>> entry : changes.entrySet()) {
            List<Either<TextEdit, SnippetTextEdit>> eitherEdits = new ArrayList<>();
            for (TextEdit textEdit : entry.getValue()) {
                eitherEdits.add(Either.forLeft(textEdit));
            }
            documentChanges.add(Either.forLeft(new TextDocumentEdit(
                    new VersionedTextDocumentIdentifier(entry.getKey(), null), eitherEdits)));
        }
        documentChanges.add(Either.forRight(new RenameFile(oldUri, newUri)));
        edit.setDocumentChanges(documentChanges);
    }

    static boolean isIdentifier(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int first = name.codePointAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }
        int i = Character.charCount(first);
        while (i < name.length()) {
            int cp = name.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp)) {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }
}
