package org.codehaus.groovy.classgen

class CallSiteTest extends GroovyTestCase {

    void testChangeMetaClass () {
        def obj = new OBJ()
        assertEquals(6, obj.method(3,3))

        ExpandoMetaClass mc = new ExpandoMetaClass(OBJ)
        mc.mutableMethod = { a, b -> a * b }
        mc.initialize()

        obj.metaClass = mc
        assertEquals(9, obj.method(3,3))

        obj.metaClass = null
        assertEquals(6, obj.method(3,3))

        mc = new ExpandoMetaClass(OBJ)
        mc.mutableMethod = { a, b -> a - b }
        mc.initialize ()
        GroovySystem.metaClassRegistry.setMetaClass(OBJ, mc)

        assertEquals(6, obj.method(3,3))

        final OBJ obj2 = new OBJ()
        assertEquals(0, obj2.method(3,3))

        assertEquals(6, obj.method(3,3))

        mc = new ExpandoMetaClass(Integer)
        mc.plus = { Integer b -> delegate * 10*b }
        mc.initialize ()
        GroovySystem.metaClassRegistry.setMetaClass(Integer, mc)

        assertEquals(150, 5 + 3)
        assertEquals(150, obj.method(5,3))
        GroovySystem.metaClassRegistry.removeMetaClass(Integer)
        assertEquals(8, 5 + 3)
        assertEquals(6, obj.method(3,3))

        use(TestCategory) {
            assertEquals(3, obj.method(3,3))
        }
    }
}

class OBJ {
    def method (a,b) {
        mutableMethod a, b
    }

    def mutableMethod (a,b) {
        a + b
    }
}

class TestCategory {
    static def mutableMethod(OBJ obj, a, b) {
        2 * a - b
    }
}