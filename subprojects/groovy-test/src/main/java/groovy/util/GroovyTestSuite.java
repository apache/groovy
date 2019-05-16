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
package groovy.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.groovy.test.ScriptTestAdapter;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * A TestSuite which will run a Groovy unit test case inside any Java IDE
 * either as a unit test case or as an application.
 * <p>
 * You can specify the GroovyUnitTest to run by running this class as an application
 * and specifying the script to run on the command line.
 * <code>
 * java groovy.util.GroovyTestSuite src/test/Foo.groovy
 * </code>
 * Or to run the test suite as a unit test suite in an IDE you can use
 * the 'test' system property to define the test script to run.
 * e.g.&#160;pass this into the JVM when the unit test plugin runs...
 * <code>
 * -Dtest=src/test/Foo.groovy
 * </code>
 */
public class GroovyTestSuite extends TestSuite {

    protected static String file = null;

    protected final GroovyClassLoader loader =
            AccessController.doPrivileged(
                    new PrivilegedAction<GroovyClassLoader>() {
                        @Override
                        public GroovyClassLoader run() {
                            return new GroovyClassLoader(GroovyTestSuite.class.getClassLoader());
                        }
                    }
            );

    public static void main(String[] args) {
        if (args.length > 0) {
            file = args[0];
        }
        TestRunner.run(suite());
    }

    public static Test suite() {
        GroovyTestSuite suite = new GroovyTestSuite();
        try {
            suite.loadTestSuite();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the test suite: " + e, e);
        }
        return suite;
    }

    public void loadTestSuite() throws Exception {
        String fileName = System.getProperty("test", file);
        if (fileName == null) {
            throw new RuntimeException("No filename given in the 'test' system property so cannot run a Groovy unit test");
        }
        System.out.println("Compiling: " + fileName);
        Class type = compile(fileName);
        String[] args = {};
        if (!Test.class.isAssignableFrom(type) && Script.class.isAssignableFrom(type)) {
            // let's treat the script as a Test
            addTest(new ScriptTestAdapter(type, args));
        } else {
            addTestSuite(type);
        }
    }

    public Class compile(String fileName) throws Exception {
        return loader.parseClass(new File(fileName));
    }
}
