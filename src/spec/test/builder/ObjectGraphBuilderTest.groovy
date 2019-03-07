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
package builder

import asciidoctor.Utils

/**
* Tests for ObjectGraphBuilder. The tests directly in this file are specific
* to ObjectGraphBuilder. Functionality in common with other Builders
* is tested in the parent class.
*/
class ObjectGraphBuilderTest  extends GroovyTestCase {

    void testBuilder() {
        assertScript '''// tag::domain_classes[]
package com.acme

class Company {
    String name
    Address address
    List employees = []
}

class Address {
    String line1
    String line2
    int zip
    String state
}

class Employee {
    String name
    int employeeId
    Address address
    Company company
}
// end::domain_classes[]

// tag::builder_example[]
def builder = new ObjectGraphBuilder()                          // <1>
builder.classLoader = this.class.classLoader                    // <2>
builder.classNameResolver = "com.acme"                          // <3>

def acme = builder.company(name: 'ACME') {                      // <4>
    3.times {
        employee(id: it.toString(), name: "Drone $it") {        // <5>
            address(line1:"Post street")                        // <6>
        }
    }
}

assert acme != null
assert acme instanceof Company
assert acme.name == 'ACME'
assert acme.employees.size() == 3
def employee = acme.employees[0]
assert employee instanceof Employee
assert employee.name == 'Drone 0'
assert employee.address instanceof Address
// end::builder_example[]
'''
    }


    void testBuildImmutableFailure() {
        def err = shouldFail '''
package com.acme
import groovy.transform.Immutable

// tag::immutable_class[]
@Immutable
class Person {
    String name
    int age
}
// end::immutable_class[]

def builder = new ObjectGraphBuilder()
builder.classLoader = this.class.classLoader
builder.classNameResolver = "com.acme"

// tag::immutable_fail_runtime[]
def person = builder.person(name:'Jon', age:17)
// end::immutable_fail_runtime[]
        '''
        assert err == Utils.stripAsciidocMarkup('''
// tag::expected_error_immutable[]
Cannot set readonly property: name for class: com.acme.Person
// end::expected_error_immutable[]
''')
    }

    void testBuildImmutableFixed() {
        assertScript '''
package com.acme
import groovy.transform.Immutable

@Immutable
class Person {
    String name
    Integer age
}

def builder = new ObjectGraphBuilder()
builder.classLoader = this.class.classLoader
builder.classNameResolver = "com.acme"

// tag::newinstanceresolver[]
builder.newInstanceResolver = { Class klazz, Map attributes ->
    if (klazz.getConstructor(Map)) {
        def o = klazz.newInstance(attributes)
        attributes.clear()
        return o
    }
    klazz.newInstance()
}
// end::newinstanceresolver[]
def person = builder.person(name:'Jon', age:17)

        '''
    }

    void testId() {
        assertScript '''package com.acme

class Company {
    String name
    Address address
    List employees = []
}

class Address {
    String line1
    String line2
    int zip
    String state
}

class Employee {
    String name
    int employeeId
    Address address
    Company company
}

def builder = new ObjectGraphBuilder()
builder.classLoader = this.class.classLoader
builder.classNameResolver = "com.acme"

// tag::test_id[]
def company = builder.company(name: 'ACME') {
    address(id: 'a1', line1: '123 Groovy Rd', zip: 12345, state: 'JV')          // <1>
    employee(name: 'Duke', employeeId: 1, address: a1)                          // <2>
    employee(name: 'John', employeeId: 2 ){
      address( refId: 'a1' )                                                    // <3>
    }
}
// end::test_id[]
def e1 = company.employees[0]
def e2 = company.employees[1]
assert e1.name == 'Duke'
assert e2.name == 'John'
assert e1.address.line1 == '123 Groovy Rd'
assert e2.address.line1 == '123 Groovy Rd'
'''
    }
}