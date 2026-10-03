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

package org.apache.groovy.lsp.internal.feature;

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Read/write highlights for the symbol under the caret.
 */
public final class HighlightService {

    public List<DocumentHighlight> highlights(final TextDocument document, final CompilationSnapshot snapshot,
                                              final Position position, final PositionEncoding encoding) {
        if (document == null || document.getUri() == null) {
            return List.of();
        }
        List<Location> refs = new NavigationService()
                .references(document, snapshot, position, encoding, true);
        List<DocumentHighlight> highlights = new ArrayList<>();
        String uri = document.getUri().toString();
        for (Location location : refs) {
            if (location != null && uri.equals(location.getUri())) {
                highlights.add(new DocumentHighlight(location.getRange(), DocumentHighlightKind.Text));
            }
        }
        return highlights;
    }
}
