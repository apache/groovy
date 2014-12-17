class ClassTest extends GroovyTestCase {

    void testClass() {
        assertScript '''// tag::class_definition[]
class Person {                       //<1>

    String name                      //<2>
    Integer age

    def increaseAge(Integer years) { //<3>
        this.age += years
    }
}
// end::class_definition[]

// tag::class_instantiation[]
def p = new Person()
// end::class_instantiation[]

assert p != null
'''
    }
}
