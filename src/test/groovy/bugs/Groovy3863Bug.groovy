package groovy.bugs

class Groovy3863Bug extends GroovyTestCase {
    void testClassNameAccessInMainMethod() {
        assertScript """
            class Foo3863 {
                static main(args) {
                    println this.name
                    assert this.name.contains('Foo3863') == true
                }
            }
        """
    }
}