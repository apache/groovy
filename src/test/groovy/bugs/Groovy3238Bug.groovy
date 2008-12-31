package groovy.bugs

class Groovy3238Bug extends GroovyTestCase {
    def void testRelativeExactnessToMatchForBigIntegerParam() {
        def obj = new Groovy3238Bug()
        def bi = new BigInteger("1")
        
        Groovy3238Bug.metaClass.m = {Double val -> "Double"}; obj.metaClass = null
        assert obj.m(bi) == "Double"
        Groovy3238Bug.metaClass.m = {double val -> "double"}; obj.metaClass = null
        assert obj.m(bi) == "double" //double should be chosen over Double
        Groovy3238Bug.metaClass.m = {BigDecimal val -> "BigDecimal"}; obj.metaClass = null
        assert obj.m(bi) == "BigDecimal" //BigDecimal should be chosen over Double, double
        Groovy3238Bug.metaClass.m = {Object val -> "Object"}; obj.metaClass = null
        assert obj.m(bi) == "Object" //Object should be chosen over Double, double, BigDecimal
        Groovy3238Bug.metaClass.m = {Number val -> "Number"}; obj.metaClass = null
        assert obj.m(bi) == "Number" //Number should be chosen over Double, double, BigDecimal, Object
        Groovy3238Bug.metaClass.m = {BigInteger val -> "BigInteger"}; obj.metaClass = null
        assert obj.m(bi) == "BigInteger" //BigInteger should be chosen over Double, double, BigDecimal, Object, Number
    }
}
