package groovy

class SerializeTest extends GroovyTestCase {

    void testFoo() {
        def foo = new Foo()
        
        println("Created ${foo}")
        
        foo.name = "Gromit"
        foo.location = "Moon"
        
        def buffer = write(foo)
        def object = read(buffer)
        
        println("Found ${object}")
        println("Found ${object} with name ${object.name} and location ${object.location}")
        assert object != null
        assert object.getMetaClass() != null , "Should have a metaclass!"
        
        assert object.name == "Gromit"
        
        assert object.class.name == "groovy.Foo" 
        assert object instanceof Foo
        assert object.location == "Moon"
    }
    
    
    def write(object) {
        def buffer = new ByteArrayOutputStream()
        def out = new ObjectOutputStream(buffer)
        out.writeObject(object)
        out.close()
        return buffer.toByteArray()
    }
    
    def read(buffer) {
        def input = new ObjectInputStream(new ByteArrayInputStream(buffer))
        def object = input.readObject()
        input.close()
        return object
    }

}
