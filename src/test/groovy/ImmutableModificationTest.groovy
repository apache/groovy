package groovy

/**
 * check that the new asImmutable() method works
 * as specified in GROOVY-623
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class ImmutableModificationTest extends GroovyTestCase {
    void testCollectionAsImmutable() {
        def challenger = ["Telson", "Sharna", "Darv", "Astra"]
        def hopefullyImmutable = challenger.asImmutable()
        try {
            challenger.add("Angel One")
            challenger << "Angel Two"

            // @todo fail("'challenger' is supposed to be an immutable collection.")

        } catch (UnsupportedOperationException e) {
            // success if this exception is thrown
            assert 4 == challenger.size()
        }
    }
}