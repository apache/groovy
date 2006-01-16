/* Copyright 2004-2005 the original author or authors.
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
import org.codehaus.groovy.grails.web.taglib.GrailsTagRegistry;
import org.codehaus.groovy.grails.web.taglib.RenderInputTag;
import org.codehaus.groovy.grails.web.pages.GroovyPage;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;

/**
 * A JSP facade that delegates to the Grails RenderInputTag (@see org.codehaus.groovy.grails.web.taglib.RenderInputTag)
 * 
 * @author Graeme Rocher
 * @since 06-Jan-2006
 */
public class JspRenderInputTag extends RequestContextAwareTag {

    private String bean;
    private String property;

    protected int doStartTagInternal() throws Exception {

        if(StringUtils.isBlank(property)) {
             throw new JspTagException("Tag [scaffold] missing required attribute [property]");
         }
         if(StringUtils.isBlank(bean)) {
             throw new JspTagException("Tag [scaffold] missing required attribute [bean]");
         }
         if(!ExpressionEvaluationUtils.isExpressionLanguage(bean)) {
             throw new JspTagException("Attribute [bean] of tag [scaffold] must be a JSTL expression");
         }
         Writer out = pageContext.getOut();
         try {
             Object beanInstance = ExpressionEvaluationUtils.evaluate("bean",this.bean,Object.class,this.pageContext);
             if(beanInstance == null)
                throw new JspTagException("Bean ["+this.bean+"] referenced by tag [scaffold] cannot be null");

            GrailsTagRegistry tagRegistry = GrailsTagRegistry.getInstance();
            Map tagContext = new HashMap();
            tagContext.put(GroovyPage.REQUEST, pageContext.getRequest());
            tagContext.put(GroovyPage.RESPONSE, pageContext.getResponse());
            tagContext.put(GroovyPage.SERVLET_CONTEXT, pageContext.getServletContext());
            RenderInputTag tag = (RenderInputTag)tagRegistry.newTag( RenderInputTag.TAG_NAME );
            tag.init(tagContext);
            tag.setBean(beanInstance);
            tag.setProperty(property);
            tag.doStartTag();

         } catch (InvalidPropertyException ipe) {
             throw new JspException("Attribute [property] with value ["+property+"] is not a valid property of bean ["+bean+"] in tag [scaffold]",ipe);
         }
        return SKIP_BODY;
    }


    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
