class MapTest extends GroovyTestCase {

    void testMap() {

        m = [1:'one', '2':'two', 3:'three']

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
        m = [:]

        assert m.size() == 0
        assert !m.containsKey("cheese")

        m.put("cheese", "cheddar")

        assert m.size() == 1
        assert m.containsKey("cheese")
    }
    
    void testMapMutation() {    
        m = [ 'abc' : 'def', 'def' : 134, 'xyz' : 'zzz' ]

        assert m['unknown'] == null

        assert m['def'] == 134

        println(m['xyz'])
        
        m['def'] = 'cafebabe'

        assert m['def'] == 'cafebabe'

        assert m.size() == 3

        m.remove('def')

        assert m['def'] == null
        assert m.size() == 2
    }
}
