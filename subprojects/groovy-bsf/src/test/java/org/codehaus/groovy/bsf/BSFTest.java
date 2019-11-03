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
package org.codehaus.groovy.bsf;

import junit.framework.TestCase;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Tests the BSF integration.
 */
public class BSFTest extends TestCase {
    private static final Class ENGINE = GroovyEngine.class;

    protected BSFManager manager;

    protected void setUp() throws Exception {
        BSFManager.registerScriptingEngine("groovy", ENGINE.getName(), new String[]{"groovy", "gy"});
        manager = new BSFManager();
    }

    public void testScriptDefPrefixRemoval() throws Exception {
        manager.exec("groovy","scriptdef_foo", 0, 0, "assert bsf != null && this.class.name.equals('foo')");
        manager.exec("groovy","scriptdef_", 0, 0, "assert bsf != null && this.class.name.equals('_')");
    }

    public void testInvalidName() throws Exception {
        manager.exec("groovy", null, 0, 0, "assert bsf != null");
        manager.exec("groovy", "", 0, 0, "assert bsf != null");
        manager.exec("groovy", "-", 0, 0, "assert bsf != null");
    }

    public void testCompileErrorWithExec() throws Exception {
        try {
            manager.exec("groovy", "dummy", 0, 0, "assert assert");
            fail("Should have caught compile exception");
        } catch (BSFException e) {
            assertTrue("e.getMessage() should contain CompilationError: " + e.getMessage(),
                    e.getMessage().contains("CompilationError"));
        }
    }

    public void testCompileErrorWithEval() throws Exception {
        try {
            manager.eval("groovy", "dummy", 0, 0, "assert assert");
            fail("Should have caught compile exception");
        } catch (BSFException e) {
            assertTrue("e.getMessage() should contain CompilationError: " + e.getMessage(),
                    e.getMessage().contains("CompilationError"));
        }
    }

    public void testExec() throws Exception {
        manager.exec("groovy", "Test1.groovy", 0, 0, "assert bsf != null , 'should have a bsf variable'");
    }

    public void testApplyWithClosure() throws Exception {
        Vector ignoreParamNames = null;
        Vector ignoreArgs = null;
        Integer actual = (Integer) manager.apply("groovy", "applyTest", 0, 0,
                "251", ignoreParamNames, ignoreArgs);
        assertEquals(251, actual.intValue());
    }

    public void testApply() throws Exception {
        Vector ignoreParamNames = null;
        Vector<Integer> args = new Vector<>();
        args.add(2);
        args.add(5);
        args.add(1);
        Integer actual = (Integer) manager.apply("groovy", "applyTest", 0, 0,
                "def summer = { a, b, c -> a * 100 + b * 10 + c }", ignoreParamNames, args);
        assertEquals(251, actual.intValue());
    }

    public void testBracketName() throws Exception {
        manager.exec("groovy", "Test1<groovy>", 0, 0, "assert bsf != null , 'should have a bsf variable'");
    }

    public void testEval() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "return [1, 2, 3]");
        assertTrue("Should return a list: " + answer, answer instanceof List);
        List list = (List) answer;
        assertEquals("List should be of right size", 3, list.size());
    }

    public void testTwoEvalsWithSameName() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "return 'cheese'");
        assertEquals("cheese", answer);
        answer = manager.eval("groovy", "Test1.groovy", 0, 0, "return 'gromit'");
        assertEquals("gromit", answer);
    }

    public void testExecBug() throws Exception {
        for (int i = 0; i < 10; i++) {
            manager.exec("groovy", "Test1.groovy", 0, 0, "assert true");
            manager.exec("groovy", "Test1.groovy", 0, 0, "assert true");
        }
    }

    public void testBsfVariables() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "assert this.bsf != null\n  return this.bsf");
        assertTrue("Should have an answer", answer != null);
    }

    public void testNotFoundVariables() throws Exception {
        manager.registerBean("x", 4);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('y'); assert valueOfX == null");
        assertNull("Undeclared beans should yield null", answer);
    }

    public void testRegisteredVariables() throws Exception {
        manager.registerBean("x", 4);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", 5, answer);
    }

    public void testUnregisteredVariables() throws Exception {
        manager.registerBean("x", 4);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", 5, answer);
        manager.unregisterBean("x");
        // have to lookup registered beans
        answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == null");
        assertNull("Unregistered beans should yield null", answer);
    }

    public void testDeclaredVariables() throws Exception {
        manager.declareBean("xyz", 4, Integer.class);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "xyz + 1");
        assertEquals("Incorrect return", 5, answer);
    }

    public void testUndeclaredVariables() throws Exception {
        manager.declareBean("abc", 4, Integer.class);
        // declared beans should just be available
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "abc + 1");
        assertEquals("Incorrect return", 5, answer);
        manager.undeclareBean("abc");
        answer = manager.eval("groovy", "Test1.groovy", 0, 0, "abc");
        assertNull("Undeclared beans should yield null", answer);
    }

    public void testCall() throws Exception {
        BSFEngine bsfEngine = manager.loadScriptingEngine("groovy");
        manager.declareBean("myvar", "hello", String.class);
        Object myvar = manager.lookupBean("myvar");
        String result = (String) bsfEngine.call(myvar, "reverse", new Object[]{});
        assertEquals("olleh", result);
    }

    public void testExecFile() throws Exception {
        execScript("src/test/resources/groovy/script/MapFromList.groovy");
    }

    protected void execScript(String fileName) throws Exception {
        manager.exec("groovy", fileName, 0, 0, ResourceGroovyMethods.getText(new File(fileName)));
    }
}
