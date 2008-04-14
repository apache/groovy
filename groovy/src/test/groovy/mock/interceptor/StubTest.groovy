package groovy.mock.interceptor

class StubTest extends GroovyTestCase {

   void testBehaviorWithInstanceCreatedOutsideUseClosure() {
      def speakerStub = new StubFor(Speaker)
      speakerStub.demand.startLecture() { "Intercepted!" }

      def speaker1 = new Speaker()
      assert speaker1.startLecture() == "Starting..."

      speakerStub.use {
         def speaker2 = new Speaker()
         assert speaker2.startLecture() == "Intercepted!"
      }

      speakerStub.demand.startLecture() { "Intercepted!" }
      speakerStub.use (speaker1) {
         assert speaker1.startLecture() == "Intercepted!"
      }
      assert speaker1.startLecture() == "Starting..."
   }

   void testWIthOGBOutsideUse() {
      def ogb = new ObjectGraphBuilder( classNameResolver: 'groovy.mock.interceptor' )
      def stub = new StubFor( Company )
      stub.demand.payroll(0..2) { 500 }
      def acme = ogb.company {
         employee( name: 'Duke', salary: 42 )
      }
      stub.use (acme) {
         assert acme.payroll() == 500
         assert acme.payroll() == 500
      }
   }

   void testWIthOGBInsideUse() {
      def ogb = new ObjectGraphBuilder( classNameResolver: 'groovy.mock.interceptor' )
      def stub = new StubFor( ObjectGraphBuilder )
      stub.use {
         def acme = ogb.company {
            // blows after the next line because property handling
            // on Company was not demanded
            employee( name: 'Duke', salary: 42 )
         }

         def stub2 = new StubFor( Company )
         stub2.demand.payroll(0..2) { 500 }
         stub2.use (acme) {
           assert acme.payroll() == 500
         }
      }
   }

}

class Speaker {
   String name
   def startLecture() { "Starting..." }
}


class Company {
   String name
   List employees = []
   def payroll() {
      def total = 0
      employees.each { total += it.salary }
      total
   }
}

class Employee {
   String name
   Number salary = 0
}
