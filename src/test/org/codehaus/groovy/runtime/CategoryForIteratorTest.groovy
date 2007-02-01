package org.codehaus.groovy.runtime;

/*
 * Test whether the Invoker includes categories when 
 * trying to find an iterator (via the method iterator())
 */ 
class CategoryForIteratorTest extends GroovyTestCase {

	def identity = { val -> val }
	def c
	def countCalls = { c = c + 1 }

	void setUp() {
		c = 0
	}
	
	/*
	 * Ensure that without the iterator category a
	 * one-element collection is returned that
	 * results in one call to the countCalls closure
	 */
	void testWithoutIteratorCategory() {
		identity.each countCalls
		assert c == 1
	}
	/*
	 * When using the IteratorCategory below we get an
	 * iterator that does no iteration. So the count
	 * has to be 0
	 */
	void testWithIteratorCategory() {
		use(IteratorCategory) {
			c = 0
			identity.each countCalls
			assert c == 0
		}
	}
}

/*
 * The category simply adds an iterator()-method returning
 * the null iterator defined below
 */
class IteratorCategory {
	static Iterator iterator(Closure c) { 
		return new TestIterator()
	}
}

/*
 * This iterator returns 0 elements, allowing us to distinguish
 * from the default collection-iterator
 */
class TestIterator implements Iterator {
    public boolean hasNext() { return false }
    public Object next() { return null }
    public void remove() {}
}
