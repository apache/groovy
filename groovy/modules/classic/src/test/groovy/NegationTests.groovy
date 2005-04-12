class NegationTests extends GroovyTestCase {

	void testNegateInteger() {
		a = -1
		assert a == -1
	}

	void testNegateIntegerExpression() {
		a = -1
		a = -a
		assert a == 1
	}

	void testNegateDouble() {
		a = -1.0
		assert a == -1.0
	}

	void testNegateDoubleExpression() {
		a = -1.0
		a = -a
		assert a == 1.0
	}

}
