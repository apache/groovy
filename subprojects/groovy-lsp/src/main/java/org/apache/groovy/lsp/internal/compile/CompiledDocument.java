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
package org.apache.groovy.lsp.internal.compile;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.WarningMessage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * One compiled Groovy source: its AST, originating unit, and collected
 * messages.
 */
public final class CompiledDocument {

    private final URI uri;
    private final int version;
    private final String text;
    private final ModuleNode module;
    private final SourceUnit sourceUnit;
    private final List<Message> errors;
    private final List<WarningMessage> warnings;

    /**
     * Creates a compiled-document snapshot, copying messages from {@code collector}.
     *
     * @param uri document URI
     * @param version LSP version, or {@code -1} when compiled from disk
     * @param text source text used for this compile
     * @param module AST module, possibly {@code null} when parse failed hard
     * @param sourceUnit compiler source unit
     * @param collector error collector, possibly {@code null}
     */
    public CompiledDocument(final URI uri, final int version, final String text, final ModuleNode module,
                            final SourceUnit sourceUnit, final ErrorCollector collector) {
        this.uri = uri;
        this.version = version;
        this.text = text == null ? "" : text;
        this.module = module;
        this.sourceUnit = sourceUnit;
        this.errors = collector != null && collector.getErrors() != null
                ? List.copyOf(new ArrayList<>(collector.getErrors())) : List.of();
        this.warnings = collector != null && collector.getWarnings() != null
                ? List.copyOf(new ArrayList<>(collector.getWarnings())) : List.of();
    }

    public URI getUri() {
        return uri;
    }

    public int getVersion() {
        return version;
    }

    public String getText() {
        return text;
    }

    public ModuleNode getModule() {
        return module;
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public List<Message> getErrors() {
        return errors;
    }

    public List<WarningMessage> getWarnings() {
        return warnings;
    }
}
