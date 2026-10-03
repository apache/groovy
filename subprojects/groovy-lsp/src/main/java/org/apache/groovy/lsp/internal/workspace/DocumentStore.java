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
package org.apache.groovy.lsp.internal.workspace;

import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.util.Uris;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe store of open text documents. The client's buffer is the
 * source of truth for open files.
 */
public final class DocumentStore {

    private final ConcurrentMap<URI, TextDocument> documents = new ConcurrentHashMap<>();

    /**
     * Opens or replaces a document from {@code textDocument/didOpen}.
     *
     * @param item the opened document
     * @return the stored snapshot
     */
    public TextDocument open(final TextDocumentItem item) {
        URI uri = Uris.parse(item.getUri());
        TextDocument document = new TextDocument(uri, item.getLanguageId(), item.getVersion(), item.getText());
        documents.put(uri, document);
        return document;
    }

    /**
     * Applies {@code textDocument/didChange} events.
     *
     * @param uriString document URI
     * @param version new version
     * @param changes change events
     * @param encoding negotiated encoding
     * @return the updated snapshot, or {@code null} when the document is unknown
     */
    public TextDocument change(final String uriString, final int version,
                               final List<TextDocumentContentChangeEvent> changes,
                               final PositionEncoding encoding) {
        URI uri = Uris.parse(uriString);
        return documents.compute(uri, (key, current) -> {
            if (current == null) {
                return new TextDocument(uri, "groovy", version, applyUnknown(changes));
            }
            return current.apply(version, changes, encoding);
        });
    }

    /**
     * Removes a document after {@code textDocument/didClose}.
     *
     * @param uriString document URI
     * @return the removed snapshot, or {@code null}
     */
    public TextDocument close(final String uriString) {
        return documents.remove(Uris.parse(uriString));
    }

    /**
     * Returns the open document, or {@code null}.
     *
     * @param uriString document URI
     * @return the snapshot
     */
    public TextDocument get(final String uriString) {
        return documents.get(Uris.parse(uriString));
    }

    /**
     * Returns the open document, or {@code null}.
     *
     * @param uri document URI
     * @return the snapshot
     */
    public TextDocument get(final URI uri) {
        return documents.get(Uris.normalize(uri));
    }

    /**
     * Returns a snapshot of every open document.
     *
     * @return open documents
     */
    public Collection<TextDocument> snapshot() {
        return List.copyOf(documents.values());
    }

    /**
     * @return the number of open documents
     */
    public int size() {
        return documents.size();
    }

    /**
     * Drops every open document.
     */
    public void clear() {
        documents.clear();
    }

    private static String applyUnknown(final List<TextDocumentContentChangeEvent> changes) {
        if (changes == null || changes.isEmpty()) {
            return "";
        }
        TextDocumentContentChangeEvent last = changes.get(changes.size() - 1);
        return last.getText() == null ? "" : last.getText();
    }
}
