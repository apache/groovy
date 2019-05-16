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
package groovy.test;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.IFileNameFinder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.codehaus.groovy.control.CompilationFailedException;
import org.apache.groovy.test.ScriptTestAdapter;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Logger;

/**
 * AllTestSuite can be used in extension of GroovyTestSuite to execute TestCases written in Groovy
 * from inside a Java IDE.
 * AllTestSuite collects all files below a given directory that comply to a given pattern.
 * From these files, a TestSuite is constructed that can be run via an IDE graphical Test runner.
 * The files are assumed to be Groovy source files and be either a TestCase or a Script that can
 * be wrapped transparently into a TestCase.
 * The directory and the pattern can be set via System properties (see this classes' constants for details.)
 * <p>
 * When setting the log level of this class to FINEST, all file loading will be logged.
 * <p>
 * See also groovy.util.AllTestSuiteTest.groovy
 */
public class AllTestSuite extends TestSuite {

    /**
     * The System Property to set as base directory for collection of Test Cases.
     * The pattern will be used as an Ant fileset include basedir.
     * Key is "groovy.test.dir".
     * Default value is "./test/".
     */
    public static final String SYSPROP_TEST_DIR = "groovy.test.dir";

    /**
     * The System Property to set as the filename pattern for collection of Test Cases.
     * The pattern will be used as Regular Expression pattern applied with the find
     * operator against each candidate file.path.
     * Key is "groovy.test.pattern".
     * Default value is "Test.groovy".
     */
    public static final String SYSPROP_TEST_PATTERN = "groovy.test.pattern";

    /**
     * The System Property to set as a filename excludes pattern for collection of Test Cases.
     * When non-empty, the pattern will be used as Regular Expression pattern applied with the
     * find operator against each candidate file.path.
     * Key is "groovy.test.excludesPattern".
     * Default value is "".
     */
    public static final String SYSPROP_TEST_EXCLUDES_PATTERN = "groovy.test.excludesPattern";

    private static final Logger LOG = Logger.getLogger(AllTestSuite.class.getName());
    private static final ClassLoader JAVA_LOADER = AllTestSuite.class.getClassLoader();
    private static final GroovyClassLoader GROOVY_LOADER =
            AccessController.doPrivileged(
                    new PrivilegedAction<GroovyClassLoader>() {
                        @Override
                        public GroovyClassLoader run() {
                            return new GroovyClassLoader(JAVA_LOADER);
                        }
                    }
            );

    private static final String[] EMPTY_ARGS = new String[]{};
    private static IFileNameFinder finder = null;

    static { // this is only needed since the Groovy Build compiles *.groovy files after *.java files
        try {
            // TODO: dk: make FileNameFinder injectable
            finder = (IFileNameFinder) Class.forName("groovy.ant.FileNameFinder").getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find and instantiate class FileNameFinder", e);
        }
    }

    public static Test suite() {
        String basedir = System.getProperty(SYSPROP_TEST_DIR, "./test/");
        String pattern = System.getProperty(SYSPROP_TEST_PATTERN, "**/*Test.groovy");
        String excludesPattern = System.getProperty(SYSPROP_TEST_EXCLUDES_PATTERN, "");
        return suite(basedir, pattern, excludesPattern);
    }

    public static Test suite(String basedir, String pattern) {
        return suite(basedir, pattern, "");
    }

    public static Test suite(String basedir, String pattern, String excludesPattern) {
        AllTestSuite suite = new AllTestSuite();
        List<String> filenames = excludesPattern.length() > 0
                ? finder.getFileNames(basedir, pattern, excludesPattern)
                : finder.getFileNames(basedir, pattern);
        for (String filename : filenames) {
            LOG.finest("trying to load " + filename);
            try {
                suite.loadTest(filename);
            } catch (CompilationFailedException cfe) {
                cfe.printStackTrace();
                throw new RuntimeException("CompilationFailedException when loading " + filename, cfe);
            } catch (IOException ioe) {
                throw new RuntimeException("IOException when loading " + filename, ioe);
            }
        }
        return suite;
    }

    @SuppressWarnings("unchecked")
    protected void loadTest(String filename) throws CompilationFailedException, IOException {
        Class type = compile(filename);
        if (TestCase.class.isAssignableFrom(type)) {
            addTestSuite((Class<? extends TestCase>)type);
        } else if (Script.class.isAssignableFrom(type)) {
            addTest(new ScriptTestAdapter(type, EMPTY_ARGS));
        } else {
            throw new RuntimeException("Don't know how to treat " + filename + " as a JUnit test");
        }
    }

    protected Class compile(String filename) throws CompilationFailedException, IOException {
        return GROOVY_LOADER.parseClass(new File(filename));
    }
}
