package org.codehaus.groovy.modules.pages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.ServletContext;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 10, 2004
 * Class loader that knows about loading from a servlet context and about class dependancies.
 */
public class Loader extends GroovyClassLoader {

	private String servletPath;
	private ServletContext context;
	private Map dependencies;

	/**
	 * Constructor.
	 * @param parent
	 * @param context
	 * @param servletPath
	 * @param dependencies
	 */
	public Loader(ClassLoader parent, ServletContext context, String servletPath, Map dependencies) {
		super(parent);
		this.context = context;
		this.servletPath = servletPath;
		this.dependencies = dependencies;
	} // Loader()

	/**
	 * Load the class.
	 * @todo Fix this to work with .gsp extensions
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected Class findClass(String className) throws ClassNotFoundException {
		String filename = className.replace('.', File.separatorChar) + ".groovy";
		URL dependentScript;
		try {
			dependentScript = context.getResource("/WEB-INF/groovy/" + filename);
			if (dependentScript == null) {
				String current = servletPath.substring(0, servletPath.lastIndexOf("/") + 1);
				dependentScript = context.getResource(current + filename);
			}
		} catch (MalformedURLException e) {
			throw new ClassNotFoundException(className + ": " + e);
		}
		if (dependentScript == null) {
			throw new ClassNotFoundException("Could not find " + className + " in webapp");
		} else {
			URLConnection dependentScriptConn;
			try {
				dependentScriptConn = dependentScript.openConnection();
				dependencies.put(dependentScript, new Long(dependentScriptConn.getLastModified()));
			} catch (IOException e1) {
				throw new ClassNotFoundException("Could not read " + className + ": " + e1);
			}
			try {
				return parseClass(dependentScriptConn.getInputStream(), filename);
			} catch (SyntaxException e2) {
				throw new ClassNotFoundException("Syntax error in " + className + ": " + e2);
			} catch (IOException e2) {
				throw new ClassNotFoundException("Problem reading " + className + ": " + e2);
			}
		}
	}
}
