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

import groovy.lang.GString;
import groovy.test.GroovyTestCase;

/**
 * Tests method invocation
 */
public class InvokeConstructorTest extends GroovyTestCase {

    public void testInvokeConstructorNoParams() throws Throwable {
        assertConstructor(new DummyBean(), new Object[0]);
    }

    public void testInvokeConstructorOneParam() throws Throwable {
        assertConstructor(new DummyBean("Bob"), "Bob");
    }

    public void testInvokeConstructorOneParamWhichIsNull() throws Throwable {
        assertConstructor(new DummyBean("Bob", new Integer(1707)), new Object[]{"Bob", new Integer(1707)});
    }

    public void testConstructorWithGStringCoercion() throws Throwable {
        GString gstring = new GString(new Object[]{new Integer(123)}) {
            public String[] getStrings() {
                return new String[]{""};
            }
        };

        Object expected = new Long(gstring.toString());

        assertConstructor(expected, new Object[]{gstring});
    }

    protected void assertConstructor(Object expected, Object arguments) throws Throwable {
        Object value = invoke(expected.getClass(), arguments);

        assertEquals("Invoking overloaded method for arguments: " + InvokerHelper.toString(arguments), expected, value);
    }

    protected Object invoke(Class type, Object args) throws Throwable {
        try {
            return InvokerHelper.invokeConstructorOf(type, args);
        }
        catch (InvokerInvocationException e) {
            throw e.getCause();
        }
    }
}
