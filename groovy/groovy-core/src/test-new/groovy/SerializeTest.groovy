import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SerializeTest extends GroovyTestCase {

    void testFoo() {
        foo = new Foo()
        
        println("Created ${foo}")
        
        foo.name = "Gromit"
        foo.location = "Moon"
        
        buffer = write(foo)
        object = read(buffer)
        
        println("Found ${object}")
        println("Found ${object} with name ${object.name} and location ${object.location}")
        assert object != null
        assert object.getMetaClass() != null : "Should have a metaclass!"
        
        assert object.name == "Gromit"
        
        assert object.class.name == "Foo" 
        assert object instanceof Foo
        assert object.location == "Moon"
    }
    
    
    write(object) {
        buffer = new ByteArrayOutputStream()
        out = new ObjectOutputStream(buffer)
        out.writeObject(object)
        out.close()
        return buffer.toByteArray()
    }
    
    read(buffer) {
        input = new ObjectInputStream(new ByteArrayInputStream(buffer))
        object = input.readObject()
        input.close()
        return object
    }

}
