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

import groovy.lang.MissingMethodException;
import groovy.util.GroovySwingTestCase;
import groovy.util.Node;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the property access of the Invoker class
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class PropertyTest extends GroovySwingTestCase {

    public void testMapProperties() throws Exception {
        Map map = new HashMap();
        map.put("foo", "abc");
        map.put("bar", new Integer(123));

        assertGetSetProperty(map, "foo", "abc", "def");
        assertGetSetProperty(map, "bar", new Integer(123), new Double(12.34));
    }

    public void testBeanProperties() throws Exception {
        DummyBean bean = new DummyBean();

        assertGetSetProperty(bean, "name", "James", "Bob");
        assertGetSetProperty(bean, "i", new Integer(123), new Integer(455));

        // dynamic properties
        assertGetSetProperty(bean, "dynamicFoo", null, "aValue");
        assertGetSetProperty(bean, "dynamicFoo", "aValue", "NewValue");
    }

    /**
     * todo this is no longer possible in new groovy
     * public void testUsingMethodProperty() throws Exception {
     * DummyBean bean = new DummyBean();
     * <p/>
     * assertGetSetProperty(bean, "name", "James", "Bob");
     * <p/>
     * Object value = InvokerHelper.getProperty(bean, "getName");
     * assertTrue("Should have returned a closure: " + value, value instanceof Closure);
     * Closure closure = (Closure) value;
     * Object result = closure.call(null);
     * assertEquals("Result of call to closure", "Bob", result);
     * }
     */


    public void testStaticProperty() throws Exception {
        Object value = InvokerHelper.getProperty(System.class, "out");
        assertEquals("static property out", System.out, value);
    }

    public void testClassProperty() throws Exception {
        Class c = String.class;
        Object value = InvokerHelper.getProperty(c, "name");
        assertEquals("class name property", c.getName(), value);
    }

    public void testMapEntryProperty() throws Exception {
        HashMap map = new HashMap();
        map.put("a", "x");
        Object[] array = map.entrySet().toArray();
        Object entry = array[0];

        Object key = InvokerHelper.getProperty(entry, "key");
        assertEquals("key property", "a", key);

        Object value = InvokerHelper.getProperty(entry, "value");
        assertEquals("value property", "x", value);
    }

    /**
     * todo this is no longer possible in new groovy
     * public void testMethodProperty() throws Exception {
     * Object value = InvokerHelper.getProperty(this, "getCheese");
     * assertTrue("Should have returned a closure: " + value, value instanceof Closure);
     * <p/>
     * Object result = ((Closure) value).call();
     * assertEquals("result of closure call", getCheese(), result);
     * <p/>
     * System.out.println("Closure: " + value + " and cheese: " + result);
     * }
     */

    public void testListCoercionProperty() throws Exception {
        DummyBean bean = new DummyBean();
        List list = new ArrayList();
        list.add(new Integer(10));
        list.add(new Integer(20));

        InvokerHelper.setProperty(bean, "point", list);
        assertEquals("Should have set a point", new Point(10, 20), bean.getPoint());
    }

    public void testListCoercionPropertyOnJFrame() throws Exception {
        if (isHeadless()) return;

        try {
            JFrame bean = new JFrame();
            List list = new ArrayList();
            list.add(new Integer(10));
            list.add(new Integer(20));

            InvokerHelper.setProperty(bean, "location", list);
            assertEquals("Should have set a point", new Point(10, 20), bean.getLocation());
        }
        catch (MissingMethodException e) {
            System.out.println("Failed with cause: " + e);
            e.printStackTrace();
            fail("Should not have throw: " + e);
        }
    }

    public void testListNavigationProperty() throws Exception {
        List list = new ArrayList();
        list.add(new DummyBean("James"));
        list.add(new DummyBean("Bob"));

        List value = (List) InvokerHelper.getProperty(list, "name");
        assertArrayEquals(new Object[]{"James", "Bob"}, value.toArray());
    }

    public void testListOfListNavigationProperty() throws Exception {
        List list = new ArrayList();
        list.add(new DummyBean("James"));
        list.add(new DummyBean("Bob"));

        List listOfList = new ArrayList();
        listOfList.add(list);

        List value = (List) InvokerHelper.getProperty(listOfList, "name");
        Object[] objects = value.toArray();
        List objectList = (List) objects[0];
        assertArrayEquals(new Object[]{"James", "Bob"}, objectList.toArray());
    }

    public void testNodeNavigationProperty() throws Exception {
        Node z = new Node(null, "z");
        Node y = new Node(null, "y");

        List children = new ArrayList();
        children.add(y);
        children.add(z);

        Node x = new Node(null, "x", children);

        children = new ArrayList();
        children.add(x);
        Node b = new Node(null, "b", children);

        // @todo should try with just a node as the child

        List value = (List) InvokerHelper.getProperty(b, "x");
        assertArrayEquals(new Object[]{x}, value.toArray());

        value = (List) InvokerHelper.getProperty(value, "z");
        assertArrayEquals(new Object[]{z}, value.toArray());
    }

    public void testUsingInPropertyOnProcessViaGroovyMethod() throws Exception {
        Process process = DefaultGroovyMethods.execute(System.getProperty("java.home") + "/bin/java -version");
        Object value = InvokerHelper.getProperty(process, "in");
        assertNotNull(value);

        System.out.println("Found in: " + value);

        process.destroy();
    }

    public Object getCheese() {
        return "cheddar";
    }

    public void testComponentParent() {
        if (isHeadless()) return;

        JPanel panel = new JPanel();
        JButton bean = new JButton();

        panel.add(bean);

        Object value = InvokerHelper.getProperty(bean, "parent");
        assertTrue(value != null);
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void assertGetSetProperty(Object object, String property, Object currentValue, Object newValue) {
        assertGetProperty(object, property, currentValue);

        InvokerHelper.setProperty(object, property, newValue);

        assertGetProperty(object, property, newValue);
    }

    protected void assertGetProperty(Object object, String property, Object expected) {
        Object value = InvokerHelper.getProperty(object, property);

        assertEquals("property: " + property + " of: " + object, expected, value);
    }
}
