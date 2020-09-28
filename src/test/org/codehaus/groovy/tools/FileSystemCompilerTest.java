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

import groovy.test.GroovyTestCase;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests the compiling & running of GroovyTestCases
 */
public class FileSystemCompilerTest extends GroovyTestCase {

    FileSystemCompiler compiler = null;
    final boolean dumpClass = true;

    public void testMethodCall() throws Exception {
        runTest(new String[] {"ClosureMethodTest.groovy"});
        runTest(new String[] {"tree/VerboseTreeTest.groovy"});
        runTest(new String[] {"tree/NestedClosureBugTest.groovy"});
        runTest(new String[] {"tree/SmallTreeTest.groovy"});
        runTest(new String[] {"LittleClosureTest.groovy"});
        runTest(new String[] {"JointJava.java", "JointGroovy.groovy"});
    }

    protected void runTest(String[] names) throws Exception {
        List files = new ArrayList();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            File file = new File("src/test/groovy/" + name);
            files.add(file);
            assertTrue("Could not find source file: " + file, file.exists());
        }

        compiler.compile((File[]) files.toArray(new File[names.length]));
    }

    protected void setUp() throws Exception {
        File dir = new File("build/test-generated-classes");
        dir.mkdirs();
        Map options = new HashMap();
        options.put("stubDir", dir);

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTargetDirectory(dir);
        configuration.setVerbose(dumpClass);
        configuration.setJointCompilationOptions(options);

        compiler = new FileSystemCompiler(configuration);
    }

    public void testCommandLine() throws Exception {
        try {
            FileSystemCompiler.commandLineCompile(new String[] {"--bogus-option"});
            fail("Compiler fails to reject bogus command line");
        } catch (Exception re) {
            // this is why shouldFail {} exists in Groovy tests
        }

        File dir = new File("build/test-generated-classes/cl");
        dir.mkdirs();
        FileSystemCompiler.commandLineCompile(new String[] {"src/test/groovy/LittleClosureTest.groovy", "-d", dir.getPath()});
    }

}
