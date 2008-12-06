package groovy.bugs

class Groovy3135Bug extends GroovyTestCase {
    static Byte b = Byte.parseByte("1")
    static Short s = Short.parseShort("2")
    static Integer i = Integer.parseInt("3")
    static Long l = Long.parseLong("4")
    static Float f = Float.parseFloat("5")
    static Double d = Double.parseDouble("6")
    static BigInteger bi = new BigInteger("7")
    static BigDecimal bd = new BigDecimal("8")

    def values

    void testConversionForPrimitiveTypeVarArgs() {
        
        setVarArgsShort("", b, s)
        checkConversionAndVarArgCount(Short.TYPE, 2)

        setVarArgsInteger("", b, s, i)
        checkConversionAndVarArgCount(Integer.TYPE, 3)

        setVarArgsLong("", b, s, i, l)
        checkConversionAndVarArgCount(Long.TYPE, 4)
        
        setVarArgsFloat("", b, s, i, l, f)
        checkConversionAndVarArgCount(Float.TYPE, 5)
        
        setVarArgsDouble("", b, s, i, l, f, d, bi, bd)
        checkConversionAndVarArgCount(Double.TYPE, 8)
    }

    def setVarArgsShort(String str, short... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsInteger(String str, int... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsLong(String str, long... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsFloat(String str, float... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsDouble(String str, double... varArgValues) {
        values = varArgValues
    }

    def checkConversionAndVarArgCount(expectedType, varArgsCount) {
        assert values.class.componentType == expectedType
        assert values.size() == varArgsCount
    }
}
