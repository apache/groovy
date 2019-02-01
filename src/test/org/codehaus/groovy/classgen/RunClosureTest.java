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
