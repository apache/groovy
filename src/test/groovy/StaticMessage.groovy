package groovy

class StaticMessageTest extends GroovyTestCase {

   void testStaticMissingMethodException() {
      try {
         Integer.foobarbaz()
      } catch (MissingMethodException mme) {
         assert mme.message ==~ '.*static.*'
      }
   }

   void testInstanceMissingMethodException() {
      try {
         Integer x = 5;
         x.foobarbaz()
      } catch (MissingMethodException mme) {
         assert ! (mme.message ==~ '.*static.*')
      }
   }
}