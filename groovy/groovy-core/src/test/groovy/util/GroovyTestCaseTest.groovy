/**
    Testing the notYetImplemented feature of GroovyTestCase.
    Todo: testing all other features.
    @author Dierk Koenig
*/

class GroovyTestCaseTest extends GroovyTestCase {

    void testNotYetImplementedSubclassUse () {
        if (notYetImplemented()) return
        fail 'here the code that is expected to fail'
    }
    void testNotYetImplementedStaticUse () {
        if (GroovyTestCase.notYetImplemented(this)) return
        fail 'here the code that is expected to fail'
    }


    // we cannot test this automatically...
    // remove the leading x, run the test and see it failing
    void xtestSubclassFailing() {
        if (notYetImplemented()) return
        assert true // passes unexpectedly
    }
    void xtestStaticFailing() {
        if (GroovyTestCase.notYetImplemented(this)) return
        assert true // passes unexpectedly
    }
}