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
package org.codehaus.groovy.wiki;

import junit.framework.TestCase;

import org.radeox.engine.context.BaseRenderContext;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class TestCaseRenderEngineTest extends TestCase {

    private BaseRenderContext context = new BaseRenderContext();

    public void testRender() {
        assertRender(
            "blah blah {code:groovy}x = 1; assert x == 1{code} whatnot",
            "package wiki\nclass someFileTest extends GroovyTestCase {\n\n/*\nblah blah */ \n\n  void testCase1() {\nx = 1; assert x == 1\n}\n\n /* whatnot\n*/\n\nvoid testDummy() {\n// this is a dummy test case\n}\n\n}\n");		
    }

    public void testRenderWithScript() {
        assertRender(
            "blah blah {code:groovysh}x = 1; println 'hello ${x}'{code} whatnot",
             "package wiki\nclass someFileTest extends GroovyTestCase {\n\n/*\nblah blah */ \n\n  void testScript1() {\n    assertScript( <<<SCRIPT_EOF1\nx = 1; println 'hello $${x}'\nSCRIPT_EOF1 )\n}    \n\n /* whatnot\n*/\n\nvoid testDummy() {\n// this is a dummy test case\n}\n\n}\n");
    }

    protected void assertRender(String input, String expected) {
        TestCaseRenderEngine test = new TestCaseRenderEngine();
        context.set("name", "someFile.wiki");
        String answer = test.render(input, context);

        System.out.println("Converted: " + input);
        System.out.println("Into: " + answer);

        // lets convert the output to a String we can cut-n-paste
        System.out.println(answer.replaceAll("\n", "\\\\n"));
        
        assertEquals("Rendering", expected, answer);
    }

}
