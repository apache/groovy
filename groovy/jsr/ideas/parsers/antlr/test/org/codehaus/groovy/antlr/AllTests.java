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

        "BigDecimalOperationTest.groovy",       // TODO: parser bug

        "ClassInNamedParamsBug.groovy",         // TODO: foo.class bug in parser
        "ConstructorBug.groovy",                // TODO: foo.class bug in parser
        "bugs/Groovy558_616_Bug.groovy",        // TODO: foo.class bug in parser
        "bugs/PrimitivePropertyBug.groovy",     // TODO: foo.class bug in parser

        "ClassGeneratorFixesTest.groovy",       // TODO: closure params
        "ClosureParameterPassingBug.groovy",    // TODO: closure params
        "ClosureTypedVariableBug.groovy",       // TODO: closure params
        "ClosureWithStaticVariablesBug.groovy", // TODO: closure params
        "bugs/Bytecode4Bug.groovy",             // TODO: closure params
        "bugs/ForAndSqlBug.groovy",             // TODO: closure params

        "DefVariableBug.groovy",                // TODO: can't use 'def' as named parameter or property

        "Groovy252_Bug.groovy",                 // TODO: surprising if parse error
        "bugs/GuillaumesBug.groovy",            // TODO: surprising if parse error

        "Groovy278_Bug.groovy",                 // TODO: constructor bug
        "Groovy308_Bug.groovy",                 // TODO: parser bug
        "NestedClosure2Bug.groovy",             // TODO: parser bug

        // Not sure of bug yet
        "ClosureVariableBug.groovy",            // TODO: closure parser bug or AST bug
        "DoubleSizeParametersBug.groovy",       // TODO: parser bug
        "InconsistentStackHeightBug.groovy",    // TODO: parser bug
        "MarkupAndMethodBug.groovy",            // TODO: parser bug
        "NestedClosureBug.groovy",              // TODO: parser bug
        "OverloadInvokeMethodBug.groovy",       // TODO: parser bug



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
