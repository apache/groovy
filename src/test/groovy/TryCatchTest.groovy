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

import gls.CompilableTestSupport

class TryCatchTest extends CompilableTestSupport {

    def exceptionCalled
    def finallyCalled

    void testTryCatch() {
        try {
            failingMethod()
        }
        catch (AssertionError e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        afterTryCatch()
        assert exceptionCalled, "should have invoked the catch clause"
        assert finallyCalled, "should have invoked the finally clause"
    }

    void testStandaloneTryBlockShouldNotCompile() {
        shouldNotCompile """
            try {
                assert true
            }
        """
    }

    void testTryFinally() {
        Boolean touched = false;

        try {
        }
        finally {
            touched = true;
        }

        assert touched, "finally not called with empty try"
    }

    void testWorkingMethod() {
        try {
            workingMethod()
        }
        catch (AssertionError e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert !exceptionCalled, "should not invoked the catch clause"
        assert finallyCalled, "should have invoked the finally clause"
    }

    void failingMethod() {
        assert false, "Failing on purpose"
    }

    void workingMethod() {
        assert true, "Should never fail"
    }

    void onException(e) {
        assert e != null
        exceptionCalled = true
    }

    void onFinally() {
        finallyCalled = true
    }

    void afterTryCatch() {
        assert exceptionCalled, "should have invoked the catch clause"
        assert finallyCalled, "should have invoked the finally clause"
    }

    protected void setUp() {
        exceptionCalled = false
        finallyCalled = false
    }

    void testTryWithReturnWithPrimitiveTypes() {
        assert intTry() == 1
        assert longTry() == 2
        assert byteTry() == 3
        assert shortTry() == 4
        assert charTry() == "c"
        assert floatTry() == 1.0
        assert doubleTry() == 2.0
    }

    int intTry() {
        try {
            return 1
        } finally {}
    }

    long longTry() {
        try {
            return 2
        } finally {}
    }

    byte byteTry() {
        try {
            return 3
        } finally {}
    }

    short shortTry() {
        try {
            return 4
        } finally {}
    }

    char charTry() {
        try {
            return 'c'
        } finally {}
    }

    float floatTry() {
        try {
            return 1.0
        } finally {}
    }

    double doubleTry() {
        try {
            return 2.0
        } finally {}
    }

    void testTryCatchWithUntyped() {
        try {
            throw new Exception();
        } catch (e) {
            assert true
            return
        }
        assert false
    }

    void testTryCatchInConstructor() {
        // the super() call construction left an
        // element on the stack, causing an inconsistent
        // stack height problem for the try-catch
        // this ensures the stack is clean after the call
        assertScript """
            class A {
                A() {
                    super()
                    try{}catch(e){}
                }
            }
            assert null != new A()
        """
    }
}
