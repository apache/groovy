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

import java.util.List;

import junit.framework.TestCase;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class RangeTest extends TestCase {

    public void testSize() {
        Range r = createRange(0, 10);
        assertEquals("Size of " + r, 10, r.size());
        r = createRange(0, 1);
        assertEquals("Size of " + r, 1, r.size());
        r = createRange(0, 0);
        assertEquals("Size of " + r, 0, r.size());
    }

    public void testProperties() {
        Range r = createRange(0, 10);
        assertEquals("from", 0, r.getFrom());
        assertEquals("to", 10, r.getTo());
    }

    public void testGet() {
        Range r = createRange(10, 20);
        for (int i = 0; i < 10; i++) {
            Integer value = (Integer) r.get(i);
            assertEquals("Item at index: " + i, i + 10, value.intValue());
        }
    }

    public void testGetOutOfRange() {
        Range r = createRange(10, 20);

        try {
            r.get(-1);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }
        try {
            r.get(10);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }

    }

    public void testContains() {
        Range r = createRange(10, 20);

        assertTrue("contains 11", r.contains(new Integer(11)));
        assertTrue("contains 10", r.contains(new Integer(10)));
        assertTrue("contains 19", r.contains(new Integer(19)));
        assertFalse("contains 9", r.contains(new Integer(9)));
        assertFalse("contains 20", r.contains(new Integer(20)));
        assertFalse("contains 100", r.contains(new Integer(100)));
        assertFalse("contains -1", r.contains(new Integer(-1)));
    }

    public void testSubList() {
        Range r = createRange(10, 20);

        List s = r.subList(2, 4);

        assertTrue("is a Range", r instanceof Range);

        Range sr = (Range) s;

        assertEquals("from", 12, sr.getFrom());
        assertEquals("to", 14, sr.getTo());
        assertEquals("size", 2, sr.size());
    }

    public void testHashCodeAndEquals() {
        Range a = createRange(1, 11);
        Range b = createRange(1, 11);
        Range c = createRange(2, 11);

        assertEquals("hashcode", a.hashCode(), b.hashCode());
        assertTrue("hashcode", a.hashCode() != c.hashCode());

        assertEquals("a and b", a, b);
        assertFalse("a != c", a.equals(c));
    }

    public void testIterator() {
    }

    protected Range createRange(int from, int to) {
        return new Range(new Integer(from), new Integer(to));
    }
    
    protected void assertEquals(String msg, int expected, Object value) {
        assertEquals(msg, new Integer(expected), value);
    }


}
