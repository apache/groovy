package groovy.util.vm5

import groovy.util.*

class ObjectGraphBuilderTest extends GroovyTestCase {
   ObjectGraphBuilder builder
   ObjectGraphBuilder reflectionBuilder

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
      assert actualCompany.drones.size == 2
      assert actualCompany.drones[0].name == expectedDrone0.name
      assert actualCompany.drones[0].employeeId == expectedDrone0.employeeId
      assert actualCompany.drones[0].address != null
      assert actualCompany.drones[0].address.line1 == expectedAddress.line1
      assert actualCompany.drones[1].name == expectedDrone1.name
      assert actualCompany.drones[1].address != null
      assert actualCompany.drones[1].employeeId == expectedDrone1.employeeId
      assert actualCompany.drones[1].address.line1 == expectedAddress.line1
   }

   void setUp() {
      builder = new ObjectGraphBuilder()
      builder.classNameResolver = "groovy.util.vm5"
      reflectionBuilder = new ObjectGraphBuilder()
      reflectionBuilder.classNameResolver = [ name: 'reflection', root: "groovy.util.vm5" ]
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