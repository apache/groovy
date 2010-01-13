package groovy.bugs

class GroovyInnerEnumBug extends GroovyTestCase {
    static public enum MyEnum { 
        a, b, c
        public static MyEnum[] myenums = [a, b, c];
    }
    
    // GROOVY-3979
    void testEnumInsideAClass3979() {
        assertScript """
            class EnumTest2 {
                enum Direction3979 { North, East, South, West }
                static void main(args) {
                    for (d in Direction3979) { 
                        assert d instanceof Direction3979
                    }
                }
            }
        """
    }

    // GROOVY-3994
    void testEnumInsideAClass3994() {
        assert MyEnum.a.name() == 'a'
        assertTrue Enum.isAssignableFrom(MyEnum.class)
        assert EnumSet.allOf(MyEnum.class) instanceof EnumSet 
    }
}
