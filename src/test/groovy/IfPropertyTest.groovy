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

class IfPropertyTest extends GroovyTestCase {

    def dummy

    // This is because normal classes are not extensible, but scripts are extensible by default.
    Object get(String key) {
        return dummy
    }

    void set(Object key, Object value) {
        dummy = value
    }

    void testIfNullPropertySet() {
        if (cheese == null) {
            cheese = 1
        }
        if (cheese != 1) {
            fail("Didn't change cheese")
        }
        assert cheese == 1
    }

    void testIfNullPropertySetRecheck() {
        if (cheese == null) {
            cheese = 1
        }
        if (cheese == 1) {
            cheese = 2
        }
        assert cheese == 2
    }

}
