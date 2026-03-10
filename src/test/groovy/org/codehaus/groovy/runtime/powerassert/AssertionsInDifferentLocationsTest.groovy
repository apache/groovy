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
package org.codehaus.groovy.runtime.powerassert

import org.junit.jupiter.api.Test

import static org.codehaus.groovy.runtime.powerassert.AssertionTestUtil.fails
import static org.junit.jupiter.api.Assertions.fail

/**
 * Defines assertions in different locations and checks if they are transformed.
 */
final class AssertionsInDifferentLocationsTest {

    @Test
    void testInConstructor() {
        fails {
            new AssertionInConstructor()
        }
    }

    @Test
    void testInConstructorAfterThisCall() {
        fails {
            new AssertionInConstructorAfterThisCall()
        }
    }

    @Test
    void testInConstructorAfterSuperCall() {
        fails {
            new AssertionInConstructorAfterSuperCall()
        }
    }

    @Test
    void testInInstanceInitializer() {
        fails {
            new AssertionInInstanceInitializer()
        }
    }

    @Test
    void testInClassInitializer() {
        fails {
            new AssertionInClassInitializer()
        }
    }

    @Test
    void testInEmbeddedScript() {
        try {
            new GroovyShell().evaluate("assert true; assert false")
            fail()
        } catch (PowerAssertionError e) {
        } catch (AssertionError expected) {
            fail()
        }
    }
}

@groovy.transform.PackageScope class AssertionInConstructor {
    def AssertionInConstructor() {
        assert true
        assert false
    }
}

@groovy.transform.PackageScope class AssertionInConstructorAfterThisCall {
    def AssertionInConstructorAfterThisCall() {
        this(true)
        assert true
        assert false
    }

    def AssertionInConstructorAfterThisCall(flag) {}
}

@groovy.transform.PackageScope class AssertionInConstructorAfterSuperCall {
    def AssertionInConstructorAfterSuperCall() {
        super()
        assert true
        assert false
    }
}

@groovy.transform.PackageScope class AssertionInInstanceInitializer {
    {
        assert true
        assert false
    }
}

@groovy.transform.PackageScope class AssertionInClassInitializer {
    static {
        assert true
        assert false
    }
}
