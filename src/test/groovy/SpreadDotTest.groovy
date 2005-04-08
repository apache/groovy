// SpreadDotTest.groovy
//
//   Test for the spread dot operator "*."
//   For an example,
//            list*.property
//        equals to
//            list.collect { it?.property }

/**
 *  @author  Pilho Kim   phkim@cluecom.co.kr
 */

public class SpreadDotTest extends GroovyTestCase {
    public void testSpreadDot() {
        m1 = ["a":1, "b":2]
        m2 = ["a":11, "b":22]
        m3 = ["a":111, "b":222]
        x = [m1,m2,m3]
        println x*.a
        println x*."a"
        assert x == [m1, m2, m3]

        m4 = null
        x << m4
        println x*.a
        println x*."a"
        assert x == [m1, m2, m3, null]

        d = new SpreadDotDemo()
        x << d
        println x*."a"
        assert x == [m1, m2, m3, null, d]

        y = new SpreadDotDemo2()
        println y."a"
        println y.a

        x << y
        println x*."a"
        assert x == [m1, m2, m3, null, d, y]
    }

    public void testSpreadDot2() {
        a = new SpreadDotDemo()
        b = new SpreadDotDemo2()
        x = [a, b]

        println ([a,b]*.fnB("1"))
        assert [a,b]*.fnB("1") == [a.fnB("1"), b.fnB("1")]

        println ([a,b]*.fnB())
        assert [a,b]*.fnB() == [a.fnB(), b.fnB()]
    }
}

class SpreadDotDemo {
    public java.util.Date getA() {
        return new Date()
    }
    public String fnB() {
        return "bb"
    }
    public String fnB(String m) {
        return "BB$m"
    }
}

class SpreadDotDemo2 {
    public String getAttribute(String key) {
        return "Attribute $key"
    }
    public String get(String key) {
        return getAttribute("Get $key")
    }
    public String fnB() {
        return "cc"
    }
    public String fnB(String m) {
        return "CC$m"
    }
}
