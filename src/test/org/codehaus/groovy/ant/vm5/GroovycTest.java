package org.codehaus.groovy.ant.vm5;

import java.io.*;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import groovy.util.GroovyTestCase;

/**
 * Unit tests for the {@link Groovyc} ant task that deals with files using generics.
 */
public class GroovycTest extends GroovyTestCase {
    private final String classDirectory = "target/test-classes/org/codehaus/groovy/ant/vm5/";
    private final File antFile = new File("src/test/org/codehaus/groovy/ant/vm5/GroovycTest.xml");
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
        System.setOut(origOut);

        // verify if passing -Xlint in compilerarg had its effect
        Pattern p = Pattern.compile(".*?found[ ]*:[ ]*java.util.ArrayList.*", Pattern.DOTALL);
        assertTrue("Expected line 1 not found in ant output", p.matcher(antOutput).matches());
        p = Pattern.compile(".*?required[ ]*:[ ]*java.util.ArrayList<java.lang.String>.*", Pattern.DOTALL);
        assertTrue("Expected line 2 not found in ant output", p.matcher(antOutput).matches());
    }
}
