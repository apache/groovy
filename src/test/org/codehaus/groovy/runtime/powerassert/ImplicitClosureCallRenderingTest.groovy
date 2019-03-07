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

import static AssertionTestUtil.*

/**
 * Tests rendering of assertions that contain a closure call
 * with the implicit "foo(args)" syntax instead of the explicit
 * "foo.call(args)" syntax. See GROOVY-4344.
 */
class ImplicitClosureCallRenderingTest extends GroovyTestCase {
    void testWithLocalVariable() {
        isRendered """
assert func(42) == null
       |        |
       42       false
        """, {
            def func = { it }
            assert func(42) == null
        }
    }

    def assertFunc(func) {
        assert func(42) == null
    }

    void testWithMethodArgument() {
        isRendered """
assert func(42) == null
       |        |
       42       false
        """, {
            assertFunc { it }
        }
    }

    private funcField = { it }

    void testWithField() {
        isRendered """
assert funcField(42) == null
       |             |
       42            false
        """, {
            assert funcField(42) == null
        }
    }

    def func = { it }

    void testWithProperty() {
        isRendered """
assert func(42) == null
       |        |
       42       false
        """, {
            assert func(42) == null
        }
    }

    @groovy.transform.PackageScope class FuncHolder {
        def func = { it }
    }

    void testWithQualifiedProperty() {
        def holder = new FuncHolder()

        isRendered """
assert holder.func(42) == null
       |      |        |
       |      42       false
       ${holder.toString()}
        """, {
            assert holder.func(42) == null
        }
    }

    // for implicit closure calls that don't
    // look like method calls, we don't currently
    // render the return value (little practical value,
    // complicates implementation, unclear how to
    // render in an intuitive way)

     void testWithMethodCall() {
        isRendered """
assert getFunc()(42) == null
       |             |
       |             false
       ${getFunc().toString()}
        """, {
            assert getFunc()(42) == null
        }
    }

    void testWithQualifiedMethodCall() {
        def holder = new FuncHolder()

        isRendered """
assert holder.getFunc()(42) == null
       |      |             |
       |      |             false
       |      ${holder.func.toString()}
       ${holder.toString()}
        """, {
            assert holder.getFunc()(42) == null
        }
    }
}