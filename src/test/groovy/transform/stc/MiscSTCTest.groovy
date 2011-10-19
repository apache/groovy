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

import org.codehaus.groovy.util.ListHashMap

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

    void testClosureWithoutArguments() {
        assertScript '''
        def clos = { println "hello!" }

        println "Executing the Closure:"
        clos() //prints "hello!"
        '''
    }

    void testClosureWithArguments() {
        assertScript '''
            def printSum = { int a, int b -> print a+b }
            printSum( 5, 7 ) //prints "12"
        '''

        shouldFailWithMessages '''
            def printSum = { int a, int b -> print a+b }
            printSum( '5', '7' ) //prints "12"
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, java.lang.String]'
    }

    void testClosureWithArgumentsAndNoDef() {
        assertScript '''
            { int a, int b -> print a+b }(5,7)
        '''
    }

    void testClosureWithArgumentsNoDefAndWrongType() {
        shouldFailWithMessages '''
            { int a, int b -> print a+b }('5',7)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInferrence() {
        assertScript '''
            def closure = { int x, int y -> return x+y }
            int total = closure(2,3)
        '''

        shouldFailWithMessages '''
            def closure = { int x, int y -> return x+y }
            int total = closure('2',3)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInferrenceWithoutDef() {
        assertScript '''
            int total = { int x, int y -> return x+y }(2,3)
        '''
    }

    void testListHashMap() {
        def map = new ListHashMap()
        map.put('a', 'a')
        assert map.size()==1
        map.put('b', 'b')
        assert map.size()==2
        map.put('c','c')
        assert map.size()==3
    }

}

