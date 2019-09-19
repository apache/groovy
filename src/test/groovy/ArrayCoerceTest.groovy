/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy

import groovy.test.GroovyTestCase

class ArrayCoerceTest extends GroovyTestCase {

    Object[] field
    Long[] numberField
    int[] primitiveField

    void testStaticallyTypedPrimitiveTypeArrays() {
        int[] a = [1, 2, 3]
        assert a instanceof int[]
        assert a.length == 3
    }

    void testStaticallyTypedPrimitiveFieldArrays() {
        primitiveField = [1, 2, 3]

        assert primitiveField instanceof int[]
        assert primitiveField.length == 3
    }


    void testFoo2() {
        def x = [1, 2, 3] as Object[]
        assert x instanceof Object[]
        def c = x.getClass()
        def et = c.componentType
        assert et == Object.class
    }

    void testStaticallyTypedObjectArrays() {
        Object[] b = [1, 2, 3]

        assert b instanceof Object[]
        assert b.length == 3
        def c = b.getClass()
        def et = c.componentType
        assert et == Object.class

    }

    void testStaticallyTypedArrays() {
        Integer[] b = [1, 2, 3]

        assert b instanceof Integer[]
        assert b.length == 3
        def c = b.getClass()
        def et = c.componentType
        assert et == Integer.class

    }

    void testStaticallyTypedObjectFieldArrays() {
        field = [1, 2, 3]

        assert field instanceof Object[]
        assert field.length == 3
    }

    void testStaticallyTypedFieldArrays() {
        numberField = [1, 2, 3]

        assert numberField instanceof Long[]
        assert numberField.length == 3
    }

    void testMakePrimitiveArrayTypes() {
        def x = null

        x = [1, 0, 1] as boolean[]
        assert x instanceof boolean[]
        assert x.length == 3

        x = [1, 2, 3] as byte[]
        assert x.length == 3
        assert x instanceof byte[]

        x = [1, 2, 3] as char[]
        assert x.length == 3
        assert x instanceof char[]

        x = [1, 2, 3] as short[]
        assert x.length == 3
        assert x instanceof short[]

        x = [1, 2, 3] as int[]
        assert x.length == 3
        assert x instanceof int[]

        x = [1, 2, 3] as long[]
        assert x.length == 3
        assert x instanceof long[]

        x = [1, 2, 3] as float[]
        assert x.length == 3
        assert x instanceof float[]

        x = [1, 2, 3] as double[]
        assert x.length == 3
        assert x instanceof double[]
    }



    void testAsObjectArray() {
        def x = [1, 2, 3] as Object[]
        def c = x.getClass()
        def et = c.componentType
        assert et == Object.class

        Integer[] y = [1, 2, 3]
        c = y.getClass()
        et = c.componentType
        assert et == Integer.class
    }

    void testMakeArrayThenCoerceToAnotherType() {
        def x = [1, 2, 3] as int[]
        assert x.size() == 3
        assert x instanceof int[]

        // lets try coerce it into an array of longs
        def y = x as long[]
        assert y instanceof long[]

        def z = y as Object[]
        assert z instanceof Object[]
        def c = z.getClass()
        def et = c.componentType
        assert et == Object.class

        x = y as int[]
        assert x.size() == 3
        assert x instanceof int[]
    }


    void testMakeArrayTypes() {
        def x = null

        x = [1, 0, 1] as Boolean[]
        assert x instanceof Boolean[]
        assert x.length == 3

        x = [1, 2, 3] as Byte[]
        assert x.length == 3
        assert x instanceof Byte[]

        x = [1, 2, 3] as Character[]
        assert x.length == 3
        assert x instanceof Character[]

        x = [1, 2, 3] as Short[]
        assert x.length == 3
        assert x instanceof Short[]

        x = [1, 2, 3] as Integer[]
        assert x.length == 3
        assert x instanceof Integer[]

        x = [1, 2, 3] as Long[]
        assert x.length == 3
        assert x instanceof Long[]

        x = [1, 2, 3] as Float[]
        assert x.length == 3
        assert x instanceof Float[]

        x = [1, 2, 3] as Double[]
        assert x.length == 3
        assert x instanceof Double[]
    }

}
