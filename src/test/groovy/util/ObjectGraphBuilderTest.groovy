package groovy.util

class ObjectGraphBuilderTest extends GroovyTestCase {
   ObjectGraphBuilder builder

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

   void setUp() {
      builder = new ObjectGraphBuilder()
      builder.classNameResolver = "groovy.util"
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
