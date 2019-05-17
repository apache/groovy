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
package org.codehaus.groovy.ant;

import groovy.test.GroovyTestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * Unit tests for the {@link Groovyc} ant task.
 * <p>
 * NB the *.groovy files in this directory should not get compiled with the rest of the test classes
 * since that would ruin the whole point of testing compilation by the Ant tasks.  In fact it doesn't
 * matter as the tests remove all class files that should not pre-exist from this directory at each step.
 */
public class GroovycTest extends GroovyTestCase {
    private final String classDirectory = "target/classes/groovy/test/org/codehaus/groovy/ant/";
    private final File antFile = new File("src/test-resources/org/codehaus/groovy/ant/GroovycTest.xml");
    private Project project;
    private static boolean warned = false;

    protected void setUp() throws Exception {
        super.setUp(); //  Potentially throws Exception.
        project = new Project();
        project.init();
        ProjectHelper.getProjectHelper().parse(project, antFile);
        project.executeTarget("clean");
        String altJavaHome = System.getProperty("java.home");
        if (altJavaHome.lastIndexOf("jre") >= 0) {
            altJavaHome = altJavaHome.substring(0, altJavaHome.lastIndexOf("jre"));
        } else {
            altJavaHome = altJavaHome + File.separator + "jre";
        }
        try {
            File altFile = new File(altJavaHome);
            if (altFile.exists()) {
                project.setProperty("alt.java.home", altJavaHome);
            }
        } catch (Exception e) {
            // could be security, io, etc.  Ignore it.
            // End result is as if .exists() returned null
        }
    }

    private void ensureNotPresent(final String classname) {
        if (!(new File(classDirectory + "GroovycTest.class")).exists()) {
            fail("Class file for GroovycTest does not exist and should.");
        }
        if ((new File(classDirectory + classname + ".class")).exists()) {
            fail("Class file for " + classname + " already exists and shouldn't.");
        }
    }

    private void ensurePresent(final String classname) {
        if (!(new File(classDirectory + classname + ".class")).exists()) {
            fail("Class file for " + classname + " does not exist and should.");
        }
    }

    private void ensureResultOK(final String classname) {
        if (!(new File(classDirectory + classname + ".class")).exists()) {
            fail("Class file for " + classname + " does not exist and should.");
        }
        final File result = new File(classDirectory + classname + "_Result.txt");
        final char[] buffer = new char[10];
        FileReader fr = null;
        try {
            fr = new FileReader(result);
            fr.read(buffer);
            assertEquals("OK.", new String(buffer).trim());
        } catch (final FileNotFoundException fnfe) {
            fail("File " + result.getName() + " should have been created but wasn't.");
        } catch (final IOException ioe) {
            fail("Error reading file " + result.getName() + ".");
        } finally {
            if (null != fr) {
                try {
                    fr.close();
                } catch (IOException e) {
                    fail("Error close file reader " + result.getName() + ".");
                }
            }
        }
    }

    public void testGroovycTest1_NoFork_NoClasspath() {
        ensureExecutes("GroovycTest1_NoFork_NoClasspath");
    }

    public void testGroovycTest1_NoFork_WithGroovyClasspath() {
        ensureExecutes("GroovycTest1_NoFork_WithGroovyClasspath");
    }

    public void testGroovycTest1_NoFork_WithJavaClasspath() {
        ensureExecutes("GroovycTest1_NoFork_WithJavaClasspath");
    }

    public void testGroovycTest1_NoFork_WithBothClasspath() {
        ensureExecutes("GroovycTest1_NoFork_WithBothClasspath");
    }

    public void testGroovycTest1_ForkGroovy_NoClasspath() {
        ensureExecutes("GroovycTest1_ForkGroovy_NoClasspath");
    }

    public void testGroovycTest1_ForkGroovy_WithGroovyClasspath() {
        ensureExecutes("GroovycTest1_ForkGroovy_WithGroovyClasspath");

    }

    public void testGroovycTest1_ForkGroovy_WithJavaClasspath() {
        ensureExecutes("GroovycTest1_ForkGroovy_WithJavaClasspath");
    }

    public void testGroovycTest1_ForkGroovy_WithBothClasspath() {
        ensureExecutes("GroovycTest1_ForkGroovy_WithBothClasspath");
    }

    public void testGroovycTest1_Joint_NoFork_NoClasspath() {
        ensureExecutes("GroovycTest1_Joint_NoFork_NoClasspath");
    }

    public void testGroovycTest1_Joint_NoFork_WithGroovyClasspath() {
        ensureExecutes("GroovycTest1_Joint_NoFork_WithGroovyClasspath");
    }

    public void testGroovyc_Joint_NoFork_NestedCompilerArg_WithGroovyClasspath() {
        // capture ant's output so we can verify the effect of passing compilerarg to javac
        ByteArrayOutputStream allOutput = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(allOutput);
        PrintStream origOut = System.out;
        System.setOut(out);

        ensureNotPresent("IncorrectGenericsUsage");
        project.executeTarget("Groovyc_Joint_NoFork_NestedCompilerArg_WithGroovyClasspath");
        ensurePresent("IncorrectGenericsUsage");

        String antOutput = allOutput.toString();
        antOutput = adjustOutputToHandleOpenJDKJavacOutputDifference(antOutput);
        System.setOut(origOut);

        // verify if passing -Xlint in compilerarg had its effect
        Pattern p = Pattern.compile(".*?found[ ]*:[ ]*java.util.ArrayList.*", Pattern.DOTALL);
        assertTrue("Expected line 1 not found in ant output", p.matcher(antOutput).matches());
        p = Pattern.compile(".*?required[ ]*:[ ]*java.util.ArrayList<java.lang.String>.*", Pattern.DOTALL);
        assertTrue("Expected line 2 not found in ant output", p.matcher(antOutput).matches());
    }

    /**
     * For the code:
     * private ArrayList<String> x = new ArrayList<String>();
     * x = (ArrayList)z ;
     * Upto JDK6, 'javac -Xlint' produces the following output:
     * found   : java.util.ArrayList
     * required: java.util.ArrayList<java.lang.String>
     * But, OpenJDK seems to be producing the following output:
     * required: ArrayList<String>
     * found:    ArrayList
     * So, we first adjust the output a bit, so that difference in the output brought in by OpenJDK javac
     * does not impact the test adversely
     */
    private String adjustOutputToHandleOpenJDKJavacOutputDifference(String antOutput) {
        if (!antOutput.contains("java.util.ArrayList") && antOutput.contains("ArrayList")) {
            antOutput = antOutput.replace("ArrayList", "java.util.ArrayList");
        }
        if (!antOutput.contains("java.lang.String") && antOutput.contains("String")) {
            antOutput = antOutput.replace("String", "java.lang.String");
        }
        return antOutput;
    }

    public void testGroovycTest1_Joint_NoFork_WithJavaClasspath() {
        ensureExecutes("GroovycTest1_Joint_NoFork_WithJavaClasspath");
    }

    public void testGroovycTest1_Joint_NoFork_WithBothClasspath() {
        ensureExecutes("GroovycTest1_Joint_NoFork_WithBothClasspath");
    }

    public void testGroovycTest1_Joint_ForkGroovy_NoClasspath() {
        ensureExecutes("GroovycTest1_Joint_ForkGroovy_NoClasspath");
    }

    public void testGroovycTest1_Joint_ForkGroovy_WithGroovyClasspath() {
        ensureExecutes("GroovycTest1_Joint_ForkGroovy_WithGroovyClasspath");
    }

    public void testGroovycTest1_Joint_ForkGroovy_WithJavaClasspath() {
        ensureExecutes("GroovycTest1_Joint_ForkGroovy_WithJavaClasspath");
    }

    public void testGroovycTest1_Joint_ForkGroovy_WithBothClasspath() {
        ensureExecutes("GroovycTest1_Joint_ForkGroovy_WithBothClasspath");
    }

    public void testGroovycTest1_ForkGroovy_NoClasspath_WithJavaHome() {
        ensureExecutesWithJavaHome("GroovycTest1_ForkGroovy_NoClasspath_WithJavaHome");
    }

    public void testGroovycTest1_ForkGroovy_WithGroovyClasspath_WithJavaHome() {
        ensureExecutesWithJavaHome("GroovycTest1_ForkGroovy_WithGroovyClasspath_WithJavaHome");
    }

    public void testGroovycTest1_ForkGroovy_WithJavaClasspath_WithJavaHome() {
        ensureExecutesWithJavaHome("GroovycTest1_ForkGroovy_WithJavaClasspath_WithJavaHome");
    }

    public void testGroovycTest1_ForkGroovy_WithBothClasspath_WithJavaHome() {
        ensureExecutesWithJavaHome("GroovycTest1_ForkGroovy_WithBothClasspath_WithJavaHome");
    }

    public void testGroovycTest1_ForkGroovy_NoClasspath_Fail() {
        ensureFails("GroovycTest1_ForkGroovy_NoClasspath_Fail");
    }

    public void testNoForkWithNoIncludeAntRuntime() {
        ensureFails("noForkNoAntRuntime");
    }

    private void ensureExecutes(String target) {
        ensureNotPresent("GroovycTest1");
        project.executeTarget(target);
        ensureResultOK("GroovycTest1");
    }

    private void ensureExecutesWithJavaHome(String target) {
        if (project.getProperty("alt.java.home") != null) {
            ensureExecutes(target);
        } else {
            if (!warned) {
                System.err.println("Forked Java tests skipped, not a sun JDK layout");
                warned = true;
            }
        }
    }

    private void ensureFails(String target) {
        File badGroovy = new File(antFile.getParentFile(), "GroovyTestBad1.groovy");
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(badGroovy));
        } catch (FileNotFoundException e) {
            fail("Could not create test file:" + badGroovy.getAbsolutePath());
        }
        ps.println("class GroovyTest1Bad { Thi$ $hould Fail! (somehow) };:??''+_|\\|");
        ps.close();
        ensureNotPresent("GroovycTestBad1");
        try {
            project.executeTarget(target);
            fail("Ant script should have failed with execution exception");
        } catch (BuildException be) {
            be.printStackTrace();
            ensureNotPresent("GroovycTestBad1");
        } finally {
            badGroovy.delete();
        }
    }

}
