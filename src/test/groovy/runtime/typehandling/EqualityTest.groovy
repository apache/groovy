package groovy.runtime.typehandling

class EqualityTest extends GroovyTestCase {

    void testEquality() {
        def classA = new EqualityTestClassA(1, "Test")
        def classB = new EqualityTestClassB(1, "Test")

        assert classA == classB
        assert classA.equals(classB)
    }
}
