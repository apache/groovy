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

import java.io.File;

/**
 * A test suite which recurses through all of the available test case in
 * the test-new area
 *
 * @version $Revision$
 */
public class AllTests extends TestSuiteSupport {

    static String[] excludedTests = {

        // TODO the following are all parser bugs
        "ArrayTest.groovy",                     // TODO: arrays
        "ConstructorParameterBug.groovy",       // TODO: arrays

        "BigDecimalOperationTest.groovy",       // TODO: parser bug with numeric operations/literals

        "ClassInNamedParamsBug.groovy",         // TODO: foo.class bug in parser
        "ConstructorBug.groovy",                // TODO: foo.class bug in parser
        "bugs/Groovy558_616_Bug.groovy",        // TODO: foo.class bug in parser
        "bugs/PrimitivePropertyBug.groovy",     // TODO: foo.class bug in parser
        "CastTest.groovy",                      // TODO: foo.class bug in parser
        "ClassExpressionTest.groovy",           // TODO: foo.class bug in parser
        "ClassTest.groovy",                     // TODO: foo.class bug in parser

        "ClassGeneratorFixesTest.groovy",       // TODO: closure params
        "ClosureParameterPassingBug.groovy",    // TODO: closure params
        "ClosureTypedVariableBug.groovy",       // TODO: closure params
        "ClosureWithStaticVariablesBug.groovy", // TODO: closure params
        "bugs/Bytecode4Bug.groovy",             // TODO: closure params
        "bugs/ForAndSqlBug.groovy",             // TODO: closure params
        "bugs/VariblePrecedence.groovy",        // TODO: closure params
        "ClosureAsParamTest.groovy",            // TODO: closure params
        "ClosureComparatorTest.groovy",         // TODO: closure params
        "ClosureCurryTest.groovy",              // TODO: closure params
        "ClosureMethodCallTest.groovy",         // TODO: closure params
        "ClosureReturnTest.groovy",             // TODO: closure params
        "ClosureReturnWithoutReturnStatementTest.groovy",             // TODO: closure params
        "ClosureSugarTest.groovy",              // TODO: closure params
        "ClosureTest.groovy",                   // TODO: closure params
        "ExpandoPropertyTest.groovy",           // TODO: closure params
        "GeneratorTest.groovy",                 // TODO: closure params

        "DefVariableBug.groovy",                // TODO: can't use 'def' as named parameter or property

        "Groovy252_Bug.groovy",                 // TODO: surprising if parse error
        "bugs/GuillaumesBug.groovy",            // TODO: surprising if parse error

        "Groovy278_Bug.groovy",                 // TODO: constructor bug
        "SuperMethod2Bug.groovy",               // TODO: constructor bug
        "SuperMethodBug.groovy",                // TODO: constructor bug
        "TestBase.groovy",                      // TODO: constructor bug
        "TestCaseBug.groovy",                   // TODO: constructor bug
        "TestDerived.groovy",                   // TODO: constructor bug

        "ClosureCloneTest.groovy",              // TODO: use of * with dot
        "DoubleOperationTest.groovy",           // TODO: use of * with dot
        "ClosureMethodTest.groovy",             // TODO: use of *
        "ClosureWithDefaultParamTest.groovy",   // TODO: use of *

        "CompareToTest.groovy",                 // TODO: can't parse <=>

        "StaticClosurePropertyBug.groovy",      // TODO: static/def
        "UseStaticInClosureBug.groovy",         // TODO: static/def

        "ClosureUsingOuterVariablesTest.groovy",    // TODO: can't handle [] as start of statement

        "DefaultParamTest.groovy",              // TODO: can't parse default parameter values

        "EscapedUnicodeTest.groovy",            // TODO: parser unicode handling

        "Groovy308_Bug.groovy",                 // TODO: parser bug
        "NestedClosure2Bug.groovy",             // TODO: parser bug

        "DoubleSizeParametersBug.groovy",       // TODO: parser bug
        "InconsistentStackHeightBug.groovy",    // TODO: parser bug
        "MarkupAndMethodBug.groovy",            // TODO: parser bug
        "NestedClosureBug.groovy",              // TODO: parser bug
        "OverloadInvokeMethodBug.groovy",       // TODO: parser bug
        "RodsBooleanBug.groovy",                // TODO: parser bug
        "SubscriptOnPrimitiveTypeArrayBug.groovy",      // TODO: parser bug


        // Not sure of bug yet
        "ClosureVariableBug.groovy",            // TODO: closure parser bug or AST bug

        // TODO: AST bugs I think...
        "PrintlnWithNewBug.groovy",



        // lets ignore the benchmark tests as they just slow down unit testing
        "benchmarks/createLoop.groovy",
        "benchmarks/loop.groovy",
        "benchmarks/loop2.groovy",
    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, new File("../../../../groovy-core/src/test-new/groovy"), excludedTests);
        return suite;
    }

}
