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
package org.codehaus.groovy.tools.shell

import groovy.transform.CompileStatic
import jline.console.ConsoleReader
import jline.internal.Log
import org.codehaus.groovy.tools.shell.util.JAnsiHelper
import org.fusesource.jansi.AnsiOutputStream

@CompileStatic
class PatchedConsoleReader extends ConsoleReader {


    public PatchedConsoleReader(final InputStream inStream, final OutputStream out) throws IOException {
        super(inStream, out);
    }

    /**
     * copied from jline2.0 and modified to invoke stripAnsi() for length calculations
     * Output the specified {@link Collection} in proper columns.
     * See https://github.com/jline/jline2/issues/132
     */
    public void printColumns(final Collection<? extends CharSequence> items) throws IOException {
        if (items == null || items.isEmpty()) {
            return;
        }

        int width = getTerminal().getWidth();
        int height = getTerminal().getHeight();

        int maxWidth = 0;
        for (CharSequence item : items) {
            maxWidth = Math.max(maxWidth, JAnsiHelper.stripAnsi(item).length());
        }
        maxWidth = maxWidth + 3;
        Log.debug("Max width: ", maxWidth);

        int showLines;
        if (isPaginationEnabled()) {
            showLines = height - 1; // page limit
        }
        else {
            showLines = Integer.MAX_VALUE;
        }

        StringBuilder buff = new StringBuilder();
        int realLength = 0;
        for (CharSequence item : items) {
            if ((realLength + maxWidth) > width) {
                println(buff);
                buff.setLength(0);
                realLength = 0;

                if (--showLines == 0) {
                    // Overflow
                    print(resources.getString("DISPLAY_MORE"));
                    flush();
                    int c = readCharacter();
                    if (c == '\r' || c == '\n') {
                        // one step forward
                        showLines = 1;
                    }
                    else if (c != 'q') {
                        // page forward
                        showLines = height - 1;
                    }

                    back(resources.getString("DISPLAY_MORE").length());
                    if (c == 'q') {
                        // cancel
                        break;
                    }
                }
            }

            // NOTE: toString() is important here due to AnsiString being retarded
            buff.append(item.toString());
            int strippedItemLength = JAnsiHelper.stripAnsi(item).length()
            realLength += strippedItemLength
            for (int i = 0; i < (maxWidth - strippedItemLength); i++) {
                buff.append(' ');
            }
            realLength += maxWidth - strippedItemLength;
        }

        if (buff.length() > 0) {
            println(buff);
        }
    }

}
