class OptionalReturnTest extends GroovyTestCase {

	@Property y
	
    void testSingleExpression() {
        value = foo()
		
        assert value == 'fooReturn'
    }

    void testLastExpressionIsSimple() {
        value = bar()
        
        assert value == 'barReturn'
    }

    void testLastExpressionIsBooleanExpression() {
        value = foo2()
        
        assert value

        value = foo3()
        
        assert value == false
    }

    void testLastExpressionIsAssignment() {
        value = assign()
        
        assert value == 'assignReturn'
        
        value = assignField()
        
        assert value == 'assignFieldReturn'
    }

    void testLastExpressionIsMethodCall() {
        value = methodCall()
        
        assert value == 'fooReturn'
    }

    void testEmptyExpression() {
        value = nullReturn()
        
        assert value == null
    }

//  now is  a compile time error
//    void testVoidMethod() {
//        value = voidMethod()
//
//        assert value == null
//    }

    void testNonAssignmentLastExpressions() {
        value = lastIsAssert()
        
        assert value == null
    }

    foo() {
        'fooReturn'
    }	
	
    bar() {
        x = 'barReturn'
        x
    }
	
    foo2() {
        x = 'cheese'
        x == 'cheese'
    }
	
    foo3() {
        x = 'cheese'
        x == 'edam'
    }
	
    assign() {
        x = 'assignReturn'
    }
	
    assignField() {
        y = 'assignFieldReturn'
    }
    
    nullReturn() {
    }

    lastIsAssert() {
        assert 1 == 1
    }

    methodCall() {
        foo()
    }
    
    void voidMethod() {
        foo()
    }
}
