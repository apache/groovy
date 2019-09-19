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
package groovy.lang

import groovy.test.GroovyShellTestCase
import org.codehaus.groovy.reflection.ReflectionCache

class MixinAnnotationTest extends GroovyShellTestCase {

    void testSingleMixinAnnotation () {
        evaluate """

interface Mixed {
  def getA ()
}

@Category(Mixed)
class CategoryToUse {
    static def msg = "under category: "

    def asText () {
        msg + this + ": " + a
    }
}

@Mixin(CategoryToUse)
class ClassToExtend implements Mixed{
    String toString () {
        "object of ClassToExtend"
    }

    def a = "blah"
}

        groovy.test.GroovyTestCase.assertEquals("under category: object of ClassToExtend: blah", new ClassToExtend().asText ())

        boolean failed = false;
        try {
            new Object().asText ()
        }
        catch (MissingMethodException e) {
          failed = true;
        }
        assert failed

        """
    }

    void testMultipleMixinAnnotation () {
        evaluate """
@Category(Object)
class CategoryToUse1 {
    def asText () {
        "under category: " + asBiggerText ()
    }
}

@Category(Object)
class CategoryToUse2 {
    def asBiggerText () {
        "under BIG category: " + this
    }
}

@Mixin([CategoryToUse1, CategoryToUse2])
class ClassToExtend {
    String toString () {
        "object of ClassToExtend"
    }
}

        groovy.test.GroovyTestCase.assertEquals("under category: under BIG category: object of ClassToExtend", new ClassToExtend().asText ())
        """
    }

    protected void tearDown() {
        super.tearDown()
        ReflectionCache.getCachedClass(ArrayList).setNewMopMethods (null)
        ReflectionCache.getCachedClass(List).setNewMopMethods (null)
    }

//    void testOneClass () {
//        List.mixin ListExt
//        ArrayList.mixin ArrayListExt
//        assertEquals 1, [0,1].swap () [0]
//        assertEquals 0, [0,1].swap () [1]
//        assertEquals 0, [0,1].swap ().unswap () [0]
//        assertEquals 1, [0,1].swap ().unswap () [1]
//    }
//
//    void testWithList () {
//        ArrayList.mixin ArrayListExt, ListExt
//        assertEquals 1, [0,1].swap () [0]
//        assertEquals 0, [0,1].swap () [1]
//        assertEquals 0, [0,1].swap ().unswap () [0]
//        assertEquals 1, [0,1].swap ().unswap () [1]
//    }
//
//    void testCombined () {
//        ArrayList.mixin Combined
//        assertEquals 1, [0,1].swap () [0]
//        assertEquals 0, [0,1].swap () [1]
//        assertEquals 0, [0,1].swap ().unswap () [0]
//        assertEquals 1, [0,1].swap ().unswap () [1]
//    }
//
//    void testWithEmc () {
//        ArrayList.metaClass.unswap = {
//            [delegate[1], delegate[0]]
//        }
//        ArrayList.mixin ArrayListExt
//        assertEquals 1, [0,1].swap () [0]
//        assertEquals 0, [0,1].swap () [1]
//        assertEquals 0, [0,1].swap ().unswap () [0]
//        assertEquals 1, [0,1].swap ().unswap () [1]
//    }
//
//    void testGroovyObject () {
//        ObjToTest obj = new ObjToTest ()
//        assertEquals "original", obj.value
//        obj.mixin ObjToTestCategory
//        assertEquals "changed", obj.value
//        assertEquals "original", new ObjToTest ().value
//    }
//
//    void testGroovyObjectWithEmc () {
//        ObjToTest.metaClass.getValue = { ->
//            "emc changed"
//        }
//        ObjToTest obj = new ObjToTest ()
//        assertEquals "emc changed", obj.getValue()
//        obj.mixin ObjToTestCategory
//        assertEquals "changed", obj.value
//        assertEquals "emc changed", new ObjToTest ().value
//    }
//}
//
//class ArrayListExt {
//    static def swap (ArrayList self) {
//        [self[1], self[0]]
//    }
//}
//
//class ListExt {
//    static def unswap (List self) {
//        [self[1], self[0]]
//    }
//}
//
//class Combined {
//    static def swap (ArrayList self) {
//        [self[1], self[0]]
//    }
//
//    static def unswap (List self) {
//        [self[1], self[0]]
//    }
//}
//
//class ObjToTest {
//    def getValue () {
//        "original"
//    }
//}
//
//class ObjToTestCategory {
//    static getValue (ObjToTest self) {
//        "changed"
//    }
}
