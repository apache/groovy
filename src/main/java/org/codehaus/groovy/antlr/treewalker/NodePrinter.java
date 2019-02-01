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

import java.io.PrintStream;

/**
 * A simple antlr AST visitor that outputs the tokenName of each node in a pseudo xml style.
 */
public class NodePrinter extends VisitorAdapter {
    private final String[] tokenNames;
    private final PrintStream out;

    /**
     * A visitor that prints a pseudo xml output to the supplied PrintStream
     * @param out supplied PrintStream to output nodes to
     * @param tokenNames an array of token names to use
     */
    public NodePrinter(PrintStream out,String[] tokenNames) {
        this.tokenNames = tokenNames;
        this.out = out;
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            out.print("<"+ tokenNames[t.getType()] + ">");
        } else {
            out.print("</" + tokenNames[t.getType()] + ">");
        }
    }
}
