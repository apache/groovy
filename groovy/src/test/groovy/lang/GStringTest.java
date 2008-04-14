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
package groovy.lang;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Tests the use of the structured Attribute type
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GStringTest extends GroovyTestCase {

    public void testIterateOverText() {
        DummyGString compString = new DummyGString(new Object[]{"James"});
        assertArrayEquals(new String[]{"Hello ", "!"}, compString.getStrings());
        assertArrayEquals(new Object[]{"James"}, compString.getValues());
        assertEquals("Hello James!", compString.toString());
    }

    public void testAppendString() {
        DummyGString a = new DummyGString(new Object[]{"James"});
        GString result = a.plus(" how are you?");
        assertEquals("Hello James! how are you?", result.toString());
        assertEquals('J', a.charAt(6));
        assertEquals("o J", a.subSequence(4, 7));
    }

    public void testAppendString2() {
        DummyGString a = new DummyGString(new Object[]{"James"}, new String[]{"Hello "});
        GString result = a.plus(" how are you?");
        System.out.println("Strings: " + InvokerHelper.toString(result.getStrings()));
        System.out.println("Values: " + InvokerHelper.toString(result.getValues()));
        assertEquals("Hello James how are you?", result.toString());
    }

    public void testAppendGString() {
        DummyGString a = new DummyGString(new Object[]{"James"});
        DummyGString b = new DummyGString(new Object[]{"Bob"});
        GString result = a.plus(b);
        assertEquals("Hello James!Hello Bob!", result.toString());
    }

    public void testAppendGString2() {
        DummyGString a = new DummyGString(new Object[]{"James"}, new String[]{"Hello "});
        DummyGString b = new DummyGString(new Object[]{"Bob"}, new String[]{"Hello "});
        GString result = a.plus(b);
        assertEquals("Hello JamesHello Bob", result.toString());
    }

    public void testEqualsAndHashCode() {
        DummyGString a = new DummyGString(new Object[]{new Integer(1)});
        DummyGString b = new DummyGString(new Object[]{new Long(1)});
        Comparable c = new DummyGString(new Object[]{new Double(2.3)});

        assertTrue("a == b", a.equals(b));
        assertEquals("hashcode a == b", a.hashCode(), b.hashCode());
        assertFalse("a != c", a.equals(c));
        assertTrue("hashcode a != c", a.hashCode() != c.hashCode());
        assertEquals("a <=> b", 0, a.compareTo(b));
        assertEquals("a <=> b", -1, a.compareTo(c));
    }
}
