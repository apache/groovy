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

import java.util.HashMap;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import junit.framework.TestCase;

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
}
