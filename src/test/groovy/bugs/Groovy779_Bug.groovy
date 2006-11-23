package groovy.bugs

class Groovy779_Bug extends GroovyTestCase {

    def boolean exceptionCalled = false
    def boolean finallyCalled = false

    public static void main(String[] args) {
        Groovy779_Bug app = new Groovy779_Bug()
        app.testFieldProperty()
        app.testBeanProperty()
        app.testAutoboxingProperty()
    }

    public void testFieldProperty() {

        try {
            def p = new Groovy779OnePerson(nameID:"foo-", age:12.2)
            assert p.age == 12
            assert p.nameID == "foo-"
            p = new Groovy779OnePerson(nameID:"foo-", age:"12")
            println p.age
            println p.nameID
        }
        catch (ClassCastException e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert exceptionCalled , "should have invoked the catch clause"
        assert finallyCalled , "should have invoked the finally clause"
        // println("Success!")
    }

    public void testBeanProperty() {

        try {
            def p2 = new Groovy779AnotherPerson(nameID:1234, age:12.2)
            assert p2.age == 12
            assert p2.nameID == "1234"
            p2 = new Groovy779AnotherPerson(nameID:111, age:"12")
            println p2.age
            println p2.nameID
        }
        catch (ClassCastException e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert exceptionCalled , "should have invoked the catch clause"
        assert finallyCalled , "should have invoked the finally clause"
        // println("Success!")
    }

    public void testAutoboxingProperty() {
        def p = new Groovy779OneProfit(signal:"bar", rate:15)
        assert p.signal == "bar"
        assert p.rate == 15.0

        p = new Groovy779OneProfit(signal:111+22, rate:new java.math.BigDecimal("15"))
        assert p.signal == "133"
        assert p.rate == 15.0

        def p2 = new Groovy779AnotherProfit(signal:"bar~", rate:15)
        assert p2.signal == "bar~"
        assert p2.rate == 15.0

        p2 = new Groovy779AnotherProfit(signal:111-22, rate:new java.math.BigDecimal("15"))
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

class Groovy779OnePerson {
   def public String nameID
   def public int age
}

class Groovy779AnotherPerson {
   String nameID
   int age
}

class Groovy779OneProfit {
   public String signal
   public double rate
}

class Groovy779AnotherProfit {
   String signal
   double rate
}
