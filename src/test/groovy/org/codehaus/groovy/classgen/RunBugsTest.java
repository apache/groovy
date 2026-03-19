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
import org.junit.jupiter.api.Test;

/**
 * A helper class for testing bugs in code generation errors. By turning on the
 * logging in TestSupport we can dump the ASM code generation code for inner
 * classes etc.
 */
final class RunBugsTest extends TestSupport {

    @Test
    void testStaticMethodCall() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/StaticMethodCallBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testTryCatchBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/TryCatchBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testRodsBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/RodsBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testCastBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/groovy/ClosureMethodCallTest.groovy");
        object.invokeMethod("testCallingClosureWithMultipleArguments", null);
    }

    @Test
    void testGuillaumesMapBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/GuillaumesMapBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testUseClosureInScript() throws Exception {
        GroovyObject object = compile("src/test/groovy/groovy/script/scriptWithClosure.groovy");
        object.invokeMethod("run", null);
    }

    @Test
    void testUseStaticInClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/UseStaticInClosureBug.groovy");
        object.invokeMethod("testBug2", null);
    }

    @Test
    void testPrimitiveTypeFieldTest() throws Exception {
        GroovyObject object = compile("src/test/groovy/groovy/PrimitiveTypeFieldTest.groovy");
        object.invokeMethod("testPrimitiveField", null);
    }

    @Test
    void testMethodDispatchBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/MethodDispatchBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testClosureInClosureTest() throws Exception {
        GroovyObject object = compile("src/test/groovy/groovy/ClosureInClosureTest.groovy");
        object.invokeMethod("testInvisibleVariable", null);
    }

    @Test
    void testOverloadInvokeMethodBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/OverloadInvokeMethodBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testClosureVariableBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ClosureVariableBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testMarkupAndMethodBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/MarkupAndMethodBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testClosureParameterPassingBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ClosureParameterPassingBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testNestedClosureBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/NestedClosure2Bug.groovy");
        object.invokeMethod("testFieldBug", null);
    }

    @Test
    void testSuperMethod2Bug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/SuperMethod2Bug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testToStringBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ToStringBug.groovy");
        object.invokeMethod("testBug", null);
    }

    @Test
    void testByteIndexBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/bugs/ByteIndexBug.groovy");
        object.invokeMethod("testBug", null);
    }
}
