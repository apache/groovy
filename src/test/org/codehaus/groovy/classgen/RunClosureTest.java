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

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class RunClosureTest extends TestSupport {

    public void testClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/ClosureUsingOuterVariablesTest.groovy");
        object.invokeMethod("testExampleUseOfClosureScopesUsingEach", null);
    }

    public void testStaticClosureBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/StaticClosurePropertyBug.groovy");
        object.invokeMethod("testCallStaticClosure", null);
    }

    public void testZoharsBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ZoharsBug.groovy");
        object.invokeMethod("testBug", null);
    }

    public void testBytecodeBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/BytecodeBug.groovy");
        object.invokeMethod("testTedsBytecodeBug", null);
    }

    public void testBytecode2Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Bytecode2Bug.groovy");
        object.invokeMethod("testTedsBytecodeBug", null);
    }

    public void testBytecode3Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Bytecode3Bug.groovy");
        //object.invokeMethod("testInject", null);
        object.invokeMethod("testIncrementPropertyInclosure", null);
    }

    public void testBytecode4Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Bytecode4Bug.groovy");
        object.invokeMethod("testInject", null);
        object.invokeMethod("testUsingProperty", null);
    }

    public void testBytecode5Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Bytecode5Bug.groovy");
        object.invokeMethod("testUsingLocalVar", null);
    }

    public void testBytecode6Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/Bytecode6Bug.groovy");
        object.invokeMethod("testPreFixReturn", null);
    }

    public void testPropertyTest() throws Exception {
        GroovyObject object = compile("src/test/groovy/PropertyTest.groovy");
        object.invokeMethod("testNormalPropertyGettersAndSetters", null);
    }

}
