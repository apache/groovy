package groovy.bugs

import java.math.BigInteger;


public class Groovy3163Test extends GroovyTestCase {

public void testSuperOverStatic()
{
    def siws = new Groovy3163SomeImplementorWithStatic()

    assert (1 == siws.build(1)[0])
    
    def c = { -> 'foo ' }
    
//    def s = c as Script
//    assert s.is(siws.build(s)[0])

    assert c.is(siws.build(c)[0])
}

}


class Groovy3163SomeBaseClass {

    public Object build(Integer i) {
        return i;
    }

    public Object build(BigInteger i) {
        return i;
    }

    public Object build(Class c) {
        return c;
    }

    public Object build(Script s) {
        return s;
    }
}

class Groovy3163SomeImplementorWithStatic extends Groovy3163SomeBaseClass {

    // Comment this out, otherwise the super.build(x) calls won't match the members in our parent.

    public static Object build(Closure c) {
        return [c]
    }

    // This one will also block a super.build, but it's the Script one.
    public static Object build(BigDecimal d) {
        return [d]
    }

    public Object build(Integer i) {
        return [super.build(i)]
    }

    public Object build(Script s) {
        return [super.build(s)]
    }

}
