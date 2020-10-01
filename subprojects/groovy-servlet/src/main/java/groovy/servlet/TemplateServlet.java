/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.servlet;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A generic servlet for serving (mostly HTML) templates.
 * <p>
 * It delegates work to a <code>groovy.text.TemplateEngine</code> implementation
 * processing HTTP requests.
 * <p>
 * <h4>Usage</h4>
 * <p>
 * <code>helloworld.html</code> is a headless HTML-like template
 * <pre><code>
 *  &lt;html&gt;
 *    &lt;body&gt;
 *      &lt;% 3.times { %&gt;
 *        Hello World!
 *      &lt;% } %&gt;
 *      &lt;br&gt;
 *    &lt;/body&gt;
 *  &lt;/html&gt;
 * </code></pre>
 * <p>
 * Minimal <code>web.xml</code> example serving HTML-like templates
 * <pre><code>
 * &lt;web-app&gt;
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </code></pre>
 * <p>
 * <h4>Template engine configuration</h4>
 * <p>
 * By default, the TemplateServer uses the {@link groovy.text.SimpleTemplateEngine}
 * which interprets JSP-like templates. The init parameter <code>template.engine</code>
 * defines the fully qualified class name of the template to use:
 * <pre>
 *   template.engine = [empty] - equals groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.GStringTemplateEngine
 *   template.engine = groovy.text.XmlTemplateEngine
 * </pre>
 * <p>
 * <h3>Servlet Init Parameters</h3>
 * <p>
 * <h4>Logging and extra-output options</h4>
 * <p>
 * This implementation provides a verbosity flag switching log statements.
 * The servlet init parameter name is:
 * <pre>
 *   generated.by = true(default) | false
 * </pre>
 * <p>
 * <h4>Groovy Source Encoding Parameter</h4>
 * <p>
 * The following servlet init parameter name can be used to specify the encoding TemplateServlet will use
 * to read the template groovy source files:
 * <pre>
 *   groovy.source.encoding
 * </pre>
 *
 * @see TemplateServlet#setVariables(ServletBinding)
 */
public class TemplateServlet extends AbstractHttpServlet {

    /**
     * Simple cache entry. If a file is supplied, then the entry is validated against
     * last modified and length attributes of the specified file.
     */
    private static class TemplateCacheEntry {

        final Date date;
        long hit;
        long lastModified;
        long length;
        final Template template;

        public TemplateCacheEntry(File file, Template template, boolean timestamp) {
            if (template == null) {
                throw new NullPointerException("template");
            }
            if (timestamp) {
                this.date = new Date(System.currentTimeMillis());
            } else {
                this.date = null;
            }
            this.hit = 0;
            if (file != null) {
                this.lastModified = file.lastModified();
                this.length = file.length();
            }
            this.template = template;
        }

        /**
         * Checks the passed file attributes against those cached ones.
         *
         * @param file Other file handle to compare to the cached values. May be null in which case the validation is skipped.
         * @return <code>true</code> if all measured values match, else <code>false</code>
         */
        public boolean validate(File file) {
            if (file != null) {
                if (file.lastModified() != this.lastModified) {
                    return false;
                }
                if (file.length() != this.length) {
                    return false;
                }
            }
            hit++;
            return true;
        }

        public String toString() {
            if (date == null) {
                return "Hit #" + hit;
            }
            return "Hit #" + hit + " since " + date;
        }
    }

    /**
     * Simple file name to template cache map.
     */
    private final Map<String, TemplateCacheEntry> cache;

    /**
     * Underlying template engine used to evaluate template source files.
     */
    private TemplateEngine engine;

    /**
     * Flag that controls the appending of the "Generated by ..." comment.
     */
    private boolean generateBy;

    private String fileEncodingParamVal;

    private static final String GROOVY_SOURCE_ENCODING = "groovy.source.encoding";

    /**
     * Create new TemplateServlet.
     */
    public TemplateServlet() {
        this.cache = new WeakHashMap<String, TemplateCacheEntry>();
        this.engine = null; // assigned later by init()
        this.generateBy = true; // may be changed by init()
        this.fileEncodingParamVal = null; // may be changed by init()
    }

    /**
     * Find a cached template for a given key. If a <code>File</code> is passed then
     * any cached object is validated against the File to determine if it is out of
     * date
     * @param key a unique key for the template, such as a file's absolutePath or a URL.
     * @param file a file to be used to determine if the cached template is stale. May be null.
     * @return The cached template, or null if there was no cached entry, or the entry was stale.
     */
    private Template findCachedTemplate(String key, File file) {
        Template template = null;

        /*
         * Test cache for a valid template bound to the key.
         */
        if (verbose) {
            log("Looking for cached template by key \"" + key + "\"");
        }
        
        TemplateCacheEntry entry = (TemplateCacheEntry) cache.get(key);
        if (entry != null) {
            if (entry.validate(file)) {
                if (verbose) {
                    log("Cache hit! " + entry);
                }
                template = entry.template;
            } else {
                if (verbose) {
                    log("Cached template " + key + " needs recompilation! " + entry);
                }
            }
        } else {
            if (verbose) {
                log("Cache miss for " + key);
            }
        }

        return template;
    }

    /**
     * Compile the template and store it in the cache.
     * @param key a unique key for the template, such as a file's absolutePath or a URL.
     * @param inputStream an InputStream for the template's source.
     * @param file a file to be used to determine if the cached template is stale. May be null.
     * @return the created template.
     * @throws Exception Any exception when creating the template.
     */
    private Template createAndStoreTemplate(String key, InputStream inputStream, File file) throws Exception {
        if (verbose) {
            log("Creating new template from " + key + "...");
        }

        Reader reader = null;

        try {
            String fileEncoding = (fileEncodingParamVal != null) ? fileEncodingParamVal :
                    System.getProperty(GROOVY_SOURCE_ENCODING);

            reader = fileEncoding == null ? new InputStreamReader(inputStream) : new InputStreamReader(inputStream, fileEncoding);
            Template template = engine.createTemplate(reader);

            cache.put(key, new TemplateCacheEntry(file, template, verbose));

            if (verbose) {
                log("Created and added template to cache. [key=" + key + "] " + cache.get(key));
            }

            //
            // Last sanity check.
            //
            if (template == null) {
                throw new ServletException("Template is null? Should not happen here!");
            }

            return template;
        } finally {
            if (reader != null) {
                reader.close();
            } else if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Gets the template created by the underlying engine parsing the request.
     *
     * <p>
     * This method looks up a simple (weak) hash map for an existing template
     * object that matches the source file. If the source file didn't change in
     * length and its last modified stamp hasn't changed compared to a precompiled
     * template object, this template is used. Otherwise, there is no or an
     * invalid template object cache entry, a new one is created by the underlying
     * template engine. This new instance is put to the cache for consecutive
     * calls.
     *
     * @return The template that will produce the response text.
     * @param file The file containing the template source.
     * @throws ServletException If the request specified an invalid template source file
     */
    protected Template getTemplate(File file) throws ServletException {
        String key = file.getAbsolutePath();
        Template template = findCachedTemplate(key, file);

        //
        // Template not cached or the source file changed - compile new template!
        //
        if (template == null) {
            try {
                template = createAndStoreTemplate(key, new FileInputStream(file), file);
            } catch (Exception e) {
                throw new ServletException("Creation of template failed: " + e, e);
            }
        }

        return template;
    }

    /**
     * Gets the template created by the underlying engine parsing the request.
     *
     * <p>
     * This method looks up a simple (weak) hash map for an existing template
     * object that matches the source URL. If there is no cache entry, a new one is
     * created by the underlying template engine. This new instance is put
     * to the cache for consecutive calls.
     *
     * @return The template that will produce the response text.
     * @param url The URL containing the template source..
     * @throws ServletException If the request specified an invalid template source URL
     */
    protected Template getTemplate(URL url) throws ServletException {
        String key = url.toString();
        Template template = findCachedTemplate(key, null);

        // Template not cached or the source file changed - compile new template!
        if (template == null) {
            try {
                template = createAndStoreTemplate(key, url.openConnection().getInputStream(), null);
            } catch (Exception e) {
                throw new ServletException("Creation of template failed: " + e, e);
            }

        }
        return template;
    }

    /**
     * Initializes the servlet from hints the container passes.
     * <p>
     * Delegates to sub-init methods and parses the following parameters:
     * <ul>
     * <li> <tt>"generatedBy"</tt> : boolean, appends "Generated by ..." to the
     * HTML response text generated by this servlet.
     * </li>
     * </ul>
     *
     * @param config Passed by the servlet container.
     * @throws ServletException if this method encountered difficulties
     * @see TemplateServlet#initTemplateEngine(ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.engine = initTemplateEngine(config);
        if (engine == null) {
            throw new ServletException("Template engine not instantiated.");
        }
        String value = config.getInitParameter("generated.by");
        if (value != null) {
            this.generateBy = Boolean.valueOf(value);
        }
        value = config.getInitParameter(GROOVY_SOURCE_ENCODING);
        if (value != null) {
            this.fileEncodingParamVal = value;
        }
        log("Servlet " + getClass().getName() + " initialized on " + engine.getClass());
    }

    /**
     * Creates the template engine.
     * <p>
     * Called by {@link TemplateServlet#init(ServletConfig)} and returns just
     * <code>new groovy.text.SimpleTemplateEngine()</code> if the init parameter
     * <code>template.engine</code> is not set by the container configuration.
     *
     * @param config Current servlet configuration passed by the container.
     * @return The underlying template engine or <code>null</code> on error.
     */
    protected TemplateEngine initTemplateEngine(ServletConfig config) {
        String name = config.getInitParameter("template.engine");
        if (name == null) {
            return new SimpleTemplateEngine();
        }
        try {
            return Class.forName(name).asSubclass(TemplateEngine.class).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            log("Could not instantiate template engine: " + name, e);
        } catch (IllegalAccessException e) {
            log("Could not access template engine class: " + name, e);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log("Could not find template engine class: " + name, e);
        }
        return null;
    }

    /**
     * Services the request with a response.
     * <p>
     * First the request is parsed for the source file uri. If the specified file
     * could not be found or can not be read an error message is sent as response.
     *
     * @param request  The http request.
     * @param response The http response.
     * @throws IOException      if an input or output error occurs while the servlet is handling the HTTP request
     * @throws ServletException if the HTTP request cannot be handled
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (verbose) {
            log("Creating/getting cached template...");
        }

        //
        // Get the template source file handle.
        //
        Template template;
        long getMillis;
        String name;
        
        File file = getScriptUriAsFile(request);
        if (file != null) {
            name = file.getName();
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return; // throw new IOException(file.getAbsolutePath());
            }
            if (!file.canRead()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Can not read \"" + name + "\"!");
                return; // throw new IOException(file.getAbsolutePath());
            }
            getMillis = System.currentTimeMillis();
            template = getTemplate(file);
            getMillis = System.currentTimeMillis() - getMillis;
        } else {
            name = getScriptUri(request);
            URL url = servletContext.getResource(name);
            if (url == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            getMillis = System.currentTimeMillis();
            template = getTemplate(url);
            getMillis = System.currentTimeMillis() - getMillis;
        }

        //
        // Create new binding for the current request.
        //
        ServletBinding binding = new ServletBinding(request, response, servletContext);
        setVariables(binding);

        //
        // Prepare the response buffer content type _before_ getting the writer.
        // and set status code to ok
        //
        response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding);
        response.setStatus(HttpServletResponse.SC_OK);

        //
        // Get the output stream writer from the binding.
        //
        Writer out = (Writer) binding.getVariable("out");
        if (out == null) {
            out = response.getWriter();
        }

        //
        // Evaluate the template.
        //
        if (verbose) {
            log("Making template \"" + name + "\"...");
        }
        // String made = template.make(binding.getVariables()).toString();
        // log(" = " + made);
        long makeMillis = System.currentTimeMillis();
        template.make(binding.getVariables()).writeTo(out);
        makeMillis = System.currentTimeMillis() - makeMillis;

        if (generateBy) {
            String sb = "\n<!-- Generated by Groovy TemplateServlet [create/get=" +
                    Long.toString(getMillis) +
                    " ms, make=" +
                    Long.toString(makeMillis) +
                    " ms] -->\n";
            out.write(sb);
        }

        //
        // flush the response buffer.
        //
        response.flushBuffer();

        if (verbose) {
            log("Template \"" + name + "\" request responded. [create/get=" + getMillis + " ms, make=" + makeMillis + " ms]");
        }
    }
}
