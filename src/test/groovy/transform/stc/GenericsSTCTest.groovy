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
 * Unit tests for static type checking : generics.
 *
 * @author Cedric Champeau
 */
class GenericsSTCTest extends StaticTypeCheckingTestCase {

    void testDeclaration() {
        assertScript '''
            List test = new LinkedList<String>()
        '''
    }

    void testDeclaration5() {
        assertScript '''
            Map<String,Integer> obj = new HashMap<String,Integer>()
        '''
    }

    void testDeclaration6() {
        shouldFailWithMessages '''
            Map<String,String> obj = new HashMap<String,Integer>()
        ''', 'Incompatible generic argument types. Cannot assign java.util.HashMap <String, Integer> to: java.util.Map <String, String>'
    }

    void testAddOnList() {
        shouldFailWithMessages '''
            List<String> list = []
            list.add(1)
        ''', "Cannot find matching method java.util.List#add(int)"
    }

    void testAddOnList2() {
        assertScript '''
            List<String> list = []
            list.add 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list.add 1
        '''
    }

    void testAddOnListWithDiamond() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list.add 'Hello'
        '''
    }
    
    void testAddOnListUsingLeftShift() {
        shouldFailWithMessages '''
            List<String> list = []
            list << 1
        ''', "Cannot call java.util.List#leftShift(java.lang.String) with arguments [int]"
    }

    void testAddOnList2UsingLeftShift() {
        assertScript '''
            List<String> list = []
            list << 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list << 1
        '''
    }

    void testAddOnListWithDiamondUsingLeftShift() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list << 'Hello'
        '''
    }

    void testAddOnListWithDiamondAndWrongType() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list.add 'Hello'
        ''', 'Cannot find matching method java.util.LinkedList#add(java.lang.String)'
    }

    void testAddOnListWithDiamondAndWrongTypeUsingLeftShift() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list << 'Hello'
        ''', 'Cannot call java.util.LinkedList#leftShift(java.lang.Integer) with arguments [java.lang.String]'
    }

    void testReturnTypeInference() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<Integer>()
            Integer result = foo.method()
        '''
    }

    void testReturnTypeInferenceWithDiamond() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<>()
            Integer result = foo.method()
        '''
    }

    void testReturnTypeInferenceWithMethodGenerics() {
        assertScript '''
            List<Long> list = Arrays.asList([0L,0L] as Long[])
        '''
    }

    void testReturnTypeInferenceWithMethodGenericsAndVarArg() {
        assertScript '''
            List<Long> list = Arrays.asList(0L,0L)
        '''
    }

    void testDiamondInferrenceFromConstructor() {
        assertScript '''
            Set< Long > s2 = new HashSet<>()
        '''
    }

    void testDiamondInferrenceFromConstructorWithoutAssignment() {
        assertScript '''
            new HashSet<>(Arrays.asList(0L,0L));
        '''
    }

    void testDiamondInferrenceFromConstructor2() {
        shouldFailWithMessages '''
            Set< Number > s3 = new HashSet<>(Arrays.asList(0L,0L));
        ''', 'Cannot assign java.util.HashSet <java.lang.Long> to: java.util.Set <Number>'
    }

    void testDiamondInferrenceFromConstructor3() {
        assertScript '''
            Set<Number> s4 = new HashSet<Number>(Arrays.asList(0L,0L))
        '''
    }


    void testLinkedListWithListArgument() {
        assertScript '''
            List<String> list = new LinkedList<String>(['1','2','3'])
        '''
    }

    void testLinkedListWithListArgumentAndWrongElementTypes() {
        shouldFailWithMessages '''
            List<String> list = new LinkedList<String>([1,2,3])
        ''', 'Cannot find matching method java.util.LinkedList#<init>(java.util.List <java.lang.Integer>)'
    }

    void testCompatibleGenericAssignmentWithInferrence() {
        shouldFailWithMessages '''
            List<String> elements = ['a','b', 1]
        ''', 'Incompatible generic argument types. Cannot assign java.util.List <java.lang.Comparable> to: java.util.List <String>'
    }

    void testGenericAssignmentWithSubClass() {
        assertScript '''
            List<String> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        '''
    }

    void testGenericAssignmentWithSubClassAndWrongGenericType() {
        shouldFailWithMessages '''
            List<Integer> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        ''', 'Incompatible generic argument types'
    }

    void testAddShouldBeAllowedOnUncheckedGenerics() {
        assertScript '''
            List list = []
            list.add 'Hello'
            list.add 2
            list.add 'the'
            list.add 'world'
        '''
    }

    void testAssignmentShouldFailBecauseOfLowerBound() {
        shouldFailWithMessages '''
            List<? super Number> list = ['string']
        ''', 'Number'
    }

    void testGroovy5154() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        '''
    }

    void testGroovy5154WithSubclass() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound2 fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            class FooBound2 extends FooBound {}
            new Foo()
        '''
    }

    void testGroovy5154WithIncorrectType() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    Object fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        ''', 'Cannot find matching method FooWithGenerics#say(java.lang.Object)'
    }

    void testVoidReturnTypeInferrence() {
      assertScript '''
        Object m() {
          def s = '1234'
          println 'Hello'
        }
      '''
    }
  
    static class MyList extends LinkedList<String> {}
}

