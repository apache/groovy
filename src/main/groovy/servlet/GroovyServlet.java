/*
 * Created on Nov 21, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package groovy.servlet;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.lang.ScriptContext;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Collections;
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

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * This servlet should be registered to *.groovy in the web.xml.
 * 
 * <servlet><servlet-name>Groovy</servlet-name><servlet-class>
 * groovy.servlet.GroovyServlet</servlet-class></servlet>
 * 
 * <servlet-mapping><servlet-name>Groovy</servlet-name><url-pattern>
 * *.groovy</url-pattern></servlet-mapping>
 * 
 * @author Sam Pullara
 */
public class GroovyServlet extends HttpServlet {

	private ServletContext sc;

	private static Map servletCache = Collections.synchronizedMap(new HashMap());
	private static ClassLoader parent;

	private static class ServletCacheEntry {
		private Class servletScriptClass;
		private long lastModified;
	}

	public void init(ServletConfig config) {
		// Get the servlet context
		sc = config.getServletContext();
		sc.log("Groovy servlet initialized");

		// Ensure that we use the correct classloader so that we can find
		// classes in an application server.
		parent = Thread.currentThread().getContextClassLoader();
		if (parent == null)
			parent = GroovyServlet.class.getClassLoader();
	}

	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

		// Convert the generic servlet request and response to their Http
		// versions
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Get the name of the Groovy script (intern the name so that we can
		// lock on it)
		int contextLength = httpRequest.getContextPath().length();
		String scriptFilename = httpRequest.getRequestURI().substring(contextLength).intern();

		// Check to make sure that the file exists in the web application
		URL groovyScriptURL = sc.getResource(scriptFilename);
		if (groovyScriptURL == null) {
			sc.log("Groovy script " + scriptFilename + " not found");
			httpResponse.sendError(404);
			return;
		}

		// Set up the script context
		ScriptContext binding = new ScriptContext();
		binding.setVariable("request", httpRequest);
		binding.setVariable("response", httpResponse);
		binding.setVariable("application", sc);
		binding.setVariable("session", httpRequest.getSession(true));
		binding.setVariable("out", httpResponse.getWriter());

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

		// Lock on the scriptFilename to ensure that only one compile occurs
		// for any script
		ServletCacheEntry entry;
		synchronized (scriptFilename) {
			// Get the URLConnection
			URLConnection groovyScriptConn = groovyScriptURL.openConnection();
			// URL last modified
			long lastModified = groovyScriptConn.getLastModified();
			// Check the cache for the script
			entry = (ServletCacheEntry) servletCache.get(scriptFilename);
			if (entry == null || entry.lastModified < lastModified) {
				// Compile the script into an object
				GroovyClassLoader groovyLoader = new GroovyClassLoader(parent);
				Class scriptClass;
				try {
					scriptClass =
						groovyLoader.parseClass(groovyScriptConn.getInputStream(), scriptFilename.substring(1));
				} catch (SyntaxException e) {
					throw new ServletException("Could not parse script: " + scriptFilename, e);
				}
				entry = new ServletCacheEntry();
				entry.servletScriptClass = scriptClass;
				entry.lastModified = lastModified;
				servletCache.put(scriptFilename, entry);
			}
		}

		// Set it to HTML by default
		response.setContentType("text/html");

		// Execute the script
		Script script = InvokerHelper.createScript(entry.servletScriptClass, binding);
		script.run();
	}

}
