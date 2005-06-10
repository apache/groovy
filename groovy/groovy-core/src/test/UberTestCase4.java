import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Christian Stein
 */
public class UberTestCase4 extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Template and servlet test suite");
        
        /*
         * Groovy text package tests.
         */
        suite.addTestSuite(groovy.text.TemplateTest.class);
        
        /*
         * Groovlet and simple template servlet tests.
         */
        suite.addTestSuite(groovy.servlet.GroovyServletTest.class);
        
        return suite;
    }

}
