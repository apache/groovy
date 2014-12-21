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

// tag::inner_class[]
class Outer {
    private String privateStr

    def callInnerMethod() {
        new Inner().methodA()       //<1>
    }

    class Inner {                   //<2>
        def methodA() {
            println "${privateStr}." //<3>
        }
    }
}
// end::inner_class[]

// tag::inner_class2[]
class Outer2 {
    private String privateStr = 'some string'

    def startThread() {
       new Thread(new Inner2()).start()
    }

    class Inner2 implements Runnable {
        void run() {
            println "${privateStr}."
        }
    }
}
// end::inner_class2[]

// tag::anonymous_inner_class[]
class Outer3 {
    private String privateStr = 'some string'

    def startThread() {
        new Thread(new Runnable() {      //<1>
            void run() {
                println "${privateStr}."
            }
        }).start()                       //<2>
    }
}
// end::anonymous_inner_class[]

// tag::abstract_class[]
abstract class Abstract {         //<1>
    String name

    abstract def abstractMethod() //<2>

    def concreteMethod() {
        println 'concrete'
    }
}
// end::abstract_class[]


assert p != null
'''
    }
}
