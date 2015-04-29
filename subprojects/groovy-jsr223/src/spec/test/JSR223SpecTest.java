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
import org.junit.Test;

// tag::jsr223_imports[]
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
// end::jsr223_imports[]
import javax.script.Invocable;

import static org.junit.Assert.assertEquals;


public class JSR223SpecTest {
    @Test
    public void testSimpleExample() throws ScriptException {
        // tag::jsr223_init[]
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        // end::jsr223_init[]

        // tag::jsr223_basic[]
        Integer sum = (Integer) engine.eval("(1..10).sum()");
        assertEquals(new Integer(55), sum);
        // end::jsr223_basic[]

        // tag::jsr223_variables[]
        engine.put("first", "HELLO");
        engine.put("second", "world");
        String result = (String) engine.eval("first.toLowerCase() + ' ' + second.toUpperCase()");
        assertEquals("hello WORLD", result);
        // end::jsr223_variables[]
    }

    @Test
    public void testInvocableFunction() throws ScriptException, NoSuchMethodException {
        // tag::jsr223_invocable[]
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        String fact = "def factorial(n) { n == 1 ? 1 : n * factorial(n - 1) }";
        engine.eval(fact);
        Invocable inv = (Invocable) engine;
        Object[] params = {5};
        Object result = inv.invokeFunction("factorial", params);
        assertEquals(new Integer(120), result);
        // end::jsr223_invocable[]
    }
}
