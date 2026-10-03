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
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Links from import names to their source.
 */
public final class DocumentLinkService {

    public List<DocumentLink> documentLinks(final CompiledDocument document, final CompilationSnapshot snapshot,
                                            final PositionEncoding encoding) {
        List<DocumentLink> links = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return links;
        }
        for (ImportNode imp : ImportSupport.allImports(document.getModule())) {
            addDocumentLink(links, document, snapshot, encoding, imp);
        }
        return links;
    }

    private static void addDocumentLink(final List<DocumentLink> links, final CompiledDocument document,
                                        final CompilationSnapshot snapshot, final PositionEncoding encoding,
                                        final ImportNode imp) {
        Range range = Positions.toRange(imp, document.getText(), encoding);
        if (range == null) {
            return;
        }
        String target = importTarget(imp, document, snapshot);
        if (target != null) {
            links.add(new DocumentLink(range, target));
        }
    }

    private static String importTarget(final ImportNode imp, final CompiledDocument document,
                                       final CompilationSnapshot snapshot) {
        String className = imp.getClassName();
        if (className == null) {
            return null;
        }
        String workspace = workspaceTypeUri(className, snapshot);
        if (workspace != null) {
            return workspace;
        }
        if (document.getUri() == null || !"file".equalsIgnoreCase(document.getUri().getScheme())) {
            return null;
        }
        Path parent = Uris.toPath(document.getUri()).getParent();
        if (parent == null) {
            return null;
        }
        return Uris.normalize(parent.resolve(className.replace('.', '/') + ".groovy").toUri()).toString();
    }

    private static String workspaceTypeUri(final String className, final CompilationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        for (CompiledDocument other : snapshot.documents()) {
            if (other.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : other.getModule().getClasses()) {
                if (className.equals(classNode.getName())) {
                    return other.getUri().toString();
                }
            }
        }
        return null;
    }
}
