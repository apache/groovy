/*
 * Copyright 2009 the original author or authors.
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

import static org.codehaus.groovy.transform.powerassert.AssertionTestUtil.*

/**
 * Defines assertions in different locations and checks if they are transformed.
 *
 * @author Peter Niederwieser
 */
class AssertionsInDifferentLocationsTest extends GroovyTestCase {
    void testInConstructor() {
        fails {
            new AssertionInConstructor()
        }
    }

    void testInConstructorAfterThisCall() {
        fails {
            new AssertionInConstructorAfterThisCall()
        }
    }

    void testInConstructorAfterSuperCall() {
        fails {
            new AssertionInConstructorAfterSuperCall()
        }
    }

    void testInInstanceInitializer() {
        fails {
            new AssertionInInstanceInitializer()
        }
    }

    void testInClassInitializer() {
        fails {
            new AssertionInClassInitializer()
        }
    }

    // not yet transformed because transforms don't currently have access to string-based source code
    void testInEmbeddedScript() {
        try {
            new GroovyShell().evaluate("assert true; assert false")
            fail()
        } catch (PowerAssertionError e) {
            fail()
        } catch (AssertionError expected) {}
    }
}

private class AssertionInConstructor {
    def AssertionInConstructor() {
        assert true
        assert false
    }
}

private class AssertionInConstructorAfterThisCall {
    def AssertionInConstructorAfterThisCall() {
        this(true)
        assert true
        assert false
    }

    def AssertionInConstructorAfterThisCall(flag) {}
}

private class AssertionInConstructorAfterSuperCall {
    def AssertionInConstructorAfterSuperCall() {
        super()
        assert true
        assert false
    }
}

private class AssertionInInstanceInitializer {
    {
        assert true
        assert false
    }
}

private class AssertionInClassInitializer {
    static {
        assert true
        assert false
    }
}
