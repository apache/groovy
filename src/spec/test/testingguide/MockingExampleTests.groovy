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
package testingguide

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import groovy.test.GroovyTestCase

// tag::map_coercion[]
        class TranslationService {
            String convert(String key) {
                return "test"
            }
        }

// end::map_coercion[]

// tag::sam_coercion[]
        abstract class BaseService {
            abstract void doSomething()
        }

// end::sam_coercion[]

// tag::collaborators[]
        class Person {
            String first, last
        }

        class Family {
            Person father, mother
            def nameOfMother() { "$mother.first $mother.last" }
        }

// end::collaborators[]

// tag::emc2[]
        class Book {
            String title
        }

// end::emc2[]

class MockingExampleTests extends GroovyTestCase {

    void tearDown() {
        GroovySystem.metaClassRegistry.setMetaClass(TranslationService, null)
        GroovySystem.metaClassRegistry.setMetaClass(BaseService, null)
        GroovySystem.metaClassRegistry.setMetaClass(Book, null)
        super.tearDown()
    }

    void testMapCoercion() {
        // tag::map_coercion[]
        def service = [convert: { String key -> 'some text' }] as TranslationService
        assert 'some text' == service.convert('key.text')
        // end::map_coercion[]
    }

    void testClosureCoercion() {
        // tag::closure_coercion[]
        def service = { String key -> 'some text' } as TranslationService
        assert 'some text' == service.convert('key.text')
        // end::closure_coercion[]
    }

    void testSAMCoercion() {
        // tag::sam_coercion[]
        BaseService service = { -> println 'doing something' }
        service.doSomething()
        // end::sam_coercion[]
    }

    void testMockFor() {
        // tag::mockFor[]
        def mock = new MockFor(Person)      // <1>
        mock.demand.getFirst{ 'dummy' }
        mock.demand.getLast{ 'name' }
        mock.use {                          // <2>
            def mary = new Person(first:'Mary', last:'Smith')
            def f = new Family(mother:mary)
            assert f.nameOfMother() == 'dummy name'
        }
        mock.expect.verify()                // <3>
        // end::mockFor[]
    }

    void testStubFor() {
        // tag::stubFor[]
        def stub = new StubFor(Person)      // <1>
        stub.demand.with {                  // <2>
            getLast{ 'name' }
            getFirst{ 'dummy' }
        }
        stub.use {                          // <3>
            def john = new Person(first:'John', last:'Smith')
            def f = new Family(father:john)
            assert f.father.first == 'dummy'
            assert f.father.last == 'name'
        }
        stub.expect.verify()                // <4>
        // end::stubFor[]
    }

    void testEMC() {
        // tag::emc[]
        String.metaClass.swapCase = {->
            def sb = new StringBuffer()
            delegate.each {
                sb << (Character.isUpperCase(it as char) ? Character.toLowerCase(it as char) :
                    Character.toUpperCase(it as char))
            }
            sb.toString()
        }

        def s = "heLLo, worLD!"
        assert s.swapCase() == 'HEllO, WORld!'
        // end::emc[]

        // tag::emc4[]
        GroovySystem.metaClassRegistry.setMetaClass(java.lang.String, null)
        // end::emc4[]
    }

    void testEMCStaticMethod() {
        // tag::emc2[]
        Book.metaClass.static.create << { String title -> new Book(title:title) }

        def b = Book.create("The Stand")
        assert b.title == 'The Stand'
        // end::emc2[]
    }

    void testEMCConstructor() {
        // tag::emc3[]
        Book.metaClass.constructor << { String title -> new Book(title:title) }

        def b = new Book("The Stand")
        assert b.title == 'The Stand'
        // end::emc3[]
    }

    void testEMCPerObject() {
        // tag::emc5[]
        def b = new Book(title: "The Stand")
        b.metaClass.getTitle {-> 'My Title' }

        assert b.title == 'My Title'
        // end::emc5[]
    }
}
