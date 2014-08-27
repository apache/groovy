
class A {
    def a() {
        a()
        a(12)
        Integer.method('0xff')
        Integer.someDummyProperty.method('0xff')
        Integer.someDummyProperty*.spreadMethod('0xff')
        Integer.someDummyProperty?.safeMethod('0xff', 12)

        //FIXME check it after bug was fixed.
        // Integer.someDummyProperty.@attributeMethod('0xff', 12)
        Integer.some.dummy.property.path.method('0xff', 12)
    }
}
