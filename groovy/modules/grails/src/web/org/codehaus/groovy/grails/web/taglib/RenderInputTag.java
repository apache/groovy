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

import groovy.lang.Writable;
import groovy.text.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.codehaus.groovy.grails.web.servlet.GrailsRequestAttributes;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A tag that attempts to render an input for a bean property into an appropriate component based on the type.
 * It uses the templates defined in "grails-app/views/scaffolding" to achieve this by looking up
 * the template by type.
 *
 * Example:
 * <code>
 *      <gr:renderInput bean="myBean" property="firstName" />
 * </code>
 * Produces for Example (unless customised):
 * <code>
 *      <input type="text" name="firstName" value="Bob" />
 * </code>
 * @author Graeme Rocher
 * @since 12-Jan-2006
 */
public class RenderInputTag extends RequestContextTag {

    public static final String PATH_PREFIX = "/WEB-INF/grails-app/views/scaffolding/";
    public static final String PATH_SUFFIX = ".gsp";
    public static final String TAG_NAME = "renderInput";

    private static final Log LOG = LogFactory.getLog(RenderInputTag.class);

    private static final String BEAN_PROPERTY = "bean";

    private Object bean;
    private String property;
    private BeanWrapper beanWrapper;
    private Map constrainedProperties = Collections.EMPTY_MAP;
    private Map cachedUris = Collections.synchronizedMap(new HashMap());

    protected RenderInputTag() {
        super(TAG_NAME);
    }


    protected void doStartTagInternal() {

         GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass(bean.getClass().getName());
         if(domainClass != null) {
             this.constrainedProperties = domainClass.getConstrainedProperties();
         }
        this.beanWrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor pd = null;
        try {
            pd = this.beanWrapper.getPropertyDescriptor(property);
        } catch (BeansException e) {
            throw new GrailsTagException("Property ["+property+"] is not a valid bean property in tag [renderInput]:" + e.getMessage(),e);
        }
        GroovyPagesTemplateEngine engine = (GroovyPagesTemplateEngine)servletContext.getAttribute(GrailsRequestAttributes.GSP_TEMPLATE_ENGINE);

        Template t = null;
        try {
            String uri = findUriForType(pd.getPropertyType());
            t = engine.createTemplate(uri,
                                               servletContext,
                                               (HttpServletRequest)request,
                                               (HttpServletResponse)response);
            if(t == null)
                throw new GrailsTagException("Type ["+pd.getPropertyType()+"] is unsupported by tag [scaffold]. No template found.");

            Map binding = new HashMap();
            binding.put("name", pd.getName());
            binding.put("value",this.beanWrapper.getPropertyValue(property));
            if(this.constrainedProperties.containsKey(property)) {
                binding.put("constraints",this.constrainedProperties.get(property));
            }
            else {
                binding.put("constraints",null);
            }
            Writable  w = t.make(binding);
            w.writeTo(out);

        } catch (ServletException e) {
            throw new GrailsTagException("Error creating template for type ["+pd.getPropertyType()+"] by tag [scaffold]: " + e.getMessage(),e);
        } catch (IOException e) {
            throw new GrailsTagException("I/O error writing tag ["+getName()+"] to writer: " + e.getMessage(),e);
        }


    }

    protected void doEndTagInternal() {
        // do nothing
    }

    public boolean isDynamicAttribute(String attr) {
        if(BEAN_PROPERTY.equals(attr))
            return true;
        return false;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }


   public String findUriForType(Class type)
            throws MalformedURLException {

        if(LOG.isTraceEnabled()) {
            LOG.trace("[JspRenderInputTag] Attempting to retrieve template for type ["+type+"]");
        }
        String templateUri;
        if(cachedUris.containsKey(type)) {
            templateUri = (String)cachedUris.get(type);
        }
        else {
            templateUri = locateTemplateUrl(type);
            cachedUris.put(type,templateUri);
        }
       return templateUri;
   }

    private String locateTemplateUrl(Class type)
                throws MalformedURLException {
        if(type == Object.class)
            return null;

        String uri = PATH_PREFIX + type.getName() + PATH_SUFFIX;
        URL returnUrl = servletContext.getResource(uri);
        if(returnUrl == null) {
            return locateTemplateUrl(type.getSuperclass());
        }
        return uri;
    }
}
