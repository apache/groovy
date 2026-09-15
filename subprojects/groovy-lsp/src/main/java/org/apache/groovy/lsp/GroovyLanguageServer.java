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
package org.apache.groovy.lsp;

import org.apache.groovy.lsp.internal.LanguageServerContext;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.protocol.GroovyTextDocumentService;
import org.apache.groovy.lsp.internal.protocol.GroovyWorkspaceService;
import org.apache.groovy.lsp.internal.protocol.ServerCapabilityFactory;
import org.apache.groovy.lsp.internal.util.Uris;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

/**
 * Apache Groovy language server. Implements LSP 3.18 with capability
 * negotiation for 3.19-preview clients. Compile-time execution paths
 * ({@code @Grab}, {@code @ASTTest}) are off unless the client enables them.
 */
public final class GroovyLanguageServer implements LanguageServer, LanguageClientAware {

    private final LanguageServerContext context;
    private final GroovyTextDocumentService textDocuments;
    private final GroovyWorkspaceService workspace;
    private final ServerCapabilityFactory capabilities = new ServerCapabilityFactory();

    /**
     * Creates a server with a fresh session.
     */
    public GroovyLanguageServer() {
        this(new LanguageServerContext());
    }

    /**
     * Creates a server around an existing session. Intended for tests.
     *
     * @param context session state
     */
    public GroovyLanguageServer(final LanguageServerContext context) {
        this.context = context;
        this.textDocuments = new GroovyTextDocumentService(context);
        this.workspace = new GroovyWorkspaceService(context);
    }

    /**
     * @return the session state
     */
    public LanguageServerContext getContext() {
        return context;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        if (params.getCapabilities() != null && params.getCapabilities().getGeneral() != null) {
            context.setPositionEncoding(PositionEncoding.negotiate(
                    params.getCapabilities().getGeneral().getPositionEncodings()));
        } else {
            context.setPositionEncoding(PositionEncoding.UTF16);
        }
        context.setClientCapabilities(params.getCapabilities());
        if (params.getWorkspaceFolders() != null) {
            for (WorkspaceFolder folder : params.getWorkspaceFolders()) {
                context.addWorkspaceFolder(Uris.parse(folder.getUri()));
            }
        } else if (params.getRootUri() != null) {
            context.addWorkspaceFolder(Uris.parse(params.getRootUri()));
        }
        InitializeResult result = new InitializeResult(capabilities.create(
                context.getPositionEncoding(), context.getExtensions().commands()));
        result.setServerInfo(new ServerInfo("groovy-lsp", GroovyLanguageServer.class.getPackage() == null
                ? "dev" : String.valueOf(GroovyLanguageServer.class.getPackage().getImplementationVersion())));
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void initialized(final InitializedParams params) {
        context.setInitialized(true);
        context.scheduleRecompile(0);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        context.setShutdown(true);
        context.close();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        context.setExitCode(context.isShutdown() ? 0 : 1);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocuments;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspace;
    }

    @Override
    public void connect(final LanguageClient client) {
        context.setClient(client);
    }
}
