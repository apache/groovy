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
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TypeDefinitionCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;

/**
 * Reads optional client capability flags. Missing capabilities mean the
 * conservative LSP default (no {@code LocationLink}, no applyEdit, …).
 */
public final class ClientFeatures {

    private ClientFeatures() {
    }

    public static boolean definitionLinks(final ClientCapabilities capabilities) {
        DefinitionCapabilities definition = text(capabilities) == null ? null : text(capabilities).getDefinition();
        return definition != null && Boolean.TRUE.equals(definition.getLinkSupport());
    }

    public static boolean typeDefinitionLinks(final ClientCapabilities capabilities) {
        TypeDefinitionCapabilities typeDefinition = text(capabilities) == null ? null : text(capabilities).getTypeDefinition();
        return typeDefinition != null && Boolean.TRUE.equals(typeDefinition.getLinkSupport());
    }

    public static boolean implementationLinks(final ClientCapabilities capabilities) {
        ImplementationCapabilities implementation = text(capabilities) == null ? null : text(capabilities).getImplementation();
        return implementation != null && Boolean.TRUE.equals(implementation.getLinkSupport());
    }

    public static boolean declarationLinks(final ClientCapabilities capabilities) {
        DeclarationCapabilities declaration = text(capabilities) == null ? null : text(capabilities).getDeclaration();
        return declaration != null && Boolean.TRUE.equals(declaration.getLinkSupport());
    }

    public static boolean hierarchicalDocumentSymbols(final ClientCapabilities capabilities) {
        DocumentSymbolCapabilities symbols = text(capabilities) == null ? null : text(capabilities).getDocumentSymbol();
        return symbols != null && Boolean.TRUE.equals(symbols.getHierarchicalDocumentSymbolSupport());
    }

    public static boolean applyEdit(final ClientCapabilities capabilities) {
        WorkspaceClientCapabilities workspace = capabilities == null ? null : capabilities.getWorkspace();
        return workspace != null && Boolean.TRUE.equals(workspace.getApplyEdit());
    }

    public static boolean workDoneProgress(final ClientCapabilities capabilities) {
        return capabilities != null && capabilities.getWindow() != null
                && Boolean.TRUE.equals(capabilities.getWindow().getWorkDoneProgress());
    }

    private static TextDocumentClientCapabilities text(final ClientCapabilities capabilities) {
        return capabilities == null ? null : capabilities.getTextDocument();
    }
}
