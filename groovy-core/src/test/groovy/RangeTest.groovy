package groovy

class RangeTest extends GroovyTestCase {
	
	void testRange() {
	    def x = 0

	    for ( i in 0..9 ) {
	        x = x + i
	    }

	    assert x == 45
	    
	    x = 0

	    for ( i in 0..<10 ) {
	        x = x + i
	    }

	    assert x == 45
	    
	    x = 0

	    for ( i in 0..'\u0009' ) {
	        x = x + i
	    }

	    assert x == 45
	}
	
	void testRangeEach() {
	    def x = 0

	    (0..9).each {
	        x = x + it
	    }

	    assert x == 45
	    
	    x = 0

	    (0..<10).each {
	        x = x + it
	    }

	    assert x == 45
	}

	void testIntStep() {
	    assertStep(0..9, 3, [0, 3, 6, 9])
	    assertStep(0..<10, 3, [0, 3, 6, 9])
	    
	    assertStep(9..0, 3, [9, 6, 3, 0])
	    assertStep(9..<0, 3, [9, 6, 3])
	}
	
	void testObjectStep() {
	    assertStep('a'..'f', 2, ['a', 'c', 'e'])
	    assertStep('a'..<'e', 2, ['a', 'c'])
	    
	    assertStep('z'..'v', 2, ['z', 'x', 'v'])
	    assertStep('z'..<'v', 2, ['z', 'x'])
	}
	
	void testIterateIntRange() {
	    assertIterate(0..9, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
	    assertIterate(1..<8, [1, 2, 3, 4, 5, 6, 7])
	    assertIterate(7..1, [7, 6, 5, 4, 3, 2, 1])
	    assertIterate(6..<1, [6, 5, 4, 3, 2])
	}
	
	void testIterateObjectRange() {
	    assertIterate('a'..'d', ['a', 'b', 'c', 'd'])
	    assertIterate('a'..<'d', ['a', 'b', 'c'])
	    assertIterate('z'..'x', ['z', 'y', 'x'])
	    assertIterate('z'..<'x', ['z', 'y'])
	}
	
	void testRangeContains() {
	    def range = 0..10
	    assert range.contains(0)
	    assert range.contains(10)
	    
	    range = 0..<5
	    assert range.contains(0)
	    assert ! range.contains(5)
	}
	
	void testBackwardsRangeContains() {
	    def range = 10..0
	    assert range.contains(0)
	    assert range.contains(10)
	    
	    range = 5..<1
	    assert range.contains(5)
	    assert ! range.contains(1)
	}
	
	void testObjectRangeContains() {
	    def range = 'a'..'x'
	    assert range.contains('a')
	    assert range.contains('x')
	    assert range.contains('z') == false
	    
	    range = 'b'..<'f'
	    assert range.contains('b')
	    assert ! range.contains('g')
	    assert ! range.contains('f')
	    assert ! range.contains('a')
	}
	
	void testBackwardsObjectRangeContains() {
	    def range = 'x'..'a'
	    assert range.contains('a')
	    assert range.contains('x')
	    assert range.contains('z') == false
	    
	    range = 'f'..<'b'
	    assert ! range.contains('g')
	    assert range.contains('f')
	    assert range.contains('c')
	    assert ! range.contains('b')
	}
	
	void testIntRangeToString() {
	    assertToString(0..10, "0..10")
	    assertToString([1, 4..10, 9], "[1, 4..10, 9]")
	    
	    assertToString(0..<11, "0..10")
	    assertToString([1, 4..<11, 9], "[1, 4..10, 9]")
	    
	    
	    assertToString(10..0, "10..0")
	    assertToString([1, 10..4, 9], "[1, 10..4, 9]")
	    
	    assertToString(11..<0, "11..1")
	    assertToString([1, 11..<4, 9], "[1, 11..5, 9]")
	}
	
	void testObjectRangeToString() {
	    assertToString('a'..'d', 'a..d', '"a".."d"')
	    assertToString('a'..<'d', 'a..c', '"a".."c"')
	    assertToString('z'..'x', 'z..x', '"z".."x"')
	    assertToString('z'..<'x', 'z..y', '"z".."y"')
	}
	
	void testRangeSize() {
	    assertSize(1..10, 10)
	    assertSize(11..<21, 10)
	    assertSize(30..21, 10)
	    assertSize(40..<30, 10)
	}

	void testBorderCases() {
	    assertIterate(0..1, [0,1])
	    assertIterate(0..0, [0])
	    assertIterate(0..-1, [0,-1])
	    assertIterate(0..<-1, [0])
	}

	void testEmptyRanges(){
	    assertSize(0..<0, 0)
	    assertSize(1..<1, 0)
	    assertSize(-1..<-1, 0)
	    assertSize('a'..<'a', 0)
	    assertSize(0.0G..<0.0G, 0)
	    (0..<0).each{assert false}
	    (0..<0).step(1){assert false}
	    for (i in 0..<0) assert false
	    assertToString(0..<0, '0..<0', '0..<0')
	    assertToString('a'..<'a', 'a..<a', '"a"..<"a"')
	    assertToString(null..<null, 'null..<null', 'null..<null')
	    assertStep(0..<0, 3, [])
	    assertIterate(0..<0, [])
	}
	
	void testStringRange() {
	    def range = 'a'..'d'
	    
	    def list = []
	    range.each { list << it }
	    assert list == ['a', 'b', 'c', 'd']
	    
	    def s = range.size()
	    assert s == 4
	}
	
	void testBackwardsStringRange() {
	    def range = 'd'..'a'
	    
	    def list = []
	    range.each { list << it }
	    assert list == ['d', 'c', 'b', 'a']
	    
	    def s = range.size()
	    assert s == 4
	}
	
	protected void assertIterate(range, expected) {
	    def list = []
	    for (it in range) {
	        list << it
	    }
	    assert list == expected , "for loop on ${range}"
	    
		list = []
	    range.each { list << it}
	    assert list == expected , "each() on ${range}"
	}
	
	protected void assertSize(range, expected) {
	    def size = range.size()
	    assert size == expected , range
	}
	
	protected void assertToString(range, expected) {
	    def text = range.toString()
	    assert text == expected , "toString() for ${range}"
	    text = range.inspect()
	    assert text == expected , "inspect() for ${range}"
	}
	
	protected void assertToString(range, expectedString, expectedInspect) {
	    def text = range.toString()
	    assert text == expectedString , "toString() for ${range}"
	    text = range.inspect()
	    assert text == expectedInspect , "inspect() for ${range}"
	}
	
	protected void assertStep(range, stepValue, expected) {
	    def list = []
	    range.step(stepValue) {
	        list << it
	    }
	    assert list == expected

	    list = []
	    for (it in range.step(stepValue)) {
	        list << it
	    }
	    assert list == expected
	}
}
