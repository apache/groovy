package groovy

class PrimitiveTypesTest extends GroovyTestCase {

	int getInt() {
		return 1;
	}
	
	short getShort() {
		return 1;
	}
	
	boolean getBoolean() {
		return true;
	}
	
	double getDouble() {
		return 1.0;
	}
	
	float getFloat() {
		return 1.0;
	}
	
	byte getByte() {
		return 1;
	}
	
	long getLong() {
		return 1;
	}

	char getChar() {
		return 'a';
	}
	
	int getNextInt(int i) {
		return i + 1
	}
	
	short getNextShort(short i) {
		return i + 1
	}
	
	void testPrimitiveTypes() {
		assert 1 == getInt()
		assert 1 == getShort()
		assert 1 == getByte()
		assert 1 == getLong()
		assert getBoolean()
		assert getDouble() > 0.99
		assert getFloat() > 0.99
		assert 'a' == getChar()
	}

	void testPrimitiveParameters() {		
		assert getNextInt(1) == 2
		assert 3 == getNextInt(2)
		
		assert getNextShort((Short) 1) == 2
		assert 3 == getNextShort((Short) 2)
	}
		
	static void main(args) {
		new PrimitiveTypesTest().testPrimitiveTypes()
	}
}
