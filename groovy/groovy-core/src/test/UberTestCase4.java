import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import groovy.util.AllTestSuite;

/**
 * Collecting all Groovy Unit Tests, written in Java.
 * @author Christian Stein
 * @author Dierk Koenig
 */
public class UberTestCase4 extends TestCase {

    public static Test suite() {
        return AllGroovyJavaTestsSuite.suite();
    }


}
