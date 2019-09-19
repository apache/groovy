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

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class DelegatesToSpecTest extends GroovyTestCase {
    void testEmailWithoutDelegatesTo() {
        assertScript '''
            // tag::emailspec_no_delegatesto[]
            class EmailSpec {
                void from(String from) { println "From: $from"}
                void to(String... to) { println "To: $to"}
                void subject(String subject) { println "Subject: $subject"}
                void body(Closure body) {
                    def bodySpec = new BodySpec()
                    def code = body.rehydrate(bodySpec, this, this)
                    code.resolveStrategy = Closure.DELEGATE_ONLY
                    code()
                }
            }
            // end::emailspec_no_delegatesto[]

            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }

            // tag::email_method_no_delegatesto[]
            def email(Closure cl) {
                def email = new EmailSpec()
                def code = cl.rehydrate(email, this, this)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code()
            }
            // end::email_method_no_delegatesto[]

            // tag::email_builder_usage[]
            email {
                from 'dsl-guru@mycompany.com'
                to 'john.doe@waitaminute.com'
                subject 'The pope has resigned!'
                body {
                    p 'Really, the pope has resigned!'
                }
            }
            // end::email_builder_usage[]
        '''
    }

    void testEmailWithoutDelegatesToTypeChecked() {
        shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
            class EmailSpec {
                void from(String from) { println "From: $from"}
                void to(String... to) { println "To: $to"}
                void subject(String subject) { println "Subject: $subject"}
                void body(Closure body) {
                    def bodySpec = new BodySpec()
                    def code = body.rehydrate(bodySpec, this, this)
                    code.resolveStrategy = Closure.DELEGATE_ONLY
                    code()
                }
            }

            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }

            def email(Closure cl) {
                def email = new EmailSpec()
                def code = cl.rehydrate(email, this, this)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code()
            }

            // tag::sendmail_typechecked_nodelegatesto[]
            @groovy.transform.TypeChecked
            void sendEmail() {
                email {
                    from 'dsl-guru@mycompany.com'
                    to 'john.doe@waitaminute.com'
                    subject 'The pope has resigned!'
                    body {
                        p 'Really, the pope has resigned!'
                    }
                }
            }
            // end::sendmail_typechecked_nodelegatesto[]
        '''
        }
    }

    void testEmailWithDelegatesTo() {
        assertScript '''
            class EmailSpec {
                void from(String from) { println "From: $from"}
                void to(String... to) { println "To: $to"}
                void subject(String subject) { println "Subject: $subject"}
                void body(@DelegatesTo(BodySpec) Closure body) {
                    def bodySpec = new BodySpec()
                    def code = body.rehydrate(bodySpec, this, this)
                    code.resolveStrategy = Closure.DELEGATE_ONLY
                    code()
                }
            }
            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }
            // tag::email_method_delegatesto[]
            def email(@DelegatesTo(EmailSpec) Closure cl) {
                def email = new EmailSpec()
                def code = cl.rehydrate(email, this, this)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code()
            }
            // end::email_method_delegatesto[]

            email {
                from 'dsl-guru@mycompany.com'
                to 'john.doe@waitaminute.com'
                subject 'The pope has resigned!'
                body {
                    p 'Really, the pope has resigned!'
                }
            }
        '''
    }

    void testEmailWithDelegatesToAndStrategy() {
        assertScript '''
            class EmailSpec {
                void from(String from) { println "From: $from"}
                void to(String... to) { println "To: $to"}
                void subject(String subject) { println "Subject: $subject"}
                void body(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=BodySpec) Closure body) {
                    def bodySpec = new BodySpec()
                    def code = body.rehydrate(bodySpec, this, this)
                    code.resolveStrategy = Closure.DELEGATE_ONLY
                    code()
                }
            }
            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }
            // tag::email_method_delegatesto_strategy[]
            def email(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=EmailSpec) Closure cl) {
                def email = new EmailSpec()
                def code = cl.rehydrate(email, this, this)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code()
            }
            // end::email_method_delegatesto_strategy[]

            email {
                from 'dsl-guru@mycompany.com'
                to 'john.doe@waitaminute.com'
                subject 'The pope has resigned!'
                body {
                    p 'Really, the pope has resigned!'
                }
            }
        '''
    }

    void testEmailWithDelegatesToAndStrategyTypeChecked() {
        assertScript '''import groovy.transform.TypeChecked

            class EmailSpec {
                void from(String from) { println "From: $from"}
                void to(String... to) { println "To: $to"}
                void subject(String subject) { println "Subject: $subject"}
                void body(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=BodySpec) Closure body) {
                    def bodySpec = new BodySpec()
                    def code = body.rehydrate(bodySpec, this, this)
                    code.resolveStrategy = Closure.DELEGATE_ONLY
                    code()
                }
            }
            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }
            def email(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=EmailSpec) Closure cl) {
                def email = new EmailSpec()
                def code = cl.rehydrate(email, this, this)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code()
            }

            // tag::sendmail_typechecked_pass[]
            @TypeChecked
            void doEmail() {
                email {
                    from 'dsl-guru@mycompany.com'
                    to 'john.doe@waitaminute.com'
                    subject 'The pope has resigned!'
                    body {
                        p 'Really, the pope has resigned!'
                    }
                }
            }
            // end::sendmail_typechecked_pass[]
        '''
    }

    void testDelegatesToSimple() {
        assertScript '''
            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }

            // tag::simple_delegation[]
            void body(@DelegatesTo(BodySpec) Closure cl) {
                // ...
            }
            // end::simple_delegation[]

            @groovy.transform.TypeChecked
            void test() {
                body {
                    p 'Text'
                }
            }

        '''
    }

    void testDelegatesToWithStrategy() {
        shouldFail(MultipleCompilationErrorsException) {
        assertScript '''
            class BodySpec {
                void p(String txt) {
                    println(txt)
                }
            }

            // tag::delegation_with_strategy[]
            void body(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=BodySpec) Closure cl) {
                // ...
            }
            // end::delegation_with_strategy[]

            void bar() {}

            @groovy.transform.TypeChecked
            void test() {
                body {
                    p 'Text'
                }
            }

            @groovy.transform.TypeChecked
            void shouldNotCompile() {
                body {
                    p 'Text'
                    bar()
                }
            }

        '''
        }
    }

    void testDelegatesToTarget() {
        assertScript '''
            class Email {
                void from(String from) {}
                void to(String to) {}
                void send() {}
            }
            // tag::exec_method_no_delegatesto[]
            def exec(Object target, Closure code) {
               def clone = code.rehydrate(target, this, this)
               clone()
            }
            // end::exec_method_no_delegatesto[]

            // tag::exec_usage[]
            def email = new Email()
            exec(email) {
               from '...'
               to '...'
               send()
            }
            // end::exec_usage[]
        '''

        assertScript '''
            class Email {
                void from(String from) {}
                void to(String to) {}
                void send() {}
            }
            // tag::exec_method_with_delegatesto[]
            def exec(@DelegatesTo.Target Object target, @DelegatesTo Closure code) {
               def clone = code.rehydrate(target, this, this)
               clone()
            }
            // end::exec_method_with_delegatesto[]

            @groovy.transform.TypeChecked
            void testEmail() {
                // tag::exec_usage_with_delegatesto[]
                def email = new Email()
                exec(email) {
                   from '...'
                   to '...'
                   send()
                }
                // end::exec_usage_with_delegatesto[]
            }
        '''
    }
    void testDelegatesToTargetFlowTyping() {
        assertScript '''
            def exec(@DelegatesTo.Target Object target, @DelegatesTo Closure code) {
               def clone = code.rehydrate(target, this, this)
               clone()
            }

            // tag::delegatesto_flow_typing_header[]
            class Greeter {
               void sayHello() { println 'Hello' }
            }
            // end::delegatesto_flow_typing_header[]

            @groovy.transform.TypeChecked
            void doTest() {
                // tag::delegatesto_flow_typing_footer[]
                def greeter = new Greeter()
                exec(greeter) {
                   sayHello()
                }
                // end::delegatesto_flow_typing_footer[]
            }
        '''
    }

    void testDelegatesToMultipleAnnotations() {
        assertScript '''

            class Foo { void foo(String msg) { println "Foo ${msg}!" } }
            class Bar { void bar(int x) { println "Bar ${x}!" } }
            class Baz { void baz(Date d) { println "Baz ${d}!" } }

            /*
            // tag::foobarbaz_method_no_delegatesto[]
            void fooBarBaz(Closure foo, Closure bar, Closure baz) {
                ...
            }
            // end::foobarbaz_method_no_delegatesto[]
            */

            // tag::foobarbaz_method_header[]
            void fooBarBaz(@DelegatesTo(Foo) Closure foo, @DelegatesTo(Bar) Closure bar, @DelegatesTo(Baz) Closure baz) {
            // end::foobarbaz_method_header[]
                foo.rehydrate(new Foo(), this, this).call()
                bar.rehydrate(new Bar(), this, this).call()
                baz.rehydrate(new Baz(), this, this).call()
            // tag::foobarbaz_method_footer[]
            }
            // end::foobarbaz_method_footer[]

            @groovy.transform.TypeChecked
            void doTest() {
                fooBarBaz(
                    { foo('Hello') },
                    { bar(123) },
                    { baz(new Date()) }
                )
            }
            doTest()
        '''
    }

    void testDelegatesToMultipleTargets() {
        assertScript '''
            // tag::foobarbaz_classes[]
            class Foo { void foo(String msg) { println "Foo ${msg}!" } }
            class Bar { void bar(int x) { println "Bar ${x}!" } }
            class Baz { void baz(Date d) { println "Baz ${d}!" } }
            // end::foobarbaz_classes[]

            // tag::foobarbaz_multitarget[]
            void fooBarBaz(
                @DelegatesTo.Target('foo') foo,
                @DelegatesTo.Target('bar') bar,
                @DelegatesTo.Target('baz') baz,

                @DelegatesTo(target='foo') Closure cl1,
                @DelegatesTo(target='bar') Closure cl2,
                @DelegatesTo(target='baz') Closure cl3) {
                cl1.rehydrate(foo, this, this).call()
                cl2.rehydrate(bar, this, this).call()
                cl3.rehydrate(baz, this, this).call()
            }
            // end::foobarbaz_multitarget[]

            @groovy.transform.TypeChecked
            void doTest() {
                // tag::multitarget_test[]
                def a = new Foo()
                def b = new Bar()
                def c = new Baz()
                fooBarBaz(
                    a, b, c,
                    { foo('Hello') },
                    { bar(123) },
                    { baz(new Date()) }
                )
                // end::multitarget_test[]
            }
            doTest()
        '''
    }

    void testDelegatesToGenericType() {
        assertScript '''
            // tag::configure_list_method[]
            public <T> void configure(List<T> elements, Closure configuration) {
               elements.each { e->
                  def clone = configuration.rehydrate(e, this, this)
                  clone.resolveStrategy = Closure.DELEGATE_FIRST
                  clone.call()
               }
            }
            // end::configure_list_method[]

            // tag::configure_list_usage[]
            @groovy.transform.ToString
            class Realm {
               String name
            }
            List<Realm> list = []
            3.times { list << new Realm() }
            configure(list) {
               name = 'My Realm'
            }
            assert list.every { it.name == 'My Realm' }
            // end::configure_list_usage[]
        '''

    }

    void testDelegatesToGenericTypeAndCompileStatic() {
        assertScript '''
            @groovy.transform.ToString
            class Realm {
               String name
            }

            // tag::configure_list_with_delegatesto[]
            public <T> void configure(
                @DelegatesTo.Target List<T> elements,
                @DelegatesTo(strategy=Closure.DELEGATE_FIRST, genericTypeIndex=0) Closure configuration) {
               def clone = configuration.rehydrate(e, this, this)
               clone.resolveStrategy = Closure.DELEGATE_FIRST
               clone.call()
            }
            // end::configure_list_with_delegatesto[]

            @groovy.transform.CompileStatic
            void doTest() {
                List<Realm> list = []
                3.times { list << new Realm() }
                configure(list) {
                   name = 'My Realm'
                }
                assert list.every { Realm it -> it.name == 'My Realm' }
            }
        '''

    }

    void testDelegatesToType() {
        def msg = shouldFail '''

// tag::delegatestotype_mapper[]
class Mapper<T,U> {                             // <1>
    final T value                               // <2>
    Mapper(T value) { this.value = value }
    U map(Closure<U> producer) {                // <3>
        producer.delegate = value
        producer()
    }
}
// end::delegatestotype_mapper[]
@groovy.transform.CompileStatic
void test() {
    // tag::delegatestotype_mapper_test[]
    def mapper = new Mapper<String,Integer>('Hello')
    assert mapper.map { length() } == 5
    // end::delegatestotype_mapper_test[]
}
'''
        assert msg.contains('Static type checking] - Cannot find matching method')

        assertScript '''

// tag::delegatestotype_mapper_fixed[]
class Mapper<T,U> {
    final T value
    Mapper(T value) { this.value = value }
    U map(@DelegatesTo(type="T") Closure<U> producer) {  // <1>
        producer.delegate = value
        producer()
    }
}
// end::delegatestotype_mapper_fixed[]
@groovy.transform.CompileStatic
void test() {
    def mapper = new Mapper<String,Integer>('Hello')
    assert mapper.map { length() } == 5
}
test()
'''
    }
}
