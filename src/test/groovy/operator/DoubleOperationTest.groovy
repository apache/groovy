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
package groovy.operator

import groovy.test.GroovyTestCase

class DoubleOperationTest extends GroovyTestCase {

    def x
    def y

    void testPlus() {
        x = 2.1 + 2.1
        assert x == 4.2

        x = 3 + 2.2
        assert x == 5.2

        x = 2.2 + 4
        assert x == 6.2

        y = x + 1
        assert y == 7.2

        def z = y + x + 1 + 2
        assert z == 16.4
    }

    void testMinus() {
        x = 6 - 2.2
        assert x == 3.8

        x = 5.8 - 2
        assert x == 3.8

        y = x - 1
        assert y == 2.8
    }

    void testMultiply() {
        x = 3 * 2.0
        assert x == 6.0

        x = 3.0 * 2
        assert x == 6.0

        x = 3.0 * 2.0
        assert x == 6.0
        y = x * 2
        assert y == 12.0
    }

    void testDivide() {
        x = 80.0 / 4
        assert x == 20.0, "x = " + x

        x = 80 / 4.0
        assert x == 20.0, "x = " + x

        y = x / 2
        assert y == 10.0, "y = " + y
    }

    void testMethodNotFound() {
        try {
            println(Math.sin("foo", 7));
            fail("Should catch a MissingMethodException");
        } catch (MissingMethodException mme) {
        }
    }

    void testCoerce() {
        def xyz = Math.sin(1.1);
        assert xyz instanceof Double;
        assert xyz == Math.sin(1.1D);

        //Note that (7.3F).doubleValue() != 7.3D
        x = Math.sin(7.3F);
        assert x instanceof Double;
        assert x == Math.sin((7.3F).doubleValue());

        x = Math.sin(7);
        assert x instanceof Double;
        assert x == Math.sin(7.0D);
    }
}
