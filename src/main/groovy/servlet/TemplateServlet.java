/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package groovy.servlet;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Servlet for "canvas-like" files.
 * 
 * @author Sormuras <sormuras@uni-koblenz.de>
 */
public class TemplateServlet extends HttpServlet {

    private ServletContext servletContext;
    private TemplateEngine templateEngine;

    public void init(ServletConfig config) {
        servletContext = config.getServletContext();
        templateEngine = new SimpleTemplateEngine();
        servletContext.log(getClass().getName() + " initialized.");
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

        /*
         * Cast to request and response to their HTTP equivalents.
         */
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        /*
         * Locate requested template resource. If it can't be found, send an
         * 404.
         */
        String uri = httpRequest.getRequestURI();
        URL url = getClass().getResource(uri);
        if (url == null) {
            url = servletContext.getResource(uri);
            if (url == null) {
                url = ClassLoader.getSystemResource(uri);
            }
        }
        if (url == null) {
            servletContext.log("Resource uri=\"" + uri + "\" not locatable.");
            httpResponse.sendError(404);
            return;
        }

        /*
         * Set default content type and link "out" short-cut.
         */
        httpResponse.setContentType("text/html");
        PrintWriter out = httpResponse.getWriter();

        /*
         * Create binding used by the template engine. Bind form parameters,
         * too. If there are multiple values for one parameter key, they are
         * passed as a String[] (which is converted by Groovy to a list?).
         */
        Map binding = new HashMap();
        binding.put("request", httpRequest);
        binding.put("response", httpResponse);
        binding.put("application", servletContext); // rename key to "context"?
        binding.put("session", httpRequest.getSession(true));
        binding.put("out", out); // remove from binding?
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            Object key = parameterNames.nextElement();
            if (binding.containsKey(key)) {
                servletContext.log("Key \"" + key + "\" already bound.");
            }
            else {
                String[] values = request.getParameterValues((String) key);
                if (values.length == 1) {
                    binding.put(key, values[0]);
                }
                else {
                    binding.put(key, values);
                }
            }
        }

        /*
         * Let the template engine do the work.
         */
        try {
            Template template = templateEngine.createTemplate(url);
            template.setBinding(binding);
            template.writeTo(out);
        }
        catch (FileNotFoundException e) {
            httpResponse.sendError(404, e.getMessage());
        }
        catch (SyntaxException e) {
            httpResponse.sendError(500, e.getMessage());
        }
        catch (ClassNotFoundException e) {
            httpResponse.sendError(500, e.getMessage());
        }
        catch (IOException e) {
            httpResponse.sendError(500, e.getMessage());
        }

    }

}