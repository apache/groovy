package groovy.bugs

class Groovy5109Bug extends GroovyTestCase {
    void testShouldNotThrowArrayOutOfBounds() {
        assertScript '''
        class C1 {
            class A {}
        }

        class C2 extends C1 {
            { new B() }
            class B extends C1.A {}
        }
        new C2()
        '''
    }
}
