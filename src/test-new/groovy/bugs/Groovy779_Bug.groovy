package bugs


public class Groovy779_Bug extends GroovyTestCase {

    def boolean exceptionCalled = false
    def boolean finallyCalled = false

    public void testFieldProperty() {

        exceptionCalled = false
        finallyCalled = false

        try {
            p = new Person(nameID:"Dave Ford", age:12.2)
            assert p.age == 12
            assert p.nameID == "Dave Ford"
            p = new Person(nameID:"Dave Ford", age:"12")
            println p.age
            println p.nameID
        }
        catch (TypeMismatchException e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert exceptionCalled , "should have invoked the catch clause"
        assert finallyCalled , "should have invoked the finally clause"
        println("Success!")
    }

    public void testBeanProperty() {
        exceptionCalled = false
        finallyCalled = false

        try {
            p2 = new AnotherPerson(nameID:1234, age:12.2)
            assert p2.age == 12
            assert p2.nameID == "1234"
            p2 = new AnotherPerson(nameID:111, age:"12")
            println p2.age
            println p2.nameID
        }
        catch (TypeMismatchException e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert exceptionCalled , "should have invoked the catch clause"
        assert finallyCalled , "should have invoked the finally clause"
        println("Success!")
    }

    public void testAutoboxingProperty() {
        p = new Profit(signal:"abcd", rate:15)
        assert p.signal == "abcd"
        assert p.rate == 15.0

        p = new Profit(signal:111+22, rate:new java.math.BigDecimal("15"))
        assert p.signal == "133"
        assert p.rate == 15.0

        p2 = new AnotherProfit(signal:"abcd", rate:15)
        assert p2.signal == "abcd"
        assert p2.rate == 15.0

        p2 = new AnotherProfit(signal:111-22, rate:new java.math.BigDecimal("15"))
        assert p2.signal == "89"
        assert p2.rate == 15.0
    }

    void onException(e) {
        assert e != null
        exceptionCalled = true
    }
	
    void onFinally() {
        finallyCalled = true
    }

}

class Person {
   def public String nameID
   def public int age
}

class AnotherPerson {
   @Property def public String nameID
   @Property def public int age
}

class Profit {
   public String signal
   public double rate
}

class AnotherProfit {
   @Property public String signal
   @Property public double rate
}
