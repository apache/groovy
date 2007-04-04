/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
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
 *
 * @author James Birchfield
 * @version $Revision$
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
