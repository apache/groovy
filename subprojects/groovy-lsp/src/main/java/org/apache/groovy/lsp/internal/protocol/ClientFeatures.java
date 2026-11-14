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
package org.apache.groovy.lsp.internal.protocol;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DeclarationCapabilities;
import org.eclipse.lsp4j.DefinitionCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.ImplementationCapabilities;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TypeDefinitionCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;

/**
 * Reads optional client capability flags. Missing capabilities mean the
 * conservative LSP default (no {@code LocationLink}, no applyEdit, …).
 */
public final class ClientFeatures {

    private ClientFeatures() {
    }

    /**
     * Returns whether definition results may be {@code LocationLink}s.
     * A missing text-document or definition capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when definition link support is explicitly enabled
     */
    public static boolean definitionLinks(final ClientCapabilities capabilities) {
        DefinitionCapabilities definition = text(capabilities) == null ? null : text(capabilities).getDefinition();
        return definition != null && Boolean.TRUE.equals(definition.getLinkSupport());
    }

    /**
     * Returns whether type-definition results may be {@code LocationLink}s.
     * A missing text-document or type-definition capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when type-definition link support is explicitly enabled
     */
    public static boolean typeDefinitionLinks(final ClientCapabilities capabilities) {
        TypeDefinitionCapabilities typeDefinition = text(capabilities) == null ? null : text(capabilities).getTypeDefinition();
        return typeDefinition != null && Boolean.TRUE.equals(typeDefinition.getLinkSupport());
    }

    /**
     * Returns whether implementation results may be {@code LocationLink}s.
     * A missing text-document or implementation capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when implementation link support is explicitly enabled
     */
    public static boolean implementationLinks(final ClientCapabilities capabilities) {
        ImplementationCapabilities implementation = text(capabilities) == null ? null : text(capabilities).getImplementation();
        return implementation != null && Boolean.TRUE.equals(implementation.getLinkSupport());
    }

    /**
     * Returns whether declaration results may be {@code LocationLink}s.
     * A missing text-document or declaration capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when declaration link support is explicitly enabled
     */
    public static boolean declarationLinks(final ClientCapabilities capabilities) {
        DeclarationCapabilities declaration = text(capabilities) == null ? null : text(capabilities).getDeclaration();
        return declaration != null && Boolean.TRUE.equals(declaration.getLinkSupport());
    }

    /**
     * Returns whether document symbols may be returned as a hierarchy
     * rather than a flat list. A missing text-document or document-symbol
     * capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when hierarchical document symbols are explicitly enabled
     */
    public static boolean hierarchicalDocumentSymbols(final ClientCapabilities capabilities) {
        DocumentSymbolCapabilities symbols = text(capabilities) == null ? null : text(capabilities).getDocumentSymbol();
        return symbols != null && Boolean.TRUE.equals(symbols.getHierarchicalDocumentSymbolSupport());
    }

    /**
     * Returns whether the client accepts {@code workspace/applyEdit}.
     * A missing workspace capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when apply-edit is explicitly enabled
     */
    public static boolean applyEdit(final ClientCapabilities capabilities) {
        WorkspaceClientCapabilities workspace = capabilities == null ? null : capabilities.getWorkspace();
        return workspace != null && Boolean.TRUE.equals(workspace.getApplyEdit());
    }

    /**
     * Returns whether the server may pull settings with {@code workspace/configuration}.
     * A missing workspace capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when configuration pull is explicitly enabled
     */
    public static boolean workspaceConfiguration(final ClientCapabilities capabilities) {
        WorkspaceClientCapabilities workspace = capabilities == null ? null : capabilities.getWorkspace();
        return workspace != null && Boolean.TRUE.equals(workspace.getConfiguration());
    }

    /**
     * Returns whether the client accepts work-done progress notifications.
     * A missing window capability is treated as unsupported.
     *
     * @param capabilities client capabilities, or {@code null}
     * @return {@code true} when work-done progress is explicitly enabled
     */
    public static boolean workDoneProgress(final ClientCapabilities capabilities) {
        return capabilities != null && capabilities.getWindow() != null
                && Boolean.TRUE.equals(capabilities.getWindow().getWorkDoneProgress());
    }

    /**
     * @param capabilities client capabilities, possibly {@code null}
     * @return {@code true} when a {@code workspace/applyEdit} can create a file
     */
    public static boolean canCreateFiles(final ClientCapabilities capabilities) {
        WorkspaceClientCapabilities workspace = capabilities == null ? null : capabilities.getWorkspace();
        WorkspaceEditCapabilities edit = workspace == null ? null : workspace.getWorkspaceEdit();
        if (edit == null || !Boolean.TRUE.equals(edit.getDocumentChanges()) || edit.getResourceOperations() == null) {
            return false;
        }
        return edit.getResourceOperations().contains(ResourceOperationKind.Create);
    }

    private static TextDocumentClientCapabilities text(final ClientCapabilities capabilities) {
        return capabilities == null ? null : capabilities.getTextDocument();
    }
}
