
class PropertyTest extends GroovyTestCase {


    // TODO should barf as TestCase has a private setter?
    // @Property String name = "abc"

    @Property String cheese = "edam"

    /** TODO can't do constructors :)
    PropertyTest() {
    }
    */

    void testProperty() {

        def foo = this.cheese
        assert foo == "edam"

        assert cheese == "edam"
        assert this.getCheese() == "edam"
        assert this.cheese == "edam"

        def bar = this
        assert bar.getCheese() == "edam"
        assert bar.cheese == "edam"

        cheese = "cheddar"

        assert this.getCheese() == "cheddar"
        assert this.cheese == "cheddar"

    }
}