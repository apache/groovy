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

        "VerboseDOMTest.groovy",                // TODO: closure params (explicit {|| to denote closure start)

        "ClosureTest.groovy",                   // TODO: closure params - doesn't seem to like typed parameters


        "ClosureComparatorTest.groovy",         // TODO: cannot pass closure into new expression

        "Groovy278_Bug.groovy",                 // TODO: constructor bug
        "SuperMethod2Bug.groovy",               // TODO: constructor bug
        "SuperMethodBug.groovy",                // TODO: constructor bug
        "TestBase.groovy",                      // TODO: constructor bug
        "TestCaseBug.groovy",                   // TODO: constructor bug
        "TestDerived.groovy",                   // TODO: constructor bug
        "Build.groovy",                         // TODO: constructor bug
        "PrivateVariableAccessFromAnotherInstanceTest.groovy",      // TODO: constructor bug
        "SocketTest.groovy",                    // TODO: constructor bug

        "ArrayTest.groovy",                     // TODO: arrays
        "ConstructorParameterBug.groovy",       // TODO: arrays
        "PropertyTest.groovy",                  // TODO: arrays
        "SubscriptTest.groovy",                 // TODO: arrays
        "GroovyMethodsTest.groovy",             // TODO: arrays
        "ConstructorBug.groovy",                // TODO: arrays

        "BigDecimalOperationTest.groovy",       // TODO: parser bug with numeric operations/literals
        "LiteralTypesTest.groovy",              // TODO: literal numbers in new parser...

        "TernaryOperatorTest.groovy",           // TODO: ternary operator not supported

        "ClosureUsingOuterVariablesTest.groovy",    // TODO: can't handle [] as start of statement

        "Groovy252_Bug.groovy",                 // TODO: surprising if parse error
        "bugs/GuillaumesBug.groovy",            // TODO: surprising if parse error

        "LeftShiftTest.groovy",                 // TODO: can't parse foo.bar << something
        "StreamingMarkupTest.groovy",           // TODO: can't parse foo.bar <<
        "BuilderSupportTest.groovy",            // TODO: can't parse foo.bar <<
        "bugs/ForAndSqlBug.groovy",             // TODO: can't parse foo.bar <<
        "PrintTest.groovy",                     // TODO: can't parse foo.bar <<

        "RegularExpressionsTest.groovy",        // TODO: regex issue

        "EscapedUnicodeTest.groovy",            // TODO: parser unicode handling

        "DoWhileLoopTest.groovy",               // TODO: do { } while () not supported yet
        "LoopBreakTest.groovy",                 // TODO: do { } while () not supported yet

        "MethodCallWithoutParenthesisTest.groovy",  // TODO: the last expression cannot be "a + b" currently return mandatory

        "IntegerOperationTest.groovy",          // TODO: what to do about integer divide?
        "NumberMathTest.groovy",                // TODO: what to do about integer divide?

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
        "InvokeNormalMethodsFirstTest.groovy",  // TODO: not sure

        "ProcessTest.groovy",                   // TODO: is this a parser bug or just a not very good compiler error?
        "ClosureVariableBug.groovy",            // TODO: closure parser bug or AST bug
        "SafeNavigationTest.groovy",            // TODO: should we support -> safe navigation token?



        // TODO: AST bugs I think...
        "ClassGeneratorFixesTest.groovy",       // TODO: some kinda return bug... maybe AST?
        "ReturnTest.groovy",
        "PrimitiveTypeFieldTest.groovy",
        "PrintlnWithNewBug.groovy",
        "AntTest.groovy",
        "dom/DOMTest.groovy",


        // Ignored test cases
        //-------------------------------------------------------------------------
        "SqlCompleteTest.groovy",                   // not easy to run from in IDE...
        "SqlCompleteWithoutDataSourceTest.groovy",  // not easy to run from in IDE...
        "SqlWithBuilderTest.groovy",                // not easy to run from in IDE...
        "SqlWithTypedResultsTest.groovy",           // not easy to run from in IDE...
        "SerializeTest.groovy",                     // not easy to run from in IDE...


    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, new File("../../../../groovy-core/src/test-new/groovy"), excludedTests);
        return suite;
    }

}
