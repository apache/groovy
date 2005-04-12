class IfElseCompactTest extends GroovyTestCase {

    void testIf_NoElse() {

        x = false

        if ( true ) {x = true}

        assert x == true
    }

    void testIf_WithElse_MatchIf() {

        x = false
        y = false

        if ( true ) {x = true} else {y = true}

        assert x == true
        assert y == false

    }
}
