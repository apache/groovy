/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ClosuresSpecTest extends GroovyTestCase {
    static void sink(Closure cl) {}

    void testClosureSyntax() {
        sink
                // tag::closure_syntax_1[]
                { item++ }                                          // <1>
                // end::closure_syntax_1[]
        sink
                // tag::closure_syntax_1bis[]
                { -> item++ }                                       // <2>
                // end::closure_syntax_1bis[]
        sink
                // tag::closure_syntax_2[]
                { println it }                                      // <3>
                // end::closure_syntax_2[]

        sink
                // tag::closure_syntax_3[]
                { it -> println it }                                // <4>
                // end::closure_syntax_3[]

        sink
                // tag::closure_syntax_4[]
                { name -> println name }                            // <5>
                // end::closure_syntax_4[]

        sink
                // tag::closure_syntax_5[]
                { String x, int y ->                                // <6>
                    println "hey ${x} the value is ${y}"
                }
                // end::closure_syntax_5[]

        sink
                // tag::closure_syntax_6[]
                { reader ->                                         // <7>
                    def line = reader.readLine()
                    line.trim()
                }
                // end::closure_syntax_6[]
    }

    void testAssignClosureToAVariable() {
        // tag::closure_is_an_instance_of_Closure[]
        def listener = { e -> println "Clicked on $e.source" }      // <1>
        assert listener instanceof Closure
        Closure callback = { println 'Done!' }                      // <2>
        Closure<Boolean> isTextFile = {
            File it -> it.name.endsWith('.txt')                     // <3>
        }
        // end::closure_is_an_instance_of_Closure[]
    }

    void testCallClosure() {
        // tag::closure_call_1[]
        def code = { 123 }
        // end::closure_call_1[]
        // tag::closure_call_1_direct[]
        assert code() == 123
        // end::closure_call_1_direct[]
        // tag::closure_call_1_explicit[]
        assert code.call() == 123
        // end::closure_call_1_explicit[]

        // tag::closure_call_2[]
        def isOdd = { int i-> i%2 == 1 }                            // <1>
        assert isOdd(3) == true                                     // <2>
        assert isOdd.call(2) == false                               // <3>

        def isEven = { it%2 == 0 }                                  // <4>
        assert isEven(3) == false                                   // <5>
        assert isEven.call(2) == true                               // <6>
        // end::closure_call_2[]
    }

    void testClosureParameters() {
        // tag::closure_param_declaration[]
        def closureWithOneArg = { str -> str.toUpperCase() }
        assert closureWithOneArg('groovy') == 'GROOVY'

        def closureWithOneArgAndExplicitType = { String str -> str.toUpperCase() }
        assert closureWithOneArgAndExplicitType('groovy') == 'GROOVY'

        def closureWithTwoArgs = { a,b -> a+b }
        assert closureWithTwoArgs(1,2) == 3

        def closureWithTwoArgsAndExplicitTypes = { int a, int b -> a+b }
        assert closureWithTwoArgsAndExplicitTypes(1,2) == 3

        def closureWithTwoArgsAndOptionalTypes = { a, int b -> a+b }
        assert closureWithTwoArgsAndOptionalTypes(1,2) == 3

        def closureWithTwoArgAndDefaultValue = { int a, int b=2 -> a+b }
        assert closureWithTwoArgAndDefaultValue(1) == 3
        // end::closure_param_declaration[]
    }

    void testImplicitIt() {
        assertScript '''
        // tag::implicit_it[]
        def greeting = { "Hello, $it!" }
        assert greeting('Patrick') == 'Hello, Patrick!'
        // end::implicit_it[]
        '''

        assertScript '''
        // tag::implicit_it_equiv[]
        def greeting = { it -> "Hello, $it!" }
        assert greeting('Patrick') == 'Hello, Patrick!'
        // end::implicit_it_equiv[]
        '''

        assertScript '''
        // tag::closure_no_arg_def[]
        def magicNumber = { -> 42 }
        // end::closure_no_arg_def[]
        try {
            // tag::closure_no_arg_fail[]
            // this call will fail because the closure doesn't accept any argument
            magicNumber(11)
            // end::closure_no_arg_fail[]
            assert false
        } catch (MissingMethodException) {
        }
        '''
    }

    void testClosureVargs() {
        // tag::closure_vargs[]
        def concat1 = { String... args -> args.join('') }           // <1>
        assert concat1('abc','def') == 'abcdef'                     // <2>
        def concat2 = { String[] args -> args.join('') }            // <3>
        assert concat2('abc', 'def') == 'abcdef'

        def multiConcat = { int n, String... args ->                // <4>
            args.join('')*n
        }
        assert multiConcat(2, 'abc','def') == 'abcdefabcdef'
        // end::closure_vargs[]

    }

    void testThisObject() {
        assertScript '''
        // tag::closure_this[]
        class Enclosing {
            void run() {
                def whatIsThisObject = { getThisObject() }          // <1>
                assert whatIsThisObject() == this                   // <2>
                def whatIsThis = { this }                           // <3>
                assert whatIsThis() == this                         // <4>
            }
        }
        class EnclosedInInnerClass {
            class Inner {
                Closure cl = { this }                               // <5>
            }
            void run() {
                def inner = new Inner()
                assert inner.cl() == inner                          // <6>
            }
        }
        class NestedClosures {
            void run() {
                def nestedClosures = {
                    def cl = { this }                               // <7>
                    cl()
                }
                assert nestedClosures() == this                     // <8>
            }
        }
        // end::closure_this[]
        new Enclosing().run()
        new EnclosedInInnerClass().run()
        new NestedClosures().run()
'''
        assertScript '''
            // tag::closure_this_call[]
            class Person {
                String name
                int age
                String toString() { "$name is $age years old" }

                String dump() {
                    def cl = {
                        String msg = this.toString()               // <1>
                        println msg
                        msg
                    }
                    cl()
                }
            }
            def p = new Person(name:'Janice', age:74)
            assert p.dump() == 'Janice is 74 years old'
            // end::closure_this_call[]
        '''
    }

    void testOwner() {
        assertScript '''
        // tag::closure_owner[]
        class Enclosing {
            void run() {
                def whatIsOwnerMethod = { getOwner() }               // <1>
                assert whatIsOwnerMethod() == this                   // <2>
                def whatIsOwner = { owner }                          // <3>
                assert whatIsOwner() == this                         // <4>
            }
        }
        class EnclosedInInnerClass {
            class Inner {
                Closure cl = { owner }                               // <5>
            }
            void run() {
                def inner = new Inner()
                assert inner.cl() == inner                           // <6>
            }
        }
        class NestedClosures {
            void run() {
                def nestedClosures = {
                    def cl = { owner }                               // <7>
                    cl()
                }
                assert nestedClosures() == nestedClosures            // <8>
            }
        }
        // end::closure_owner[]
        new Enclosing().run()
        new EnclosedInInnerClass().run()
        new NestedClosures().run()
'''
    }

    void testClosureDelegate() {
        assertScript '''
        // tag::delegate_is_owner[]
        class Enclosing {
            void run() {
                def cl = { getDelegate() }                          // <1>
                def cl2 = { delegate }                              // <2>
                assert cl() == cl2()                                // <3>
                assert cl() == this                                 // <4>
                def enclosed = {
                    { -> delegate }.call()                          // <5>
                }
                assert enclosed() == enclosed                       // <6>
            }
        }
        // end::delegate_is_owner[]
        new Enclosing().run()
        '''

        assertScript '''
            // tag::change_delegate_classes[]
            class Person {
                String name
            }
            class Thing {
                String name
            }

            def p = new Person(name: 'Norman')
            def t = new Thing(name: 'Teapot')

            // end::change_delegate_classes[]

            // tag::change_delegate_closure[]
            def upperCasedName = { delegate.name.toUpperCase() }
            // end::change_delegate_closure[]

            // tag::change_delegate_asserts[]
            upperCasedName.delegate = p
            assert upperCasedName() == 'NORMAN'
            upperCasedName.delegate = t
            assert upperCasedName() == 'TEAPOT'
            // end::change_delegate_asserts[]

            // tag::delegate_alernative[]
            def target = p
            def upperCasedNameUsingVar = { target.name.toUpperCase() }
            assert upperCasedNameUsingVar() == 'NORMAN'
            // end::delegate_alernative[]

        '''
    }

    void testDelegationStrategy() {
        assertScript '''
                // tag::delegation_strategy_intro[]
                class Person {
                    String name
                }
                def p = new Person(name:'Igor')
                def cl = { name.toUpperCase() }                 // <1>
                cl.delegate = p                                 // <2>
                assert cl() == 'IGOR'                           // <3>
                // end::delegation_strategy_intro[]
            '''
    }

    void testOwnerFirst() {
        assertScript '''
            // tag::closure_owner_first[]
            class Person {
                String name
                def pretty = { "My name is $name" }             // <1>
                String toString() {
                    pretty()
                }
            }
            class Thing {
                String name                                     // <2>
            }

            def p = new Person(name: 'Sarah')
            def t = new Thing(name: 'Teapot')

            assert p.toString() == 'My name is Sarah'           // <3>
            p.pretty.delegate = t                               // <4>
            assert p.toString() == 'My name is Sarah'           // <5>
            // end::closure_owner_first[]

            // tag::closure_delegate_first[]
            p.pretty.resolveStrategy = Closure.DELEGATE_FIRST
            assert p.toString() == 'My name is Teapot'
            // end::closure_delegate_first[]
        '''
    }

    void testDelegateOnly() {
        assertScript '''
            // tag::delegate_only[]
            class Person {
                String name
                int age
                def fetchAge = { age }
            }
            class Thing {
                String name
            }

            def p = new Person(name:'Jessica', age:42)
            def t = new Thing(name:'Printer')
            def cl = p.fetchAge
            cl.delegate = p
            assert cl() == 42
            cl.delegate = t
            assert cl() == 42
            cl.resolveStrategy = Closure.DELEGATE_ONLY
            cl.delegate = p
            assert cl() == 42
            cl.delegate = t
            try {
            cl()
                assert false
            } catch (MissingPropertyException ex) {
                // "age" is not defined on the delegate
            }
            // end::delegate_only[]
        '''
    }
}
