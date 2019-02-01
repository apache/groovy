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

package org.codehaus.groovy.tools;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.GroovyExceptionInterface;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Provides services for reporting compilation errors to the
 * user.  Primary entry point is <code>write()</code>.
 */
public class ErrorReporter {
    private Throwable base = null;    // The exception on which to report
    private boolean debug = false;   // If true, stack traces are always output

    private Object output = null;    // The stream/writer to which to output


    /**
     * Configures a new Reporter.  Default mode is not to report a stack trace unless
     * the error was not of one of the supported types.
     *
     * @param e the exception on which to report
     */
    public ErrorReporter(Throwable e) {
        this.base = e;
    }


    /**
     * Configures a new Reporter.
     *
     * @param e     the exception on which to report
     * @param debug if set, stack traces will be output for all reports
     */
    public ErrorReporter(Throwable e, boolean debug) {
        this.base = e;
        this.debug = debug;
    }


    /**
     * Writes the error to the specified <code>PrintStream</code>.
     */
    public void write(PrintStream stream) {
        this.output = stream;
        dispatch(base, false);
        stream.flush();
    }


    /**
     * Writes the error to the specified <code>PrintWriter</code>.
     */
    public void write(PrintWriter writer) {
        this.output = writer;
        dispatch(base, false);
        writer.flush();
    }


    /**
     * Runs the report once all initialization is complete.
     */
    protected void dispatch(Throwable object, boolean child) {
        if (object instanceof CompilationFailedException) {
            report((CompilationFailedException) object, child);
        } else if (object instanceof GroovyExceptionInterface) {
            report((GroovyExceptionInterface) object, child);
        } else if (object instanceof GroovyRuntimeException) {
            report((GroovyRuntimeException) object, child);
        } else if (object instanceof Exception) {
            report((Exception) object, child);
        } else {
            report(object, child);
        }

    }


    //---------------------------------------------------------------------------
    // REPORTING ROUTINES


    /**
     * For CompilationFailedException.
     */
    protected void report(CompilationFailedException e, boolean child) {
        println(e.toString());
        stacktrace(e, false);
    }


    /**
     * For GroovyException.
     */
    protected void report(GroovyExceptionInterface e, boolean child) {
        println(((Exception) e).getMessage());
        stacktrace((Exception) e, false);
    }


    /**
     * For Exception.
     */
    protected void report(Exception e, boolean child) {
        println(e.getMessage());
        stacktrace(e, false);
    }


    /**
     * For everything else.
     */
    protected void report(Throwable e, boolean child) {
        println(">>> a serious error occurred: " + e.getMessage());
        stacktrace(e, true);
    }


    //---------------------------------------------------------------------------
    // GENERAL SUPPORT ROUTINES


    /**
     * Prints a line to the underlying <code>PrintStream</code>
     */
    protected void println(String line) {
        if (output instanceof PrintStream) {
            ((PrintStream) output).println(line);
        } else {
            ((PrintWriter) output).println(line);
        }
    }

    protected void println(StringBuffer line) {
        if (output instanceof PrintStream) {
            ((PrintStream) output).println(line);
        } else {
            ((PrintWriter) output).println(line);
        }
    }


    /**
     * Displays an exception's stack trace, if <code>debug</code> or
     * <code>always</code>.
     */
    protected void stacktrace(Throwable e, boolean always) {
        if (debug || always) {
            println(">>> stacktrace:");
            if (output instanceof PrintStream) {
                e.printStackTrace((PrintStream) output);
            } else {
                e.printStackTrace((PrintWriter) output);
            }
        }
    }


}
