/*
 $Id$

 Copyright 2005 (C) Jeremy Rayner. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.antlr;

import groovy.util.GroovyTestCase;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */
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
