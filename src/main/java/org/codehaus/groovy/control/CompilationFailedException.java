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
package org.codehaus.groovy.control;

import groovy.lang.GroovyRuntimeException;


/**
 * Thrown when compilation fails from source errors.
 */
public class CompilationFailedException extends GroovyRuntimeException {

    private static final long serialVersionUID = 4500269747315896966L;
    protected int phase;   // The phase in which the failures occurred
    protected ProcessingUnit unit;    // The *Unit object this exception wraps

    public CompilationFailedException(int phase, ProcessingUnit unit, Throwable cause) {
        super(Phases.getDescription(phase) + " failed", cause);
        this.phase = phase;
        this.unit = unit;
    }


    public CompilationFailedException(int phase, ProcessingUnit unit) {
        super(Phases.getDescription(phase) + " failed");
        this.phase = phase;
        this.unit = unit;
    }


    /**
     * Formats the error data as a String.
     */

    /*public String toString() {
        StringWriter data = new StringWriter();
        PrintWriter writer = new PrintWriter(data);
        Janitor janitor = new Janitor();

        try {
            unit.getErrorReporter().write(writer, janitor);
        }
        finally {
            janitor.cleanup();
        }

        return data.toString();
    }*/


    /**
     * Returns the ProcessingUnit in which the error occurred.
     */

    public ProcessingUnit getUnit() {
        return this.unit;
    }

}
