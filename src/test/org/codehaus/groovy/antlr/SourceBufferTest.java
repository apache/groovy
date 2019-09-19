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

import groovy.test.GroovyTestCase;

import java.io.Reader;
import java.io.StringReader;

public class SourceBufferTest extends GroovyTestCase {

    public void testEmptyBuffer() throws Exception {
        SourceBuffer buffer = getSourceBuffer("");
        assertNull(buffer.getSnippet(new LineColumn(1, 1), new LineColumn(1, 1)));
    }

    public void testSimpleUsage() throws Exception {
        SourceBuffer buffer = getSourceBuffer("println 'hello world'");
        assertEquals("hello", buffer.getSnippet(new LineColumn(1, 10), new LineColumn(1, 15)));
    }

    public void testUnixLineUsage() throws Exception {
        String endOfLine = "\n";
        StringBuffer src = new StringBuffer();
        src.append("println 'hello world'").append(endOfLine);
        src.append("println 'oh not, not that again'").append(endOfLine);
        SourceBuffer buffer = getSourceBuffer(src.toString());
        assertEquals("hello", buffer.getSnippet(new LineColumn(1, 10), new LineColumn(1, 15)));
        assertEquals("world'" + endOfLine + "print", buffer.getSnippet(new LineColumn(1, 16), new LineColumn(2, 6)));
        assertEquals(endOfLine, buffer.getSnippet(new LineColumn(1, 22), new LineColumn(1, 23)));
        assertEquals(endOfLine, buffer.getSnippet(new LineColumn(2, 33), new LineColumn(2, 34)));
    }

    public void testDOSLineUsage() throws Exception {
        String endOfLine = "\r\n";
        StringBuffer src = new StringBuffer();
        src.append("println 'hello world'").append(endOfLine);
        src.append("println 'oh not, not that again'").append(endOfLine);
        SourceBuffer buffer = getSourceBuffer(src.toString());
        assertEquals("hello", buffer.getSnippet(new LineColumn(1, 10), new LineColumn(1, 15)));
        assertEquals("oh not", buffer.getSnippet(new LineColumn(2, 10), new LineColumn(2, 16)));
        assertEquals("world'" + endOfLine + "print", buffer.getSnippet(new LineColumn(1, 16), new LineColumn(2, 6)));
        assertEquals(endOfLine, buffer.getSnippet(new LineColumn(1, 22), new LineColumn(1, 24)));
        assertEquals(endOfLine, buffer.getSnippet(new LineColumn(2, 33), new LineColumn(2, 35)));
    }

    public void testOutOfBounds() throws Exception {
        String endOfLine = "\n";
        StringBuffer src = new StringBuffer();
        src.append("println 'hello world'").append(endOfLine);
        src.append("println 'oh not, not that again'").append(endOfLine);
        SourceBuffer buffer = getSourceBuffer(src.toString());
        assertEquals("println", buffer.getSnippet(new LineColumn(0, 0), new LineColumn(1, 8)));
        assertEquals("println", buffer.getSnippet(new LineColumn(-10, -1), new LineColumn(1, 8)));
        assertEquals(endOfLine, buffer.getSnippet(new LineColumn(2, 33), new LineColumn(2, 40)));
        assertEquals("", buffer.getSnippet(new LineColumn(3, 33), new LineColumn(6, 40)));
    }

    private SourceBuffer getSourceBuffer(String text) throws Exception {
        SourceBuffer buffer = new SourceBuffer();
        Reader reader = new UnicodeEscapingReader(new StringReader(text), buffer);

        while (reader.read() != -1) {
            // empty loop
            // - read all characters till the end of the reader
            // UnicodeEscapingReader has side effects of
            // filling the buffer
        }
        return buffer;
    }
}
