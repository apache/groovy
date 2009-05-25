/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.transform.powerassert

// show that assertions in script body and script methods are transformed and work as expected

// for some reason, class AssertionTestUtil cannot be resolved from here, so we copy this method over
private static fails(Closure assertion) {
        try {
            assertion.call();
            junit.framework.Assert.fail("assertion should have failed but didn't")
        } catch (PowerAssertionError expected) {}
    }

assert true
fails { assert false }

x = 0
method()
assert x == 2

def method() {
    assert ++x == 1
    fails { assert ++x == 1 }
}
