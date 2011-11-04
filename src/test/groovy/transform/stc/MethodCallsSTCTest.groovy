/*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Unit tests for static type checking : method calls.
 *
 * @author Cedric Champeau
 */
class MethodCallsSTCTest extends StaticTypeCheckingTestCase {
    @Override
    protected void configure() {
        final ImportCustomizer ic = new ImportCustomizer()
        ic.addImport('A','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass')
        ic.addImport('B','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass2')
        ic.addImport('C','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass3')
        config.addCompilationCustomizers(
                ic
        )
    }

    void testMethodCallOnInstance() {
        assertScript '''
            A a = new A()
            assert a.add(1,1)==2
        '''
    }

    void testBestChoiceMethodCallOnInstance() {
        assertScript '''
            A a = new A()
            assert a.add(1d,1d)==3
        '''
    }

    void testMissingInstanceMethod() {
        shouldFailWithMessages '''
            A a = new A()
            assert a.foo(1,1)==2
        ''', 'Cannot find matching method'
    }

    void testMethodCallOnInstanceWithVarArgs() {
        assertScript '''
            A a = new A()
            assert a.sum(1,1,2)==4
        '''
    }

    void testMethodCallOnInstanceWithVarArgs2() {
        assertScript '''
            A a = new A()
            int[] arr = [1,1,2]
            assert a.sum(arr)==4
        '''
    }

    void testStaticMethodCall() {
        assertScript '''
            String echo = A.echo 'echo'
            assert  echo == 'echo'
        '''
    }

    void testMissingStaticMethod() {
        shouldFailWithMessages '''
            A.missing 'echo'
        ''', 'Cannot find matching method'
    }

    void testStaticMethodWithVarArgs() {
        assertScript '''
            int mul = A.mul([1,2,3] as int[])
            assert mul == 6
        '''
    }

    void testStaticMethodWithVarArgs2() {
        assertScript '''
            int mul = A.mul(1,2,3)
            assert mul == 6
        '''
    }

    void testStaticMethodCallWithInheritance() {
        assertScript '''
            String echo = B.echo 'echo'
            assert  echo == 'echo'
        '''
    }

    void testStaticMethodCallThroughInstance() {
        // this is bad style, but supported by java
        assertScript '''
            A a = new A()
            String echo = a.echo 'echo'
            assert  echo == 'echo'
        '''
    }

    void testStaticMethodCallOnJDK() {
        assertScript '''
            int[] arr = [3,2,1]
            Arrays.sort(arr)
            assert arr == [1,2,3] as int[]
        '''
    }

    void testStaticMethodCallOnJDK2() {
        assertScript '''
            String[] arr = ['3','2','1']
            Arrays.sort(arr)
            assert arr == ['1','2','3']  as String[]
        '''
    }

    void testStaticMethodCallOnJDK3() {
        assertScript '''
            List arr = ['3','2','1']
            Collections.sort(arr)
            assert arr == ['1','2','3']
        '''
    }

    void testStaticMethodCallOnJDK4() {
        assertScript '''
            List<String> arr = ['3','2','1']
            Collections.sort(arr)
            assert arr == ['1','2','3']
        '''
    }

    void testGenericMethodCall() {
        assertScript '''
            C c = new C()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        '''
    }

    void testGenericMethodCallWithVarArg() {
        assertScript '''
            C c = new C()
            assert c.identity('a','b','c') == ['a','b','c']
        '''
    }

    void testGenericMethodCallWithVarArgAndSingleArg() {
        assertScript '''
            C c = new C()
            assert c.identity('a') == ['a']
        '''
    }

    void testGenericMethodCallWithVarArgAndNoArg() {
        assertScript '''
            C c = new C()
            assert c.identity() == []
        '''
    }

    void testGenericMethodCall2() {
        assertScript '''
            B c = new B<String>()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        '''
    }

    void testGenericMethodCall3() {
        shouldFailWithMessages '''
            B c = new B<Integer>()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        ''', 'Cannot call groovy.transform.stc.MethodCallsSTCTest$MyMethodCallTestClass2#identity([Ljava.lang.Integer;) with arguments [[Ljava.lang.String;]'
    }

    static class MyMethodCallTestClass {

        static int mul(int... args) { args.toList().inject(1) { x,y -> x*y } }
        static String echo(String msg) {
            msg
        }

        int add(int x, int y) { x+y }
        int add(double x, double y) { 2*x+y } // stupid but useful for tests :)
        int sum(int... args) { args.toList().sum() }

    }

    static class MyMethodCallTestClass2<T> extends MyMethodCallTestClass {
        T[] identity(T... args) { args }
    }

    static class MyMethodCallTestClass3 extends MyMethodCallTestClass2<String> {}
}
