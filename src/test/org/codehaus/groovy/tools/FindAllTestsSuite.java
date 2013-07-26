/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A TestSuite which will run a Groovy unit test case inside any Java IDE
 * either as a unit test case or as an application.
 * <p>
 * You can specify the GroovyUnitTest to run by running this class as an application
 * and specifying the script to run on the command line.
 * <p>
 * <code>
 * java groovy.util.GroovyTestSuite src/test/Foo.groovy
 * </code>
 * <p>
 * Or to run the test suite as a unit test suite in an IDE you can use
 * the 'test' system property to define the test script to run.
 * e.g. pass this into the JVM when the unit test plugin runs...
 * <p>
 * <code>
 * -Dtest=src/test/Foo.groovy
 * </code>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class FindAllTestsSuite extends TestSuite {

    protected static final String testDirectory = "target/test-classes";

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        FindAllTestsSuite suite = new FindAllTestsSuite();
        try {
            suite.loadTestSuite();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the test suite: " + e, e);
        }
        return suite;
    }

    public void loadTestSuite() throws Exception {
        recurseDirectory(new File(testDirectory));
    }

    protected void recurseDirectory(File dir) throws Exception {
        File[] files = dir.listFiles();
        List traverseList = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                traverseList.add(file);
            } else {
                String name = file.getName();
                if (name.endsWith("Test.class") || name.endsWith("Bug.class")) {
                    addTest(file);
                }
            }
        }
        for (Iterator iter = traverseList.iterator(); iter.hasNext();) {
            recurseDirectory((File) iter.next());
        }
    }

    protected void addTest(File file) throws Exception {
        String name = file.getPath();

        name = name.substring(testDirectory.length() + 1, name.length() - ".class".length());
        name = name.replace(File.separatorChar, '.');

        //System.out.println("Found: " + name);
        Class type = loadClass(name);
        addTestSuite(type);
    }

    protected Class loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e1) {
                return Class.forName(name);
            }
        }
    }
}
