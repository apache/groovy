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

import java.util.List;
import java.util.Vector;

/**
 * Tests the Caching BSF integration
 */
public class CacheBSFTest extends TestCase {

    protected BSFManager manager;
    private static final Class CACHING_ENGINE = CachingGroovyEngine.class;

    protected void setUp() throws Exception {
        // override standard engine with caching one
        BSFManager.registerScriptingEngine("groovy", CACHING_ENGINE.getName(), new String[]{"groovy", "gy"});
        manager = new BSFManager();
    }

    public void testVersion() throws Exception {
        //System.out.println("BSFManager.getVersion() = " + BSFManager.getVersion());
        BSFEngine bsfEngine = manager.loadScriptingEngine("groovy");
        assertEquals(CACHING_ENGINE, bsfEngine.getClass());
    }

    public void testExec() throws Exception {
        manager.exec("groovy", "Test1.groovy", 0, 0, "println('testing Exec')");
        //nothing to really test here...just looking for debug that says it
        // used cache version
        manager.exec("groovy", "Test1.groovy", 0, 0, "println('testing Exec')");
    }

    public void testCompileErrorWithExec() throws Exception {
        try {
            manager.exec("groovy", "dummy", 0, 0, "assert assert");
            fail("Should have caught compile exception");
        } catch (BSFException e) {
            assertTrue("e.getMessage() should contain CompilationError: " + e.getMessage(),
                    e.getMessage().indexOf("CompilationError") != -1);
        }
    }

    public void testEval() throws Exception {
        Object dontcare = manager.eval("groovy", "Test1.groovy", 0, 0, "return [1, 2, 3]");
        // nothing to really test here...just looking for debug that says it
        // used cache version
        Object answer = manager.eval("groovy", "Test.groovy", 0, 0, "return [1, 2, 3]");
        assertTrue("Should return a list: " + answer, answer instanceof List);
        List list = (List) answer;
        assertEquals("List should be of right size", 3, list.size());
    }

    public void testCompileErrorWithEval() throws Exception {
        try {
            manager.eval("groovy", "dummy", 0, 0, "assert assert");
            fail("Should have caught compile exception");
        } catch (BSFException e) {
            assertTrue("e.getMessage() should contain CompilationError: " + e.getMessage(),
                    e.getMessage().indexOf("CompilationError") != -1);
        }
    }

    public void testBuiltInVariable() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "assert this.bsf != null\n  return this.bsf");
        assertTrue("Should have an answer", answer != null);
    }

    public void testVariables() throws Exception {
        manager.registerBean("x", new Integer(4));
        Object dontcare = manager.eval("groovy", "Test1.groovy", 0, 0,
                "valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        // nothing to really test here...just looking for debug that says it
        // used cache version
        Object answer = manager.eval("groovy", "Test2.groovy", 0, 0,
                "valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
    }

    public void testClassLoaderSet() throws BSFException {
        CachingGroovyEngine cachingGroovyEngine = new CachingGroovyEngine();
        manager.setClassLoader(null);
        cachingGroovyEngine.initialize(manager, "dummy", new Vector());
        // still working implies classloader set, coverage confirms this
        assertEquals("hi", manager.eval("groovy", "dummy", 0, 0, "'hi'"));
    }

    public void testDeclaredVariables() throws Exception {
        manager.declareBean("foo", new Integer(5), Integer.class);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "valueOfFoo = foo; return valueOfFoo");
        assertEquals(new Integer(5), answer);
        manager.declareBean("foo", new Integer(6), Integer.class);
        answer = manager.eval("groovy", "Test2.groovy", 0, 0, "valueOfFoo = foo; return valueOfFoo");
        assertEquals(new Integer(6), answer);
    }
}
