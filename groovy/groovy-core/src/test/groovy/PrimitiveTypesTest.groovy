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
	
	char getChar() {
		return 1;
	}
	
	long getLong() {
		return 1;
	}
	
	void testPrimitiveTypes() {
		assert 1 == getInt()
		/*
		assert new Short(1) == getShort()
		assert new Byte(1) == getByte()
		assert new Character(1) == getChar()
		assert new Long(1) == getLong()
		assert getBoolean()
		assert getDouble > 0.99
		assert getFloat > 0.99
		*/
	}
	
}
