import groovy.OuterUser

/**
 * Checks that it's possible to call inner classes constructor from groovy
 * @author Guillaume Laforge
 */
class CallInnerClassCtorTest extends GroovyTestCase {

    void testCallCtor() {
        user = new OuterUser()
        user.name = "Guillaume"
        user.age = 27

        assert user.name == "Guillaume"
        assert user.age == 27
    }

    void testCallInnerCtor() {
        address = new OuterUser.InnerAddress()
        address.city = "Meudon"
        address.zipcode = 92360

        assert address.city == "Meudon"
        assert address.zipcode == 92360
    }

}