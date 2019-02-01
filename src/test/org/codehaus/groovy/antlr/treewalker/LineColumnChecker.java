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
package org.codehaus.groovy.antlr.treewalker;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.SourceBuffer;

/**
 * Source AST Visitor that will assert each node has a correct line/column info
 * given a SourceBuffer
 */
public class LineColumnChecker extends VisitorAdapter {
    private SourceBuffer sourceBuffer;
    private String[] tokenNames;

    public LineColumnChecker(SourceBuffer sourceBuffer, String[] tokenNames) {
        this.sourceBuffer = sourceBuffer;
        this.tokenNames = tokenNames;
    }
    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT ) {
            System.out.println("[" + tokenNames[t.getType()] + "]");
            int line = t.getLine();
            int column = t.getColumn();
            int lineLast = t.getLineLast();
            int columnLast = t.getColumnLast();

            System.out.println("" + line + " / " +  column + " - " + lineLast + " / " + columnLast);
            if (line > 0 && column > 0 && lineLast > 0 && columnLast > 0) {
                System.out.println("" + sourceBuffer.getSnippet(new LineColumn(line, column), new LineColumn(lineLast, columnLast)));
            } else {
                System.out.println("ZERO");
            }
        } else if (visit == CLOSING_VISIT) {
            System.out.println();
        }

    }
}
