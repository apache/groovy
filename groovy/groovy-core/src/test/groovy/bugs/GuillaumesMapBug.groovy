/**
 * @author Guillaume Laforge
 * @version $Revision$
 */
class GuillaumesMapBug extends GroovyTestCase {
    
    void testBug2() {
        list = [1, 2, 3]
        map = [:]
        
        doLoop(list, map)
    
        assert map[0] == 1 
        assert map[1] == 2 
        assert map[2] == 3 
    }
    
    void doLoop(list, map) {
        i = 0
        for (it in list) {
            map[i++] = it
        }
    }
    
    
    void testBug() {
        list = [1, 2, 3]
        map = [:]
        doClosureLoop(list, map)
        
        assert map[0] == 1 
        assert map[1] == 2 
        assert map[2] == 3 
    }
    
    void doClosureLoop(list, map) {
        i = 0
        list.each { map[i++] = it }
    }    
}