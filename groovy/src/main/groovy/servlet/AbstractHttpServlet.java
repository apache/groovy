/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.servlet;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * A common ground dealing with the HTTP servlet API wrinkles.
 * 
 * <h4>Resource name mangling (pattern replacement)</h4>
 * 
 * <p> 
 * Also implements Groovy's {@link groovy.util.ResourceConnector} in dynamic
 * manner. It allows to modifiy the resource name that is searched for with a
 * <i>replace all</i> operation. See {@link java.util.regex.Pattern} and
 * {@link java.util.regex.Matcher} for details.
 * The servlet init parameter names are:
 * <pre>
 * resource.name.regex = empty - defaults to null
 * resource.name.replacement = empty - defaults to null
 * resource.name.replace.all = true (default) | false means replaceFirst()
 * </pre>
 * Note: If you specify a regex, you have to specify a replacement string too!
 * Otherwise an exception gets raised.
 *
 * <h4>Logging and bug-hunting options</h4>
 *
 * <p> 
 * This implementation provides a verbosity flag switching log statements.
 * The servlet init parameter name is:
 * <pre>
 * verbose = false(default) | true
 * </pre>
 * 
 * <p> 
 * In order to support class-loading-troubles-debugging with Tomcat 4 or
 * higher, you can log the class loader responsible for loading some classes.
 * See <a href="http://jira.codehaus.org/browse/GROOVY-861">GROOVY-861</a> for details.
 * The servlet init parameter name is:
 * <pre>
 * log.GROOVY861 = false(default) | true
 * </pre>
 * 
 * <p> 
 * If you experience class-loading-troubles with Tomcat 4 (or higher) or any
 * other servlet container using custom class loader setups, you can fallback
 * to use (slower) reflection in Groovy's MetaClass implementation. Please
 * contact the dev team with your problem! Thanks.
 * The servlet init parameter name is:
 * <pre>
 * reflection = false(default) | true
 * </pre>
 * 
 *
 * @author Christian Stein
 */
public abstract class AbstractHttpServlet extends HttpServlet implements ResourceConnector {

    /**
     * Content type of the HTTP response.
     */
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    /**
     * Servlet API include key name: path_info
     */
    public static final String INC_PATH_INFO = "javax.servlet.include.path_info";

    /* *** Not used, yet. See comments in getScriptUri(HttpServletRequest). ***
     * Servlet API include key name: request_uri
     */
    public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";

    /**
     * Servlet API include key name: servlet_path
     */
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

    /**
     * Servlet (or the web application) context.
     */
    protected ServletContext servletContext;

    /**
     * <b>Null</b> or compiled pattern matcher read from "resource.name.regex"
     *  and used in {@link AbstractHttpServlet#getResourceConnection(String)}.
     */
    protected Matcher resourceNameMatcher;

    /**
     * The replacement used by the resource name matcher.
     */
    protected String resourceNameReplacement;

    /**
     * The replace method to use on the matcher.
     * <pre>
     * true - replaceAll(resourceNameReplacement); (default)
     * false - replaceFirst(resourceNameReplacement);
     * </pre>
     */
    protected boolean resourceNameReplaceAll;

    /**
     * Controls almost all log output.
     */
    protected boolean verbose;

    /**
     * Encoding to use, becomes charset part of contentType.
     */
    protected String encoding = "UTF-8";

    /**
     * Mirrors the static value of the reflection flag in MetaClass.
     * See AbstractHttpServlet#logGROOVY861
     */
    protected boolean reflection;

    /**
     * Debug flag logging the class the class loader of the request.
     */
    private boolean logGROOVY861;

    /**
     * Initializes all fields with default values.
     */
    public AbstractHttpServlet() {
        this.servletContext = null;
        this.resourceNameMatcher = null;
        this.resourceNameReplacement = null;
        this.resourceNameReplaceAll = true;
        this.verbose = false;
        this.reflection = false;
        this.logGROOVY861 = false;
    }

    /**
     * Interface method for ResourceContainer. This is used by the GroovyScriptEngine.
     */
    public URLConnection getResourceConnection(String name) throws ResourceException {
        /*
         * First, mangle resource name with the compiled pattern.
         */
        Matcher matcher = resourceNameMatcher;
        if (matcher != null) {
            matcher.reset(name);
            String replaced;
            if (resourceNameReplaceAll) {
                replaced = resourceNameMatcher.replaceAll(resourceNameReplacement);
            } else {
                replaced = resourceNameMatcher.replaceFirst(resourceNameReplacement);
            }
            if (!name.equals(replaced)) {
                if (verbose) {
                    log("Replaced resource name \"" + name + "\" with \"" + replaced + "\".");
                }
                name = replaced;
            }
        }

        /*
         * Try to locate the resource and return an opened connection to it.
         */
        try {
            URL url = servletContext.getResource("/" + name);
            if (url == null) {
                url = servletContext.getResource("/WEB-INF/groovy/" + name);
            }
            if (url == null) {
                throw new ResourceException("Resource \"" + name + "\" not found!");
            }
            return url.openConnection();
        } catch (IOException e) {
            throw new ResourceException("Problems getting resource named \"" + name + "\"!", e);
        }
    }

    /**
     * Returns the include-aware uri of the script or template file.
     * 
     * @param request
     *  the http request to analyze
     * @return the include-aware uri either parsed from request attributes or
     *  hints provided by the servlet container
     */
    protected String getScriptUri(HttpServletRequest request) {
        /*
         * Log some debug information for http://jira.codehaus.org/browse/GROOVY-861
         */
        if (logGROOVY861) {
            log("Logging request class and its class loader:");
            log(" c = request.getClass() :\"" + request.getClass() + "\"");
            log(" l = c.getClassLoader() :\"" + request.getClass().getClassLoader() + "\"");
            log(" l.getClass()           :\"" + request.getClass().getClassLoader().getClass() + "\"");
            /*
             * Keep logging, if we're verbose. Else turn it off.
             */
            logGROOVY861 = verbose;
        }

        //
        // NOTE: This piece of code is heavily inspired by Apaches Jasper2!
        // 
        // http://cvs.apache.org/viewcvs.cgi/jakarta-tomcat-jasper/jasper2/ \
        //        src/share/org/apache/jasper/servlet/JspServlet.java?view=markup
        //
        // Why doesn't it use request.getRequestURI() or INC_REQUEST_URI?
        //

        String uri = null;
        String info = null;

        //
        // Check to see if the requested script/template source file has been the
        // target of a RequestDispatcher.include().
        //
        uri = (String) request.getAttribute(INC_SERVLET_PATH);
        if (uri != null) {
            //
            // Requested script/template file has been target of 
            // RequestDispatcher.include(). Its path is assembled from the relevant
            // javax.servlet.include.* request attributes and returned!
            //
            info = (String) request.getAttribute(INC_PATH_INFO);
            if (info != null) {
                uri += info;
            }
            return uri;
        }

        //
        // Requested script/template file has not been the target of a 
        // RequestDispatcher.include(). Reconstruct its path from the request's
        // getServletPath() and getPathInfo() results.
        //
        uri = request.getServletPath();
        info = request.getPathInfo();
        if (info != null) {
            uri += info;
        }

        /*
         * TODO : Enable auto ".groovy" extension replacing here!
         * http://cvs.groovy.codehaus.org/viewrep/groovy/groovy/groovy-core/src/main/groovy/servlet/GroovyServlet.java?r=1.10#l259 
         */

        return uri;
    }

    /**
     * Parses the http request for the real script or template source file.
     * @param request the http request to analyze
     * @return a file object using an absolute file path name
     */
    protected File getScriptUriAsFile(HttpServletRequest request) {
        String uri = getScriptUri(request);
        String real = servletContext.getRealPath(uri);
        File file = new File(real).getAbsoluteFile();
        return file;
    }

    /**
     * Overrides the generic init method to set some debug flags.
     * 
     * @param config
     *  the servlet coniguration provided by the container
     * @throws ServletException if init() method defined in super class 
     *  javax.servlet.GenericServlet throws it
     */
    public void init(ServletConfig config) throws ServletException {
        /*
         * Never forget super.init()!
         */
        super.init(config);

        /*
         * Grab the servlet context.
         */
        this.servletContext = config.getServletContext();

        // Get verbosity hint.
        String value = config.getInitParameter("verbose");
        if (value != null) {
            this.verbose = Boolean.valueOf(value).booleanValue();
        }

        // get encoding
        value = config.getInitParameter("encoding");
        if (value != null) {
            this.encoding = value;
        }

        // And now the real init work...
        if (verbose) {
            log("Parsing init parameters...");
        }

        String regex = config.getInitParameter("resource.name.regex");
        if (regex != null) {
            String replacement = config.getInitParameter("resource.name.replacement");
            if (replacement == null) {
                Exception npex = new NullPointerException("resource.name.replacement");
                String message = "Init-param 'resource.name.replacement' not specified!";
                log(message, npex);
                throw new ServletException(message, npex);
            }
            int flags = 0; // TODO : Parse pattern compile flags.
            this.resourceNameMatcher = Pattern.compile(regex, flags).matcher("");
            this.resourceNameReplacement = replacement;
            String all = config.getInitParameter("resource.name.replace.all");
            if (all != null) {
                this.resourceNameReplaceAll = Boolean.valueOf(all).booleanValue();
            }
        }

        value = config.getInitParameter("logGROOVY861");
        if (value != null) {
            this.logGROOVY861 = Boolean.valueOf(value).booleanValue();
            // nothing else to do here
        }

        /*
         * If verbose, log the parameter values.
         */
        if (verbose) {
            log("(Abstract) init done. Listing some parameter name/value pairs:");
            log("verbose = " + verbose); // this *is* verbose! ;)
            log("reflection = " + reflection);
            log("logGROOVY861 = " + logGROOVY861);
            if (resourceNameMatcher != null) {
                log("resource.name.regex = " + resourceNameMatcher.pattern().pattern());
            }
            else {
                log("resource.name.regex = null");
            }
            log("resource.name.replacement = " + resourceNameReplacement);
        }
    }
}
