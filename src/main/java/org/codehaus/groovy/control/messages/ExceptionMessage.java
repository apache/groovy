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

import java.io.PrintWriter;

/**
 * A class for error messages produced by the parser system.
 */
public class ExceptionMessage extends Message {
    protected boolean verbose = true;

    private Exception cause = null;   // The exception source of the message, if any
    ProcessingUnit owner = null;

    public ExceptionMessage(Exception cause, boolean v, ProcessingUnit owner) {
        this.verbose = v;
        this.cause = cause;
        this.owner = owner;
    }

    /**
     * Returns the underlying Exception.
     */

    public Exception getCause() {
        return this.cause;
    }


    /**
     * Writes out a nicely formatted summary of the exception.
     */

    public void write(PrintWriter output, Janitor janitor) {
        String description = "General error during " + owner.getPhaseDescription() + ": ";

        String message = cause.getMessage();
        if (message != null) {
            output.println(description + message);
        } else {
            output.println(description + cause);
        }
        output.println();

        //if (verbose) {
        cause.printStackTrace(output);
        //}
    }

}



