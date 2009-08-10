package groovy.bugs

class Groovy3658Bug extends GroovyTestCase {
    void testConstructorWithParameterWithInitialValueAsStaticMethodCallResult() {
        Groovy3658BugHelper bug2 = new Groovy3658BugHelper('person', 'tag')
        assert bug2.dump() != null
    }
}

class Groovy3658BugHelper {
    Groovy3658BugHelper(final String name1, final String name2 = f(name1)) { 
        this.name1 = name1
        this.name2 = name2
    }
    static String f(String s) { 
        s 
    }
    final String name1, name2
}
