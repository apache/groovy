package groovy.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * Support class for creating XML Factories
 */
public class FactorySupport {
    public static Object createFactory(PrivilegedExceptionAction action) throws ParserConfigurationException {
        Object factory;
        try {
            factory = AccessController.doPrivileged(action);
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof ParserConfigurationException) {
                throw(ParserConfigurationException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return factory;
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        return (DocumentBuilderFactory) createFactory(new PrivilegedExceptionAction() {
            public Object run() throws ParserConfigurationException {
                return DocumentBuilderFactory.newInstance();
            }
        });
    }

    public static SAXParserFactory createSaxParserFactory() throws ParserConfigurationException {
        return (SAXParserFactory) createFactory(new PrivilegedExceptionAction() {
                public Object run() throws ParserConfigurationException {
                    return SAXParserFactory.newInstance();
                }
            });
    }
}
