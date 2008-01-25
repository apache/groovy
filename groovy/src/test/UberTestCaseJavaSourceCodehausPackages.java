/**
 * The tests collected here all take a 'significant' length of time to execute,
 * i.e.&nsbp;greater than 2 seconds elapsed on my machine.
 *
 * to prevent a JVM startup-shutdown time per test, it should be more efficient to
 * collect the tests together into a suite.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import junit.framework.Test;
import junit.framework.TestCase;

public class UberTestCaseJavaSourceCodehausPackages extends TestCase {
    public static Test suite() {
        return JavaSourceCodehausPackagesSuite.suite();
    }

}
