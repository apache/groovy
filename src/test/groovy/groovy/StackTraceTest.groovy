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

/**
 * This test case is added to ensure an exception thrown from inside
 * groovy does always contain a valid line number and file name for
 * the script method the exception is thrown from.
 *
 * See also GROOVY-726
 */
class StackTraceTest extends GroovyTestCase {


    public void testTrace() {
        def className = this.class.name
        def assertDone = false
        try {
            throw new Exception("e")
        } catch (Exception e) {
            assert e.message == "e"
            def trace = e.stackTrace
            trace.each {
                if (it.className == className && it.methodName == "testTrace") {
                    assert it.lineNumber > 0
                    assert it.fileName != null
                    assert it.fileName.length() > 0
                    assertDone = true
                }
            }
        }
        assert assertDone
    }


    public void testMissingProperty() {
        def assertDone = false
        def script = new GroovyShell().parse('println(unknownProp)', 'testMissingProperty.tst')
        try {
            script.run()
        } catch (MissingPropertyException mpe) {
            assert mpe.message.indexOf('unknownProp') > 0
            def found = mpe.stackTrace.find {
                it.lineNumber == 1 &&
                        it.fileName == 'testMissingProperty.tst'
            }
            assertDone = found != null
        }
        assert assertDone
    }
}