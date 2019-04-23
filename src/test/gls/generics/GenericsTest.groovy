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
package gls.generics

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class GenericsTest extends GenericsTestBase {

    void testClassWithoutParameterExtendsClassWithFixedParameter() {
        createClassInfo """
            class B extends ArrayList<Long> {}
        """
        assert signatures == [
                "class": "Ljava/util/ArrayList<Ljava/lang/Long;>;Lgroovy/lang/GroovyObject;",
        ]
    }

    void testMultipleImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements Runnable,List<T> {}
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Runnable;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
    }

    void testImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements List<T> {}
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
    }

    void testExtendsWithParameter() {
        createClassInfo """
            class B<T> extends ArrayList<T> {}
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/util/ArrayList<TT;>;Lgroovy/lang/GroovyObject;"]
    }

    void testNestedExtendsWithParameter() {
        createClassInfo """
            class B<T> extends HashMap<T,List<T>> {}
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/util/HashMap<TT;Ljava/util/List<TT;>;>;Lgroovy/lang/GroovyObject;"]
    }

    void testBoundInterface() {
        createClassInfo """
            class B<T extends List> {}
        """
        assert signatures == ["class": "<T::Ljava/util/List;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"]
    }

    void testNestedReuseOfParameter() {
        createClassInfo """
            class B<Y,T extends Map<String,Map<Y,Integer>>> {}
        """
        assert signatures == ["class": "<Y:Ljava/lang/Object;T::Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<TY;Ljava/lang/Integer;>;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"]
    }

    void testFieldWithParameter() {
        createClassInfo """
            class B { public Collection<Integer> books }
        """
        assert signatures == [books: "Ljava/util/Collection<Ljava/lang/Integer;>;"]
    }

    void testFieldReusedParameter() {
        createClassInfo """
            class B<T> { public Collection<T> collection }
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                collection: "Ljava/util/Collection<TT;>;"]
    }

    void testParameterAsReturnType() {
        createClassInfo """
            class B {
                static <T> T foo() {return null}
            }
        """
        assert signatures == ["foo()Ljava/lang/Object;": "<T:Ljava/lang/Object;>()TT;"]
    }

    void testParameterAsReturnTypeAndParameter() {
        createClassInfo """
            class B {
                static <T> T foo(T t) {return null}
            }
        """
        assert signatures == ["foo(Ljava/lang/Object;)Ljava/lang/Object;": "<T:Ljava/lang/Object;>(TT;)TT;"]
    }

    void testParameterAsMethodParameter() {
        createClassInfo """
            class B<T> {
                void foo(T t){}
            }
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/lang/Object;)V": "(TT;)V"]
    }

    void testParameterAsNestedMethodParameter() {
        createClassInfo """
            class B<T> {
                void foo(List<T> t){}
            }
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/util/List;)V": "(Ljava/util/List<TT;>;)V"]
    }

    void testParameterAsNestedMethodParameterReturningInterface() {
        createClassInfo """
            class B<T> {
                Cloneable foo(List<T> t){}
            }
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/util/List;)Ljava/lang/Cloneable;": "(Ljava/util/List<TT;>;)Ljava/lang/Cloneable;"]
    }

    void testArray() {
        createClassInfo """
            class B<T> {
                T[] get(T[] arr) {return null}
            }
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "get([Ljava/lang/Object;)[Ljava/lang/Object;": "([TT;)[TT;"]
    }

    void testMultipleBounds() {
        createClassInfo """
            class Pair<    A extends Comparable<A> & Cloneable , 
                        B extends Cloneable & Comparable<B> > 
            {
                A foo(){}
                B bar(){}
            }
        """
        assert signatures ==
                ["class": "<A::Ljava/lang/Comparable<TA;>;:Ljava/lang/Cloneable;B::Ljava/lang/Cloneable;:Ljava/lang/Comparable<TB;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                        "foo()Ljava/lang/Comparable;": "()TA;",
                        "bar()Ljava/lang/Cloneable;": "()TB;"]
    }

    void testWildCard() {
        createClassInfo """
            class B {
                private Collection<?> f1 
                private List<? extends Number> f2 
                private Comparator<? super String> f3 
                private Map<String,?> f4  
            }
        """
        assert signatures == [
                f1: "Ljava/util/Collection<*>;",
                f2: "Ljava/util/List<+Ljava/lang/Number;>;",
                f3: "Ljava/util/Comparator<-Ljava/lang/String;>;",
                f4: "Ljava/util/Map<Ljava/lang/String;*>;"
        ]
    }

    void testwildcardWithBound() {
        createClassInfo """
            class Something<T extends Number> {
                List<? super T> dependency
            }
        """
        assert signatures == [
                "class":    "<T:Ljava/lang/Number;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                dependency: "Ljava/util/List<-TT;>;",
                "setDependency(Ljava/util/List;)V"  : "(Ljava/util/List<-TT;>;)V",
                "getDependency()Ljava/util/List;"   : "()Ljava/util/List<-TT;>;",
        ]
    }

    void testParameterAsParameterForReturnTypeAndFieldClass() {
        createClassInfo """
               class B<T> {
                   private T owner;
                   Class<T> getOwnerClass(){}
   
            } 
        """
        assert signatures == [
                "class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "owner": "TT;",
                "getOwnerClass()Ljava/lang/Class;": "()Ljava/lang/Class<TT;>;"
        ]
    }

    void testInterfaceWithParameter() {
        createClassInfo """
            interface B<T> {}
        """
        assert signatures == ["class": "<T:Ljava/lang/Object;>Ljava/lang/Object;"]
    }


    void testTypeParamAsBound() {
        createClassInfo """
    class Box<A> {
      public <V extends A> void foo(V v) {
      }

    }
        """
        assert signatures == ["foo(Ljava/lang/Object;)V": "<V:TA;>(TV;)V", "class": "<A:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"]
    }

    void testInvalidParameterUsage() {
        shouldNotCompile """
            abstract class B<T> implements Map<T>{}
        """
        shouldNotCompile """
            class A<T,V> extends ArrayList<T,V>{}
        """
        shouldNotCompile """
            class A<T extends Number> {}
            class B<T> extends A<T>{}
        """
        shouldNotCompile """
            class B<T> extends ArrayList<?>{}
        """
    }

    void testCovariantReturn() {
        shouldNotCompile """
          class A<T> {
              T foo(T t) {1}
           }

          class B extends A<Long>{
              String foo(Long l){"2"}
          }
        """

        assertScript """
          class A<T> {
              T foo(T t) {1}
           }

          class B extends A<Long>{
              Long foo(Long l){2}
          }
          def b = new B();
          try {
            b.foo(new Object())
            assert false
          } catch (ClassCastException cce) {
            assert true
          }
          assert b.foo((Long) 1) == 2
        """
    }

    void testCovariantReturnWithInterface() {
        assertScript """
        import java.util.concurrent.*

        class CallableTask implements Callable<String> {
            String call() { "x" }
        } 
        
        def task = new CallableTask()
        assert task.call() == "x"
      """
    }

    void testCovariantReturnWithEmptyAbstractClassesInBetween() {
        assertScript """
        import java.util.concurrent.*;

        abstract class AbstractCallableTask<T> implements Callable<T> { }
        abstract class SubclassCallableTask<T> extends AbstractCallableTask<T> { }
        class CallableTask extends SubclassCallableTask<String> {
            String call() { return "x"; }
        }
        assert "x" == new CallableTask().call();
      """
    }

    void testGenericsDiamondShortcutSimple() {
        assertScript """
            List<List<String>> list1 = new ArrayList<>()
            assert list1.size() == 0
        """
    }

    void testGenericsDiamondShortcutComplex() {
        assertScript """
            List<List<List<List<List<String>>>>> list2 = new ArrayList<>()
            assert list2.size() == 0
        """
    }

    void testGenericsDiamondShortcutMethodCall() {
        assertScript """
            def method(List<List<String>> list3) {
              list3.size()
            }

            assert method(new ArrayList<>()) == 0
        """
    }

    void testGenericsDiamondShortcutIllegalPosition() {
        shouldFailCompilationWithMessage '''
            List<> list4 = []
        ''', 'unexpected token: <'
    }

    void testGenericsInAsType() {
        // this is to ensure no regression to GROOVY-2725 will happen
        // "as ThreadLocal<Integer>\n" did not compile because the nls
        // was swallowed and could not be used to end the expression
        assertScript """
import java.util.concurrent.atomic.AtomicInteger

class ThreadId {
    private static final AtomicInteger nextId = new AtomicInteger(0)
    private static final ThreadLocal<Integer> threadId = [
        initialValue: { return nextId.getAndIncrement() }
    ] as ThreadLocal<Integer>

    static int get() {
        println "Thread ID: " + threadId.get()
        return threadId.get()
    }

 }
 // we do not actually want to execute something, just
 // ensure this compiles, so we do a dummy command here
 assert ThreadId != null
       """
    }

    void testCompilationWithMissingClosingBracketsInGenerics() {
        shouldFailCompilationWithExpectedMessage """
            def list1 = new ArrayList<Integer()
        """

        shouldFailCompilationWithExpectedMessage """
            List<Integer list2 = new ArrayList<Integer>()
        """

        shouldFailCompilationWithExpectedMessage """
            def c = []
            for (Iterator<String i = c.iterator(); i.hasNext(); ) { }
        """

        shouldFailCompilationWithExpectedMessage """
            def m(Class<Integer someParam) {}
        """

        shouldFailCompilationWithExpectedMessage """
            abstract class ArrayList1<E extends AbstractList<E> implements List<E> {}
        """

        shouldFailCompilationWithExpectedMessage """
            abstract class ArrayList2<E> extends AbstractList<E implements List<E> {}
        """

        shouldFailCompilationWithExpectedMessage """
            abstract class ArrayList3<E> extends AbstractList<E> implements List<E {}
        """

        shouldFailCompilationWithExpectedMessage """
            def List<List<Integer> history = new ArrayList<List<Integer>>()
        """

        shouldFailCompilationWithExpectedMessage """
            def List<List<Integer>> history = new ArrayList<List<Integer>()
        """
    }

    private void shouldFailCompilationWithExpectedMessage(scriptText) {
        shouldFailCompilationWithMessage scriptText, "Missing closing bracket '>' for generics types"
    }

    private void shouldFailCompilationWithMessage(scriptText, String errorMessage) {
        shouldFailCompilationWithMessages(scriptText, [errorMessage])
    }

    private void shouldFailCompilationWithMessages(scriptText, List<String> errorMessages) {
        try {
            assertScript scriptText
            fail("The script compilation should have failed as it contains generics errors, e.g. mis-matching generic brackets")
        } catch (MultipleCompilationErrorsException mcee) {
            def text = mcee.toString()
            errorMessages.each {
                assert text.contains(it)
            }
        }
    }

    void testGenericsInfoForClosureParameters() {
        def cl = { List<String> s -> }
        def type = cl.getClass().getMethod("call", List).genericParameterTypes[0]
        assert type.toString().contains("java.util.List<java.lang.String>")

        type = cl.getClass().getMethod("doCall", List).genericParameterTypes[0]
        assert type.toString().contains("java.util.List<java.lang.String>")
    }

    void testBoundedGenericsWithInheritanceGroovy4974() {
        assertScript '''
            class TestGenerics {
                static interface Z {}
                static class V implements Z {}
                static class W extends V {}
                static interface X extends Z {}
                static class Y implements X {}

                static class A <T extends Z> { def a(T t) { this } }
                static class B extends A<W> {}
                static class C extends A<V> {}
                static class D extends A<Y> {}

                static void main(String[] args) {
                    assert new B().a(new W()) instanceof B
                    assert new C().a(new W()) instanceof C
                    assert new D().a(new Y()) instanceof D
                }
            }
        '''
    }

    void "test method with generic return type defined at class level"() {
        // class Bar should compile successfully

        // the classes it references should be available as class files to check for ASM resolving
        //  so they're defined in compiled GenericsTestData and not loaded from text in the test
        createClassInfo 'class Bar extends gls.generics.GenericsTestData.Abstract<String> {}'
    }

    void testFriendlyErrorMessageForGenericsArityErrorsGroovy7865() {
        shouldFailCompilationWithMessages '''
            class MyList extends ArrayList<String, String> {}
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            class MyList extends ArrayList<> {}
        ''', ['(supplied with 0 type parameters)', 'which takes 1 parameter', 'invalid Diamond <> usage?']
        shouldFailCompilationWithMessages '''
            class MyMap extends HashMap<String> {}
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class MyList implements List<String, String> {}
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            class MyList implements Map<> {}
        ''', ['(supplied with 0 type parameters)', 'which takes 2 parameters', 'invalid Diamond <> usage?']
        shouldFailCompilationWithMessages '''
            class MyMap implements Map<String> {}
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            List<String> ss = new LinkedList<Integer, String>()
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessage '''
            List<String> ss = new LinkedList<>(){}
        ''', 'Cannot use diamond <> with anonymous inner classes'
        shouldFailCompilationWithMessages '''
            List<String> ss = new LinkedList<String, String>(){}
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            List<String> ss = new LinkedList<String, String>()
        ''', ['supplied with 2 type parameters', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            def now = new Date<Calendar>()
        ''', ['supplied with 1 type parameter', 'which takes no parameters']
        shouldFailCompilationWithMessages '''
            def method(Map<String> map) { map.toString() }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def method(Map<String, Map<String>> map) { map.toString() }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class MyClass { Map<String> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class MyClass { Map<String, Map<String>> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
             def method() { Map<String> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
             def method() { Map<String, Map<String>> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        assertScript '''
            List<String> ss = new LinkedList<>()
        '''
    }
}
