/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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
package groovy.lang;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.groovy.runtime.InvokerHelper;

import junit.framework.TestCase;

/**
 * A default JUnit TestCase in Groovy. This provides a number of helper methods
 * plus avoids the JUnit restriction of requiring all test* methods to be void
 * return type.
 * 
 * @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyTestCase extends TestCase {

    public GroovyTestCase() {
    }

    protected void assertLength(int length, char[] array) {
        assertEquals(length, array.length);
    }

    protected void assertLength(int length, int[] array) {
        assertEquals(length, array.length);
    }

    protected void assertLength(int length, Object[] array) {
        assertEquals(length, array.length);
    }

    protected void assertContains(char expected, char[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append(expected + " not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'" + array[i] + "'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }

    protected void assertContains(int expected, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append(expected + " not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'" + array[i] + "'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }
    
    /**
     * Asserts that the console output of the given object matches the
     * given text string
     * 
     * @param value the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertConsoleOutput(Object value, String expected) {
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);
        InvokerHelper.invokeMethod(value, "print", out);
        assertEquals("print() to a console output of: " + value, expected, buffer.toString());
        
        
        Object console = InvokerHelper.invokeMethod(value, "toConsoleOutput", null);
        assertEquals("toConsoleOutput() on value: " + value, expected, console);
    }
}
