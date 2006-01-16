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

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.web.pages.GroovyPage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public abstract class RequestContextTag implements GrailsTag {
    protected Writer out;
    protected Map attributes = new HashMap();
    protected ServletRequest request;
    protected String contextPath;
    protected UrlPathHelper urlPathHelper = new UrlPathHelper();
    protected GrailsTagRegistry registry;
    private boolean init;
    protected BeanWrapper bean;
    protected ServletContext servletContext;
    protected ServletResponse response;
    protected WebApplicationContext applicationContext;
    protected GrailsApplication grailsApplication;
    private String name;

    protected RequestContextTag(String name) {
        this.name = name;
        this.bean = new BeanWrapperImpl(this);
    }

    public String getName() {
        return this.name;
    }

    public void init(Map context) {
        if(context == null)
            throw new IllegalArgumentException("Argument 'context' cannot be null");
        this.out = (Writer)context.get(GroovyPage.OUT);
        this.request = (ServletRequest)context.get(GroovyPage.REQUEST);
        this.servletContext = (ServletContext)context.get(GroovyPage.SERVLET_CONTEXT);
        this.response = (ServletResponse)context.get(GroovyPage.RESPONSE);
        this.applicationContext = RequestContextUtils.getWebApplicationContext(request, servletContext);
        this.grailsApplication = (GrailsApplication)this.applicationContext.getBean(GrailsApplication.APPLICATION_ID);
        if(context.get(GroovyPage.ATTRIBUTES) == null)
            this.attributes = new HashMap();
        else {
            this.attributes = (Map)context.get(GroovyPage.ATTRIBUTES);
        }
        this.contextPath = urlPathHelper.getContextPath( (HttpServletRequest)this.request );
        this.init = true;
    }

    public void setAttribute(String name, Object value) {
        if(this.bean.isWritableProperty(name)) {
            this.bean.setPropertyValue(name,value);
        }else {
            this.attributes.put(name,value);
        }
    }

    public GrailsTagRegistry getRegistry() {
        return this.registry;
    }

    public void setWriter(Writer w) {
        this.out = w;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public final void doStartTag() {
        if(!init)
            throw new IllegalStateException("Tag not initialised called 'init' first");

        doStartTagInternal();
    }

    protected abstract void doStartTagInternal() ;
    protected abstract void doEndTagInternal() ;

    public final void doEndTag()  {
        if(!init)
            throw new IllegalStateException("Tag not initialised called 'init' first");

        doEndTagInternal();
    }
}
