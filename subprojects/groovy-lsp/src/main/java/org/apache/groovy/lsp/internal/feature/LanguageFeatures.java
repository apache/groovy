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

    public CompletionService completions() {
        return completions;
    }

    public HoverService hovers() {
        return hovers;
    }

    public NavigationService navigation() {
        return navigation;
    }

    public SymbolService symbols() {
        return symbols;
    }

    public RenameService rename() {
        return rename;
    }

    public HierarchyService hierarchy() {
        return hierarchy;
    }

    public SignatureHelpService signatureHelp() {
        return signatureHelp;
    }

    public HighlightService highlights() {
        return highlights;
    }

    public FoldingService folding() {
        return folding;
    }

    public FormattingService formatting() {
        return formatting;
    }

    public SemanticTokensService semanticTokens() {
        return semanticTokens;
    }

    public InlayHintService inlays() {
        return inlays;
    }

    public CodeLensService codeLenses() {
        return codeLenses;
    }

    public SelectionRangeService selectionRanges() {
        return selectionRanges;
    }

    public DocumentLinkService documentLinks() {
        return documentLinks;
    }

    public CodeActionService codeActions() {
        return codeActions;
    }
}
