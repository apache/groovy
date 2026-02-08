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

import groovy.test.GroovyTestCase

class WhileLoopTest extends GroovyTestCase {

    void testVerySimpleWhile() {
        def val = doWhileMethod(0, 5)
        assert val == 5
    }

    void testWhileWithEmptyBody() {
        int x = 3
        while (--x);
        assert x == 0
    }

    void testMoreComplexWhile() {
        def x = 0
        def y = 5

        while ( y > 0 ) {
            x = x + 1
            y = y - 1
        }

        assert x == 5
    }

    def doWhileMethod(x, m) {
        while ( x < m ) {
            x = increment(x)
        }

        return x
    }

    def increment(x) {
        x + 1
    }
}
