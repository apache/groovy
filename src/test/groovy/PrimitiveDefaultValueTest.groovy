/**
 * @TODO: GROOVY-1037
 *
 *    $Revision 1.0
 *    Test for non-initialized fields or variables of the primitive types.
 *
 * @author Pilho Kim
 */

class PrimitiveDefaultValueTest extends GroovyTestCase {

    private int x
    private long y
    private double z
    private byte b
    private short s
    private float f
    private boolean flag
    private char c

    void testThisPrimitiveDefaultValues() {
        this.x == 0
        this.y == 0L
        this.z == 0.0
        this.b == (byte) 0
        this.s == (short) 0
        this.f == 0.0F
        this.flag == false
        this.c == (char) 0
    }

    void testPrimitiveDefaultValues() {
        def a = new ClassForPrimitiveDefaultValue()
        a.x == 0
        a.y == 0L
        a.z == 0.0
        a.b == (byte) 0
        a.s == (short) 0
        a.f == 0.0F
        a.flag == false
        a.c == (char) 0
    }

    void testDefaultPrimitiveValuesForAttributes() {
        def a = new ClassForPrimitiveDefaultValue()
        a.@x == 0
        a.@y == 0L
        a.@z == 0.0
        a.@b == (byte) 0
        a.@s == (short) 0
        a.@f == 0.0F
        a.@flag == false
        a.@c == (char) 0
    }

    void testDefaultPrimitiveValuesForProperties() {
        def a = new ClassForPrimitiveDefaultValue()
        a.x1 == 0
        a.y1 == 0L
        a.z1 == 0.0
        a.b1 == (byte) 0
        a.s1 == (short) 0
        a.f1 == 0.0F
        a.flag1 == false
        a.c1 == (char) 0
    }

  /*
    public static void main(String[] args) {
        def app = new PrimitiveDefaultValueTest()
        app.testThisPrimitiveDefaultValues()
        app.testPrimitiveDefaultValues()
        app.testDefaultPrimitiveValuesForAttributes()
        app.testDefaultPrimitiveValuesForProperties()
    }
  */
}

class ClassForPrimitiveDefaultValue {
    int x
    long y
    double z
    byte b
    short s
    float f
    boolean flag
    char c

    @Property int x1
    @Property long y1
    @Property double z1
    @Property byte b1
    @Property short s1
    @Property float f1
    @Property boolean flag1
    @Property char c1
}


