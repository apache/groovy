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

class Groovy7709Bug extends GroovyTestCase {

    void testConvertedClosureAsGroovyObject() {
        def closure = { 43 }
        def proxy = closure as Groovy7709BugY
        assert proxy instanceof GroovyObject
        assert proxy.foo() == 43
    }

    void testConvertedMapAsGroovyObject() {
        def map = [foo: { 43 }]
        def proxy = map as Groovy7709BugY
        assert proxy instanceof GroovyObject
        assert proxy.foo() == 43
    }

}

interface Groovy7709BugY extends GroovyObject {
    int foo()
}
