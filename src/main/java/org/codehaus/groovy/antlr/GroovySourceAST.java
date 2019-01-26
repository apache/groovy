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
package org.codehaus.groovy.antlr;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * We have an AST subclass so we can track source information.
 * Very odd that ANTLR doesn't do this by default.
 */
public class GroovySourceAST extends CommonAST implements Comparable, SourceInfo {
    private static final long serialVersionUID = 9116765466538981906L;
    private int line;
    private int col;
    private int lineLast;
    private int colLast;
    private String snippet;

    public GroovySourceAST() {
    }

    public GroovySourceAST(Token t) {
        super(t);
    }

    public void initialize(AST ast) {
        super.initialize(ast);
        line = ast.getLine();
        col = ast.getColumn();
        if (ast instanceof GroovySourceAST) {
            GroovySourceAST node = (GroovySourceAST)ast;
            lineLast = node.getLineLast();
            colLast = node.getColumnLast();
        }
    }

    public void initialize(Token t) {
        super.initialize(t);
        line = t.getLine();
        col = t.getColumn();
        if (t instanceof SourceInfo) {
            SourceInfo info = (SourceInfo) t;
            lineLast = info.getLineLast();
            colLast  = info.getColumnLast();
        }
    }

    public void setLast(Token last) {
        lineLast = last.getLine();
        colLast = last.getColumn();
    }

    public int getLineLast() {
        return lineLast;
    }

    public void setLineLast(int lineLast) {
        this.lineLast = lineLast;
    }

    public int getColumnLast() {
        return colLast;
    }

    public void setColumnLast(int colLast) {
        this.colLast = colLast;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return (line);
    }

    public void setColumn(int column) {
        this.col = column;
    }

    public int getColumn() {
        return (col);
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSnippet() {
        return snippet;
    }

    public int compareTo(Object object) {
        if (object == null) {
            return 0;
        }
        if (!(object instanceof AST)) {
            return 0;
        }
        AST that = (AST) object;

        // todo - possibly check for line/col with values of 0 or less...

        if (this.getLine() < that.getLine()) {
            return -1;
        }
        if (this.getLine() > that.getLine()) {
            return 1;
        }

        if (this.getColumn() < that.getColumn()) {
            return -1;
        }
        if (this.getColumn() > that.getColumn()) {
            return 1;
        }

        return 0;
    }

    public GroovySourceAST childAt(int position) {
        int cur = 0;
        AST child = this.getFirstChild();
        while (child != null && cur <= position) {
            if (cur == position) {
                return (GroovySourceAST) child;
            }
            cur++;
            child = child.getNextSibling();
        }
        return null;
    }

    public GroovySourceAST childOfType(int type) {
        AST child = this.getFirstChild();
        while (child != null) {
            if (child.getType() == type) { return (GroovySourceAST)child; }
            child = child.getNextSibling();
        }
        return null;
    }

    public List<GroovySourceAST> childrenOfType(int type) {
        List<GroovySourceAST> result = new ArrayList<>();
        AST child = this.getFirstChild();
        while (child != null) {
            if (child.getType() == type) { result.add((GroovySourceAST) child); }
            child = child.getNextSibling();
        }
        return result;
    }

}
