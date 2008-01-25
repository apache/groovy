package groovy

class SerializeTest extends GroovyTestCase {

    void testGString () {
        def a = 2, b = 2
        def gs = "${a + b} = ${b * a}"

        assertTrue(gs instanceof GString)

        def buffer = write(gs)
        def res = read(buffer)

        assertTrue(res instanceof GString)
        assertEquals "4 = 4", res
    }

    void testGStringField () {
        def a = 2, b = 2

        def obj = new WithGStringField()
        obj.f = "${a + b} = ${b * a}"

        def buffer = write(obj)
        def resObj = read(buffer)

        assertEquals "4 = 4", resObj.f
    }

    void testFoo() {
        def foo = new Foo()
        foo.name = "Gromit"
        foo.location = "Moon"

        def buffer = write(foo)
        def object = read(buffer)

        assert object != null
        assert object.metaClass != null , "Should have a metaclass!"
        assert object.name == "Gromit"
        assert object.location == "Moon"
        assert object.class.name == "groovy.Foo"
        def fooClass = this.class.classLoader.systemClassLoader.loadClass('groovy.Foo')
        assert foo.class == fooClass
        assert object.class == fooClass
    }
    
    def write(object) {
        def buffer = new ByteArrayOutputStream()
        def out = new ObjectOutputStream(buffer)
        out << object
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

class WithGStringField implements Serializable{
    private static final long serialVersionUID = 1L;

    GString f;
}
