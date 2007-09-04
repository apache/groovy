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

import junit.framework.TestCase;
import org.codehaus.groovy.runtime.metaclass.TemporaryMethodKey;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodKeyTest extends TestCase {

    public void testDefaultImplementation() throws Exception {
        MethodKey a = new DefaultMethodKey(Object.class, "foo", new Class[]{Object.class, Integer.class}, false);
        MethodKey a2 = new DefaultMethodKey(Object.class, "foo", new Class[]{Object.class, Integer.class}, false);
        MethodKey b = new DefaultMethodKey(Object.class, "foo", new Class[]{Object.class}, false);
        MethodKey c = new DefaultMethodKey(Object.class, "bar", new Class[]{Object.class, Integer.class}, false);

        assertCompare(a, a, true);
        assertCompare(a, a2, true);
        assertCompare(b, b, true);

        assertCompare(a, b, false);
        assertCompare(a, c, false);
        assertCompare(b, c, false);
    }

    public void testTemporaryImplementation() throws Exception {
        MethodKey a = new DefaultMethodKey(Object.class, "foo", new Class[]{Object.class, Integer.class}, false);
        MethodKey a2 = new TemporaryMethodKey(Object.class, "foo", new Object[]{new Object(), new Integer(1)}, false);
        MethodKey b = new TemporaryMethodKey(Object.class, "foo", new Object[]{new Object()}, false);
        MethodKey c = new TemporaryMethodKey(Object.class, "bar", new Object[]{new Object(), new Integer(1)}, false);

        assertCompare(a, a, true);
        assertCompare(a, a2, true);
        assertCompare(b, b, true);

        assertCompare(a, b, false);
        assertCompare(a, c, false);
        assertCompare(b, c, false);
    }

    protected void assertCompare(Object a, Object b, boolean expected) {
        assertEquals("Compare " + a + " to " + b, expected, a.equals(b));
        if (expected) {
            assertEquals("hashCode " + a + " to " + b, a.hashCode(), b.hashCode());
        }
    }
}