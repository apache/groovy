package uk.org.xml.sax;

import java.io.Writer;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

public interface DocumentHandler extends org.xml.sax.DocumentHandler {
  Writer startDocument(final Writer writer) throws SAXException;
  Writer startElement(final String name, final AttributeList attributes, final Writer writer)
        throws SAXException;
}
