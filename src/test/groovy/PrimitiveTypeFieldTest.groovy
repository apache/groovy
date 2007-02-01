package groovy

class PrimitiveTypeFieldTest extends GroovyTestCase {
    private long longField
    private static short shortField

    void setValue() {
        longField = 1
    }

    def getValue() {
        def x = longField
        return x
    }

    void testPrimitiveField() {
        setValue()

        def value = getValue()
        assert value == 1

        assert longField == 1
    }

    void testIntParamBug() {
        assert bugMethod(123) == 246
        assert bugMethod2(123) == 246

        // @todo GROOVY-133
        def closure = {int x-> x * 2 }
        assert closure.call(123) == 246

    }

    int bugMethod(int x) {
        x * 2
    }

    def bugMethod2(int x) {
        x * 2
    }
    void testStaticPrimitiveField() {
        shortField = (Short) 123

        assert shortField == 123
    }

    void testIntLocalVariable() {
        int x = 123
        def y = x + 1
        assert y == 124
    }

    void testLongLocalVariable() {
        long x = 123
        def y = x + 1
        assert y == 124
    }
}
