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

import groovy.ui.GroovyMain;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A simpler test runner to run multiple test cases straight from the groovy scripts
 *
 * @version $Revision$
 */
public class UberTests extends TestSuiteSupport {

    static String[] classicTests = {
        // "AmbiguousInvocationTest.groovy",  - requires classpath stuff
        "AssertNumberTest.groovy",
        "AssertTest.groovy",
        "Bar.groovy",
        "BindingTest.groovy",
        "BooleanOperationTest.groovy",
        //"CastTest.groovy",                    TODO parser bug: ${foo.class} in GStrings
        "ChainedAssignment.groovy",
        "ClassLoaderBug.groovy",
        //"ClosureCloneTest.groovy",            TODO parser bug: doesn't like 'it * foo' expressions
        "ClosureInClosureTest.groovy",
        "CompilerErrorTest.groovy",
        "DateTest.groovy",
        "Foo.groovy",
        "ForLoopTest.groovy",
        "ForLoopWithLocalVariablesTest.groovy",
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
        // "OptionalReturnTest.groovy",         TODO parser currently requires mandatory return for last exp = booleanExpr
        // "PrimitiveTypeFieldTest.groovy",     TODO when parser can do closure arguments
        "PostfixTest.groovy",
        "PrimitiveTypesTest.groovy",
        "PrefixTest.groovy",
        // "ReturnTest.groovy",                 TODO parser bug
        "RangeTest.groovy",
        "StringOperationTest.groovy",
        //"SubscriptTest.groovy",               TODO array initialisers
        "ThrowTest.groovy",
        "UnaryMinusTest.groovy",
        //"WhileLoopTest.groovy",               TODO parser bug
    };

    static String[] tckTests = {
        "misc/AnnotationTest.groovy",
        "misc/FieldPropertyMethodDisambiguationTest.groovy",
        "misc/PropertyCalledNameTest.groovy",
        "misc/PropertyTest.groovy",
        "misc/SampleTest.groovy",
    };


    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, "../../../../groovy-core/src/test-new/groovy/", classicTests);
        addTests(suite, "../../../tck/test/", tckTests);
        return suite;
    }

}
