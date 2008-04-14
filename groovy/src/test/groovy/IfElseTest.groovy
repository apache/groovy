package groovy

class IfElseTest extends GroovyTestCase {

    void testIf_NoElse() {

        def x = false

        if ( true ) {
            x = true
        }

        assert x == true
    }

    void testIf_WithElse_MatchIf() {

        def x = false
        def y = false

        if ( true ) {
            x = true
        } else {
            y = true
        }

        assert x == true
        assert y == false

    }

    void testIf_WithElse_MatchElse() {

        def x = false
        def y = false

        if ( false ) {
            x = true
        } else {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_MatchIf() {

        def x = false
        def y = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        }

        assert x == true
        assert y == false
    }

    void testIf_WithElseIf_MatchElseIf() {

        def x = false
        def y = false

        if ( false ) {
            x = true
        } else if ( true ) {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_WithElse_MatchIf() {

        def x = false
        def y = false
        def z = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assert x == true
        assert y == false
        assertEquals( false, z )
    }

    void testIf_WithElseIf_WithElse_MatchElseIf() {

        def x = false
        def y = false
        def z = false

        if ( false ) {
            x = true
        } else if ( true ) {
            y = true
        } else {
            z = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
        assertEquals( false, z )
    }

    void testIf_WithElseIf_WithElse_MatchElse() {

        def x = false
        def y = false
        def z = false

        if ( false ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assertEquals( false, x )
        assert y == false
        assertEquals( true, z )
    }
}
