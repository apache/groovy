package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy5193Bug extends GroovyTestCase {
    void testMixingMethodsWithPrivatePublicAccessInSameClassV1() {
        try{
            assertScript """
                class Repository5193V1 {
                  def find(String id) {}
                  private <T> T find(Class<T> type, String id, boolean suppressNotFoundExceptions) { }
                }
            """
            fail("compilation should have failed saying that mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden.")
        } catch(MultipleCompilationErrorsException ex) {
            assertTrue ex.message.contains("Mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden")
        }
    }

    void testMixingMethodsWithPrivatePublicAccessInSameClassV2() {
        try{
            assertScript """
                class Repository5193V2 {
                  def find(String id) {}
                  private <T> T find(Class<T> type, String id, boolean suppressNotFoundExceptions = true) { }
                }
            """
            fail("compilation should have failed saying that mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden.")
        } catch(MultipleCompilationErrorsException ex) {
            assertTrue ex.message.contains("Mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden")
        }
    }
}
