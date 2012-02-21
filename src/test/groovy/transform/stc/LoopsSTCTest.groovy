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
 * Unit tests for static type checking : loops.
 *
 * @author Cedric Champeau
 */
class LoopsSTCTest extends StaticTypeCheckingTestCase {

    void testMethodCallInLoop() {
        assertScript '''
            int foo(int x) { x+1 }
            int x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }

    void testMethodCallInLoopAndDef() {
        assertScript '''
            int foo(int x) { x+1 }
            def x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }


    void testMethodCallWithEachAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Integer foo(Double x) { x+1 }
            def x = 0
            10.times {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Integer foo(Double x) { x+1 }
            def x = 0
            for (int i=0;i<10;i++) {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadType() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallWithEachAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            10.times {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }
}

