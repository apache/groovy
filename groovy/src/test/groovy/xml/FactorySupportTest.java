package groovy.xml;

import junit.framework.TestCase;

import javax.xml.parsers.ParserConfigurationException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class FactorySupportTest extends TestCase {
    private static final PrivilegedActionException PRIVILEGED_ACTION_EXCEPTION = new PrivilegedActionException(new IllegalStateException());
    private static final ParserConfigurationException PARSER_CONFIGURATION_EXCEPTION = new ParserConfigurationException();

    public void testCreatesFactories() throws Exception {
        assertNotNull(FactorySupport.createDocumentBuilderFactory());
        assertNotNull(FactorySupport.createSaxParserFactory());
    }

    public void testParserConfigurationExceptionNotWrapped() throws ParserConfigurationException {
        try {
            FactorySupport.createFactory(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    throw PARSER_CONFIGURATION_EXCEPTION;
                }
            });
            fail("Exception was not caught");
        } catch (Throwable t) {
            assertSame(PARSER_CONFIGURATION_EXCEPTION, t);
        }
    }

    public void testOtherExceptionsWrappedAsUnchecked() throws ParserConfigurationException {
        try {
            FactorySupport.createFactory(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    throw PRIVILEGED_ACTION_EXCEPTION;
                }
            });
            fail("Exception was not caught");
        } catch (RuntimeException re) {
            assertSame(PRIVILEGED_ACTION_EXCEPTION, re.getCause());
        } catch (Throwable t) {
            fail("Exception was not wrapped as runtime");
        }
    }
}