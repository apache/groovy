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
import junit.framework.AssertionFailedError

/**
 * Testing the notYetImplemented feature of GroovyTestCase.
 */
class GroovyTestCaseTest extends GroovyTestCase {

    void testNotYetImplementedSubclassUse () {
        if (notYetImplemented()) return
        fail 'here the code that is expected to fail'
    }

    void testNotYetImplementedStaticUse () {
        if (GroovyTestCase.notYetImplemented(this)) return
        fail 'here the code that is expected to fail'
    }

    void testSubclassFailing() {
        try{ if (notYetImplemented()) return}
        catch (AssertionFailedError expected){
        }
        fail 'Expected AssertionFailedError was not thrown.'
    }

    void testStaticFailing() {
        try{ if (GroovyTestCase.notYetImplemented(this)) return}
        catch (AssertionFailedError expected){
        }
        fail 'Expected AssertionFailedError was not thrown.'
    }

    void testShouldFailWithMessage() {
        def msg = shouldFail { throw new RuntimeException('x') }
        assertEquals 'x', msg
    }
    void testShouldFailWithMessageForClass() {
        def msg = shouldFail(RuntimeException) { throw new RuntimeException('x') }
        println msg
        assertEquals 'x', msg
    }

    void testShouldFail() {
        shouldFail(MyException) {
            new Foo().createBar()
        }
    }

    void testShouldFailWithNestedException() {
        shouldFail(MyException) {
            new Foo().createBarWithNestedException()
        }
    }

    void testShouldFailWithCauseMessageWhenUsedIncorrectly() {
        def msg = shouldFail(AssertionError) {
            shouldFailWithCause(Exception) {
                throw new Exception()
            }
        }
        assert msg.contains("was expected to fail due to a nested cause of type java.lang.Exception but instead got a direct exception of type java.lang.Exception with no nested cause(s).")
        assert msg.contains("Code under test has a bug or perhaps you meant shouldFail?")
    }
}

class Foo {
    def createBar() {
        throw new MyException(null)
    }

    def createBarWithNestedException() {
        throw new MyException(new NullPointerException())
    }
}

class MyException extends RuntimeException {
    MyException(Throwable cause) {
        super((Throwable) cause);
    }
}
