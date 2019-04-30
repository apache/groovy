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

class GenericsBytecodeTest extends GenericsTestBase {

    void testClassWithoutParameterExtendsClassWithFixedParameter() {
        createClassInfo """
            class B extends ArrayList<Long> {}
        """
        assert signatures == ["class": "Ljava/util/ArrayList<Ljava/lang/Long;>;Lgroovy/lang/GroovyObject;",]
    }

    void testMultipleImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements Runnable,List<T> {}
        """
        assert signatures == [
                "class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Runnable;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"
        ]
    }

    void testImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements List<T> {}
        """
        assert signatures == [
                "class": "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"
        ]
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
        assert signatures == [
                "class": "<T:Ljava/lang/Object;>Ljava/util/HashMap<TT;Ljava/util/List<TT;>;>;Lgroovy/lang/GroovyObject;"
        ]
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
        assert signatures == [
                "class": "<Y:Ljava/lang/Object;T::Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<TY;Ljava/lang/Integer;>;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"
        ]
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
        assert signatures == ["class" : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
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
        assert signatures == [
                "class"                   : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/lang/Object;)V": "(TT;)V"
        ]
    }

    void testParameterAsNestedMethodParameter() {
        createClassInfo """
            class B<T> {
                void foo(List<T> t){}
            }
        """
        assert signatures == [
                "class"                 : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/util/List;)V": "(Ljava/util/List<TT;>;)V"
        ]
    }

    void testParameterAsNestedMethodParameterReturningInterface() {
        createClassInfo """
            class B<T> {
                Cloneable foo(List<T> t){}
            }
        """
        assert signatures == [
                "class"                                     : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo(Ljava/util/List;)Ljava/lang/Cloneable;": "(Ljava/util/List<TT;>;)Ljava/lang/Cloneable;"
        ]
    }

    void testArray() {
        createClassInfo """
            class B<T> {
                T[] get(T[] arr) {return null}
            }
        """
        assert signatures == [
                "class"                                      : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "get([Ljava/lang/Object;)[Ljava/lang/Object;": "([TT;)[TT;"
        ]
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
        assert signatures == [
                "class"                      : "<A::Ljava/lang/Comparable<TA;>;:Ljava/lang/Cloneable;B::Ljava/lang/Cloneable;:Ljava/lang/Comparable<TB;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "foo()Ljava/lang/Comparable;": "()TA;",
                "bar()Ljava/lang/Cloneable;" : "()TB;"
        ]
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
                "class"                           : "<T:Ljava/lang/Number;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                dependency                        : "Ljava/util/List<-TT;>;",
                "setDependency(Ljava/util/List;)V": "(Ljava/util/List<-TT;>;)V",
                "getDependency()Ljava/util/List;" : "()Ljava/util/List<-TT;>;",
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
                "class"                           : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
                "owner"                           : "TT;",
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
        assert signatures == [
                "foo(Ljava/lang/Object;)V": "<V:TA;>(TV;)V", "class": "<A:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"
        ]
    }

    void "test method with generic return type defined at class level"() {
        // class Bar should compile successfully

        // the classes it references should be available as class files to check for ASM resolving
        //  so they're defined in compiled GenericsTestData and not loaded from text in the test
        createClassInfo 'class Bar extends gls.generics.GenericsTestData.Abstract<String> {}'
    }
}
