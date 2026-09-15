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

import org.apache.groovy.lsp.internal.feature.SupportServices;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.eclipse.lsp4j.CallHierarchyRegistrationOptions;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DiagnosticRegistrationOptions;
import org.eclipse.lsp4j.DocumentLinkOptions;
import org.eclipse.lsp4j.DocumentOnTypeFormattingOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileOperationFilter;
import org.eclipse.lsp4j.FileOperationOptions;
import org.eclipse.lsp4j.FileOperationPattern;
import org.eclipse.lsp4j.FileOperationPatternKind;
import org.eclipse.lsp4j.FileOperationsServerCapabilities;
import org.eclipse.lsp4j.FoldingRangeProviderOptions;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TypeHierarchyRegistrationOptions;
import org.eclipse.lsp4j.WorkspaceFoldersOptions;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Advertises LSP 3.18 capabilities. Features that do not apply to Groovy
 * (document color, notebooks, inline completion) are omitted rather than
 * stubbed.
 */
public final class ServerCapabilityFactory {

    /**
     * Builds server capabilities for the negotiated encoding.
     *
     * @param encoding negotiated position encoding
     * @return capabilities
     */
    public ServerCapabilities create(final PositionEncoding encoding) {
        return create(encoding, List.of());
    }

    /**
     * Builds server capabilities including extra execute-command names
     * contributed by plugins.
     *
     * @param encoding negotiated position encoding
     * @param extraCommands plugin command ids
     * @return capabilities
     */
    public ServerCapabilities create(final PositionEncoding encoding, final List<String> extraCommands) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setPositionEncoding(encoding.protocolName());
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);

        CompletionOptions completion = new CompletionOptions(true, List.of(".", "@"));
        capabilities.setCompletionProvider(completion);
        capabilities.setHoverProvider(true);
        capabilities.setSignatureHelpProvider(new SignatureHelpOptions(List.of("(", ",")));
        capabilities.setDefinitionProvider(true);
        capabilities.setTypeDefinitionProvider(true);
        capabilities.setImplementationProvider(true);
        capabilities.setDeclarationProvider(true);
        capabilities.setReferencesProvider(true);
        capabilities.setDocumentHighlightProvider(true);
        capabilities.setDocumentSymbolProvider(true);
        capabilities.setWorkspaceSymbolProvider(true);

        CodeActionOptions codeAction = new CodeActionOptions(List.of(
                CodeActionKind.QuickFix, CodeActionKind.SourceOrganizeImports, CodeActionKind.Source,
                CodeActionKind.SourceFixAll, CodeActionKind.RefactorRewrite,
                "source.generate.accessors", "source.generate.toString",
                "source.generate.hashCodeEquals", "source.generate.constructors"));
        codeAction.setResolveProvider(false);
        capabilities.setCodeActionProvider(codeAction);
        capabilities.setCodeLensProvider(new CodeLensOptions(false));
        capabilities.setDocumentFormattingProvider(true);
        capabilities.setDocumentRangeFormattingProvider(true);
        capabilities.setDocumentOnTypeFormattingProvider(
                new DocumentOnTypeFormattingOptions("\n", List.of("}")));
        capabilities.setDocumentLinkProvider(new DocumentLinkOptions(false));

        RenameOptions rename = new RenameOptions();
        rename.setPrepareProvider(true);
        capabilities.setRenameProvider(rename);

        capabilities.setFoldingRangeProvider(new FoldingRangeProviderOptions());
        capabilities.setSelectionRangeProvider(true);

        SemanticTokensWithRegistrationOptions tokens = new SemanticTokensWithRegistrationOptions(SupportServices.legend());
        tokens.setFull(true);
        tokens.setRange(true);
        capabilities.setSemanticTokensProvider(tokens);

        capabilities.setInlayHintProvider(true);
        capabilities.setDiagnosticProvider(new DiagnosticRegistrationOptions(true, false));
        capabilities.setCallHierarchyProvider(new CallHierarchyRegistrationOptions());
        capabilities.setTypeHierarchyProvider(new TypeHierarchyRegistrationOptions());

        WorkspaceFoldersOptions folders = new WorkspaceFoldersOptions();
        folders.setSupported(true);
        folders.setChangeNotifications(true);
        WorkspaceServerCapabilities workspace = new WorkspaceServerCapabilities();
        workspace.setWorkspaceFolders(folders);
        FileOperationPattern pattern = new FileOperationPattern("**/*.{groovy,gvy,gy,gsh}");
        pattern.setMatches(FileOperationPatternKind.File);
        FileOperationsServerCapabilities fileOps = new FileOperationsServerCapabilities();
        fileOps.setWillRename(new FileOperationOptions(List.of(new FileOperationFilter(pattern, "file"))));
        workspace.setFileOperations(fileOps);
        capabilities.setWorkspace(workspace);

        List<String> commands = new ArrayList<>();
        commands.add(GroovyWorkspaceService.ORGANIZE_IMPORTS);
        commands.add(GroovyWorkspaceService.SHOW_REFERENCES);
        commands.add(GroovyWorkspaceService.GOTO_SUPER_METHOD);
        if (extraCommands != null) {
            for (String command : extraCommands) {
                if (command != null && !command.isBlank() && !commands.contains(command)) {
                    commands.add(command);
                }
            }
        }
        capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(commands));
        return capabilities;
    }
}
