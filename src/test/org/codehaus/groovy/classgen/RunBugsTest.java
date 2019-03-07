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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;

/**
 * A helper class for testing bugs in code generation errors. By turning on the
 * logging in TestSupport we can dump the ASM code generation code for inner
 * classes etc.
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

}
