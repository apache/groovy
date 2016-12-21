
interface Greetable {
    String name();
    default String hello() {
        return 'hello'
    }
    default public String sayHello() {
        return this.hello() + ', ' + this.name()
    }
}

class Person implements Greetable {
    @Override
    public String name() {
        return 'Daniel'
    }
}

def p = new Person()
assert 'hello, Daniel' == "${p.hello()}, ${p.name()}"
assert 'hello, Daniel' == p.sayHello()