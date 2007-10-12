package groovy

import static java.lang.Boolean.FALSE as F
import static java.text.DateFormat.MEDIUM as M
import static java.text.DateFormat.MEDIUM
import static java.awt.Color.*
import static junit.framework.Assert.format
import static junit.framework.Assert.assertEquals
import static StaticImportTarget.x
import static java.lang.Math.*
import static java.util.Calendar.getInstance as now

class StaticImportTest extends GroovyTestCase {
    void testFieldWithAliasInExpression() {
        assert !F
    }

    void testMethodAndField() {
        assert cos(2 * PI) == 1.0
    }

    static myStaticMethod() {
        cos(2 * PI)
    }

    void testMethodAndFieldInStaticContext() {
        assert myStaticMethod() == 1.0
    }

    void testMethodAndFieldInClosure() {
        def closure = { cos(2 * PI) }
        assert closure() == 1.0
    }

    void testFieldAsObjectExpression() {
        assert PI.equals(Math.PI)
    }

    void testFieldAsArgumentList() {
        assert ("" + PI.toString()).contains('3.14')
    }

    void testFieldAliasing() {
        assert MEDIUM == M
    }

    void testMethodAliasing() {
        // GROOVY-1809 making this not possible on one line?
        def now = now().time
        assert now.class == Date
    }

    void testWildCardAliasing() {
        assert LIGHT_GRAY == java.awt.Color.LIGHT_GRAY
    }

    private format(a, b, c, ignored) { format(a, b, c) }

    void testMethodDefCanUseStaticallyImportedMethodWithSameNameButDiffArgs() {
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
