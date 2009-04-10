package groovy.bugs

class Groovy3462Bug extends GroovyTestCase {

    void testClosureWithParameterHavingDefaultExpression() {
        assertScript """
            month = { String date = new Date().format("yyyyMM") ->
                date
            }

            def obj = month("200101")
            assert month("200101") == "200101"

            String expectedDate = new Date().format("yyyyMM")
            assert month() == expectedDate
        """
    }
}
