package groovy

class CompareEqualsTest extends GroovyTestCase {
    void testEqualsOperatorIsMultimethodAware() {
        assert new Xyz() == new Xyz()

        assertEquals new Xyz(), new Xyz()

        shouldFail {
          assert new Xyz() == 239
        }
        assert new Xyz () == "GROOVY.XYZ"
    }
}

class Xyz {
    boolean equals(Xyz other) {
        println "${other.class} TRUE"
        true
    }

    boolean equals(Object other) {
        println "${other.class} FALSE"
        null
    }

    boolean equals(String str) {
        str.equalsIgnoreCase this.class.getName()
    }
}
