/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.runtime;

import java.io.IOException;
import java.io.Writer;

/**
 * This class codes around a silly limiation of StringWriter which doesn't allow a StringBuffer
 * to be passed in as a constructor for some bizzare reason.
 * So we replicate the behaviour of StringWriter here but allow a StringBuffer to be passed in.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class StringBufferWriter extends Writer {

    private StringBuffer buffer;

    /**
     * Create a new string writer which will append the text to the given StringBuffer
     */
    public StringBufferWriter(StringBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Write a single character.
     */
    public void write(int c) {
        buffer.append((char) c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param text Array of characters
     * @param offset Offset from which to start writing characters
     * @param length Number of characters to write
     */
    public void write(char text[], int offset, int length) {
        if ((offset < 0) || (offset > text.length) || (length < 0) || ((offset + length) > text.length) || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        else if (length == 0) {
            return;
        }
        buffer.append(text, offset, length);
    }

    /**
     * Write a string.
     */
    public void write(String text) {
        buffer.append(text);
    }

    /**
     * Write a portion of a string.
     *
     * @param text the text to be written
     * @param offset offset from which to start writing characters
     * @param length Number of characters to write
     */
    public void write(String text, int offset, int length) {
        buffer.append(text.substring(offset, offset + length));
    }

    /**
     * Return the buffer's current value as a string.
     */
    public String toString() {
        return buffer.toString();
    }

    /**
     * Flush the stream.
     */
    public void flush() {
    }

    /**
     * Closing a <tt>StringWriter</tt> has no effect. The methods in this
     * class can be called after the stream has been closed without generating
     * an <tt>IOException</tt>.
     */
    public void close() throws IOException {
    }
}
