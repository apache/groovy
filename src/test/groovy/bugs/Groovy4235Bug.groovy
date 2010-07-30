package groovy.bugs

class Groovy4235Bug extends GroovyTestCase {
    void testAccessStaticPropInsideClosure() {
        assertScript """
            class Foo4235 {
                static prop = "sadfs"
                static foo() {
                    return { -> this.prop }
                }
            }
            assert Foo4235.foo().call() == 'sadfs'
        """
    }
}