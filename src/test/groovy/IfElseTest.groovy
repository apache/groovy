class IfElseTest extends GroovyTestCase {

    void testIf_NoElse() {

        x = false

        if ( true ) {
            x = true
        }

        assertEquals( true, x )
    }

    void testIf_WithElse_MatchIf() {

        x = false
        y = false

        if ( true ) {
            x = true
        } else {
            y = true
        }

        assertEquals( true, x )
        assertEquals( false, y )

    }

    void testIf_WithElse_MatchElse() {

        x = false
        y = false

        if ( false ) {
            x = true
        } else {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_MatchIf() {

        x = false
        y = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        }

        assertEquals( true, x )
        assertEquals( false, y )
    }

    void testIf_WithElseIf_MatchElseIf() {

        x = false
        y = false

        if ( false ) {
            x = true
        } else if ( true ) {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_WithElse_MatchIf() {

        x = false
        y = false
        z = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assertEquals( true, x )
        assertEquals( false, y )
        assertEquals( false, z )
    }

    void testIf_WithElseIf_WithElse_MatchElseIf() {

        x = false
        y = false
        z = false

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

        x = false
        y = false
        z = false

        if ( false ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assertEquals( false, x )
        assertEquals( false, y )
        assertEquals( true, z )
    }
}
