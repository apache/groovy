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
package org.codehaus.groovy.grails.web.pages;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
import org.codehaus.groovy.grails.web.metaclass.GetParamsDynamicProperty;
import org.codehaus.groovy.grails.web.metaclass.GetSessionDynamicProperty;
import org.codehaus.groovy.grails.web.taglib.GrailsTagRegistry;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Main servlet class.  Example usage in web.xml:
 * 	<servlet>
 *       <servlet-name>GroovyPages</servlet-name>
 *       <servlet-class>org.codehaus.groovy.grails.web.pages.GroovyPages</servlet-class>
 *		<init-param>
 *			<param-name>showSource</param-name>
 *			<param-value>1</param-value>
 *			<description>
 *             Allows developers to view the intermediade source code, when they pass
 *				a showSource argument in the URL (eg /edit/list?showSource=true.
 *          </description>
 *		</init-param>
 *    </servlet>
 *
 * @author Troy Heninger
 * @author Graeme Rocher
 * Date: Jan 10, 2004
 *
 */
public class GroovyPages extends HttpServlet /*implements GroovyObject*/ {
	Object x;
	private ServletContext context;
	private boolean showSource = false;

	private static Map pageCache = Collections.synchronizedMap(new HashMap());
	private static ClassLoader parent;

	/**
	 * @return the servlet context
	 */
	public ServletContext getServletContext() { return context; }

	private static class PageMeta {
		private Class servletScriptClass;
		private long lastModified;
		private Map dependencies = new HashMap();
		private InputStream groovySource;
	} // PageMeta

	/**
	 * Initialize the servlet, set it's parameters.
	 * @param config servlet settings
	 */
	public void init(ServletConfig config) {
		// Get the servlet context
		context = config.getServletContext();
		context.log("Groovy servlet initialized");

		// Ensure that we use the correct classloader so that we can find
		// classes in an application server.
		parent = Thread.currentThread().getContextClassLoader();
		if (parent == null) parent = getClass().getClassLoader();

		showSource = config.getInitParameter("showSource") != null;
	} // init()

	/**
	 * Handle HTTP GET requests.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPage(request, response);
	} // doGet()

	/**
	 * Handle HTTP POST requests.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPage(request, response);
	} // doPost()

	/**
	 * Execute page and produce output.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pageId = getPageId(request);
		URL pageUrl = getPageUrl(pageId);
		if (pageUrl == null) {
			context.log("GroovyPages:  \"" + pageUrl + "\" not found");
			response.sendError(404, "\"" + pageUrl + "\" not found.");
			return;
		}

		boolean spillGroovy = showSource && request.getParameter("spillGroovy") != null;
		PageMeta pageMeta = getPage(pageId, pageUrl, request.getServletPath(), spillGroovy);
		Writer out = GroovyWriter.getInstance(response, 8192);
		try {
			if (spillGroovy) {
					// Set it to TEXT
				response.setContentType("text/plain"); // must come before response.getOutputStream()
				send(pageMeta.groovySource, out);
				pageMeta.groovySource = null;
			} else {
				// Set it to HTML by default
				response.setContentType("text/html"); // must come before response.getWriter()
				Binding binding = getBinding(request, response, out);
				Script page = InvokerHelper.createScript(pageMeta.servletScriptClass, binding);
				page.run();
			}
		} finally {
			if (out != null) out.close();
		}
	} // doPage()

	/**
	 * Prepare Bindings before instantiating page.
	 * @param request
	 * @param response
	 * @param out
	 * @return the Bindings
	 * @throws IOException
	 */
	protected Binding getBinding(HttpServletRequest request, HttpServletResponse response, Writer out)
	        throws IOException {
		// Set up the script context
		Binding binding = new Binding();
        GroovyObject controller = (GroovyObject)request.getAttribute(GrailsControllerClass.REQUEST_CONTROLLER);
        binding.setVariable("request", controller.getProperty(ControllerDynamicMethods.REQUEST_PROPERTY));
		binding.setVariable("response", controller.getProperty(ControllerDynamicMethods.RESPONSE_PROPERTY));
		binding.setVariable("application", context);
		binding.setVariable("session", controller.getProperty(GetSessionDynamicProperty.PROPERTY_NAME));
        binding.setVariable("params", controller.getProperty(GetParamsDynamicProperty.PROPERTY_NAME));
        binding.setVariable("out", out);
        binding.setVariable("grailsTagRegistry", GrailsTagRegistry.getInstance());

        // Go through request attributes and add them to the binding as the model
        for (Enumeration attributeEnum =  request.getAttributeNames(); attributeEnum.hasMoreElements();) {
			String key = (String) attributeEnum.nextElement();
            try {
                binding.getVariable(key);
            }
            catch(MissingPropertyException mpe) {
                binding.setVariable( key, request.getAttribute(key) ); 
            }
		}
		return binding;
	} // getBinding()

	/**
	 * Lookup page class or load new one if needed.
	 * @param pageId
	 * @param pageUrl
	 * @param servletPath
	 * @param spillGroovy
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	protected PageMeta getPage(String pageId, URL pageUrl, String servletPath, boolean spillGroovy)
	        throws IOException, ServletException  {
			// Lock on the pageId to ensure that only one compile occurs for any script
		synchronized (pageId) {
				// Get the URLConnection
			URLConnection groovyScriptConn = pageUrl.openConnection();
				// URL last modified
			long lastModified = groovyScriptConn.getLastModified();
				// Check the cache for the script
			PageMeta pageMeta = (PageMeta)pageCache.get(pageId);
				// If the pageMeta isn't null check all the dependencies
			boolean dependencyOutOfDate = false;
			if (pageMeta != null && !spillGroovy) {
				isPageNew(pageMeta);
			}
			if (pageMeta == null || pageMeta.lastModified < lastModified || dependencyOutOfDate || spillGroovy) {
				pageMeta = newPage(pageId, servletPath, groovyScriptConn, lastModified, spillGroovy);
			}
			return pageMeta;
		}
	} // getPage()

	/**
	 * Is page new or changed?
	 * @param pageMeta page data
	 * @return true if compile needed
	 */
	private boolean isPageNew(PageMeta pageMeta) {
		for (Iterator i = pageMeta.dependencies.keySet().iterator(); i.hasNext(); ) {
			URLConnection urlc = null;
			URL url = (URL)i.next();
			try {
				urlc = url.openConnection();
				urlc.setDoInput(false);
				urlc.setDoOutput(false);
				long dependentLastModified = urlc.getLastModified();
				if (dependentLastModified > ((Long)pageMeta.dependencies.get(url)).longValue()) {
					return true;
				}
			} catch (IOException ioe) {
				return true;
			}
		}
		return false;
	} // isPageNew()

	/**
	 * Load and compile new page.
	 * @param pageId
	 * @param servletPath
	 * @param groovyScriptConn
	 * @param lastModified
	 * @param spillGroovy
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	private PageMeta newPage(String pageId, String servletPath, URLConnection groovyScriptConn, long lastModified,
	                         boolean spillGroovy) throws IOException, ServletException {
		Parse parse = new Parse(pageId, groovyScriptConn.getInputStream());
		InputStream in = parse.parse();

			// Make a new pageMeta
		PageMeta pageMeta = new PageMeta();

			// just return groovy and don't compile if asked
		if (spillGroovy) {
			pageMeta.groovySource = in;
			return pageMeta;
		}
			// Compile the script into an object
		GroovyClassLoader groovyLoader = new Loader(parent, context, servletPath, pageMeta.dependencies);
		Class scriptClass;
		try {
			scriptClass =
				groovyLoader.parseClass(in, pageId.substring(1));
		} catch (CompilationFailedException e) {
			throw new ServletException("Could not parse script: " + pageId, e);
		}
		pageMeta.servletScriptClass = scriptClass;
		pageMeta.lastModified = lastModified;
		pageCache.put(pageId, pageMeta);
		return pageMeta;
	} // newPage()

	/**
	 * Return the page identifier.
	 * @param request
	 * @return The page id
	 */
	protected String getPageId(HttpServletRequest request) {
		// Get the name of the Groovy script (intern the name so that we can
		// lock on it)
		int contextLength = request.getContextPath().length();
		return request.getRequestURI().substring(contextLength).intern();
	} // getPageId()

	/**
	 * Return the page URL from the request path.
	 * @param pageId
	 * @return
	 * @throws MalformedURLException
	 */
	protected URL getPageUrl(String pageId) throws MalformedURLException {
		// Check to make sure that the file exists in the web application
		return context.getResource(pageId);
	} // getPageUrl()

	/**
	 * Copy all of input to output.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void send(InputStream in, Writer out) throws IOException {
		try {
			Reader reader = new InputStreamReader(in);
			char[] buf = new char[8192];
			for (;;) {
				int read = reader.read(buf);
				if (read <= 0) break;
				out.write(buf, 0, read);
			}
		} finally {
			out.close();
			in.close();
		}
	} // send()
	
} // GroovyPages
