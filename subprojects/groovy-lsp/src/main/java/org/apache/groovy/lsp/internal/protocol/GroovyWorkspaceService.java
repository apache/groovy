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
import org.eclipse.lsp4j.FileRename;
import org.eclipse.lsp4j.RenameFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Workspace folders, configuration, watched files, symbols and commands.
 */
public final class GroovyWorkspaceService implements WorkspaceService {

    static final String ORGANIZE_IMPORTS = "groovy.lsp.organizeImports";
    static final String SHOW_REFERENCES = "groovy.lsp.showReferences";
    static final String GOTO_SUPER_METHOD = "groovy.lsp.gotoSuperMethod";
    private static final String GRAPE_ENABLED = "grapeEnabled";
    private static final String AST_TEST_ENABLED = "astTestEnabled";
    private static final Set<String> CORE_SETTING_KEYS = Set.of(
            "classpath", "sourcePaths", GRAPE_ENABLED, AST_TEST_ENABLED);

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
        applyFolders(params.getEvent().getAdded(), true);
        applyFolders(params.getEvent().getRemoved(), false);
        context.scheduleRecompile(0);
    }

    @Override
    public CompletableFuture<WorkspaceEdit> willRenameFiles(final RenameFilesParams params) {
        List<FileRename> files = params == null || params.getFiles() == null
                ? List.of() : params.getFiles();
        return CompletableFuture.completedFuture(context.getRename().willRenameFiles(
                files, context.getSnapshot(), context.getPositionEncoding()));
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
        CompletableFuture<Object> core = coreCommand(command, args);
        if (core != null) {
            return core;
        }
        return CompletableFuture.completedFuture(context.getExtensions().executeCommand(command, args, context));
    }

    private CompletableFuture<Object> coreCommand(final String command, final List<Object> args) {
        if (ORGANIZE_IMPORTS.equals(command) && !args.isEmpty()) {
            return organizeImports(String.valueOf(args.get(0)));
        }
        if (SHOW_REFERENCES.equals(command) && args.size() >= 3) {
            return atCaret(args, (document, position) -> context.getNavigation().references(
                    document, context.getSnapshot(), position, context.getPositionEncoding(), true));
        }
        if (GOTO_SUPER_METHOD.equals(command) && args.size() >= 3) {
            return atCaret(args, (document, position) -> context.getNavigation().superMethod(
                    document, context.getSnapshot(), position, context.getPositionEncoding()));
        }
        return null;
    }

    private CompletableFuture<Object> organizeImports(final String uri) {
        CompiledDocument compiled = context.getSnapshot().get(uri);
        List<TextEdit> edits = context.getSupport().organizeImports(compiled, context.getPositionEncoding());
        WorkspaceEdit edit = new WorkspaceEdit(Map.of(uri, edits));
        if (ClientFeatures.applyEdit(context.getClientCapabilities()) && context.getClient() != null
                && !edits.isEmpty()) {
            context.getClient().applyEdit(new ApplyWorkspaceEditParams(edit));
        }
        return CompletableFuture.completedFuture(edit);
    }

    private CompletableFuture<Object> atCaret(final List<Object> args,
                                              final BiFunction<TextDocument, Position, Object> action) {
        TextDocument document = context.documentFor(String.valueOf(args.get(0)));
        if (document == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        int line = args.get(1) instanceof Number number ? number.intValue() : 0;
        int character = args.get(2) instanceof Number number ? number.intValue() : 0;
        return CompletableFuture.completedFuture(action.apply(document, new Position(line, character)));
    }

    @SuppressWarnings("unchecked")
    private void apply(final Map<?, ?> map) {
        List<String> classpath = stringList(map.get("classpath"));
        List<String> sourcePaths = stringList(map.get("sourcePaths"));
        CompilerSettings current = context.getSettings();
        boolean grape = map.containsKey(GRAPE_ENABLED)
                ? Boolean.TRUE.equals(map.get(GRAPE_ENABLED)) : current.isGrapeEnabled();
        boolean astTest = map.containsKey(AST_TEST_ENABLED)
                ? Boolean.TRUE.equals(map.get(AST_TEST_ENABLED)) : current.isAstTestEnabled();
        context.setSettings(new CompilerSettings(
                classpath.isEmpty() ? current.getClasspath() : classpath,
                sourcePaths.isEmpty() ? current.getSourcePaths() : sourcePaths,
                current.getThroughPhase(), grape, astTest, extraSettings(map, current)));
    }

    private static Map<String, String> extraSettings(final Map<?, ?> map, final CompilerSettings current) {
        Map<String, String> extra = new LinkedHashMap<>(current.getExtra());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                String key = entry.getKey().toString();
                if (!CORE_SETTING_KEYS.contains(key)) {
                    extra.put(key, extraValue(entry.getValue()));
                }
            }
        }
        return extra;
    }

    private static String extraValue(final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List<?> list) {
            return stringList(list).toString();
        }
        return value.toString();
    }

    private void applyFolders(final List<WorkspaceFolder> folders, final boolean add) {
        if (folders == null) {
            return;
        }
        for (WorkspaceFolder folder : folders) {
            if (folder == null || folder.getUri() == null) {
                continue;
            }
            if (add) {
                context.addWorkspaceFolder(Uris.parse(folder.getUri()));
            } else {
                context.removeWorkspaceFolder(Uris.parse(folder.getUri()));
            }
        }
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
