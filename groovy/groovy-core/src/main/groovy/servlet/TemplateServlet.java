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

import groovy.lang.Binding;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.util.ResourceException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A generic servlet for templates.
 * 
 * It wraps a <code>groovy.text.TemplateEngine</code> to process HTTP requests.
 * By default, it uses the <code>groovy.text.SimpleTemplateEngine</code> which
 * interprets Canvas-like (or JSP-like) templates.
 * <br>
 * <br>
 * 
 * Example <code>HelloWorld.canvas</code>:
 * <pre><code>
 * <html>
 * <body>
 * <% 3.times { %>
 * Hello Canvas World!<br>
 * <% } %>
 * </body>
 * </html> 
 * </code><pre>
 * <br>
 * 
 * @author <a mailto:sormuras@web.de>Christian Stein</a>
 * @version 1.2
 */
public class TemplateServlet extends HttpServlet {
    
    public static final String DEFAULT_CONTENT_TYPE = "text/html";
    
    private TemplateEngine templateEngine;
    
    private ServletContext servletContext;
    
    /**
     * Initializes the servlet.
     * 
     * @param config Passed by the servlet container.
     */
    public void init(ServletConfig config) {
        templateEngine = createTemplateEngine(config);
        if (templateEngine == null) {
            throw new RuntimeException("Template engine not instantiated.");
        }
        servletContext = config.getServletContext();
        servletContext.log(
                getClass().getName() + " initialized. (" + templateEngine + ")");
    }
    
    /**
     * Creates the template engine.
     * 
     * Called by {@link #init(ServletConfig)} and returns just <code>
     * new SimpleTemplateEngine()</code>. Override this method to alter
     * this behaviour and return an engine which serves your needs
     * better.
     * 
     * @return The underlying template engine.
     * @param config This serlvet configuration passed by the container.
     * @see #createTemplateEngine()
     */
    protected TemplateEngine createTemplateEngine(ServletConfig config) {
        return new SimpleTemplateEngine();
    }
    
    /**
     * Delegates to {@link #doRequest(HttpServletRequest, HttpServletResponse)}.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doRequest(request, response);
    }
    
    /**
     * Delegates to {@link #doRequest(HttpServletRequest, HttpServletResponse)}.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doRequest(request, response);
    }
    
    /**
     * Processes all requests by dispatching to helper methods.
     * 
     * TODO: Outline the algorithm. Although the method name are well-chosen. :)
     * 
     * @param request The http request.
     * @param response The http response.
     * @throws ServletException ...
     * @throws IOException ...
     */
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException {
        
        Binding binding = null;
        
        try {
            
            /*
             * Create binding.
             */
            binding = createBinding(request, response);
            
            /*
             * Set default content type.
             */
            setContentType(request, response);
            
            /*
             * Create the template by its engine.
             */
            Template template = handleRequest(request, response, binding);
            
            /*
             * Let the template, that is groovy, do the merge.
             */
            merge(template, binding, response);
            
        } catch (Exception exception) {
            
            /*
             * Call exception handling hook.
             */
            error(request, response, exception);
            
        } finally {
            
            /*
             * Indicate we are finally done with this request.
             */
            requestDone(request, response, binding);
            
        }
        
    }
    
    /**
     * Creates the application context.
     * 
     * Sets 5 variables by default:
     * <pre><code>
     *   binding.setVariable("request", request);
     *   binding.setVariable("response", response);
     *   binding.setVariable("context", servletContext);
     *   binding.setVariable("session", request.getSession(true));
     *   binding.setVariable("out", response.getWriter());
     * </code></pre>
     *
     * Binds all form parameters, too. This is, where we leave the clean 
     * MVC pattern and Velocity behind. (...) Nobody told you to quit
     * Velocity anyway. :)  
     * 
     * @return Groovy Binding also known as application context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @throws Exception Any exception.
     */
    protected Binding createBinding(
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        
        /*
         * Set default bindings.
         */
        Binding binding = new Binding();
        binding.setVariable("request", request);
        binding.setVariable("response", response);
        binding.setVariable("context", servletContext);
        binding.setVariable("session", request.getSession(true));
        binding.setVariable("out", response.getWriter());
        
        /*
         * 
         */
        bindRequestParameters(request, binding);
        
        return binding;
        
    }
    
    /**
     * Binds form parameters.
     *
     * If there are multiple values for one parameter key, they 
     * are passed as a String array (which is converted by Groovy
     * to a list, right?).
     * 
     * @param request
     * @param binding
     */
    protected void bindRequestParameters(
            HttpServletRequest request,
            Binding binding) {
        
        /*
         * Iterate over all parameter names provided by the request.  
         */
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = (String) parameterNames.nextElement();
            if (binding.getVariables().containsKey(key)) {
                servletContext.log("Key \"" + key + "\" already bound.");
                continue;
            }
            String[] values = request.getParameterValues(key);
            if (values.length == 1) {
                binding.setVariable(key, values[0]);
            } else {
                binding.setVariable(key, values);
            }
        }
        
    }
    
    /**
     * Sets {@link #DEFAULT_CONTENT_TYPE}.
     * 
     * @param request The HTTP request.
     * @param response The HTTP response.
     */
    protected void setContentType(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        response.setContentType(DEFAULT_CONTENT_TYPE);
        
    }
    
    /**
     * Default request handling.
     * <br>
     * 
     * Leaving Velocity behind again. The template, actually the
     * Groovy code in it, can handle/process the entire request.
     * Good or not? This depends on you! :)
     * <br>
     * 
     * Anyway, here no exception is thrown -- but it's strongly
     * recommended to override this method in derived class and
     * do the real processing against the model inside it. The
     * canvas template should be used, like Velocity templates,
     * to produce the view, the html page. Again, it's up to you!
     * 
     * @return The template that will be merged. 
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param binding The application context.
     * @throws Exception
     */
    protected Template handleRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Binding binding)
    throws Exception {
        
        /*
         * Delegate to getTemplate(String).
         */
        return getTemplate(request.getRequestURI());
        
    }
    
    /**
     * Gets the template by its name.
     * 
     * @return The template that will be merged. 
     * @param templateName The name of the template.
     * @throws Exception Any exception.
     */
    protected Template getTemplate(String templateName) throws Exception {
        
        /*
         * Delegate to resolveTemplateName(String).
         */
        URL url = resolveTemplateName(templateName);
        
        /*
         * Template not found?
         */
        if (url == null) {
            servletContext.log("Resource \"" + templateName + "\" not found.");
            throw new FileNotFoundException(templateName);
        }
        
        /*
         * Delegate to getTemplate(URL).
         */
        return getTemplate(url);
        
    }
    
    /**
     * Locate template and convert its location to an URL.
     * 
     * @return The URL pointing to the resource... the template.
     * @param templateName The name of the template.
     * @throws Exception Any exception.
     */
    protected URL resolveTemplateName(String templateName) throws Exception {
        
        URL url = null;
        
        /*
         * Try servlet context resource facility.
         * 
         * Good for names pointing to templates relatively to
         * the servlet context. 
         */
        url = servletContext.getResource(templateName);
        if (url != null) {
            return url;
        }
        
        /*
         * Try the class loader, that loaded this class.
         * 
         * Good for templates located within the class path.
         */
        url = getClass().getResource(templateName);
        if (url != null) {
            return url;
        }
        
        /*
         * Still here? Try system and other class loaders.
         */
        ClassLoader classLoader = null;
        
        /*
         * Try the system class loader.
         */
        classLoader = ClassLoader.getSystemClassLoader();
        url = classLoader.getResource(templateName);
        if (url != null) {
            return url;
        }
        
        /*
         * Try the thread context class loader.
         */
        classLoader = Thread.currentThread().getContextClassLoader();
        url = classLoader.getResource(templateName);
        if (url != null) {
            return url;
        }
        
        /*
         * Still, still here? Throw an exception.
         */
        throw new ResourceException("Template \"" + templateName + "\" not found.");
        
    }
    
    /**
     * Gets the template by its url.
     * 
     * @return The template that will be merged. 
     * @param templateURL The url of the template.
     * @throws Exception Any exception.
     */
    protected Template getTemplate(URL templateURL) throws Exception {
        
        /*
         * Let the engine create the template from given URL.
         * 
         * TODO: Is createTemplate(Reader); faster? Fail safer?
         */
        return templateEngine.createTemplate(templateURL);
        
    }
    
    /**
     * Merges the template and writes response.
     * 
     * @param template The template that will be merged... now! 
     * @param binding The application context.
     * @param response The HTTP response.
     * @throws Exception Any exception.
     */
    protected void merge(
            Template template,
            Binding binding,
            HttpServletResponse response)
    throws Exception {
        
        /*
         * Set binding and write response.
         */
        template.setBinding(binding.getVariables());
        template.writeTo(response.getWriter());
        
    }
    
    /**
     * Simply sends an internal server error page (code 500).
     * 
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param exception The cause.
     */
    protected void error(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception) {
        
        try {
            response.sendError(500, exception.getMessage());
        } catch (IOException ioException) {
            servletContext.log("Should not happen.", ioException);
        }
        
    }
    
    /**
     * Called one request is processed.
     * 
     * This clean-up hook is always called, even if there was an
     * exception flying around and the error method was executed. 
     * 
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param binding The application context.
     */
    protected void requestDone(
            HttpServletRequest request,
            HttpServletResponse response,
            Binding binding) {
        
        /*
         * Nothing to clean up. 
         */
        return;
        
    }
    
}
