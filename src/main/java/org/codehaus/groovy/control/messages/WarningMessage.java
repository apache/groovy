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
 * A class for warning messages.
 */
public class WarningMessage extends LocatedMessage {

    //--------------------------------------------------------------------------
    // WARNING LEVELS

    /** Ignore all (for querying) */
    public static final int NONE = 0;
    /** Warning indicates likely error */
    public static final int LIKELY_ERRORS = 1;
    /** Warning indicates possible error */
    public static final int POSSIBLE_ERRORS = 2;
    /** Warning indicates paranoia on the part of the compiler */
    public static final int PARANOIA = 3;

    /**
     * Returns true if a warning would be relevant to the specified level.
     */
    public static boolean isRelevant(final int actual, final int limit) {
        return actual <= limit;
    }

    /**
     * Returns true if this message is as or more important than the
     * specified importance level.
     */
    public boolean isRelevant(final int importance) {
        return isRelevant(this.importance, importance);
    }

    //--------------------------------------------------------------------------

    /** The warning level (for filtering). */
    private final int importance;

    /**
     * Creates a new warning message.
     *
     * @param importance the warning level
     * @param message    the message text
     * @param context    for locating the offending source text
     */
    public WarningMessage(final int importance, final String message, final CSTNode context, final SourceUnit owner) {
        super(message, context, owner);
        this.importance = importance;
    }

    /**
     * Creates a new warning message.
     *
     * @param importance the warning level
     * @param message    the message text
     * @param data       data needed for generating the message
     * @param context    for locating the offending source text
     */
    public WarningMessage(final int importance, final String message, final Object data, final CSTNode context, final SourceUnit owner) {
        super(message, data, context, owner);
        this.importance = importance;
    }

    @Override
    public void write(final PrintWriter writer, final Janitor janitor) {
        writer.print("warning: ");
        super.write(writer, janitor);
    }
}
