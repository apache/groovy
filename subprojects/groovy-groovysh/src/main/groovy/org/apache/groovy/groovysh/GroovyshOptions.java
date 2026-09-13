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
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jline.console.CommandRegistry;
import org.jline.console.ConsoleEngine;
import org.jline.console.SystemRegistry;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Configuration for embedding groovysh programmatically via
 * {@link Main#start(GroovyshOptions, String[])}.
 * <p>
 * Binding variables stay in {@link #getBindings()}; they are not mixed
 * with embedding hooks. Construct with {@link #builder()}.
 *
 * @since 7.0.0
 */
@Incubating
@SuppressWarnings("deprecation")
public final class GroovyshOptions {

    /**
     * Handles a value produced by the REPL loop.
     */
    @FunctionalInterface
    public interface ResultHandler {
        /**
         * Renders {@code result} after a line has been executed.
         *
         * @param console the console engine used by the session
         * @param result the value returned by {@code SystemRegistry.execute}, which may be {@code null}
         * @throws Exception if rendering fails; groovysh then routes it to the error handler
         */
        void handle(ConsoleEngine console, Object result) throws Exception;
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
         * @param registry the system registry for the session
         * @param error the thrown error or exception
         */
        void handle(SystemRegistry registry, Throwable error);
    }

    private final CompilerConfiguration compilerConfiguration;
    private final GroovyEngine engine;
    private final Map<String, Object> bindings;
    private final List<CommandRegistry> extraCommandRegistries;
    private final Supplier<String> prompt;
    private final ResultHandler resultHandler;
    private final ErrorHandler errorHandler;
    private final boolean showBanner;
    private final Terminal terminal;

    private GroovyshOptions(Builder builder) {
        this.compilerConfiguration = builder.compilerConfiguration;
        this.engine = builder.engine;
        this.bindings = Collections.unmodifiableMap(new LinkedHashMap<>(builder.bindings));
        this.extraCommandRegistries = Collections.unmodifiableList(new ArrayList<>(builder.extraCommandRegistries));
        this.prompt = builder.prompt;
        this.resultHandler = builder.resultHandler;
        this.errorHandler = builder.errorHandler;
        this.showBanner = builder.showBanner;
        this.terminal = builder.terminal;
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
     * Extra command registries appended after groovysh's own registries.
     *
     * @return an unmodifiable list, never {@code null}
     */
    public List<CommandRegistry> getExtraCommandRegistries() {
        return extraCommandRegistries;
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
     * Result renderer. {@code null} means print {@code result?.toString()} via the console engine.
     *
     * @return the result handler, or {@code null}
     */
    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    /**
     * Error renderer. {@code null} means {@code SystemRegistry.trace(error)}.
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
     * Builder for {@link GroovyshOptions}.
     *
     * @since 7.0.0
     */
    @Incubating
    public static final class Builder {
        private CompilerConfiguration compilerConfiguration;
        private GroovyEngine engine;
        private final Map<String, Object> bindings = new LinkedHashMap<>();
        private final List<CommandRegistry> extraCommandRegistries = new ArrayList<>();
        private Supplier<String> prompt;
        private ResultHandler resultHandler;
        private ErrorHandler errorHandler;
        private boolean showBanner = true;
        private Terminal terminal;

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
         * Appends an extra command registry.
         *
         * @param registry the registry to append, must not be {@code null}
         * @return this builder
         */
        public Builder extraCommandRegistry(CommandRegistry registry) {
            this.extraCommandRegistries.add(Objects.requireNonNull(registry, "registry"));
            return this;
        }

        /**
         * Appends extra command registries.
         *
         * @param registries registries to append; {@code null} is ignored
         * @return this builder
         */
        public Builder extraCommandRegistries(Iterable<? extends CommandRegistry> registries) {
            if (registries != null) {
                for (CommandRegistry registry : registries) {
                    extraCommandRegistry(registry);
                }
            }
            return this;
        }

        /**
         * Sets the prompt supplier. Called once per REPL iteration.
         *
         * @param prompt the supplier, or {@code null} for {@code "groovy> "}
         * @return this builder
         */
        public Builder prompt(Supplier<String> prompt) {
            this.prompt = prompt;
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
         * @param errorHandler the handler, or {@code null} for {@code SystemRegistry.trace}
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
         *
         * @param terminal the terminal, or {@code null} to build the default
         * @return this builder
         */
        public Builder terminal(Terminal terminal) {
            this.terminal = terminal;
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
