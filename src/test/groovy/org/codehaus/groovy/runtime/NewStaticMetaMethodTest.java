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
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import java.lang.reflect.Method;

public class NewStaticMetaMethodTest extends TestCase {

    public void testInvokeMetaMethod() throws Exception {
        Method method = getClass().getMethod("dummyMethod", new Class[]{String.class, String.class});
        assertTrue("Should have found a method", method != null);

        NewInstanceMetaMethod metaMethod = createNewMetaMethod(method);

        Object answer = metaMethod.invoke("abc", new Object[]{"xyz"});
        assertEquals("def", answer);

        assertTrue("Should not appear as static method", !metaMethod.isStatic());
    }

    public void testInvokeDefaultGroovyMethod() throws Exception {
        Method method = StringGroovyMethods.class.getMethod("plus", new Class[]{CharSequence.class, Object.class});
        assertTrue("Should have found a method", method != null);

        NewInstanceMetaMethod metaMethod = createNewMetaMethod(method);

        Object answer = metaMethod.invoke("abc", new Object[]{"123"});
        assertEquals("abc123", answer);
    }

    public void testInvokeDefaultGroovyMethodUsingMetaClass() {
        Object answer = InvokerHelper.invokeMethod("abc", "plus", new Object[]{"123"});
        assertEquals("abc123", answer);
    }

    public static String dummyMethod(String foo, String bar) throws Exception {
        assertEquals("abc", foo);
        assertEquals("xyz", bar);
        return "def";
    }

    protected NewInstanceMetaMethod createNewMetaMethod(Method method) {
        return new NewInstanceMetaMethod(CachedMethod.find(method));
    }
}
