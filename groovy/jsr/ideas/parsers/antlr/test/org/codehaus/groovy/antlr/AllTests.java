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

        "ArrayTest.groovy",                     // TODO: arrays
        "ConstructorParameterBug.groovy",       // TODO: arrays
        "PropertyTest.groovy",                  // TODO: arrays
        "SubscriptTest.groovy",                 // TODO: arrays
        "GroovyMethodsTest.groovy",             // TODO: arrays
        "ConstructorBug.groovy",                // TODO: arrays
        "ClosureTest.groovy",                   // TODO: arrays
        "SubscriptOnPrimitiveTypeArrayBug.groovy",      // TODO: arrays

        "Groovy252_Bug.groovy",                 // TODO: surprising if parse error
        "bugs/GuillaumesBug.groovy",            // TODO: surprising if parse error

        "LeftShiftTest.groovy",                 // TODO: can't parse foo.bar << something
        "StreamingMarkupTest.groovy",           // TODO: can't parse foo.bar <<
        "BuilderSupportTest.groovy",            // TODO: can't parse foo.bar <<
        "bugs/ForAndSqlBug.groovy",             // TODO: can't parse foo.bar <<
        "PrintTest.groovy",                     // TODO: can't parse foo.bar <<
        "Groovy308_Bug.groovy",                 // TODO: can't parse foo.bar <<


        "EscapedUnicodeTest.groovy",            // TODO: parser unicode handling



        // TODO: Parser decisions to make
        "BigDecimalOperationTest.groovy",       // TODO: do we disallow .1 as a number?
        "DoWhileLoopTest.groovy",               // TODO: do { } while () not supported yet
        "LoopBreakTest.groovy",                 // TODO: do { } while () not supported yet
        "LiteralTypesTest.groovy",              // TODO: literal numbers in new parser...
        "IntegerOperationTest.groovy",          // TODO: what to do about integer divide?
        "NumberMathTest.groovy",                // TODO: what to do about integer divide?
        "SafeNavigationTest.groovy",            // TODO: should we support -> safe navigation token?


        // Ignored test cases which can't easily be run in this harness
        // due to classpath issues
        // (run them when we migrate New Groovy into groovy/groovy-core)
        //-------------------------------------------------------------------------
        "SqlCompleteTest.groovy",                   // not easy to run from in IDE...
        "SqlCompleteWithoutDataSourceTest.groovy",  // not easy to run from in IDE...
        "SqlWithBuilderTest.groovy",                // not easy to run from in IDE...
        "SqlWithTypedResultsTest.groovy",           // not easy to run from in IDE...
        "SerializeTest.groovy",                     // not easy to run from in IDE...
        "PrintlnWithNewBug.groovy",
        "dom/DOMTest.groovy",
        "SuperMethod2Bug.groovy",
        "SuperMethodBug.groovy",
    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        addTests(suite, new File("../../../../groovy-core/src/test-new/groovy"), excludedTests);
        return suite;
    }

}
