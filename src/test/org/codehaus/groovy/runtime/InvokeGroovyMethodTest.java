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

import groovy.lang.Closure;
import groovy.test.GroovyTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests method invocation
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
