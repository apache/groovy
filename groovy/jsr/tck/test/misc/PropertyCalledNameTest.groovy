
class PropertyCalledNameTest extends GroovyTestCase {

    @Property String name

    void setName(String name) {
        // JUnit doesn't like it if we overload the setName() so lets keep it working :)
        super.setName(name)
        this.name = name
    }

    void testProperty() {

        def foo = this.name
        assert foo == "testProperty"

        assert name == "testProperty"
        assert this.getName() == "testProperty"
        assert this.name == "testProperty"

        def bar = this
        assert bar.getName() == "testProperty"
        assert bar.name == "testProperty"

        name = "cheddar"

        assert this.getName() == "cheddar"
        assert this.name == "cheddar"

    }
}