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
package org.codehaus.groovy.tools;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;

/**
 * A handy unit test case for dumping the output of the compiler
 */
public class CompilerTest extends GroovyTestCase {

    Compiler compiler = null;
    boolean dumpClass = true;

    public void testMethodCall() throws Exception {
        //runTest("ClosureMethodTest.groovy");
        //runTest("tree/VerboseTreeTest.groovy");
        //runTest("tree/NestedClosureBugTest.groovy");
        runTest("tree/SmallTreeTest.groovy");
        //runTest("LittleClosureTest.groovy");
    }

    protected void runTest(String name) throws Exception {
        File file = new File("src/test/groovy/" + name);

        assertTrue("Could not find source file: " + file, file.exists());

        compiler.compile(file);
    }

    protected void setUp() throws Exception {
        File dir = new File("target/test-generated-classes");
        dir.mkdirs();

        CompilerConfiguration config = new CompilerConfiguration();
        config.setTargetDirectory(dir);
        config.setDebug(dumpClass);

        compiler = new Compiler(config);
    }

}
