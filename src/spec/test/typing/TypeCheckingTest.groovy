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

package typing

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * This unit test contains both assertScript and new GroovyShell().evaluate
 * calls. It is important *not* to replace the evaluate calls with assertScript, or the semantics
 * of the tests would be very different!
 */
class TypeCheckingTest extends StaticTypeCheckingTestCase {

    void testIntroduction() {
        new GroovyShell().evaluate '''
        // tag::stc_intro_magic[]
        Person.metaClass.getFormattedName = { "$delegate.firstName $delegate.lastName" }
        // end::stc_intro_magic[]
        // tag::stc_intro[]
        class Person {                                                          // <1>
            String firstName
            String lastName
        }
        def p = new Person(firstName: 'Raymond', lastName: 'Devos')             // <2>
        assert p.formattedName == 'Raymond Devos'                               // <3>
        // end::stc_intro[]
        '''
    }

    void testTypeCheckedAnnotation() {
        def shell = new GroovyShell()
        shell.evaluate '''
        // tag::typechecked_class[]
        @groovy.transform.TypeChecked
        class Calculator {
            int sum(int x, int y) { x+y }
        }
        // end::typechecked_class[]
        assert new Calculator().sum(4,5) == 9
        '''
        shell.evaluate '''
        // tag::typechecked_method[]
        class Calculator {
            @groovy.transform.TypeChecked
            int sum(int x, int y) { x+y }
        }
        // end::typechecked_method[]
        assert new Calculator().sum(4,5) == 9
        '''
    }

    void testTypeCheckingAssignmentRules() {
        assertScript '''
            // tag::stc_assign_equals[]
            Date now = new Date()
            // end::stc_assign_equals[]
        '''
        assertScript '''
            // tag::stc_assign_specialcase[]
            String s = new Date() // implicit call to toString
            Boolean boxed = 'some string'       // Groovy truth
            boolean prim = 'some string'        // Groovy truth
            Class clazz = 'java.lang.String'    // class coercion
            // end::stc_assign_specialcase[]
        '''

        assertScript '''
            // tag::stc_assign_null[]
            String s = null         // passes
            // end::stc_assign_null[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_null2prim[]
            int i = null            // fails
            // end::stc_assign_null2prim[]
        ''', 'Cannot assign value of type java.lang.Object to variable of type int'

        assertScript '''
            // tag::stc_assign_array[]
            int[] i = new int[4]        // passes
            // end::stc_assign_array[]
        '''

        assertScript '''
            // tag::stc_assign_array_list[]
            int[] i = [1,2,3]               // passes
            // end::stc_assign_array_list[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_array_fail[]
            int[] i = new String[4]     // fails
            // end::stc_assign_array_fail[]
        ''', 'Cannot assign value of type java.lang.String[] to variable of type int[]'

        shouldFailWithMessages '''
            // tag::stc_assign_array_list_fail[]
            int[] i = [1,2, new Date()]     // fails
            // end::stc_assign_array_list_fail[]
        ''', 'Cannot assign value of type java.util.Date into array of type int[]'

        assertScript '''
            // tag::stc_assign_superclass[]
            AbstractList list = new ArrayList()     // passes
            // end::stc_assign_superclass[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_superclass_fail[]
            LinkedList list = new ArrayList()       // fails
            // end::stc_assign_superclass_fail[]
        ''', 'Cannot assign value of type java.util.ArrayList to variable of type java.util.LinkedList'

        assertScript '''
            // tag::stc_assign_interface[]
            List list = new ArrayList()             // passes
            // end::stc_assign_interface[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_interface_fail[]
            RandomAccess list = new LinkedList()    // fails
            // end::stc_assign_interface_fail[]
        ''', 'Cannot assign value of type java.util.LinkedList to variable of type java.util.RandomAccess'

        assertScript '''
            // tag::stc_assign_prim[]
            int i = 0
            Integer bi = 1
            int x = new Integer(123)
            double d = new Float(5f)
            // end::stc_assign_prim[]
        '''

        assertScript '''
            // tag::stc_closure_coercion[]
            Runnable r = { println 'Hello' }
            interface SAMType {
                int doSomething()
            }
            SAMType sam = { 123 }
            assert sam.doSomething() == 123
            abstract class AbstractSAM {
                int calc() { 2* value() }
                abstract int value()
            }
            AbstractSAM c = { 123 }
            assert c.calc() == 246
            // end::stc_closure_coercion[]
        '''

        assertScript '''
            // tag::stc_assign_to_double[]
            Double d1 = 4d
            Double d2 = 4f
            Double d3 = 4l
            Double d4 = 4i
            Double d5 = (short) 4
            Double d6 = (byte) 4
            // end::stc_assign_to_double[]
        '''

        assertScript '''
            // tag::stc_assign_to_float[]
            Float f1 = 4f
            Float f2 = 4l
            Float f3 = 4i
            Float f4 = (short) 4
            Float f5 = (byte) 4
            // end::stc_assign_to_float[]
        '''

        assertScript '''
            // tag::stc_assign_to_long[]
            Long l1 = 4l
            Long l2 = 4i
            Long l3 = (short) 4
            Long l4 = (byte) 4
            // end::stc_assign_to_long[]
        '''

        assertScript '''
            // tag::stc_assign_to_int[]
            Integer i1 = 4i
            Integer i2 = (short) 4
            Integer i3 = (byte) 4
            // end::stc_assign_to_int[]
        '''

        assertScript '''
            // tag::stc_assign_to_short[]
            Short s1 = (short) 4
            Short s2 = (byte) 4
            // end::stc_assign_to_short[]
        '''

        assertScript '''
            // tag::stc_assign_to_byte[]
            Byte b1 = (byte) 4
            // end::stc_assign_to_byte[]
        '''
    }

    void testGroovyConstructors() {
        assertScript '''
            // tag::stc_ctor_point_classic[]
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }
            Person classic = new Person('Ada','Lovelace')
            // end::stc_ctor_point_classic[]
            // tag::stc_ctor_point_list[]
            Person list = ['Ada','Lovelace']
            // end::stc_ctor_point_list[]
            // tag::stc_ctor_point_map[]
            Person map = [firstName:'Ada', lastName:'Lovelace']
            // end::stc_ctor_point_map[]
        '''
        shouldFailWithMessages '''
            // tag::stc_ctor_fail[]
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }
            Person map = [firstName:'Ada', lastName:'Lovelace', age: 24]     // <1>
            // end::stc_ctor_fail[]
        ''', 'No such property: age for class: Person'
    }

    void testMatchArgumentsWithParameters() {
        assertScript '''
            // tag::stc_argparam_equals[]
            int sum(int x, int y) {
                x+y
            }
            assert sum(3,4) == 7
            // end::stc_argparam_equals[]
        '''
        assertScript '''
            // tag::stc_argparam_specialcase[]
            String format(String str) {
                "Result: $str"
            }
            assert format("${3+4}") == "Result: 7"
            // end::stc_argparam_specialcase[]
        '''

        assertScript '''
            // tag::stc_argparam_null[]
            String format(int value) {
                "Result: $value"
            }
            assert format(7) == "Result: 7"
            // end::stc_argparam_null[]
        '''

        shouldFailWithMessages '''
            String format(int value) {
                "Result: $value"
            }
            // tag::stc_argparam_null2prim[]
            format(null)           // fails
            // end::stc_argparam_null2prim[]
        ''', '#format(int) with arguments [<unknown parameter type>]'

        assertScript '''
            // tag::stc_argparam_array[]
            String format(String[] values) {
                "Result: ${values.join(' ')}"
            }
            assert format(['a','b'] as String[]) == "Result: a b"
            // end::stc_argparam_array[]
        '''

        shouldFailWithMessages '''
            String format(String[] values) {
                "Result: ${values.join(' ')}"
            }
            // tag::stc_argparam_array_fail[]
            format([1,2] as int[])              // fails
            // end::stc_argparam_array_fail[]
        ''', '#format(int[]). Please check if the declared type is correct and if the method exists.'


        assertScript '''
            // tag::stc_argparam_superclass[]
            String format(AbstractList list) {
                list.join(',')
            }
            format(new ArrayList())              // passes
            // end::stc_argparam_superclass[]
        '''

        shouldFailWithMessages '''
            // tag::stc_argparam_superclass_fail[]
            String format(LinkedList list) {
                list.join(',')
            }
            format(new ArrayList())              // fails
            // end::stc_argparam_superclass_fail[]
        ''', '#format(java.util.ArrayList). Please check if the declared type is correct and if the method exists.'

        assertScript '''
            // tag::stc_argparam_interface[]
            String format(List list) {
                list.join(',')
            }
            format(new ArrayList())                  // passes
            // end::stc_argparam_interface[]
        '''

        shouldFailWithMessages '''
            // tag::stc_argparam_interface_fail[]
            String format(RandomAccess list) {
                'foo'
            }
            format(new LinkedList())                 // fails
            // end::stc_argparam_interface_fail[]
        ''', '#format(java.util.LinkedList). Please check if the declared type is correct and if the method exists.'

        assertScript '''
            // tag::stc_argparam_prim[]
            int sum(int x, Integer y) {
                x+y
            }
            assert sum(3, new Integer(4)) == 7
            assert sum(new Integer(3), 4) == 7
            assert sum(new Integer(3), new Integer(4)) == 7
            assert sum(new Integer(3), 4) == 7
            // end::stc_argparam_prim[]
        '''

        assertScript '''
            // tag::stc_arg_closure_coercion[]
            interface SAMType {
                int doSomething()
            }
            int twice(SAMType sam) { 2*sam.doSomething() }
            assert twice { 123 } == 246
            abstract class AbstractSAM {
                int calc() { 2* value() }
                abstract int value()
            }
            int eightTimes(AbstractSAM sam) { 4*sam.calc() }
            assert eightTimes { 123 } == 984
            // end::stc_arg_closure_coercion[]
        '''

    }

    void testNoMatchingMethodError() {
        new GroovyShell().parse '''
            // tag::method_not_type_checked[]
            class MyService {
                void doSomething() {
                    printLine 'Do something'            // <1>
                }
            }
            // end::method_not_type_checked[]
        '''
        shouldFailWithMessages '''
            // tag::method_type_checked[]
            @groovy.transform.TypeChecked
            class MyService {
                void doSomething() {
                    printLine 'Do something'            // <1>
                }
            }
            // end::method_type_checked[]
        ''','Cannot find matching method MyService#printLine(java.lang.String)'
    }

    void testDuckTypingShouldFailWithTypeChecked() {
        shouldFailWithMessages '''
            // tag::ducktyping_failure[]
            class Duck {
                void quack() {              // <1>
                    println 'Quack!'
                }
            }
            class QuackingBird {
                void quack() {              // <2>
                    println 'Quack!'
                }
            }
            @groovy.transform.TypeChecked
            void accept(quacker) {
                quacker.quack()             // <3>
            }
            accept(new Duck())              // <4>
            // end::ducktyping_failure[]
        ''', 'Cannot find matching method java.lang.Object#quack()'
    }

    void testTypeInference() {
        assertScript '''
        // tag::simple_var_type_inference[]
        def message = 'Welcome to Groovy!'              // <1>
        println message.toUpperCase()                   // <2>
        // end::simple_var_type_inference[]
        '''
        shouldFailWithMessages '''
        def message = 'Welcome to Groovy!'
        // tag::simple_var_type_inference_fail[]
        println message.upper() // compile time error   <3>
        // end::simple_var_type_inference_fail[]
        ''', 'Cannot find matching method java.lang.String#upper()'
    }

    void testCollectionLiteralInference() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            assert node.getNodeMetaData(INFERRED_TYPE) == make(List)
        })
        // tag::empty_list_literal_inference[]
        def list = []
        // end::empty_list_literal_inference[]
        '''

        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            def inft = node.getNodeMetaData(INFERRED_TYPE)
            assert inft == make(List)
            assert inft.genericsTypes[0].type == make(String)
        })
        // tag::list_literal_inference_simple[]
        def list = ['foo','bar']
        // end::list_literal_inference_simple[]
        '''

        assertScript '''
        def foo = 1
        def bar = 2
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            def inft = node.getNodeMetaData(INFERRED_TYPE)
            assert inft == make(List)
            assert inft.genericsTypes[0].type == make(GString)
        })
        // tag::list_literal_inference_gstring[]
        def list = ["${foo}","${bar}"]
        // end::list_literal_inference_gstring[]
        '''
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            assert node.getNodeMetaData(INFERRED_TYPE) == make(LinkedHashMap)
        })
        // tag::empty_map_literal_inference[]
        def map = [:]
        // end::empty_map_literal_inference[]
        '''

        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
             def inft = node.getNodeMetaData(INFERRED_TYPE)
             assert inft == make(LinkedHashMap)
             assert inft.genericsTypes[0].type == make(String)
             assert inft.genericsTypes[1].type == make(String)
        })
        // tag::map_literal_inference_simple[]
        def map1 = [someKey: 'someValue']
        def map2 = ['someKey': 'someValue']
        // end::map_literal_inference_simple[]
        '''

        assertScript '''
        def someKey = 123
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
             def inft = node.getNodeMetaData(INFERRED_TYPE)
             assert inft == make(LinkedHashMap)
             assert inft.genericsTypes[0].type == make(GString)
             assert inft.genericsTypes[1].type == make(String)
        })
        // tag::map_literal_inference_gstring[]
        def map = ["${someKey}": 'someValue']
        // end::map_literal_inference_gstring[]
        '''

        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            assert node.getNodeMetaData(INFERRED_TYPE) == make(IntRange)
        })
        // tag::intRange_literal_inference[]
        def intRange = (0..10)
        // end::intRange_literal_inference[]
        '''

        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            def inft = node.getNodeMetaData(INFERRED_TYPE)
            assert inft == make(Range)
            assert inft.genericsTypes[0].type == make(String)
        })
        // tag::charRange_literal_inference[]
        def charRange = ('a'..'z')
        // end::charRange_literal_inference[]
        '''

    }

    void testTypeInferenceFieldVsLocalVariable() {
        shouldFailWithMessages '''
            // tag::typeinference_field_vs_local_variable[]
            class SomeClass {
                def someUntypedField                                                                // <1>
                String someTypedField                                                               // <2>

                void someMethod() {
                    someUntypedField = '123'                                                        // <3>
                    someUntypedField = someUntypedField.toUpperCase()  // compile-time error        // <4>
                }

                void someSafeMethod() {
                    someTypedField = '123'                                                          // <5>
                    someTypedField = someTypedField.toUpperCase()                                   // <6>
                }

                void someMethodUsingLocalVariable() {
                    def localVariable = '123'                                                       // <7>
                    someUntypedField = localVariable.toUpperCase()                                  // <8>
                }
            }
            // end::typeinference_field_vs_local_variable[]
            SomeClass
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testLeastUpperBound() {
        assertScript '''import org.codehaus.groovy.ast.ClassHelper

            import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound

            Class leastUpperBound(Class a, Class b) {
                lowestUpperBound(ClassHelper.make(a), ClassHelper.make(b)).typeClass
            }

            // tag::least_upper_bound_simple[]
            class Top {}
            class Bottom1 extends Top {}
            class Bottom2 extends Top {}

            assert leastUpperBound(String, String) == String                    // <1>
            assert leastUpperBound(ArrayList, LinkedList) == AbstractList       // <2>
            assert leastUpperBound(ArrayList, List) == List                     // <3>
            assert leastUpperBound(List, List) == List                          // <4>
            assert leastUpperBound(Bottom1, Bottom2) == Top                     // <5>
            assert leastUpperBound(List, Serializable) == Object                // <6>
            // end::least_upper_bound_simple[]
        '''

        assertScript '''import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound as leastUpperBound

            // tag::least_upper_bound_complex[]
            interface Foo {}
            class Top {}
            class Bottom extends Top implements Serializable, Foo {}
            class SerializableFooImpl implements Serializable, Foo {}
            // end::least_upper_bound_complex[]

            def lub = leastUpperBound(ClassHelper.make(Bottom), ClassHelper.make(SerializableFooImpl))
            assert lub instanceof ClassNode
            assert lub.superClass == ClassHelper.OBJECT_TYPE
            assert lub.interfaces.size() == 3 // because of Serializable, Foo and GroovyObject
            assert (lub.interfaces as Set).containsAll([ClassHelper.make(Serializable), ClassHelper.make(Foo)])
        '''
    }

    void testLUBInferenceInCombinationWithMethodCall() {
        shouldFailWithMessages '''
            // tag::least_upper_bound_collection_inference[]
            interface Greeter { void greet() }                  // <1>
            interface Salute { void salute() }                  // <2>

            class A implements Greeter, Salute {                // <3>
                void greet() { println "Hello, I'm A!" }
                void salute() { println "Bye from A!" }
            }
            class B implements Greeter, Salute {                // <4>
                void greet() { println "Hello, I'm B!" }
                void salute() { println "Bye from B!" }
                void exit() { println 'No way!' }               // <5>
            }
            def list = [new A(), new B()]                       // <6>
            list.each {
                it.greet()                                      // <7>
                it.salute()                                     // <8>
                it.exit()                                       // <9>
            }
            // end::least_upper_bound_collection_inference[]
        ''', '[Static type checking] - Cannot find matching method Greeter or Salute#exit()'
    }

    void testInstanceOfInference() {
        assertScript '''
            // tag::instanceof_inference[]
            class Greeter {
                String greeting() { 'Hello' }
            }

            void doSomething(def o) {
                if (o instanceof Greeter) {     // <1>
                    println o.greeting()        // <2>
                }
            }

            doSomething(new Greeter())
            // end::instanceof_inference[]
            /*
            // tag::instanceof_java_equiv[]
            if (o instanceof Greeter) {
                System.out.println(((Greeter)o).greeting());
            }
            // end::instanceof_java_equiv[]
            */
        '''
    }

    void testFlowTyping() {
        new GroovyShell().evaluate '''
            // tag::flowtyping_basics[]
            @groovy.transform.TypeChecked
            void flowTyping() {
                def o = 'foo'                       // <1>
                o = o.toUpperCase()                 // <2>
                o = 9d                              // <3>
                o = Math.sqrt(o)                    // <4>
            }
            // end::flowtyping_basics[]
            flowTyping()
        '''
        shouldFailWithMessages '''
            def o
            // tag::flowtyping_basics_fail[]
            o = 9d
            o = o.toUpperCase()
            // end::flowtyping_basics_fail[]

        ''', 'toUpperCase'
    }

    void testFlowTypingTypeConstraints() {
        shouldFailWithMessages '''
            // tag::flowtyping_typeconstraints[]
            @groovy.transform.TypeChecked
            void flowTypingWithExplicitType() {
                List list = ['a','b','c']           // <1>
                list = list*.toUpperCase()          // <2>
                list = 'foo'                        // <3>
            }
            // end::flowtyping_typeconstraints[]
            flowTypingWithExplicitType()
        ''', 'Cannot assign value of type java.lang.String to variable of type java.util.List'
    }

    void testFlowTypingTypeConstraintsFailure() {
        shouldFailWithMessages '''
            // tag::flowtyping_typeconstraints_failure[]
            @groovy.transform.TypeChecked
            void flowTypingWithExplicitType() {
                List list = ['a','b','c']           // <1>
                list.add(1)                         // <2>
            }
            // end::flowtyping_typeconstraints_failure[]
            flowTypingWithExplicitType()
        ''', '[Static type checking] - Cannot find matching method java.util.List#add(int)'

        assertScript '''
            // tag::flowtyping_typeconstraints_fixed[]
            @groovy.transform.TypeChecked
            void flowTypingWithExplicitType() {
                List<? extends Serializable> list = []                      // <1>
                list.addAll(['a','b','c'])                                  // <2>
                list.add(1)                                                 // <3>
            }
            // end::flowtyping_typeconstraints_fixed[]
            flowTypingWithExplicitType()
        '''
    }

    void testFlowTypingMethodSelectionGroovy() {
        new GroovyShell().evaluate '''
            // tag::groovy_method_selection[]
            int compute(String string) { string.length() }
            String compute(Object o) { "Nope" }
            Object o = 'string'
            def result = compute(o)
            println result
            // end::groovy_method_selection[]
            assert result == 6
        '''
    }

    void testIfElse() {
        shouldFailWithMessages '''
            // tag::flow_lub_ifelse_header[]
            class Top {
               void methodFromTop() {}
            }
            class Bottom extends Top {
               void methodFromBottom() {}
            }
            // end::flow_lub_ifelse_header[]
            [true, false].each { someCondition ->
                // tag::flow_lub_ifelse_test[]
                def o
                if (someCondition) {
                    o = new Top()                               // <1>
                } else {
                    o = new Bottom()                            // <2>
                }
                o.methodFromTop()                               // <3>
                o.methodFromBottom()  // compilation error      // <4>
                // end::flow_lub_ifelse_test[]
            }
        ''','Cannot find matching method Top#methodFromBottom()'
    }

    void testClosureSharedVariable(){
        assertScript '''
            // tag::closure_shared_variable_definition[]
            def text = 'Hello, world!'                          // <1>
            def closure = {
                println text                                    // <2>
            }
            // end::closure_shared_variable_definition[]
        '''

        assertScript '''
            void doSomething(Closure cl) { cl('hello') }
            // tag::closure_shared_variable_ex1[]
            String result
            doSomething { String it ->
                result = "Result: $it"
            }
            result = result?.toUpperCase()
            // end::closure_shared_variable_ex1[]
        '''

        shouldFailWithMessages '''
            // tag::closure_shared_variable_ex2[]
            class Top {
               void methodFromTop() {}
            }
            class Bottom extends Top {
               void methodFromBottom() {}
            }
            def o = new Top()                               // <1>
            Thread.start {
                o = new Bottom()                            // <2>
            }
            o.methodFromTop()                               // <3>
            o.methodFromBottom()  // compilation error      // <4>
            // end::closure_shared_variable_ex2[]
            ''','Cannot find matching method Top#methodFromBottom()'
    }

    void testClosureReturnTypeInference() {
        assertScript '''
            // tag::closure_return_type_inf[]
            @groovy.transform.TypeChecked
            int testClosureReturnTypeInference(String arg) {
                def cl = { "Arg: $arg" }                                // <1>
                def val = cl()                                          // <2>

                val.length()                                            // <3>
            }
            // end::closure_return_type_inf[]
            assert testClosureReturnTypeInference('foo') == 8
        '''
    }

    void testShouldNotRelyOnMethodReturnTypeInference() {
        shouldFailWithMessages '''import groovy.transform.TypeChecked
            // tag::method_return_type_matters[]
            @TypeChecked
            class A {
                def compute() { 'some string' }             // <1>
                def computeFully() {
                    compute().toUpperCase()                 // <2>
                }
            }
            @TypeChecked
            class B extends A {
                def compute() { 123 }                       // <3>
            }
            // end::method_return_type_matters[]
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testClosureParameterTypeInference() {
        shouldFailWithMessages '''
        // tag::cl_pt_failure[]
        class Person {
            String name
            int age
        }

        void inviteIf(Person p, Closure<Boolean> predicate) {           // <1>
            if (predicate.call(p)) {
                // send invite
                // ...
            }
        }

        @groovy.transform.TypeChecked
        void failCompilation() {
            Person p = new Person(name: 'Gerard', age: 55)
            inviteIf(p) {                                               // <2>
                it.age >= 18 // No such property: age                   // <3>
            }
        }
        // end::cl_pt_failure[]
        ''', 'No such property: age for class: java.lang.Object', 'Cannot find matching method'

        assertScript '''
        class Person {
            String name
            int age
        }

        void inviteIf(Person p, Closure<Boolean> predicate) {
            if (predicate.call(p)) {
                // send invite
                // ...
            }
        }

        @groovy.transform.TypeChecked
        void passesCompilation() {
            Person p = new Person(name: 'Gerard', age: 55)

            // tag::cl_pt_workaround[]
            inviteIf(p) { Person it ->                                  // <1>
                it.age >= 18
            }
            // end::cl_pt_workaround[]
        }
        '''

        assertScript '''
        class Person {
            String name
            int age
        }

        // tag::cl_pt_workaround_sam[]
        interface Predicate<On> { boolean apply(On e) }                 // <1>

        void inviteIf(Person p, Predicate<Person> predicate) {          // <2>
            if (predicate.apply(p)) {
                // send invite
                // ...
            }
        }

        @groovy.transform.TypeChecked
        void passesCompilation() {
            Person p = new Person(name: 'Gerard', age: 55)

            inviteIf(p) {                                               // <3>
                it.age >= 18                                            // <4>
            }
        }
        // end::cl_pt_workaround_sam[]
        '''

        assertScript '''
// tag::cl_pt_workaround_closureparams_imports[]
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
// end::cl_pt_workaround_closureparams_imports[]

        class Person {
            String name
            int age
        }

        // tag::cl_pt_workaround_closureparams_method[]
        void inviteIf(Person p, @ClosureParams(FirstParam) Closure<Boolean> predicate) {        // <1>
            if (predicate.call(p)) {
                // send invite
                // ...
            }
        }
        // end::cl_pt_workaround_closureparams_method[]

        @groovy.transform.TypeChecked
        void passesCompilation() {
            Person p = new Person(name: 'Gerard', age: 55)

            // tag::cl_pt_workaround_closureparams_call[]
            inviteIf(p) {                                                                       // <2>
                it.age >= 18
            }
            // end::cl_pt_workaround_closureparams_call[]
        }
        '''
    }

    void testSkip() {
        def shell = new GroovyShell()
        shell.evaluate '''
            class SentenceBuilder {
                StringBuilder sb = new StringBuilder()
                def methodMissing(String name, args) {
                    if (sb) sb.append(' ')
                    sb.append(name)
                    this
                }

                def propertyMissing(String name) {
                    if (sb) sb.append(' ')
                    sb.append(name)
                    this
                }
                String toString() { sb }
            }

            // tag::stc_skip[]
            import groovy.transform.TypeChecked
            import groovy.transform.TypeCheckingMode

            @TypeChecked                                        // <1>
            class GreetingService {
                String greeting() {                             // <2>
                    doGreet()
                }

                @TypeChecked(TypeCheckingMode.SKIP)             // <3>
                private String doGreet() {
                    def b = new SentenceBuilder()
                    b.Hello.my.name.is.John                     // <4>
                    b
                }
            }
            def s = new GreetingService()
            assert s.greeting() == 'Hello my name is John'
            // end::stc_skip[]
            '''
    }
}

