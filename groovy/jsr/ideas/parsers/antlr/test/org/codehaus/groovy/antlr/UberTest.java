/**
 *
 * Copyright 2004 James Strachan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.antlr;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestCase;
import groovy.ui.GroovyMain;

import java.io.File;
import java.io.IOException;

/**
 * A simpler test runner to run multiple test cases straight from the groovy scripts
 *
 * @version $Revision$
 */
public class UberTest implements Test {

    static String[] classicTests = {
        // "AmbiguousInvocationTest.groovy",  - requires classpath stuff
        "AssertNumberTest.groovy",
        "AssertTest.groovy",
        "Bar.groovy",
        "BindingTest.groovy",
        "ChainedAssignment.groovy",
        "ClosureInClosureTest.groovy",
        "Foo.groovy",
        //"ForLoopTest.groovy",                 TODO - fixme - AST gen bug
         "GStringTest.groovy",
        "HeredocsTest.groovy",
        "IfElseCompactTest.groovy",
        "IfElseTest.groovy",
        "IfTest.groovy",
        "ImportTest.groovy",
        "InstanceofTest.groovy",
         //"ListTest.groovy",                   TODO parser bugs
        "LogicTest.groovy",
        "MethodCallTest.groovy",
        "MultilineStringTest.groovy",
        "MultiplyDivideEqualsTest.groovy",
        "NegationTests.groovy",
        // "PrimitiveTypeFieldTest.groovy",     TODO when parser can do closure arguments
        "PrimitiveTypesTest.groovy",
        // "ReturnTest.groovy",                 TODO parser bug
        "StringOperationTest.groovy",
        //"SubscriptTest.groovy",               TODO array initialisers
        "ThrowTest.groovy",
        //"UnaryMinusTest.groovy",              TODO needs classpath stuff
        //"WhileLoopTest.groovy",               TODO parser bug
    };

    static String[] tckTests = {
        "misc/AnnotationTest.groovy",
        "misc/FieldPropertyMethodDisambiguationTest.groovy",
        "misc/PropertyCalledNameTest.groovy",
        "misc/PropertyTest.groovy",
        "misc/SampleTest.groovy",
    };

    private String fullName;


    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, "../../../../groovy-core/src/test-new/groovy/", classicTests);
        addTests(suite, "../../../tck/test/", tckTests);
        return suite;
    }

    protected static void addTests(TestSuite suite, String root, String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String fullName = root + name;

            UberTest uberTest = new UberTest();
            uberTest.fullName = fullName;
            TestSuite childSuite = new TestSuite(fullName);
            childSuite.addTest(uberTest);
            suite.addTest(childSuite);
        }
    }

    public int countTestCases() {
        return 1;
    }

    public void run(TestResult testResult) {
        System.out.println("Running test: " + fullName);
        GroovyMain.main(new String[] { fullName } );
    }
}
