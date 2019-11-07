/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.MissingMethodException;
import groovy.test.GroovyTestCase;
import groovy.util.HeadlessTestSupport;
import groovy.util.Node;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the property access of the Invoker class
 */
public class PropertyTest extends GroovyTestCase {

    public void testMapProperties() throws Exception {
        Map map = new HashMap();
        map.put("foo", "abc");
        map.put("bar", Integer.valueOf(123));

        assertGetSetProperty(map, "foo", "abc", "def");
        assertGetSetProperty(map, "bar", Integer.valueOf(123), new Double(12.34));
    }

    public void testBeanProperties() throws Exception {
        DummyBean bean = new DummyBean();

        assertGetSetProperty(bean, "name", "James", "Bob");
        assertGetSetProperty(bean, "i", Integer.valueOf(123), Integer.valueOf(455));

        // dynamic properties
        assertGetSetProperty(bean, "dynamicFoo", null, "aValue");
        assertGetSetProperty(bean, "dynamicFoo", "aValue", "NewValue");
    }

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

    public void testListCoercionProperty() throws Exception {
        DummyBean bean = new DummyBean();
        List list = new ArrayList();
        list.add(Integer.valueOf(10));
        list.add(Integer.valueOf(20));

        InvokerHelper.setProperty(bean, "point", list);
        assertEquals("Should have set a point", new Point(10, 20), bean.getPoint());
    }

    public void testListCoercionPropertyOnJFrame() throws Exception {
        if (HeadlessTestSupport.isHeadless()) return;

        try {
            JFrame bean = new JFrame();
            List list = new ArrayList();
            list.add(Integer.valueOf(10));
            list.add(Integer.valueOf(20));

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
        String[] java = {System.getProperty("java.home") + "/bin/java", "-version"};
        Process process = ProcessGroovyMethods.execute(java);
        Object value = InvokerHelper.getProperty(process, "in");
        assertNotNull(value);
        process.destroy();
    }

    public Object getCheese() {
        return "cheddar";
    }

    public void testComponentParent() {
        if (HeadlessTestSupport.isHeadless()) return;

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
