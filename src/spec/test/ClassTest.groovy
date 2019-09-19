import groovy.test.GroovyTestCase

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
class ClassTest extends GroovyTestCase {

    void testClassDefinition() {
        assertScript '''
            // tag::class_definition[]
            class Person {                       //<1>

                String name                      //<2>
                Integer age

                def increaseAge(Integer years) { //<3>
                    this.age += years
                }
            }
            // end::class_definition[]

            // tag::class_instantiation[]
            def p = new Person()
            // end::class_instantiation[]

        '''
    }

    void testInnerClass() {
        assertScript '''
            // tag::inner_class[]
            class Outer {
                private String privateStr

                def callInnerMethod() {
                    new Inner().methodA()       //<1>
                }

                class Inner {                   //<2>
                    def methodA() {
                        println "${privateStr}." //<3>
                    }
                }
            }
            // end::inner_class[]
            def o = new Outer()
        '''
    }

    void testInnerClass2() {
        assertScript '''
            // tag::inner_class2[]
            class Outer2 {
                private String privateStr = 'some string'

                def startThread() {
                   new Thread(new Inner2()).start()
                }

                class Inner2 implements Runnable {
                    void run() {
                        println "${privateStr}."
                    }
                }
            }
            // end::inner_class2[]
            def o2 = new Outer2()
        '''
    }

    void testAnonymousInnerClass() {
        assertScript '''
            // tag::anonymous_inner_class[]
            class Outer3 {
                private String privateStr = 'some string'

                def startThread() {
                    new Thread(new Runnable() {      //<1>
                        void run() {
                            println "${privateStr}."
                        }
                    }).start()                       //<2>
                }
            }
            // end::anonymous_inner_class[]
            def o3 = new Outer3()
        '''
    }

    void testAbstractClass() {
        assertScript '''
            // tag::abstract_class[]
            abstract class Abstract {         //<1>
                String name

                abstract def abstractMethod() //<2>

                def concreteMethod() {
                    println 'concrete'
                }
            }
            // end::abstract_class[]
            def ac = new Abstract() { def abstractMethod() {} }
        '''
    }

    void testConstructorPositionalParameters() {
        assertScript '''
            // tag::constructor_positional_parameters[]
            class PersonConstructor {
                String name
                Integer age

                PersonConstructor(name, age) {          //<1>
                    this.name = name
                    this.age = age
                }
            }

            def person1 = new PersonConstructor('Marie', 1)  //<2>
            def person2 = ['Marie', 2] as PersonConstructor  //<3>
            PersonConstructor person3 = ['Marie', 3]         //<4>
            // end::constructor_positional_parameters[]

            assert person1.name == 'Marie'
            assert person2.name == 'Marie'
            assert person3.name == 'Marie'
        '''
    }

    void testConstructorNamedParameters() {
        assertScript '''
            // tag::constructor_named_parameters[]
            class PersonWOConstructor {                                  //<1>
                String name
                Integer age
            }

            def person4 = new PersonWOConstructor()                      //<2>
            def person5 = new PersonWOConstructor(name: 'Marie')         //<3>
            def person6 = new PersonWOConstructor(age: 1)                //<4>
            def person7 = new PersonWOConstructor(name: 'Marie', age: 2) //<5>
            // end::constructor_named_parameters[]

            assert person4.name == null
        '''
    }

    void testInterfaceDefinition() {
        assertScript '''
            // tag::interface_def_1[]
            interface Greeter {                                         // <1>
                void greet(String name)                                 // <2>
            }
            // end::interface_def_1[]

            // tag::class_implements[]
            class SystemGreeter implements Greeter {                    // <1>
                void greet(String name) {                               // <2>
                    println "Hello $name"
                }
            }

            def greeter = new SystemGreeter()
            assert greeter instanceof Greeter                           // <3>
            // end::class_implements[]
            greeter.greet('Laura')

            // tag::extended_interface[]
            interface ExtendedGreeter extends Greeter {                 // <1>
                void sayBye(String name)
            }
            // end::extended_interface[]

            // tag::no_structural_interface[]
            class DefaultGreeter {
                void greet(String name) { println "Hello" }
            }

            greeter = new DefaultGreeter()
            assert !(greeter instanceof Greeter)
            // end::no_structural_interface[]

            def coerced
            // tag::interface_coercion[]
            greeter = new DefaultGreeter()                              // <1>
            coerced = greeter as Greeter                                // <2>
            assert coerced instanceof Greeter                           // <3>
            // end::interface_coercion[]
        '''

        def err = shouldFail {
            assertScript '''
                // tag::protected_forbidden[]
                interface Greeter {
                    protected void greet(String name)           // <1>
                }
                // end::protected_forbidden[]
                1
            '''
        }
        assert err.contains("Method 'greet' is protected but should be public in interface 'Greeter'")

        err = shouldFail {
            assertScript '''
                // tag::private_forbidden[]
                interface Greeter {
                    private void greet(String name)
                }
                // end::private_forbidden[]
                1
            '''
        }
        assert err.contains("Method 'greet' is private but should be public in interface 'Greeter'")
    }


    void testFields() {
        assertScript '''
            // tag::field_declaration[]
            class Data {
                private int id                                  // <1>
                protected String description                    // <2>
                public static final boolean DEBUG = false       // <3>
            }
            // end::field_declaration[]
            def d = new Data()
        '''
        assertScript '''
            class IDGenerator { static int next() {0} }
            // tag::field_initialization[]
            class Data {
                private String id = IDGenerator.next() // <1>
                // ...
            }
            // end::field_initialization[]
            new Data()
        '''

        assertScript '''
            // tag::typing_fields[]
            class BadPractice {
                private mapping                         // <1>
            }
            class GoodPractice {
                private Map<String,String> mapping      // <2>
            }
            // end::typing_fields[]
            BadPractice
        '''
    }

    void testProperties() {
        assertScript '''
            // tag::properties_definition[]
            class Person {
                String name                             // <1>
                int age                                 // <2>
            }
            // end::properties_definition[]

            assert Person.declaredFields.name.containsAll (['name','age'])
            assert Person.getDeclaredMethod('getName')
            assert Person.getDeclaredMethod('getAge')
            assert Person.getDeclaredMethod('setName',String)
            assert Person.getDeclaredMethod('setAge',int)
        '''

        assertScript '''
            // tag::readonly_property[]
            class Person {
                final String name                   // <1>
                final int age                       // <2>
                Person(String name, int age) {
                    this.name = name                // <3>
                    this.age = age                  // <4>
                }
            }
            // end::readonly_property[]

            def p = new Person('Bob', 42)
            assert Person.declaredFields.name.containsAll (['name','age'])
            assert Person.getDeclaredMethod('getName')
            assert Person.getDeclaredMethod('getAge')
            try {
                assert Person.getDeclaredMethod('setName',String) == null
            } catch (NoSuchMethodException e) {

            }
            try {
                assert Person.getDeclaredMethod('setAge',int) == null
            } catch (NoSuchMethodException e) {

            }

        '''

        assertScript '''
            // tag::property_access[]
            class Person {
                String name
                void name(String name) {
                    this.name = "Wonder$name"       // <1>
                }
                String wonder() {
                    this.name                       // <2>
                }
            }
            def p = new Person()
            p.name = 'Marge'                        // <3>
            assert p.name == 'Marge'                // <4>
            p.name('Marge')                         // <5>
            assert p.wonder() == 'WonderMarge'      // <6>
            // end::property_access[]
        '''

        assertScript '''
            // tag::properties_meta[]
            class Person {
                String name
                int age
            }
            def p = new Person()
            assert p.properties.keySet().containsAll(['name','age'])
            // end::properties_meta[]

        '''

        assertScript '''
            // tag::pseudo_properties[]
            class PseudoProperties {
                // a pseudo property "name"
                void setName(String name) {}
                String getName() {}

                // a pseudo read-only property "age"
                int getAge() { 42 }

                // a pseudo write-only property "groovy"
                void setGroovy(boolean groovy) {  }
            }
            def p = new PseudoProperties()
            p.name = 'Foo'                      // <1>
            assert p.age == 42                  // <2>
            p.groovy = true                     // <3>
            // end::pseudo_properties[]
        '''
    }


    void testDefineAnnotation() {
        assertScript '''
            // tag::define_annotation[]
            @interface SomeAnnotation {}
            // end::define_annotation[]
            SomeAnnotation
        '''
    }

    void testAnnotationMembers() {
        assertScript '''
            // tag::ann_member_string[]
            @interface SomeAnnotation {
                String value()                          // <1>
            }
            // end::ann_member_string[]
            SomeAnnotation
        '''
        assertScript '''
            // tag::ann_member_string_default[]
            @interface SomeAnnotation {
                String value() default 'something'      // <2>
            }
            // end::ann_member_string_default[]
            SomeAnnotation
        '''
        assertScript '''
            // tag::ann_member_int[]
            @interface SomeAnnotation {
                int step()                              // <3>
            }
            // end::ann_member_int[]
            SomeAnnotation
        '''
       assertScript '''
            // tag::ann_member_class[]
            @interface SomeAnnotation {
                Class appliesTo()                       // <4>
            }
            // end::ann_member_class[]
            SomeAnnotation
        '''
       assertScript '''
            // tag::ann_member_annotation[]
            @interface SomeAnnotation {}
            @interface SomeAnnotations {
                SomeAnnotation[] value()                // <5>
            }
            // end::ann_member_annotation[]
            SomeAnnotation
        '''
        assertScript '''
            // tag::ann_member_enum[]
            enum DayOfWeek { mon, tue, wed, thu, fri, sat, sun }
            @interface Scheduled {
                DayOfWeek dayOfWeek()                   // <6>
            }
            // end::ann_member_enum[]
            Scheduled
        '''
    }

    void testApplyAnnotation() {
        assertScript '''
            @interface SomeAnnotation {
                int value() default 0
            }

            // tag::apply_annotation_1[]
            @SomeAnnotation                 // <1>
            void someMethod() {
                // ...
            }

            @SomeAnnotation                 // <2>
            class SomeClass {}

            @SomeAnnotation String var      // <3>

            // end::apply_annotation_1[]
            someMethod()
        '''

        assertScript '''
            // tag::annotation_value_set[]
            @interface Page {
                int statusCode()
            }

            @Page(statusCode=404)
            void notFound() {
                // ...
            }
            // end::annotation_value_set[]
        '''

        assertScript '''
            // tag::annotation_value_set_option[]
            @interface Page {
                String value()
                int statusCode() default 200
            }

            @Page(value='/home')                    // <1>
            void home() {
                // ...
            }

            @Page('/users')                         // <2>
            void userList() {
                // ...
            }

            @Page(value='error',statusCode=404)     // <3>
            void notFound() {
                // ...
            }
            // end::annotation_value_set_option[]
        '''


    }

    void testAnnotationTarget() {
        assertScript '''
            // tag::ann_target[]
            import java.lang.annotation.ElementType
            import java.lang.annotation.Target

            @Target([ElementType.METHOD, ElementType.TYPE])     // <1>
            @interface SomeAnnotation {}                        // <2>
            // end::ann_target[]
            SomeAnnotation
        '''
    }

    void testAnnotationRetention() {
        assertScript '''
            // tag::ann_retention[]
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy

            @Retention(RetentionPolicy.SOURCE)                   // <1>
            @interface SomeAnnotation {}                         // <2>
            // end::ann_retention[]
            SomeAnnotation
        '''
    }

    void testClosureAsAnnotationValue() {
        assertScript '''
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.reflect.Modifier
            // tag::closure_ann_def[]
            @Retention(RetentionPolicy.RUNTIME)
            @interface OnlyIf {
                Class value()                    // <1>
            }
            // end::closure_ann_def[]

            // tag::closure_ann_example[]
            class Tasks {
                Set result = []
                void alwaysExecuted() {
                    result << 1
                }
                @OnlyIf({ jdk>=6 })
                void supportedOnlyInJDK6() {
                    result << 'JDK 6'
                }
                @OnlyIf({ jdk>=7 && windows })
                void requiresJDK7AndWindows() {
                    result << 'JDK 7 Windows'
                }
            }
            // end::closure_ann_example[]

            // tag::closure_ann_runner[]
            class Runner {
                static <T> T run(Class<T> taskClass) {
                    def tasks = taskClass.newInstance()                                         // <1>
                    def params = [jdk:6, windows: false]                                        // <2>
                    tasks.class.declaredMethods.each { m ->                                     // <3>
                        if (Modifier.isPublic(m.modifiers) && m.parameterTypes.length == 0) {   // <4>
                            def onlyIf = m.getAnnotation(OnlyIf)                                // <5>
                            if (onlyIf) {
                                Closure cl = onlyIf.value().newInstance(tasks,tasks)            // <6>
                                cl.delegate = params                                            // <7>
                                if (cl()) {                                                     // <8>
                                    m.invoke(tasks)                                             // <9>
                                }
                            } else {
                                m.invoke(tasks)                                                 // <10>
                            }
                        }
                    }
                    tasks                                                                       // <11>
                }
            }
            // end::closure_ann_runner[]

            // tag::closure_ann_runner_exec[]
            def tasks = Runner.run(Tasks)
            assert tasks.result == [1, 'JDK 6'] as Set
            // end::closure_ann_runner_exec[]
        '''
    }

    void testAnnotationCollector() {
        assertScript '''import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

            @Retention(RetentionPolicy.RUNTIME)
            @interface Service {}
            @Retention(RetentionPolicy.RUNTIME)
            @interface Transactional {}

            // tag::transactionalservice_class[]
            @Service
            @Transactional
            class MyTransactionalService {}
            // end::transactionalservice_class[]

            MyTransactionalService
        '''

        assertScript '''import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

            @Retention(RetentionPolicy.RUNTIME)
            @interface Service {}
            @Retention(RetentionPolicy.RUNTIME)
            @interface Transactional {}

            // tag::metaann_ts[]
            import groovy.transform.AnnotationCollector

            @Service                                        // <1>
            @Transactional                                  // <2>
            @AnnotationCollector                            // <3>
            @interface TransactionalService {
            }
            // end::metaann_ts[]

            // tag::transactionalservice_class2[]
            @TransactionalService                           // <1>
            class MyTransactionalService {}
            // end::transactionalservice_class2[]

            // tag::annotations_expanded[]
            def annotations = MyTransactionalService.annotations*.annotationType()
            assert (Service in annotations)
            assert (Transactional in annotations)
            // end::annotations_expanded[]
        '''
    }

    void testAnnotationCollectorWithParams() {
        assertScript '''
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import groovy.transform.AnnotationCollector

            @Retention(RetentionPolicy.RUNTIME)
            @interface Timeout { int after() }
            @Retention(RetentionPolicy.RUNTIME)
            @interface Dangerous { String type() }

            // tag::collected_ann_explosive[]
            @Timeout(after=3600)
            @Dangerous(type='explosive')
            // end::collected_ann_explosive[]
            // tag::collector_ann_explosive[]
            @AnnotationCollector
            public @interface Explosive {}
            // end::collector_ann_explosive[]

            // tag::example_bomb[]
            @Explosive(after=0)                 // <1>
            class Bomb {}
            // end::example_bomb[]
            @Explosive
            class ReferenceBomb {}

            assert Bomb.getAnnotation(Timeout).after() == 0
            assert ReferenceBomb.getAnnotation(Timeout).after() == 3600
        '''
    }

    void testAnnotationCollectorWithSameParamNames() {
        assertScript '''

import groovy.transform.AnnotationCollector

import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy

            // tag::collector_ann_same_values[]
            @Retention(RetentionPolicy.RUNTIME)
            public @interface Foo {
               String value()                                   // <1>
            }
            @Retention(RetentionPolicy.RUNTIME)
            public @interface Bar {
                String value()                                  // <2>
            }

            @Foo
            @Bar
            @AnnotationCollector
            public @interface FooBar {}                         // <3>

            @Foo('a')
            @Bar('b')
            class Bob {}                                        // <4>

            assert Bob.getAnnotation(Foo).value() == 'a'        // <5>
            println Bob.getAnnotation(Bar).value() == 'b'       // <6>

            @FooBar('a')
            class Joe {}                                        // <7>
            assert Joe.getAnnotation(Foo).value() == 'a'        // <8>
            println Joe.getAnnotation(Bar).value() == 'a'       // <9>
            // end::collector_ann_same_values[]
        '''

        def err = shouldFail '''

            import groovy.transform.AnnotationCollector

            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy

            @Retention(RetentionPolicy.RUNTIME)
            public @interface Foo {
               String value()
            }
            @Retention(RetentionPolicy.RUNTIME)
            public @interface Bar {
                int value()
            }

            @Foo
            @Bar
            @AnnotationCollector
            public @interface FooBar {}

            @FooBar(666)
            class Evil {}
            Evil
        '''
        assert err =~ /Attribute 'value' should have type 'java.lang.String'; but found type 'int' in @Foo/
    }

    void testCustomProcessor() {
        assertScript '''import groovy.transform.AnnotationCollector
            import groovy.transform.CompileStatic
            import groovy.transform.TypeCheckingMode

            // tag::compiledynamic_naive[]
            @CompileStatic(TypeCheckingMode.SKIP)
            @AnnotationCollector
            public @interface CompileDynamic {}
            // end::compiledynamic_naive[]

            @CompileDynamic
            class Foo {}
            Foo
        '''

        assertScript '''import groovy.transform.AnnotationCollector
            import groovy.transform.CompileStatic
            import groovy.transform.TypeCheckingMode
            import org.codehaus.groovy.ast.AnnotatedNode
            import org.codehaus.groovy.ast.AnnotationNode
            import org.codehaus.groovy.ast.ClassHelper
            import org.codehaus.groovy.ast.ClassNode
            import org.codehaus.groovy.ast.expr.ClassExpression
            import org.codehaus.groovy.ast.expr.PropertyExpression
            import org.codehaus.groovy.control.SourceUnit
            import org.codehaus.groovy.transform.AnnotationCollectorTransform

            // tag::compiledynamic_def_fixed[]
            @AnnotationCollector(processor = "org.codehaus.groovy.transform.CompileDynamicProcessor")
            public @interface CompileDynamic {
            }
            // end::compiledynamic_def_fixed[]

            // tag::compiledynamic_processor[]
            @CompileStatic                                                                  // <1>
            class CompileDynamicProcessor extends AnnotationCollectorTransform {            // <2>
                private static final ClassNode CS_NODE = ClassHelper.make(CompileStatic)    // <3>
                private static final ClassNode TC_NODE = ClassHelper.make(TypeCheckingMode) // <4>

                List<AnnotationNode> visit(AnnotationNode collector,                        // <5>
                                           AnnotationNode aliasAnnotationUsage,             // <6>
                                           AnnotatedNode aliasAnnotated,                    // <7>
                                           SourceUnit source) {                             // <8>
                    def node = new AnnotationNode(CS_NODE)                                  // <9>
                    def enumRef = new PropertyExpression(
                        new ClassExpression(TC_NODE), "SKIP")                               // <10>
                    node.addMember("value", enumRef)                                        // <11>
                    Collections.singletonList(node)                                         // <12>
                }
            }
            // end::compiledynamic_processor[]

            @CompileDynamic
            class Foo {}
            Foo
        '''
    }
}
