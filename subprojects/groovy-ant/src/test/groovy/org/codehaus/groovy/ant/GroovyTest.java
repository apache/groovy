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

import groovy.lang.GroovyRuntimeException;
import groovy.util.GroovyTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Unit tests for the {@link Groovy} ant task.
 * Caution: the *.groovy files used by this test should not get compiled with the rest of the
 * test classes compilation process otherwise they would be available in the classpath
 * and the tests here would be meaningless (tested by testClasspath_missing).
 */
public class GroovyTest extends GroovyTestCase {
    public static String FLAG = null;
    private final File antFile = new File("src/test-resources/org/codehaus/groovy/ant/GroovyTest.xml");
    private Project project;

    public GroovyTest(final String name) {
        setName(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new GroovyTest("testGroovyCodeWithinTag"));
        suite.addTest(new GroovyTest("testGroovyCodeExternalFile"));
        suite.addTest(new GroovyTest("testGroovyCodeInExternalFileWithOtherClass"));
        suite.addTest(new GroovyTest("testPropertiesWithoutFork"));
        suite.addTest(new GroovyTest("testClasspath_missing"));
        suite.addTest(new GroovyTest("testClasspath_classpathAttribute"));
        suite.addTest(new GroovyTest("testClasspath_classpathrefAttribute"));
        suite.addTest(new GroovyTest("testClasspath_nestedclasspath"));
        suite.addTest(new GroovyTest("testGroovyArgUsage"));
        suite.addTest(new GroovyTest("testFileNameInStackTrace"));
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
        project = new Project();
        project.init();
        ProjectHelper.getProjectHelper().parse(project, antFile);
        FLAG = null;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGroovyCodeWithinTag() {
        assertNull(FLAG);
        project.executeTarget("groovyCodeWithinTask");
        assertEquals("from groovy inlined in ant", FLAG);
    }

    public void testGroovyCodeExternalFile() {
        assertNull(FLAG);
        project.executeTarget("groovyCodeInExternalFile");
        assertEquals("from groovy file called from ant", FLAG);
    }

    public void testPropertiesWithoutFork() {
        assertNull(FLAG);
        project.executeTarget("groovyAntPropertyWithoutFork");
        assertEquals("someProp was someValue", FLAG);
    }

    public void testGroovyCodeInExternalFileWithOtherClass() {
        assertNull(FLAG);
        project.executeTarget("groovyCodeInExternalFileWithOtherClass");
        assertEquals("from GroovyTest2Class.doSomething()", FLAG);
    }

    public void testClasspath_missing() {
        try {
            project.executeTarget("groovyClasspath_missing");
            fail();
        } catch (final Exception e) {
            assertEquals(BuildException.class, e.getClass());
        }
    }

    public void testClasspath_classpathAttribute() {
        assertNull(FLAG);
        project.executeTarget("groovyClasspath_classpathAttribute");
        assertEquals("from groovytest3.GroovyTest3Class.doSomething()", FLAG);
    }

    public void testClasspath_classpathrefAttribute() {
        assertNull(FLAG);
        project.executeTarget("groovyClasspath_classpathrefAttribute");
        assertEquals("from groovytest3.GroovyTest3Class.doSomething()", FLAG);
    }

    public void testClasspath_nestedclasspath() {
        assertNull(FLAG);
        project.executeTarget("groovyClasspath_nestedClasspath");
        assertEquals("from groovytest3.GroovyTest3Class.doSomething()", FLAG);
    }

    public void testGroovyArgUsage() {
        assertNull(FLAG);
        project.executeTarget("groovyArgUsage");
        assertEquals("from groovytest3.GroovyTest3Class.doSomethingWithArgs() 1 2 3", FLAG);
    }

    /**
     * Test that helpful "file name" appears in the stack trace and not just "Script1" 
     */
    public void testFileNameInStackTrace() {
        testFileNameInStackTrace("groovyErrorMsg", "\\(embedded_script_in_.*GroovyTest_dot_xml");
        testFileNameInStackTrace("groovyErrorMsg_ExternalFile", "GroovyTest_errorMessage.groovy");
    }

    private void testFileNameInStackTrace(final String target, final String fileNamePattern) {
        try {
            project.executeTarget(target);
            fail();
        }
        catch (final BuildException e) {
            assertEquals(BuildException.class, e.getClass());
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof GroovyRuntimeException);

            final Writer sw = new StringBuilderWriter();
            cause.printStackTrace(new PrintWriter(sw));
            
            final String stackTrace = sw.toString();
            final Pattern pattern = Pattern.compile(fileNamePattern);
            assertTrue("Does >" + stackTrace + "< contain >" + fileNamePattern + "<?", 
                    pattern.matcher(stackTrace).find());
        }
    }
}
