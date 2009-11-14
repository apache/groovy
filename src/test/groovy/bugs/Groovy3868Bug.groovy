package groovy.bugs

class Groovy3868Bug extends GroovyTestCase {
    void testAsTypeCallWithPrimitiveType() {
        assert "1".asType(Long.TYPE) == "1" as Long
        assert "1".asType(Integer.TYPE) == "1" as Integer
        assert "1".asType(Short.TYPE) == "1" as Short
        assert "1".asType(Character.TYPE) == "1" as Character
        assert "1".asType(Byte.TYPE) == "1" as Byte
        assert "1".asType(Double.TYPE) == "1" as Double
        assert "1".asType(Float.TYPE) == "1" as Float
    }
}
