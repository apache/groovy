class AnnotationTest extends GroovyTestCase {
    @Property String foo
    @Property protected def bar
    protected @Property def mooky
    
    @Property
    String wibble = "wobble"

    @Property String gouda, edam, wensleydale, gorgonzola, parmesan,
    mozarella, 
    cheddar

    def bazouki = "off"

    public void testValues() {
        assert bazouki == "off"

        assert wibble == "wobble"
        assert getWibble() == "wobble"
    }
}

