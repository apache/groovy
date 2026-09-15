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
package org.apache.groovy.lsp.spi;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.util.List;

/**
 * One contribution to the language server. Implementors ship on the
 * server classpath and register via {@code ServiceLoader},
 * or tests pass them to {@code ExtensionHost.of}. Every method has a
 * no-op default so a GDSL, STC, or MCP plugin implements only what it
 * needs.
 * <p>
 * Hooks run on the compile thread except {@code extraCodeActions} and
 * {@code executeCommand}, which run on the request thread and must
 * read the last snapshot rather than compiling.
 */
@Incubating
public interface GroovyLspExtension {

    /**
     * Stable id used in compile fingerprints so enabling a plugin
     * forces a recompile.
     *
     * @return a non-blank id
     */
    default String id() {
        return getClass().getName();
    }

    /**
     * Mutates {@code configuration} before the compilation unit is
     * created (extra customizers, extra disabled transforms).
     *
     * @param configuration compiler configuration
     */
    default void configure(final CompilerConfiguration configuration) {
    }

    /**
     * Runs after {@code unit.compile} and before the snapshot is built.
     * GDSL injection and a static-type-checking intelligence pass belong
     * here. Must not generate class files.
     *
     * @param unit the compiled unit, possibly partial
     */
    default void afterCompile(final CompilationUnit unit) {
    }

    /**
     * Extra diagnostics published with the compiler's own messages.
     *
     * @param uri document URI
     * @param sourceText document text
     * @return extra diagnostics, never {@code null}
     */
    default List<Diagnostic> extraDiagnostics(final URI uri, final String sourceText) {
        return List.of();
    }

    /**
     * Extra code actions for a range.
     *
     * @param uri document URI
     * @param diagnostics client diagnostics
     * @param range requested range
     * @return extra actions, never {@code null}
     */
    default List<Either<Command, CodeAction>> extraCodeActions(final URI uri, final List<Diagnostic> diagnostics,
                                                               final Range range) {
        return List.of();
    }

    /**
     * Extra {@code workspace/executeCommand} names to advertise.
     *
     * @return command identifiers, never {@code null}
     */
    default List<String> commands() {
        return List.of();
    }

    /**
     * Handles {@code command} when {@link #commands()} contains it.
     *
     * @param command command id
     * @param arguments protocol arguments
     * @param session narrow session (apply-edit only)
     * @return the LSP result, possibly {@code null}
     */
    default Object executeCommand(final String command, final List<Object> arguments,
                                  final GroovyLspSession session) {
        return null;
    }
}
