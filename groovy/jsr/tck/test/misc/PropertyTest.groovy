
class PropertyTest extends GroovyTestCase {


    // TODO should barf as TestCase has a private setter?
    // @Property String name = "abc"

    @Property String cheese = "edam"

    /** TODO can't do constructors :)
    PropertyTest() {
    }
    */

    void testProperty() {

        assert cheese == "edam"
        assert this.getCheese() == "edam"
        //assert this.cheese == "edam"

        cheese = "cheddar"

        assert this.getCheese() == "cheddar"
        //assert this.cheese == "cheddar"

    }
}