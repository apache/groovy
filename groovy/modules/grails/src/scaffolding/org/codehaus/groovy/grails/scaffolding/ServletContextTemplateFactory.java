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
package org.codehaus.groovy.grails.scaffolding;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilationFailedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;

/**
 * A template factory that looks up templates from the servlet context
 *
 * @author Graeme Rocher
 * @since 06-Jan-2006
 */
public class ServletContextTemplateFactory implements TemplateFactory {

    public static final Log LOG = LogFactory.getLog(ServletContextTemplateFactory.class);
    public static final String PATH_PREFIX = "/WEB-INF/grails-app/views/scaffolding/";
    public static final String PATH_SUFFIX = ".gsp";

    private ServletContext servletContext;
    private TemplateEngine templateEngine = new SimpleTemplateEngine();
    private Map cachedTemplates = new HashMap();
    private Map cachedUrls = new HashMap();
    public ServletContextTemplateFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Simple class to hold the last modified info and file name
     */
    class StoredTemplate {
        private Template template;
        private URL url;
        private long lastModified;
    }
    public Template findTemplateForType(Class type) {
        try {
            if(LOG.isTraceEnabled()) {
                LOG.trace("[TemplateFactory] Attempting to retrieve template for type ["+type+"]");
            }
            URL templateUrl;
            if(cachedTemplates.containsKey(type)) {
                templateUrl = (URL)cachedUrls.get(type);
            }
            else {
                templateUrl = locateTemplateUrl(type);
                cachedUrls.put(type,templateUrl);
            }

            if(templateUrl != null) {
                Template t;
                URLConnection con = templateUrl.openConnection();

                if(cachedTemplates.containsKey(type)) {
                    StoredTemplate st = (StoredTemplate)cachedTemplates.get(type);
                    if(con.getLastModified() > st.lastModified) {
                        t = templateEngine.createTemplate(templateUrl);
                        st.template = t;
                        st.lastModified = con.getLastModified();
                    }
                    else {
                        t = st.template;
                    }
                }
                else {
                    t = templateEngine.createTemplate(templateUrl);
                    StoredTemplate st = new StoredTemplate();
                    st.lastModified = con.getLastModified();
                    st.template = t;
                    st.url = templateUrl;
                    cachedTemplates.put(type,st);
                }
                if(LOG.isTraceEnabled()) {
                    LOG.trace("[TemplateFactory] Building template for URL ["+templateUrl+"]");
                }

                return t;
            }

        } catch (CompilationFailedException e) {
            LOG.error("Failed to compile template for type ["+type+"]: " + e.getMessage(),e);
        } catch (ClassNotFoundException e) {
            LOG.error("Class not found compiling template for type ["+type+"]: " + e.getMessage(),e);
        } catch (IOException e) {
           LOG.error("I/O error reading template for type ["+type+"]: " + e.getMessage(),e);
        }
        return null;
    }

    private URL locateTemplateUrl(Class type)
                throws MalformedURLException {
        if(type == Object.class)
            return null;

        URL returnUrl = this.servletContext.getResource(PATH_PREFIX + type.getName() + PATH_SUFFIX);
        if(returnUrl == null) {
            return locateTemplateUrl(type.getSuperclass());
        }
        return returnUrl;
    }

    public Template findNamedTemplateForType(Class type, String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
