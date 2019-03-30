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
import junit.framework.TestCase;

import java.util.HashMap;

import static org.codehaus.groovy.runtime.InvokerHelper.initialCapacity;


public class InvokerHelperTest extends TestCase {
    private HashMap bindingVariables;

    protected void setUp() throws Exception {
        bindingVariables = new HashMap();
        bindingVariables.put("name", "hans");
    }

    public void testCreateScriptWithNullClass() {
        Script script = InvokerHelper.createScript(null, new Binding(bindingVariables));
        assertEquals(bindingVariables, script.getBinding().getVariables());
    }

    public void testCreateScriptWithScriptClass() {
        GroovyClassLoader classLoader = new GroovyClassLoader();
        String controlProperty = "text";
        String controlValue = "I am a script";
        String code = controlProperty + " = '" + controlValue + "'";
        GroovyCodeSource codeSource = new GroovyCodeSource(code, "testscript", "/groovy/shell");
        Class scriptClass = classLoader.parseClass(codeSource, false);
        Script script = InvokerHelper.createScript(scriptClass, new Binding(bindingVariables));
        assertEquals(bindingVariables, script.getBinding().getVariables());
        script.run();
        assertEquals(controlValue, script.getProperty(controlProperty));
    }

    public void testInitialCapacity() {
        assertEquals(1, initialCapacity(1));
        assertEquals(2, initialCapacity(2));
        assertEquals(4, initialCapacity(3));
        assertEquals(4, initialCapacity(4));
        assertEquals(8, initialCapacity(5));
        assertEquals(8, initialCapacity(6));
        assertEquals(8, initialCapacity(7));
        assertEquals(8, initialCapacity(8));
        assertEquals(16, initialCapacity(9));
        assertEquals(16, initialCapacity(10));
        assertEquals(16, initialCapacity(11));
        assertEquals(16, initialCapacity(12));
        assertEquals(16, initialCapacity(13));
        assertEquals(16, initialCapacity(14));
        assertEquals(16, initialCapacity(15));
        assertEquals(16, initialCapacity(16));
        assertEquals(32, initialCapacity(17));
    }
}
