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
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SelectionRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Nested selection ranges from the AST path.
 */
public final class SelectionRangeService {

    public List<SelectionRange> selectionRanges(final TextDocument document, final CompilationSnapshot snapshot,
                                                final List<Position> positions, final PositionEncoding encoding) {
        List<SelectionRange> ranges = new ArrayList<>();
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();
        for (Position position : positions) {
            SelectionRange current = null;
            if (module != null) {
                List<ASTNode> path = AstQuery.containing(module, document, position, encoding);
                for (int i = path.size() - 1; i >= 0; i--) {
                    Range range = Positions.toRange(path.get(i), document.getText(), encoding);
                    if (range != null) {
                        current = new SelectionRange(range, current);
                    }
                }
            }
            if (current == null) {
                current = new SelectionRange(new Range(position, position), null);
            }
            ranges.add(current);
        }
        return ranges;
    }
}
