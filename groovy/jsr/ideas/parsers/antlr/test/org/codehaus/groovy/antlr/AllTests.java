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
        "InvokeNormalMethodsFirstTest.groovy",  // TODO: closure params
        "MockTest.groovy",                      // TODO: closure params
        "RegularExpressionsTest.groovy",        // TODO: closure params
        "SqlTest.groovy",                       // TODO: closure params
        "AntTest.groovy",                       // TODO: closure params
        "BuilderSupportTest.groovy",            // TODO: closure params (and the use of the << operator)
        "VerboseDOMTest.groovy",                // TODO: closure params (explicit {|| to denote closure start)

        "Groovy278_Bug.groovy",                 // TODO: constructor bug
        "SuperMethod2Bug.groovy",               // TODO: constructor bug
        "SuperMethodBug.groovy",                // TODO: constructor bug
        "TestBase.groovy",                      // TODO: constructor bug
        "TestCaseBug.groovy",                   // TODO: constructor bug
        "TestDerived.groovy",                   // TODO: constructor bug
        "Build.groovy",                         // TODO: constructor bug
        "PrivateVariableAccessFromAnotherInstanceTest.groovy",      // TODO: constructor bug
        "SocketTest.groovy",                    // TODO: constructor bug

        "ClosureCloneTest.groovy",              // TODO: use of * with dot
        "DoubleOperationTest.groovy",           // TODO: use of * with dot
        "ClosureMethodTest.groovy",             // TODO: use of *
        "ClosureWithDefaultParamTest.groovy",   // TODO: use of *
        "GroovyMethodsTest.groovy",             // TODO: use of *
        "IntegerOperationTest.groovy",          // TODO: use of *
        "ListTest.groovy",                      // TODO: use of *
        "ModuloTest.groovy",                    // TODO: use of *
        "NumberMathTest.groovy",                // TODO: use of *
        "OverloadInvokeMethodTest.groovy",      // TODO: use of *
        "PrimitiveTypeFieldTest.groovy",        // TODO: use of *
        "ReturnTest.groovy",                    // TODO: use of *

        "ArrayTest.groovy",                     // TODO: arrays
        "ConstructorParameterBug.groovy",       // TODO: arrays
        "PropertyTest.groovy",                  // TODO: arrays
        "SubscriptTest.groovy",                 // TODO: arrays

        "BigDecimalOperationTest.groovy",       // TODO: parser bug with numeric operations/literals
        "LiteralTypesTest.groovy",              // TODO: literal numbers in new parser...

        "MapConstructionTest.groovy",           // TODO: map keys cannot be numeric literals
        "MapTest.groovy",                       // TODO: map keys cannot be numeric literals
        "PrintTest.groovy",                     // TODO: map keys cannot be numeric literals
        
        "TernaryOperatorTest.groovy",           // TODO: ternary operator not supported

        "ClassInNamedParamsBug.groovy",         // TODO: foo.class bug in parser
        "ConstructorBug.groovy",                // TODO: foo.class bug in parser
        "bugs/Groovy558_616_Bug.groovy",        // TODO: foo.class bug in parser
        "bugs/PrimitivePropertyBug.groovy",     // TODO: foo.class bug in parser
        "CastTest.groovy",                      // TODO: foo.class bug in parser
        "ClassExpressionTest.groovy",           // TODO: foo.class bug in parser
        "ClassTest.groovy",                     // TODO: foo.class bug in parser
        "SerializeTest.groovy",                 // TODO: foo.class bug in parser
        "dom/DOMTest.groovy",                   // TODO: markup(class:foo) bug in parser

        "DefVariableBug.groovy",                // TODO: can't use 'def' as named parameter or property

        "ClosureUsingOuterVariablesTest.groovy",    // TODO: can't handle [] as start of statement

        "DefaultParamTest.groovy",              // TODO: can't parse default parameter values

        "Groovy252_Bug.groovy",                 // TODO: surprising if parse error
        "bugs/GuillaumesBug.groovy",            // TODO: surprising if parse error

        "CompareToTest.groovy",                 // TODO: can't parse <=>
        "StreamingMarkupTest.groovy",           // TODO: can't parse <<

        "EscapedUnicodeTest.groovy",            // TODO: parser unicode handling

        "DoWhileLoopTest.groovy",               // TODO: do { } while () not supported yet
        "LoopBreakTest.groovy",                 // TODO: do { } while () not supported yet

        "MethodCallWithoutParenthesisTest.groovy",  // TODO: the last expression cannot be "a + b" currently return mandatory


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
        "ProcessTest.groovy",                   // TODO: is this a parser bug or just a not very good compiler error?
        "ClosureVariableBug.groovy",            // TODO: closure parser bug or AST bug
        "SafeNavigationTest.groovy",            // TODO: should we support -> safe navigation token?

        // TODO: AST bugs I think...
        "PrintlnWithNewBug.groovy",


        // Ignored test cases
        //-------------------------------------------------------------------------
        "SqlCompleteTest.groovy",                   // not easy to run from in IDE...
        "SqlCompleteWithoutDataSourceTest.groovy",  // not easy to run from in IDE...
        "SqlWithBuilderTest.groovy",                // not easy to run from in IDE...
        "SqlWithTypedResultsTest.groovy",           // not easy to run from in IDE...


    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, new File("../../../../groovy-core/src/test-new/groovy"), excludedTests);
        return suite;
    }

}
