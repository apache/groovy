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
package groovy

import groovy.test.GroovyTestCase

class AssertTest extends GroovyTestCase {

    void testAssert() {
        def x = null

        assert x == null
        assert x != "abc"
        assert x != "foo"

        x = "abc"

        assert x != "foo"
        assert x != null
        assert x != "def"
        assert x == "abc"

        assert x.equals("abc")

        assert !x.equals("def")
        assert !false
        assert !(1 == 2)
        assert !(1 > 3)
        assert !(1 != 1)
    }

    void testAssertFail() {
        def x = 1234

        def runCode = false
        try {
            runCode = true
            assert x == 5

            fail("Should have thrown an exception")
        }
        catch (AssertionError e) {
            //msg = "Expression: (x == 5). Values: x = 1234"
            //assert e.getMessage() == msg
            //assert e.message == msg
        }
        assert runCode, "has not ran the try / catch block code"
    }

    void testAssertMessageAndExpressionReversed() {
        try {
            // the order is wrong for this one ""=="" becomes
            // the assertion message string. Since it is an
            // expression it must made sure the expression is
            // transformed into an object
            assert "": "" == ""
        } catch (AssertionError ae) {
            assert ae.message.contains("true");
        }
    }

    // GROOVY-1769
    void testAssertWithNewlineAfterColonOrComma() {
        assert true:
            "Assert on newline after colon"

        assert true,
            "Assert on newline after comma"
    }
}
