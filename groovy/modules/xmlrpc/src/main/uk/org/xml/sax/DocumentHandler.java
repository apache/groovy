package uk.org.xml.sax;

import java.io.Writer;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

public interface DocumentHandler extends org.xml.sax.DocumentHandler {
  Writer startDocument(final Writer writer) throws SAXException;
  Writer startElement(final String name, final AttributeList attributes, final Writer writer)
        throws SAXException;
}
