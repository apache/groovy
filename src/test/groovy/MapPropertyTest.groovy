package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapPropertyTest extends GroovyTestCase {

    void testGetAndSetProperties() {
        def m = [ 'name' : 'James', 'location' : 'London', 'id':1 ]
        
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
        def m = [:]
        
        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2
        
        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }
    
    void testMapSubclassing() {
        def c = new MyClass()

        c.id = "hello"
        c.class = 1
        c.myMethod()
        assert c.id == "hello"
        assert c.class == 1
        assert c.getClass() != 1
    }
}

class MyClass extends HashMap {
    def myMethod() {
        assert id == "hello"
        assert this.class == 1
    }
}
