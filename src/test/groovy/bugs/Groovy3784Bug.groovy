package groovy.bugs

class Groovy3784Bug extends GroovyTestCase {
    void testUseOfDelegateAndThenGenericsSharingTheSameClassHelper() {
        GroovyClassLoader gcl = new GroovyClassLoader()

        gcl.parseClass """
            class A {
                @Delegate List a
            }
        """

        gcl.parseClass """
            class B {
                List<String> a
            }
        """
    }
}
