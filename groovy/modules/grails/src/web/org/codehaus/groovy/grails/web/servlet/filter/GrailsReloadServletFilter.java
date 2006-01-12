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
package org.codehaus.groovy.grails.web.servlet.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.io.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyClassLoader;

/**
 * A servlet filter that copies resources from the source on content change and manages reloading if necessary
 *
 * @author Graeme Rocher
 * @since Jan 10, 2006
 */
public class GrailsReloadServletFilter extends OncePerRequestFilter {

    public static final Log LOG = LogFactory.getLog(GrailsReloadServletFilter.class);

    private Resource basedir;
    private Resource destdir;
    private GroovyObject copyScript;

    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Appling filter for basedir ["+basedir+"]");
            LOG.debug("Appling filter for destdir ["+destdir+"]");
        }
        if(basedir != null && destdir != null) {
              if(copyScript == null) {
                  GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

                  Class groovyClass = null;
                  try {
                      groovyClass = gcl.parseClass(gcl.getResource("org/codehaus/groovy/grails/web/servlet/filter/GrailsResourceCopier.groovy").openStream());
                      copyScript = (GroovyObject)groovyClass.newInstance();
                  } catch (IllegalAccessException e) {
                      LOG.error("Illegal access creating resource copier. Save/reload disabled: " + e.getMessage(), e);
                  } catch (InstantiationException e) {
                      LOG.error("Error instantiating resource copier. Save/reload disabled: " + e.getMessage(), e);
                  } catch (CompilationFailedException e) {
                       LOG.error("Error compiling resource copier. Save/reload disabled: " + e.getMessage(), e);
                  } catch(Exception e) {
                     LOG.error("Error loading resource copier. Save/reload disabled: " + e.getMessage(), e);
                  }


              }

            if(copyScript != null) {
                copyScript.invokeMethod("run", new Object[0]);
            }
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }

    /**
     * Sets the base directory of the source project
     * @param basedir
     */
    public void setBasedir(Resource basedir) {
        this.basedir = basedir;
    }

    /**
     * Sets the destination deployment directory
     * @param destdir
     */
    public void setDestdir(Resource destdir) {
        this.destdir = destdir;
    }
}
