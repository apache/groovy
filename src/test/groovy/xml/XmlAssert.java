package groovy.xml;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;

/**
 * Helper class for checking XML tests.
 */
public class XmlAssert {
    public static void assertXmlEquals(String expected, String actual) throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue(diff.toString(), diff.similar());
    }
}
