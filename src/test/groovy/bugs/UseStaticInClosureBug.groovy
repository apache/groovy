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
package groovy.bugs

import groovy.test.GroovyTestCase

/**
 */
class UseStaticInClosureBug extends GroovyTestCase {

    static def stuff = [:]

    void testBug() {
        [1,2,3].each { stuff[it] = "dog" }

        assert stuff.size() == 3
        assert stuff[2] == "dog"
    }

    void testBug2() {
        doStatic()
    }

    static def doStatic() {
        [1,2,3].each { stuff[it] = "dog" }

        assert stuff.size() == 3
        assert stuff[2] == "dog"
    }
}
