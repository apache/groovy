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

import java.util.List;

import junit.framework.TestCase;

import org.apache.bsf.BSFManager;

/**
 * Tests the BSF integration
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BSFTest extends TestCase {

    protected BSFManager manager;

    protected void setUp() throws Exception {
        // lets manually register Groovy until its part of the BSF distro
        BSFManager.registerScriptingEngine("groovy", GroovyEngine.class.getName(), new String[] { "groovy", "gy" });

        manager = new BSFManager();
    }

    public void testExec() throws Exception {
        manager.exec(
            "groovy",
            "Test1.groovy",
            0,
            0,
            "println('testing Exec'); assert bsf != null : 'should have a bsf variable'");
    }

    public void testEval() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "println('testing Eval')\n  return [1, 2, 3]");

        assertTrue("Should return a list: " + answer, answer instanceof List);
        List list = (List) answer;
        assertEquals("List should be of right size", 3, list.size());

        System.out.println("The eval returned the value: " + list);
    }

    public void testTwoEvalsWithSameName() throws Exception {
        Object answer = manager.eval("groovy", "Test1.groovy", 0, 0, "println('first line')\n  return 'cheese'");
        assertEquals("cheese", answer);

        answer = manager.eval("groovy", "Test1.groovy", 0, 0, "println('second line')\n  return 'gromit'");
        assertEquals("gromit", answer);
    }

    public void testExecBug() throws Exception {
        for (int i = 0; i < 10; i++) {
            manager.exec("groovy", "Test1.groovy", 0, 0, "println('testing Exec')");

            manager.exec("groovy", "Test1.groovy", 0, 0, "println('testing Exec')");
        }
    }

    public void testBsfVariables() throws Exception {
        Object answer =
            manager.eval(
                "groovy",
                "Test1.groovy",
                0,
                0,
                "println('testing variables')\n  assert this.bsf != null\n  return this.bsf");
        assertTrue("Should have an answer", answer != null);
    }

    public void testVariables() throws Exception {
        manager.registerBean("x", new Integer(4));

        Object answer =
            manager.eval(
                "groovy",
                "Test1.groovy",
                0,
                0,
                "valueOfX = this.bsf.lookupBean('x'); assert valueOfX == 4; valueOfX + 1");
        assertEquals("Incorrect return", new Integer(5), answer);
    }
}
