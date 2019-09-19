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

class NestedCategoryTest extends GroovyTestCase {
    void testGreeter_plain() {
        def greeter = new Greeter()
        assertEquals "Hello Groovy!", greeter.greet()
    }

    void testGreeter_withOne() {
        def greeter = new Greeter()
        assertEquals "Hello Groovy!", greeter.greet()
        use(CategoryOne.class) {
            assertEquals "Hello from One", greeter.greet()
        }
        assertEquals "Hello Groovy!", greeter.greet()
    }

    void testGreeter_withTwo() {
        def greeter = new Greeter();
        assertEquals "Hello Groovy!", greeter.greet();
        use(CategoryTwo.class) {
            assertEquals "Hello from Two", greeter.greet();
        }
        assertEquals "Hello Groovy!", greeter.greet();
    }

    void testGreeter_withOneAndTwo_nested() {
        // fails!
        def greeter = new Greeter()
        assertEquals "Hello Groovy!", greeter.greet()
        use(CategoryOne) {
            assertEquals "Hello from One", greeter.greet()
            use(CategoryTwo) {
                assertEquals "Hello from Two", greeter.greet()
            }
            assertEquals "Hello from One", greeter.greet()
        }
        assertEquals "Hello Groovy!", greeter.greet()
    }
}

class Greeter {
    String greet() {
        return "Hello Groovy!"
    }

    String say(String s) {
        return "I say: " + s
    }
}

class CategoryOne {
    static String greet(Greeter self) {
        return "Hello from One"
    }
}

class CategoryTwo {
    static String greet(Greeter self) {
        return "Hello from Two"
    }
}