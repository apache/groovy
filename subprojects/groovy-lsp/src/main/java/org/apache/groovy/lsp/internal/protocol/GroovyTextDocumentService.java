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
import org.apache.groovy.lsp.internal.feature.LanguageFeatures;
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
import org.eclipse.lsp4j.CreateFile;
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
import org.eclipse.lsp4j.FormattingOptions;
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
import java.util.function.Function;

/**
 * Dispatches {@code textDocument/*} requests to feature services.
 */
public final class GroovyTextDocumentService implements TextDocumentService {

    private final LanguageServerContext context;
    private final LanguageFeatures features;

    /**
     * Binds {@code textDocument/*} dispatch to one session and its features.
     *
     * @param context session state the notifications update
     * @param features feature objects that answer the requests
     */
    public GroovyTextDocumentService(final LanguageServerContext context, final LanguageFeatures features) {
        this.context = context;
        this.features = features;
    }

    /**
     * Handles {@code textDocument/didOpen} by storing the document on the
     * session and scheduling a recompile after 50 milliseconds.
     *
     * @param params open notification
     */
    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        context.getDocuments().open(params.getTextDocument());
        context.scheduleRecompile(50);
    }

    /**
     * Handles {@code textDocument/didChange} by applying the edits to the
     * open document and scheduling a recompile after 150 milliseconds.
     *
     * @param params change notification
     */
    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
        context.getDocuments().change(params.getTextDocument().getUri(), params.getTextDocument().getVersion(),
                params.getContentChanges(), context.getPositionEncoding());
        context.scheduleRecompile(150);
    }

    /**
     * Handles {@code textDocument/didClose} by dropping the document,
     * publishing an empty diagnostic list when a client is connected, and
     * scheduling a recompile after 50 milliseconds.
     *
     * @param params close notification
     */
    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        context.getDocuments().close(uri);
        if (context.getClient() != null) {
            context.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, List.of()));
        }
        context.scheduleRecompile(50);
    }

    /**
     * Handles {@code textDocument/didSave} by scheduling a recompile
     * with no debounce delay.
     *
     * @param params save notification
     */
    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
        context.scheduleRecompile(0);
    }

    /**
     * Handles {@code textDocument/completion}. Recompiles, then delegates
     * to {@link LanguageFeatures#completions()} and returns that
     * {@link CompletionList} on the right of the {@link Either}. The list
     * is empty when the document is missing.
     *
     * @param params completion request
     * @return the completion list
     */
    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(final CompletionParams params) {
        context.recompile();
        return withDocument(params.getTextDocument().getUri(), Either.forRight(new CompletionList(false, List.of())),
                document -> Either.forRight(features.completions().complete(document, context.getSnapshot(),
                        params.getPosition(), context.getPositionEncoding(),
                        context.getParentLoader(), context.getHostConfiguration(), context.getSettings())));
    }

    /**
     * Handles {@code completionItem/resolve} by delegating to
     * {@link LanguageFeatures#completions()} with the current compilation
     * snapshot.
     *
     * @param unresolved item from a prior completion
     * @return the resolved item
     */
    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(final CompletionItem unresolved) {
        return CompletableFuture.completedFuture(features.completions().resolve(unresolved, context.getSnapshot()));
    }

    /**
     * Handles {@code textDocument/hover}. Recompiles, then delegates to
     * {@link LanguageFeatures#hovers()}. Returns {@code null} when the
     * document is missing.
     *
     * @param params hover request
     * @return hover contents, or {@code null}
     */
    @Override
    public CompletableFuture<Hover> hover(final HoverParams params) {
        context.recompile();
        return withDocument(params.getTextDocument().getUri(), null,
                document -> features.hovers().hover(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/signatureHelp}. Recompiles, then
     * delegates to {@link LanguageFeatures#signatureHelp()}. Returns an
     * empty {@link SignatureHelp} when the document is missing.
     *
     * @param params signature-help request
     * @return signature help
     */
    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(final SignatureHelpParams params) {
        context.recompile();
        return withDocument(params.getTextDocument().getUri(), new SignatureHelp(List.of(), 0, 0),
                document -> features.signatureHelp().signatureHelp(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/definition} by delegating to
     * {@link LanguageFeatures#navigation()}. Returns an empty link list
     * when the document is missing. When it is present, a client with link
     * support receives {@link LocationLink}s and any other client receives
     * {@link Location}s.
     *
     * @param params definition request
     * @return locations or location links
     */
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            final DefinitionParams params) {
        return withLinks(params.getTextDocument().getUri(),
                document -> features.navigation().definition(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()),
                ClientFeatures.definitionLinks(context.getClientCapabilities()));
    }

    /**
     * Handles {@code textDocument/typeDefinition} by delegating to
     * {@link LanguageFeatures#navigation()}. Returns an empty link list
     * when the document is missing. When it is present, a client with link
     * support receives {@link LocationLink}s and any other client receives
     * {@link Location}s.
     *
     * @param params type-definition request
     * @return locations or location links
     */
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(
            final TypeDefinitionParams params) {
        return withLinks(params.getTextDocument().getUri(),
                document -> features.navigation().typeDefinition(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()),
                ClientFeatures.typeDefinitionLinks(context.getClientCapabilities()));
    }

    /**
     * Handles {@code textDocument/implementation} by delegating to
     * {@link LanguageFeatures#navigation()}. Returns an empty link list
     * when the document is missing. When it is present, a client with link
     * support receives {@link LocationLink}s and any other client receives
     * {@link Location}s.
     *
     * @param params implementation request
     * @return locations or location links
     */
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(
            final ImplementationParams params) {
        return withLinks(params.getTextDocument().getUri(),
                document -> features.navigation().implementation(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()),
                ClientFeatures.implementationLinks(context.getClientCapabilities()));
    }

    /**
     * Handles {@code textDocument/declaration} by delegating to
     * {@link LanguageFeatures#navigation()}. Returns an empty link list
     * when the document is missing. When it is present, a client with link
     * support receives {@link LocationLink}s and any other client receives
     * {@link Location}s.
     *
     * @param params declaration request
     * @return locations or location links
     */
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(
            final DeclarationParams params) {
        return withLinks(params.getTextDocument().getUri(),
                document -> features.navigation().declaration(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()),
                ClientFeatures.declarationLinks(context.getClientCapabilities()));
    }

    /**
     * Handles {@code textDocument/references} by delegating to
     * {@link LanguageFeatures#navigation()}. The declaration is included
     * when the request context is absent or asks for it. Returns an empty
     * list when the document is missing.
     *
     * @param params references request
     * @return reference locations
     */
    @Override
    public CompletableFuture<List<? extends Location>> references(final ReferenceParams params) {
        boolean include = params.getContext() == null || params.getContext().isIncludeDeclaration();
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.navigation().references(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding(), include));
    }

    /**
     * Handles {@code textDocument/documentHighlight} by delegating to
     * {@link LanguageFeatures#highlights()}. Returns an empty list when
     * the document is missing.
     *
     * @param params highlight request
     * @return highlights in this document
     */
    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(final DocumentHighlightParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.highlights().highlights(document, context.getSnapshot(), params.getPosition(),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/documentSymbol} on the compiled snapshot.
     * Delegates to {@link LanguageFeatures#symbols()}, returning
     * hierarchical {@link DocumentSymbol}s when the client supports them
     * and flat {@link SymbolInformation} for every other client. A
     * snapshot with no document yields an empty list.
     *
     * @param params document-symbol request
     * @return symbols for the compiled document
     */
    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
            final DocumentSymbolParams params) {
        return fromCompiled(params.getTextDocument().getUri(), compiled -> {
            List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
            if (ClientFeatures.hierarchicalDocumentSymbols(context.getClientCapabilities())) {
                for (DocumentSymbol symbol : features.symbols().documentSymbols(compiled, context.getPositionEncoding())) {
                    result.add(Either.forRight(symbol));
                }
            } else {
                for (SymbolInformation symbol : features.symbols().documentSymbolInformation(compiled,
                        context.getPositionEncoding())) {
                    result.add(Either.forLeft(symbol));
                }
            }
            return result;
        });
    }

    /**
     * Handles {@code textDocument/codeAction} by delegating to
     * {@link LanguageFeatures#codeActions()}. Actions that create a file
     * are removed when the client cannot create files. When the request
     * names kinds, a kind is kept only when it equals a requested kind
     * or extends it with a dot. Returns an empty list when the document
     * is missing.
     *
     * @param params code-action request
     * @return commands or code actions
     */
    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(final CodeActionParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(), document -> {
            List<Diagnostic> diagnostics = params.getContext() == null
                    ? List.of() : params.getContext().getDiagnostics();
            List<Either<Command, CodeAction>> actions = new ArrayList<>(
                    features.codeActions().collect(document, context.getSnapshot(),
                            diagnostics, params.getRange(), context.getPositionEncoding(),
                            context.workspaceFolders(), context.getSettings().getSourcePaths()));
            if (!ClientFeatures.canCreateFiles(context.getClientCapabilities())) {
                actions.removeIf(GroovyTextDocumentService::createsFile);
            }
            return filterByOnly(actions, params.getContext() == null ? null : params.getContext().getOnly());
        });
    }

    /**
     * Handles {@code textDocument/codeLens} by delegating to
     * {@link LanguageFeatures#codeLenses()} on the compiled snapshot.
     * A snapshot with no document yields an empty list.
     *
     * @param params code-lens request
     * @return code lenses
     */
    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(final CodeLensParams params) {
        return fromCompiled(params.getTextDocument().getUri(),
                compiled -> features.codeLenses().codeLenses(compiled, context.getSnapshot(),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/formatting} by delegating to
     * {@link LanguageFeatures#formatting()} for the whole document.
     * Returns an empty edit list when the document is missing.
     *
     * @param params formatting request
     * @return text edits
     */
    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(final DocumentFormattingParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.formatting().format(document, null, tabSize(params.getOptions()),
                        insertSpaces(params.getOptions()), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/rangeFormatting} by delegating to
     * {@link LanguageFeatures#formatting()} for the requested range.
     * Returns an empty edit list when the document is missing.
     *
     * @param params range-formatting request
     * @return text edits
     */
    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(final DocumentRangeFormattingParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.formatting().format(document, params.getRange(), tabSize(params.getOptions()),
                        insertSpaces(params.getOptions()), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/onTypeFormatting} by delegating to
     * {@link LanguageFeatures#formatting()}. Returns an empty edit list
     * when the document is missing.
     *
     * @param params on-type formatting request
     * @return text edits
     */
    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(final DocumentOnTypeFormattingParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.formatting().onTypeFormat(document, params.getPosition(), params.getCh(),
                        tabSize(params.getOptions()), insertSpaces(params.getOptions()),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/prepareRename} by delegating to
     * {@link LanguageFeatures#rename()}. Returns {@code null} when the
     * document is missing or the feature declines. A {@link Range} is the
     * first alternative and a {@link PrepareRenameResult} is the second.
     *
     * @param params prepare-rename request
     * @return the range or prepare result, or {@code null}
     */
    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(
            final PrepareRenameParams params) {
        return withDocument(params.getTextDocument().getUri(), null, document -> {
            Either<Range, PrepareRenameResult> result = features.rename().prepareRename(
                    document, context.getSnapshot(), params.getPosition(), context.getPositionEncoding());
            if (result == null) {
                return null;
            }
            return result.isLeft() ? Either3.forFirst(result.getLeft()) : Either3.forSecond(result.getRight());
        });
    }

    /**
     * Handles {@code textDocument/rename} by delegating to
     * {@link LanguageFeatures#rename()}. Returns an empty
     * {@link WorkspaceEdit} when the document is missing.
     *
     * @param params rename request
     * @return the workspace edit
     */
    @Override
    public CompletableFuture<WorkspaceEdit> rename(final RenameParams params) {
        return withDocument(params.getTextDocument().getUri(), new WorkspaceEdit(Map.of()),
                document -> features.rename().rename(document, context.getSnapshot(), params.getPosition(),
                        params.getNewName(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/foldingRange} by delegating to
     * {@link LanguageFeatures#folding()} on the compiled snapshot.
     * A snapshot with no document yields an empty list.
     *
     * @param params folding-range request
     * @return folding ranges
     */
    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(final FoldingRangeRequestParams params) {
        return fromCompiled(params.getTextDocument().getUri(), features.folding()::folding);
    }

    /**
     * Handles {@code textDocument/selectionRange} by delegating to
     * {@link LanguageFeatures#selectionRanges()}. Returns an empty list
     * when the document is missing.
     *
     * @param params selection-range request
     * @return selection ranges
     */
    @Override
    public CompletableFuture<List<SelectionRange>> selectionRange(final SelectionRangeParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.selectionRanges().selectionRanges(document, context.getSnapshot(),
                        params.getPositions(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/semanticTokens/full} by delegating to
     * {@link LanguageFeatures#semanticTokens()} with the document text and
     * its compiled snapshot entry. Returns empty tokens when the document
     * is missing.
     *
     * @param params full semantic-token request
     * @return semantic tokens
     */
    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(final SemanticTokensParams params) {
        return withDocument(params.getTextDocument().getUri(), new SemanticTokens(List.of()),
                document -> features.semanticTokens().semanticTokens(document.getText(),
                        context.getSnapshot().get(document), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/semanticTokens/range} by delegating to
     * {@link LanguageFeatures#semanticTokens()} on the compiled snapshot
     * for the requested range. A snapshot with no document yields empty
     * tokens.
     *
     * @param params ranged semantic-token request
     * @return semantic tokens
     */
    @Override
    public CompletableFuture<SemanticTokens> semanticTokensRange(final SemanticTokensRangeParams params) {
        return fromCompiled(params.getTextDocument().getUri(),
                compiled -> features.semanticTokens().semanticTokens(compiled, context.getPositionEncoding(),
                        params.getRange()));
    }

    /**
     * Handles {@code textDocument/inlayHint} by delegating to
     * {@link LanguageFeatures#inlays()} on the compiled snapshot for the
     * requested range. A snapshot with no document yields an empty list.
     *
     * @param params inlay-hint request
     * @return inlay hints
     */
    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(final InlayHintParams params) {
        return fromCompiled(params.getTextDocument().getUri(),
                compiled -> features.inlays().inlayHints(compiled, params.getRange(),
                        context.getPositionEncoding(), context.getSnapshot()));
    }

    /**
     * Handles {@code textDocument/documentLink} by delegating to
     * {@link LanguageFeatures#documentLinks()} on the compiled snapshot.
     * A snapshot with no document yields an empty list.
     *
     * @param params document-link request
     * @return document links
     */
    @Override
    public CompletableFuture<List<DocumentLink>> documentLink(final DocumentLinkParams params) {
        return fromCompiled(params.getTextDocument().getUri(),
                compiled -> features.documentLinks().documentLinks(compiled, context.getSnapshot(),
                        context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/diagnostic} by wrapping
     * {@link LanguageServerContext#diagnosticsFor(CompiledDocument)} for
     * the compiled snapshot in a full report. A snapshot with no document
     * yields an empty report.
     *
     * @param params diagnostic request
     * @return the full document report
     */
    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(final DocumentDiagnosticParams params) {
        return fromCompiled(params.getTextDocument().getUri(), compiled -> {
            RelatedFullDocumentDiagnosticReport report = new RelatedFullDocumentDiagnosticReport(
                    context.diagnosticsFor(compiled));
            return new DocumentDiagnosticReport(report);
        });
    }

    /**
     * Handles {@code textDocument/prepareCallHierarchy} by delegating to
     * {@link LanguageFeatures#hierarchy()}. Returns an empty list when
     * the document is missing.
     *
     * @param params prepare request
     * @return call-hierarchy items
     */
    @Override
    public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(final CallHierarchyPrepareParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.hierarchy().prepareCallHierarchy(document, context.getSnapshot(),
                        params.getPosition(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code callHierarchy/incomingCalls} by delegating to
     * {@link LanguageFeatures#hierarchy()} for the item in the request.
     *
     * @param params incoming-calls request
     * @return incoming calls
     */
    @Override
    public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(
            final CallHierarchyIncomingCallsParams params) {
        return CompletableFuture.completedFuture(features.hierarchy().incomingCalls(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code callHierarchy/outgoingCalls} by delegating to
     * {@link LanguageFeatures#hierarchy()} for the item in the request.
     *
     * @param params outgoing-calls request
     * @return outgoing calls
     */
    @Override
    public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(
            final CallHierarchyOutgoingCallsParams params) {
        return CompletableFuture.completedFuture(features.hierarchy().outgoingCalls(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code textDocument/prepareTypeHierarchy} by delegating to
     * {@link LanguageFeatures#hierarchy()}. Returns an empty list when
     * the document is missing.
     *
     * @param params prepare request
     * @return type-hierarchy items
     */
    @Override
    public CompletableFuture<List<TypeHierarchyItem>> prepareTypeHierarchy(final TypeHierarchyPrepareParams params) {
        return withDocument(params.getTextDocument().getUri(), List.of(),
                document -> features.hierarchy().prepareTypeHierarchy(document, context.getSnapshot(),
                        params.getPosition(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code typeHierarchy/supertypes} by delegating to
     * {@link LanguageFeatures#hierarchy()} for the item in the request.
     *
     * @param params supertypes request
     * @return supertype items
     */
    @Override
    public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySupertypes(final TypeHierarchySupertypesParams params) {
        return CompletableFuture.completedFuture(features.hierarchy().supertypes(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    /**
     * Handles {@code typeHierarchy/subtypes} by delegating to
     * {@link LanguageFeatures#hierarchy()} for the item in the request.
     *
     * @param params subtypes request
     * @return subtype items
     */
    @Override
    public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySubtypes(final TypeHierarchySubtypesParams params) {
        return CompletableFuture.completedFuture(features.hierarchy().subtypes(
                params.getItem(), context.getSnapshot(), context.getPositionEncoding()));
    }

    private static boolean createsFile(final Either<Command, CodeAction> action) {
        if (action == null || !action.isRight() || action.getRight() == null || action.getRight().getEdit() == null
                || action.getRight().getEdit().getDocumentChanges() == null) {
            return false;
        }
        for (var change : action.getRight().getEdit().getDocumentChanges()) {
            if (change != null && change.isRight() && change.getRight() instanceof CreateFile) {
                return true;
            }
        }
        return false;
    }

    static List<Either<Command, CodeAction>> filterByOnly(final List<Either<Command, CodeAction>> actions,
                                                          final List<String> only) {
        if (actions == null || actions.isEmpty() || only == null || only.isEmpty()) {
            return actions == null ? List.of() : actions;
        }
        return actions.stream()
                .filter(action -> kindAccepted(action, only))
                .toList();
    }

    private static boolean kindAccepted(final Either<Command, CodeAction> action, final List<String> only) {
        if (action == null || !action.isRight() || action.getRight() == null) {
            return false;
        }
        String kind = action.getRight().getKind();
        if (kind == null) {
            return false;
        }
        for (String requested : only) {
            if (requested != null && (kind.equals(requested) || kind.startsWith(requested + "."))) {
                return true;
            }
        }
        return false;
    }

    private <T> CompletableFuture<T> withDocument(final String uri, final T absent,
                                                  final Function<TextDocument, T> action) {
        TextDocument document = context.documentFor(uri);
        if (document == null) {
            return CompletableFuture.completedFuture(absent);
        }
        return CompletableFuture.completedFuture(action.apply(document));
    }

    private <T> CompletableFuture<T> fromCompiled(final String uri,
                                                  final Function<CompiledDocument, T> action) {
        return CompletableFuture.completedFuture(action.apply(context.getSnapshot().get(uri)));
    }

    private CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> withLinks(
            final String uri, final Function<TextDocument, List<LocationLink>> action, final boolean linkSupport) {
        TextDocument document = context.documentFor(uri);
        if (document == null) {
            return CompletableFuture.completedFuture(Either.forRight(List.of()));
        }
        return asLocations(action.apply(document), linkSupport);
    }

    private static int tabSize(final FormattingOptions options) {
        return options == null ? 4 : options.getTabSize();
    }

    private static boolean insertSpaces(final FormattingOptions options) {
        return options == null || options.isInsertSpaces();
    }

    private static CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> asLocations(
            final List<LocationLink> links, final boolean linkSupport) {
        if (linkSupport) {
            return CompletableFuture.completedFuture(Either.forRight(links));
        }
        List<Location> locations = links.stream()
                .map(link -> new Location(link.getTargetUri(), link.getTargetSelectionRange()))
                .toList();
        return CompletableFuture.completedFuture(Either.forLeft(locations));
    }
}
