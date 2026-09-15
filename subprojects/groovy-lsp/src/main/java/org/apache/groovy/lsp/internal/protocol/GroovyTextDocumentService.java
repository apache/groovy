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

import org.apache.groovy.lsp.internal.LanguageServerContext;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.eclipse.lsp4j.CallHierarchyIncomingCall;
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CallHierarchyOutgoingCall;
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams;
import org.eclipse.lsp4j.CallHierarchyPrepareParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SelectionRange;
import org.eclipse.lsp4j.SelectionRangeParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.TypeHierarchyItem;
import org.eclipse.lsp4j.TypeHierarchyPrepareParams;
import org.eclipse.lsp4j.TypeHierarchySubtypesParams;
import org.eclipse.lsp4j.TypeHierarchySupertypesParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dispatches {@code textDocument/*} requests to feature services.
 */
public final class GroovyTextDocumentService implements TextDocumentService {

    private final LanguageServerContext context;

    public GroovyTextDocumentService(final LanguageServerContext context) {
        this.context = context;
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        context.getDocuments().open(params.getTextDocument());
        context.scheduleRecompile(50);
    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
        context.getDocuments().change(params.getTextDocument().getUri(), params.getTextDocument().getVersion(),
                params.getContentChanges(), context.getPositionEncoding());
        context.scheduleRecompile(150);
    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        context.getDocuments().close(uri);
        if (context.getClient() != null) {
            context.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, List.of()));
        }
        context.scheduleRecompile(50);
    }

    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
        context.scheduleRecompile(0);
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(final CompletionParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(Either.forRight(new CompletionList(false, List.of())));
        }
        CompletionList list = context.getCompletions().complete(document, context.getSnapshot(), params.getPosition(),
                context.getPositionEncoding());
        return CompletableFuture.completedFuture(Either.forRight(list));
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(final CompletionItem unresolved) {
        return CompletableFuture.completedFuture(context.getCompletions().resolve(unresolved, context.getSnapshot()));
    }

    @Override
    public CompletableFuture<Hover> hover(final HoverParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(
                context.getHovers().hover(document, context.getSnapshot(), params.getPosition(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(final SignatureHelpParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(new SignatureHelp(List.of(), 0, 0));
        }
        return CompletableFuture.completedFuture(
                context.getSupport().signatureHelp(document, context.getSnapshot(), params.getPosition(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            final DefinitionParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return emptyLinks();
        }
        return asLocations(context.getNavigation().definition(document, context.getSnapshot(), params.getPosition(),
                context.getPositionEncoding()), ClientFeatures.definitionLinks(context.getClientCapabilities()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(
            final TypeDefinitionParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return emptyLinks();
        }
        return asLocations(context.getNavigation().typeDefinition(document, context.getSnapshot(), params.getPosition(),
                context.getPositionEncoding()), ClientFeatures.typeDefinitionLinks(context.getClientCapabilities()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(
            final ImplementationParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return emptyLinks();
        }
        return asLocations(context.getNavigation().implementation(document, context.getSnapshot(), params.getPosition(),
                context.getPositionEncoding()), ClientFeatures.implementationLinks(context.getClientCapabilities()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(
            final DeclarationParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return emptyLinks();
        }
        return asLocations(context.getNavigation().declaration(document, context.getSnapshot(), params.getPosition(),
                context.getPositionEncoding()), ClientFeatures.declarationLinks(context.getClientCapabilities()));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(final ReferenceParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        boolean include = params.getContext() == null || params.getContext().isIncludeDeclaration();
        return CompletableFuture.completedFuture(
                context.getNavigation().references(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding(), include));
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(final DocumentHighlightParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.completedFuture(
                context.getSupport().highlights(document, context.getSnapshot(), params.getPosition(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
            final DocumentSymbolParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
        if (ClientFeatures.hierarchicalDocumentSymbols(context.getClientCapabilities())) {
            for (DocumentSymbol symbol : context.getSymbols().documentSymbols(compiled, context.getPositionEncoding())) {
                result.add(Either.forRight(symbol));
            }
        } else {
            for (SymbolInformation symbol : context.getSymbols().documentSymbolInformation(compiled, context.getPositionEncoding())) {
                result.add(Either.forLeft(symbol));
            }
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(final CodeActionParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<Diagnostic> diagnostics = params.getContext() == null
                ? List.of() : params.getContext().getDiagnostics();
        List<Either<Command, CodeAction>> actions = new ArrayList<>(
                context.getSupport().codeActions(document, context.getSnapshot(),
                        diagnostics, params.getRange(), context.getPositionEncoding(),
                        context.getWorkspaceFolders(), context.getSettings().getSourcePaths()));
        actions.addAll(context.getExtensions().extraCodeActions(document, context.getSnapshot(),
                diagnostics, params.getRange(), context.getPositionEncoding()));
        return CompletableFuture.completedFuture(actions);
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(final CodeLensParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(
                context.getSupport().codeLenses(compiled, context.getSnapshot(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(final DocumentFormattingParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        int tab = params.getOptions() == null ? 4 : params.getOptions().getTabSize();
        boolean spaces = params.getOptions() == null || params.getOptions().isInsertSpaces();
        return CompletableFuture.completedFuture(
                context.getSupport().format(document, null, tab, spaces, context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(final DocumentRangeFormattingParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        int tab = params.getOptions() == null ? 4 : params.getOptions().getTabSize();
        boolean spaces = params.getOptions() == null || params.getOptions().isInsertSpaces();
        return CompletableFuture.completedFuture(
                context.getSupport().format(document, params.getRange(), tab, spaces, context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(final DocumentOnTypeFormattingParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        int tab = params.getOptions() == null ? 4 : params.getOptions().getTabSize();
        boolean spaces = params.getOptions() == null || params.getOptions().isInsertSpaces();
        return CompletableFuture.completedFuture(context.getSupport().onTypeFormat(document, params.getPosition(),
                params.getCh(), tab, spaces));
    }

    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(
            final PrepareRenameParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(null);
        }
        Either<Range, PrepareRenameResult> result = context.getRename().prepareRename(
                document, context.getSnapshot(), params.getPosition(), context.getPositionEncoding());
        if (result == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (result.isLeft()) {
            return CompletableFuture.completedFuture(Either3.forFirst(result.getLeft()));
        }
        return CompletableFuture.completedFuture(Either3.forSecond(result.getRight()));
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(final RenameParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(new WorkspaceEdit(Map.of()));
        }
        return CompletableFuture.completedFuture(
                context.getRename().rename(document, context.getSnapshot(), params.getPosition(), params.getNewName(),
                        context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(final FoldingRangeRequestParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(context.getSupport().folding(compiled));
    }

    @Override
    public CompletableFuture<List<SelectionRange>> selectionRange(final SelectionRangeParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.completedFuture(
                context.getSupport().selectionRanges(document, context.getSnapshot(), params.getPositions(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(final SemanticTokensParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(context.getSupport().semanticTokens(compiled, context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensRange(final SemanticTokensRangeParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(
                context.getSupport().semanticTokens(compiled, context.getPositionEncoding(), params.getRange()));
    }

    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(final InlayHintParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(
                context.getSupport().inlayHints(compiled, params.getRange(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<DocumentLink>> documentLink(final DocumentLinkParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        return CompletableFuture.completedFuture(
                context.getSupport().documentLinks(compiled, context.getSnapshot(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(final DocumentDiagnosticParams params) {
        CompiledDocument compiled = context.getSnapshot().get(params.getTextDocument().getUri());
        RelatedFullDocumentDiagnosticReport report = new RelatedFullDocumentDiagnosticReport(
                context.diagnosticsFor(compiled));
        return CompletableFuture.completedFuture(new DocumentDiagnosticReport(report));
    }

    @Override
    public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(final CallHierarchyPrepareParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.completedFuture(
                context.getHierarchy().prepareCallHierarchy(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(
            final CallHierarchyIncomingCallsParams params) {
        return CompletableFuture.completedFuture(context.getHierarchy().incomingCalls(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(
            final CallHierarchyOutgoingCallsParams params) {
        return CompletableFuture.completedFuture(context.getHierarchy().outgoingCalls(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<TypeHierarchyItem>> prepareTypeHierarchy(final TypeHierarchyPrepareParams params) {
        TextDocument document = require(params.getTextDocument().getUri());
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.completedFuture(
                context.getHierarchy().prepareTypeHierarchy(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySupertypes(final TypeHierarchySupertypesParams params) {
        return CompletableFuture.completedFuture(context.getHierarchy().supertypes(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    @Override
    public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySubtypes(final TypeHierarchySubtypesParams params) {
        return CompletableFuture.completedFuture(context.getHierarchy().subtypes(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    private TextDocument require(final String uri) {
        TextDocument document = context.getDocuments().get(uri);
        if (document != null) {
            return document;
        }
        CompiledDocument compiled = context.getSnapshot().get(uri);
        if (compiled == null) {
            return null;
        }
        return new TextDocument(compiled.getUri(), "groovy", compiled.getVersion(), compiled.getText());
    }

    private static CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> emptyLinks() {
        return CompletableFuture.completedFuture(Either.forRight(List.of()));
    }

    private static CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> asLocations(
            final List<LocationLink> links, final boolean linkSupport) {
        if (linkSupport) {
            return CompletableFuture.completedFuture(Either.forRight(links));
        }
        List<Location> locations = new ArrayList<>();
        for (LocationLink link : links) {
            locations.add(new Location(link.getTargetUri(), link.getTargetSelectionRange()));
        }
        return CompletableFuture.completedFuture(Either.forLeft(locations));
    }
}
