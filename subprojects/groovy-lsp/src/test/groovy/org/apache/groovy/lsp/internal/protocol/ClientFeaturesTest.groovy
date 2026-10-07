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
package org.apache.groovy.lsp.internal.protocol

import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.DeclarationCapabilities
import org.eclipse.lsp4j.DefinitionCapabilities
import org.eclipse.lsp4j.DocumentSymbolCapabilities
import org.eclipse.lsp4j.ImplementationCapabilities
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TypeDefinitionCapabilities
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.junit.jupiter.api.Test

final class ClientFeaturesTest {

    @Test
    void clientFeaturesReadOptionalFlags() {
        assert !ClientFeatures.definitionLinks(null)
        assert !ClientFeatures.typeDefinitionLinks(null)
        assert !ClientFeatures.implementationLinks(null)
        assert !ClientFeatures.declarationLinks(null)
        assert !ClientFeatures.hierarchicalDocumentSymbols(null)
        assert !ClientFeatures.applyEdit(null)
        assert !ClientFeatures.workspaceConfiguration(null)
        assert !ClientFeatures.workDoneProgress(null)
        def caps = new ClientCapabilities()
        assert !ClientFeatures.definitionLinks(caps)
        def text = new TextDocumentClientCapabilities()
        text.definition = new DefinitionCapabilities(false, true)
        text.typeDefinition = new TypeDefinitionCapabilities(false, true)
        text.implementation = new ImplementationCapabilities(false, true)
        text.declaration = new DeclarationCapabilities(false, true)
        def symbols = new DocumentSymbolCapabilities()
        symbols.hierarchicalDocumentSymbolSupport = true
        text.documentSymbol = symbols
        caps.textDocument = text
        def workspace = new WorkspaceClientCapabilities()
        workspace.applyEdit = true
        workspace.configuration = true
        caps.workspace = workspace
        def window = new WindowClientCapabilities()
        window.workDoneProgress = true
        caps.window = window
        assert ClientFeatures.workDoneProgress(caps)
        assert ClientFeatures.definitionLinks(caps)
        assert ClientFeatures.typeDefinitionLinks(caps)
        assert ClientFeatures.implementationLinks(caps)
        assert ClientFeatures.declarationLinks(caps)
        assert ClientFeatures.hierarchicalDocumentSymbols(caps)
        assert ClientFeatures.applyEdit(caps)
        assert ClientFeatures.workspaceConfiguration(caps)
        assert ClientFeatures.workDoneProgress(caps)
    }


    @Test
    void privateConstructorCanBeInvoked() {
        [ClientFeatures].each { Class type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            ctor.newInstance()
        }
    }
}
