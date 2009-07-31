package org.codehaus.groovy.runtime

import org.codehaus.groovy.runtime.metaclass.*

class MemoryAwareConcurrentReadMapTest extends GroovyTestCase {

    void testRemove() {
        def map = new MemoryAwareConcurrentReadMap(4, 100)
        def keys = [new MapCollisionKey(),new MapCollisionKey(),new MapCollisionKey()]
        def values = []
        keys.eachWithIndex {it,i-> 
          map.put(it,i)
          values << i
        }
        
        assert map.size() == 3
        
        map.remove(keys[1])
        assert map.get(keys[0]) == 0
        assert map.get(keys[1]) == null
        assert map.get(keys[2]) == 2  
        assert map.size() == 2
        
        map.remove(keys[1])
        assert map.get(keys[0]) == 0
        assert map.get(keys[1]) == null
        assert map.get(keys[2]) == 2  
        assert map.size() == 2
        
        map.remove(keys[0])
        assert map.get(keys[0]) == null
        assert map.get(keys[1]) == null
        assert map.get(keys[2]) == 2  
        assert map.size() == 1
        
        map.remove(keys[2])
        assert map.get(keys[0]) == null
        assert map.get(keys[1]) == null
        assert map.get(keys[2]) == null
        assert map.size() == 0
       
    }
}

class MapCollisionKey {
    // equals only if identitiy, hashcode always the same
    int hashCode(){1}
}