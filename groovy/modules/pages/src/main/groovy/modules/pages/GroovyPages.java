/*
 * Copyright 2003 (C) Troy Heninger. All Rights Reserved.
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
package groovy.modules.pages;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.modules.pages.Loader;
import org.codehaus.groovy.modules.pages.Parse;

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
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 10, 2004
 * Main servlet class.  Example usage in web.xml:
 * 	<servlet>
 *       <servlet-name>GroovyPages</servlet-name>
 *       <servlet-class>groovy.modules.pages.GroovyPages</servlet-class>
 *		<init-param>
 *			<param-name>allowSpilling</param-name>
 *			<param-value>1</param-value>
 *			<description>
 *             Allows developers to view the intermediade source code, when they pass
 *				a spillGroovy argument in the URL.
 *          </description>
 *		</init-param>
 *    </servlet>
 */

public class GroovyPages extends HttpServlet /*implements GroovyObject*/ {
	Object x;
	private ServletContext context;
	private boolean allowSpilling = false;

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

		allowSpilling = config.getInitParameter("allowSpilling") != null;
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

		boolean spillGroovy = allowSpilling && request.getParameter("spillGroovy") != null;
		PageMeta pageMeta = getPage(pageId, pageUrl, request.getServletPath(), spillGroovy);
		Writer out = GroovyWriter.getInstance(response, 8192);
		try {
			if (spillGroovy) {
					// Set it to TEXT
				response.setContentType("text/plain"); // must come before response.getOutputStream()
				send(pageMeta.groovySource, out);
				pageMeta.groovySource = null;
			} else {
		System.out.println("Start");
					// Set it to HTML by default
				response.setContentType("text/html"); // must come before response.getWriter()
				Binding binding = getBinding(request, response, out);
				Script page = InvokerHelper.createScript(pageMeta.servletScriptClass, binding);
//				page.setBinding(binding);
				page.run();
			}
		} finally {
			if (out != null) out.close();
	System.out.println("Done");
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
		binding.setVariable("request", new GroovyRequest(request));
		binding.setVariable("response", response);
		binding.setVariable("application", context);
		binding.setVariable("session", new GroovySession(request));
		binding.setVariable("out", out);

		// Form parameters. If there are multiple its passed as a list.
		for (Enumeration paramEnum = request.getParameterNames(); paramEnum.hasMoreElements();) {
			String key = (String) paramEnum.nextElement();
			if (binding.getVariable(key) == null) {
				String[] values = request.getParameterValues(key);
				if (values.length == 1) {
					binding.setVariable(key, values[0]);
				} else {
					binding.setVariable(key, values);
				}
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
		} catch (SyntaxException e) {
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
	 * @return
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
