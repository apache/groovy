package groovy

class OptionalReturnTest extends GroovyTestCase {

	def y
	
    void testSingleExpression() {
        def value = foo()
		
        assert value == 'fooReturn'
    }

    void testLastExpressionIsSimple() {
        def value = bar()
        
        assert value == 'barReturn'
    }

    void testLastExpressionIsBooleanExpression() {
        def value = foo2()
        
        assert value

        value = foo3()
        
        assert value == false
    }

    void testLastExpressionIsAssignment() {
        def value = assign()
        
        assert value == 'assignReturn'
        
        value = assignField()
        
        assert value == 'assignFieldReturn'
    }

    void testLastExpressionIsMethodCall() {
        def value = methodCall()
        
        assert value == 'fooReturn'
    }

    void testEmptyExpression() {
        def value = nullReturn()
        
        assert value == null
    }

    //  now this is not a compile time error in jsr-03
    void testVoidMethod() {
        def value = voidMethod()

        assert value == null
    }

    void testNonAssignmentLastExpressions() {
        def value = lastIsAssert()
        
        assert value == null
    }

    def foo() {
        'fooReturn'
    }	
	
    def bar() {
        def x = 'barReturn'
        x
    }
	
    def foo2() {
        def x = 'cheese'
        x == 'cheese'
    }
	
    def foo3() {
        def x = 'cheese'
        x == 'edam'
    }
	
    def assign() {
        def x = 'assignReturn'
    }
	
    def assignField() {
        y = 'assignFieldReturn'
    }
    
    def nullReturn() {
    }

    def lastIsAssert() {
        assert 1 == 1
    }

    def methodCall() {
        foo()
    }
    
    void voidMethod() {
        foo()
    }
}
