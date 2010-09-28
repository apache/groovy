package groovy.bugs

/**
 * NullObject has no asBoolean() method,
 * making null.asBoolean() return true instead of false.
 *
 * @author Guillaume Laforge
 */
class NullAsBooleanCoercionTest extends GroovyTestCase {

    void testNullBooleanCoercion() {

        assert !null
        assert !(null as boolean)
        assert !(null as Boolean)
        assert !(null.asBoolean())

    }
}
