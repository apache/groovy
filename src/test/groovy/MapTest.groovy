package groovy

class MapTest extends GroovyTestCase {

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

        assert m.containsKey("cheese")
        assert m.containsValue("cheddar")


        if ( m.containsKey("cheese") ) {
            // ignore
        }
        else {
            assert false , "should contain cheese!"
        }

        if ( m.containsKey(3) ) {
            // ignore
        }
        else {
            assert false , "should contain 3!"
        }
    }

    void testEmptyMap() {
        def m = [:]

        assert m.size() == 0
        assert !m.containsKey("cheese")

        m.put("cheese", "cheddar")

        assert m.size() == 1
        assert m.containsKey("cheese")
    }
    
    void testMapMutation() {    
        def m = [ 'abc' : 'def', 'def' : 134, 'xyz' : 'zzz' ]

        assert m['unknown'] == null

        assert m['def'] == 134

        m['def'] = 'cafebabe'

        assert m['def'] == 'cafebabe'

        assert m.size() == 3

        m.remove('def')

        assert m['def'] == null
        assert m.size() == 2
        
        def foo = m['def'] = 5
        assert m['def'] == 5
//  it is not valid any more
//        assert foo == null
        assert foo == 5
    }

    void testMapLeftShift(){
        def map = [a:1, b:2]
        def other = [c:3]
        def entry = [d:4].iterator().toList()[0]
        map += other
        assert map == [a:1, b:2, c:3]
        map << entry
        assert map == [a:1, b:2, c:3, d:4]
    }

    void testFindAll(){
        assert [a:1] == ['a':1, 'b':2].findAll {it.value == 1}
        assert [a:1] == ['a':1, 'b':2].findAll {it.key == 'a'}
        assert [a:1] == ['a':1, 'b':2].findAll {key,value -> key == 'a'}
        assert [a:1] == ['a':1].findAll {true}
        assert [:]   == ['a':1].findAll {false}
    }

    void testMapSort(){
        def map = [a:100, c:20, b:3]
        def mapByValue = map.sort{ it.value }
        assert mapByValue.collect{ it.key } == ['b', 'c', 'a']
        def mapByKey = map.sort{ it.key }
        assert mapByKey.collect{ it.value } == [100, 3, 20]
    }

    void testMapAdditionProducesCorrectValueAndPreservesOriginalMaps() {
        def left = [a:1, b:2]
        def right = [c:3]
        assert left + right == [a:1, b:2, c:3], "should contain all entries from both maps"
        assert left == [a:1, b:2] && right == [c:3], "LHS/RHS should not be modified"
    }

    void testMapAdditionGivesPrecedenceOfOverlappingValuesToRightMap() {
        def left = [a:1, b:1]
        def right = [a:2]
        assert left + right == [a:2, b:1], "RHS should take precedence when entries have same key"
    }

    void testMapAdditionPreservesOriginalTypeForCommonCases() {
        def other = [c: 3]
        assert ([a: 1, b: 2] as Properties)    + other == [a:1, b:2, c:3] as Properties
        assert ([a: 1, b: 2] as Hashtable)     + other == [a:1, b:2, c:3] as Hashtable
        assert ([a: 1, b: 2] as LinkedHashMap) + other == [a:1, b:2, c:3] as LinkedHashMap
        assert ([a: 1, b: 2] as TreeMap)       + other == [a:1, b:2, c:3] as TreeMap
    }
}
