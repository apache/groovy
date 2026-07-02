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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests the compiling & running of GroovyTestCases
 */
public class FileSystemCompilerTest {

    private FileSystemCompiler compiler = null;
    private final boolean dumpClass = true;

    @Test
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
            File file = new File("src/test/groovy/groovy/" + name);
            files.add(file);
            assertTrue(file.exists(), "Could not find source file: " + file);
        }

        compiler.compile((File[]) files.toArray(new File[names.length]));
    }

    @BeforeEach
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

    @Test
    public void testCommandLine() throws Exception {
        try {
            FileSystemCompiler.commandLineCompile(new String[] {"--bogus-option"});
            fail("Compiler fails to reject bogus command line");
        } catch (Exception re) {
            // this is why shouldFail {} exists in Groovy tests
        }

        File dir = new File("build/test-generated-classes/cl");
        dir.mkdirs();
        FileSystemCompiler.commandLineCompile(new String[] {"src/test/groovy/groovy/LittleClosureTest.groovy", "-d", dir.getPath()});
    }

    @Test
    public void testDeleteRecursiveDoesNotFollowSymlink() throws Exception {
        File base = Files.createTempDirectory("deleteRecursiveSymlink").toFile();
        try {
            // a directory outside the tree being deleted, holding a file that must survive
            File outside = new File(base, "outside");
            assertTrue(outside.mkdir());
            File survivor = new File(outside, "survivor.txt");
            Files.write(survivor.toPath(), "keep".getBytes());

            // the tree we delete, containing a symlink pointing at the outside directory
            File tree = new File(base, "tree");
            assertTrue(tree.mkdir());
            File link = new File(tree, "link");
            try {
                Files.createSymbolicLink(link.toPath(), outside.toPath());
            } catch (IOException | UnsupportedOperationException e) {
                assumeTrue(false, "symbolic links not supported on this platform: " + e);
            }

            FileSystemCompiler.deleteRecursive(tree);

            // the tree and the link are gone, but the target's contents are untouched
            assertFalse(tree.exists(), "tree should be deleted");
            assertTrue(outside.exists(), "linked-to directory must survive");
            assertTrue(survivor.exists(), "linked-to file must survive");
        } finally {
            FileSystemCompiler.deleteRecursive(base);
        }
    }

}
