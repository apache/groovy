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
import org.codehaus.groovy.syntax.CSTNode;

import java.io.PrintWriter;

/**
 * A base class for compilation messages.
 */
public class LocatedMessage extends SimpleMessage {

    /** The CSTNode that indicates the location to which the message applies. */
    private final CSTNode context;

    public LocatedMessage(String message, CSTNode context, SourceUnit source) {
        super(message, source);
        this.context = context;
    }

    public LocatedMessage(String message, Object data, CSTNode context, SourceUnit source) {
        super(message, data, source);
        this.context = context;
    }

    public CSTNode getContext() {
        return context;
    }

    public void write(PrintWriter writer, Janitor janitor) {
        if (owner instanceof SourceUnit) {
            SourceUnit source = (SourceUnit) owner;

            String name = source.getName();
            int line = context.getStartLine();
            int column = context.getStartColumn();
            String sample = source.getSample(line, column, janitor);

            if (sample != null) {
                writer.println(source.getSample(line, column, janitor));
            }

            writer.println(name + ": " + line + ": " + this.message);
            writer.println("");
        } else {
            writer.println("<No Relevant Source>: " + this.message);
            writer.println("");
        }
    }
}
