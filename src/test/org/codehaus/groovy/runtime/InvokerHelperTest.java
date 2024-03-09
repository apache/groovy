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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public final class InvokerHelperTest {

    private final Map<String, Object> variables = new HashMap<>();

    @Test
    public void testCreateScriptWithScriptClass() throws Exception {
        try (GroovyClassLoader classLoader = new GroovyClassLoader()) {
            String controlProperty = "text", controlValue = "I am a script";
            Class<?> scriptClass = classLoader.parseClass(new GroovyCodeSource(
                    controlProperty + " = '" + controlValue + "'", "testscript", "/groovy/shell"), false);

            Script script = InvokerHelper.createScript(scriptClass, new Binding(variables));

            assertSame(variables, script.getBinding().getVariables());

            script.run();

            assertEquals(controlValue, script.getProperty(controlProperty));
        }
    }

    @Test
    public void testCreateScriptWithNullClass() {
        Script script = InvokerHelper.createScript(null, new Binding(variables));

        assertSame(variables, script.getBinding().getVariables());
    }

    @Test
    public void testInitialCapacity() {
        assertEquals(16, InvokerHelper.initialCapacity(0));
        assertEquals( 2, InvokerHelper.initialCapacity(1));
        assertEquals( 4, InvokerHelper.initialCapacity(2));
        assertEquals( 4, InvokerHelper.initialCapacity(3));
        assertEquals( 8, InvokerHelper.initialCapacity(4));
        assertEquals( 8, InvokerHelper.initialCapacity(5));
        assertEquals( 8, InvokerHelper.initialCapacity(6));
        assertEquals( 8, InvokerHelper.initialCapacity(7));
        assertEquals(16, InvokerHelper.initialCapacity(8));
        assertEquals(16, InvokerHelper.initialCapacity(9));
        assertEquals(16, InvokerHelper.initialCapacity(10));
        assertEquals(16, InvokerHelper.initialCapacity(11));
        assertEquals(16, InvokerHelper.initialCapacity(12));
        assertEquals(16, InvokerHelper.initialCapacity(13));
        assertEquals(16, InvokerHelper.initialCapacity(14));
        assertEquals(16, InvokerHelper.initialCapacity(15));
        assertEquals(32, InvokerHelper.initialCapacity(16));
        assertEquals(32, InvokerHelper.initialCapacity(17));
    }

    @Test
    public void testToTypeString() {
        Object[] objects = null;
        assertEquals("null", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[0];
        assertEquals("", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[] {null};
        assertEquals("null", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[] {0};
        assertEquals("Integer", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[] {0, 1d};
        assertEquals("Integer, Double", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[] {new DummyBean(), new DummyBean()}; // GROOVY-11270
        assertEquals("o.c.g.r.DummyBean, o.c.g.r.DummyBean", InvokerHelper.toTypeString(objects, 42));

        objects = new Object[] {0f, new DummyBean()};
        assertEquals("Float, org.codehaus.groovy.runtime.DummyBean", InvokerHelper.toTypeString(objects, 44));
    }
}
