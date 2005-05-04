/*
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package groovy.servlet;

import groovy.lang.Binding;
import groovy.lang.MetaClass;
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * This servlet will run Groovy scripts as Groovlets.  Groovlets are scripts
 * with these objects implicit in their scope:
 *
 * <ul>
 * 	<li>request - the HttpServletRequest</li>
 *  <li>response - the HttpServletResponse</li>
 *  <li>application - the ServletContext associated with the servlet</li>
 *  <li>session - the HttpSession associated with the HttpServletRequest</li>
 *  <li>out - the PrintWriter associated with the ServletRequest</li>
 * </ul>
 *
 * <p>Your script sources can be placed either in your web application's normal
 * web root (allows for subdirectories) or in /WEB-INF/groovy/* (also allows
 * subdirectories).
 *
 * <p>To make your web application more groovy, you must add the GroovyServlet
 * to your application's web.xml configuration using any mapping you like, so
 * long as it follows the pattern *.* (more on this below).  Here is the
 * web.xml entry:
 *
 * <pre>
 *    <servlet>
 *      <servlet-name>Groovy</servlet-name>
 *      <servlet-class>groovy.servlet.GroovyServlet</servlet-class>
 *    </servlet>
 *
 *    <servlet-mapping>
 *      <servlet-name>Groovy</servlet-name>
 *      <url-pattern>*.groovy</url-pattern>
 *    </servlet-mapping>
 * </pre>
 *
 * <p>The URL pattern does not require the "*.groovy" mapping.  You can, for
 * example, make it more Struts-like but groovy by making your mapping "*.gdo".
 *
 * <p>Whatever your mapping, the GroovyServlet will check to see if your
 * URL ends with ".groovy" and, if it does not, it will strip your mapping
 * and append ".groovy".
 *
 * <p>NOTE!  The GroovyServlet only handles mappings of the *.* type, not the
 * path-like type of /groovy/*</p>
 *
 * @author Sam Pullara
 * @author Mark Turansky (markturansky at hotmail.com)
 * @author Guillaume Laforge
 */
public class GroovyServlet extends HttpServlet implements ResourceConnector {

    // ------------------------------------------------------ instance variables

    /**
     * A constant for ".groovy" which gets appended to script paths as needed.
     */
    public static final String GROOVY_EXTENSION = ".groovy";

    /**
     * The context in which this servlet is executing
     */
    private ServletContext sc;

    /**
     * The classloader associated with this servlet
     */
    private static ClassLoader parent;

    /**
     * The script engine executing the Groovy scripts for this servlet
     */
    private static GroovyScriptEngine gse;

    // ---------------------------------------------------------- public methods

    /**
     * Returns the ServletContext for this servlet
     */
    public ServletContext getServletContext() {
        return sc;
    }

    /**
     * Initialize the GroovyServlet.
     */
    public void init(ServletConfig config) {

        // Use reflection, some containers don't load classes properly
        MetaClass.setUseReflection(true);

        // Get the servlet context
        sc = config.getServletContext();
        sc.log("Groovy servlet initialized");

        // Ensure that we use the correct classloader so that we can find
        // classes in an application server.
        parent = Thread.currentThread().getContextClassLoader();
        if (parent == null)
            parent = GroovyServlet.class.getClassLoader();

        // Set up the scripting engine
        gse = new GroovyScriptEngine(this);
    }

    /**
     * Interface method for ResourceContainer. This is used by the GroovyScriptEngine.
     */
    public URLConnection getResourceConnection(String name) throws ResourceException {
        try {
            URL url = sc.getResource("/" + name);
            if (url == null) {
                url = sc.getResource("/WEB-INF/groovy/" + name);
                if (url == null) {
                    throw new ResourceException("Resource " + name + " not found");
                }
            }
            return url.openConnection();
        } catch (IOException ioe) {
            throw new ResourceException("Problem reading resource " + name);
        }
    }

    /**
     * Handle web requests to the GroovyServlet
     */
    public void service(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {

        // Convert the generic servlet request and response to their Http versions
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        // get the script path from the request
        final String scriptFilename = getGroovyScriptPath(httpRequest);

        // Set it to HTML by default
        response.setContentType("text/html");

        // Set up the script context
        final Binding binding = new ServletBinding((HttpServletRequest) request, (HttpServletResponse) response, sc);

        // Run the script
        try {
            Closure closure = new Closure(gse) {
                public Object call() {
                    try {
                        return ((GroovyScriptEngine)getDelegate()).run(scriptFilename, binding);
                    } catch (ResourceException e) {
                        throw new RuntimeException(e);
                    } catch (ScriptException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            GroovyCategorySupport.use(ServletCategory.class, closure);
        } catch (RuntimeException re) {

            StringBuffer error = new StringBuffer("GroovyServlet Error: ");
            error.append(" script: '");
            error.append(scriptFilename);
            error.append("': ");

            Throwable e = re.getCause();
            if (e instanceof ResourceException) {
                error.append(" Script not found, sending 404.");
                sc.log(error.toString());
                System.out.println(error.toString());
                httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {

                // write the script errors (if any) to the servlet context's log
                if (re.getMessage() != null)
                    error.append(re.getMessage());

                if (e != null) {
                    sc.log("An error occurred processing the request", e);
                } else {
                    sc.log("An error occurred processing the request", re);
                }
                sc.log(error.toString());
                System.out.println(error.toString());

                httpResponse.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    // --------------------------------------------------------- private methods

    /**
     * From the HttpServletRequest, parse the groovy script path using the URL,
     * adding the extension ".groovy" as needed.
     */
    private String getGroovyScriptPath(HttpServletRequest request) {

        // Get the name of the Groovy script - include aware (GROOVY-815)
        String includeURI = (String)request.getAttribute("javax.servlet.include.request_uri");
        String strURI = null;
        if (includeURI != null) {
            strURI = includeURI; 
        } else {
            strURI = request.getRequestURI(); 
        }

        int contextLength = request.getContextPath().length();
        String scriptFilename = strURI.substring(contextLength).substring(1);

        // if the servlet mapping is .groovy, we don't need to strip the mapping from the filename.
        // if the mapping is anything else, we need to strip it and append .groovy
        if (scriptFilename.endsWith(GROOVY_EXTENSION))
            return scriptFilename;

        // strip the servlet mapping (from the last ".") and append .groovy
        int lastDot = scriptFilename.lastIndexOf(".");
        scriptFilename = scriptFilename.substring(0, lastDot)
            + GROOVY_EXTENSION;
        return scriptFilename;

    }
}
