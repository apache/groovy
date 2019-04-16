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
package groovy.util

class ObjectGraphBuilderTest extends GroovyTestCase {
   ObjectGraphBuilder builder
   ObjectGraphBuilder reflectionBuilder

   void testCompany() {
      def expected = new Company( name: 'ACME', employees: [] )
      def actual = builder.company( name: 'ACME', employees: [] )
      assert actual != null
      //assert actual.class == Company
      assert actual.name == expected.name
      assert actual.address == expected.address
      assert actual.employees == expected.employees
   }

   void testCompanyAndAddress() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedCompany = new Company( name: 'ACME', employees: [], address: expectedAddress )
      def actualCompany = builder.company( name: 'ACME', employees: [] ) {
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      }
      assert actualCompany != null
      //assert actualCompany.class == Company
      assert actualCompany.name == expectedCompany.name
      assert actualCompany.employees == expectedCompany.employees
      def actualAddress = actualCompany.address
      assert actualAddress != null
      //assert actualAddress.class == Address
      assert actualAddress.line1 == expectedAddress.line1
      assert actualAddress.line2 == expectedAddress.line2
      assert actualAddress.zip == expectedAddress.zip
      assert actualAddress.state == expectedAddress.state
   }

   void testCompanyAndEmployeeAndAddress() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedEmployee = new Employee( name: 'Duke', employeeId: 1, address: expectedAddress )
      def expectedCompany = new Company( name: 'ACME' )
      def actualCompany = builder.company( name: 'ACME', employees: [] ) {
         employee(  name: 'Duke', employeeId: 1 ) {
            address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
         }
      }
      assert actualCompany != null
      //assert actualCompany.class == Company
      assert actualCompany.name == expectedCompany.name
      assert actualCompany.employees.size() == 1
      def actualEmployee = actualCompany.employees[0]
      //assert actualEmployee.class == Employee
      assert actualEmployee.name == expectedEmployee.name
      assert actualEmployee.employeeId == expectedEmployee.employeeId
      def actualAddress = actualEmployee.address
      assert actualAddress != null
      //assert actualAddress.class == Address
      assert actualAddress.line1 == expectedAddress.line1
      assert actualAddress.line2 == expectedAddress.line2
      assert actualAddress.zip == expectedAddress.zip
      assert actualAddress.state == expectedAddress.state
   }

   void testCompanyAndEmployeeSameAddress() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedEmployee = new Employee( name: 'Duke', employeeId: 1, address: expectedAddress )
      def expectedCompany = new Company( name: 'ACME' )
      def actualCompany = builder.company( name: 'ACME', employees: [] ) {
         address( id: 'a1', line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
         employee(  name: 'Duke', employeeId: 1, address: a1 )
      }
      assert actualCompany != null
      //assert actualCompany.class == Company
      assert actualCompany.name == expectedCompany.name
      assert actualCompany.employees.size() == 1
      def actualEmployee = actualCompany.employees[0]
      //assert actualEmployee.class == Employee
      assert actualEmployee.name == expectedEmployee.name
      assert actualEmployee.employeeId == expectedEmployee.employeeId
      assert actualCompany.address == actualEmployee.address
   }

   void testCompanyAndEmployeeSameAddressWithRef() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedEmployee = new Employee( name: 'Duke', employeeId: 1, address: expectedAddress )
      def expectedCompany = new Company( name: 'ACME' )
      def actualCompany = builder.company( name: 'ACME', employees: [] ) {
         address( id: 'a1', line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
         employee(  name: 'Duke', employeeId: 1 ){
            address( refId: 'a1' )
         }
      }
      assert actualCompany != null
      //assert actualCompany.class == Company
      assert actualCompany.name == expectedCompany.name
      assert actualCompany.employees.size() == 1
      def actualEmployee = actualCompany.employees[0]
      //assert actualEmployee.class == Employee
      assert actualEmployee.name == expectedEmployee.name
      assert actualEmployee.employeeId == expectedEmployee.employeeId
      assert actualCompany.address == actualEmployee.address
      assert actualEmployee.company == actualCompany
   }

   void testCompanyAndManyEmployees() {
      def actualCompany = builder.company( name: 'ACME', employees: [] ) {
         3.times {
            employee(  name: "Duke $it", employeeId: it )
         }
      }
      assert actualCompany != null
      assert actualCompany.employees.size() == 3
      3.times {
         assert actualCompany.employees[it].name == "Duke $it"
      }
      //assert actualCompany.employees*.getClass() == [Employee,Employee,Employee]
   }

   void testStringfiedIdentifierResolver() {
      builder.identifierResolver = "uid"
      def company = builder.company( name: 'ACME', employees: [], uid: 'acme' )
      assert company != null
      assert builder.acme != null
      assert builder.acme == company
   }

   void testStringfiedReferenceResolver() {
      builder.referenceResolver = "ref_id"
      def company = builder.company( name: 'ACME', employees: [] ) {
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV', id: 'a1' )
         employee(  name: 'Duke', employeeId: 1, id: 'e1' ) {
            address( ref_id: 'a1' )
         }
      }
      assert company != null
      assert company.employees.size() == 1
      assert builder.e1 == company.employees[0]
      assert builder.a1 == company.address
      assert builder.a1 == builder.e1.address
   }

   void testReferenceResolver() {
      def company = builder.company( name: 'ACME', employees: [] ) {
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV', id: 'a1' )
         employee(  name: 'Duke', employeeId: 1, id: 'e1' ) {
            address( refId: 'a1' )
         }
      }
      assert company != null
      assert company.employees.size() == 1
      assert builder.e1 == company.employees[0]
      assert builder.a1 == company.address
      assert builder.a1 == builder.e1.address
   }

   void testReferenceResolver_referenceIsLiveObject() {
      def company = builder.company( name: 'ACME', employees: [] ) {
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV', id: 'a1' )
         employee(  name: 'Duke', employeeId: 1, id: 'e1' ) {
            address( refId: a1 )
         }
      }
      assert company != null
      assert company.employees.size() == 1
      assert builder.e1 == company.employees[0]
      assert builder.a1 == company.address
      assert builder.a1 == builder.e1.address
   }

   void testDirectReference() {
      def company = builder.company( name: 'ACME', employees: [] ) {
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV', id: 'a1' )
         employee(  name: 'Duke', employeeId: 1, id: 'e1' ) {
            address( a1 )
         }
      }
      assert company != null
      assert company.employees.size() == 1
      assert builder.e1 == company.employees[0]
      assert builder.a1 == company.address
      assert builder.a1 == builder.e1.address
   }

   void testLazyReferences() {
      def company = builder.company( name: 'ACME', employees: [] ) {
         employee(  name: 'Duke', employeeId: 1, id: 'e1' ) {
            address( refId: 'a1' )
         }
         address( line1: '123 Groovy Rd', zip: 12345, state: 'JV', id: 'a1' )
      }
      assert company != null
      assert company.employees.size() == 1
      assert builder.e1 == company.employees[0]
      assert builder.a1 == company.address
      assert builder.a1 == builder.e1.address
   }

   void testReflectionCompany() {
      def expected = new ReflectionCompany( name: 'ACME' )
      def actual = reflectionBuilder.reflectionCompany( name: 'ACME' )
      assert actual != null
      assert actual.name == expected.name
   }

   void testReflectionCompanyAndAddress() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedCompany = new ReflectionCompany( name: 'ACME', addr: expectedAddress )
      def actualCompany = reflectionBuilder.reflectionCompany( name: 'ACME' ) {
         addr( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      }
      assert actualCompany != null
      assert actualCompany.name == expectedCompany.name
      def actualAddress = actualCompany.addr
      assert actualAddress != null
      assert actualAddress.line1 == expectedAddress.line1
      assert actualAddress.line2 == expectedAddress.line2
      assert actualAddress.zip == expectedAddress.zip
      assert actualAddress.state == expectedAddress.state
   }

    void testReflectionCompanyAddressAndEmployees() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedCompany = new ReflectionCompany( name: 'ACME', addr: expectedAddress )
      def expectedDirector = new Employee( name: 'Duke', employeeId: 1, address: expectedAddress )
      def expectedFinancialController = new Employee( name: 'Craig', employeeId: 2, address: expectedAddress )
      def expectedDrone0 = new Employee( name: 'Drone0', employeeId: 3, address: expectedAddress )
      def expectedDrone1 = new Employee( name: 'Drone1', employeeId: 4, address: expectedAddress )

      def actualCompany = reflectionBuilder.reflectionCompany( name: 'ACME', drones: [] ) {
         addr( id: 'a1', line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
         director(  name: expectedDirector.name, employeeId: expectedDirector.employeeId ){
            address( refId: 'a1' )
         }
         financialController(  name: expectedFinancialController.name, employeeId: expectedFinancialController.employeeId ){
            address( refId: 'a1' )
         }
         drones(  name: expectedDrone0.name, employeeId: expectedDrone0.employeeId ){
            address( refId: 'a1' )
         }
         drones(  name: expectedDrone1.name, employeeId: expectedDrone1.employeeId ){
            address( refId: 'a1' )
         }
      }
      
      assert actualCompany != null
      assert actualCompany.name == expectedCompany.name
      def actualAddress = actualCompany.addr
      assert actualAddress != null
      assert actualAddress.line1 == expectedAddress.line1
      assert actualAddress.line2 == expectedAddress.line2
      assert actualAddress.zip == expectedAddress.zip
      assert actualAddress.state == expectedAddress.state

      assert actualCompany.director != null
      assert actualCompany.director.name == expectedDirector.name
      assert actualCompany.director.employeeId == expectedDirector.employeeId
      assert actualCompany.director.address.line1 == expectedAddress.line1

      assert actualCompany.financialController != null
      assert actualCompany.financialController.name == expectedFinancialController.name
      assert actualCompany.financialController.employeeId == expectedFinancialController.employeeId
      assert actualCompany.financialController.address.line1 == expectedAddress.line1

      assert actualCompany.drones != null
      assert actualCompany.drones.size() == 2
      assert actualCompany.drones[0].name == expectedDrone0.name
      assert actualCompany.drones[0].employeeId == expectedDrone0.employeeId
      assert actualCompany.drones[0].address != null
      assert actualCompany.drones[0].address.line1 == expectedAddress.line1
      assert actualCompany.drones[1].name == expectedDrone1.name
      assert actualCompany.drones[1].address != null
      assert actualCompany.drones[1].employeeId == expectedDrone1.employeeId
      assert actualCompany.drones[1].address.line1 == expectedAddress.line1
   }

   void testPlural() {
       def dracula = builder.person(name: 'Dracula') {
           allergy(name: 'garlic', reaction: 'moderate burns')
           allergy(name: 'cross', reaction: 'aversion')
           allergy(name: 'wood stake', reaction: 'death')
           allergy(name: 'sunlight', reaction: 'burst into flames')
           petMonkey(name: 'La-la')
           petMonkey(name: 'Ampersand')
       }

       assert dracula.allergies.size() == 4
       assert dracula.petMonkeys.size() == 2
   }

   void testCompanyAndEmployeeAndAddressUsingBeanFactory() {
      def expectedAddress = new Address( line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
      def expectedEmployee = new Employee( name: 'Duke', employeeId: 1, address: expectedAddress )
      def expectedCompany = new Company( name: 'ACME' )
      def actualCompany = builder.bean(new Company(), name: 'ACME', employees: [] ) {
         bean(Employee, name: 'Duke', employeeId: 1 ) {
            bean(Address, line1: '123 Groovy Rd', zip: 12345, state: 'JV' )
         }
      }
      assert actualCompany != null
      // assert actualCompany.class == Company
      assert actualCompany.name == expectedCompany.name
      assert actualCompany.employees.size() == 1
      def actualEmployee = actualCompany.employees[0]
      // assert actualEmployee.class == Employee
      assert actualEmployee.name == expectedEmployee.name
      assert actualEmployee.employeeId == expectedEmployee.employeeId
      def actualAddress = actualEmployee.address
      assert actualAddress != null
      // assert actualAddress.class == Address
      assert actualAddress.line1 == expectedAddress.line1
      assert actualAddress.line2 == expectedAddress.line2
      assert actualAddress.zip == expectedAddress.zip
      assert actualAddress.state == expectedAddress.state
   }

   void setUp() {
      builder = new ObjectGraphBuilder()
      builder.classNameResolver = "groovy.util"
      reflectionBuilder = new ObjectGraphBuilder()
      reflectionBuilder.classNameResolver = [ name: 'reflection', root: "groovy.util" ]
   }
}

class Company {
   String name
   Address address
   List employees = []

   String toString() { "Company=[name:${name}, address:${address}, employees:${employees}]" }
}

class Address {
   String line1
   String line2
   int zip
   String state

   String toString() { "Address=[line1:${line1}, line2:${line2}, zip:${zip}, state:${state}]" }
}

class Employee {
   String name
   int employeeId
   Address address
   Company company

   String toString() { "Employee=[name:${name}, employeeId:${employeeId}, address:${address}, company:${company.name}]" }
}

class ReflectionCompany {
   String name
   Address addr
   Employee director
   Employee financialController
   List<Employee> drones

   String toString() { "Company=[name:${name}, address:${address}, director:${md}, financialController:${cio}, drones:${drones}]" }
}

class Person {
    String name
    List allergies = []
    List petMonkeys = []

    String toString() { "Person=[name:${name}, allergies:${allergies}, petMonkeys:${petMonkeys}]" }
}

class Allergy {
    String name
    String reaction

    String toString() { "Allergy=[name:${name}, reaction:${reaction}]" }
}

class PetMonkey {
    String name

    String toString() { "PetMonkey=[name:${name}]" }
}
