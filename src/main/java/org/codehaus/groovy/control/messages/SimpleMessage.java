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
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.io.PrintWriter;

/**
 * A base class for compilation messages.
 */
public class SimpleMessage extends Message {

    /** used when {@link #message} is an I18N identifier */
    protected Object data;
    /**
     * Message text to render.
     */
    protected String message;
    /**
     * Processing unit that owns the message.
     */
    protected ProcessingUnit owner;

    /**
     * Creates a simple message with no auxiliary data.
     *
     * @param message the message text
     * @param owner the owning processing unit
     */
    public SimpleMessage(final String message, final ProcessingUnit owner) {
        this(message, null, owner);
    }

    /**
     * Creates a simple message with optional auxiliary data.
     *
     * @param message the message text
     * @param data supplemental message data
     * @param owner the owning processing unit
     */
    public SimpleMessage(final String message, final Object data, final ProcessingUnit owner) {
        this.message = message;
        this.owner = owner;
        this.data = data;
    }

    /**
     * Returns the message text.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Writes this message, prefixing it with the source name when available.
     *
     * @param writer the destination writer
     * @param janitor the cleanup helper for temporary source access
     */
    @Override
    public void write(final PrintWriter writer, final Janitor janitor) {
        if (owner instanceof SourceUnit) {
            writer.println(((SourceUnit) owner).getName() + ": " + message);
        } else {
            writer.println(message);
        }
    }
}
