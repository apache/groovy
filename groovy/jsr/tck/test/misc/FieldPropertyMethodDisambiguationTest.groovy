class FieldPropertyMethodDisambiguationTest extends GroovyTestCase {
    String bar = "field"

    String getBar() {
        return "propertyMethod"
    }

    String bar() {
        return "method"
    }


    String bar(param) {
        return "method with param: " + param
    }


    void testCase() {
        def answer = bar()
        assert answer == "method"
        assert this.bar() == "method"

        assert bar(1) == "method with param: 1"
        assert this.bar(1) == "method with param: 1"


        assert getBar() == "propertyMethod"
        assert this.getBar() == "propertyMethod"

        // TODO should these 2 expressions call the getter or return the field?
        assert bar == "field"
        assert this.bar == "field"


        def value = this.@bar
        assert value == "field"

        assert this.@bar == "field"

        def tmp = this
        assert tmp.@bar == "field"

        tmp.@bar = "whatnot"

        assert bar == "whatnot"
        assert this.@bar == "whatnot"
    }
}