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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Class loader that knows about loading from a servlet context and about class dependancies.
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
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
            } catch (CompilationFailedException e2) {
                throw new ClassNotFoundException("Syntax error in " + className + ": " + e2);
            } catch (IOException e2) {
                throw new ClassNotFoundException("Problem reading " + className + ": " + e2);
            }
        }
    }
}
