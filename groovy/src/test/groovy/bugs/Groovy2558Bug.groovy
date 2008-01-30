package groovy.bugs

class Groovy2558Bug extends GroovyTestCase {
    void testMe () {
        Person person = new Person()
        String propertyName = 'name'
        person."$propertyName" = 'peter'
        assertEquals "peter", person.name
    }
}
