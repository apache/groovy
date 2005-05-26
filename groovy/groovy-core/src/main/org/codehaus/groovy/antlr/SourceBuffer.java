/**
 *
 * Copyright 2005 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.antlr;

import java.util.List;
import java.util.ArrayList;

/**
 * A simple buffer that provides line/col access to chunks of source code
 * held within itself.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */
public class SourceBuffer {
    List lines;
    StringBuffer current;

    public SourceBuffer() {
        lines = new ArrayList();
        current = new StringBuffer();
        lines.add(current);
    }

    /**
     * Obtains a snippet of the source code within the bounds specified
     * @param startLine
     * @param startColumn
     * @param endLine
     * @param endColumn
     * @return specified snippet of source code as a String, or null if no source available
     */
    public String getSnippet(int startLine, int startColumn, int endLine, int endColumn) {
        if (startLine == endLine && startColumn == endColumn) { return null; } // no text to return
        if (lines.size() == 1 && current.length() == 0) { return null; } // buffer hasn't been filled yet

        // lets not allow out of bounds requests
        if (startLine < 1) { startLine = 1;}
        if (endLine < 1) { endLine = 1;}
        if (startLine > lines.size()) { startLine = lines.size() + 1; }
        if (endLine > lines.size()) { endLine = lines.size() + 1; }

        // obtain the snippet from the buffer within specified bounds
        StringBuffer snippet = new StringBuffer();
        for (int i = startLine - 1; i < endLine;i++) {
            String line = ((StringBuffer)lines.get(i)).toString();
            if (startLine == endLine) {
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
        current.append((char)c);

        if (c == '\n') {
            current = new StringBuffer();
            lines.add(current);
        }
    }
}
