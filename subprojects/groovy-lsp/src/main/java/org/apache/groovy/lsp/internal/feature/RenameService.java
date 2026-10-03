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
import org.apache.groovy.lsp.internal.compile.Identifiers;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.lsp4j.FileRename;
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

import java.net.URI;
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
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return null;
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        if (!MethodBinding.isRenameSafe(node, snapshot)) {
            return null;
        }
        String name = Identifiers.nameOf(node);
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
        ASTNode node = renameTarget(document, snapshot, position, newName, encoding);
        if (node == null) {
            return emptyEdit();
        }
        Map<String, List<TextEdit>> changes = referenceEdits(document, snapshot, position, encoding, newName);
        WorkspaceEdit edit = new WorkspaceEdit(changes);
        addFileRename(edit, document, node, newName, changes);
        return edit;
    }

    /**
     * Inverse of class→file rename: rewrite the type whose name equals the
     * old stem. The client already moves the file; this returns text edits
     * only.
     *
     * @param files rename pairs
     * @param snapshot latest compile
     * @param encoding negotiated encoding
     * @return a workspace edit, never {@code null}
     */
    public WorkspaceEdit willRenameFiles(final List<FileRename> files, final CompilationSnapshot snapshot,
                                         final PositionEncoding encoding) {
        WorkspaceEdit combined = new WorkspaceEdit(new HashMap<>());
        if (files == null || snapshot == null) {
            return combined;
        }
        for (FileRename file : files) {
            if (file == null) {
                continue;
            }
            WorkspaceEdit one = willRenameFile(file.getOldUri(), file.getNewUri(), snapshot, encoding);
            if (one.getChanges() != null) {
                one.getChanges().forEach((uri, edits) ->
                        combined.getChanges().computeIfAbsent(uri, key -> new ArrayList<>()).addAll(edits));
            }
        }
        return combined;
    }

    private WorkspaceEdit willRenameFile(final String oldUriString, final String newUriString,
                                         final CompilationSnapshot snapshot, final PositionEncoding encoding) {
        if (oldUriString == null || newUriString == null) {
            return emptyEdit();
        }
        URI oldUri = Uris.parse(oldUriString);
        URI newUri = Uris.parse(newUriString);
        String oldName = Uris.fileName(oldUri);
        String newName = Uris.fileName(newUri);
        String oldExt = Uris.groovyExtension(oldName);
        String newExt = Uris.groovyExtension(newName);
        if (oldExt == null || newExt == null || !oldExt.equals(newExt)) {
            return emptyEdit();
        }
        String oldStem = oldName.substring(0, oldName.length() - oldExt.length());
        String newStem = newName.substring(0, newName.length() - newExt.length());
        if (!isIdentifier(newStem) || !sameDirectory(oldUri, newUri)) {
            return emptyEdit();
        }
        CompiledDocument compiled = snapshot.get(oldUri);
        if (compiled == null || compiled.getModule() == null) {
            return emptyEdit();
        }
        ClassNode type = uniqueStemClass(compiled.getModule(), oldStem);
        if (type == null) {
            return emptyEdit();
        }
        Range nameRange = Positions.toNameRange(type, compiled.getText(), encoding);
        if (nameRange == null) {
            return emptyEdit();
        }
        TextDocument document = compiled.toTextDocument();
        return renameText(document, snapshot, nameRange.getStart(), newStem, encoding);
    }

    private WorkspaceEdit renameText(final TextDocument document, final CompilationSnapshot snapshot,
                                     final Position position, final String newName,
                                     final PositionEncoding encoding) {
        if (renameTarget(document, snapshot, position, newName, encoding) == null) {
            return emptyEdit();
        }
        return new WorkspaceEdit(referenceEdits(document, snapshot, position, encoding, newName));
    }

    private ASTNode renameTarget(final TextDocument document, final CompilationSnapshot snapshot,
                                 final Position position, final String newName,
                                 final PositionEncoding encoding) {
        if (document == null || newName == null || newName.isBlank() || !isIdentifier(newName)) {
            return null;
        }
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return null;
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        return MethodBinding.isRenameSafe(node, snapshot) ? node : null;
    }

    private Map<String, List<TextEdit>> referenceEdits(final TextDocument document,
                                                       final CompilationSnapshot snapshot,
                                                       final Position position,
                                                       final PositionEncoding encoding,
                                                       final String newName) {
        List<Location> locations = navigation.references(document, snapshot, position, encoding, true);
        Map<String, List<TextEdit>> changes = new HashMap<>();
        for (Location location : locations) {
            changes.computeIfAbsent(location.getUri(), key -> new ArrayList<>())
                    .add(new TextEdit(location.getRange(), newName));
        }
        return changes;
    }

    private static WorkspaceEdit emptyEdit() {
        return new WorkspaceEdit(Map.of());
    }

    private static ClassNode uniqueStemClass(final ModuleNode module, final String stem) {
        ClassNode match = null;
        for (ClassNode classNode : module.getClasses()) {
            if (classNode instanceof InnerClassNode || classNode.isScript() || classNode.getLineNumber() <= 0) {
                continue;
            }
            if (stem.equals(classNode.getNameWithoutPackage())) {
                if (match != null) {
                    return null;
                }
                match = classNode;
            }
        }
        return match;
    }

    private static boolean sameDirectory(final URI oldUri, final URI newUri) {
        String oldPath = oldUri.getPath();
        String newPath = newUri.getPath();
        if (oldPath == null || newPath == null) {
            return false;
        }
        int oldSlash = Uris.lastSeparator(oldPath);
        int newSlash = Uris.lastSeparator(newPath);
        if (oldSlash < 0 || newSlash < 0) {
            return false;
        }
        return oldPath.substring(0, oldSlash).equals(newPath.substring(0, newSlash));
    }

    private static void addFileRename(final WorkspaceEdit edit, final TextDocument document, final ASTNode node,
                                      final String newName, final Map<String, List<TextEdit>> changes) {
        if (!(node instanceof ClassNode classNode)) {
            return;
        }
        String fileName = Uris.fileName(document.getUri());
        String extension = Uris.groovyExtension(fileName);
        if (extension == null) {
            return;
        }
        String stem = fileName.substring(0, fileName.length() - extension.length());
        if (!stem.equals(classNode.getNameWithoutPackage())) {
            return;
        }
        String oldUri = document.getUri().toString();
        int slash = Uris.lastSeparator(oldUri);
        if (slash < 0) {
            return;
        }
        String newUri = oldUri.substring(0, slash + 1) + newName + extension;
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
