class WhileLoopTest extends GroovyTestCase {

	void testVerySimpleWhile() {
	    val = doWhileMethod(0, 5)
        println(val)
	}
	
	void testMoreComplexWhile() {
	    x = 0
	    y = 5

	    while ( y > 0 ) {
	        x = x + 1
	        y = y - 1
	    }

	    assert x == 5
	}

	void testDoWhileWhile() {
	    x = 0
	    y = 5

	    do {
	        x = x + 1
	        y = y - 1
	    } 
	    while ( y > 0 )

	    assert x == 5
	}

	doWhileMethod(x, m) {
        while ( x < m ) {
            x = increment(x)
        }

		return x
    }
	
	increment(x) {
	    x + 1
	}
}
