/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.bsf;

import junit.framework.TestCase;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Tests the BSF integration
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
 * @version $Revision$
 */
public class BSFTest extends TestCase {

    protected BSFManager manager;

    protected void setUp() throws Exception {
        manager = new BSFManager();
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
                    e.getMessage().indexOf("CompilationError") != -1);
        }
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
        Vector args = new Vector();
        args.add(new Integer(2));
        args.add(new Integer(5));
        args.add(new Integer(1));
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
        manager.registerBean("x", new Integer(4));
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('y'); assert valueOfX == null");
        assertNull("Undeclared beans should yield null", answer);
    }

    public void testRegisteredVariables() throws Exception {
        manager.registerBean("x", new Integer(4));
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
    }

    public void testUnregisteredVariables() throws Exception {
        manager.registerBean("x", new Integer(4));
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
        manager.unregisterBean("x");
        // have to lookup registered beans
        answer = manager.eval("groovy", "Test1.groovy", 0, 0,
                "def valueOfX = this.bsf.lookupBean('x'); assert valueOfX == null");
        assertNull("Unregistered beans should yield null", answer);
    }

    public void testDeclaredVariables() throws Exception {
        manager.declareBean("xyz", new Integer(4), Integer.class);
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "xyz + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
    }

    public void testUndeclaredVariables() throws Exception {
        manager.declareBean("abc", new Integer(4), Integer.class);
        // declared beans should just be available
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "abc + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
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
        execScript("src/test/groovy/script/MapFromList.groovy");
        execScript("src/test/groovy/script/AtomTestScript.groovy");
    }

    protected void execScript(String fileName) throws Exception {
        manager.exec("groovy", fileName, 0, 0, DefaultGroovyMethods.getText(new File(fileName)));
    }
}
