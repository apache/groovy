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
package org.apache.groovy.groovysh;

import org.apache.groovy.groovysh.jline.GroovyEngine;
import org.apache.groovy.groovysh.jline.GroovySystemRegistry;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jline.console.Printer;
import org.jline.reader.LineReader;
import org.jline.shell.CommandGroup;
import org.jline.shell.ShellBuilder;
import org.jline.terminal.Terminal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Configuration for embedding groovysh programmatically via
 * {@link Main#start(GroovyshOptions, String[])}.
 * <p>
 * Where a setting has an equivalent on JLine's {@link ShellBuilder},
 * this class deliberately uses the same name and parameter type, so that a future
 * move of groovysh onto {@code org.jline.shell} is re-plumbing rather than a break
 * in Groovy's API. The Groovy-specific settings — compiler configuration, engine,
 * bindings, banner, and the result/error handlers a language REPL needs and a
 * command shell does not — have no such equivalent.
 * <p>
 * Binding variables stay in {@link #getBindings()}; they are not mixed
 * with embedding hooks. Construct with {@link #builder()}.
 *
 * @since 7.0.0
 */
@Incubating
public final class GroovyshOptions {

    /**
     * Handles a value produced by the REPL loop.
     */
    @FunctionalInterface
    public interface ResultHandler {
        /**
         * Renders {@code result} after a line has been executed.
         *
         * @param printer the printer used by the session
         * @param result the value returned by the command dispatch, which may be {@code null}
         * @throws Exception if rendering fails; groovysh then routes it to the error handler
         */
        void handle(Printer printer, Object result) throws Exception;
    }

    /**
     * Handles an error thrown by the REPL loop.
     */
    @FunctionalInterface
    public interface ErrorHandler {
        /**
         * Handles an error that escaped command execution.
         * {@code UserInterruptException} and {@code EndOfFileException} are
         * not delivered here; {@link Main} treats those as control flow.
         *
         * A handler that throws is not caught again: the error escapes the REPL
         * loop and {@link Main#start(GroovyshOptions, String[])} returns 1.
         *
         * @param error the thrown error or exception
         * @param defaultTrace groovysh's own renderer, for a handler that wants to delegate
         */
        void handle(Throwable error, Consumer<Throwable> defaultTrace);
    }

    private final CompilerConfiguration compilerConfiguration;
    private final GroovyEngine engine;
    private final Map<String, Object> bindings;
    private final List<CommandGroup> groups;
    private final Supplier<String> prompt;
    private final Supplier<String> rightPrompt;
    private final ResultHandler resultHandler;
    private final ErrorHandler errorHandler;
    private final boolean showBanner;
    private final Terminal terminal;
    private final Path historyFile;
    private final BiConsumer<LineReader, GroovySystemRegistry> onReaderReady;

    private GroovyshOptions(Builder builder) {
        this.compilerConfiguration = builder.compilerConfiguration;
        this.engine = builder.engine;
        this.bindings = Collections.unmodifiableMap(new LinkedHashMap<>(builder.bindings));
        this.groups = Collections.unmodifiableList(new ArrayList<>(builder.groups));
        this.prompt = builder.prompt;
        this.rightPrompt = builder.rightPrompt;
        this.resultHandler = builder.resultHandler;
        this.errorHandler = builder.errorHandler;
        this.showBanner = builder.showBanner;
        this.terminal = builder.terminal;
        this.historyFile = builder.historyFile;
        this.onReaderReady = builder.onReaderReady;
    }

    /**
     * Creates a builder for embedding options.
     *
     * @return a new builder with default values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Compiler configuration used when this object does not already hold an
     * {@link #getEngine() engine}. {@code null} means {@link CompilerConfiguration#DEFAULT}.
     *
     * @return the compiler configuration, or {@code null}
     */
    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    /**
     * Pre-built engine. When non-{@code null}, {@link #getCompilerConfiguration()}
     * is ignored because the engine already carries its configuration.
     *
     * @return the engine, or {@code null} to let {@link Main} construct one
     */
    public GroovyEngine getEngine() {
        return engine;
    }

    /**
     * Binding variables installed on the engine before the REPL starts.
     *
     * @return an unmodifiable map, never {@code null}
     */
    public Map<String, Object> getBindings() {
        return bindings;
    }

    /**
     * Command groups contributed by the embedder. They are resolved
     * <em>before</em> groovysh's own commands, so a group may override a
     * built-in command name.
     *
     * @return an unmodifiable list, never {@code null}
     */
    public List<CommandGroup> getGroups() {
        return groups;
    }

    /**
     * Prompt supplier invoked on each REPL iteration. {@code null} means {@code "groovy> "}.
     *
     * @return the prompt supplier, or {@code null}
     */
    public Supplier<String> getPrompt() {
        return prompt;
    }

    /**
     * Right-hand prompt supplier invoked on each REPL iteration.
     *
     * @return the right prompt supplier, or {@code null} for none
     */
    public Supplier<String> getRightPrompt() {
        return rightPrompt;
    }

    /**
     * Result renderer. {@code null} means print {@code result?.toString()} via the printer.
     *
     * @return the result handler, or {@code null}
     */
    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    /**
     * Error renderer. {@code null} means groovysh's own trace.
     *
     * @return the error handler, or {@code null}
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Whether {@link Main} should print the groovysh banner (or the quiet version line).
     *
     * @return {@code true} to print the banner
     */
    public boolean isShowBanner() {
        return showBanner;
    }

    /**
     * Terminal to use instead of the one {@link Main} would build from CLI flags.
     *
     * @return the terminal, or {@code null} to build the default
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * History file for the session. {@code null} means groovysh's own
     * {@code groovysh_history} in the user state directory.
     *
     * @return the history file, or {@code null}
     */
    public Path getHistoryFile() {
        return historyFile;
    }

    /**
     * Callback invoked once the reader and the command registry are both wired,
     * immediately before the REPL starts, for settings this class does not
     * model — the secondary prompt pattern, key bindings, reader options, and
     * the completer.
     *
     * @return the callback, or {@code null}
     */
    public BiConsumer<LineReader, GroovySystemRegistry> getOnReaderReady() {
        return onReaderReady;
    }

    /**
     * Builder for {@link GroovyshOptions}.
     *
     * @since 7.0.0
     */
    @Incubating
    public static final class Builder {
        private CompilerConfiguration compilerConfiguration;
        private GroovyEngine engine;
        private final Map<String, Object> bindings = new LinkedHashMap<>();
        private final List<CommandGroup> groups = new ArrayList<>();
        private Supplier<String> prompt;
        private Supplier<String> rightPrompt;
        private ResultHandler resultHandler;
        private ErrorHandler errorHandler;
        private boolean showBanner = true;
        private Terminal terminal;
        private Path historyFile;
        private BiConsumer<LineReader, GroovySystemRegistry> onReaderReady;

        private Builder() {
        }

        /**
         * Sets the compiler configuration used when no {@link #engine(GroovyEngine)} is supplied.
         *
         * @param compilerConfiguration the configuration, or {@code null} for the default
         * @return this builder
         */
        public Builder compilerConfiguration(CompilerConfiguration compilerConfiguration) {
            this.compilerConfiguration = compilerConfiguration;
            return this;
        }

        /**
         * Supplies a pre-built engine. When set, {@link #compilerConfiguration(CompilerConfiguration)}
         * is not used to construct a second engine.
         *
         * @param engine the engine
         * @return this builder
         */
        public Builder engine(GroovyEngine engine) {
            this.engine = engine;
            return this;
        }

        /**
         * Replaces the binding variables that will be installed on the engine.
         *
         * @param bindings variables to install; {@code null} is treated as empty
         * @return this builder
         */
        public Builder bindings(Map<String, ?> bindings) {
            this.bindings.clear();
            if (bindings != null) {
                for (Map.Entry<String, ?> entry : bindings.entrySet()) {
                    if (entry.getKey() != null) {
                        this.bindings.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return this;
        }

        /**
         * Adds a single binding variable.
         *
         * @param name variable name, must not be {@code null}
         * @param value variable value
         * @return this builder
         */
        public Builder binding(String name, Object value) {
            Objects.requireNonNull(name, "name");
            this.bindings.put(name, value);
            return this;
        }

        /**
         * Appends command groups. Mirrors {@code ShellBuilder.groups(CommandGroup...)}.
         *
         * @param groups groups to append; {@code null} entries are rejected
         * @return this builder
         */
        public Builder groups(CommandGroup... groups) {
            if (groups != null) {
                for (CommandGroup group : groups) {
                    this.groups.add(Objects.requireNonNull(group, "group"));
                }
            }
            return this;
        }

        /**
         * Appends command groups.
         *
         * @param groups groups to append; {@code null} is ignored
         * @return this builder
         */
        public Builder groups(Iterable<? extends CommandGroup> groups) {
            if (groups != null) {
                for (CommandGroup group : groups) {
                    this.groups.add(Objects.requireNonNull(group, "group"));
                }
            }
            return this;
        }

        /**
         * Sets a fixed prompt. Mirrors {@code ShellBuilder.prompt(String)}.
         *
         * @param prompt the prompt text, or {@code null} for {@code "groovy> "}
         * @return this builder
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt == null ? null : () -> prompt;
            return this;
        }

        /**
         * Sets the prompt supplier, called once per REPL iteration.
         * Mirrors {@code ShellBuilder.prompt(Supplier)}.
         *
         * @param prompt the supplier, or {@code null} for {@code "groovy> "}
         * @return this builder
         */
        public Builder prompt(Supplier<String> prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Sets a fixed right-hand prompt. Mirrors {@code ShellBuilder.rightPrompt(String)}.
         *
         * @param rightPrompt the prompt text, or {@code null} for none
         * @return this builder
         */
        public Builder rightPrompt(String rightPrompt) {
            this.rightPrompt = rightPrompt == null ? null : () -> rightPrompt;
            return this;
        }

        /**
         * Sets the right-hand prompt supplier. Mirrors {@code ShellBuilder.rightPrompt(Supplier)}.
         *
         * @param rightPrompt the supplier, or {@code null} for none
         * @return this builder
         */
        public Builder rightPrompt(Supplier<String> rightPrompt) {
            this.rightPrompt = rightPrompt;
            return this;
        }

        /**
         * Sets the result handler.
         *
         * @param resultHandler the handler, or {@code null} for the default printer
         * @return this builder
         */
        public Builder resultHandler(ResultHandler resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        /**
         * Sets the error handler.
         *
         * @param errorHandler the handler, or {@code null} for groovysh's own trace
         * @return this builder
         */
        public Builder errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Sets whether the groovysh banner is printed.
         *
         * @param showBanner {@code false} to suppress the banner
         * @return this builder
         */
        public Builder showBanner(boolean showBanner) {
            this.showBanner = showBanner;
            return this;
        }

        /**
         * Supplies a terminal instead of letting {@link Main} build one from CLI flags.
         * Mirrors {@code ShellBuilder.terminal(Terminal)}.
         *
         * @param terminal the terminal, or {@code null} to build the default
         * @return this builder
         */
        public Builder terminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        /**
         * Sets the history file. Mirrors {@code ShellBuilder.historyFile(Path)}.
         *
         * @param historyFile the file, or {@code null} for groovysh's own
         * @return this builder
         */
        public Builder historyFile(Path historyFile) {
            this.historyFile = historyFile;
            return this;
        }

        /**
         * Registers a callback invoked once the reader is wired, immediately
         * before the REPL starts. Mirrors {@code ShellBuilder.onReaderReady(Consumer)}.
         *
         * @param onReaderReady the callback, or {@code null} for none
         * @return this builder
         */
        public Builder onReaderReady(Consumer<LineReader> onReaderReady) {
            this.onReaderReady = onReaderReady == null ? null
                    : (reader, registry) -> onReaderReady.accept(reader);
            return this;
        }

        /**
         * Registers a callback invoked once the reader and the command registry
         * are both wired, immediately before the REPL starts. Mirrors
         * {@code ShellBuilder.onReaderReady(BiConsumer)}.
         * <p>
         * This is the hook for completion. groovysh has already assigned
         * {@code reader.setCompleter(registry.completer())} by the time the
         * callback runs, so an embedder contributing its own completion wraps
         * that, for example
         * {@code reader.setCompleter(new AggregateCompleter(registry.completer(), mine))}.
         * Candidates from an {@code AggregateCompleter} are merged, so a
         * completer added this way supplements groovysh's rather than replacing
         * it; assign a completer of your own to replace it outright.
         *
         * @param onReaderReady the callback, or {@code null} for none
         * @return this builder
         */
        public Builder onReaderReady(BiConsumer<LineReader, GroovySystemRegistry> onReaderReady) {
            this.onReaderReady = onReaderReady;
            return this;
        }

        /**
         * Builds an immutable options object.
         *
         * @return the options
         */
        public GroovyshOptions build() {
            return new GroovyshOptions(this);
        }
    }
}
