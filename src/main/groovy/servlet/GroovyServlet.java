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
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
 *      <url-pattern>*.gdo</url-pattern>
 *    </servlet-mapping>
 * </pre>
 *
 * <p>The URL pattern does not require the "*.groovy" mapping.  You can, for
 * example, make it more Struts-like but groovy by making your mapping "*.gdo".
 *
 * @author Sam Pullara
 * @author Mark Turansky (markturansky at hotmail.com)
 * @author Guillaume Laforge
 * @author Christian Stein
 * 
 * @see groovy.servlet.ServletBinding
 */
public class GroovyServlet extends AbstractHttpServlet {

  /**
   * The script engine executing the Groovy scripts for this servlet
   */
  private static GroovyScriptEngine gse;

  /**
   * Initialize the GroovyServlet.
   *
   * @throws ServletException
   *  if this method encountered difficulties
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // Set up the scripting engine
    gse = new GroovyScriptEngine(this);

    servletContext.log("Groovy servlet initialized");
  }

  /**
   * Handle web requests to the GroovyServlet
   */
  public void service(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    // Get the script path from the request - include aware (GROOVY-815)
    final String scriptUri = getScriptUri(request);

    // Set it to HTML by default
    response.setContentType("text/html");

    // Set up the script context
    final Binding binding = new ServletBinding(request, response, servletContext);

    // Run the script
    try {
      Closure closure = new Closure(gse) {

        public Object call() {
          try {
            return ((GroovyScriptEngine) getDelegate()).run(scriptUri, binding);
          }
          catch (ResourceException e) {
            throw new RuntimeException(e);
          }
          catch (ScriptException e) {
            throw new RuntimeException(e);
          }
        }

      };
      GroovyCategorySupport.use(ServletCategory.class, closure);
      // Set reponse code 200 and flush buffers
      response.setStatus(HttpServletResponse.SC_OK);
      response.flushBuffer();
      // log("Flushed response buffer.");
    }
    catch (RuntimeException re) {
      StringBuffer error = new StringBuffer("GroovyServlet Error: ");
      error.append(" script: '");
      error.append(scriptUri);
      error.append("': ");
      Throwable e = re.getCause();
      if (e instanceof ResourceException) {
        error.append(" Script not found, sending 404.");
        servletContext.log(error.toString());
        System.out.println(error.toString());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        // write the script errors (if any) to the servlet context's log
        if (re.getMessage() != null)
          error.append(re.getMessage());

        if (e != null) {
          servletContext.log("An error occurred processing the request", e);
        }
        else {
          servletContext.log("An error occurred processing the request", re);
        }
        servletContext.log(error.toString());
        System.out.println(error.toString());
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      }
    }

  }

}
