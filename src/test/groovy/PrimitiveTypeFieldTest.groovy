class PrimitiveTypeFieldTest extends GroovyTestCase {

	private int foo
	private static short bar
	
	void testPrimitiveField() {
		setValue()
		
		value = getValue()
		assert value == 1
		
		assert foo == 1
	}
	
	void testStaticPrimitiveField() {
		bar = (Short) 123
		
		assert bar == 123
	}
	
	void testIntLocalVariable() {
		int x = 123
		y = x + 1
		assert y == 124
	}
	
	void testIntParamBug() {
		assert bugMethod(123) == 246
		assert bugMethod2(123) == 246
		
		/* @todo GROOVY-133 */
		closure = { int x | x * 2 }
		assert closure.call(123) == 246

	}
	
	int bugMethod(int x) {
		x * 2
	}
	
	bugMethod2(int x) {
		x * 2
	}
	
	void setValue() {
		foo = 1
	}
	
	getValue() {
		x = foo
		return x
	}
}
