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
        return AllTestSuite.suite("./src/test/groovy","Bug.groovy");
    }

// no tests inside (should we have an AbstractGroovyTestCase???)
//        suite.addTestSuite(groovy.bugs.TestSupport.class);
//        suite.addTestSuite(groovy.sql.TestHelper.class);
//        suite.addTestSuite(groovy.swing.Demo.class);

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//
//        suite.addTestSuite(cheese.Cheddar.class);
//        suite.addTestSuite(cheese.Provolone.class);
//        suite.addTestSuite(groovy.bugs.Cheese.class);
//        suite.addTestSuite(groovy.bugs.MyRange.class);
//        suite.addTestSuite(groovy.bugs.Scholastic.class);
//        suite.addTestSuite(groovy.bugs.SimpleModel.class);
//        suite.addTestSuite(groovy.DummyInterface.class);
//        suite.addTestSuite(groovy.DummyMethods.class);
//        suite.addTestSuite(groovy.gravy.Build.class);
//        suite.addTestSuite(groovy.j2ee.J2eeConsole.class);
//        suite.addTestSuite(groovy.lang.DerivedScript.class);
//        suite.addTestSuite(groovy.lang.DummyGString.class);
//        suite.addTestSuite(groovy.lang.MockWriter.class);
//        suite.addTestSuite(groovy.mock.example.CheeseSlicer.class);
//        suite.addTestSuite(groovy.mock.example.SandwichMaker.class);
//        suite.addTestSuite(groovy.model.MvcDemo.class);
//        suite.addTestSuite(groovy.OuterUser.class);
//        suite.addTestSuite(groovy.script.AtomTestScript.class);
//        suite.addTestSuite(groovy.script.Entry.class);
//        suite.addTestSuite(groovy.script.Feed.class);
//        suite.addTestSuite(groovy.script.PackageScript.class);
//        suite.addTestSuite(groovy.script.Person.class);
//        suite.addTestSuite(groovy.sql.Person.class);
//        suite.addTestSuite(groovy.swing.MyTableModel.class);
//        suite.addTestSuite(groovy.swing.SwingDemo.class);
//        suite.addTestSuite(groovy.swing.TableDemo.class);
//        suite.addTestSuite(groovy.swing.TableLayoutDemo.class);
//        suite.addTestSuite(groovy.txn.TransactionBean.class);
//        suite.addTestSuite(groovy.txn.TransactionBuilder.class);
//        suite.addTestSuite(groovy.util.Dummy.class);
//        suite.addTestSuite(groovy.util.DummyMBean.class);
//        suite.addTestSuite(groovy.util.SpoofTask.class);
//        suite.addTestSuite(groovy.util.SpoofTaskContainer.class);
//        suite.addTestSuite(groovy.xml.TestXmlSupport.class);

}
