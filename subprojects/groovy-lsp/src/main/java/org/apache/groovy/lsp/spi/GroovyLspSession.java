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
import org.eclipse.lsp4j.WorkspaceEdit;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Narrow session view for {@link GroovyLspExtension}. Plugins must not
 * compile or publish diagnostics through this type. Use
 * {@link GroovyLspExtension#afterCompile} to see the live AST.
 */
@Incubating
public interface GroovyLspSession {

    /**
     * Asks the client to apply {@code edit} when {@code workspace/applyEdit}
     * is available. No-op if there is no client or the client did not
     * advertise the capability.
     *
     * @param edit a workspace edit
     */
    void applyEdit(WorkspaceEdit edit);

    /**
     * Open-buffer or last-compiled text for {@code uri}.
     *
     * @param uri document URI
     * @return the text, or {@code null} when unknown
     */
    default String documentText(URI uri) {
        return null;
    }

    /**
     * Workspace folders in registration order.
     *
     * @return folders, never {@code null}
     */
    default List<URI> workspaceFolders() {
        return List.of();
    }

    /**
     * Client {@code groovy.classpath} entries.
     *
     * @return classpath strings, never {@code null}
     */
    default List<String> classpath() {
        return List.of();
    }

    /**
     * Client {@code groovy.sourcePaths} entries.
     *
     * @return source paths, never {@code null}
     */
    default List<String> sourcePaths() {
        return List.of();
    }

    /**
     * Other {@code groovy.*} settings the core does not interpret
     * ({@code groovy.dependencies}, a GDSL path, …). Values are the
     * client's string form.
     *
     * @return extra settings, never {@code null}
     */
    default Map<String, String> extraSettings() {
        return Map.of();
    }

    /**
     * One extra setting.
     *
     * @param key setting name without the {@code groovy.} prefix
     * @return the value, or {@code null}
     */
    default String extraSetting(final String key) {
        return key == null ? null : extraSettings().get(key);
    }

    /**
     * Negotiated LSP position encoding ({@code utf-16}, {@code utf-8},
     * or {@code utf-32}).
     *
     * @return the protocol name, never {@code null}
     */
    default String positionEncoding() {
        return "utf-16";
    }
}
