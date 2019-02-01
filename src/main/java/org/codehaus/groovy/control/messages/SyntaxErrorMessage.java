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
package org.codehaus.groovy.control.messages;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;

/**
 * A class for error messages produced by the parser system.
 */
public class SyntaxErrorMessage extends Message {
    protected SyntaxException cause;
    protected SourceUnit source;

    public SyntaxErrorMessage(SyntaxException cause, SourceUnit source) {
        this.cause = cause;
        this.source = source;
        cause.setSourceLocator(source.getName());
    }

    /**
     * Returns the underlying SyntaxException.
     */
    public SyntaxException getCause() {
        return this.cause;
    }

    /**
     * Writes out a nicely formatted summary of the syntax error.
     */
    public void write(PrintWriter output, Janitor janitor) {
        String name = source.getName();
        int line = getCause().getStartLine();
        int column = getCause().getStartColumn();
        String sample = source.getSample(line, column, janitor);

        output.print(name + ": " + line + ": " + getCause().getMessage());
        if (sample != null) {
            output.println();
            output.print(sample);
            output.println();
        }
    }

}
