/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package grails.util;

import groovy.xml.MarkupBuilder;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletResponse;

/**
 * <p>OpenRicoBuilder provides support for creating OpenRico AJAX responses.
 * 
 * <p>If this builder is used in controllers no views should be configured since
 * content will be written to the HttpServletResponse instance. Also no operations
 * should be performed on the response object prior to passing it to this builder.
 * 
 * <p>This builder will set the content type of the response to "text/xml" and will 
 * render a correct XML prolog (<?xml version="1.0" encoding="ISO-8859-1"?>).
 * 
 * <p>This builder supports no namespaces or hyphens in element and attribute names.
 * 
 * <p>Use this builder to write a OpenRico reponse to the client. Sending a simple
 * DIV tag to the client requires this code:
 * 
 * <pre>
 * new OpenRicoBuilder(response).ajax { element(id:"personInfo") { div(class:"person") } }
 * </pre>
 * 
 * <p>Sending object XML to the client requires this code:
 * 
 * <pre>
 * new OpenRicoBuilder(response).ajax { object(id:"formLetterUpdater") { 
 *     person(
 *         fullName:"Pat Barnes",
 *         title:"Mr.",
 *         firstName:"Pat",
 *         lastName:"Barnes")
 *     }
 * }
 * </pre>
 * 
 * <p>The first call on a OpenRicoBuilder instance should be "ajax", the second either "element" (to return HTML)
 * or object (to return XML) both with a required "id" attribute. Violations against these rules will be
 * reported as exceptions.
 * 
 * <p>OpenRico responses can contain multiple element and/or object nodes which is supported by this builder.
 * 
 * <p>When using OpenRicoBuilder in controllers you may need to return a null value at the end of a closure to avoid an
 * UnsupporterReturnValueException.
 * 
 * @author Steven Devijver
 * @since Jul 5, 2005
 */
public class OpenRicoBuilder extends MarkupBuilder {

	private static final String TEXT_XML = "text/xml";
	private static final String UTF_8 = "UTF-8";
	private static final String AJAX = "ajax";
	private static final String AJAX_RESPONSE = "ajax-response";
	private static final String ELEMENT = "element";
	private static final String OBJECT = "object";
	private static final String RESPONSE = "response";
	private static final String TYPE = "type";
	private static final String ID = "id";
	private static final String OPENRICO = "OpenRicoBuilder: ";
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private boolean ajaxOnly = true;
	private boolean responseOnly = false;
	private boolean start = true;
	
	public OpenRicoBuilder(HttpServletResponse response) throws IOException {
		this(new GrailsHttpServletResponse(response));
	}
	
	public OpenRicoBuilder(GrailsHttpServletResponse response) throws IOException {
		super(response.getWriter(TEXT_XML, UTF_8));
		getPrinter().println(XML_HEADER);
	}

	protected Object createNode(Object name) {
		if (responseOnly) {
			throw new IllegalArgumentException(OPENRICO + "only call to [element { }] is allowed with attribute [id], not [" + name + "{ }]!");
		}
		if (start && AJAX.equals(name)) {
			ajaxOnly = false;
			responseOnly = true;
			start = false;
			return super.createNode(AJAX_RESPONSE);
		} else if (ajaxOnly) {
			throw new IllegalArgumentException(OPENRICO + "only call to [ajax { }] is allowed, not [" + name + " { }]!");
		} else {
			return super.createNode(name);
		}
	}
		
	protected Object createNode(Object name, Map attributes, Object value) {
		if (checkElementName((String)name, attributes)) {
			return super.createNode(name, attributes, value);
		} else {
			return RESPONSE;
		}
	}
	
	protected Object createNode(Object name, Object value) {
		if (checkElementName((String)name, null)) {
			return super.createNode(name, value);
		} else {
			throw new UnsupportedOperationException(OPENRICO + "invalid method call [" + name + "{ }]!");
		}
	}
	
	protected void nodeCompleted(Object parent, Object node) {
		responseOnly = AJAX_RESPONSE.equals(parent) && RESPONSE.equals(node);
		super.nodeCompleted(parent, node);
	}
	
	private boolean checkElementName(String elementName, Map attributes) {
		if (ajaxOnly) {
			throw new IllegalArgumentException(OPENRICO + "only call to [ajax {}] allowed without arguments, not [" + elementName + " { }]!");
		}
		if (responseOnly) {
			responseOnly = false;
			if (!(ELEMENT.equals(elementName) || OBJECT.equals(elementName))) {
				throw new IllegalArgumentException(OPENRICO + "only calls to [element { }] or [object { }] are allowed, not [" + elementName + " { }]!");
			}
			if (attributes == null || attributes.isEmpty()) {				
					throw new IllegalArgumentException(OPENRICO + "call to [" + elementName + " { }] requires id attribute!");
			} else if (!(attributes.containsKey(ID) && attributes.get(ID) != null)) {
					throw new IllegalArgumentException(OPENRICO + "call to [" + elementName + " { }] requires id attribute with value!");
			} else if (ELEMENT.equals(elementName)) {
				attributes.put(TYPE, ELEMENT);
				super.createNode(RESPONSE, attributes);
			} else if (OBJECT.equals(elementName)) {
				attributes.put(TYPE, OBJECT);
				super.createNode(RESPONSE, attributes);
			}
			return false;
		}
		return true;
	}
}
