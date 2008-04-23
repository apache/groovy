/*
 * Copyright 2003-2008 the original author or authors.
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
 * Collecting all Groovy unit tests that are written in Groovy, not in root, and not Bug-related.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig
 * @version $Revision$
 */
public class UberTestCaseGroovySourceSubPackages extends TestCase {
    private static final String EXCLUDES = "groovy/**/vm5/*Test.groovy";
    private static final String BASE = "src/test";
    public static Test suite() {
        TestSuite suite = new TestSuite();
        String excludes = "true".equals(System.getProperty("java.awt.headless"))
                ? EXCLUDES + ",groovy/*/**/SwingBuilderTest.groovy" : EXCLUDES;
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/*Test.groovy", EXCLUDES));
        // temp hack to track down bamboo build issue
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/A*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/B*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/C*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/D*Test.groovy", excludes));
        // TODO remove comment once ExpandoMetaClassTest is fixed
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/E*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/F*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/G*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/H*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/I*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/J*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/K*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/L*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/M*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/N*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/O*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/P*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Q*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/R*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/S*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/T*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/U*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/V*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/W*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/X*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Y*Test.groovy", excludes));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Z*Test.groovy", excludes));
        return suite;
    }
}
