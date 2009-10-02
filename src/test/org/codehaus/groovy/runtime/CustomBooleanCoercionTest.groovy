package org.codehaus.groovy.runtime

/**
 * Test the customization of the Groovy truth, aka. boolean coercion.
 * It is possible to customize how instances of a type are coerced into boolean
 * by implementing a method with the following signature:
 * <code>boolean asBoolean()</code>
 * <p>
 * The test also checks it's possible to override the behaviour of pre-existing boolean coercion.
 *
 * @author Guillaume Laforge
 */
class CustomBooleanCoercionTest extends GroovyTestCase {

    void testPredefinedGroovyTruth() {
        // null is false
        def nullVar = null
        assert !nullVar

        // non-null object is true
        def obj = new Object()
        assert obj

        // empty strings are false, and >1 length strings are true
        assert !""
        assert "  \t \n "
        assert "Groovy Truth"

        // characters are true unless if it's the zeroth one
        char c0 = '\0'
        char c1 = 'a'
        assert !c0
        assert c1

        // empty lists are true, and list with 1+ elements are true
        assert ![]
        assert [1, 2, 3]
        assert 1..10

        // empty maps are false, and non-empty maps are true
        assert ![:]
        assert [firstname: 'Guillaume', lastname: 'Laforge']

        // number equal to 0 is false, and true otherwise
        assert !0
        assert 1
        assert !0.0
        assert 1.1

        // an iterator is true if there are other elements to iterate over, false otherwise
        assert ![].iterator()
        assert [1, 2, 3].iterator()

        assert Boolean.TRUE
        assert !Boolean.FALSE

        // empty arrays are false, non-empty are true
        assert !([] as Object[])
        assert [1, 2, 3] as int[]
    }

    /**
     * A Predicate instance should coerce to the same boolean as the one in the value property of the instance
     */
    void testCustomAsBooleanMethod() {
        assert new Predicate(value: true)
        assert !new Predicate(value: false)
    }

    void testOverrideAsBooleanMethodWithACategory() {
        use (BoolCategory) {
            assert !new Predicate(value: true)
            assert new Predicate(value: false)
        }
    }

    void testOverrideAsBooleanMethodWithEMC() {
        try {
            Predicate.metaClass.asBoolean = { -> true }
            assert new Predicate(value: true)
            assert new Predicate(value: false)
        } finally {
            Predicate.metaClass = null
        }
    }

    void testOverideStringAsBooleanThroughEMC() {
        try {
            String.metaClass.asBoolean = { -> true }

            assert ""
            assert " \t \n "
            assert "ok"
            assert "true"
            assert "false"
            assert "Groovy rocks!"
        } finally {
            String.metaClass = null
        }
    }
}

/** A Predicate classe coercible to a boolea expression */
class Predicate {
    boolean value
    boolean asBoolean() { value }
}

/** A Boolean Category which coerces the boolean value to its opposite */
class BoolCategory {
    static boolean asBoolean(Predicate self) {
        !self.value
    }
}