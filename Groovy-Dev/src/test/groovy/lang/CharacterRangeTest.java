/**
 *
 */
package groovy.lang;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * Provides a few unit tests for {@link ObjectRange}s of {@link Character}s.  More tests are needed.
 *
 * @author Edwin Tellman
 */
public class CharacterRangeTest extends TestCase {
    /**
     * The range to test.
     */
    private ObjectRange range = null;

    /**
     * The first character in the range.
     */
    private final Character FROM = new Character('a');

    /**
     * The last character in the range.
     */
    private final Character TO = new Character('d');

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        range = new ObjectRange(FROM, TO);
    }

    /**
     * Tests iterating through the range.
     */
    public void testIterate() {
        Iterator iter = range.iterator();
        assertEquals(FROM, iter.next());
        for (char expected = (char) (FROM.charValue() + 1); expected <= TO.charValue(); expected++) {
            assertEquals(expected, ((Character)iter.next()).charValue());
        }
    }

    /**
     * Tests getting the 'from' value.
     */
    public void testGetFrom() {
        assertEquals("wrong 'from' value", FROM, range.getFrom());
    }

    /**
     * Tests getting the 'to' value.
     */
    public void testGetTo() {
        assertEquals("wrong 'to' value", TO, range.getTo());
    }

}
