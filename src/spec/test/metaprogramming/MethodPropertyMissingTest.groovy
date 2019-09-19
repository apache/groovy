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
package metaprogramming

import groovy.test.GroovyTestCase

class MethodPropertyMissingTest extends GroovyTestCase {

    void testMethodMissing() {
        assertScript '''
            //tag::method_missing_simple[]
            class Foo {
            
               def methodMissing(String name, def args) {
                    return "this is me"
               }
            }

            assert new Foo().someUnknownMethod(42l) == 'this is me'
            //end::method_missing_simple[]
        '''
    }

    void testPropertyMissingGetter() {
        assertScript '''
            //tag::property_missing_getter[]
            class Foo {
               def propertyMissing(String name) { name }
            }

            assert new Foo().boo == 'boo'
            //end::property_missing_getter[]
        '''
    }

    void testPropertyMissingGetterSetter() {
        assertScript '''
            //tag::property_missing_getter_setter[]
            class Foo {
               def storage = [:]
               def propertyMissing(String name, value) { storage[name] = value }
               def propertyMissing(String name) { storage[name] }
            }

            def f = new Foo()
            f.foo = "bar"

            assert f.foo == "bar"
            //end::property_missing_getter_setter[]
        '''
    }

}
