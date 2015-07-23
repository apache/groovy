package groovy.bugs

class Groovy7520Bug extends GroovyTestCase {
    void testShouldSeeConflictUsingAbstractMethod() {
        def msg = shouldFail '''
            abstract class DefinesMethod {
                abstract String getName()
            }
            class Foo extends DefinesMethod {
                static int name = 666
            }
            new Foo().name
            '''

        assert msg.contains("Abstract method 'java.lang.String getName()' is not implemented but a method of the same name but different return type is defined: static method 'int getName()'")
    }
}
