/*
 * Copyright 2003-2010 the original author or authors.
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

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Collects all TestCases in the Groovy test root that are written in Groovy.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig (refactored to use AllTestSuite)
 * @version $Revision$
 */
public class UberTestCaseGroovySourceRootPackage extends TestCase {
    public static Test suite() throws ClassNotFoundException {
        TestSuite suite = (TestSuite) AllTestSuite.suite("src/test", "groovy/*Test.groovy");

        String osName = System.getProperty("os.name");
        if (osName.equals("Linux") || osName.equals("SunOS") || osName.equals("Solaris") || osName.equals("Mac OS X")) {
            Class linuxTestClass = Class.forName("groovy.execute.ExecuteTest_LinuxSolaris");
            suite.addTestSuite(linuxTestClass);
        } else if (osName.startsWith("Windows ")) {
            Class windowsTestClass = Class.forName("groovy.execute.ExecuteTest_Windows");
            suite.addTestSuite(windowsTestClass);
        } else {
            System.err.println("No execute tests for operating system: " + osName + "!!!");
        }

        return suite;
    }
}

