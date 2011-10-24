/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

/**
 * Unit tests for static type checking : miscellaneous tests.
 *
 * @author Cedric Champeau
 */
class MiscSTCTest extends StaticTypeCheckingTestCase {

     void testFibonacci() {
         assertScript '''
            long sd = System.currentTimeMillis()
            int fib(int i) {
                i < 2 ? 1 : fib(i - 2) + fib(i - 1);
            }
            println fib(40)
            long dur = System.currentTimeMillis()-sd
            println "${dur}ms"
         '''
     }

    void testGreeter() {
        assertScript '''
            class Greet {
                def name

                Greet(String who) {
                    name = who[0].toUpperCase() +
                            who[1..-1]
                }

                def salute() { println "Hello $name!" }
            }

            def g = new Greet('world')  // create object
            g.salute()               // output "Hello World!"
        '''
    }

    void testClosureReturnTypeShouldNotBeTestedAgainstMethodReturnType() {
        assertScript '''
        void method() {
            def cl = { String it -> it.toUpperCase() }
            assert cl('test')=='TEST'
        }
        '''
    }
}

