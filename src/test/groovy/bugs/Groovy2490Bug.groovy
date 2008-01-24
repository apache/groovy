package groovy.bugs

class Groovy2490Bug extends GroovyTestCase {
    void test () {
        System.out.println("One.foo = " + One.foo);
        System.out.println("Two.foo = " + Two.foo);
        assertEquals One.foo, "hello"
        assertEquals Two.foo, "goodbye"
    }
}

class One {

    static String foo = "hello";
}

class Two extends One{

    static String foo = "goodbye";
}