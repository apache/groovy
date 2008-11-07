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

package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;

import java.awt.*;

/**
 * Tests dynamically compiling and running a new class
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class RunGroovyTest extends TestSupport {

    public void testArrayBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/ToArrayBugTest.groovy");
        object.invokeMethod("testToArrayBug", null);
    }


    public void testPostfix() throws Exception {
        GroovyObject object = compile("src/test/groovy/PostfixTest.groovy");
        object.invokeMethod("testIntegerPostfix", null);
    }

    public void testMap() throws Exception {
        GroovyObject object = compile("src/test/groovy/MapTest.groovy");
        object.invokeMethod("testMap", null);
    }

    public void testClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureMethodTest.groovy");
        object.invokeMethod("testListCollect", null);
    }

    public void testClosureWithDefaultParam() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureWithDefaultParamTest.groovy");
        object.invokeMethod("methodWithDefaultParam", null);
    }

    public void testOptionalReturn() throws Exception {
        GroovyObject object = compile("src/test/groovy/OptionalReturnTest.groovy");
        object.invokeMethod("testSingleExpression", null);
        object.invokeMethod("testLastExpressionIsSimple", null);
    }

    public void testConsole() throws Exception {
        try {
            GroovyObject object = compile("src/main/groovy/ui/Console.groovy");
        } catch (NoClassDefFoundError e) {
            
        } catch (HeadlessException he) {
            // ignore to deal with headless environments
        }
    }
}
