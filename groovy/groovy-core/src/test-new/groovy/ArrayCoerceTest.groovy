class ArrayCoerceTest extends GroovyTestCase {

/*
    void testFoo2() {
        def x = [1, 2, 3] as Object[]
        dump(x)
    }


    void testFoo() {
        Object[] foo = [1, 2, 3]
        dump(foo)
    }

    Object[] field

    void testAsObjectArray() {
        def x = [1, 2, 3] as Object[]
        def c = x.getClass()
        def et = c.componentType
        assert et == Object.class
        dump(x)

        Integer[] y = [1, 2, 3]
        c = y.getClass()
        et = c.componentType
        assert et == Integer.class
        dump(y)
    }

    void testStaticallyTypedArrays() {
        int[] a = [1, 2, 3]
        assert a instanceof int[]
        assert a.length == 3
        dump(a)

        Object[] b = [1, 2, 3]
        dump(b)
        assert b instanceof Object[]
        assert b.length == 3

    }

    void testStaticallyTypedFieldArrays() {
        field = [1, 2, 3]
        dump(field)

        assert field instanceof Object[]
        assert field.length == 3
    }

*/

    void testMakeArrayThenCoerceToAnotherType() {
        def x = [1, 2, 3] as int[]
        assert x.size() == 3
        assert x instanceof int[]
        dump(x)

        // lets try coerce it into an array of longs
        def y = x as long[]
        assert y instanceof long[]
        dump(y)

        def z = y as Object[]
        assert z instanceof Object[]
        def c = z.getClass()
        def et = c.componentType
        assert et == Object.class
        dump(z)

        x = y as int[]
        assert x.size() == 3
        assert x instanceof int[]
        dump(x)
    }

    void testMakePrimitiveArrayTypes() {
        def x = null

        x = [1, 0, 1] as boolean[]
        assert x instanceof boolean[]
        assert x.length == 3
        dump(x)

        x = [1, 2, 3] as byte[]
        assert x.length == 3
        assert x instanceof byte[]
        dump(x)

        x = [1, 2, 3] as char[]
        assert x.length == 3
        assert x instanceof char[]
        dump(x)

        x = [1, 2, 3] as short[]
        assert x.length == 3
        assert x instanceof short[]
        dump(x)

        x = [1, 2, 3] as int[]
        assert x.length == 3
        assert x instanceof int[]
        dump(x)

        x = [1, 2, 3] as long[]
        assert x.length == 3
        assert x instanceof long[]
        dump(x)

        x = [1, 2, 3] as float[]
        assert x.length == 3
        assert x instanceof float[]
        dump(x)

        x = [1, 2, 3] as double[]
        assert x.length == 3
        assert x instanceof double[]
        dump(x)
    }

    void testMakeArrayTypes() {
        def x = null

        x = [1, 0, 1] as Boolean[]
        assert x instanceof Boolean[]
        assert x.length == 3
        dump(x)

        x = [1, 2, 3] as Byte[]
        assert x.length == 3
        assert x instanceof Byte[]
        dump(x)

        x = [1, 2, 3] as Character[]
        assert x.length == 3
        assert x instanceof Character[]
        dump(x)

        x = [1, 2, 3] as Short[]
        assert x.length == 3
        assert x instanceof Short[]
        dump(x)

        x = [1, 2, 3] as Integer[]
        assert x.length == 3
        assert x instanceof Integer[]
        dump(x)

        x = [1, 2, 3] as Long[]
        assert x.length == 3
        assert x instanceof Long[]
        dump(x)

        x = [1, 2, 3] as Float[]
        assert x.length == 3
        assert x instanceof Float[]
        dump(x)

        x = [1, 2, 3] as Double[]
        assert x.length == 3
        assert x instanceof Double[]
        dump(x)
    }

    void dump(array) {
        println "Array is of type ${array.class} which has element type ${array.class.componentType}"
        for (i in array) {
            println "Contains entry $i of type ${i.class}"
        }
        println()
    }

}
