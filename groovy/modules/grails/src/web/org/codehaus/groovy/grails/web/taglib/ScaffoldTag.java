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
package org.codehaus.groovy.grails.web.taglib;

import groovy.text.Template;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.scaffolding.ServletContextTemplateFactory;
import org.codehaus.groovy.grails.scaffolding.TemplateFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Graeme Rocher
 * @since 06-Jan-2006
 */
public class ScaffoldTag extends RequestContextAwareTag {

    private String bean;
    private String property;
    private BeanWrapper beanWrapper;
    private Map constrainedProperties = Collections.EMPTY_MAP;
    private  static  TemplateFactory templateFactory;

    protected int doStartTagInternal() throws Exception {
        if(templateFactory == null)
             templateFactory = new ServletContextTemplateFactory(pageContext.getServletContext());

        if(StringUtils.isBlank(property)) {
             throw new JspException("Tag [scaffold] missing required attribute [property]");
         }
         if(StringUtils.isBlank(bean)) {
             throw new JspException("Tag [scaffold] missing required attribute [bean]");
         }
         if(!ExpressionEvaluationUtils.isExpressionLanguage(bean)) {
             throw new JspException("Attribute [bean] of tag [scaffold] must be a JSTL expression");
         }
         Writer out = pageContext.getOut();
         try {
             Object beanInstance = ExpressionEvaluationUtils.evaluate("bean",this.bean,Object.class,this.pageContext);
             if(beanInstance == null)
                throw new JspTagException("Bean ["+this.bean+"] referenced by tag [scaffold] cannot be null");

             GrailsApplication application = (GrailsApplication) super.getRequestContext().getWebApplicationContext().getBean(GrailsApplication.APPLICATION_ID);
             if(application != null) {
                 GrailsDomainClass domainClass = application.getGrailsDomainClass(beanInstance.getClass().getName());
                 if(domainClass != null) {
                     this.constrainedProperties = domainClass.getConstrainedProperties();
                 }
             }
             this.beanWrapper = new BeanWrapperImpl(beanInstance);

             PropertyDescriptor pd = this.beanWrapper.getPropertyDescriptor(property);

             Template t = templateFactory.findTemplateForType(pd.getPropertyType());
             if(t == null)
                 throw new JspException("Type ["+pd.getPropertyType()+"] is unsupported by tag [scaffold]. No template found.");

             Map binding = new HashMap();
             binding.put("name", pd.getName());
             binding.put("value",this.beanWrapper.getPropertyValue(property));
             binding.put("request",this.pageContext.getRequest());
             binding.put("response",this.pageContext.getResponse());
             binding.put("application",this.pageContext.getServletContext());
             if(this.constrainedProperties.containsKey(property)) {
                 binding.put("constraints",this.constrainedProperties.get(property));
             }
             else {
                 binding.put("constraints",null);
             }
             out.write(t.make(binding).toString());
         } catch (InvalidPropertyException ipe) {
             throw new JspException("Attribute [property] with value ["+property+"] is not a valid property of bean ["+bean+"] in tag [scaffold]",ipe);
         } catch (IOException e) {
             throw new JspException("I/O error writing tag [scaffold]: " + e.getMessage(),e);
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
