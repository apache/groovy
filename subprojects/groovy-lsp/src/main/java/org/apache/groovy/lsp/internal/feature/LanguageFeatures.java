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

/**
 * Stateless feature services for one server process. The session
 * ({@code LanguageServerContext}) does not own these.
 */
public final class LanguageFeatures {

    private final CompletionService completions = new CompletionService();
    private final HoverService hovers = new HoverService();
    private final NavigationService navigation = new NavigationService();
    private final SymbolService symbols = new SymbolService();
    private final RenameService rename = new RenameService();
    private final HierarchyService hierarchy = new HierarchyService();
    private final SignatureHelpService signatureHelp = new SignatureHelpService();
    private final HighlightService highlights = new HighlightService();
    private final FoldingService folding = new FoldingService();
    private final FormattingService formatting = new FormattingService();
    private final SemanticTokensService semanticTokens = new SemanticTokensService();
    private final InlayHintService inlays = new InlayHintService();
    private final CodeLensService codeLenses = new CodeLensService();
    private final SelectionRangeService selectionRanges = new SelectionRangeService();
    private final DocumentLinkService documentLinks = new DocumentLinkService();
    private final CodeActionService codeActions = new CodeActionService();

    /**
     * Feature for {@code textDocument/completion} and
     * {@code completionItem/resolve}.
     *
     * @return the completion feature
     */
    public CompletionService completions() {
        return completions;
    }

    /**
     * Feature for {@code textDocument/hover}.
     *
     * @return the hover feature
     */
    public HoverService hovers() {
        return hovers;
    }

    /**
     * Feature for {@code textDocument/definition},
     * {@code textDocument/typeDefinition},
     * {@code textDocument/implementation},
     * {@code textDocument/declaration}, and
     * {@code textDocument/references}. Also answers
     * {@code groovy.lsp.showReferences} and
     * {@code groovy.lsp.gotoSuperMethod}.
     *
     * @return the navigation feature
     */
    public NavigationService navigation() {
        return navigation;
    }

    /**
     * Feature for {@code textDocument/documentSymbol} and
     * {@code workspace/symbol}.
     *
     * @return the symbol feature
     */
    public SymbolService symbols() {
        return symbols;
    }

    /**
     * Feature for {@code textDocument/prepareRename},
     * {@code textDocument/rename}, and {@code workspace/willRenameFiles}.
     *
     * @return the rename feature
     */
    public RenameService rename() {
        return rename;
    }

    /**
     * Feature for {@code textDocument/prepareCallHierarchy},
     * {@code callHierarchy/incomingCalls},
     * {@code callHierarchy/outgoingCalls},
     * {@code textDocument/prepareTypeHierarchy},
     * {@code typeHierarchy/supertypes}, and
     * {@code typeHierarchy/subtypes}.
     *
     * @return the hierarchy feature
     */
    public HierarchyService hierarchy() {
        return hierarchy;
    }

    /**
     * Feature for {@code textDocument/signatureHelp}.
     *
     * @return the signature-help feature
     */
    public SignatureHelpService signatureHelp() {
        return signatureHelp;
    }

    /**
     * Feature for {@code textDocument/documentHighlight}.
     *
     * @return the highlight feature
     */
    public HighlightService highlights() {
        return highlights;
    }

    /**
     * Feature for {@code textDocument/foldingRange}.
     *
     * @return the folding feature
     */
    public FoldingService folding() {
        return folding;
    }

    /**
     * Feature for {@code textDocument/formatting},
     * {@code textDocument/rangeFormatting}, and
     * {@code textDocument/onTypeFormatting}.
     *
     * @return the formatting feature
     */
    public FormattingService formatting() {
        return formatting;
    }

    /**
     * Feature for {@code textDocument/semanticTokens/full} and
     * {@code textDocument/semanticTokens/range}.
     *
     * @return the semantic-token feature
     */
    public SemanticTokensService semanticTokens() {
        return semanticTokens;
    }

    /**
     * Feature for {@code textDocument/inlayHint}.
     *
     * @return the inlay-hint feature
     */
    public InlayHintService inlays() {
        return inlays;
    }

    /**
     * Feature for {@code textDocument/codeLens}.
     *
     * @return the code-lens feature
     */
    public CodeLensService codeLenses() {
        return codeLenses;
    }

    /**
     * Feature for {@code textDocument/selectionRange}.
     *
     * @return the selection-range feature
     */
    public SelectionRangeService selectionRanges() {
        return selectionRanges;
    }

    /**
     * Feature for {@code textDocument/documentLink}.
     *
     * @return the document-link feature
     */
    public DocumentLinkService documentLinks() {
        return documentLinks;
    }

    /**
     * Feature for {@code textDocument/codeAction} and the
     * {@code groovy.lsp.organizeImports} command.
     *
     * @return the code-action feature
     */
    public CodeActionService codeActions() {
        return codeActions;
    }
}
