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

class VariblePrecedence extends GroovyTestCase {
    
    void testVariablePrecedence() {
 
        assertScript( """
            class VariableFoo {
                def x = 100
                def y = 93
                def c = {x -> assert x == 1; assert y == 93; }

                static void main(args) {
                    def vfoo = new VariableFoo()
                    vfoo.c.call(1)

                    def z = 874;
                    1.times { assert vfoo.x == 100; assert z == 874; z = 39; }
                    assert z == 39;

                    vfoo.local();
                }

                void local() {
                    c.call(1);

                    def z = 874;
                    1.times { assert x == 100; assert z == 874; z = 39; }
                    assert z == 39;
                }
            }

        """ );

    }

    void testVariablePrecedenceInScript_FAILS() {
        assertScript( """
            c = { x -> assert x == 1; assert y == 93; }
            x = 100;
            y = 93;

            c.call(1);
        """);
    }

}
