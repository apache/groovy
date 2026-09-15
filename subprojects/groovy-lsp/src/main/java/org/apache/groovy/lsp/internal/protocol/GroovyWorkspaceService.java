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
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Workspace folders, configuration, watched files, symbols and commands.
 */
public final class GroovyWorkspaceService implements WorkspaceService {

    private final LanguageServerContext context;

    public GroovyWorkspaceService(final LanguageServerContext context) {
        this.context = context;
    }

    @Override
    public void didChangeConfiguration(final DidChangeConfigurationParams params) {
        Object settings = params.getSettings();
        if (settings instanceof Map<?, ?> map) {
            Object groovy = map.get("groovy");
            if (groovy instanceof Map<?, ?> groovyMap) {
                apply(groovyMap);
            } else {
                apply(map);
            }
        }
        context.scheduleRecompile(0);
    }

    @Override
    public void didChangeWatchedFiles(final DidChangeWatchedFilesParams params) {
        context.scheduleRecompile(100);
    }

    @Override
    public void didChangeWorkspaceFolders(final DidChangeWorkspaceFoldersParams params) {
        if (params.getEvent() == null) {
            return;
        }
        if (params.getEvent().getAdded() != null) {
            for (WorkspaceFolder folder : params.getEvent().getAdded()) {
                context.getWorkspaceFolders().add(Uris.parse(folder.getUri()));
            }
        }
        if (params.getEvent().getRemoved() != null) {
            for (WorkspaceFolder folder : params.getEvent().getRemoved()) {
                context.getWorkspaceFolders().remove(Uris.parse(folder.getUri()));
            }
        }
        context.scheduleRecompile(0);
    }

    @Override
    public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(
            final WorkspaceSymbolParams params) {
        List<WorkspaceSymbol> symbols = context.getSymbols().workspaceSymbols(context.getSnapshot(),
                params.getQuery(), context.getPositionEncoding());
        return CompletableFuture.completedFuture(Either.forRight(symbols));
    }

    @Override
    public CompletableFuture<Object> executeCommand(final ExecuteCommandParams params) {
        String command = params.getCommand();
        List<Object> args = params.getArguments() == null ? List.of() : params.getArguments();
        if ("groovy.lsp.organizeImports".equals(command) && !args.isEmpty()) {
            String uri = String.valueOf(args.get(0));
            CompiledDocument compiled = context.getSnapshot().get(uri);
            List<TextEdit> edits = context.getSupport().organizeImports(compiled, context.getPositionEncoding());
            WorkspaceEdit edit = new WorkspaceEdit(Map.of(uri, edits));
            if (ClientFeatures.applyEdit(context.getClientCapabilities()) && context.getClient() != null
                    && !edits.isEmpty()) {
                context.getClient().applyEdit(new ApplyWorkspaceEditParams(edit));
            }
            return CompletableFuture.completedFuture(edit);
        }
        if ("groovy.lsp.showReferences".equals(command) && args.size() >= 3) {
            String uri = String.valueOf(args.get(0));
            int line = args.get(1) instanceof Number number ? number.intValue() : 0;
            int character = args.get(2) instanceof Number number ? number.intValue() : 0;
            TextDocument document = context.getDocuments().get(uri);
            if (document == null) {
                CompiledDocument compiled = context.getSnapshot().get(uri);
                if (compiled != null) {
                    document = new TextDocument(compiled.getUri(), "groovy", compiled.getVersion(), compiled.getText());
                }
            }
            if (document == null) {
                return CompletableFuture.completedFuture(List.of());
            }
            return CompletableFuture.completedFuture(context.getNavigation().references(
                    document, context.getSnapshot(), new Position(line, character),
                    context.getPositionEncoding(), true));
        }
        Object extra = context.getExtensions().executeCommand(command, args, context);
        return CompletableFuture.completedFuture(extra);
    }

    @SuppressWarnings("unchecked")
    private void apply(final Map<?, ?> map) {
        List<String> classpath = stringList(map.get("classpath"));
        List<String> sourcePaths = stringList(map.get("sourcePaths"));
        CompilerSettings current = context.getSettings();
        boolean grape = map.containsKey("grapeEnabled")
                ? Boolean.TRUE.equals(map.get("grapeEnabled")) : current.isGrapeEnabled();
        boolean astTest = map.containsKey("astTestEnabled")
                ? Boolean.TRUE.equals(map.get("astTestEnabled")) : current.isAstTestEnabled();
        context.setSettings(new CompilerSettings(
                classpath.isEmpty() ? current.getClasspath() : classpath,
                sourcePaths.isEmpty() ? current.getSourcePaths() : sourcePaths,
                current.getThroughPhase(), grape, astTest));
    }

    private static List<String> stringList(final Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }
}
