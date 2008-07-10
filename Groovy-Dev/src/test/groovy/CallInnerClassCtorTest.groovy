package groovy

/**
 * Checks that it's possible to call inner classes constructor from groovy
 * @author Guillaume Laforge
 */
class CallInnerClassCtorTest extends GroovyTestCase {

    void testCallCtor() {
        def user = new groovy.OuterUser()
        user.name = "Guillaume"
        user.age = 27

        assert user.name == "Guillaume"
        assert user.age == 27
    }

    void testCallInnerCtor() {
        def address = new groovy.OuterUser.InnerAddress()
        address.city = "Meudon"
        address.zipcode = 92360

        assert address.city == "Meudon"
        assert address.zipcode == 92360
    }

    void testCallInnerInnerCtor() {
        def address = new groovy.OuterUser.InnerAddress.Street()
        address.name = "rue de la paix"
        address.number = 17

        assert address.name == "rue de la paix"
        assert address.number == 17
    }

}