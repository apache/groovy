package groovy.bugs;

public class Groovy3799Helper {
	private final Foo3799[] foos;

    public Groovy3799Helper(Foo3799... foos){
        this.foos = foos;
    }

    public Groovy3799Helper(String x, String y, Foo3799... foos) {
        this.foos = foos;
    }

    public Foo3799[] getFoos() {
        return foos;
    }
}

interface Foo3799 {}

class AbstractFoo3799 implements Foo3799 {}

class ConcreteFoo3799 extends AbstractFoo3799 {}

class UnrelatedFoo3799 implements Foo3799 {}
