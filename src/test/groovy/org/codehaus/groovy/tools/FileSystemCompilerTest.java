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
import org.codehaus.groovy.control.ErrorFormat;
import org.codehaus.groovy.control.Phases;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // GROOVY-12204: a target phase before CLASS_GENERATION must suppress class file output
    @Test
    public void testCheckOnlyCompileProducesNoClassFiles(@TempDir Path dir) throws Exception {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTargetPhase(Phases.INSTRUCTION_SELECTION);
        compile(dir, "CheckDemo", "class CheckDemo { }", configuration, null);
        assertFalse(Files.exists(dir.resolve("CheckDemo.class")), "check-only compile must not write class files");

        // control: the same shape of source with the default configuration does produce a class file
        compile(dir, "FullDemo", "class FullDemo { }", null, null);
        assertTrue(Files.exists(dir.resolve("FullDemo.class")), "sanity: full compile writes class files");
    }

    // GROOVY-12204: check-only compilation still surfaces static type-checking errors
    @Test
    public void testCheckOnlyCompileReportsTypeErrors(@TempDir Path dir) throws Exception {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTargetPhase(Phases.INSTRUCTION_SELECTION);
        Exception e = assertThrows(Exception.class, () ->
                compile(dir, "CheckError", "@groovy.transform.CompileStatic\nclass CheckError { int f() { int x = 'oops'; x } }", configuration, null));
        assertTrue(e.getMessage().contains("Cannot assign value of type"), "expected a static type-checking error but got: " + e.getMessage());
        assertFalse(Files.exists(dir.resolve("CheckError.class")));
    }

    // GROOVY-12204: --check command-line sugar for a check-only compile
    @Test
    public void testCheckCommandLineOption(@TempDir Path dir) throws Exception {
        File file = dir.resolve("CliCheckDemo.groovy").toFile();
        Files.write(file.toPath(), "class CliCheckDemo { }".getBytes(StandardCharsets.UTF_8));
        FileSystemCompiler.commandLineCompile(new String[] {"--check", "-d", dir.toString(), file.getPath()});
        assertFalse(Files.exists(dir.resolve("CliCheckDemo.class")), "--check must not write class files");

        FileSystemCompiler.commandLineCompile(new String[] {"-d", dir.toString(), file.getPath()});
        assertTrue(Files.exists(dir.resolve("CliCheckDemo.class")), "sanity: without --check the class file is written");
    }

    // GROOVY-12312: --error-format selects the diagnostic rendering; picocli accepts the
    // lower-case toString() form as well as the enum name
    @Test
    public void testErrorFormatCommandLineOption() throws Exception {
        assertEquals(ErrorFormat.FULL, parseErrorFormat(), "an absent option leaves the default in place");
        assertEquals(ErrorFormat.SHORT, parseErrorFormat("--error-format", "short"));
        assertEquals(ErrorFormat.SHORT, parseErrorFormat("--error-format=short"));
        assertEquals(ErrorFormat.SHORT, parseErrorFormat("--error-format", "SHORT"));
        assertEquals(ErrorFormat.FULL, parseErrorFormat("--error-format", "full"));
    }

    private static ErrorFormat parseErrorFormat(String... args) throws Exception {
        FileSystemCompiler.CompilationOptions options = new FileSystemCompiler.CompilationOptions();
        FileSystemCompiler.configureParser(options).parseArgs(args);
        return options.toCompilerConfiguration().getErrorFormat();
    }

    // GROOVY-12311: --check stops after CLASS_GENERATION, so errors raised by class
    // verification — which runs in that phase — must be reported rather than missed
    @Test
    public void testCheckCommandLineOptionReportsVerifierErrors(@TempDir Path dir) throws Exception {
        File file = dir.resolve("CliVerifyDemo.groovy").toFile();
        Files.write(file.toPath(), "class CliVerifyDemo { def foo() { 1 }\n def foo() { 2 } }".getBytes(StandardCharsets.UTF_8));

        Exception e = assertThrows(Exception.class, () ->
                FileSystemCompiler.commandLineCompile(new String[] {"--check", "-d", dir.toString(), file.getPath()}));
        assertTrue(e.getMessage().contains("Repetitive method name/signature"),
                "expected the verifier error but got: " + e.getMessage());
        assertFalse(Files.exists(dir.resolve("CliVerifyDemo.class")), "--check must not write class files");

        // control: a full compile rejects the same source, so --check agrees with it
        assertThrows(Exception.class, () ->
                FileSystemCompiler.commandLineCompile(new String[] {"-d", dir.toString(), file.getPath()}));
    }

    // GROOVY-12311: an earlier target phase remains selectable, and because config scripts are
    // processed last it overrides --check. This is the only route back to a shallower check, so
    // the ordering it depends on is pinned here.
    @Test
    public void testConfigScriptOverridesCheckTargetPhase(@TempDir Path dir) throws Exception {
        File file = dir.resolve("CliPhaseDemo.groovy").toFile();
        Files.write(file.toPath(), "class CliPhaseDemo { def foo() { 1 }\n def foo() { 2 } }".getBytes(StandardCharsets.UTF_8));
        File script = dir.resolve("phase.groovy").toFile();
        Files.write(script.toPath(), ("configuration.targetPhase = "
                + "org.codehaus.groovy.control.Phases.INSTRUCTION_SELECTION").getBytes(StandardCharsets.UTF_8));

        // stopping before CLASS_GENERATION skips verification, so the duplicate method goes unseen
        FileSystemCompiler.commandLineCompile(new String[] {
                "--check", "--configscript", script.getPath(), "-d", dir.toString(), file.getPath()});
        assertFalse(Files.exists(dir.resolve("CliPhaseDemo.class")), "an earlier phase still writes no class files");
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
