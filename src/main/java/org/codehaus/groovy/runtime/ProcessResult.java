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
package org.codehaus.groovy.runtime;

/**
 * Captures the result of executing an external process, including
 * the standard output, standard error, and exit code.
 *
 * <pre>
 * def result = "ls -la".execute().waitForResult()
 * assert result.ok
 * println result.out
 * </pre>
 *
 * @since 6.0.0
 * @see ProcessGroovyMethods#waitForResult(Process)
 */
public class ProcessResult {
    private final String out;
    private final String err;
    private final int exitCode;

    public ProcessResult(final String out, final String err, final int exitCode) {
        this.out = out;
        this.err = err;
        this.exitCode = exitCode;
    }

    /**
     * Returns the standard output of the process as a String.
     */
    public String getOut() {
        return out;
    }

    /**
     * Returns the standard error of the process as a String.
     */
    public String getErr() {
        return err;
    }

    /**
     * Returns the exit code of the process.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Returns {@code true} if the process exited with code 0.
     */
    public boolean isOk() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "ProcessResult(exitCode=" + exitCode
                + ", out=" + abbreviate(out)
                + ", err=" + abbreviate(err) + ")";
    }

    private static String abbreviate(final String s) {
        if (s == null) return "null";
        String trimmed = s.trim();
        if (trimmed.length() <= 60) return "\"" + trimmed + "\"";
        return "\"" + trimmed.substring(0, 57) + "...\"";
    }
}