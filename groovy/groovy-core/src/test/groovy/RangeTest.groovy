class RangeTest extends GroovyTestCase {

	property x
	
	void testRange() {
	    x = 0

	    for ( i in 0..10 ) {
	        x = x + i
	    }

	    assert x == 45
	}

	void testRangeEach() {
	    x = 0

	    (0..10).each {
	        x = x + it
	    }

	    assert x == 45
	}

	void testRangeStepEach() {
	    x = 0

	    (0..10).step(3) {
	        x = x + it
	    }

	    assert x == 18
	}

	void testRangeStepFor() {
	    x = 0

	    for (it in (0..10).step(3)) {
	        x = x + it
	    }

	    assert x == 18
	}
}
