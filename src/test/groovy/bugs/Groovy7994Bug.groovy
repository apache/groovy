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

class Groovy7994Bug extends GroovyTestCase {
    void testJavaBeanPropertiesAvailableInInnerClasses() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Outer {
                String prop = 'wally'
                String getName() { "sally" }
                class Inner {
                    String innerGo() { new Other(name).text + new Other(Outer.this.name).text + new Other(getName()).text }
                }
                String go() {
                    new Other(name).text + new Other(getName()).text
                }
                def makeAIC() {
                    new Object() {
                        String aicGo() { new Other(name).text + new Other(Outer.this.name).text + new Other(getName()).text }
                        String staticMethodGo() { Other.foo(name) + Other.foo(Outer.this.name) + Other.foo(getName()) }
                        String instanceMethodGo() { new Other().bar(name) + new Other().bar(getName()) }
                        String methodWithClosureGo() { new Other().with { bar(name) + bar(getName()) } }
                        String propGo() { new Other(prop).text + new Other(getProp()).text }
                    }
                }
            }

            @CompileStatic
            class Other {
                public final String text
                static String foo(Object object) { "Object|$object:" }
                static String foo(String string) { "String|$string:" }
                String bar(Object object) { "Object|$object:" }
                String bar(String string) { "String|$string:" }
                Other(Object object) { text = "Object:$object|" }
                Other(String string) { text = "String:$string|" }
                Other() {}
            }

            def o = new Outer()
            assert o.go() == 'String:sally|String:sally|'
            assert o.makeAIC().aicGo() == 'String:sally|String:sally|String:sally|'
            assert o.makeAIC().propGo() == 'String:wally|String:wally|'
            assert o.makeAIC().staticMethodGo() == 'String|sally:String|sally:String|sally:'
            assert o.makeAIC().instanceMethodGo() == 'String|sally:String|sally:'
            assert o.makeAIC().methodWithClosureGo() == 'String|sally:String|sally:'
            assert new Outer.Inner(o).innerGo() == 'String:sally|String:sally|String:sally|'
        '''
    }
}
