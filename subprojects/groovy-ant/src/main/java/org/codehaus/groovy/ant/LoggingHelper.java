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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Helper to make logging from Ant easier.
 */
public class LoggingHelper {
    private final Task owner;

    /**
     * Creates a logging helper for the supplied Ant task.
     *
     * @param owner the task that should receive log messages
     */
    public LoggingHelper(final Task owner) {
        assert owner != null;
        this.owner = owner;
    }

    /**
     * Logs an error message.
     *
     * @param msg the message to log
     */
    public void error(final String msg) {
        owner.log(msg, Project.MSG_ERR);
    }

    /**
     * Logs an error message together with its cause.
     *
     * @param msg the message to log
     * @param t the associated failure
     */
    public void error(final String msg, final Throwable t) {
        owner.log(msg, t, Project.MSG_ERR);
    }

    /**
     * Logs a warning message.
     *
     * @param msg the message to log
     */
    public void warn(final String msg) {
        owner.log(msg, Project.MSG_WARN);
    }

    /**
     * Logs an informational message.
     *
     * @param msg the message to log
     */
    public void info(final String msg) {
        owner.log(msg, Project.MSG_INFO);
    }

    /**
     * Logs a verbose diagnostic message.
     *
     * @param msg the message to log
     */
    public void verbose(final String msg) {
        owner.log(msg, Project.MSG_VERBOSE);
    }

    /**
     * Logs a debug message.
     *
     * @param msg the message to log
     */
    public void debug(final String msg) {
        owner.log(msg, Project.MSG_DEBUG);
    }
}
