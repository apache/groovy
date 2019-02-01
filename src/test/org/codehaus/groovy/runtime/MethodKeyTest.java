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

import junit.framework.TestCase;
import org.codehaus.groovy.runtime.metaclass.TemporaryMethodKey;

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