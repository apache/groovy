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
    protected String message;  // Message text
    protected Object data;     // Data, when the message text is an I18N identifier
    protected ProcessingUnit owner;

    public SimpleMessage(String message, ProcessingUnit source) {
        this(message, null, source);
    }

    public SimpleMessage(String message, Object data, ProcessingUnit source) {
        this.message = message;
        this.data = null;
        this.owner = source;
    }

    @Override
    public void write(PrintWriter writer, Janitor janitor) {
        if (owner instanceof SourceUnit) {
            String name = ((SourceUnit) owner).getName();
            writer.println("" + name + ": " + message);
        } else {
            writer.println(message);
        }
    }

    public String getMessage() {
        return message;
    }

}
