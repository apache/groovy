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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    // warnings collected during a SUCCESSFUL compile must reach the command-line user via the
    // injectable writer; the failure path already surfaces them alongside the errors
    @Test
    public void testWarningsAreDisplayedOnSuccessfulCompile(@TempDir Path dir) throws Exception {
        StringWriter warnings = new StringWriter();
        compile(dir, "WarnDemo", "class WarnDemo { }", warningEmittingConfiguration(), warnings);
        String output = warnings.toString();
        assertTrue(output.contains("spike warning marker"), "expected the collected warning but got: " + output);
        assertTrue(output.contains("1 warning"), "expected a warning summary but got: " + output);
    }

    @Test
    public void testCleanCompileWritesNothing(@TempDir Path dir) throws Exception {
        StringWriter warnings = new StringWriter();
        compile(dir, "CleanDemo", "class CleanDemo { }", null, warnings);
        assertTrue(warnings.toString().isEmpty(), "clean compile should produce no warning output but got: " + warnings);
    }

    // a null writer (the default for embedders such as the in-process Ant task) must leave
    // warning handling to the caller — the shared doCompilation entry point stays quiet
    @Test
    public void testNullWriterProducesNoWarningOutput(@TempDir Path dir) throws Exception {
        // proves the suppression is due to the null writer, not the absence of a warning:
        // the same configuration DOES emit when a writer is supplied
        StringWriter probe = new StringWriter();
        compile(dir, "QuietProbe", "class QuietProbe { }", warningEmittingConfiguration(), probe);
        assertTrue(probe.toString().contains("spike warning marker"), "sanity: warning should exist with a writer");

        // must not throw and (having no sink) produces nothing observable
        compile(dir, "QuietDemo", "class QuietDemo { }", warningEmittingConfiguration(), null);
    }

    // integration of the display fix with the level-1 demotion: a genuinely warnable program
    // (a property that cannot override a final accessor, GROOVY-8659) now surfaces its warning
    // at the DEFAULT warning level, where previously it was a suppressed level-2 warning
    @Test
    public void testDemotedWarningVisibleAtDefaultLevel(@TempDir Path dir) throws Exception {
        StringWriter warnings = new StringWriter();
        compile(dir, "OverrideDemo",
                "abstract class A { final String getFoo() { 'A' } }\n"
                        + "class OverrideDemo extends A { final String foo = 'C' }\n",
                null, warnings);
        assertTrue(warnings.toString().contains("cannot override final method getFoo"),
                "expected the demoted (level-1) property-override warning at default level but got: " + warnings);
    }

    private static CompilerConfiguration warningEmittingConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                source.getErrorCollector().addWarning(WarningMessage.LIKELY_ERRORS, "spike warning marker",
                        Token.newString(classNode.getName(), 1, 1), source);
            }
        });
        return configuration;
    }

    private static void compile(Path dir, String className, String source, CompilerConfiguration configuration, Writer warningWriter) throws Exception {
        File file = dir.resolve(className + ".groovy").toFile();
        Files.write(file.toPath(), source.getBytes(StandardCharsets.UTF_8));
        if (configuration == null) {
            configuration = new CompilerConfiguration();
        }
        configuration.setTargetDirectory(dir.toFile());
        FileSystemCompiler.doCompilation(configuration, null, new String[]{file.getPath()}, false, warningWriter);
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
