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

import groovy.lang.GroovyRuntimeException;
import groovy.util.GroovyTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Test the Invoker class
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokerTest extends GroovyTestCase {

    protected Invoker invoker = new Invoker();

    public void testAsCollectionWithArray() {
        Object[] array = { "A", "B", "C" };
        assertAsCollection(array, 3);
    }

    public void testAsCollectionWithMap() {
        Map map = new HashMap();
        map.put("A", "abc");
        map.put("B", "def");
        map.put("C", "xyz");
        assertAsCollection(map, 3);
    }

    public void testAsCollectionWithList() {
        List list = new ArrayList();
        list.add("A");
        list.add("B");
        list.add("C");
        assertAsCollection(list, 3);
    }

    public void testInvokerException() throws Throwable {
        try {
            throw new GroovyRuntimeException("message", new NullPointerException());
        }
        catch (GroovyRuntimeException e) {
            // worked
            assertEquals("message", e.getMessage());
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    public void testAsBoolean() {
        assertAsBoolean(true, Boolean.TRUE);
        assertAsBoolean(true, "true");
        assertAsBoolean(true, "TRUE");
        assertAsBoolean(false, Boolean.FALSE);
        assertAsBoolean(false, "false");
        assertAsBoolean(false, "FALSE");
        
        try {
            assertAsBoolean(false, new Integer(1234));
            fail("Should fail - Numbers can't be converted to booleans");
        }
        catch (GroovyRuntimeException e) {
            // worked
        }
    }
    
    public void testLessThan() {
        assertTrue(InvokerHelper.compareLessThan(new Integer(1), new Integer(2)));
        assertTrue(InvokerHelper.compareLessThanEqual(new Integer(2), new Integer(2)));
    }
    
    public void testGreaterThan() {
        assertTrue(InvokerHelper.compareGreaterThan(new Integer(3), new Integer(2)));
        assertTrue(InvokerHelper.compareGreaterThanEqual(new Integer(2), new Integer(2)));
    }
    
    /** @todo when GROOVY-155 is really fixed
    public void testCompareTo() {
        assertTrue(InvokerHelper.compareEqual("x", new Integer((int) 'x')));
    }
    */
    
    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Asserts the asBoolean method returns the given flag
     */
    protected void assertAsBoolean(boolean expected, Object value) {
        boolean answer = InvokerHelper.asBool(value);
        assertEquals("value: " + value + " asBoolean()", expected, answer);
    }

    /**
     * Asserts that the given object can be converted into a collection and iterator
     * of the given size
     */
    protected void assertAsCollection(Object collectionObject, int count) {
        Collection collection = invoker.asCollection(collectionObject);
        assertTrue("Collection is not null", collection != null);
        assertEquals("Collection size", count, collection.size());

        assertIterator("collections iterator", collection.iterator(), count);
        assertIterator("invoker.asIterator", invoker.asIterator(collectionObject), count);
        assertIterator("invoker.asIterator(invoker.asCollection)", invoker.asIterator(collection), count);
        assertIterator("invoker.asIterator(invoker.asIterator)", invoker.asIterator(invoker.asIterator(collectionObject)), count);
    }

    /**
     * Asserts that the iterator is valid and of the right size
     */
    protected void assertIterator(String message, Iterator iterator, int count) {
        for (int i = 0; i < count; i++) {
            assertTrue(message + ": should have item: " + i, iterator.hasNext());
            assertTrue(message + ": item: " + i + " should not be null", iterator.next() != null);
        }

        assertFalse(
            message + ": should not have item after iterating through: " + count + " items",
            iterator.hasNext());
    }

   
}
