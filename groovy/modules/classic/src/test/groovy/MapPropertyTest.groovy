/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapPropertyTest extends GroovyTestCase {

    void testGetAndSetProperties() {
        m = [ 'name' : 'James', 'location' : 'London', 'id':1 ]
        
        assert m.name == 'James'
        assert m.location == 'London'
        assert m.id == 1
        
        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2
        
        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }

    void testSetupAndEmptyMap() {
        m = [:]
        
        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2
        
        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }
}
