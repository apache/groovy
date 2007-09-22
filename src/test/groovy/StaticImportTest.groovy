package groovy

import static Boolean.FALSE as F
import static java.text.DateFormat.MEDIUM as M
import static java.text.DateFormat.MEDIUM
import static java.awt.Color.*
import static junit.framework.Assert.format
import static junit.framework.Assert.assertEquals
import static StaticImportTarget.x
// TODO: make below work if we leave off java.lang.
import static java.lang.Math.*

class StaticImportTest extends GroovyTestCase {
    void testNormalUsage() {
        assert !F
    }

    void testMath() {
        assert cos(2 * PI) == 1.0
        def closure = { cos(2 * PI) }
        assert closure() == 1.0
    }

    void testAliasing() {
        assert MEDIUM == M
    }

    void testWildCarding() {
        assert LIGHT_GRAY == java.awt.Color.LIGHT_GRAY
    }

    private format(a, b, c, ignored) { format(a, b, c) }

    void testMethodSelection() {
        assert format("different", "abc", "aBc", 3) == 'different expected:<abc> but was:<aBc>'
    }

    void testAssertEqualsFromJUnit() {
        double[] values = [3.9999, 4.0001, 0.00021, 0.00019]
        assertEquals(values[0], values[1], values[2])
        shouldFail(junit.framework.AssertionFailedError) {
            assertEquals(values[0], values[1], values[3])
        }
    }

    void testStaticImportFromGroovy() {
        def nonstaticval = new StaticImportTarget().y("he", 3)
        def staticval = x("he", 3)
        assert nonstaticval == staticval
    }
}
