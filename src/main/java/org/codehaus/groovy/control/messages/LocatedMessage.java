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
    private CSTNode context;

    /**
     * Creates a located message with the supplied text.
     *
     * @param message the message text
     * @param context the source location
     * @param source the owning source unit
     */
    public LocatedMessage(String message, CSTNode context, SourceUnit source) {
        super(message, source);
        this.context = context;
    }

    /**
     * Creates a located message with supplemental data.
     *
     * @param message the message text
     * @param data supplemental message data
     * @param context the source location
     * @param source the owning source unit
     */
    public LocatedMessage(String message, Object data, CSTNode context, SourceUnit source) {
        super(message, data, source);
        this.context = context;
    }

    /**
     * Returns the CST node that identifies the message location.
     *
     * @return the source location node
     */
    public CSTNode getContext() {
        return context;
    }

    /**
     * Writes the message together with source-location information.
     *
     * @param writer the destination writer
     * @param janitor the cleanup helper for temporary source access
     */
    @Override
    public void write(PrintWriter writer, Janitor janitor) {
        if (owner instanceof SourceUnit source) {

            String name = source.getName();
            int line = context.getStartLine();
            int column = context.getStartColumn();
            String sample = source.getSample(line, column, janitor);

            if (sample != null) {
                writer.println(sample);
            }

            writer.println(name + ": " + line + ": " + this.message);
            writer.println("");
        } else {
            writer.println("<No Relevant Source>: " + this.message);
            writer.println("");
        }
    }
}
