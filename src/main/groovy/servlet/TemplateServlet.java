/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org.
 * 
 * 4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 * 
 * 5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */

import groovy.lang.Binding;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

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
 * It wraps a <code>groovy.text.TemplateEngine</code> to process HTTP
 * requests. By default, it uses the
 * <code>groovy.text.SimpleTemplateEngine</code> which interprets JSP-like (or
 * Canvas-like) templates. <br>
 * <br>
 * 
 * Example <code>HelloWorld.template</code>:
 * 
 * <pre><code>
 * 
 *  &lt;html&gt;
 *  &lt;body&gt;
 *  &lt;% 3.times { %&gt;
 *  Hello World!
 * <br>
 * 
 *  &lt;% } %&gt;
 *  &lt;/body&gt;
 *  &lt;/html&gt; 
 *  
 * </code></pre>
 * 
 * <br>
 * <br>
 * 
 * Note: <br>
 * Automatic binding of context variables and request (form) parameters is
 * disabled by default. You can enable it by setting the servlet config init
 * parameters to <code>true</code>.
 * 
 * <pre><code>
 * bindDefaultVariables = init(&quot;bindDefaultVariables&quot;, false);
 * bindRequestParameters = init(&quot;bindRequestParameters&quot;, false);
 * </code></pre>
 * 
 * @author <a mailto:sormuras@web.de>Christian Stein </a>
 * @version 1.3
 */
public class TemplateServlet extends HttpServlet {

    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    private ServletContext servletContext;

    protected TemplateEngine templateEngine;

    protected boolean bindDefaultVariables;

    protected boolean bindRequestParameters;

    /**
     * Initializes the servlet.
     * 
     * @param config
     *            Passed by the servlet container.
     */
    public void init(ServletConfig config) {

        /*
         * BEGIN
         */
        String className = getClass().getName();
        servletContext.log("Initializing on " + className + "...");

        /*
         * Save the context.
         */
        this.servletContext = config.getServletContext();

        /*
         * Configure from servlet config.
         */
        this.bindDefaultVariables = init(config, "bindDefaultVariables", false);
        this.bindRequestParameters = init(config, "bindRequestParameters", false);

        /*
         * Get TemplateEngine instance.
         */
        this.templateEngine = createTemplateEngine(config);
        if (templateEngine == null) { throw new RuntimeException("Template engine not instantiated."); }

        /*
         * END;
         */
        String engineName = templateEngine.getClass().getName();
        servletContext.log(className + " initialized on " + engineName + ".");
    }

    /**
     * Convient evaluation of boolean configuration parameters.
     * 
     * @return <code>true</code> or <code>false</code>.
     * @param config
     *            Servlet configuration passed by the servlet container.
     * @param param
     *            Name of the paramter to look up.
     * @param value
     *            Default value if parameter name is not set.
     */
    protected boolean init(ServletConfig config, String param, boolean value) {
        String string = config.getInitParameter(param);
        if (string == null) { return value; }
        return Boolean.valueOf(string).booleanValue();
    }

    /**
     * Creates the template engine.
     * 
     * Called by {@link #init(ServletConfig)}and returns just <code>
     * new SimpleTemplateEngine()</code>.
     * Override this method to alter this behaviour and return an engine which
     * serves your needs better.
     * 
     * @return The underlying template engine.
     * @param config
     *            This serlvet configuration passed by the container.
     * @see #createTemplateEngine()
     */
    protected TemplateEngine createTemplateEngine(ServletConfig config) {
        return new SimpleTemplateEngine();
    }

    /**
     * Delegates to {@link #doRequest(HttpServletRequest, HttpServletResponse)}.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    /**
     * Delegates to {@link #doRequest(HttpServletRequest, HttpServletResponse)}.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    /**
     * Processes all requests by dispatching to helper methods.
     * 
     * TODO Outline the algorithm. Although the method names are well-chosen. :)
     * 
     * @param request
     *            The http request.
     * @param response
     *            The http response.
     * @throws ServletException
     *             ...
     * @throws IOException
     *             ...
     */
    protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {

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

        }
        catch (Exception exception) {

            /*
             * Call exception handling hook.
             */
            error(request, response, exception);

        }
        finally {

            /*
             * Indicate we are finally done with this request.
             */
            requestDone(request, response, binding);

        }

    }

    /**
     * Creates the application context.
     * 
     * Sets 5 variables if and only if <code>bindDefaultParameters</code> is
     * <code>true</code>:
     * 
     * <pre><code>
     * binding.setVariable(&quot;request&quot;, request);
     * binding.setVariable(&quot;response&quot;, response);
     * binding.setVariable(&quot;context&quot;, servletContext);
     * binding.setVariable(&quot;session&quot;, request.getSession(true));
     * binding.setVariable(&quot;out&quot;, response.getWriter());
     * </code></pre>
     * 
     * Binds all form parameters, too. This is, where we leave the clean MVC
     * pattern and Velocity behind. (...) Nobody told you to quit Velocity
     * anyway. :)
     * 
     * @return Groovy Binding also known as application context.
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @throws Exception
     *             Any exception.
     */
    protected Binding createBinding(HttpServletRequest request, HttpServletResponse response) throws Exception {

        /*
         * Create empty binding.
         */
        Binding binding = new Binding();

        /*
         * Bind default variables.
         */
        if (bindDefaultVariables) {
            binding.setVariable("request", request);
            binding.setVariable("response", response);
            binding.setVariable("context", servletContext);
            binding.setVariable("session", request.getSession(true));
            binding.setVariable("out", response.getWriter());
        }

        /*
         * Bind form (aka request) parameters.
         */
        if (bindRequestParameters) {
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
                }
                else {
                    binding.setVariable(key, values);
                }
            }
        }

        return binding;

    }

    /**
     * Sets {@link #DEFAULT_CONTENT_TYPE}.
     * 
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     */
    protected void setContentType(HttpServletRequest request, HttpServletResponse response) {

        response.setContentType(DEFAULT_CONTENT_TYPE);

    }

    /**
     * Default request handling. <br>
     * 
     * Leaving Velocity behind again. The template, actually the Groovy code in
     * it, could handle/process the entire request. Good or not? This depends on
     * you! :)<br>
     * 
     * Anyway, here no exception is thrown -- but it's strongly recommended to
     * override this method in derived class and do the real processing against
     * the model inside it. The template should be used, like Velocity
     * templates, to produce the view, the html page. Again, it's up to you!
     * 
     * @return The template that will be merged.
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @param binding
     *            The application context.
     * @throws Exception
     */
    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Binding binding)
            throws Exception {

        /*
         * Delegate to getTemplate(String).
         */
        return getTemplate(request);

    }

    /**
     * Gets the template by its name.
     * 
     * @return The template that will be merged.
     * @param templateName
     *            The name of the template.
     * @throws Exception
     *             Any exception.
     */
    protected Template getTemplate(HttpServletRequest request) throws Exception {

        /*
         * Delegate to resolveTemplateName(String). Twice if necessary.
         */
        URL url = resolveTemplateName(request.getServletPath());
        if (url == null) {
            url = resolveTemplateName(request.getRequestURI());
        }

        /*
         * Template not found?
         */
        if (url == null) {
            String uri = request.getRequestURI();
            servletContext.log("Resource \"" + uri + "\" not found.");
            throw new FileNotFoundException(uri);
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
     * @param templateName
     *            The name of the template.
     * @throws Exception
     *             Any exception.
     */
    protected URL resolveTemplateName(String templateName) throws Exception {

        URL url = null;

        //servletContext.log("Resolving \"" + templateName + "\"...");
        //servletContext.log(servletContext.getRealPath(templateName));

        /*
         * Try servlet context resource facility.
         * 
         * Good for names pointing to templates relatively to the servlet
         * context.
         */
        url = servletContext.getResource(templateName);
        if (url != null) { return url; }

        /*
         * Try the class loader, that loaded this class.
         * 
         * Good for templates located within the class path.
         */
        url = getClass().getResource(templateName);
        if (url != null) { return url; }

        /*
         * Still here? Try system and other class loaders.
         */
        ClassLoader classLoader = null;

        /*
         * Try the system class loader.
         */
        classLoader = ClassLoader.getSystemClassLoader();
        url = classLoader.getResource(templateName);
        if (url != null) { return url; }

        /*
         * Try the thread context class loader.
         */
        classLoader = Thread.currentThread().getContextClassLoader();
        url = classLoader.getResource(templateName);
        if (url != null) { return url; }

        /*
         * Still, still here? Just return null.
         */
        return null;

    }

    /**
     * Gets the template by its url.
     * 
     * @return The template that will be merged.
     * @param templateURL
     *            The url of the template.
     * @throws Exception
     *             Any exception.
     */
    protected Template getTemplate(URL templateURL) throws Exception {

        /*
         * Let the engine create the template from given URL.
         * 
         * TODO Is createTemplate(Reader); faster? Fail safer?
         */
        return templateEngine.createTemplate(templateURL);

    }

    /**
     * Merges the template and writes response.
     * 
     * @param template
     *            The template that will be merged... now!
     * @param binding
     *            The application context.
     * @param response
     *            The HTTP response.
     * @throws Exception
     *             Any exception.
     */
    protected void merge(Template template, Binding binding, HttpServletResponse response) throws Exception {

        /*
         * Set binding and write response.
         */
        template.setBinding(binding.getVariables());
        template.writeTo(response.getWriter());

    }

    /**
     * Simply sends an internal server error page (code 500).
     * 
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @param exception
     *            The cause.
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception exception) {

        try {
            response.sendError(500, exception.getMessage());
        }
        catch (IOException ioException) {
            servletContext.log("Should not happen.", ioException);
        }

    }

    /**
     * Called one request is processed.
     * 
     * This clean-up hook is always called, even if there was an exception
     * flying around and the error method was executed.
     * 
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @param binding
     *            The application context.
     */
    protected void requestDone(HttpServletRequest request, HttpServletResponse response, Binding binding) {

        /*
         * Nothing to clean up.
         */
        return;

    }

}