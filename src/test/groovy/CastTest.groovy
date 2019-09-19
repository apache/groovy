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

class CastTest extends GroovyTestCase {

    Short b = 1
    
    void testCast() {
        def x = (Short) 5

        assert x.class == Short
        
        methodWithShort(x)
    }
    
    void testImplicitCast() {
        Short x = 6
        
        assert x.class == Short , "Type is ${x.class}"
        
        methodWithShort(x)
        
        x = 7
        assert x.class == Short , "Type is ${x.class}"
    }

    void testImplicitCastOfField() {

        assert b.class == Short , "Type is ${b.class}"
        
        b = 5
        
        assert b.class == Short , "Type is ${b.class}"
    }
    
    void testIntCast() {
        def i = (Integer) 'x'
        
        assert i instanceof Integer
    }
    
    void testCharCompare() {
        def i = (Integer) 'x'
        def c = 'x'
        
        assert i == c
        assert i =='x'
        assert c == 'x'
        assert i == i
        assert c == c

        assert 'x' == 'x'
        assert 'x' == c
        assert 'x' == i
    }
    
    void testCharCast() {
        def c = (Character) 'x'
        
        assert c instanceof Character
        
        c = (Character)10
        
        assert c instanceof Character
    }
    
    void methodWithShort(Short s) {
        assert s.class == Short
    }
    
    void methodWithChar(Character x) {
        def text = "text"
        def idx = text.indexOf(x)
        
        assert idx == 2
    }
    // br
    void testPrimitiveCasting() {
        def d = 1.23
        def i1 = (int)d
        def i2 = (Integer)d
        assert i1.class.name == 'java.lang.Integer'
        assert i2.class.name == 'java.lang.Integer'

        def ch = (char) i1
        assert ch.class.name == 'java.lang.Character'

        def dd = (double)d
        assert dd.class.name == 'java.lang.Double'

    }

    void testAsSet() {
        def mySet = [2, 3, 4, 3] as SortedSet
        assert mySet instanceof SortedSet
        
        // identity test
        mySet = {} as SortedSet
        assert mySet.is ( mySet as SortedSet )
        
        mySet = [2, 3, 4, 3] as Set
        assert mySet instanceof HashSet
        
        // identitiy test
        mySet = {} as Set
        assert mySet.is ( mySet as Set )

        // array test
        mySet = new String[2] as Set // Array of 2 null Strings
        assert mySet instanceof Set
        assert mySet.size() == 1
        assert mySet.iterator().next() == null

        mySet = "a,b".split(",") as Set // Array of 2 different Strings
        assert mySet instanceof Set
        assert mySet.size() == 2
        assert mySet == new HashSet([ "a", "b" ])

        mySet = "a,a".split(",") as Set // Array of 2 different Strings
        assert mySet instanceof Set
        assert mySet.size() == 1
        assert mySet == new HashSet([ "a" ])
    }

    void testCastToAbstractClass() {
        def closure = { 42 }
        def myList = closure as AbstractList
        assert myList[-1] == 42
        assert myList.size() == 42
    }

    void testArrayCast() {
        def a = '1' as Integer
        assert [a, a.class] == [1, Integer]
        def b = '2' as int
        assert [b, b.class] == [2, Integer]
        def c = '100' as Integer
        assert [c, c.class] == [100, Integer]
        def d = '200' as int
        assert [d, d.class] == [200, Integer]
        def e = ['1', '2'] as Integer[]
        assert e == [1, 2]
        assert e.class.componentType == Integer
        def f = ['1', '2'] as int[]
        assert f == [1, 2]
        assert f.class.componentType == int
        def g = ['100', '200'] as Integer[]
        assert g == [100, 200]
        assert g.class.componentType == Integer
        def h = ['100', '200'] as int[]
        assert h == [100, 200]
        assert h.class.componentType == int
        
        def sa = [null,"1"] as String[]
        assert sa[0]==null
        assert sa[1]=="1"
        assert sa.class.componentType == String
    }
    
    void testCastString() {
        // this test must pass on 1.7.x
        // see GROOVY-3978, GROOVY-4657, GROOVY-4669
        def val = "abcde" as byte[]
        assert val == "abcde".bytes
    }

    void testCastEnum() {
        CastEnum val;
        val = 'value1'
        assert val == val.value1
        def i = 2
        val = "value$i"
        assert val == val.value2
    }

    enum CastEnum {
        value1,
        value2
    }
}
