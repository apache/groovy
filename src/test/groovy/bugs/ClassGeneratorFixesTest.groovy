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


class ClassGeneratorFixesTest extends GroovyTestCase {
    def count = 0;

    def pf(int p) {
        int i = p
        boolean b = true
    }

    void testPrimitvesInFunc() { // groovy-373, 453, 385, 451, 199
        pf(10)
    }

    void testPlusEqual() { // 372
        count += 1
        assert count == 1

        def foo =
            {i->
                return {j->
                    i += j
                    i
                }
            }
        def x = foo(1)
        x(5)
        foo(3)
        println x(2.3)
    }

    void testIfAndSwitchInClosure (){ // 321, 324, 412

        def a = 1
        1.times {
            if (a ==1) {
                a = 2
            }
        }

        def noneYet=true;
        ["a","b","c","d"].each { c ->
          if (noneYet) {
            noneYet=false;
          } else {
            print(" > ");
          }
          print( c );
        }

        a = 1
        switch (a) {
        case 1:
            a = 2;
        case 2:
            break;
        default:
            break;
        }
    }

    void returnVoid() {
        return
    }

    void testReturnVoid() { // groovy-405, 387
        returnVoid()
    }
    
    void testBooleanValue() { // groovy-385
            /** @todo
            boolean value
            */
        }

}

