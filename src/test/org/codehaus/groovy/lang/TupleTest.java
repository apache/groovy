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
public class TupleTest extends TestCase {

    Object[] data = { "a", "b", "c" };
    Tuple t = new Tuple(data);

    public void testSize() {
        assertEquals("Size of " + t, 3, t.size());

        assertEquals("get(0)", "a", t.get(0));
        assertEquals("get(1)", "b", t.get(1));
    }

    public void testGetOutOfTuple() {
        try {
            t.get(-1);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }
        try {
            t.get(10);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }

    }

    public void testContains() {
        assertTrue("contains a", t.contains("a"));
        assertTrue("contains b", t.contains("b"));
    }

    public void testSubList() {
        List s = t.subList(1, 2);

        assertTrue("is a Tuple", s instanceof Tuple);

        assertEquals("size", 1, s.size());
    }

    public void testHashCodeAndEquals() {
        Tuple a = new Tuple(new Object[] { "a", "b", "c" });
        Tuple b = new Tuple(new Object[] { "a", "b", "c" });
        Tuple c = new Tuple(new Object[] { "d", "b", "c" });

        assertEquals("hashcode", a.hashCode(), b.hashCode());
        assertTrue("hashcode", a.hashCode() != c.hashCode());

        assertEquals("a and b", a, b);
        assertFalse("a != c", a.equals(c));
    }

    public void testIterator() {
    }

}
