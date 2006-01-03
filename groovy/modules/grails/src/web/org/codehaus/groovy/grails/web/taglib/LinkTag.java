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
package org.codehaus.groovy.grails.web.taglib;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UrlPathHelper;
/**
 * A link tag for easily creating links to controllers and actions within grails. Examples:
 * 
 * <code>
 * 		<gr:link controller="entry" action="list" />
 * 		<gr:link controller="entry"  />
 * 		<gr:link controller="entry" action="edit" id="1" />
 *  
 *  </code>
 * @author Graeme Rocher
 * @since Jan 3, 2006
 */
public class LinkTag extends BodyTagSupport implements DynamicAttributes {

	Map dyanmicAttributes = new HashMap();
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	private String action;
	private String controller;
	private String id;
	
	/**
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param controller The controller to set.
	 */
	public void setController(String controller) {
		this.controller = controller;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		Writer out = super.pageContext.getOut();
		
		String contextPath = urlPathHelper.getContextPath( (HttpServletRequest)pageContext.getRequest() );
		try {
			StringBuffer buf = new StringBuffer();
			
			buf.append("<a href=\"")
				.append(contextPath)
				.append('/')
				.append(controller);
				if(!StringUtils.isBlank(action)) {
					buf.append('/')
						.append(action);
				}
				if(!StringUtils.isBlank(id)) {
					Object evalId;
					if(id.startsWith("${") && id.endsWith("}")) {
						try {
							evalId = pageContext.getExpressionEvaluator().evaluate(id, Object.class, pageContext.getVariableResolver(),null);
						} catch (ELException e) {
							evalId = id;
						}
					}	
					else {
						evalId = id;
					}
					buf.append('?')
					   .append("id=")
					   .append(evalId);
				}			
				
				buf.append("\" ");
			
				if(dyanmicAttributes.size() > 0) {
					for (Iterator i = dyanmicAttributes.keySet().iterator(); i.hasNext();) {
						String attributeName = (String) i.next();
						Object attributeValue = dyanmicAttributes.get(attributeName);
						buf.append(attributeName)
						   .append("=\"")
						   .append(attributeValue)
						   .append("\" ");
					}
				}			
				buf.append('>');
				out.write(buf.toString());
		} catch (IOException e) {
			throw new JspException(e.getMessage(),e);
		}
		return EVAL_BODY_BUFFERED;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		BodyContent b = getBodyContent();
		JspWriter out = b.getEnclosingWriter();
		try {
			out.write(b.getString());
		} catch (IOException e) {
			throw new JspException(e.getMessage(),e);
		}
		b.clearBody();
		return SKIP_BODY;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		Writer out = super.pageContext.getOut();
		try {
			out.write("</a>");
		} catch (IOException e) {
			throw new JspException(e.getMessage(),e);
		}
		return super.doEndTag();
	}



	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
		dyanmicAttributes.put(localName,value);
	}
}
