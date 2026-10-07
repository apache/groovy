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

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Process entry point. Speaks LSP over stdio by default; {@code --socket
 * <port>} listens for a single client connection. {@code System.out} is
 * redirected to {@code System.err} so that incidental prints cannot
 * corrupt the JSON-RPC stream.
 */
@SuppressWarnings("java:S106") // stdout is the JSON-RPC stream; --help/--version are CLI
public final class GroovyLanguageServerLauncher {

    private GroovyLanguageServerLauncher() {
    }

    /**
     * Launches the language server.
     *
     * @param args {@code --help}, {@code --version}, or {@code --socket <port>}
     * @throws IOException when the socket transport fails
     * @throws InterruptedException when waiting for {@code exit} is interrupted
     * @throws ExecutionException when the JSON-RPC listener fails
     */
    public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
        ListArgs parsed = ListArgs.parse(args);
        if (parsed.help) {
            System.out.println("""
                    groovylsp - Apache Groovy Language Server (LSP 3.18)
                    Usage: groovylsp [--stdio] [--socket <port>] [--version] [--help]
                    Default transport is stdio.
                    """);
            return;
        }
        if (parsed.version) {
            System.out.println("groovy-lsp " + GroovyLanguageServer.implementationVersion());
            return;
        }
        if (parsed.port != null) {
            try (ServerSocket serverSocket = new ServerSocket(parsed.port);
                 Socket socket = serverSocket.accept()) {
                run(socket.getInputStream(), socket.getOutputStream(), true);
            }
            return;
        }
        InputStream in = System.in;
        OutputStream out = System.out;
        System.setOut(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        System.exit(run(in, out, true));
    }

    /**
     * Runs a server on the given streams. When {@code waitForExit} is true,
     * the method blocks until the LSP {@code exit} notification or until
     * the input stream reaches EOF, whichever comes first. A client that
     * sends {@code exit} and keeps the transport open must not leave the
     * process blocked in the reader. EOF without {@code exit} (a dropped
     * client) returns {@code 0}. {@code exit} after {@code shutdown}
     * returns {@code 0}; {@code exit} without {@code shutdown} returns {@code 1}.
     * <p>
     * {@code exit} does not call {@link System#exit(int)}. Stdio
     * {@link #main} does that after this method returns, which also stops
     * the non-daemon reader. Socket mode closes the accepted socket
     * instead. An in-process caller that returns on {@code exit} while
     * the stream stays open should close that stream so the reader can
     * finish.
     *
     * @param in JSON-RPC input
     * @param out JSON-RPC output
     * @param waitForExit whether to wait for {@code exit} or input EOF
     * @return the exit code, or {@code null} when not waiting
     * @throws InterruptedException when waiting for {@code exit} is interrupted
     * @throws ExecutionException when the JSON-RPC listener fails
     */
    public static Integer run(final InputStream in, final OutputStream out, final boolean waitForExit)
            throws InterruptedException, ExecutionException {
        GroovyLanguageServer server = new GroovyLanguageServer();
        Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in, out);
        server.connect(launcher.getRemoteProxy());
        Future<Void> listening = launcher.startListening();
        if (!waitForExit) {
            return server.getContext().getExitCode();
        }
        return awaitExit(server, listening);
    }

    static Integer awaitExit(final GroovyLanguageServer server, final Future<Void> listening)
            throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> done = new CompletableFuture<>();
        server.exited().thenAccept(done::complete);
        Thread transport = new Thread(() -> completeOnEof(listening, server, done), "groovy-lsp-transport");
        transport.setDaemon(true);
        transport.start();
        try {
            Integer code = done.get();
            return code == null ? 0 : code;
        } catch (ExecutionException e) {
            throw unwrapListener(e);
        }
    }

    /**
     * Input EOF is the other way {@code run} stops. {@code exit} wins when
     * it was already recorded, because {@link CompletableFuture#complete}
     * keeps the first result.
     */
    static void completeOnEof(final Future<Void> listening, final GroovyLanguageServer server,
                                       final CompletableFuture<Integer> done) {
        try {
            listening.get();
            done.complete(server.getContext().getExitCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            done.completeExceptionally(e);
        } catch (ExecutionException e) {
            done.completeExceptionally(e);
        }
    }

    static ExecutionException unwrapListener(final ExecutionException wrapped)
            throws InterruptedException, ExecutionException {
        Throwable cause = wrapped.getCause();
        if (cause instanceof ExecutionException execution) {
            throw execution;
        }
        if (cause instanceof InterruptedException interrupted) {
            throw interrupted;
        }
        throw wrapped;
    }

    static final class ListArgs {
        boolean help;
        boolean version;
        Integer port;

        static ListArgs parse(final String[] args) {
            ListArgs parsed = new ListArgs();
            List<String> list = Arrays.asList(args == null ? new String[0] : args);
            int i = 0;
            while (i < list.size()) {
                String arg = list.get(i);
                i += 1;
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    parsed.help = true;
                } else if ("--version".equals(arg) || "-v".equals(arg)) {
                    parsed.version = true;
                } else if ("--socket".equals(arg) && i < list.size()) {
                    parsed.port = Integer.parseInt(list.get(i));
                    i += 1;
                }
            }
            return parsed;
        }
    }
}
