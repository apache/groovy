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

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.fail


class DoubleOperationsTest {

    def x
    def y

    @Test
    void testPlus() {
        x = 2.1d + 2.1d
        assert x == 4.2d

        x = 3d + 2.2d
        assert x == 5.2d

        x = 2.2d + 4d
        assert x == 6.2d

        y = x + 1d
        assert y == 7.2d

        def z = y + x + 1d + 2d
        assert z == 16.4d
    }

    @Test
    void testMinus() {
        x = 6d - 2.2d
        assert x == 3.8d

        x = 5.8d - 2d
        assert x == 3.8d

        y = x - 1d
        assert y == 2.8d
    }

    @Test
    void testMultiply() {
        x = 3d * 2.0d
        assert x == 6.0d

        x = 3.0d * 2d
        assert x == 6.0d

        x = 3.0d * 2.0d
        assert x == 6.0d
        y = x * 2d
        assert y == 12.0d
    }

    @Test
    void testDivide() {
        x = 80.0d / 4d
        assert x == 20.0d, "x = " + x

        x = 80d / 4.0d
        assert x == 20.0d, "x = " + x

        y = x / 2d
        assert y == 10.0d, "y = " + y
    }

    @Test
    void testMod() {
        x = 100d.mod(3)
        assert x == 1d

        y = 11d
        y = y.mod(3d)
        assert y == 2d
        y = -11d
        y = y.mod(3d)
        assert y == 1d
    }

    @Test
    void testRemainder() {
        x = 100d % 3
        assert x == 1d

        y = 11d
        y %= 3d
        assert y == 2d
        y = -11d
        y %= 3d
        assert y == -2d
    }

    @Test
    void testMethodNotFound() {
        try {
            println(Math.sin("foo", 7));
            fail("Should catch a MissingMethodException");
        } catch (MissingMethodException mme) {
        }
    }

    @Test
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
