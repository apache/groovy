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
package metaprogramming

import groovy.test.GroovyTestCase


class ExpandoMetaClassTest extends GroovyTestCase {

    void testConstructors() {
        assertScript '''
             // tag::emc_constructors[]
            class Book {
                String title
            }
            Book.metaClass.constructor << { String title -> new Book(title:title) }

            def book = new Book('Groovy in Action - 2nd Edition')
            assert book.title == 'Groovy in Action - 2nd Edition'
            // end::emc_constructors[]
        '''
    }

    void testMethodPointer() {
        assertScript '''
            // tag::emc_method_pointer[]
            class Person {
                String name
            }
            class MortgageLender {
               def borrowMoney() {
                  "buy house"
               }
            }

            def lender = new MortgageLender()

            Person.metaClass.buyHouse = lender.&borrowMoney

            def p = new Person()

            assert "buy house" == p.buyHouse()
            // end::emc_method_pointer[]
        '''
    }

    void testDynamicMethodNames() {
        assertScript '''
            // tag::emc_dynamic_method_names[]
            class Person {
               String name = "Fred"
            }

            def methodName = "Bob"

            Person.metaClass."changeNameTo${methodName}" = {-> delegate.name = "Bob" }

            def p = new Person()

            assert "Fred" == p.name

            p.changeNameToBob()

            assert "Bob" == p.name
            // end::emc_dynamic_method_names[]
        '''
    }

    void testOverrideInvokeMethod() {
        assertScript '''
            // tag::emc_invoke_method[]
            class Stuff {
               def invokeMe() { "foo" }
            }

            Stuff.metaClass.invokeMethod = { String name, args ->
               def metaMethod = Stuff.metaClass.getMetaMethod(name, args)
               def result
               if(metaMethod) result = metaMethod.invoke(delegate,args)
               else {
                  result = "bar"
               }
               result
            }

            def stf = new Stuff()

            assert "foo" == stf.invokeMe()
            assert "bar" == stf.doStuff()
            // end::emc_invoke_method[]
        '''
    }

    void testOverrideGetProperty() {
        assertScript '''
            // tag::emc_get_property[]
            class Person {
               String name = "Fred"
            }

            Person.metaClass.getProperty = { String name ->
               def metaProperty = Person.metaClass.getMetaProperty(name)
               def result
               if(metaProperty) result = metaProperty.getProperty(delegate)
               else {
                  result = "Flintstone"
               }
               result
            }

            def p = new Person()

            assert "Fred" == p.name
            assert "Flintstone" == p.other
            // end::emc_get_property[]
        '''
    }

    void testOverrideInvokeMethodStatic() {
        assertScript '''
            // tag::emc_invoke_method_static[]
            class Stuff {
               static invokeMe() { "foo" }
            }

            Stuff.metaClass.'static'.invokeMethod = { String name, args ->
               def metaMethod = Stuff.metaClass.getStaticMetaMethod(name, args)
               def result
               if(metaMethod) result = metaMethod.invoke(delegate,args)
               else {
                  result = "bar"
               }
               result
            }

            assert "foo" == Stuff.invokeMe()
            assert "bar" == Stuff.doStuff()

            // end::emc_invoke_method_static[]
        '''
    }

    void testGetProperty() {
        assertScript '''
            // tag::emc_getter[]
            class Book {
              String title
            }
            Book.metaClass.getAuthor << {-> "Stephen King" }

            def b = new Book()

            assert "Stephen King" == b.author
            // end::emc_getter[]
        '''
    }

    void testSetGetProperty() {
        assertScript '''
            // tag::emc_getter_setter[]
            class Book {
              String title
            }

            def properties = Collections.synchronizedMap([:])

            Book.metaClass.setAuthor = { String value ->
               properties[System.identityHashCode(delegate) + "author"] = value
            }
            Book.metaClass.getAuthor = {->
               properties[System.identityHashCode(delegate) + "author"]
            }
            // end::emc_getter_setter[]
        '''
    }

    void testProperty() {
        assertScript '''
            // tag::emc_property[]
            class Book {
               String title
            }

            Book.metaClass.author = "Stephen King"
            def b = new Book()

            assert "Stephen King" == b.author
            // end::emc_property[]
        '''
    }

    void testStaticMethod() {
        assertScript '''
            // tag::emc_static[]
            class Book {
               String title
            }

            Book.metaClass.static.create << { String title -> new Book(title:title) }

            def b = Book.create("The Stand")
            // end::emc_static[]
        '''
    }

    void testMethod() {
        assertScript '''
            // tag::emc_method[]
            class Book {
               String title
            }

            Book.metaClass.titleInUpperCase << {-> title.toUpperCase() }

            def b = new Book(title:"The Stand")

            assert "THE STAND" == b.titleInUpperCase()
            // end::emc_method[]
        '''
    }

    void testInterface() {
        assertScript '''
            // tag::emc_interface[]
            List.metaClass.sizeDoubled = {-> delegate.size() * 2 }

            def list = []

            list << 1
            list << 2

            assert 4 == list.sizeDoubled()
            // end::emc_interface[]
        '''
    }



}
