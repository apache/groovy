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

final class MapTest extends GroovyTestCase {

    void testMap() {
        def m = [1:'one', '2':'two', 3:'three']

        assert m.size() == 3
        assert m.get(1) == 'one'
        assert m.get('2') == 'two'
        assert m.get(3) == 'three'

        assert m.containsKey(1)
        assert m.containsKey('2')
        assert m.containsKey(3)

        assert m.containsValue('one')
        assert m.containsValue('two')
        assert m.containsValue('three')

        assert m.keySet().size() == 3
        assert m.values().size() == 3
        assert m.keySet().contains(1)
        assert m.values().contains('one')

        m.remove(1)
        m.remove('2')

        assert m.size() == 1
        assert m.get('1') == null
        assert m.get('2') == null

        m.put('cheese', 'cheddar')

        assert m.size() == 2

        assert m.containsKey('cheese')
        assert m.containsValue('cheddar')


        if ( m.containsKey('cheese') ) {
            // ignore
        }
        else {
            assert false , 'should contain cheese!'
        }

        if ( m.containsKey(3) ) {
            // ignore
        }
        else {
            assert false , 'should contain 3!'
        }
    }

    void testEmptyMap() {
        def m = [:]

        assert m.size() == 0
        assert !m.containsKey('cheese')

        m.put('cheese', 'cheddar')

        assert m.size() == 1
        assert m.containsKey('cheese')
    }

    /**
     * Map "empty" property isn't Map#isEmpty.
     */
    void testMapEmpty() {
        def m = [:].withDefault{ 'entry' }

        assert m.get('empty') == 'entry'
        assert m['empty'] == 'entry'
        assert m.empty == 'entry'
        assert m.Empty == false
    }

    /**
     * Map "class" property isn't Object#getClass.
     */
    void testMapClass() {
        def m = [:].withDefault{ 'entry' }

        assert m.get('class') == 'entry'
        assert m['class'] == 'entry'
        assert m.class == 'entry'
        assert m.Class == 'entry'
    }

    // GROOVY-5001, GROOVY-11367
    void testMapDelegate() {
        for (mode in ['','static']) {
            assertScript """import groovy.transform.*
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode v = 'field'
                    public        $mode w = 'field'
                    protected     $mode x = 'field'
                    @PackageScope $mode y = 'field'
                    private       $mode z = 'field'
                }

                def map = new M()
                assert map.m         === map.@m
                assert map.v         == 'field'
                assert map.w         == 'field'
                assert map.x         == 'entry'
                assert map.y         == 'entry'
                assert map.z         == 'entry'
                assert map.empty     == 'entry'
                assert map.class     == 'entry'
                assert map.metaClass instanceof MetaClass

                map.with {
                    assert v         == 'field'
                    assert w         == 'field'
                    assert x         == 'entry'
                    assert y         == 'entry'
                    assert z         == 'entry'
                    assert empty     == 'entry'
                    assert it.class  == 'entry' // "class" cannot be a variable expression
                    assert metaClass instanceof org.codehaus.groovy.runtime.metaclass.ClosureMetaClass
                }
            """
            assertScript """import groovy.transform.*
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode getV() { 'getter' }
                    public        $mode getW() { 'getter' }
                    protected     $mode getX() { 'getter' }
                    @PackageScope $mode getY() { 'getter' }
                    private       $mode getZ() { 'getter' }
                }

                def map = new M()
                assert map.v == 'getter'
                assert map.w == 'getter'
                assert map.x == 'entry'
                assert map.y == 'entry'
                assert map.z == 'entry'
                map.with {
                    assert v == 'getter'
                    assert w == 'getter'
                    assert x == 'entry'
                    assert y == 'entry'
                    assert z == 'entry'
                }
            """
        }
    }

    void testMapMutation() {
        def k = 'abc'
        def m = [(k): 'xyz', 'def': 123, ghi: null]

        assert m['abc'] == 'xyz'
        assert m['def'] ==  123
        assert m['ghi'] == null
        assert m['jkl'] == null

        assert m.size() == 3

        m['def'] = 'cafebabe'

        assert m['def'] == 'cafebabe'

        assert m.size() == 3

        m.remove('def')

        assert m['def'] == null
        assert m.size() == 2

        def foo = m['def'] = 5
        assert m['def'] == 5
        assert foo == 5

        m['class'] = 'entry'
        m['empty'] = 'entry'

        assert m.size() == 5
    }

    // GROOVY-5001, GROOVY-5491, GROOVY-11367
    void testMapMutation2() {
        for (mode in ['','static']) {
            assertScript """import groovy.transform.*
                class M extends HashMap { // just like GROOVY-662, GROOVY-8065, GROOVY-8074
                    def           $mode v = 'v'
                    public        $mode w = 'w'
                    protected     $mode x = 'x'
                    @PackageScope $mode y = 'y'
                    private       $mode z = 'z'
                }

                def map = new M()
                assert map.@v == 'v'
                assert map.@w == 'w'
                assert map.@x == 'x'
                assert map.@y == 'y'
                assert map.@z == 'z'

                map.v = 'V'
                map.w = 'W'
                map.x = 'X'
                map.y = 'Y'
                map.z = 'Z'

                assert map.@v == 'V'
                assert map.@w == 'W'
                assert map.@x == 'x'
                assert map.@y == 'y'
                assert map.@z == 'z'

                assert map.keySet() == ['x','y','z'].toSet()
            """
            assertScript """import groovy.transform.*
                import static groovy.test.GroovyAssert.*
                class M extends HashMap {
                    def           $mode final v = 'v'
                    public        $mode final w = 'w'
                    protected     $mode final x = 'x'
                    @PackageScope $mode final y = 'y'
                    private       $mode final z = 'z'
                }

                def map = new M()
                shouldFail(ReadOnlyPropertyException) {
                    map.v = 'V'
                }
                shouldFail(ReadOnlyPropertyException) {
                    map.w = 'W'
                }
                map.x = 'X'
                map.y = 'Y'
                map.z = 'Z'

                assert map.@v == 'v'
                assert map.@w == 'w'
                assert map.@x == 'x'
                assert map.@y == 'y'
                assert map.@z == 'z'

                assert map.keySet() == ['x','y','z'].toSet()
            """
        }
    }

    // GROOVY-5001, GROOVY-5491, GROOVY-11367
    void testMapMutation3() {
        for (mode in [''/*,'static'*/]) {
            assertScript """import groovy.transform.*
                class M extends HashMap { // just like GROOVY-662, GROOVY-8065, GROOVY-8074
                    def           $mode void setV(value) { put('setV', value) }
                    public        $mode void setW(value) { put('setW', value) }
                    protected     $mode void setX(value) { put('setX', value) }
                    @PackageScope $mode void setY(value) { put('setY', value) }
                    private       $mode void setZ(value) { put('setZ', value) }
                }

                def map = new M()
                map.v = 'V'
                map.w = 'W'
                map.x = 'X'
                map.y = 'Y'
                map.z = 'Z'

                assert map.v    == null
                assert map.w    == null
                assert map.x    ==  'X'
                assert map.y    ==  'Y'
                assert map.z    ==  'Z'
                assert map.setV ==  'V'
                assert map.setW ==  'W'
                assert map.setX == null
                assert map.setY == null
                assert map.setZ == null
                assert map.keySet() == ['setV','setW','x','y','z'].toSet()
            """
        }
    }

    void testMapLeftShift() {
        def map = [a:1, b:2]
        def other = [c:3]
        def entry = [d:4].iterator().toList()[0]
        map += other
        assert map == [a:1, b:2, c:3]
        map << entry
        assert map == [a:1, b:2, c:3, d:4]
    }

    void testFindAll() {
        assert [a:1] == ['a':1, 'b':2].findAll {it.value == 1}
        assert [a:1] == ['a':1, 'b':2].findAll {it.key == 'a'}
        assert [a:1] == ['a':1, 'b':2].findAll {key,value -> key == 'a'}
        assert [a:1] == ['a':1].findAll {true}
        assert [:]   == ['a':1].findAll {false}
    }

    void testPutAllCollectionMapEntry() {
        def map1 = [a:1, b:2]
        def map2 = [c:3, d:4]
        def map3 = [3:'c', 4:'d']
        def control = map1 + map2

        map1.putAll(map2.entrySet())
        assert map1 == control

        map1 = [a:1, b:2]
        map1.putAll(map3.entrySet().collect{ [it.value, it.key] as MapEntry })
        assert map1 == control

        map1 = [a:1, b:2]
        def values = [3, 4]
        def keys = ['c', 'd']
        def items = [keys, values].transpose()
        map1.putAll(items.collect{ it as MapEntry })
        assert map1 == control
    }

    void testRemoveAll() {
        // given:
        def map1 = [a:1, b:2]
        def map2 = [c:3, d:4]

        // when: 'two parameters = key,value'
        map1.removeAll { k,v ->
            k == 'a'
        }
        // then:
        assert map1 == [b:2]

        // when: 'one parameter = entry'
        map2.removeAll { e ->
            e.value == 3
        }
        // then:
        assert map2 == [d:4]
    }

    void testRetainAll() {
        // given:
        def map1 = [a:1, b:2]
        def map2 = [c:3, d:4]

        // when: 'two parameters = key,value'
        map1.retainAll { k,v ->
            k == 'a'
        }
        // then:
        assert map1 == [a:1]

        // when: 'one parameter = entry'
        map2.retainAll { e ->
            e.value == 3
        }
        // then:
        assert map2 == [c:3]
    }

    void testPlusCollectionMapEntry() {
        def map1 = [a:1, b:2]
        def map2 = [c:3, d:4]
        def map3 = [3:'c', 4:'d']
        def control = map1 + map2

        assert control == map1 + map2.entrySet()
        assert map1 == [a:1, b:2]

        assert control == map1 + map3.entrySet().collect{ [it.value, it.key] as MapEntry }
        assert map1 == [a:1, b:2]

        map1 = [a:1, b:2]
        def values = [3, 4]
        def keys = ['c', 'd']
        def items = [keys, values].transpose()
        assert control == map1 + items.collect{ it as MapEntry }
        assert map1 == [a:1, b:2]
    }

    void testMapSort() {
        def map = [a:100, c:20, b:3]
        def mapByValue = map.sort{ it.value }
        assert mapByValue.collect{ it.key } == ['b', 'c', 'a']
        def mapByKey = map.sort{ it.key }
        assert mapByKey.collect{ it.value } == [100, 3, 20]
    }

    void testMapAdditionProducesCorrectValueAndPreservesOriginalMaps() {
        def left = [a:1, b:2]
        def right = [c:3]
        assert left + right == [a:1, b:2, c:3], 'should contain all entries from both maps'
        assert left == [a:1, b:2] && right == [c:3], 'LHS/RHS should not be modified'
    }

    void testMapAdditionGivesPrecedenceOfOverlappingValuesToRightMap() {
        def left = [a:1, b:1]
        def right = [a:2]
        assert left + right == [a:2, b:1], 'RHS should take precedence when entries have same key'
    }

    void testMapAdditionPreservesOriginalTypeForCommonCases() {
        def other = [c: 3]
        assert ([a: 1, b: 2] as Properties)    + other == [a:1, b:2, c:3] as Properties
        assert ([a: 1, b: 2] as Hashtable)     + other == [a:1, b:2, c:3] as Hashtable
        assert ([a: 1, b: 2] as LinkedHashMap) + other == [a:1, b:2, c:3] as LinkedHashMap
        assert ([a: 1, b: 2] as TreeMap)       + other == [a:1, b:2, c:3] as TreeMap
    }

    void testFlattenUsingClosure() {
        def map = [a: 1, b: 2, c: 3, d: [e: 4, f: 5]]
        def findingNestedMapValues = { it.value instanceof Map ? it.value.entrySet() : it }
        def result = [:].putAll(map.entrySet().flatten(findingNestedMapValues))
        assert result == [a: 1, b: 2, c: 3, e: 4, f: 5]
    }

    void testTreeMapEach() {
        TreeMap map = [c:2, b:3, a:1]
        String result1 = '', result2 = ''
        map.each{ k, v -> result1 += "$k$v " }
        assert result1 == 'a1 b3 c2 '
        map.reverseEach{ e -> result2 += "$e.key$e.value " }
        assert result2 == 'c2 b3 a1 '
    }

    void testMapWithDefault() {
        def m = [:].withDefault {k -> k * 2}
        m[1] = 3
        assert m[1] == 3
        assert m[2] == 4
        assert [1: 3, 2: 4] == m
        assert m == [1: 3, 2: 4]
    }

    void testMapIsCaseWithGrep() {
        def predicate = [apple:true, banana:true, lemon:false, orange:false, pear:true]
        def fruitList = ['apple', 'apple', 'pear', 'orange', 'pear', 'lemon', 'banana']
        def expected = ['apple', 'apple', 'pear', 'pear', 'banana']
        assert fruitList.grep(predicate) == expected
    }

    void testMapIsCaseWithSwitch() {
        switch ('foo') {
            case [foo: true, bar: false]: assert true; break
            default: assert false
        }
        switch ('bar') {
            case [foo: true, bar: false]: assert false; break
            default: assert true
        }
    }

    void testMapWithDefaultCanBeConfiguredToNotStoreDefaultValue() {
        def defaultValue = 0
        def m = [:].withDefault(false, true) { defaultValue }
        assert m.isEmpty()

        m[1] = defaultValue
        assert m.isEmpty()

        m[1] = defaultValue + 1
        assert !m.isEmpty()

        m[1]--
        assert m.isEmpty()

        m.putAll([a:0, b:1, c:0])
        assert m.size() == 1
    }
}
