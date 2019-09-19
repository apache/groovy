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

class Groovy8048Bug extends GroovyTestCase {
    void testFinalFieldInPreCompiledTrait() {
        def shell = new GroovyShell(getClass().classLoader)
        shell.evaluate('''
            class Bar implements groovy.bugs.Groovy8048Bug.Groovy8048Trait, BarTrait {
                static void main(args) {
                    assert Bar.otherItems1 && Bar.otherItems1[0] == 'bar1'
                    assert Bar.items1 && Bar.items1[0] == 'foo1'
                    new Bar().with {
                        assert otherItems2 && otherItems2[0] == 'bar2'
                        assert items2 && items2[0] == 'foo2'
                    }
                }
                trait BarTrait {
                    final static List otherItems1 = ['bar1']
                    final List otherItems2 = ['bar2']
                }
            }
        ''', 'testScript')
    }

    trait Groovy8048Trait {
        final static List items1 = ['foo1']
        final List items2 = ['foo2']
    }
}
