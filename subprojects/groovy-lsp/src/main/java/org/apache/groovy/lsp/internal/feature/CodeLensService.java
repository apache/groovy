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

import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference-count code lenses.
 */
public final class CodeLensService {

    private static void addCodeLens(final List<CodeLens> lenses, final TextDocument open,
                                    final CompiledDocument document, final CompilationSnapshot snapshot,
                                    final PositionEncoding encoding, final NavigationService navigation,
                                    final ASTNode node) {
        if (!(node instanceof ClassNode) && !(node instanceof MethodNode)) {
            return;
        }
        if (node instanceof MethodNode method && method.isSynthetic()) {
            return;
        }
        Range range = Positions.toNameRange((AnnotatedNode) node, document.getText(), encoding);
        if (range == null) {
            return;
        }
        int count = navigation.references(open, snapshot, range.getStart(), encoding, false).size();
        String label = count == 1 ? "1 reference" : count + " references";
        CodeLens lens = new CodeLens(range);
        lens.setCommand(new Command(label, "groovy.lsp.showReferences",
                List.of(document.getUri().toString(), range.getStart().getLine(), range.getStart().getCharacter())));
        lenses.add(lens);
    }

    public List<CodeLens> codeLenses(final CompiledDocument document, final CompilationSnapshot snapshot,
                                     final PositionEncoding encoding) {
        List<CodeLens> lenses = new ArrayList<>();
        if (document == null || document.getModule() == null || snapshot == null) {
            return lenses;
        }
        TextDocument open = document.toTextDocument();
        NavigationService navigation = new NavigationService();
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            addCodeLens(lenses, open, document, snapshot, encoding, navigation, node);
        }
        return lenses;
    }
}
