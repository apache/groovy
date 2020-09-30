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
package org.codehaus.groovy.control.io;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Janitor;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * For ReaderSources that can choose a parent class, a base that
 * provides common functionality.
 */
public abstract class AbstractReaderSource implements ReaderSource {

    protected CompilerConfiguration configuration;

    public AbstractReaderSource(final CompilerConfiguration configuration) {
        this.configuration = configuration != null ? configuration : CompilerConfiguration.DEFAULT;
    }

    /**
     * Returns true if the source can be restarted (ie. if getReader()
     * will return non-null on subsequent calls.
     */
    @Override
    public boolean canReopenSource() {
        return true;
    }

    private BufferedReader lineSource = null;    // If set, a reader on the current source file
    private String line = null;    // The last line read from the current source file
    private int number = 0;       // The last line number read

    /**
     * Returns a line from the source, or null, if unavailable.  If
     * you supply a Janitor, resources will be cached.
     */
    @Override
    public String getLine(int lineNumber, Janitor janitor) {
        // If the source is already open and is passed the line we
        // want, close it.
        if (lineSource != null && number > lineNumber) {
            cleanup();
        }

        // If the line source is closed, try to open it.
        if (lineSource == null) {
            try {
                lineSource = new BufferedReader(getReader());
            } catch (Exception e) {
                // Ignore
            }
            number = 0;
        }

        // Read until the appropriate line number.
        if (lineSource != null) {
            while (number < lineNumber) {
                try {
                    line = lineSource.readLine();
                    number++;
                }
                catch (IOException e) {
                    cleanup();
                }
            }

            if (janitor == null) {
                final String result = line;   // otherwise cleanup() will wipe out value
                cleanup();
                return result;
            } else {
                janitor.register(this);
            }
        }

        return line;
    }

    /**
     * Cleans up any cached resources used by getLine().
     */
    @Override
    public void cleanup() {
        if (lineSource != null) {
            try {
                lineSource.close();
            } catch (Exception e) {
                // Ignore
            }
        }

        lineSource = null;
        line = null;
        number = 0;
    }

}
