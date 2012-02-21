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
package groovy.test

import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail
import static groovy.test.GroovyAssert.shouldFailWithCause

class GroovyAssertTest {

    @Test
    void shouldFailAndReturnException() {
        def re = shouldFail { throw new RuntimeException('x') }
        assert re?.message == 'x'
        assert re instanceof RuntimeException
    }

    @Test
    void shouldFailCheckExceptionClassAndReturnException() {
        def re = shouldFail(RuntimeException) { throw new RuntimeException('x') }
        assert re?.message == 'x'
        assert re instanceof RuntimeException
    }

    @Test
    void shouldFailCheckingCustomException() {
        shouldFail(GroovyAssertDummyException) {
            GroovyAssertDummyClass.throwException()
        }
    }

    @Test
    void shouldFailWithNestedException() {
        def throwable = shouldFail(GroovyAssertDummyException) {
            new GroovyAssertDummyClass().throwExceptionWithCause()
        }
        assert throwable instanceof GroovyAssertDummyException
        assert throwable.cause instanceof NullPointerException

        throwable = shouldFailWithCause(NullPointerException) {
            new GroovyAssertDummyClass().throwExceptionWithCause()
        }
        assert throwable instanceof NullPointerException
    }
}

@groovy.transform.PackageScope class GroovyAssertDummyClass {
    static throwException() {
        throw new GroovyAssertDummyException()
    }

    def throwExceptionWithCause() {
        throw new GroovyAssertDummyException(new NullPointerException())
    }
}

@groovy.transform.PackageScope class GroovyAssertDummyException extends RuntimeException {
    GroovyAssertDummyException(Throwable cause) {
        super(cause);
    }

    GroovyAssertDummyException() {
        super();
    }
}
