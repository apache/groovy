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

package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.util.GroovyTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests method invocation
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokeGroovyMethodTest extends GroovyTestCase {

    private StringBuffer buffer;

    // Method invocation tests
    //-------------------------------------------------------------------------

    public void testInvokeMethodNoParams() throws Throwable {
        buffer = new StringBuffer();

        List list = new ArrayList();
        list.add("abc");
        list.add("def");

        InvokerHelper.invokeMethod(list, "each", new Closure(this) {
            protected Object doCall(Object arguments) {
                buffer.append(arguments.toString());
                return null;
            }
        });

        assertEquals("buffer", "abcdef", buffer.toString());
    }

    public void testMatchesWithObject() throws Throwable {
        assertMatches(new Integer(1), new Integer(1), true);
        assertMatches(new Integer(1), new Integer(2), false);
    }

    public void testMatchesWithClass() throws Throwable {
        assertMatches(new Integer(1), Integer.class, true);
        assertMatches(new Integer(1), Number.class, true);
        assertMatches(new Integer(1), Double.class, false);
    }

    public void testMatchesWithList() throws Throwable {
        assertMatches(new Integer(1), Arrays.asList(new Object[]{new Integer(2), new Integer(1)}), true);
        assertMatches(new Integer(1), Arrays.asList(new Object[]{new Integer(2), new Integer(3)}), false);
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void assertMatches(Object switchValue, Object caseValue, boolean expected) {
        assertEquals(
                "Switch on: " + switchValue + " Case: " + caseValue,
                expected,
                ((Boolean) (InvokerHelper.invokeMethod(caseValue, "isCase", switchValue))).booleanValue());
    }

}
