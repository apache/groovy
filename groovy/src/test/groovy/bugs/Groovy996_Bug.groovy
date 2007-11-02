package groovy.bugs

class Groovy996_Bug extends GroovyTestCase {
    void testAccessToSuperProtectedField() {
        def a = new Groovy996_SubClass()
        a.out()
    }
}

class Groovy996_SuperClass {
    protected String x = 'This is an X'
}

class Groovy996_SubClass extends Groovy996_SuperClass {
    void out() {
       println( x )
    }
}
