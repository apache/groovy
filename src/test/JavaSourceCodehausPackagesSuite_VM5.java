import org.codehaus.groovy.ant.vm5.GroovycTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JavaSourceCodehausPackagesSuite_VM5 {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(GroovycTest.class);
        return suite;
    }
}
