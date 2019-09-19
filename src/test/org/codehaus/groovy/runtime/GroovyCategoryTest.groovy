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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

class GroovyCategoryTest extends GroovyTestCase {
    void testUseWithVarArg() {
        // Try out the single class case
        use(Category1) {
            assert "HeLlO".upper() == "HELLO"
        }

        // Try out the list case
        use([Category1, Category2]) {
            assert "HeLlO".upper() == "HELLO"
            assert "HeLlO".lower() == "hello"
        }

        // Try out the vararg version
        use(Category1, Category2) {
            assert "HeLlO".upper() == "HELLO"
            assert "HeLlO".lower() == "hello"
        }

        // This should fail
        try {
            use(Category1)
            fail()
        } catch (IllegalArgumentException e) {
        }

        // And so should this
        try {
            use(Category1, Category2)
            fail()
        } catch (IllegalArgumentException e) {
        }
    }
}

class Category1 {
    static String upper(String message) {return message.toUpperCase()}
}

class Category2 {
    static String lower(String message) {return message.toLowerCase()}
}