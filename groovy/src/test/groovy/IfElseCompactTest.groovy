package groovy

class IfElseCompactTest extends GroovyTestCase {

    void testIf_NoElse() {

        def x = false

        if ( true ) {x = true}

        assert x == true
    }

    void testIf_WithElse_MatchIf() {

        def x = false
        def y = false

        if ( true ) {x = true} else {y = true}

        assert x == true
        assert y == false

    }
}
