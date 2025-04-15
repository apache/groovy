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
package bugs

import groovy.test.GroovyTestCase

/**
 */
class Bytecode4Bug extends GroovyTestCase {

    def count = 0

    void testInject() {
        def x = [1, 2, 3].inject(0) { c, s -> c += s }
        assert x == 6
    }

    void testUsingProperty() {
        count = 0
        getCollection().each { count += it }
        assert count == 10
    }

    void testUsingIncrementingProperty() {
        count = 0
        getCollection().each { count++ }
        assert count == 4
    }

    def getCollection() {
        [1, 2, 3, 4]
    }
}
