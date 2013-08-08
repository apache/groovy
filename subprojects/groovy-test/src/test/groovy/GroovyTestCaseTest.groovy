/*
* Copyright 2003-2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Testing the notYetImplemented feature of GroovyTestCase.
 * TODO: testing all other features.
 * @author Dierk Koenig
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

    // we cannot test this automatically...
    // remove the leading x, run the test and see it failing
    void xtestSubclassFailing() {
        if (notYetImplemented()) return
        assert true // passes unexpectedly
    }
    void xtestStaticFailing() {
        if (GroovyTestCase.notYetImplemented(this)) return
        assert true // passes unexpectedly
    }

// ----------------

    void testShouldFailWithMessage() {
        def msg = shouldFail { throw new RuntimeException('x') }
        assertEquals 'x', msg
    }
    void testShouldFailWithMessageForClass() {
        def msg = shouldFail(RuntimeException.class) { throw new RuntimeException('x') }
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
