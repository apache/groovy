package uk.org.xml.sax;

public interface Parser extends org.xml.sax.Parser {
  void setDocumentHandler(DocumentHandler handler);
}