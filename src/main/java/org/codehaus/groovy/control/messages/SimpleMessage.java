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

    /** used when {@link message} is an I18N identifier */
    protected Object data;
    protected String message;
    protected ProcessingUnit owner;

    public SimpleMessage(final String message, final ProcessingUnit owner) {
        this(message, null, owner);
    }

    public SimpleMessage(final String message, final Object data, final ProcessingUnit owner) {
        this.message = message;
        this.owner = owner;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void write(final PrintWriter writer, final Janitor janitor) {
        if (owner instanceof SourceUnit) {
            writer.println(((SourceUnit) owner).getName() + ": " + message);
        } else {
            writer.println(message);
        }
    }
}
