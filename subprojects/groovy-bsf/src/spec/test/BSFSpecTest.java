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
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.junit.Test;

import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;

public class BSFSpecTest {
    @Test
    public void testSimpleIntegration() throws BSFException {
        // tag::bsf_simple[]
        String myScript = "println('Hello World')\n  return [1, 2, 3]";
        BSFManager manager = new BSFManager();
        List answer = (List) manager.eval("groovy", "myScript.groovy", 0, 0, myScript);
        assertEquals(3, answer.size());
        // end::bsf_simple[]
    }

    @Test
    public void testVariablePassing() throws BSFException {
        // tag::bsf_variable_passing[]
        BSFManager manager = new BSFManager();
        manager.declareBean("xyz", 4, Integer.class);
        Object answer = manager.eval("groovy", "test.groovy", 0, 0, "xyz + 1");
        assertEquals(5, answer);
        // end::bsf_variable_passing[]
    }

    @Test
    public void testApply() throws BSFException {
        // tag::bsf_apply[]
        BSFManager manager = new BSFManager();
        Vector<String> ignoreParamNames = null;
        Vector<Integer> args = new Vector<>();
        args.add(2);
        args.add(5);
        args.add(1);
        Integer actual = (Integer) manager.apply("groovy", "applyTest", 0, 0,
                "def summer = { a, b, c -> a * 100 + b * 10 + c }", ignoreParamNames, args);
        assertEquals(251, actual.intValue());
        // end::bsf_apply[]
    }

    @Test
    public void testAccess() throws BSFException {
        // tag::bsf_access[]
        BSFManager manager = new BSFManager();
        BSFEngine bsfEngine = manager.loadScriptingEngine("groovy");
        manager.declareBean("myvar", "hello", String.class);
        Object myvar = manager.lookupBean("myvar");
        String result = (String) bsfEngine.call(myvar, "reverse", new Object[0]);
        assertEquals("olleh", result);
        // end::bsf_access[]
    }


}
