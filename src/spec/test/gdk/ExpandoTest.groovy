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
package gdk

import groovy.test.GroovyTestCase

class ExpandoTest extends GroovyTestCase {
    void testExpandoAddProperty() {
        // tag::expando_property[]
        def expando = new Expando()
        expando.name = 'John'

        assert expando.name == 'John'
        // end::expando_property[]
    }

    void testExpandoAddMethod() {
        // tag::expando_method[]
        def expando = new Expando()
        expando.toString = { -> 'John' }
        expando.say = { String s -> "John says: ${s}" }

        assert expando as String == 'John'
        assert expando.say('Hi') == 'John says: Hi'
        // end::expando_method[]
    }

}
