/*
 * Copyright 2003-2014 the original author or authors.
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


package typing

import groovy.transform.stc.StaticTypeCheckingTestCase

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
}

