package org.codehaus.groovy.sandbox.util;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.codehaus.groovy.sandbox.markup.Buildable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class XmlSlurper extends DefaultHandler {
  private final XMLReader reader;
	private List result = null;
	private List body = null;
	private final StringBuffer charBuffer = new StringBuffer();

    public XmlSlurper() throws ParserConfigurationException, SAXException {
        this(false, true);
    }

    public XmlSlurper(final boolean validating, final boolean namespaceAware) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = null;
        
	    	try {
				factory = (SAXParserFactory) AccessController.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws ParserConfigurationException {
						return SAXParserFactory.newInstance();
					}
				});
	    	} catch (final PrivilegedActionException pae) {
	    	final Exception e = pae.getException();
	    		
	    		if (e instanceof ParserConfigurationException) {
	    			throw (ParserConfigurationException) e;
	    		} else {
	    			throw new RuntimeException(e);
	    		}
	    	}
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);

        final SAXParser parser = factory.newSAXParser();
        this.reader = parser.getXMLReader();
    }

    public XmlSlurper(final XMLReader reader) {
        this.reader = reader;
    }

    public XmlSlurper(final SAXParser parser) throws SAXException {
        this(parser.getXMLReader());
    }

    /**
     * Parse the content of the specified input source into a List
     */
    public XmlList parse(final InputSource input) throws IOException, SAXException {
    		this.reader.setContentHandler(this);
    		this.reader.parse(input);
        
        return (XmlList)this.result.get(0);
    }
    
    /**
     * Parses the content of the given file as XML turning it into a List
     */
    public XmlList parse(final File file) throws IOException, SAXException {
    final InputSource input = new InputSource(new FileInputStream(file));
    
        input.setSystemId("file://" + file.getAbsolutePath());
        
        return parse(input);

    }

    /**
     * Parse the content of the specified input stream into a List.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public XmlList parse(final InputStream input) throws IOException, SAXException {
        return parse(new InputSource(input));
    }

    /**
     * Parse the content of the specified reader into a List.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public XmlList parse(final Reader in) throws IOException, SAXException {
        return parse(new InputSource(in));
    }

    /**
     * Parse the content of the specified URI into a List
     */
    public XmlList parse(final String uri) throws IOException, SAXException {
        return parse(new InputSource(uri));
    }

    /**
     * A helper method to parse the given text as XML
     * 
     * @param text
     * @return
     */
    public XmlList parseText(final String text) throws IOException, SAXException {
        return parse(new StringReader(text));
    }
    

    // ContentHandler interface
    //-------------------------------------------------------------------------                    
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		this.result = null;
		this.body = new LinkedList();
		this.charBuffer.setLength(0);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
		addNonWhitespaceCdata();
		
		final Map attributes = new HashMap();
		
		for (int i = atts.getLength() - 1; i != -1; i--) {
			if (atts.getURI(i).length() == 0) {
				attributes.put(atts.getQName(i), atts.getValue(i));
			} else {
				//
				// Note this is strictly incorrect the name is really localname + URI
				// We need to figure out what to do with paramenters in namespaces
				//
				attributes.put(atts.getLocalName(i), atts.getValue(i));
			}
			
		}
		
		final List newBody = new LinkedList();

		newBody.add(attributes);
		
		newBody.add(this.body);

		this.body = newBody;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		this.charBuffer.append(ch, start, length);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
		addNonWhitespaceCdata();
		
		final List children = this.body;
		
		final Map attributes = (Map)this.body.remove(0);
		
		this.body = (List)this.body.remove(0);
		
		final XmlList xmlList;
		if (namespaceURI.length() == 0){
			xmlList = new XmlList(qName, attributes, children, namespaceURI);
		} else {
			xmlList = new XmlList(localName, attributes, children, namespaceURI);
		}
		this.body.add(xmlList);

		//update all chilidren's parent node.
		for(final Iterator it=children.iterator();it.hasNext();) {
	  final Object child = it.next();
    
			if(child instanceof XmlList){
				((XmlList)child).setElementsParent (xmlList);
			}
    }
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		this.result = this.body;
		this.body = null;
	}

    // Implementation methods
    //-------------------------------------------------------------------------           

	/**
	 * 
	 */
	private void addNonWhitespaceCdata() {
		if (this.charBuffer.length() != 0) {
			//
			// This element is preceeded by CDATA if it's not whitespace add it to the body
			// Note that, according to the XML spec, we should preserve the CDATA if it's all whitespace
			// but for the sort of work I'm doing ignoring the whitespace is preferable
			//
			final String cdata = this.charBuffer.toString();
			
			this.charBuffer.setLength(0);
			if (cdata.trim().length() != 0) {
				this.body.add(cdata);
			}
		}		
	}
}

class XmlList extends GroovyObjectSupport implements Writable, Buildable {
	final String name;
	final Map attributes;
	final Object[] children;
	final String namespaceURI;
	XmlList parent = null;
	
    public XmlList(final String name, final Map attributes, final List body, final String namespaceURI) {
        super();
        
        this.name = name;
        this.attributes = attributes;
        this.children = body.toArray();
        this.namespaceURI = namespaceURI;
    }
    
    public void setElementsParent(final XmlList parent){
        this.parent = parent;
    }

    public Object getProperty(final String elementName) {
	    	if (elementName.startsWith("@")) {
	    		return this.attributes.get(elementName.substring(1));
	    	} else {
	    	final int indexOfFirst = getNextXmlElement(elementName, -1);
	    	
	    		if (indexOfFirst == -1) { // no elements match the element name
    				return new ElementCollection() {
        				protected ElementCollection getResult(final String property) {
        					return this;
        				}

	    	    	    		/**
	    	    	    		 * 
	    	    	    		 * Used by the Invoker when it wants to iterate over this object
	    	    	    		 * 
	    	    	    		 * @return
	    	    	    		 */
	    	    	    		public ElementIterator iterator() {
	    	    	    			return new ElementIterator(new XmlList[]{XmlList.this}, new int[]{-1}) {
	    	    	    				{
	    	    	    					findNextChild();		// set up the element indexes
	    	    	    				}
	    	    	    				
	    	        				protected void findNextChild() {
	    	        					this.nextParentElements[0] = -1;
	    	        				}
	    	    	    			};
	    	    	    		}
    				};
	    		}
	    		
    			if (getNextXmlElement(elementName, indexOfFirst) == -1) {	// one element matches the element name
    				return this.children[indexOfFirst];
    			} else {		// > 1 element matches the element name
	    	    		return new ElementCollection() {
	        				protected ElementCollection getResult(final String property) {
	        					return new ComplexElementCollection(new XmlList[]{XmlList.this},
	    							     						new int[] {indexOfFirst},
	    														new String[] {elementName},
	    														property);
	        				}
	
	    	    	    		/**
	    	    	    		 * 
	    	    	    		 * Used by the Invoker when it wants to iterate over this object
	    	    	    		 * 
	    	    	    		 * @return
	    	    	    		 */
	    	    	    		public ElementIterator iterator() {
	    	    	    			return new ElementIterator(new XmlList[]{XmlList.this}, new int[]{indexOfFirst}) {
	    	        				protected void findNextChild() {
	    	        					this.nextParentElements[0] = XmlList.this.getNextXmlElement(elementName, this.nextParentElements[0]);
	    	        				}
	    	    	    			};
	    	    	    		}
	    	    	    };
    			}
	    	}
    }
    
    public Object getAt(final int index) {
    		if (index == 0) {
    			return this;
    		} else {
    			throw new ArrayIndexOutOfBoundsException(index);
    		}
    	}
    
    public int size() {
    		return 1;
    }

    public Object invokeMethod(final String name, final Object args) {
		if ("attributes".equals(name)) {
			return this.attributes;
		} else if ("name".equals(name)) {
			return this.name;
		} else if ("children".equals(name)) {
			return this.children;
		} else if ("parent".equals(name)) {
			return this.parent;
		} else if ("contents".equals(name)) {
			return new Buildable() {
				public void build(GroovyObject builder) {
					buildChildren(builder);
				}
			};
		} else if ("text".equals(name)) {
			return text();
		} else if ("getAt".equals(name) && ((Object[])args)[0] instanceof String) {
			return getProperty((String)((Object[])args)[0]);
		} else if ("depthFirst".equals(name)) {
			//
			// TODO: replace this with an iterator
			//
			
			return new GroovyObjectSupport() {
				public Object invokeMethod(final String name, final Object args) {
					if ("getAt".equals(name) && ((Object[])args)[0] instanceof String) {
						return getProperty((String)((Object[])args)[0]);
					} else {
						return XmlList.this.invokeMethod(name, args);
					}
				}
				
				public Object getProperty(final String property) {
					if (property.startsWith("@")) {
						return XmlList.this.getProperty(property);
					} else {
					final List result = new LinkedList();

						depthFirstGetProperty(property, XmlList.this.children, result);
						
						return result;
					}
				}
				
				private void depthFirstGetProperty(final String property, final Object[] contents, final List result) {
			    		for (int i = 0; i != contents.length; i++) {
			    		final Object item = contents[i];
			    		
			    			if (item instanceof XmlList) {
			    				if (((XmlList)item).name.equals(property)) {
			    					result.add(item);
			    				}
			    				
			    				depthFirstGetProperty(property, ((XmlList)item).children, result);
			    			}
					}
				}
			};
    		} else {
    			return getMetaClass().invokeMethod(this, name, args);
    		}
    }
    
	/* (non-Javadoc)
	 * @see groovy.lang.Writable#writeTo(java.io.Writer)
	 */
	public Writer writeTo(Writer out) throws IOException {

		for (int i = 0; i != this.children.length; i++) {
		final Object child = this.children[i];
		
			if (child instanceof String) {
				out.write((String)child);
			} else {
				((XmlList)child).writeTo(out);
			}
		}
		
		return out;
	}
    
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.sandbox.markup.Buildable#build(groovy.lang.GroovyObject)
	 */
	public void build(final GroovyObject builder) {
		// TODO handle Namespaces
	final Closure rest = new Closure(null) {
		public Object doCall(final Object o) {
			buildChildren(builder);
			
			return null;
		}
	};

		builder.invokeMethod(this.name, new Object[]{this.attributes, rest});
		
	}
	
	public String toString() {
		return text();
	}
	
	private String text() {
	final StringBuffer buff = new StringBuffer();

		for (int i = 0; i != this.children.length; i++) {
		final Object child = this.children[i];
		
			if (child instanceof String) {
				buff.append(child);
			} else {
				buff.append(((XmlList)child).text());
			}
		}	
	
		return buff.toString();
	}
	
	private void buildChildren(final GroovyObject builder) {
		for (int i = 0; i != this.children.length; i++) {
			if (this.children[i] instanceof Buildable) {
				((Buildable)this.children[i]).build(builder);
			} else {
				builder.getProperty("mkp");
				builder.invokeMethod("yield", new Object[]{this.children[i]});
			}
		}
	}

    	protected int getNextXmlElement(final String name, final int lastFound) {
    		for (int i = lastFound + 1; i < this.children.length; i++) {
	    	final Object item = this.children[i];
	    		
	    		if (item instanceof XmlList && ((XmlList)item).name.equals(name)) {
	    			return i;
	    		}
	    	}
    		
    		return -1;
    	}
}

abstract class ElementIterator implements Iterator {
	protected final XmlList[] parents;
	protected final int[] nextParentElements;
	
	protected ElementIterator(final XmlList[] parents, int[] nextParentElements) {
		this.parents = new XmlList[parents.length];
		System.arraycopy(parents, 0, this.parents, 0, parents.length);
		
		this.nextParentElements = new int[nextParentElements.length];
		System.arraycopy(nextParentElements, 0, this.nextParentElements, 0, nextParentElements.length);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return this.nextParentElements[0] != -1;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
	final Object result = this.parents[0].children[this.nextParentElements[0]];
			
		findNextChild();
	
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	protected abstract void findNextChild();
}

abstract class ElementCollection extends GroovyObjectSupport {
	private int count = -1;
	
	public abstract ElementIterator iterator();
	
	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
	 */
	public Object getProperty(final String property) {
	final ElementCollection result = getResult(property);
	final Iterator iterator = result.iterator();

		if (iterator.hasNext()) {				
			//
			// See if there's only one available
			//
			final Object first = iterator.next();
			
			if (!iterator.hasNext()) {
				return first;
			}
		}
		
		return result;
	}
	
	protected abstract ElementCollection getResult(String property);
    
    public synchronized Object getAt(int index) {
	    	if (index >= 0) {
		final Iterator iter = iterator();
		
			while (iter.hasNext()) {
				if (index-- == 0) {
					return iter.next();
				} else {
					iter.next();
				}
			}
	    	}
	    	
	    	throw new ArrayIndexOutOfBoundsException(index);
    }
    
	public synchronized int size() {
		if (this.count == -1) {
		final Iterator iter = iterator();
		
			this.count = 0;
			
			while (iter.hasNext()) {
				this.count++;
				iter.next();
			}
		}
		return this.count;
	}
}

class ComplexElementCollection extends ElementCollection {
	private final XmlList[] parents;
	private final int[] nextParentElements;
	private final String[] parentElementNames;
	
	public ComplexElementCollection(final XmlList[] parents,
              				  	  final int[] nextParentElements,
								  final String[] parentElementNames,
								  final String childElementName)
	{
		this.parents = new XmlList[parents.length + 1];
		this.parents[0] = (XmlList)parents[0].children[nextParentElements[0]];
		System.arraycopy(parents, 0, this.parents, 1, parents.length);
		
		this.nextParentElements = new int[nextParentElements.length + 1];
		this.nextParentElements[0] = -1;	
		System.arraycopy(nextParentElements, 0, this.nextParentElements, 1, nextParentElements.length);
		
		this.parentElementNames = new String[parentElementNames.length + 1];
		this.parentElementNames[0] = childElementName;
		System.arraycopy(parentElementNames, 0, this.parentElementNames, 1, parentElementNames.length);
		
		//
		// Use the iterator to get the index of the first element
		//
		
		final ElementIterator iter = this.iterator();
		
		iter.findNextChild();
		
		this.nextParentElements[0] = iter.nextParentElements[0];
	}
	
	protected ElementCollection getResult(final String property) {
		return new ComplexElementCollection(this.parents,
				   							this.nextParentElements,
											this.parentElementNames,
											property);
	}
	
	/**
	 * 
	 * Used by the Invoker when it wants to iterate over this object
	 * 
	 * @return
	 */
	public ElementIterator iterator() {
		return new ElementIterator(this.parents, this.nextParentElements) {
						protected void findNextChild() {	
							this.nextParentElements[0] = this.parents[0].getNextXmlElement(ComplexElementCollection.this.parentElementNames[0], this.nextParentElements[0]);
							
							while (this.nextParentElements[0] == -1) {
								this.parents[0] = findNextParent(1);
								
								if (this.parents[0] == null) {
									return;
								} else {
									this.nextParentElements[0] = this.parents[0].getNextXmlElement(ComplexElementCollection.this.parentElementNames[0], -1);
								}
							}
						}
						
						private XmlList findNextParent(final int i) {
							if (i == this.nextParentElements.length) return null;
							
							this.nextParentElements[i] = this.parents[i].getNextXmlElement(ComplexElementCollection.this.parentElementNames[i], this.nextParentElements[i]);
							
							while (this.nextParentElements[i] == -1) {
								this.parents[i] = findNextParent(i + 1);
								
								if (this.parents[i] == null) {
									return null;
								} else {
									this.nextParentElements[i] = this.parents[i].getNextXmlElement(ComplexElementCollection.this.parentElementNames[i], -1);
								}
							}
						
							return (XmlList)this.parents[i].children[this.nextParentElements[i]];
						}
		};
	}
}
