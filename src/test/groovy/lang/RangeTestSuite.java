/**
 *
 */
package groovy.lang;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Calls all the range-related tests.
 *
 * @author Edwin Tellman
 */
public final class RangeTestSuite extends TestSuite {

    /**
     * Creates a new {@link RangeTestSuite}
     */
    public RangeTestSuite() {
        addTestSuite(IntRangeTest.class);
        addTestSuite(ShortRangeTest.class);
        addTestSuite(IntegerRangeTest.class);
        addTestSuite(LongRangeTest.class);
        addTestSuite(FloatRangeTest.class);
        addTestSuite(BigDecimalRangeTest.class);
        addTestSuite(CharacterRangeTest.class);
        addTestSuite(RangeTest.class);
    }

    /**
     * Runs the tests in the {@link TestRunner}.
     *
     * @param argv not used
     */
    public static void main(String[] argv) {
        junit.textui.TestRunner.run(new RangeTestSuite());
    }
}
