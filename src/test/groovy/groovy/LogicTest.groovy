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

import org.junit.jupiter.api.Test


class LogicTest {

    @Test
    void testAndWithTrue() {

        def x = false
        def n = 2

        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == true
    }

    @Test
    void testAndWithFalse() {

        def x = false
        def n = 20

        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == false

        n = 0

        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == false
    }

    @Test
    void testOrWithTrue() {

        def x = false
        def n = 2

        if ( n > 1 || n < 10 ) {
            x = true
        }

        assert x == true

        x = false
        n = 0

        if ( n > 1 || n == 0 ) {
            x = true
        }

        assert x == true
    }

    @Test
    void testOrWithFalse() {

        def x = false
        def n = 11

        if ( n < 10 || n > 20 ) {
            x = true
        }

        assert x == false

        n = 11

        if ( n < 10 || n > 20 ) {
            x = true
        }

        assert x == false
    }
}
