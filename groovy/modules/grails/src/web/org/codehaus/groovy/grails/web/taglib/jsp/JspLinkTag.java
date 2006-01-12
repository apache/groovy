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
package org.codehaus.groovy.grails.web.taglib.jsp;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.web.taglib.GrailsTag;
import org.codehaus.groovy.grails.web.taglib.GrailsTagRegistry;
import org.codehaus.groovy.grails.web.taglib.LinkTag;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
/**
 * A JSP facade that delegates to the Grails LinkTag (@see org.codehaus.groovy.grails.web.taglib.LinkTag)
 *
 * @author Graeme Rocher
 * @since Jan 3, 2006
 */
public class JspLinkTag extends BodyTagSupport implements DynamicAttributes {

	Map dyanmicAttributes = new HashMap();
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	private String action;
	private String controller;
	private String id;
    private GrailsTag tag;

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
        if(StringUtils.isBlank(controller)) {
            throw new JspTagException("Tag link missing required attribute 'controller'");
        }
        Writer out = super.pageContext.getOut();


        Map attributes = new HashMap();
        attributes.putAll(this.dyanmicAttributes);
        attributes.put(LinkTag.ATTRIBUTE_CONTROLLER, controller);
        if(!StringUtils.isBlank(action)) {
            attributes.put(LinkTag.ATTRIBUTE_ACTION, action);
        }
        if(!StringUtils.isBlank(id)) {
            Object evalId;
            if(ExpressionEvaluationUtils.isExpressionLanguage(id)) {
                evalId = ExpressionEvaluationUtils.evaluate("id",id, Object.class, pageContext);
            }
            else {
                evalId = pageContext.findAttribute(id);
                if(evalId == null)
                    evalId = id;
            }
            attributes.put(LinkTag.ATTRIBUTE_ID, evalId);
        }

        try {
            GrailsTagRegistry tagRegistry = GrailsTagRegistry.getInstance();
            this.tag = tagRegistry.loadTag( LinkTag.TAG_NAME,
                                pageContext.getServletContext(),
                                (HttpServletRequest)pageContext.getRequest(),
                                (HttpServletResponse)pageContext.getResponse());

            this.tag.setAttributes(attributes);            
            tag.doStartTag();
		} catch (IOException e) {
			throw new JspTagException(e.getMessage(),e);
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
		try {
			tag.doEndTag();
		} catch (IOException e) {
			throw new JspException(e.getMessage(),e);
		}
		return super.doEndTag();
	}



	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
		dyanmicAttributes.put(localName,value);
	}
}
