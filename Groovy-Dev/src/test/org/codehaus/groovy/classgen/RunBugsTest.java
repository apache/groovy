/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
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

package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;

/**
 * A helper class for testing bugs in code generation errors. By turning on the
 * logging in TestSupport we can dump the ASM code generation code for inner
 * classes etc.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class RunBugsTest extends TestSupport {

    public void testStaticMethodCall() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/StaticMethodCallBug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testTryCatchBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/TryCatchBug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testRodsBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/RodsBug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testCastBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureMethodCallTest.groovy");
        object.invokeMethod("testCallingClosureWithMultipleArguments", null);
    }

    public void testGuillaumesMapBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/GuillaumesMapBug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testUseClosureInScript() throws Exception {
        GroovyObject object = compile("src/test/groovy/script/UseClosureInScript.groovy");
        object.invokeMethod("run", null);
    }

    public void testUseStaticInClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/UseStaticInClosureBug.groovy");
        object.invokeMethod("testBug2", null);
    }

    public void testPrimitiveTypeFieldTest() throws Exception {
        GroovyObject object = compile("src/test/groovy/PrimitiveTypeFieldTest.groovy");
        object.invokeMethod("testPrimitiveField", null);
    }
    
    public void testMethodDispatchBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/MethodDispatchBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testClosureInClosureTest() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureInClosureTest.groovy");
        object.invokeMethod("testInvisibleVariable", null);
    }
    public void testStaticMarkupBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/StaticMarkupBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testOverloadInvokeMethodBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/OverloadInvokeMethodBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testClosureVariableBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ClosureVariableBug.groovy");
        object.invokeMethod("testBug", null);
    }
    
    public void testMarkupAndMethodBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/MarkupAndMethodBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testClosureParameterPassingBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ClosureParameterPassingBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testNestedClosureBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/NestedClosure2Bug.groovy");
        object.invokeMethod("testFieldBug", null);
    }
    public void testSuperMethod2Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/SuperMethod2Bug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testToStringBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ToStringBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testByteIndexBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ByteIndexBug.groovy");
        object.invokeMethod("testBug", null);
    }
    public void testGroovy252_Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Groovy252_Bug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testGroovy303_Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Groovy303_Bug.groovy");
        object.invokeMethod("testBug", null);
    }


}
