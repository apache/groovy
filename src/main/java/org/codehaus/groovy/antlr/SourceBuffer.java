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
package org.codehaus.groovy.antlr;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple buffer that provides line/col access to chunks of source code
 * held within itself.
 */
public class SourceBuffer {
    private final List<StringBuilder> lines;
    private StringBuilder current;

    public SourceBuffer() {
        lines = new ArrayList<StringBuilder>();
        //lines.add(new StringBuilder()); // dummy row for position [0] in the List

        current = new StringBuilder();
        lines.add(current);
    }

    /**
     * Obtains a snippet of the source code within the bounds specified
     * @param start (inclusive line/ inclusive column)
     * @param end (inclusive line / exclusive column)
     * @return specified snippet of source code as a String, or null if no source available
     */
    public String getSnippet(LineColumn start, LineColumn end) {
        // preconditions
        if (start == null || end == null) { return null; } // no text to return
        if (start.equals(end)) { return null; } // no text to return
        if (lines.size() == 1 && current.length() == 0) { return null; } // buffer hasn't been filled yet

        // working variables
        int startLine = start.getLine();
        int startColumn = start.getColumn();
        int endLine = end.getLine();
        int endColumn = end.getColumn();

        // reset any out of bounds requests
        if (startLine < 1) { startLine = 1;}
        if (endLine < 1) { endLine = 1;}
        if (startColumn < 1) { startColumn = 1;}
        if (endColumn < 1) { endColumn = 1;}
        if (startLine > lines.size()) { startLine = lines.size(); }
        if (endLine > lines.size()) { endLine = lines.size(); }

        // obtain the snippet from the buffer within specified bounds
        StringBuilder snippet = new StringBuilder();
        for (int i = startLine - 1; i < endLine;i++) {
            String line = ((StringBuilder)lines.get(i)).toString();
            if (startLine == endLine) {
                // reset any out of bounds requests (again)
                if (startColumn > line.length()) { startColumn = line.length();}
                if (startColumn < 1) { startColumn = 1;}
                if (endColumn > line.length()) { endColumn = line.length() + 1;}
                if (endColumn < 1) { endColumn = 1;}
                if (endColumn < startColumn) { endColumn = startColumn;}

                line = line.substring(startColumn - 1, endColumn - 1);
            } else {
                if (i == startLine - 1) {
                    if (startColumn - 1 < line.length()) {
                        line = line.substring(startColumn - 1);
                    }
                }
                if (i == endLine - 1) {
                    if (endColumn - 1 < line.length()) {
                        line = line.substring(0,endColumn - 1);
                    }
                }
            }
            snippet.append(line);
        }
        return snippet.toString();
    }

    /**
     * Writes the specified character into the buffer
     * @param c
     */
    public void write(int c) {
        if (c != -1) {
            current.append((char)c);
        }
        if (c == '\n') {
            current = new StringBuilder();
            lines.add(current);
        }
    }
}
