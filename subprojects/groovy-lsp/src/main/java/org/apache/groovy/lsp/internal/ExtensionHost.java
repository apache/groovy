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
package org.apache.groovy.lsp.internal;

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.apache.groovy.lsp.spi.GroovyLspExtension;
import org.apache.groovy.lsp.spi.GroovyLspSession;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Discovers and fans out {@link GroovyLspExtension} instances. One bad
 * plugin must not abort compile or a request.
 */
public final class ExtensionHost {

    private static final ExtensionHost NONE = new ExtensionHost(List.of());

    private final List<GroovyLspExtension> extensions;

    private ExtensionHost(final List<GroovyLspExtension> extensions) {
        this.extensions = List.copyOf(extensions);
    }

    /**
     * @return a host with no plugins
     */
    public static ExtensionHost none() {
        return NONE;
    }

    /**
     * @param extensions explicit plugins, typically from tests
     * @return a host
     */
    public static ExtensionHost of(final GroovyLspExtension... extensions) {
        if (extensions == null || extensions.length == 0) {
            return NONE;
        }
        List<GroovyLspExtension> list = new ArrayList<>();
        for (GroovyLspExtension extension : extensions) {
            if (extension != null) {
                list.add(extension);
            }
        }
        return list.isEmpty() ? NONE : new ExtensionHost(list);
    }

    /**
     * Loads {@link ServiceLoader} registrations from {@code loader}.
     *
     * @param loader class loader, or {@code null} for the SPI type's loader
     * @return a host
     */
    public static ExtensionHost discover(final ClassLoader loader) {
        ClassLoader use = loader == null ? GroovyLspExtension.class.getClassLoader() : loader;
        List<GroovyLspExtension> found = new ArrayList<>();
        for (GroovyLspExtension extension : ServiceLoader.load(GroovyLspExtension.class, use)) {
            found.add(extension);
        }
        return found.isEmpty() ? NONE : new ExtensionHost(found);
    }

    /**
     * @return plugin ids in registration order
     */
    public List<String> ids() {
        List<String> ids = new ArrayList<>(extensions.size());
        for (GroovyLspExtension extension : extensions) {
            ids.add(extension.id());
        }
        return ids;
    }

    /**
     * @return advertised extra execute-command names
     */
    public List<String> commands() {
        List<String> commands = new ArrayList<>();
        for (GroovyLspExtension extension : extensions) {
            List<String> extra = extension.commands();
            if (extra != null) {
                commands.addAll(extra);
            }
        }
        return commands;
    }

    /**
     * @return the plugins
     */
    public List<GroovyLspExtension> all() {
        return extensions;
    }

    public void configure(final CompilerConfiguration configuration, final CompilerSettings settings) {
        for (GroovyLspExtension extension : extensions) {
            try {
                extension.configure(configuration);
            } catch (RuntimeException ignored) {
                // one plugin must not abort configure
            }
        }
    }

    public void afterCompile(final CompilationUnit unit) {
        for (GroovyLspExtension extension : extensions) {
            try {
                extension.afterCompile(unit);
            } catch (RuntimeException ignored) {
                // one plugin must not abort compile
            }
        }
    }

    public List<Diagnostic> extraDiagnostics(final CompiledDocument document, final PositionEncoding encoding) {
        List<Diagnostic> extras = new ArrayList<>();
        for (GroovyLspExtension extension : extensions) {
            try {
                List<Diagnostic> extra = document == null
                        ? null
                        : extension.extraDiagnostics(document.getUri(), document.getText());
                if (extra != null) {
                    extras.addAll(extra);
                }
            } catch (RuntimeException ignored) {
                // skip
            }
        }
        return extras;
    }

    public List<Either<Command, CodeAction>> extraCodeActions(final TextDocument document,
                                                              final CompilationSnapshot snapshot,
                                                              final List<Diagnostic> diagnostics,
                                                              final Range range, final PositionEncoding encoding) {
        List<Either<Command, CodeAction>> extras = new ArrayList<>();
        for (GroovyLspExtension extension : extensions) {
            try {
                List<Either<Command, CodeAction>> extra = document == null
                        ? null
                        : extension.extraCodeActions(document.getUri(), diagnostics, range);
                if (extra != null) {
                    extras.addAll(extra);
                }
            } catch (RuntimeException ignored) {
                // skip
            }
        }
        return extras;
    }

    public Object executeCommand(final String command, final List<Object> arguments,
                                 final GroovyLspSession session) {
        if (command == null) {
            return null;
        }
        for (GroovyLspExtension extension : extensions) {
            List<String> commands = extension.commands();
            if (commands == null || !commands.contains(command)) {
                continue;
            }
            try {
                return extension.executeCommand(command, arguments, session);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ExtensionHost host && ids().equals(host.ids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids());
    }
}
