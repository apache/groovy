/**
 * Collects all Bug-related tests.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
import junit.framework.*;
import groovy.util.AllTestSuite;

public class UberTestCase2 extends TestCase {
    public static Test suite() {
        return AllTestSuite.suite("./src/test/groovy","**/*Bug.groovy");
    }

// no tests inside (should we have an AbstractGroovyTestCase???)
//        groovy.bugs.TestSupport.class
//        groovy.sql.TestHelper.class
//        groovy.swing.Demo.class

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//
//        cheese.Cheddar.class
//        cheese.Provolone.class
//        groovy.bugs.Cheese.class
//        groovy.bugs.MyRange.class
//        groovy.bugs.Scholastic.class
//        groovy.bugs.SimpleModel.class
//        groovy.DummyInterface.class
//        groovy.DummyMethods.class
//        groovy.gravy.Build.class
//        groovy.j2ee.J2eeConsole.class
//        groovy.lang.DerivedScript.class
//        groovy.lang.DummyGString.class
//        groovy.lang.MockWriter.class
//        groovy.mock.example.CheeseSlicer.class
//        groovy.mock.example.SandwichMaker.class
//        groovy.OuterUser.class
//        groovy.script.AtomTestScript.class
//        groovy.script.Entry.class
//        groovy.script.Feed.class
//        groovy.script.PackageScript.class
//        groovy.script.Person.class
//        groovy.sql.Person.class
//        groovy.txn.TransactionBean.class
//        groovy.txn.TransactionBuilder.class
//        groovy.util.Dummy.class
//        groovy.util.DummyMBean.class
//        groovy.util.SpoofTask.class
//        groovy.util.SpoofTaskContainer.class
//        groovy.xml.TestXmlSupport.class);

}
