package groovy.xml.dom;

import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.StringWriter;

public class DOMUtil {
    public static String serialize(Element element) {
        StringWriter sw = new StringWriter();
        serialize(element, new StreamResult(sw));
        return sw.toString();
    }

    public static void serialize(Element element, OutputStream os) {
        serialize(element, new StreamResult(os));
    }

    private static void serialize(Element element, StreamResult outputTarget) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StringWriter sw = new StringWriter();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(element), outputTarget);
        }
        catch (TransformerException e) {
            // ignore
        }
    }
}
