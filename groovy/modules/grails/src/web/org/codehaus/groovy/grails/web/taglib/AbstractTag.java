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

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public abstract class AbstractTag implements GrailsTag {
    protected Writer out;
    protected Map attributes;
    protected ServletRequest request;
    protected String contextPath;
    protected UrlPathHelper urlPathHelper = new UrlPathHelper();
    protected GrailsTagRegistry registry;
    private boolean init;
    private BeanWrapper bean;


    public void init(GrailsTagContext context) {
        if(context == null)
            throw new IllegalArgumentException("Argument 'context' cannot be null");
        try {
            this.out = context.getOut();
        } catch (IOException e) {
            throw new GrailsTagException("I/O error obtaining response writer: " + e.getMessage(),e);
        }
        this.request = context.getRequest();
        this.bean = new BeanWrapperImpl(this);
        if(context.getAttributes() == null)
            this.attributes = new HashMap();
        else {
            this.attributes = context.getAttributes();
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

    public void doStartTag() throws IOException {
        if(!init)
            throw new IllegalStateException("Tag not initialised called 'init' first");

        doStartTagInternal();
    }

    protected abstract void doStartTagInternal()  throws IOException;
    protected abstract void doEndTagInternal()  throws IOException;

    public void doEndTag() throws IOException {
        if(!init)
            throw new IllegalStateException("Tag not initialised called 'init' first");

        doEndTagInternal();
    }
}
