/*
 * Created on Sep 7, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.codehaus.groovy.lang;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class RangeTest extends TestCase {

    public void testSize() {
        Range r = new Range(0, 10);
        assertEquals("Size of " + r, 10, r.size());
        r = new Range(0, 1);
        assertEquals("Size of " + r, 1, r.size());
        r = new Range(0, 0);
        assertEquals("Size of " + r, 0, r.size());
    }

    public void testProperties() {
        Range r = new Range(0, 10);
        assertEquals("from", 0, r.getFrom());
        assertEquals("to", 10, r.getTo());
    }

    public void testGet() {
        Range r = new Range(10, 20);
        for (int i = 0; i < 10; i++) {
            Integer value = (Integer) r.get(i);
            assertEquals("Item at index: " + i, i + 10, value.intValue());
        }
    }

    public void testGetOutOfRange() {
        Range r = new Range(10, 20);

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
        Range r = new Range(10, 20);

        assertTrue("contains 11", r.contains(new Integer(11)));
        assertTrue("contains 10", r.contains(new Integer(10)));
        assertTrue("contains 19", r.contains(new Integer(19)));
        assertFalse("contains 9", r.contains(new Integer(9)));
        assertFalse("contains 20", r.contains(new Integer(20)));
        assertFalse("contains 100", r.contains(new Integer(100)));
        assertFalse("contains -1", r.contains(new Integer(-1)));
    }

    public void testSubList() {
        Range r = new Range(10, 20);

        List s = r.subList(2, 4);

        assertTrue("is a Range", r instanceof Range);

        Range sr = (Range) s;

        assertEquals("from", 12, sr.getFrom());
        assertEquals("to", 14, sr.getTo());
        assertEquals("size", 2, sr.size());
    }

    public void testHashCodeAndEquals() {
        Range a = new Range(1, 11);
        Range b = new Range(1, 11);
        Range c = new Range(2, 11);

        assertEquals("hashcode", a.hashCode(), b.hashCode());
        assertTrue("hashcode", a.hashCode() != c.hashCode());

        assertEquals("a and b", a, b);
        assertFalse("a != c", a.equals(c));
    }

    public void testIterator() {
    }

}
