/**
 * to prevent a JVM startup-shutdown time per test, it should be more efficient to
 * collect the tests together into a suite.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
import junit.framework.*;
public class UberTestCase2 extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(groovy.bugs.ArrayMethodCallBug.class);
        suite.addTestSuite(groovy.bugs.ClassGeneratorFixesTest.class);
        suite.addTestSuite(groovy.bugs.ClassInScriptBug.class);
        suite.addTestSuite(groovy.bugs.ClosuresInScriptBug.class);
        suite.addTestSuite(groovy.bugs.ClosureWithStaticVariablesBug.class);
        suite.addTestSuite(groovy.bugs.ConstructorParameterBug.class);
        suite.addTestSuite(groovy.bugs.Groovy278_Bug.class);
        suite.addTestSuite(groovy.bugs.Groovy303_Bug.class);
        suite.addTestSuite(groovy.bugs.Groovy308_Bug.class);
        suite.addTestSuite(groovy.bugs.Groovy666_Bug.class);
        suite.addTestSuite(groovy.bugs.IanMaceysBug.class);
        suite.addTestSuite(groovy.bugs.InterfaceImplBug.class);
        suite.addTestSuite(groovy.bugs.MarkupInScriptBug.class);
        suite.addTestSuite(groovy.bugs.PrimitivePropertyBug.class);
        suite.addTestSuite(groovy.bugs.ScriptBug.class);
        suite.addTestSuite(groovy.bugs.SeansBug.class);
        suite.addTestSuite(groovy.bugs.StaticMethodCallBug.class);
        suite.addTestSuite(groovy.bugs.SubscriptOnPrimitiveTypeArrayBug.class);
        suite.addTestSuite(groovy.bugs.SubscriptOnStringArrayBug.class);
        suite.addTestSuite(groovy.lang.GroovyShellTest.class);
        suite.addTestSuite(groovy.lang.GStringTest.class);
        suite.addTestSuite(groovy.lang.IntRangeTest.class);
        suite.addTestSuite(groovy.lang.MetaClassTest.class);
        suite.addTestSuite(groovy.lang.RangeTest.class);
        suite.addTestSuite(groovy.lang.ScriptIntegerDivideTest.class);
        suite.addTestSuite(groovy.lang.ScriptPrintTest.class);
        suite.addTestSuite(groovy.lang.ScriptTest.class);
        suite.addTestSuite(groovy.lang.SequenceTest.class);
        suite.addTestSuite(groovy.lang.TupleTest.class);
        suite.addTestSuite(groovy.mock.example.SandwichMakerTest.class);
        suite.addTestSuite(groovy.mock.MockTest.class);
        suite.addTestSuite(groovy.model.TableModelTest.class);
//todo - error in some test environments        suite.addTestSuite(groovy.security.RunAllGroovyScriptsSuite.class);
//todo - error in some test environments        suite.addTestSuite(groovy.security.RunOneGroovyScript.class);
//todo - error in some test environments        suite.addTestSuite(groovy.security.SecurityTest.class);
//todo - error in some test environments        suite.addTestSuite(groovy.security.SecurityTestSupport.class);
//todo - error in some test environments        suite.addTestSuite(groovy.security.SignedJarTest.class);
        suite.addTestSuite(groovy.sql.PersonTest.class);
        suite.addTestSuite(groovy.sql.SqlCompleteTest.class);
        suite.addTestSuite(groovy.sql.SqlCompleteWithoutDataSourceTest.class);
        suite.addTestSuite(groovy.sql.SqlTest.class);
        suite.addTestSuite(groovy.sql.SqlWithBuilderTest.class);
        suite.addTestSuite(groovy.sql.SqlWithTypedResultsTest.class);
        suite.addTestSuite(groovy.text.TemplateTest.class);
        suite.addTestSuite(groovy.tree.NodePrinterTest.class);
        suite.addTestSuite(groovy.txn.TransactionTest.class);
        suite.addTestSuite(groovy.util.EmptyScriptTest.class);
        suite.addTestSuite(groovy.util.MBeanTest.class);
        suite.addTestSuite(groovy.util.NodeTest.class);
        suite.addTestSuite(groovy.util.XmlParserTest.class);
        suite.addTestSuite(groovy.xml.dom.DOMTest.class);
        suite.addTestSuite(groovy.xml.DOMTest.class);
        suite.addTestSuite(groovy.xml.MarkupTest.class);
        suite.addTestSuite(groovy.xml.NamespaceDOMTest.class);
        suite.addTestSuite(groovy.xml.SAXTest.class);
        suite.addTestSuite(groovy.xml.SmallNamespaceDOMTest.class);
        suite.addTestSuite(groovy.xml.VerboseDOMTest.class);
        suite.addTestSuite(groovy.xml.XmlTest.class);
        return suite;
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
